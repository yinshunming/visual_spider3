## Why

Visual Spider 可视化爬虫系统需要一个稳定的基础设施来支撑后续功能开发。M1（基础设施）是整个项目的地基，需要在开始爬虫核心、可视化配置等功能之前完成。

## What Changes

- Spring Boot 3.x 项目初始化（Maven）
- PostgreSQL 数据库连接配置（application.yml）
- JPA Entity 实体类：SpiderTask、SpiderField、ContentItem
- Repository 数据访问层接口
- REST API Controller 骨架
- 基础 Service 层占位

## Capabilities

### New Capabilities

- `task-management`: 爬虫任务的基础数据模型，包含 url_mode、status、schedule_cron 等字段
- `field-management`: 自定义字段的数据模型，支持 text/image/link/richText 四种类型
- `content-item`: 爬取内容的数据模型，fields 使用 JSONB 存储动态字段值

## Impact

- 创建 `backend/src/main/java/com/example/visualspider/` 目录结构
- 依赖：Spring Boot Starter Data JPA、PostgreSQL Driver
- 为 M2（任务管理）、M3（爬虫核心）提供基础设施
