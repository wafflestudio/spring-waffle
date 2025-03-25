group = "com.wafflestudio.spring"
dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    compileOnly("software.amazon.awssdk:secretsmanager:2.20.66")
    compileOnly("software.amazon.awssdk:sts:2.20.66")

    testImplementation(kotlin("test"))
}
