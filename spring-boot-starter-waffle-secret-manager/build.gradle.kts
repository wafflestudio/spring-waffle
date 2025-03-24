group = "com.wafflestudio.spring"
dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    implementation("software.amazon.awssdk:secretsmanager:2.20.66")
    implementation("software.amazon.awssdk:sts:2.20.66")

    testImplementation(kotlin("test"))
}
