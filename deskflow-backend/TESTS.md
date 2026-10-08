# Backend Tests

Backend tests live under `src/test/java` and use JUnit 5, AssertJ, Mockito, Spring Boot Test, ArchUnit and Testcontainers.

Run all backend tests:

```bash
mvn test
```

Some integration/system tests use PostgreSQL via Testcontainers, configured in `src/test/resources/application-test.properties`, so they require a working Docker environment.

## Summary

- Total test classes: 52
- Unit/domain tests: pure Java/JUnit, no Spring context.
- Application service tests: mostly Mockito-based unit tests.
- Architecture tests: ArchUnit rules for clean architecture and domain isolation.
- Integration tests: `@DataJpaTest` with Testcontainers PostgreSQL.
- System/REST tests: `@SpringBootTest` + `MockMvc` + JWT security.
- MVC slice tests: `@WebMvcTest` for REST controllers and security/validation checks that do not require Docker.

## Architecture Tests

### `CleanArchitectureTest`

Verifies clean architecture dependency boundaries with ArchUnit:

- Domain must not depend on infrastructure.
- Domain must not depend on adapters.
- Domain must not depend on application layer.
- Application layer must not depend on infrastructure.
- Application layer must not depend on adapters.
- Domain contexts must not have cyclic dependencies.

### `DomainIsolationTest`

Verifies that the domain model stays framework-independent:

- Domain classes must not have JPA annotations.
- Domain classes must not depend on Spring packages.
- Domain classes must not depend on `jakarta.persistence`.

## Domain Unit Tests

### `AllocationRequestTest`

Tests the allocation request aggregate lifecycle:

- New request starts as `SUBMITTED`.
- Submitted, approved and rejected domain events are created.
- Approve changes status to `APPROVED`.
- Reject changes status and stores rejection reason.
- Cancel works from `SUBMITTED` and `APPROVED`.
- Cancel from `ACTIVE` is rejected.
- Issue resource changes status to `ACTIVE` and sets issue timestamp.
- Issue from `SUBMITTED` is rejected.
- Return resource from `ACTIVE` or `OVERDUE` changes status to `COMPLETED`.
- Mark overdue works only from `ACTIVE`.
- Approve after rejection/cancellation is rejected.
- Request stores input data.
- Reconstituted request has no pending events.

### `DateRangeTest`

Tests the date range value object:

- Valid range creation.
- Rejects equal start/end dates.
- Rejects start date after end date.
- Rejects null start date.
- Calculates length in days.
- Detects partial overlap.
- Detects non-overlap.
- Detects containment overlap.
- Equality and hash code behavior.
- Inequality for different dates.

### `OfficeResourceTest`

Tests the office resource aggregate:

- New resource is active and available.
- Blank name is rejected.
- Mark unavailable changes availability only.
- Mark available after unavailable.
- Deactivation makes resource inactive and unavailable.
- Deactivating twice is rejected.
- Update changes name and location.
- Cannot mark unavailable on deactivated resource.
- Resource keeps correct type and location.
- Reconstituted resource preserves persisted state.

### `LocationTest`

Tests the location value object:

- Valid location creation.
- Blank building is rejected.
- Blank number is rejected.
- Equality and hash code behavior.
- Inequality for different fields.

### `EmployeeTest`

Tests the employee domain model:

- Missing resource type limit defaults to `1`.
- Limit can be changed, including to `0`.
- Negative limit is rejected.
- Full name joins first and last name.
- Getters return expected values.
- Change limit overwrites existing limit.
- Constructor makes defensive copy of limits.
- `getLimits()` returns unmodifiable map.
- Limit for zero is valid.

### `AdministeredAllocationRequestTest`

Tests administration authorization guards:

- Approve passes from `SUBMITTED` status.
- Approve throws from `APPROVED`, `ACTIVE`, `REJECTED`, `CANCELLED`, `COMPLETED`, `OVERDUE`.
- Reject passes from `SUBMITTED` status.
- Reject throws from non-`SUBMITTED` statuses.
- Issue passes from `APPROVED` status.
- Issue throws from non-`APPROVED` statuses.
- Return passes from `ACTIVE` and `OVERDUE`.
- Return throws from `SUBMITTED`, `APPROVED`, `COMPLETED`, `REJECTED`, `CANCELLED`.

### `AdministeredAllocationRequestIdTest`

Tests administration request ID value object:

- Creates from UUID.
- Rejects null.
- Equality and hash code.
- `toString` returns UUID string.

### `AdministeredAllocationStatusTest`

Tests administration status enum:

- `from()` returns correct enum for all values.
- `from()` throws for invalid input.

### `RejectionReasonTest`

Tests rejection reason value object:

- Creates with valid reason.
- Rejects null.
- Rejects blank.
- Equality and hash code.
- `toString` representation.

### `EmployeeIdTest`

Tests employee ID value object:

- Creates from UUID and string.
- Rejects null via constructor and factory.
- Equality, inequality and hash code consistency.
- `toString` returns UUID string.

### `ResourceIdTest`

Tests resource ID value object:

- Creates from UUID and string.
- Rejects null.
- Equality and inequality.
- `toString` returns UUID string.

### `ResourceTypeTest`

Tests resource type enum:

- Has expected values and ordering.
- `valueOf()` returns correct enum.
- `valueOf()` throws for invalid input.

### `EmployeeLimitDomainServiceTest`

Tests employee allocation limit rule directly:

- Allocation is allowed when current usage is below limit.
- Allocation is rejected when current usage equals limit.
- Allocation is rejected when current usage exceeds limit.

### `ResourceAvailabilityDomainServicesTest`

Tests allocation-domain resource rules:

- Resource is available when there are no overlapping requests.
- Overlapping active requests make resource unavailable.
- Active and available resource is eligible for allocation.
- Unavailable resource is rejected for allocation.

### `AllocationLifecycleDomainServicesTest`

Tests allocation lifecycle domain services directly:

- Approve service approves submitted request.
- Reject service rejects request and stores reason.
- Reject service rejects blank reason.
- Issue service issues approved request.
- Return service completes active request.
- Overdue service marks active request as overdue.
- Cancel service cancels the employee's own request.
- Cancel service rejects cancelling another employee's request.

### `ResourceDeactivationDomainServicesTest`

Tests resource-domain deactivation rules:

- Resources cannot be modified when related allocation status is `IN_USE`, `APPROVED`, `REQUESTED` or `OVERDUE`.
- Deactivation is rejected for those blocking statuses.
- Resource can be deactivated when there are no related allocations.

### `ResourceLifecycleDomainServicesTest`

Tests resource lifecycle domain services directly:

- Update service changes resource name and location and records `ResourceUpdated`.
- Mark unavailable service marks resource unavailable and records `ResourceMadeUnavailable`.
- Mark available service marks resource available and records `ResourceMadeAvailable`.

## Application Service Unit Tests

### `AllocationRequestAppServiceTest`

Tests employee request flow with mocked ports:

- Submit saves request when employee is within limit and resource is available.
- Submit is rejected when employee limit is exceeded.
- Submit is rejected when resource is unavailable because of overlapping active request.
- Cancel changes status and saves request.

### `AllocationRequestAppServiceMissingDataTest`

Tests missing-data error paths for allocation requests:

- Submit is rejected when employee does not exist.
- Submit is rejected when resource does not exist.
- Cancel is rejected when request does not exist.

### `AllocationAdministrationAppServiceTest`

Tests administrative allocation operations:

- Approve changes status and publishes approval event.
- Reject changes status, stores reason and publishes rejection event.
- Issue changes status to `ACTIVE` and publishes issue event.
- Return changes status to `COMPLETED` and publishes return event.
- Overdue detection marks active past-due requests as `OVERDUE`.
- Approve is rejected when request does not exist.

### `AdministrationAppServiceTest`

Tests administration application service delegation and business checks:

- Approve delegates to allocation port for submitted requests.
- List requests delegates to allocation port.
- Overdue detection delegates to allocation port.
- Approve rejects non-submitted request before calling allocation port.

### `AllocationRequestViewTest`

Tests the `AllocationRequestView.from()` factory:

- Creates view with all fields for submitted request.
- Creates view for completed request with issued/returned timestamps.
- Creates view for rejected request with rejection reason.

### `ResourceAppServiceTest`

Tests resource management service with mocked ports:

- Add resource creates active and available resource and saves it.
- Update resource saves changes and publishes `ResourceUpdated`.
- Mark unavailable saves resource and publishes `ResourceMadeUnavailable`.
- Deactivate checks related allocation statuses, saves resource and publishes `ResourceDeactivated`.
- Mark unavailable is rejected when resource does not exist.

### `ResourceAppServiceMissingResourceTest`

Tests missing-resource path:

- Update resource is rejected when resource does not exist.

### `EmployeeAppServiceTest`

Tests employee service logic:

- Upsert limit changes employee limit and saves employee.
- Upsert limit is rejected when employee does not exist.
- Upsert limit is rejected for invalid resource type.

### `IdentityAccessAppServiceTest`

Tests identity access service with mocked ports:

- Login authenticates credentials and returns profile with access token.
- Login is rejected when authenticated user has no employee profile.
- Current identity returns employee identity profile by email.

### `NotificationAppServiceTest`

Tests notification service with mocked ports:

- Submitted sends email with correct subject and body.
- Approved sends email.
- Rejected sends email with rejection reason.
- Cancelled sends email.
- Resource issued sends email.
- Resource returned sends email.
- Overdue sends email with return instructions.
- Employee not found skips email.
- Employee lookup failure is caught gracefully.
- Notification failure is caught gracefully.

## Adapter/Mapper Unit Tests

### `EmployeeProfileAdapterTest`

Tests the allocation-to-employee profile adapter:

- Returns profile with limits when employee found.
- Returns empty when employee not found.
- Returns empty limits when employee has none.

### `ResourceCatalogAdapterTest`

Tests the allocation-to-resource catalog adapter:

- `findResource` returns mapped profile when found.
- `findResource` returns empty when not found.
- `findActiveResources` returns all active.
- `findActiveResources` returns empty when none.

### `AllocationAdministrationAdapterTest`

Tests the administration-to-allocation adapter:

- `getRequest` returns admin view when found.
- `getRequest` returns empty when not found.
- `listRequests` converts status and maps.
- `listRequests` passes null status.
- `approveRequest`, `rejectRequest`, `issueResource`, `returnResource` delegate correctly.
- `monitorActiveAllocations` returns mapped views.
- `detectOverdueAllocations` delegates.

### `AllocationRequestAdapterTest`

Tests the resources-to-allocation adapter (status mapping):

- `SUBMITTED` maps to `REQUESTED`.
- `APPROVED` maps to `APPROVED`.
- `ACTIVE` maps to `IN_USE`.
- `OVERDUE` maps to `OVERDUE`.
- `REJECTED`, `CANCELLED`, `COMPLETED` map to `CLOSED`.
- Unknown status throws.
- Returns multiple statuses.

### `AllocationRequestRestMapperTest`

Tests REST DTO mapping:

- `toResponse` maps all fields without employee names.
- `toResponse` with employee names sets firstName/lastName.
- `toResponse` maps completed request with timestamps.
- `toResponse` maps rejected request with reason.

### `EmployeeUserCredentialsAdapterTest`

Tests identity-to-employee credentials adapter:

- `findByEmail` returns mapped credentials when found.
- `findByEmail` returns empty when not found.

### `EmployeeIdentityProfileAdapterTest`

Tests identity-to-employee profile adapter:

- `findByEmail` returns mapped profile when found.
- `findByEmail` returns empty when not found.

### `SpringDomainEventPublisherAdapterTest`

Tests domain event publisher adapter:

- Event publishing delegates to Spring `ApplicationEventPublisher`.

### `GlobalExceptionHandlerTest`

Tests global REST exception handling:

- `IllegalStateException` returns 409 CONFLICT.
- `IllegalArgumentException` returns 400 BAD_REQUEST.
- `NoSuchElementException` returns 404 NOT_FOUND.
- `MethodArgumentNotValidException` returns 400 with details.

### `RestControllerWebMvcTest`

Tests REST controller behavior, security and validation without loading persistence/Testcontainers:

- Login returns identity data and token.
- `/api/auth/me` returns current identity for a valid token.
- `/api/auth/me` returns 404 when the profile is missing.
- Resource browsing is protected and returns catalog data for authenticated users.
- Employee role cannot add resources.
- Manager can add resources.
- Resource creation rejects blank location fields, invalid resource type, malformed JSON and missing `Content-Type`.
- Manager can mark resources available/unavailable.
- Resource path variables reject invalid UUID values.
- Only IT admin can delete resources.
- Administration can list employees with limits.
- Administration can update an employee limit and later list the updated employee.
- Employee limit update rejects negative amount and invalid resource type.
- Administration request listing applies status filter and enriches employee names.
- Invalid administration status filter returns 400.
- Invalid lifecycle operation returns 409 conflict.
- Submit request maps date-range business validation to 400.
- Missing resource from controller returns 404.

## Security Unit Tests

### `JwtTokenProviderTest`

Tests JWT token behavior:

- Generated token contains email and role claims.
- Invalid token is rejected.
- Expired token is rejected.

### `DeskflowUserDetailsServiceTest`

Tests Spring Security user details service:

- `loadUserByUsername` returns user with email, password and role.
- `loadUserByUsername` throws when user not found.

## Event Listener Tests

### `NotificationEventListenerTest`

Tests that notification event listeners delegate correctly:

- `onRequestSubmitted` delegates to input port.
- `onRequestApproved` delegates to input port.
- `onRequestRejected` delegates to input port with reason.
- `onRequestCancelled` delegates to input port.
- `onResourceIssued` delegates to input port.
- `onResourceReturned` delegates to input port.
- `onAllocationOverdue` delegates to input port.

### `ResourceAvailabilityEventListenerTest`

Tests resource availability event listeners:

- `onResourceIssued` calls `markUnavailable`.
- `onResourceReturned` calls `markAvailable`.

## Scheduler Tests

### `OverdueAllocationSchedulerTest`

Tests the overdue detection scheduler:

- `detectOverdueAllocations` delegates to `AdministrationAppService`.

## Outbound Adapter Tests

### `SmtpNotificationAdapterTest`

Tests SMTP notification sending:

- Sends email with correct to, subject, body, from fields.
- Mail exception is caught and logged.
- Sends to different recipients.

## JPA Integration Tests

These tests use `@DataJpaTest`, `@ActiveProfiles("test")`, `@AutoConfigureTestDatabase(replace = NONE)` and PostgreSQL via Testcontainers.

### `AllocationRequestRepositoryIntegrationTest`

Tests allocation request persistence adapter:

- Save and find allocation request.
- Find overdue active past-due requests.
- Find active requests for resource with overlapping date range.

### `AllocationRequestRepositoryDateEdgeCasesIntegrationTest`

Tests date-overlap edge cases in allocation repository queries:

- Search starting exactly on request end is included as overlapping.
- Request ending before search start is not included.
- Request fully inside search range is included.

### `OfficeResourceRepositoryIntegrationTest`

Tests office resource persistence adapter:

- Save and find resource.
- Find active returns only active resources.
- Find active returns empty when there are no active resources.
- Find returns empty when resource is missing.

### `EmployeeRepositoryIntegrationTest`

Tests employee persistence adapter:

- Save and find employee with all fields.
- Find by email.
- Find by email returns empty when missing.
- Find returns empty when missing.
- Save and load allocation limits.
- Find all returns all employees.

## REST/System Tests

These tests use `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")`, JWT tokens and the real Spring security filter chain. They also require the Testcontainers database.

### `FullAllocationCycleSystemTest`

Tests end-to-end REST allocation workflows:

- Full cycle: submit request, approve, issue resource, return resource.
- Employee can cancel submitted request.

### `AuthorizationSystemTest`

Tests HTTP security rules:

- Admin endpoint requires authentication.
- Invalid token is unauthorized.
- Employee role cannot access admin endpoints.
- Manager role can access admin endpoints.
- Resource browsing requires authentication.
- Auth login endpoint is public but still validates input.
- Manager cannot delete resources.
- IT admin is authorized to delete resources.

### `InputValidationSystemTest`

Tests REST request validation:

- Submit request requires `resourceId`.
- Submit request requires `dateFrom`.
- Submit request rejects past `dateFrom`.
- Submit request requires `dateTo`.
- Add resource requires `type`.
- Add resource requires non-blank `name`.
- Reject request requires non-blank reason.

## Useful Commands

Run only fast unit tests (no Docker needed):

```bash
mvn test -Dtest='!*SystemTest,!*IntegrationTest'
```

Run only domain service tests:

```bash
mvn test -Dtest=AllocationLifecycleDomainServicesTest,EmployeeLimitDomainServiceTest,ResourceLifecycleDomainServicesTest,ResourceAvailabilityDomainServicesTest,ResourceDeactivationDomainServicesTest
```

Run only architecture tests:

```bash
mvn test -Dtest=CleanArchitectureTest,DomainIsolationTest
```

Run only JPA integration tests:

```bash
mvn test -Dtest=AllocationRequestRepositoryIntegrationTest,AllocationRequestRepositoryDateEdgeCasesTest,OfficeResourceRepositoryIntegrationTest,EmployeeRepositoryIntegrationTest
```

Run only REST/system tests:

```bash
mvn test -Dtest=FullAllocationCycleSystemTest,AuthorizationSystemTest,InputValidationSystemTest
```
