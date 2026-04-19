# AGENTS.md

## 项目目标
构建一个基于 Java 的可视化爬虫 MVP，支持列表页和详情页的分离规则配置。
目标链路：任务配置 -> 列表规则配置 -> 详情规则配置 -> 调度执行 -> 失败排查

## 技术栈
- Java 21
- Spring Boot
- Thymeleaf
- MyBatis
- PostgreSQL
- Quartz
- Playwright for Java
- Jsoup

## Domain 结构（5个核心域）

### Domain 1: 任务配置
- 任务 CRUD（名称、URL、调度策略）
- 列表字段定义（标题、链接、时间 → 映射到 list_data 表）
- 详情字段定义（正文、作者、标签 → 映射到 detail_data 表）
- 列表规则ID、详情规则ID 关联

### Domain 2: 列表规则生成
- Playwright 渲染页面 → 截图
- 用户点击列表区域 → 生成列表容器选择器
- 用户定义分页选择器
- 用户点击列表项 → 生成候选选择器（标题、链接、时间）
- 用户选择一个 → 保存规则
- 预览：提取列表数据

### Domain 3: 详情规则生成
- 用户选择一个列表项 → 跳转详情页
- Playwright 渲染详情页 → 截图
- 用户点击各字段区域 → 生成候选选择器
- 用户选择每个字段的规则
- 预览：提取详情数据

### Domain 4: 调度执行
- Quartz 定时触发
- 加载列表规则 → 提取列表数据（标题、链接、时间）
- 遍历链接 → 加载详情规则 → 提取详情数据
- 处理分页（根据分页规则翻页）
- Upsert 入库（list_data 表、detail_data 表）
- 记录执行日志

### Domain 5: 失败排查
- 查看执行历史
- 失败详情（哪个URL失败、错误类型）
- 手动重试（单条/全部）

## 核心数据库表

| 表名 | 用途 |
|------|------|
| `crawl_task` | 任务配置 |
| `crawl_list_rule` | 列表规则 |
| `crawl_detail_rule` | 详情规则 |
| `list_data` | 列表数据 |
| `detail_data` | 详情数据 |
| `crawl_log` | 执行日志 |

## 不可违反的约束
- 除非明确要求，否则不要引入新的前端框架
- 第一版优先采用服务端渲染后台页面
- 规则配置必须支持版本化（未来扩展，当前MVP不实现）
- 每个里程碑结束时必须能编译通过
- 每个数据库变更都必须包含 migration SQL
- 每个功能变更都必须给出至少一条可执行的手工验证路径
- 不要重写或重构无关模块
- 不要把规则系统设计成只依赖单个 XPath
- 不要使用 iframe 嵌入第三方网站做选区

## 项目类型
- Java 项目（从 `.gitignore` 模式推断：`*.class`、`*.jar`、`*.war`、`hs_err_pid*`）

## 状态
- 已定义主规格结构（OpenSpec artifacts）
- 已规划 6 个里程碑（M1 项目骨架 → M6 失败排查）

## OpenSpec
- 运行 `openspec status` 查看活跃变更
- 运行 `openspec list` 查看所有变更
- 详细规格见 `openspec/changes/visual-spider-mvp-arch/`
