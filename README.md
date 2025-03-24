# waffle-spring
## 와플스튜디오 스프링 라이브러리
- 와플스튜디오에서 사용하는 스프링 라이브러리 모음

## 사용법
### spring 연동
- `build.gradle.kts` 혹은 `build.gradle` 파일에 아래와 같이 추가
    - build.gradle.kts
      ```kotlin
      repository {
        // ...
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
      // ...
      dependencies {
        // ...
        implementation("com.wafflestudio.spring:spring-boot-starter-waffle:1.0.0")
      }
      ```
    - build.gradle
      ```groovy
      import java.io.BufferedReader
      import java.io.InputStreamReader

      def getAuthToken() {
        def process = new ProcessBuilder(
          "aws", "codeartifact", "get-authorization-token",
          "--domain", "wafflestudio", "--domain-owner", "405906814034",
          "--query", "authorizationToken", "--region", "ap-northeast-1", "--output", "text"
        ).start()
        process.inputStream.withCloseable { input ->
          return new BufferedReader(new InputStreamReader(input)).readText().trim()
        }
      }

      repositories {
        // ...
        maven {
          def authToken = project.findProperty("codeArtifactAuthToken") ?: getAuthToken()
          url = uri("https://wafflestudio-405906814034.d.codeartifact.ap-northeast-1.amazonaws.com/maven/spring-waffle/")
          credentials {
            username = "aws"
            password = authToken
          }
        }
      }
      // ...
      dependencies {
        // ...
        implementation 'com.wafflestudio.spring:spring-boot-starter-waffle:1.0.0'
      }
      ```
## 기능 목록
- [secret-manager](./spring-boot-starter-waffle-secret-manager)
    - secrets-name 을 통해 AWS Secret Manager 에서 secret 을 가져오는 라이브러리
- [truffle-kotlin](./truffle)
    - 와플스튜디오 slack 채널에 로그를 전송하는 라이브러리
#### 기능별 사용법은 링크 참조

