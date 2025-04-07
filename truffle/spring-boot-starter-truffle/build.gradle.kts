group = "com.wafflestudio.spring.truffle"

dependencies {
    implementation("org.springframework.boot:spring-boot")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    implementation(project(":truffle:truffle-core"))
    implementation(project(":truffle:truffle-logback"))
}
