## 1. 项目初始化

- [x] 1.1 创建 Maven 项目结构（pom.xml）
- [x] 1.2 配置 Spring Boot 3.x 依赖
- [x] 1.3 添加 PostgreSQL Driver 依赖
- [x] 1.4 添加 JPA Starter 依赖

## 2. 配置

- [x] 2.1 创建 application.yml 配置文件
- [x] 2.2 配置 PostgreSQL 数据源
- [x] 2.3 启用 JPA 审计（createdAt/updatedAt）

## 3. Entity 层

- [x] 3.1 创建 SpiderTask 实体类
- [x] 3.2 创建 SpiderField 实体类
- [x] 3.3 创建 ContentItem 实体类
- [x] 3.4 添加 Lombok 注解

## 4. Repository 层

- [x] 4.1 创建 SpiderTaskRepository 接口
- [x] 4.2 创建 SpiderFieldRepository 接口
- [x] 4.3 创建 ContentItemRepository 接口
- [x] 4.4 定义自定义查询方法（findByStatus、findByTaskId）

## 5. Controller 骨架

- [x] 5.1 创建 SpiderTaskController 骨架
- [x] 5.2 创建 ContentController 骨架
- [x] 5.3 定义基础 REST API 端点

## 6. Service 骨架

- [x] 6.1 创建 SpiderTaskService 骨架
- [x] 6.2 创建 ContentService 骨架
- [x] 6.3 定义基础业务方法占位
