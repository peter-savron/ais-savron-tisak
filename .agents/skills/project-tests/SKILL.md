---
name: project-tests
description: Write or refactor this project's JUnit Jupiter and Mockito tests with lifecycle-managed mocks, focused scenarios, and meaningful behavior assertions.
---

# Write clean project tests

Read root `AGENTS.md` and the current production contract. Test classes mirror the
production package under `src/test/java` and use the `*Test` suffix. Existing tests
are references, not a reason to repeat large mixed-scenario methods.

## Mock lifecycle (user requirement)

Use Mockito for collaborators. Never write a class, anonymous implementation, or
in-memory implementation of a production interface solely to act as a test mock.
Do not use `mock()` in test methods, helpers called from tests, or field initializers.
Prefer this pattern:

```java
@ExtendWith(MockitoExtension.class)
class UserServiceCliImplTest {
    @Mock
    private UserRepository repository;
    @Mock
    private UserAuthnService authentication;
    private UserServiceCliImpl service;

    @BeforeEach
    void setUp() {
        service = new UserServiceCliImpl(authentication, repository);
    }
}
```

Alternatively initialize Mockito mocks explicitly in `@BeforeEach`. Instantiate
real domain objects and the subject normally; they are not mocks. Prefer plain
unit tests over booting Quarkus unless CDI, validation interception, configuration,
or profile wiring is the behavior under test.

## Keep each scenario readable

- Name tests for an observable behavior and condition, such as
  `deleteUserRejectsWrongPasswordWithoutRemovingUser`.
- Use arrange/act/assert separation. Put shared construction in `@BeforeEach`;
  keep scenario-specific stubs close to the action. Preserve extension strictness;
  avoid broad leniency and unused shared stubs.
- Separate creation, lookup, listing, duplicate rejection, deletion success,
  wrong password, and missing-user cases. Use parameterized tests for equivalent
  malformed inputs when the existing dependencies support them.
- Build fixtures directly with domain builders or small data helpers. Do not call
  another service to arrange a unit test's data or reproduce a repository's state
  machine with `doAnswer`. Helpers should remove noise, not hide the scenario.
- Stub the collaborator result, invoke the subject, and assert the result or
  required side effect. Capture generated objects with `ArgumentCaptor` when their
  UUIDs, salts, or hashes are created by the subject.
- Verify meaningful writes and forbidden effects on failure, such as
  `verify(repository, never()).removeUser(any(UUID.class))`. Do not add blanket
  `verifyNoMoreInteractions` or assert private call order without a real contract.
- For transient keys closed after a call, a narrowly scoped `doAnswer` may copy
  bytes while the key is live; capture the handle separately to test closure.
  Do not turn that callback into a fake key store.
- Assert arrays with `assertArrayEquals`. Check password verification, independent
  salts, deterministic key derivation, and key closure rather than private helper
  structure. Use established vectors when checking cryptographic correctness;
  avoid tests that only recompute an expected value with the same method.

## Integration boundaries

A real embedded ArcadeDB under `@TempDir` is appropriate for query, mapping,
transaction, index, or pagination behavior; this is not a mock implementation.
Close database resources with try-with-resources. Use Mockito for other
collaborators; avoid passing null dependencies as a shortcut. Do not mock the SQL
engine to prove that SQL works. Keep filesystem tests inside temporary directories.

Run focused tests first and the relevant suite afterward using root guidance.
Distinguish test assertion failures from dependency resolution, JVM agent attachment,
or sandbox failures. Report actual results; do not claim validation based only on
compilation. For test-only changes, preserve production behavior.
