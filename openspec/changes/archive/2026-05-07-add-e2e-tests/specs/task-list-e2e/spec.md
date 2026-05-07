## ADDED Requirements

### Requirement: Task List Page Load
The system SHALL display the task list page with all configured tasks when user navigates to the tasks endpoint.

#### Scenario: Task list page renders
- **WHEN** user navigates to `/tasks`
- **THEN** page SHALL display "任务列表" or "Task List" as page title
- **AND** task table SHALL be visible

#### Scenario: Task list loads task data
- **WHEN** task list page loads
- **THEN** system SHALL fetch tasks from GET /api/tasks
- **AND** task data SHALL be displayed in table format

#### Scenario: Empty task list
- **WHEN** no tasks exist
- **THEN** page SHALL display empty state message
- **AND** "Create Task" button SHALL be visible

### Requirement: Task List Pagination
The system SHALL support pagination for task list display.

#### Scenario: Pagination controls visible
- **WHEN** task list has multiple pages
- **THEN** pagination controls SHALL be displayed
- **AND** user SHALL be able to navigate between pages

### Requirement: Task Row Actions
The system SHALL provide action buttons for each task row.

#### Scenario: Task edit button
- **WHEN** user views task list
- **THEN** each task row SHALL have an "Edit" button
- **AND** clicking "Edit" SHALL navigate to task configuration page

#### Scenario: Task delete button
- **WHEN** user views task list
- **THEN** each task row SHALL have a "Delete" button
- **AND** clicking "Delete" SHALL show confirmation dialog

#### Scenario: Task enable/disable toggle
- **WHEN** user views task list
- **THEN** each task row SHALL have enable/disable toggle
- **AND** toggling SHALL call appropriate API endpoint
