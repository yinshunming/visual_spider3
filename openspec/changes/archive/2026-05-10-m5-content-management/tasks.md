# M5 内容管理 - 实现任务清单

## 1. Backend DTO 和 Response

- [x] 1.1 创建 `ContentResponse` DTO（包含 fields 解析为 Map<String, Object>）
- [x] 1.2 在 `ContentItemRequest` 中添加 `status` 字段支持

## 2. ContentController 实现

- [x] 2.1 实现 `GET /api/contents` 分页列表查询（支持 taskId 筛选）
- [x] 2.2 实现 `GET /api/contents/{id}` 内容详情查询
- [x] 2.3 实现 `PUT /api/contents/{id}` 内容更新（字段 + 状态）
- [x] 2.4 实现 `DELETE /api/contents/{id}` 内容删除
- [x] 2.5 实现 `GET /api/contents/export` 导出接口（format 参数：xlsx/csv）

## 3. ContentService 增强

- [x] 3.1 新增 `updateContent(id, fields)` 方法
- [x] 3.2 新增 `updateStatus(id, status)` 方法
- [x] 3.3 新增 `exportToExcel(taskId, response)` 导出 Excel 逻辑
- [x] 3.4 新增 `exportToCsv(taskId, response)` 导出 CSV 逻辑
- [x] 3.5 添加导出字段 JSON 展开逻辑（fields 解析为独立列）

## 4. Maven/依赖

- [x] 4.1 在 `pom.xml` 添加 Apache POI 依赖（poi-ooxml）

## 5. Service 单元测试

- [x] 5.1 创建 `ContentServiceTest` 测试类
- [x] 5.2 测试 `updateContent` - 成功更新字段
- [x] 5.3 测试 `updateContent` - 内容不存在时抛出异常
- [x] 5.4 测试 `updateStatus` - 状态变更
- [x] 5.5 测试 `updateStatus` - PUBLISHED 时设置 publishedAt
- [x] 5.6 测试 `findById` - 返回解析后的 fields Map
- [x] 5.7 测试 `findAll` 分页返回正确数据

## 6. Repository 分页查询测试

- [x] 6.1 `ContentItemRepositoryTest` - 测试 `findByTaskId` 分页
- [x] 6.2 `ContentItemRepositoryTest` - 测试 `findByStatus` 分页
- [x] 6.3 `ContentItemRepositoryTest` - 测试 `findByTaskIdAndStatus` 分页

## 7. 导出逻辑测试

- [x] 7.1 创建 `ContentExportTest` 测试类
- [x] 7.2 测试 `exportToCsv` - 生成正确的 CSV 格式
- [x] 7.3 测试 `exportToCsv` - 特殊字符（逗号、引号、换行）正确转义
- [x] 7.4 测试 `exportToExcel` - 生成有效的 .xlsx 文件
- [x] 7.5 测试导出 - fields JSON 展开为独立列
- [x] 7.6 测试导出 - 超过 10000 条时截断

## 8. Frontend API 集成

- [x] 8.1 在 `frontend/src/api/index.js` 添加 `getContents(params)` 方法
- [x] 8.2 添加 `getContent(id)` 方法
- [x] 8.3 添加 `updateContent(id, data)` 方法
- [x] 8.4 添加 `deleteContent(id)` 方法
- [x] 8.5 添加 `exportContent(params)` 方法（触发文件下载）

## 9. Frontend 内容列表页面

- [x] 9.1 创建 `ContentList.vue` 页面
- [x] 9.2 实现 el-table 显示内容列表（source_url, status, created_at）
- [x] 9.3 实现分页组件
- [x] 9.4 实现 taskId 下拉筛选
- [x] 9.5 添加"预览"按钮（点击打开预览对话框）
- [x] 9.6 添加"编辑"按钮（跳转到编辑页）
- [x] 9.7 添加"删除"按钮（含确认对话框）

## 10. Frontend 内容预览页面

- [x] 10.1 创建 `ContentPreview.vue` 对话框组件
- [x] 10.2 使用 iframe sandbox 渲染 rawHtml
- [x] 10.3 显示 fields 各字段值

## 11. Frontend 内容编辑页面

- [x] 11.1 创建 `ContentEdit.vue` 页面
- [x] 11.2 页面加载时获取内容详情
- [x] 11.3 实现 fields 各字段编辑表单
- [x] 11.4 实现状态下拉选择（PENDING/PUBLISHED/DELETED）
- [x] 11.5 实现保存功能（调用 updateContent）
- [x] 11.6 实现返回按钮

## 12. 前端路由配置

- [x] 12.1 在路由配置中添加 `/contents` 路由
- [x] 12.2 在路由配置中添加 `/contents/:id/edit` 路由

## 13. 验证和测试

- [x] 13.1 后端编译通过 `mvn compile`
- [x] 13.2 后端测试通过 `mvn test`
- [x] 13.3 前端编译通过 `npm run build`
- [ ] 13.4 手动验证内容列表 API
- [ ] 13.5 手动验证内容预览功能
- [ ] 13.6 手动验证内容编辑功能
- [ ] 13.7 手动验证导出 Excel 功能
- [ ] 13.8 手动验证导出 CSV 功能

**注意**: 13.4-13.8 需要手动验证，因为它们需要运行服务器和前端。
