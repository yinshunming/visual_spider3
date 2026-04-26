# Spring Boot 项目规范

## 技术栈
- Java 17+ / Spring Boot 3.x
- MyBatis 或 JPA / Maven 或 Gradle
- PostgreSQL / Redis / MQ

## 架构原则
- 业务逻辑放在 Service / Domain 层
- DTO、Entity、VO 分层清晰，避免直接混用
- 关注事务边界、幂等、并发安全、缓存一致性

## 命令
- 构建：`./mvnw clean package` 或 `gradle build`
- 运行：`java -jar target/*.jar` 或 `gradle bootRun`
- 测试：`./mvnw test` 或 `gradle test`

## 参考
- 全局规范：`C:\Users\yin-s\.config\opencode\AGENTS.md`
- 项目 skill：使用 `/springboot-patterns`、`/java-coding-standards` 等
