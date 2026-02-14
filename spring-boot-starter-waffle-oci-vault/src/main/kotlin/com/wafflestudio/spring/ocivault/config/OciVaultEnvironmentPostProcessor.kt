package com.wafflestudio.spring.ocivault.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.oracle.bmc.ConfigFileReader
import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.auth.InstancePrincipalsAuthenticationDetailsProvider
import com.oracle.bmc.secrets.SecretsClient
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest
import org.springframework.boot.EnvironmentPostProcessor
import org.springframework.boot.SpringApplication
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import org.slf4j.LoggerFactory
import org.springframework.core.env.getProperty
import java.util.Base64

class OciVaultEnvironmentPostProcessor : EnvironmentPostProcessor {
    private val log = LoggerFactory.getLogger(javaClass)
    private val objectMapper = jacksonObjectMapper()

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val isAotProcessing = environment.getProperty<Boolean>("spring.aot.processing", false)
        if (isAotProcessing) {
            return
        }
        val secretIdsProperty = environment.getProperty("oci.vault.secret-ids") ?: return
        val secretIds = secretIdsProperty.split(",").map { it.trim() }
        val region =
            Region.fromRegionId(
                environment.getProperty("oci.vault.region", "ap-chuncheon-1"),
            )

        val authProvider = createAuthProvider(environment)
        val client = SecretsClient.builder().region(region).build(authProvider)
        val secrets = mutableMapOf<String, Any>()

        client.use { client ->
            secretIds.forEach { secretId ->
                val secretString = getSecretString(client, secretId)
                val parsedSecrets = objectMapper.readValue<Map<String, Any>>(secretString)
                secrets.putAll(
                    parsedSecrets.filterKeys {
                        environment.getProperty(it).isNullOrEmpty()
                    },
                )
            }
        }

        if (secrets.isNotEmpty()) {
            environment.propertySources.addFirst(
                MapPropertySource("oci-vault-secrets", secrets),
            )
        }
    }

    private fun createAuthProvider(environment: ConfigurableEnvironment): BasicAuthenticationDetailsProvider {
        // Default to `auto` so apps "just work" on OCI (Instance Principals) and locally (config file fallback).
        return when (val authType = environment.getProperty("oci.auth.type", "auto").trim().lowercase()) {
            "auto" -> {
                try {
                    InstancePrincipalsAuthenticationDetailsProvider.builder().build()
                } catch (e: Exception) {
                    log.info("OCI instance principal auth failed; falling back to config file auth (oci.auth.type=auto).", e)
                    createConfigAuthProvider(environment)
                }
            }

            "config",
            "configfile",
            "config_file",
            "config-file",
            -> createConfigAuthProvider(environment)

            "instance_principal",
            "instanceprincipal",
            "instance-principal",
            "ip",
            -> InstancePrincipalsAuthenticationDetailsProvider.builder().build()

            else ->
                throw IllegalArgumentException(
                    "Unsupported oci.auth.type='$authType'. Supported: config, instance_principal, auto",
                )
        }
    }

    private fun createConfigAuthProvider(environment: ConfigurableEnvironment): BasicAuthenticationDetailsProvider {
        val profile = environment.getProperty("oci.config.profile", "DEFAULT").trim().ifEmpty { "DEFAULT" }
        val configPath =
            environment.getProperty("oci.config.path")
                ?.trim()
                ?.ifEmpty { null }
                ?.let { expandHome(it) }

        if (configPath == null) {
            return ConfigFileAuthenticationDetailsProvider(profile)
        }

        val configFile = ConfigFileReader.parse(configPath, profile)
        return ConfigFileAuthenticationDetailsProvider(configFile)
    }

    private fun expandHome(path: String): String {
        val home = System.getProperty("user.home")
        return if (path == "~") home else path.replaceFirst(Regex("^~(?=/|$)"), home)
    }

    private fun getSecretString(
        client: SecretsClient,
        secretId: String,
    ): String {
        val request =
            GetSecretBundleRequest.builder()
                .secretId(secretId)
                .build()
        val response = client.getSecretBundle(request)
        val content = (response.secretBundle.secretBundleContent as Base64SecretBundleContentDetails).content
        return String(Base64.getDecoder().decode(content))
    }
}
