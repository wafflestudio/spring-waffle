group = "com.wafflestudio.spring"
dependencies {
    implementation("org.springframework.boot:spring-boot")

    implementation("software.amazon.awssdk:secretsmanager:2.20.66")
    implementation("software.amazon.awssdk:sts:2.20.66")

    testImplementation(kotlin("test"))
}
