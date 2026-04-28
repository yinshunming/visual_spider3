## 1. DTO 类

- [x] 1.1 创建 SpiderTaskRequest DTO（创建/更新请求）
- [x] 1.2 创建 SpiderTaskResponse DTO（响应）
- [x] 1.3 创建 FieldRequest DTO（字段请求）

## 2. SpiderTaskService 业务逻辑

- [x] 2.1 实现 findAll(Pageable) 分页查询
- [x] 2.2 实现 findById(Long) 查询
- [x] 2.3 实现 save(SpiderTaskRequest) 创建任务
- [x] 2.4 实现 update(Long, SpiderTaskRequest) 更新任务
- [x] 2.5 实现 delete(Long) 删除任务（级联删除字段）
- [x] 2.6 实现 enable(Long) 启用任务
- [x] 2.7 实现 disable(Long) 禁用任务
- [x] 2.8 实现 resetToDraft(Long) 取消任务

## 3. 状态机校验

- [x] 3.1 状态转换规则校验（DRAFT → ENABLED/DISABLED）
- [x] 3.2 状态转换规则校验（DISABLED ↔ DRAFT/ENABLED）
- [x] 3.3 启用前校验字段配置

## 4. SpiderField 关联管理

- [x] 4.1 实现字段与任务的同时保存
- [x] 4.2 实现字段与任务的同时更新
- [x] 4.3 实现级联删除字段

## 5. SpiderTaskController 实现

- [x] 5.1 实现 GET /api/tasks 分页查询
- [x] 5.2 实现 GET /api/tasks/{id} 获取详情（含字段）
- [x] 5.3 实现 POST /api/tasks 创建任务
- [x] 5.4 实现 PUT /api/tasks/{id} 更新任务
- [x] 5.5 实现 DELETE /api/tasks/{id} 删除任务
- [x] 5.6 实现 POST /api/tasks/{id}/enable 启用任务
- [x] 5.7 实现 POST /api/tasks/{id}/disable 禁用任务
- [ ] 5.8 实现 POST /api/tasks/{id}/run 运行任务（TODO: M3）
