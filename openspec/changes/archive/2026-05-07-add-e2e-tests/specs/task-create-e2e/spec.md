## ADDED Requirements

### Requirement: Task Creation Form
The system SHALL provide a form for creating new tasks with required fields validation.

#### Scenario: Create task button navigates to form
- **WHEN** user clicks "Create Task" button
- **THEN** system SHALL navigate to task creation page
- **AND** create form SHALL be displayed

#### Scenario: Task creation form fields
- **WHEN** task creation form is displayed
- **THEN** form SHALL include task name field (required)
- **AND** form SHALL include task URL field (required)
- **AND** form SHALL include task type selector (list page / direct URL)

#### Scenario: Form validation on empty submit
- **WHEN** user submits form with empty required fields
- **THEN** validation errors SHALL be displayed
- **AND** form SHALL NOT be submitted

#### Scenario: Successful task creation
- **WHEN** user fills all required fields correctly
- **AND** user clicks "Create" button
- **THEN** system SHALL call POST /api/tasks
- **AND** system SHALL navigate back to task list
- **AND** success message SHALL be displayed

### Requirement: Task Creation Cancel
The system SHALL allow user to cancel task creation.

#### Scenario: Cancel button returns to list
- **WHEN** user clicks "Cancel" button
- **THEN** system SHALL navigate back to task list
- **AND** no task SHALL be created
