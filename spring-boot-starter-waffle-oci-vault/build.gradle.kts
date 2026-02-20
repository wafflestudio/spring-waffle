group = "com.wafflestudio.spring"

dependencies {
    implementation("org.springframework.boot:spring-boot")

    implementation("com.oracle.oci.sdk:oci-java-sdk-secrets:3.80.1")
    implementation("com.oracle.oci.sdk:oci-java-sdk-common-httpclient-jersey3:3.80.1")

    testImplementation(kotlin("test"))
}
