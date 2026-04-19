# AGENTS.md

## 项目目标
构建一个基于 Java 的可视化爬虫 MVP：
URL 输入 -> 可视化选区 -> 规则生成 -> 抽取预览 -> 字段校验 -> 数据库映射 -> Quartz 定时调度。

## 技术栈
- Java 21
- Spring Boot
- Thymeleaf
- MyBatis
- PostgreSQL
- Quartz
- Playwright for Java
- Jsoup

## 不可违反的约束
- 除非明确要求，否则不要引入新的前端框架
- 第一版优先采用服务端渲染后台页面
- 规则配置必须支持版本化
- 每个里程碑结束时必须能编译通过
- 每个数据库变更都必须包含 migration SQL
- 每个功能变更都必须给出至少一条可执行的手工验证路径
- 不要重写或重构无关模块
- 不要把规则系统设计成只依赖单个 XPath
- 不要使用 iframe 嵌入第三方网站做选区

## 项目类型
- Java 项目（从 `.gitignore` 模式推断：`*.class`、`*.jar`、`*.war`、`hs_err_pid*`）

## 状态
- 空仓库 — 尚未提交任何源代码
- 仅包含初始提交的 `.gitignore`

## 备注
- 项目名 `visual_spider3` 暗示可能是可视化/爬虫工具，但无代码确认
- 添加代码后，请更新本文件，补充实际的构建命令、包结构和项目规范
