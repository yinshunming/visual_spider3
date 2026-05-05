## ADDED Requirements

### Requirement: SpiderTaskController REST endpoints
The system SHALL provide REST API endpoints for task management.

#### Scenario: Create task via POST
- **WHEN** POST `/api/tasks` is called with valid request body
- **THEN** 201 Created is returned with task data
- **WHEN** called with invalid request
- **THEN** 400 Bad Request is returned

#### Scenario: Get task via GET
- **WHEN** GET `/api/tasks/{id}` is called with existing ID
- **THEN** 200 OK is returned with task data including fields
- **WHEN** called with non-existent ID
- **THEN** 404 Not Found is returned

#### Scenario: Update task via PUT
- **WHEN** PUT `/api/tasks/{id}` is called with valid request
- **THEN** 200 OK is returned with updated task
- **WHEN** called with non-existent ID
- **THEN** 404 Not Found is returned

#### Scenario: Delete task via DELETE
- **WHEN** DELETE `/api/tasks/{id}` is called
- **THEN** 204 No Content is returned

#### Scenario: Enable task via POST
- **WHEN** POST `/api/tasks/{id}/enable` is called
- **THEN** 200 OK is returned with updated task status ENABLED
- **WHEN** task is already enabled
- **THEN** 409 Conflict is returned

#### Scenario: Disable task via POST
- **WHEN** POST `/api/tasks/{id}/disable` is called
- **THEN** 200 OK is returned with updated task status DISABLED

#### Scenario: List tasks with pagination
- **WHEN** GET `/api/tasks?page=0&size=10` is called
- **THEN** 200 OK is returned with Page of tasks

### Requirement: ContentController REST endpoints
The system SHALL provide REST API endpoints for content management.

#### Scenario: Get content via GET
- **WHEN** GET `/api/contents/{id}` is called with existing ID
- **THEN** 200 OK is returned with content data
- **WHEN** called with non-existent ID
- **THEN** 404 Not Found is returned

#### Scenario: List content by task
- **WHEN** GET `/api/contents?taskId=1&page=0&size=10` is called
- **THEN** 200 OK is returned with Page of content items

#### Scenario: Update content via PUT
- **WHEN** PUT `/api/contents/{id}` is called with valid request
- **THEN** 200 OK is returned with updated content

#### Scenario: Delete content via DELETE
- **WHEN** DELETE `/api/contents/{id}` is called
- **THEN** 204 No Content is returned