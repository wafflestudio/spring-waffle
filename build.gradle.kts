import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    id("org.springframework.boot") version "3.5.11" apply false
    kotlin("jvm") version "2.2.0"
    kotlin("plugin.spring") version "2.2.0"
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
    id("maven-publish")
}

allprojects {
    repositories {
        mavenCentral()
        mavenLocal()
    }

    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("io.spring.dependency-management")
        plugin("org.jlleitschuh.gradle.ktlint")
        plugin("org.gradle.maven-publish")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        withSourcesJar()
        withJavadocJar()
    }

    dependencies {
        implementation(platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES))

        implementation(kotlin("stdlib"))
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")

        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("org.springframework.boot:spring-boot-starter-web")
        testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    }

    publishing {
        repositories {
            maven {
                url = uri("https://maven.pkg.github.com/wafflestudio/spring-waffle")
                credentials {
                    username = "wafflestudio"
                    password = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN") ?: ""
                }
            }
        }

        publications {
            register<MavenPublication>("spring-waffle") {
                from(components["java"])
            }
        }
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict")
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

tasks.register("updateVersion") {
    properties["releaseVersion"]?.let { releaseVersion ->
        val newSnapshotVersion =
            (releaseVersion as String).split(".").let {
                "${it[0]}.${it[1].toInt() + 1}.0-SNAPSHOT"
            }

        val file = File(rootDir, "gradle.properties")
        val prop = Properties().apply { load(FileInputStream(file)) }
        if (prop.getProperty("version") != newSnapshotVersion) {
            prop.setProperty("version", newSnapshotVersion)
            prop.store(FileOutputStream(file), null)
        }
    }
}
