group = "com.wafflestudio.spring.truffle.logback"

dependencies {
    compileOnly("ch.qos.logback:logback-classic")

    implementation(project(":truffle:truffle-core"))
}
