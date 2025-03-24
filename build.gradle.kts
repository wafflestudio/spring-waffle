import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Properties

plugins {
    id("org.springframework.boot") version "3.2.4" apply false
    id("io.spring.dependency-management") version "1.1.4"
    kotlin("jvm") version "1.8.0"
    kotlin("plugin.spring") version "1.8.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
    id("maven-publish")
}

java.sourceCompatibility = JavaVersion.VERSION_17

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
        withSourcesJar()
        withJavadocJar()
    }

    dependencyManagement {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.4")
        }
    }

    dependencies {
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
                val authToken = properties["codeArtifactAuthToken"] as String? ?: ProcessBuilder(
                    "aws", "codeartifact", "get-authorization-token",
                    "--domain", "wafflestudio", "--domain-owner", "405906814034",
                    "--query", "authorizationToken", "--region", "ap-northeast-1", "--output", "text"
                ).start().inputStream.bufferedReader().readText().trim()
                url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/spring-waffle/")
                credentials {
                    username = "aws"
                    password = authToken
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
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}

task("updateVersion") {
    properties["releaseVersion"]?.let { releaseVersion ->
        val newSnapshotVersion = (releaseVersion as String).split(".").let {
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
