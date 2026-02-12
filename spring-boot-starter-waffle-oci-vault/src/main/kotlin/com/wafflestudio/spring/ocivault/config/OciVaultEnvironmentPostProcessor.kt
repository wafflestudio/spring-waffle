package com.wafflestudio.spring.ocivault.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.oracle.bmc.Region
import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider
import com.oracle.bmc.secrets.SecretsClient
import com.oracle.bmc.secrets.model.Base64SecretBundleContentDetails
import com.oracle.bmc.secrets.requests.GetSecretBundleRequest
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import java.util.Base64

class OciVaultEnvironmentPostProcessor : EnvironmentPostProcessor {
    private val objectMapper = jacksonObjectMapper()

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
        val region =
            Region.fromRegionId(
                environment.getProperty("oci.vault.region", "ap-chuncheon-1"),
            )

        val authProvider = createAuthProvider(environment)
        val client = SecretsClient.builder().region(region).build(authProvider)
        val secrets = mutableMapOf<String, Any>()

        try {
            secretIds.forEach { secretId ->
                val secretString = getSecretString(client, secretId)
                val parsedSecrets = objectMapper.readValue<Map<String, Any>>(secretString)
                secrets.putAll(
                    parsedSecrets.filterKeys {
                        environment.getProperty(it).isNullOrEmpty()
                    },
                )
            }
        } finally {
            client.close()
        }

        if (secrets.isNotEmpty()) {
            environment.propertySources.addFirst(
                MapPropertySource("oci-vault-secrets", secrets),
            )
        }
    }

    private fun createAuthProvider(environment: ConfigurableEnvironment): BasicAuthenticationDetailsProvider {
        return ConfigFileAuthenticationDetailsProvider("DEFAULT")
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
