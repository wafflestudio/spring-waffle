group = "com.wafflestudio.spring"
dependencies {
    implementation("org.springframework.boot:spring-boot")

    implementation("software.amazon.awssdk:secretsmanager:2.25.15")
    implementation("software.amazon.awssdk:sts:2.25.15")

    testImplementation(kotlin("test"))
}
