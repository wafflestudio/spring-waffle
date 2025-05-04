package com.wafflestudio.spring.secretsmanager.config

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.MapPropertySource
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest

class SecretsManagerEnvironmentPostProcessor : EnvironmentPostProcessor {
    private val objectMapper = jacksonObjectMapper()
    private val region = Region.AP_NORTHEAST_2

    override fun postProcessEnvironment(
        environment: ConfigurableEnvironment,
        application: SpringApplication,
    ) {
        val secretNamesProperty = environment.getProperty("secret-names") ?: return
        val secretNames = secretNamesProperty.split(",")
        val secrets = mutableMapOf<String, Any>()

        secretNames.forEach { secretName ->
            val secretString = getSecretString(secretName)
            val parsedSecrets = objectMapper.readValue<Map<String, Any>>(secretString)
            secrets.putAll(
                parsedSecrets.filterKeys {
                    environment.getProperty(it).isNullOrEmpty()
                },
            )
        }

        // Add directly to environment
        if (secrets.isNotEmpty()) {
            environment.propertySources.addFirst(
                MapPropertySource("aws-secrets", secrets),
            )
        }
    }

    private fun getSecretString(secretName: String): String {
        val client = SecretsManagerClient.builder().region(region).build()
        val request = GetSecretValueRequest.builder().secretId(secretName).build()
        return client.getSecretValue(request).secretString()
    }
}
