## ADDED Requirements

### Requirement: Task Configuration Page Load
The system SHALL display task configuration page with existing task data when user navigates to edit a task.

#### Scenario: Load task configuration
- **WHEN** user navigates to task configuration page for task ID 1
- **THEN** system SHALL call GET /api/tasks/1
- **AND** existing task data SHALL populate the form

#### Scenario: Configuration page fields
- **WHEN** task configuration page loads
- **THEN** form SHALL display task name (editable)
- **AND** form SHALL display task URL (editable)
- **AND** form SHALL display field configuration section

### Requirement: Task Field Configuration
The system SHALL allow user to configure custom fields for data extraction.

#### Scenario: Add field button
- **WHEN** user is on task configuration page
- **THEN** "Add Field" button SHALL be visible
- **AND** clicking "Add Field" SHALL add new field row

#### Scenario: Field configuration row
- **WHEN** field row is added
- **THEN** user SHALL be able to set field name
- **AND** user SHALL be able to set field type (text/image/link/richText)
- **AND** user SHALL be able to set CSS selector

### Requirement: Save Task Configuration
The system SHALL save task configuration changes when user clicks Save.

#### Scenario: Save configuration
- **WHEN** user modifies task configuration
- **AND** clicks "Save" button
- **THEN** system SHALL call PUT /api/tasks/{id}
- **AND** success message SHALL be displayed
- **AND** system SHALL navigate back to task list

#### Scenario: Discard changes
- **WHEN** user clicks "Discard" button
- **THEN** system SHALL navigate back to task list
- **AND** no changes SHALL be saved
