group = "com.wafflestudio.spring.truffle"

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":truffle:truffle-core"))
    implementation(project(":truffle:truffle-logback"))
}
