# waffle-secret-manager
## 와플스튜디오 AWS Secret Manager 연동 라이브러리
- 와플스튜디오에서 사용하는 AWS Secret Manager 를 spring boot environment 로 쉽게 가져오기 위한 라이브러리

## 사용법
### AWS Secret Manager 설정
- AWS Secret Manager 에 secret 생성
- spring environment 설정처럼 key value 형태로 secret 생성
  - ex)
    - key: `spring.datasource.url`
    - value: `jdbc:postgresql://localhost:5432/test`

### spring 연동
#### 프로퍼티 설정
- 다음과 같은 spring 프로퍼티 생성
    - `secret-names`: {aws-secret-manager-name}

#### waffle-secret-manager 라이브러리 연동
- codeartifact 등록 [메인 페이지](../README.md) 참조
- `build.gradle.kts` 혹은 `build.gradle` 파일에 아래와 같이 추가 (spring-boot-starter-waffle 추가 시 생략 가능)
    - build.gradle.kts
      ```kotlin
      dependencies {
        //...
        implementation("com.wafflestudio.spring:spring-boot-starter-waffle-secret-manager:1.0.1")
      }
      ```
    - build.gradle
      ```groovy
      dependencies {
        //...
        implementation 'com.wafflestudio.spring:spring-boot-starter-waffle-secret-manager:1.0.1'
      }
      ```
