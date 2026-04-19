# Implementation Tasks

## M1: 项目骨架

- [ ] 1.1 创建 Spring Boot 项目结构（Java 21 + Spring Boot 3.x）
- [ ] 1.2 配置 pom.xml 依赖（Spring Boot, MyBatis, PostgreSQL, Quartz, Playwright, Jsoup）
- [ ] 1.3 创建数据库 Migration SQL（crawl_task, crawl_list_rule, crawl_detail_rule, list_data, detail_data, crawl_log）
- [ ] 1.4 配置 MyBatis 和 PostgreSQL 连接
- [ ] 1.5 配置 Quartz Scheduler
- [ ] 1.6 验证项目编译通过

## M2: 任务配置（Domain 1）

- [ ] 2.1 实现 Task CRUD API（创建/查询/更新/删除任务）
- [ ] 2.2 实现列表字段定义配置
- [ ] 2.3 实现详情字段定义配置
- [ ] 2.4 实现规则关联（list_rule_id, detail_rule_id）
- [ ] 2.5 编写手工验证文档

## M3: 列表规则生成（Domain 2）

- [ ] 3.1 集成 Playwright Engine（页面渲染 + 截图）
- [ ] 3.2 实现 Browser-in-Box 可视化选区（坐标映射）
- [ ] 3.3 实现候选选择器生成（CSS Selector / XPath 多候选）
- [ ] 3.4 实现分页选择器配置
- [ ] 3.5 实现列表数据预览
- [ ] 3.6 实现列表规则保存
- [ ] 3.7 编写手工验证文档

## M4: 详情规则生成（Domain 3）

- [ ] 4.1 实现列表项点击跳转详情页
- [ ] 4.2 实现详情页 Playwright 渲染
- [ ] 4.3 实现详情页字段候选选择器生成
- [ ] 4.4 实现详情数据预览
- [ ] 4.5 实现详情规则保存
- [ ] 4.6 编写手工验证文档

## M5: 调度执行（Domain 4）

- [ ] 5.1 实现 Quartz Job 配置
- [ ] 5.2 实现列表数据提取流程
- [ ] 5.3 实现分页处理流程
- [ ] 5.4 实现详情数据提取流程
- [ ] 5.5 实现 Upsert 入库（list_data, detail_data）
- [ ] 5.6 实现执行日志记录
- [ ] 5.7 编写手工验证文档

## M6: 失败排查（Domain 5）

- [ ] 6.1 实现执行历史查看
- [ ] 6.2 实现失败详情展示（URL、错误类型、错误信息）
- [ ] 6.3 实现单条重试功能
- [ ] 6.4 实现全部重试功能
- [ ] 6.5 编写手工验证文档
