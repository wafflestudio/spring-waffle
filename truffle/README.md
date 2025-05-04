# truffle-kotlin
## 와플스튜디오 로깅 라이브러리
- 연동된 와플스튜디오 slack 채널에 로그를 전송하는 kotlin(java) 라이브러리

## 사용법
### slack 채널 생성 및 key 신청
- slack 의 #project-truffle (https://wafflestudio.slack.com/archives/C04KKHS2P1D) 에서 프로젝트 추가 요청
- ex) `안녕하세요! #team-ex 에서 truffle 을 이용하려고 해서, #team-ex-alert 채널에 연결되도록 프로젝트 하나 추가 요청드립니다!`

### spring 연동
#### 프로퍼티 설정
- 다음과 같은 spring 프로퍼티 생성
    - `truffle.client.api-key: {api-key}`
        - `{api-key}` 는 slack 트러플 채널(#project-truffle)에서 발급받은 key

#### truffle-kotlin 연동
- codeartifact 등록 [메인 페이지](../README.md) 참조
- `build.gradle.kts` 혹은 `build.gradle` 파일에 아래와 같이 추가 (spring-boot-starter-waffle 추가 시 생략 가능)
    - build.gradle.kts
      ```kotlin
      dependencies {
        //...
        implementation("com.wafflestudio.spring.truffle:spring-boot-starter-truffle:1.0.3")
      }
      ```
    - build.gradle
      ```groovy
      dependencies {
        //...
        implementation 'com.wafflestudio.spring.truffle:spring-boot-starter-truffle:1.0.3'
      }
      ```

#### logback customize (선택)
- 기존 설정은 에러 레벨의 root 로그만 트러플로 보내는 구조
- 추가로 특정 패키지, 특정 레벨의 로그를 트러플로 보내는 설정을 추가하기 위해서는 `logback-spring.xml` 파일에 추가 설정이 필요
- logback 연동 설정
  - logback-spring.xml 파일 (혹은 spring `logging.config`에 설정한 logback xml 설정 파일)에 truffle-appender.xml include
    - `<include resource="com/wafflestudio/truffle/sdk/logback/truffle-appender.xml"/>`
  - logback-spring.xml 예시
    ```xml
    <?xml version="1.0" encoding="UTF-8" ?>
    <configuration>
        <statusListener class="ch.qos.logback.core.status.NopStatusListener" />

        <include resource="org/springframework/boot/logging/logback/defaults.xml" />
        <include resource="org/springframework/boot/logging/logback/file-appender.xml" />
        <include resource="org/springframework/boot/logging/logback/console-appender.xml"/>
        <include resource="com/wafflestudio/spring/truffle/appender/truffle-appender.xml"/>

        <root level="INFO">
            <appender-ref ref="CONSOLE" />
            <appender-ref ref="TRUFFLE_ERROR_APPENDER"/>
        </root>
    </configuration>
    ```
  - 추가로 특정 패키지, 특정 레벨의 로그를 트러플로 보내는 설정을 추가하기 위해서는 다음과 같이 추가
    ```xml
    <logger name="com.wafflestudio" level="DEBUG">
        <appender-ref ref="TRUFFLE_APPENDER"/>
    </logger>
    ```
  - 환경별 설정을 위해서는 logback-dev.xml, logback-prod.xml 등으로 설정 파일을 나누어 사용
    - ex) logback-prod.xml, logback-dev.xml
    - 환경별로 logging.config=classpath:logback-prod.xml 과 같이 설정해 사용

