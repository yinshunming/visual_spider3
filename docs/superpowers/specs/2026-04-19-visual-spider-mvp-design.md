# Visual Spider MVP 规格文档

**版本**: 1.0
**日期**: 2026-04-19
**状态**: 待实现

---

## 1. 项目概述

### 1.1 目标
构建一个基于 Java 的可视化爬虫 MVP，完整链路：
```
URL 输入 → 页面选区 → 规则生成 → 抽取预览 → 字段校验 → 字段映射 → 入库 → 定时执行 → 失败排查
```

### 1.2 技术栈
- Java 21
- Spring Boot 3.x
- Thymeleaf（服务端渲染）
- MyBatis
- PostgreSQL
- Quartz Scheduler
- Playwright for Java
- Jsoup

### 1.3 约束
- 除非明确要求，否则不要引入新的前端框架
- 第一版优先采用服务端渲染后台页面
- 规则配置必须支持版本化
- 每个里程碑结束时必须能编译通过
- 每个数据库变更都必须包含 migration SQL
- 每个功能变更都必须给出至少一条可执行的手工验证路径
- 不要重写或重构无关模块
- 不要把规则系统设计成只依赖单个 XPath
- 不要使用 iframe 嵌入第三方网站做选区

---

## 2. 架构设计

### 2.1 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Web 层（Thymeleaf）                     │
│   TaskController / RuleController / ExecutionController     │
├─────────────────────────────────────────────────────────────┤
│                     Service 层（业务逻辑）                    │
│  TaskService / RuleService / ExtractionService / Scheduler  │
│  DiagnosisService                                           │
├─────────────────────────────────────────────────────────────┤
│                    Repository 层（MyBatis）                  │
│     TaskMapper / RuleMapper / FieldMappingMapper /          │
│     CrawlLogMapper                                          │
├─────────────────────────────────────────────────────────────┤
│                      引擎层（独立组件）                      │
│   PlaywrightEngine / SelectorGenerator / JsoupExtractor     │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 Package 结构

```
com.visualspider
├── web
│   ├── TaskController           # 任务配置页面
│   ├── RuleController          # 规则编辑页面
│   └── ExecutionController     # 执行监控页面
│
├── service
│   ├── task                    # Domain 1: 任务配置
│   │   └── TaskService
│   ├── rule                    # Domain 2: 规则生成 + 数据抽取
│   │   ├── RuleService
│   │   └── ExtractionService
│   ├── execution               # Domain 3: 调度执行
│   │   ├── SchedulerService
│   │   └── CrawlExecutor
│   └── diagnosis               # Domain 4: 失败排查
│       └── DiagnosisService
│
├── repository
│   ├── TaskMapper
│   ├── RuleMapper
│   ├── FieldMappingMapper
│   └── CrawlLogMapper
│
├── engine
│   ├── PlaywrightEngine        # 页面渲染 + 截图
│   ├── SelectorGenerator       # 选择器生成
│   └── JsoupExtractor          # 数据抽取
│
└── model
    ├── Task
    ├── Rule
    ├── FieldMapping
    └── CrawlLog
```

---

## 3. Domain 分解

### 3.1 Domain 1: 任务配置（Task Configuration）

**边界**: URL 输入、字段映射配置、爬虫任务 CRUD

**核心行为**:
- 创建/编辑/删除爬虫任务
- 配置目标 URL
- 定义字段映射规则（source field → target table column）
- 配置调度策略（Cron 表达式）

**关键接口**:
```java
interface TaskService {
    Task create(TaskCreateRequest request);
    Task update(Long id, TaskUpdateRequest request);
    void delete(Long id);
    Task getById(Long id);
    List<Task> list();
}
```

**依赖关系**:
- → 规则生成：任务配置是规则生成的输入
- → 调度执行：任务配置触发调度
- ← 失败排查：任务配置错误可能导致失败

---

### 3.2 Domain 2: 规则生成 + 数据抽取（Rule Generation & Extraction）

**边界**: 可视化选区、候选规则生成、数据抽取预览

**核心行为**:
- 页面渲染（Playwright）
- 可视化选区（Browser-in-Box 方案）
  - Playwright 启动独立浏览器进程
  - 截图 + 坐标映射
  - 用户在截图上点击选区
  - 后端根据坐标生成候选选择器
- 生成候选 CSS Selector / XPath 规则
- 数据抽取预览（Jsoup）
- 字段级联校验

**关键接口**:
```java
interface RuleService {
    // 生成候选选择器
    List<SelectorCandidate> generateCandidates(Long taskId, int x, int y, String screenshotId);

    // 规则测试预览
    ExtractionPreview preview(Long ruleId, String testUrl);

    // 保存规则
    Rule save(RuleCreateRequest request);
}

interface ExtractionService {
    // 执行抽取
    ExtractionResult extract(Rule rule, String html);

    // 字段校验
    ValidationResult validate(ExtractionResult result, List<FieldMapping> mappings);
}
```

**依赖关系**:
- ← 任务配置：提供 URL 和字段映射作为输入
- → 调度执行：规则是调度的数据源
- ← 失败排查：抽取失败需要反馈给规则引擎优化

---

### 3.3 Domain 3: 调度执行（Scheduling & Execution）

**边界**: Quartz 调度、任务执行、数据入库

**核心行为**:
- Quartz 定时触发
- 加载任务规则
- 执行数据抽取
- 数据清洗转换
- **Upsert 入库**（根据唯一标识判断 Insert 或 Update，幂等性保证）
- 执行日志记录

**关键接口**:
```java
interface SchedulerService {
    void scheduleTask(Long taskId, String cronExpression);
    void unscheduleTask(Long taskId);
    void triggerNow(Long taskId);  // 手动触发
}

interface CrawlExecutor {
    ExecutionResult execute(Long taskId);

    // 内部流程
    // 1. PlaywrightEngine.fetchPage(url) → HTML
    // 2. JsoupExtractor.extract(html, rules) → ExtractionResult
    // 3. DataMapper.map(extractionResult, fieldMappings) → Entities
    // 4. Repository.upsert(entities) → 入库
    // 5. CrawlLogMapper.log(executionResult) → 记录日志
}
```

**依赖关系**:
- ← 任务配置：获取调度策略和字段映射
- ← 规则生成：获取抽取规则
- → 失败排查：执行结果反馈

---

### 3.4 Domain 4: 失败排查（Failure Diagnosis）

**边界**: 失败日志、执行监控、手动重试

**核心行为**:
- 失败任务记录和分类（网络错误/规则错误/入库错误）
- 错误信息归因
- 手动重试入口
- 执行历史追溯

**MVP 简化策略**: 仅记录日志到数据库，不实现实时告警

**关键接口**:
```java
interface DiagnosisService {
    // 记录执行日志
    void logExecution(CrawlLog log);

    // 查询失败记录
    List<CrawlLog> getFailedLogs(Long taskId, LocalDateTime since);

    // 手动重试
    void retry(Long logId);
}
```

**依赖关系**:
- ← 调度执行：接收执行结果和失败事件
- ← 规则生成：规则错误反馈给规则引擎优化
- → 任务配置：严重失败可能需要修改配置

---

## 4. 数据库设计

### 4.1 核心表结构

#### crawl_task（爬虫任务）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| name | VARCHAR(255) | 任务名称 |
| url | TEXT | 目标URL |
| cron_expression | VARCHAR(100) | 调度Cron表达式 |
| status | VARCHAR(20) | 状态（ACTIVE/PAUSED/DELETED） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### crawl_rule（抽取规则）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务ID |
| name | VARCHAR(255) | 规则名称 |
| selector_type | VARCHAR(20) | CSS_SELECTOR / XPATH |
| selector | TEXT | 选择器表达式 |
| field_name | VARCHAR(100) | 字段名 |
| is_unique_key | BOOLEAN | 是否为唯一标识（Upsert用） |
| created_at | TIMESTAMP | 创建时间 |
| updated_at | TIMESTAMP | 更新时间 |

#### field_mapping（字段映射）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务ID |
| source_field | VARCHAR(100) | 源字段名 |
| target_column | VARCHAR(100) | 目标数据库列名 |
| transform_expr | TEXT | 转换表达式（可选） |

#### crawl_log（执行日志）
| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 主键 |
| task_id | BIGINT | 关联任务ID |
| start_time | TIMESTAMP | 开始时间 |
| end_time | TIMESTAMP | 结束时间 |
| status | VARCHAR(20) | SUCCESS / FAILED / PARTIAL |
| records_count | INT | 抽取记录数 |
| error_message | TEXT | 错误信息（失败时） |
| error_type | VARCHAR(50) | 错误类型 |

### 4.2 Migration SQL 要求
每个数据库变更都必须包含 migration SQL，命名规范：
```
V{version}__{description}.sql
例如：V001__create_crawl_task_table.sql
```

---

## 5. 数据流

### 5.1 任务创建流程
```
用户 → 填写URL/字段映射 → 保存Task
                          ↓
                    创建/更新 Task + FieldMapping 记录
```

### 5.2 规则配置流程
```
用户 → 输入URL → Playwright渲染 → 截图
                              ↓
                         用户点击元素
                              ↓
                    SelectorGenerator生成候选选择器
                              ↓
                         用户选择/调整
                              ↓
                    保存 Rule 记录
                              ↓
                    ExtractionService.preview() → 预览结果
```

### 5.3 调度执行流程
```
Quartz触发 → CrawlExecutor.execute(taskId)
                        ↓
              TaskService.getTask(taskId)
                        ↓
              RuleService.getRulesByTask(taskId)
                        ↓
              PlaywrightEngine.fetchPage(url) → HTML
                        ↓
              JsoupExtractor.extract(html, rules) → ExtractionResult
                        ↓
              DataMapper.map(result, fieldMappings) → Entities
                        ↓
              Repository.upsert(entities) → 入库（幂等）
                        ↓
              CrawlLogMapper.log(executionResult) → 记录日志
                        ↓
              返回 ExecutionResult（成功/失败）
```

---

## 6. 关键技术方案

### 6.1 Browser-in-Box 可视化选区
- Playwright 启动独立 Chromium 浏览器进程
- 页面加载完成后截图
- 截图返回前端展示
- 用户在截图上点击元素（坐标 x, y）
- 后端根据坐标计算元素的 CSS Selector / XPath 候选集

### 6.2 规则生成算法
- 根据坐标定位 DOM 元素
- 向上遍历 DOM 树，收集可能的选择器路径
- 生成多个候选选择器（ID > Class > Attribute > Position）
- 用户选择最稳定的那个

### 6.3 Upsert 入库
- 根据 `is_unique_key` 标记的字段组合生成唯一标识
- 使用 PostgreSQL `ON CONFLICT ... DO UPDATE` 实现
- 保证重复执行的幂等性

---

## 7. 里程碑规划

### M1: 项目骨架
- Spring Boot 项目初始化
- 数据库表创建（Migration SQL）
- 基础 CRUD 功能

### M2: 规则配置
- PlaywrightEngine 集成
- 可视化选区
- 规则生成和预览

### M3: 数据抽取
- Jsoup 抽取逻辑
- 字段映射
- 预览功能

### M4: 调度执行
- Quartz 集成
- 定时任务配置
- Upsert 入库

### M5: 失败排查
- 执行日志
- 手动重试
- 历史追溯

---

## 8. 验证路径

每个功能变更必须提供至少一条可执行的手工验证路径。

### 示例：
| 功能 | 验证路径 |
|------|----------|
| 任务创建 | POST /api/tasks → 创建任务 → GET /api/tasks/{id} 确认 |
| 规则预览 | 创建规则 → POST /api/rules/{id}/preview → 查看预览结果 |
| 手动触发 | 点击"立即执行" → 查看日志确认执行成功 |

---

## 9. 非目标（Out of Scope）

- 规则版本化管理（MVP阶段）
- 实时告警通知（MVP阶段）
- 分布式爬虫
- 代理池管理
- 反爬对抗策略
