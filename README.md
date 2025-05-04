# waffle-spring
## 와플스튜디오 스프링 라이브러리
- 와플스튜디오에서 사용하는 스프링 라이브러리 모음

## 사용법
### AWS 설정
- AWS 콘솔에 로그인
- 액세스 키 발급
    - IAM 탭 이동
    - 사용자(User) 메뉴 선택
    - 본인 계정(혹은 원하는 사용자)을 클릭.
    - 보안 자격 증명(Security credentials) 탭 클릭
    - 새 액세스 키 생성
    - AWS 에서 주는 aws_access_key_id, aws_secret_access_key 를 저장
#### 로컬 환경설정
- AWS CLI 설치
- `aws configure` 을 이용해 aws_access_key_id, aws_secret_access_key 설정 완료

#### 배포 시 환경설정
- Jar 파일 생성 시 `-PcodeArtifactAuthToken=` 을 이용해 `codeArtifactAuthToken` 값을 직접 넘긴다.
- github action을 사용한다면 다음과 같이 codeArtifactAuthToken 추출 가능
  - github secrets 설정 필요 `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
  - ```
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ap-northeast-2
    - name: Get and save Auth Token for CodeArtifact
      id: get-save-codeartifact-auth-token
      run: |
        aws codeartifact get-authorization-token --domain wafflestudio --domain-owner 405906814034 --query authorizationToken --region ap-northeast-1 --output text > .codeartifact-auth-token
    
### spring 연동
- `build.gradle.kts` 혹은 `build.gradle` 파일에 아래와 같이 추가
    - build.gradle.kts
      ```kotlin
      repositories {
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
        implementation("com.wafflestudio.spring:spring-boot-starter-waffle:1.0.3")
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
        implementation 'com.wafflestudio.spring:spring-boot-starter-waffle:1.0.3'
      }
      ```
## 기능 목록
- [secret-manager](./spring-boot-starter-waffle-secret-manager)
    - secret-names 을 통해 AWS Secret Manager 에서 secret 을 가져오는 라이브러리
    - 활성 property: `secret-names`
- [truffle](./truffle)
    - 와플스튜디오 slack 채널에 로그를 전송하는 라이브러리
    - 활성 property: `truffle.client.api-key`
#### 기능별 사용법은 링크 참조

## 사용처 예시
- [snutt](https://github.com/wafflestudio/snutt)
- [snutt-ev](https://github.com/wafflestudio/snutt-ev)
