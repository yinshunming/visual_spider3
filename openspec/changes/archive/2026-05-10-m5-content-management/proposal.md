## Why

M5 是内容管理的里程碑，目标是完成内容存储、预览、编辑、导出功能。当前 ContentItem 实体和 Repository 已就绪，ContentController 骨架存在但所有端点均为 TODO，导出方法为空，前端无内容管理页面。实现 M5 后，用户可以查看爬取的内容、编辑字段和状态、导出数据到 Excel/CSV。

## What Changes

### Backend API
- 实现 `ContentController` 5个端点：列表查询、内容详情、更新、删除、导出
- 增强 `ContentService`：新增内容更新、状态变更方法，实现导出逻辑（Excel + CSV）
- 新增 `ContentResponse` DTO 用于 API 返回

### Frontend
- 新增 `ContentList.vue` 页面：内容列表、分页、taskId 筛选
- 新增 `ContentPreview.vue` 页面：渲染 rawHtml 展示内容预览
- 新增 `ContentEdit.vue` 页面：编辑内容字段和状态
- 扩展 `frontend/src/api/index.js`：新增内容相关 API 调用

### Testing
- 新增 `ContentServiceTest` 单元测试
- 新增导出逻辑测试（Excel/CSV）
- Repository 分页查询测试覆盖

## Capabilities

### New Capabilities

- `content-management-api`: 内容管理后端 REST API，包括内容列表、详情、更新、删除、导出端点
- `content-management-ui`: 内容管理前端页面，包括列表、预览、编辑功能
- `content-export`: 内容导出功能，支持 Excel (.xlsx) 和 CSV 两种格式

### Modified Capabilities

- `visual-spider-spec`: 更新 M5-M6 里程碑状态从"未开始"到"进行中/已完成"

## Impact

### 影响的代码模块
- `backend/src/main/java/com/example/visualspider/controller/ContentController.java`
- `backend/src/main/java/com/example/visualspider/service/ContentService.java`
- `backend/src/main/java/com/example/visualspider/dto/ContentResponse.java` (新建)
- `frontend/src/views/ContentList.vue` (新建)
- `frontend/src/views/ContentPreview.vue` (新建)
- `frontend/src/views/ContentEdit.vue` (新建)
- `frontend/src/api/index.js` (扩展)

### 依赖
- M3 (爬虫核心) 已完成 - 提供 `ContentService.saveContent()` 写入数据
- Apache POI 或 OpenCSV 用于 Excel 导出
- 前端 Element Plus UI 组件库（已使用）

### API 影响
新增 `ContentController` 路由 `/api/contents`：
| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/contents | 分页查询内容列表（支持 taskId 筛选） |
| GET | /api/contents/{id} | 获取内容详情 |
| PUT | /api/contents/{id} | 更新内容（字段 + 状态） |
| DELETE | /api/contents/{id} | 删除内容 |
| GET | /api/contents/export | 导出内容（format=xlsx 或 csv） |
