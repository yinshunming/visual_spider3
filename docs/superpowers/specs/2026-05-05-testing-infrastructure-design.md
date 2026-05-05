# Visual Spider 测试基础设施设计方案

**日期**: 2026-05-05
**状态**: 待评审

---

## 1. 目标

为 Visual Spider 项目建立完整的测试金字塔，覆盖 Service 层、Controller 层、Repository 层，确保核心业务逻辑可测试、可回归。

---

## 2. 技术选型

### 2.1 测试框架

| 组件 | 技术 | 说明 |
|------|------|------|
| 单元测试 | JUnit 5 (JUnit Jupiter) | 已有 spring-boot-starter-test |
| Mock 框架 | Mockito | 已有 |
| Controller 测试 | MockMvc + @WebMvcTest | 已有 |
| 集成测试数据库 | H2 内存数据库 | 新增 |
| 断言库 | AssertJ | 已有 |

### 2.2 新增依赖

```xml
<!-- H2 内存数据库（集成测试用） -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>

<!-- Lombok（测试也需要） -->
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>test</scope>
</dependency>
```

---

## 3. 测试目录结构

```
src/test/
├── java/com/example/visualspider/
│   ├── controller/
│   │   ├── SpiderTaskControllerTest.java
│   │   └── ContentControllerTest.java
│   ├── service/
│   │   ├── SpiderTaskServiceTest.java
│   │   ├── CrawlerEngineTest.java
│   │   └── ContentServiceTest.java
│   ├── repository/
│   │   ├── SpiderTaskRepositoryTest.java
│   │   ├── SpiderFieldRepositoryTest.java
│   │   └── ContentItemRepositoryTest.java
│   └── fixture/
│       ├── TaskFixtures.java
│       └── FieldFixtures.java
└── resources/
    ├── application-test.yml
    └── schema.sql
```

---

## 4. 测试配置

### 4.1 application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
    driver-class-name: org.h2.Driver
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.H2Dialect
  sql:
    init:
      mode: always
      schema-locations: classpath:schema.sql
```

### 4.2 schema.sql

创建与生产环境一致的表结构（H2 兼容 PostgreSQL 语法）。

---

## 5. Fixtures 设计

### 5.1 TaskFixtures.java

```java
public class TaskFixtures {
    public static SpiderTask draftTask() { ... }
    public static SpiderTask enabledTask() { ... }
    public static SpiderTask disabledTask() { ... }
    public static SpiderTask runningTask() { ... }
    public static SpiderTaskRequest createRequest() { ... }
    public static SpiderTaskRequest updateRequest() { ... }
}
```

### 5.2 FieldFixtures.java

```java
public class FieldFixtures {
    public static SpiderField textField() { ... }
    public static SpiderField imageField() { ... }
    public static SpiderField linkField() { ... }
    public static SpiderField richTextField() { ... }
}
```

---

## 6. 测试策略

### 6.1 Repository 层（@DataJpaTest）

- 使用 H2 内存数据库
- 测试 CRUD 方法（save、findById、delete）
- 测试查询方法（分页、条件查询）
- 每个 Repository 一个测试类

### 6.2 Service 层（@ExtendWith(MockitoExtension)）

- 使用 Mockito mock 依赖对象
- 测试业务逻辑（状态机、CRUD、异常情况）
- 使用 assertAll 分组断言
- 每个 Service 一个测试类

### 6.3 Controller 层（@WebMvcTest）

- 使用 MockMvc 进行 HTTP 级别测试
- 测试 REST API 端点（CRUD、状态变更）
- 测试请求验证、异常处理
- 使用 jsonPath 验证响应

---

## 7. 覆盖率目标

| 层级 | 目标 | 最低可接受 |
|------|------|----------|
| Repository | 80%+ | 70% |
| Service | 80%+ | 70% |
| Controller | 70%+ | 60% |

---

## 8. 实现顺序

1. **Phase 1**: 测试基础设施
   - 创建目录结构
   - 添加 H2 依赖
   - 创建 application-test.yml 和 schema.sql
   - 创建 Fixtures 类

2. **Phase 2**: Repository 层测试
   - SpiderTaskRepositoryTest
   - SpiderFieldRepositoryTest
   - ContentItemRepositoryTest

3. **Phase 3**: Service 层测试
   - SpiderTaskServiceTest
   - CrawlerEngineTest
   - ContentServiceTest

4. **Phase 4**: Controller 层测试
   - SpiderTaskControllerTest
   - ContentControllerTest

---

## 9. 排除范围

- ContentController 中未实现的方法（返回 null 的 TODO）不写测试
- Testcontainers 暂不引入（后续有需要再添加）
- 前端 E2E 测试暂不包含
