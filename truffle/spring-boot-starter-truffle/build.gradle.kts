group = "com.wafflestudio.spring.truffle"

dependencies {
    implementation("org.springframework.boot:spring-boot")

    implementation(project(":truffle:truffle-core"))
    implementation(project(":truffle:truffle-logback"))
}
