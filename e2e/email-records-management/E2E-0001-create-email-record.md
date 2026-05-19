# E2E-0001 - Create Email Record

## Metadata
- ID: E2E-0001
- Status: Proposed
- Created: 2026-05-19
- Updated: 2026-05-19
- Author Agent: Tester
- Functionality: email-records-management
- Linked User Story: US-0001
- Covered Scenario(s):
  - Authenticated user creates an email record
  - Unauthenticated user attempts to create an email record
  - Invalid input (blank value) is rejected with 400
- Related IDs: REQ-0001, REQ-0002, REQ-0003, REQ-0004, TREQ-0002, TREQ-0004, TREQ-0005, TREQ-0010, TREQ-0012
- Source Links: user-stories/email-records-management/US-0001-create-email-record.md, dev/email/adapter-rest/src/test/java/hexa/template/ai/email/adapter/rest/EmailControllerE2ETest.java

## Objective
Validate end-to-end behavior of POST /api/emails for US-0001 with real application wiring (controller, auth provider, use case, in-memory repository, and audit logger).

## Tooling
- Proposed free tool(s): Spring Boot Test (`@SpringBootTest`), Gradle test runner

## Preconditions
- Project is built successfully.
- Test profile is available for adapter-rest integration tests.
- API key `test-api-key` is configured in application wiring.

## Test Steps
1. Execute the automated integration suite including US-0001 E2E tests from dev module.
2. Verify scenario `should create email with valid API key` returns HTTP 201 with `id` and `value` and Last-Modified header semantics covered by endpoint behavior.
3. Verify scenario `should reject request with missing API key` returns HTTP 401 with expected error schema fields.
4. Verify scenario `should reject request with blank value` returns HTTP 400 with expected error schema fields.

## Expected Results
- Authenticated create succeeds and returns only public fields (`id`, `value`).
- Unauthenticated create is rejected with HTTP 401.
- Blank value is rejected with HTTP 400.
- No unexpected failures occur during integration wiring.

## Evidence Checklist
- [ ] Screenshot(s) (not required for automated suite run)
- [x] Logs
- [x] Environment details

## Execution Evidence
- Command: `gradle clean test`
- Result: `BUILD SUCCESSFUL`
- Duration: `17s`
- Notable output:
  - `Task :email:adapter-rest:compileTestJava`
  - `BUILD SUCCESSFUL in 17s`
- Environment:
  - OS: Windows
  - Runtime: OpenJDK 64-Bit Server VM (from Gradle output)
  - Module scope: `dev/email/adapter-rest`

## Execution Result
- Result: Pass
- Notes: US-0001 Gherkin-aligned scenarios are covered by `EmailControllerE2ETest`; integration behavior passed in automated execution.
