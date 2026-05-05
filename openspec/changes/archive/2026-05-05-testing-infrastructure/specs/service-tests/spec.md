## ADDED Requirements

### Requirement: SpiderTaskService task CRUD operations
The system SHALL provide task CRUD operations through SpiderTaskService.

#### Scenario: Create task
- **WHEN** `spiderTaskService.createTask(request)` is called with valid request
- **THEN** a new task is created with status DRAFT and returned

#### Scenario: Get task by ID
- **WHEN** `spiderTaskService.getTaskById(id)` is called with existing ID
- **THEN** the task with fields is returned
- **WHEN** called with non-existent ID
- **THEN** EntityNotFoundException is thrown

#### Scenario: Update task
- **WHEN** `spiderTaskService.updateTask(id, request)` is called
- **THEN** the task is updated and returned

#### Scenario: Delete task
- **WHEN** `spiderTaskService.deleteTask(id)` is called
- **THEN** the task and associated fields are deleted

#### Scenario: List tasks with pagination
- **WHEN** `spiderTaskService.listTasks(pageable)` is called
- **THEN** a Page of tasks is returned

### Requirement: SpiderTaskService task state transitions
The system SHALL manage task state transitions according to the state machine.

#### Scenario: Enable task
- **WHEN** `spiderTaskService.enableTask(id)` is called on DRAFT or DISABLED task
- **THEN** task status changes to ENABLED
- **WHEN** called on already ENABLED task
- **THEN** IllegalStateException is thrown
- **WHEN** called on RUNNING task
- **THEN** IllegalStateException is thrown

#### Scenario: Disable task
- **WHEN** `spiderTaskService.disableTask(id)` is called on ENABLED task
- **THEN** task status changes to DISABLED
- **WHEN** called on RUNNING task
- **THEN** IllegalStateException is thrown

#### Scenario: Run task
- **WHEN** `spiderTaskService.runTask(id)` is called on ENABLED task
- **THEN** task status changes to RUNNING and crawling starts asynchronously

### Requirement: CrawlerEngine executes crawling
The system SHALL execute crawling based on task configuration.

#### Scenario: Execute list page mode task
- **WHEN** `crawlerEngine.execute(task)` is called with LIST_PAGE mode task
- **THEN** list page is parsed, content pages are extracted, results are saved

#### Scenario: Execute direct URL mode task
- **WHEN** `crawlerEngine.execute(task)` is called with DIRECT_URL mode task
- **THEN** seed URLs are processed directly, results are saved

### Requirement: ContentService content management
The system SHALL manage content items through ContentService.

#### Scenario: Get content by ID
- **WHEN** `contentService.getContentById(id)` is called with existing ID
- **THEN** the content item is returned
- **WHEN** called with non-existent ID
- **THEN** EntityNotFoundException is thrown

#### Scenario: List content by task
- **WHEN** `contentService.listContentByTask(taskId, pageable)` is called
- **THEN** a Page of content items is returned

#### Scenario: Delete content
- **WHEN** `contentService.deleteContent(id)` is called
- **THEN** the content item is deleted