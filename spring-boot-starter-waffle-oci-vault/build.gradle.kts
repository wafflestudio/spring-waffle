group = "com.wafflestudio.spring"

dependencies {
    implementation("org.springframework.boot:spring-boot")

    // OCI Vault (Secrets) access via OCI Java SDK.
    implementation("com.oracle.oci.sdk:oci-java-sdk-secrets:3.80.1")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3:3.80.1") {
        // Avoid pulling HK2 injection implementation transitively.
        exclude(group = "org.glassfish.jersey.inject", module = "jersey-hk2")
    }

    testImplementation(kotlin("test"))
}
