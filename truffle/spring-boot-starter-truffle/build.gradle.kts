group = "com.wafflestudio.spring.truffle"

plugins {
    kotlin("kapt")
}

apply {
    plugin("kotlin-allopen")
    plugin("kotlin-spring")
}

dependencies {
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    compileOnly(project(":truffle:truffle-core"))
    implementation(project(":truffle:truffle-logback"))
}
