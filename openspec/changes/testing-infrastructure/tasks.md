## 1. Test Infrastructure Setup

- [x] 1.1 Add H2 dependency to pom.xml (test scope)
- [x] 1.2 Create src/test/java directory structure (controller/, service/, repository/, fixture/)
- [x] 1.3 Create src/test/resources directory
- [x] 1.4 Create application-test.yml with H2 datasource configuration
- [x] 1.5 Create schema.sql with PostgreSQL-compatible table definitions
- [x] 1.6 Create TaskFixtures.java with static factory methods (draftTask, enabledTask, disabledTask, runningTask, enabledTaskWithFields)
- [x] 1.7 Create FieldFixtures.java with static factory methods (textField, imageField, linkField, richTextField)

## 2. Repository Layer Tests

- [x] 2.1 Create SpiderTaskRepositoryTest with @DataJpaTest
- [x] 2.2 Test save (new task returns with ID)
- [x] 2.3 Test findById (existing and non-existing)
- [x] 2.4 Test delete
- [x] 2.5 Test findAll with pagination
- [x] 2.6 Create SpiderFieldRepositoryTest with @DataJpaTest
- [x] 2.7 Test save and findByTaskId
- [x] 2.8 Test deleteById
- [x] 2.9 Create ContentItemRepositoryTest with @DataJpaTest
- [x] 2.10 Test save, findByTaskId, findByStatus with pagination
- [x] 2.11 Test deleteById

## 3. Service Layer Tests

- [x] 3.1 Create SpiderTaskServiceTest with @ExtendWith(MockitoExtension)
- [x] 3.2 Test createTask returns DRAFT status
- [x] 3.3 Test getTaskById throws EntityNotFoundException for non-existing
- [x] 3.4 Test updateTask
- [x] 3.5 Test deleteTask
- [x] 3.6 Test listTasks with pagination
- [x] 3.7 Test enableTask (DRAFT→ENABLED, DISABLED→ENABLED)
- [x] 3.8 Test enableTask throws IllegalStateException for ENABLED or RUNNING
- [x] 3.9 Test disableTask (ENABLED→DISABLED)
- [x] 3.10 Test disableTask throws IllegalStateException for RUNNING
- [x] 3.11 Test runTask changes status to RUNNING
- [x] 3.12 Create CrawlerEngineTest with mocked dependencies
- [x] 3.13 Test execute with LIST_PAGE mode parses list page and extracts content
- [x] 3.14 Test execute with DIRECT_URL mode processes seed URLs directly
- [x] 3.15 Create ContentServiceTest with @ExtendWith(MockitoExtension)
- [x] 3.16 Test getContentById
- [x] 3.17 Test getContentById throws EntityNotFoundException
- [x] 3.18 Test listContentByTask
- [x] 3.19 Test deleteContent

## 4. Controller Layer Tests

- [x] 4.1 Create SpiderTaskControllerTest with @WebMvcTest(SpiderTaskController.class)
- [x] 4.2 Test POST /api/tasks returns 201
- [x] 4.3 Test POST /api/tasks with invalid request returns 400
- [x] 4.4 Test GET /api/tasks/{id} returns 200 with task data
- [x] 4.5 Test GET /api/tasks/{id} with non-existing ID returns 404
- [x] 4.6 Test PUT /api/tasks/{id} returns 200
- [x] 4.7 Test DELETE /api/tasks/{id} returns 204
- [x] 4.8 Test POST /api/tasks/{id}/enable returns 200
- [x] 4.9 Test POST /api/tasks/{id}/enable with already enabled returns 409
- [x] 4.10 Test POST /api/tasks/{id}/disable returns 200
- [x] 4.11 Test GET /api/tasks with pagination returns 200
- [x] 4.12 Create ContentControllerTest with @WebMvcTest(ContentController.class) - SKIPPED (ContentController全是TODO)
- [ ] 4.13 Test GET /api/contents/{id} returns 200 - SKIPPED
- [ ] 4.14 Test GET /api/contents/{id} with non-existing returns 404 - SKIPPED
- [ ] 4.15 Test GET /api/contents with taskId returns 200 - SKIPPED
- [ ] 4.16 Test PUT /api/contents/{id} returns 200 - SKIPPED
- [ ] 4.17 Test DELETE /api/contents/{id} returns 204 - SKIPPED

## 5. Build Verification

- [ ] 5.1 Run `./mvnw test` to verify all tests pass
- [ ] 5.2 Verify test coverage meets targets (Service/Repository 80%+, Controller 70%+)
- [ ] 5.3 Commit all test files