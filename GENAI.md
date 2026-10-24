# GenAI usage

Exercise: generate a REST API for a simple task manager (CRUD on tasks with `title`, `description`, `status`, `due_date`, each task owned by a user), then show the prompt, a sample of the output, and how I validated and corrected it.

I used Claude (chat) as a pair-programmer. I'd rather describe how I actually work with it than present a polished story, so this doc covers both the exercise and what happened while building the Pokedex project.

---

## 1. How I work with it

My rule of thumb: the model writes, I own. I treat generated code like a PR from a fast junior who has read a lot but hasn't seen my codebase. That shapes the workflow:

1. **Plan before code.** Ask for a plan (layout, endpoints, assumptions) and fix the plan first. Cheaper to correct a bullet than 40 files.
2. **Constraints up front.** Architecture rules, what's forbidden, error format, test expectations. Models drift toward the average Spring tutorial unless told otherwise.
3. **Vertical slices.** One feature end to end, with tests, then the next. Big-bang generation is where subtle bugs hide.
4. **Run everything.** Compiler, linter, tests. "Looks right" doesn't count.

The Pokedex project followed exactly that chain. The prompts, in order:

| # | What I asked for | Why in this order |
|---|---|---|
| 1 | A step-by-step roadmap from the task description, split backend / frontend | Turns the brief into a spec I can check against later |
| 2 | The base backend (Java, Spring Boot) following that roadmap, zipped | Implementation anchored to the roadmap, not to the model's defaults |
| 3 | The Vue frontend, "following the roadmap and calling the existing backend" | The backend DTOs act as the contract, so the frontend isn't guessed |
| 4 | The interview presentation from the evaluation criteria | Forces me to re-read the whole thing critically |

Using the roadmap as the shared spec was the most useful trick: every later prompt could say "follow the roadmap" instead of re-explaining the architecture.

---

## 2. The prompt (task management API)

This is what I'd send to Claude Code / Cursor for the exercise. It's long on purpose; the constraints are the valuable part.

````text
You are helping me build a REST API for a task management system. Work like a
careful senior engineer: small steps, tests first, no surprises.

## Stack
Java 21, Spring Boot 3.3, Maven, Spring Data JPA, Flyway, PostgreSQL (H2 for
local/tests), Spring Security with stateless JWT (jjwt 0.12), springdoc-openapi,
JUnit 5, Mockito, MockMvc.

## Architecture (strict Clean Architecture)
Packages under com.example.tasks:
  domain/          records + domain exceptions. No Spring, no JPA.
  application/     use cases (plain Java) + ports (interfaces).
  infrastructure/  JPA adapters, JWT, BCrypt, config. Use cases are wired in a
                   @Configuration class, NOT annotated with @Service.
  presentation/    controllers, request/response DTOs, @RestControllerAdvice.
Dependencies point inward only. Never return JPA entities from controllers.

## Domain
User: id, username (unique), email (unique), passwordHash.
Task: id, ownerId, title (required, 1-120 chars), description (optional, max 2000),
status (TODO | IN_PROGRESS | DONE, default TODO), dueDate (LocalDate, optional,
JSON name "due_date"), version (optimistic locking), createdAt, updatedAt.

## Endpoints
POST   /api/auth/register, /api/auth/login
POST   /api/tasks
GET    /api/tasks            paged; filters: status, due_before; sort whitelist
GET    /api/tasks/{id}
PUT    /api/tasks/{id}
PATCH  /api/tasks/{id}/status
DELETE /api/tasks/{id}

## Security rules
- Everything except /api/auth/** requires a valid JWT.
- A user can only see and modify their own tasks. Resolve the owner from the
  token, never from the request body. A task owned by someone else must behave
  exactly like a task that does not exist (404), so ids can't be enumerated.
- BCrypt for passwords. No sensitive data in logs or error messages.

## Validation and errors
- Bean Validation at the edge for shape (sizes, required).
- Business rules in the use cases (status transitions, due date rules), not in
  controllers.
- Errors as RFC 7807 ProblemDetail: 400 validation/malformed JSON, 401, 404,
  409 (version conflict), 500 without stack traces.
- Pagination: default size 20, max 100, reject invalid values with 400.

## Process
1. First reply ONLY with: package tree, endpoint table, and every assumption
   you are making. Wait for my confirmation.
2. Then implement one vertical slice at a time, starting with "create task".
   Write the tests first, show them failing, then the implementation.
3. After each slice, list what you did NOT cover.

## Do not
- Accept ownerId/status transitions from the client unchecked.
- Use deprecated APIs (WebSecurityConfigurerAdapter, etc.).
- Add a dependency without telling me why.
- Invent library versions: if unsure, say so.
````

Why it's written this way:

- **"Reply ONLY with a plan first"** catches wrong assumptions early (e.g. does DONE allow reopening?).
- **The 404-for-foreign-tasks rule** is stated explicitly because models default to either leaking a 403 or, worse, forgetting the ownership check entirely.
- **"Do not" list** targets the failure modes I see repeatedly (listed in section 4).
- **"Say so if unsure about versions"** gives the model permission to be uncertain instead of hallucinating a plausible-looking artifact version.

---

## 3. Representative output

Not the whole project, just the pieces that carry the interesting decisions.

**Status transitions live in the domain**, not in a controller `if`:

```java
public enum TaskStatus {
    TODO, IN_PROGRESS, DONE;

    public boolean canTransitionTo(TaskStatus next) {
        return switch (this) {
            case TODO        -> next == IN_PROGRESS || next == DONE;
            case IN_PROGRESS -> next == TODO || next == DONE;
            case DONE        -> next == IN_PROGRESS; // reopen only
        };
    }
}

public record Task(Long id, Long ownerId, String title, String description,
                   TaskStatus status, LocalDate dueDate, Long version) { }
```

**The use case enforces ownership, versioning and business rules** (plain Java, no Spring):

```java
public class UpdateTaskUseCase {
    private final TaskRepository tasks;
    private final Clock clock;

    public UpdateTaskUseCase(TaskRepository tasks, Clock clock) {
        this.tasks = tasks;
        this.clock = clock;
    }

    public Task execute(UpdateTaskCommand cmd) {
        // Scoped by owner: someone else's task is indistinguishable from a missing one
        Task current = tasks.findByIdAndOwnerId(cmd.id(), cmd.ownerId())
                .orElseThrow(() -> new NotFoundException("Task " + cmd.id() + " not found"));

        if (cmd.expectedVersion() != null && !cmd.expectedVersion().equals(current.version())) {
            throw new ConflictException("Task was modified by someone else");
        }
        if (cmd.status() != current.status() && !current.status().canTransitionTo(cmd.status())) {
            throw new ValidationException(
                    "Cannot move a task from " + current.status() + " to " + cmd.status());
        }
        // Only reject a past due date when the client is actually changing it,
        // otherwise editing the title of an overdue task would fail.
        if (cmd.dueDate() != null && !cmd.dueDate().equals(current.dueDate())
                && cmd.dueDate().isBefore(LocalDate.now(clock))) {
            throw new ValidationException("due_date cannot be in the past");
        }

        return tasks.save(new Task(current.id(), current.ownerId(), cmd.title().trim(),
                blankToNull(cmd.description()), cmd.status(), cmd.dueDate(), current.version()));
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
```

**The controller stays thin.** The owner comes from the token, never from the body:

```java
@RestController
@RequestMapping("/api/tasks")
class TaskController {
    // constructor injection of use cases omitted

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    TaskResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                        @Valid @RequestBody CreateTaskRequest body) {
        return TaskResponse.from(createTask.execute(
                new CreateTaskCommand(user.id(), body.title(), body.description(), body.dueDate())));
    }

    @GetMapping("/{id}")
    TaskResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable long id) {
        return TaskResponse.from(getTask.execute(id, user.id()));
    }
}

record CreateTaskRequest(
        @NotBlank @Size(max = 120) String title,
        @Size(max = 2000) String description,
        @JsonProperty("due_date") LocalDate dueDate) { }
```

**Persistence only ever queries through the owner:**

```java
public interface TaskJpaRepository extends JpaRepository<TaskEntity, Long> {
    Optional<TaskEntity> findByIdAndOwnerId(Long id, Long ownerId);
    Page<TaskEntity> findByOwnerId(Long ownerId, Pageable pageable);
    Page<TaskEntity> findByOwnerIdAndStatus(Long ownerId, TaskStatus status, Pageable pageable);
}
```

**A test that matters more than most:**

```java
@Test
void cannotUpdateSomeoneElsesTask() {
    when(tasks.findByIdAndOwnerId(10L, 2L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> useCase.execute(command(10L, 2L, TaskStatus.DONE)));

    verify(tasks, never()).save(any());
}
```

The same behavior is covered end to end with MockMvc: user A creates a task, user B gets a 404 on it, and A can still read it.

---

## 4. How I validated the suggestions

### The generic checklist

Before I trust any generated backend code I go through the same list:

- **It compiles and the tests pass.** On my machine, not in the model's head.
- **Authorization.** Every query touching user data is scoped by owner? Is any id accepted from the body that should come from the token? (IDOR is the classic miss.)
- **Entities vs DTOs.** Is anything persistence-shaped leaking out of a controller?
- **N+1 and unbounded queries.** Lazy collections in list endpoints, missing page caps.
- **Dependencies are real.** The artifact and version exist, and the API matches that version (jjwt 0.12 changed its builder API; plenty of generated snippets still use the old one).
- **Deprecations.** `WebSecurityConfigurerAdapter`, `antMatchers`, `@MockBean` on newer Boot versions.
- **Error handling.** No stack traces or internals in responses, and one consistent error shape.
- **Do the tests assert behavior** or just that the mock was called?

### What I actually did on this project

- **Frontend: fully machine-checked.** `vue-tsc`, ESLint, Vitest (17 tests) and a production build all ran and pass.
- **Backend: written but not compiled by the assistant.** The sandbox it worked in had a JRE but no `javac` or Maven, so it couldn't build the Java project. I treated the backend as unverified until I run `mvn verify` myself. [Fill in after running: result and any fixes.] I think saying this plainly is more useful than pretending it was verified.
- **Contract between the two halves.** The frontend types were written from the actual backend DTOs (`sprite`, `image`, `localizedName`, `version`, the RFC 7807 `errors` map), not from the model's memory of "what a Pokemon API looks like". I still need to confirm it live with both running together. [Confirm after the first end-to-end run.]
- **Read the diff, not just the result.** That's where most of the corrections below came from.

---

## 5. What I corrected or improved

### Real issues found on this project

| Issue | How it surfaced | Fix |
|---|---|---|
| A shell command using `mkdir -p src/{api,types,...}` ran under `/bin/sh`, which doesn't expand braces. It created one literal directory and half the files silently failed to write | stderr said "Directory nonexistent" | Recreated the directories explicitly and rewrote the failed files. Lesson: read stderr, list the tree, don't assume |
| `vue-tsc` failed on `vite.config.ts` | Type check | Missing Node typings: added `@types/node` and `"node"` in tsconfig types |
| ESLint formatting warnings in the Vue templates | Lint run | Auto-fixed, then re-ran lint clean |
| README told users to run `./mvnw`, but no wrapper was generated | Reading the docs against the file list | Replaced with `mvn` |
| `ErrorState` titled every non-HTTP error "Connection problem", and one caller passed a plain object instead of an `Error` | Reading the component against its call sites | Removed the misleading title and passed a real `Error` |
| The initial H2 setup was in-memory only, so nothing could connect to it from outside | My own question while using it | Documented the console route and the file-mode alternative |

### Failure modes I review for, regardless of tool

- Controllers returning entities instead of DTOs.
- Ownership checks present on `GET` but forgotten on `PUT` or `DELETE`.
- Business rules scattered in controllers rather than the domain.
- Over-mocked tests that assert interactions and prove nothing. I prefer at least one MockMvc test that runs the real security chain against H2.
- Confident but outdated security config.

### Design calls where I overrode or refined the default

- **Use cases as plain classes wired in config**, so the application layer has no Spring import. The default `@Service` everywhere is faster to write and breaks the "independent of frameworks" requirement.
- **Optimistic locking surfaced to the client** (`version` in the PUT body, 409 on mismatch), not just left to the database to throw.
- **Sync as an upsert that preserves custom fields**, with a unit test, because "re-sync wipes my edits" is the kind of bug nobody notices until production.

---

## 6. Edge cases, authentication and validation

### Authentication and authorization

- Stateless JWT, short lifetime, BCrypt-hashed passwords, secret from the environment (the app refuses to start with a secret under 32 bytes).
- 401 for missing, invalid or expired tokens. The frontend reacts to a 401 on an authenticated request by signing the user out and redirecting to login.
- Ownership is enforced in the **query** (`findByIdAndOwnerId`), not by fetching then comparing. That makes the safe path the default path.
- **404 instead of 403** for other people's tasks. A 403 confirms the id exists, which helps enumeration.
- CORS restricted to the frontend origin, CSRF disabled only because there are no cookie sessions.

### Validation: two layers on purpose

- **Edge** (Bean Validation): required fields, max lengths, shape. Gives field-level errors the UI can render under inputs.
- **Domain** (use cases): status transitions, due-date rules, tag limits, version conflicts. These must hold no matter which entry point calls them.
- The frontend mirrors the rules for fast feedback, but the backend is the source of truth.

### Edge cases I made explicit

| Case | Behavior |
|---|---|
| Malformed JSON or wrong field types | 400, ProblemDetail |
| Blank title, over-long fields | 400 with a per-field `errors` map |
| `page < 0`, `size = 0`, `size > max` | 400, not a silent clamp |
| Non-numeric id in the path | 400 |
| Task belongs to another user | 404, same as not found |
| Illegal status move (e.g. DONE to TODO) | 400 with a readable message |
| Past `due_date` on create or change | 400 |
| Editing an already-overdue task without touching its date | Allowed |
| Two clients editing the same task | Second write gets 409, UI offers "reload latest" |
| Client sends `ownerId` in the body | Ignored: the DTO doesn't have the field |
| Sort by an arbitrary column | Only whitelisted fields, otherwise 400 |
| Unexpected exception | 500 with a generic message, details only in server logs |
| Upstream dependency down (Pokedex case) | 502 with a friendly message and a retry in the UI |

### Not done yet (and I'd say so in the review)

- Rate limiting and login throttling.
- Refresh tokens or an HttpOnly cookie instead of `localStorage` for the JWT.
- Testcontainers tests against real PostgreSQL (H2 in PostgreSQL mode is close but not identical).
- Correlation-ID logging.

---

## 7. What I'd tell someone starting out with these tools

- A good prompt is mostly constraints and a process, not a feature list.
- Make the model show its plan and assumptions first. Most bad output starts as a wrong assumption.
- Keep a written spec (the roadmap here) and point every later prompt at it.
- Ask what it did *not* cover. It's often honest about that if you ask.
- The review is the job. Speed comes from the generation, quality from the reading.
