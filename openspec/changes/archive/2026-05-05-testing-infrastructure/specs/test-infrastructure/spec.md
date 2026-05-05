## ADDED Requirements

### Requirement: Test infrastructure setup
The system SHALL provide complete test infrastructure including H2 in-memory database, test configuration, and reusable fixtures.

#### Scenario: H2 dependency is configured
- **WHEN** Maven build runs with `mvn test`
- **THEN** H2 database is available in test classpath

#### Scenario: Application test configuration loads
- **WHEN** Spring Boot test context initializes
- **THEN** `application-test.yml` is used with H2 datasource configuration

#### Scenario: Schema initialization runs
- **WHEN** H2 datasource is created for tests
- **THEN** `schema.sql` is executed to create tables

### Requirement: TaskFixtures provides test data builders
The test fixtures SHALL provide static factory methods for creating SpiderTask entities in various states.

#### Scenario: Create draft task
- **WHEN** `TaskFixtures.draftTask()` is called
- **THEN** a SpiderTask with status DRAFT is returned

#### Scenario: Create enabled task
- **WHEN** `TaskFixtures.enabledTask()` is called
- **THEN** a SpiderTask with status ENABLED is returned

#### Scenario: Create disabled task
- **WHEN** `TaskFixtures.disabledTask()` is called
- **THEN** a SpiderTask with status DISABLED is returned

#### Scenario: Create task with fields
- **WHEN** `TaskFixtures.enabledTaskWithFields()` is called
- **THEN** a SpiderTask with associated SpiderFields is returned

### Requirement: FieldFixtures provides test data builders
The test fixtures SHALL provide static factory methods for creating SpiderField entities of various types.

#### Scenario: Create text field
- **WHEN** `FieldFixtures.textField()` is called
- **THEN** a SpiderField with type TEXT is returned

#### Scenario: Create image field
- **WHEN** `FieldFixtures.imageField()` is called
- **THEN** a SpiderField with type IMAGE is returned

#### Scenario: Create link field
- **WHEN** `FieldFixtures.linkField()` is called
- **THEN** a SpiderField with type LINK is returned

#### Scenario: Create richText field
- **WHEN** `FieldFixtures.richTextField()` is called
- **THEN** a SpiderField with type RICHTEXT is returned