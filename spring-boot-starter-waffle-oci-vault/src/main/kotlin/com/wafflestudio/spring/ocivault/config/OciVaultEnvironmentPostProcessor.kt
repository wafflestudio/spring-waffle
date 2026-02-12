package com.wafflestudio.spring.ocivault.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.file.Files
import java.nio.file.Path
import java.security.KeyFactory
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64

class OciVaultEnvironmentPostProcessor : EnvironmentPostProcessor {
    private val objectMapper = jacksonObjectMapper()
    private val httpClient = HttpClient.newHttpClient()

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val isAotProcessing = environment.getProperty("spring.aot.processing", Boolean::class.java, false)
        if (isAotProcessing) {
            return
        }
        val secretIdsProperty = environment.getProperty("oci-vault-secret-ids") ?: return
        val secretIds = secretIdsProperty.split(",").map { it.trim() }
        val region = environment.getProperty("oci.vault.region", "ap-chuncheon-1")

        val ociConfig = loadOciConfig()
        val secrets = mutableMapOf<String, Any>()

        secretIds.forEach { secretId ->
            val secretString = getSecretString(ociConfig, region, secretId)
            val parsedSecrets = objectMapper.readValue<Map<String, Any>>(secretString)
            secrets.putAll(
                parsedSecrets.filterKeys {
                    environment.getProperty(it).isNullOrEmpty()
                },
            )
        }

        if (secrets.isNotEmpty()) {
            environment.propertySources.addFirst(
                MapPropertySource("oci-vault-secrets", secrets),
            )
        }
    }

    private fun getSecretString(
        config: OciConfig,
        region: String,
        secretId: String,
    ): String {
        val host = "secrets.vaults.$region.oci.oraclecloud.com"
        val path = "/20190301/secretbundles/$secretId"
        val date = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneOffset.UTC))

        val signingString = "(request-target): get $path\ndate: $date\nhost: $host"
        val signature = sign(config.privateKey, signingString)
        val keyId = "${config.tenancy}/${config.user}/${config.fingerprint}"
        val authHeader =
            """Signature version="1",keyId="$keyId",algorithm="rsa-sha256",headers="(request-target) date host",signature="$signature""""

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create("https://$host$path"))
                .header("date", date)
                .header("authorization", authHeader)
                .GET()
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() != 200) {
            throw RuntimeException("OCI Vault API error (${response.statusCode()}): ${response.body()}")
        }

        val body = objectMapper.readValue<Map<String, Any>>(response.body())

        @Suppress("UNCHECKED_CAST")
        val secretBundleContent =
            body["secretBundleContent"] as Map<String, Any>
        val content = secretBundleContent["content"] as String
        return String(Base64.getDecoder().decode(content))
    }

    private fun sign(
        privateKeyPem: String,
        signingString: String,
    ): String {
        val pemContent =
            privateKeyPem
                .substringAfter("-----\n")
                .substringBefore("\n-----")
                .replace("\\s".toRegex(), "")
        val keyBytes = Base64.getDecoder().decode(pemContent)
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        val key = keyFactory.generatePrivate(keySpec)
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(key)
        signature.update(signingString.toByteArray())
        return Base64.getEncoder().encodeToString(signature.sign())
    }

    private fun loadOciConfig(): OciConfig {
        val configPath = Path.of(System.getProperty("user.home"), ".oci", "config")
        val lines = Files.readAllLines(configPath)
        val props = mutableMapOf<String, String>()
        var inDefaultProfile = false

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed == "[DEFAULT]") {
                inDefaultProfile = true
                continue
            }
            if (trimmed.startsWith("[")) {
                inDefaultProfile = false
                continue
            }
            if (inDefaultProfile && trimmed.contains("=")) {
                val (key, value) = trimmed.split("=", limit = 2)
                props[key.trim()] = value.trim()
            }
        }

        val keyFilePath =
            props["key_file"]?.replace("~", System.getProperty("user.home"))
                ?: throw RuntimeException("key_file not found in OCI config")

        return OciConfig(
            tenancy = props["tenancy"] ?: throw RuntimeException("tenancy not found in OCI config"),
            user = props["user"] ?: throw RuntimeException("user not found in OCI config"),
            fingerprint = props["fingerprint"] ?: throw RuntimeException("fingerprint not found in OCI config"),
            privateKey = Files.readString(Path.of(keyFilePath)),
        )
    }

    private data class OciConfig(
        val tenancy: String,
        val user: String,
        val fingerprint: String,
        val privateKey: String,
    )
}
