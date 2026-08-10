# Changelog

All notable changes to the **E-Commerce Service** are documented in this file.

This changelog is written **by hand, for people**. It is not generated from commit messages. Its job
is to answer one question quickly: *"What actually changed, and does it affect me?"* Entries are
therefore written in plain language and describe impact rather than implementation.

The format is based on [Keep a Changelog 1.1.0](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html).

---

## How to read this file

Each released version has its own section, newest first, with the release date in `YYYY-MM-DD`
format. Within a version, changes are grouped under standard headings:

| Heading | Meaning |
| :--- | :--- |
| **Added** | New capabilities that did not exist before. |
| **Changed** | Existing behaviour that now works differently. |
| **Deprecated** | Still works, but is scheduled for removal — start migrating. |
| **Removed** | Gone. If you depended on it, you must change something. |
| **Fixed** | A defect that is no longer present. |
| **Security** | A vulnerability that has been addressed. |

Anything marked **⚠️ BREAKING** requires action from anyone consuming this project.

### The `[Unreleased]` section

`[Unreleased]` collects work that has been merged into `develop` but not yet cut into a release.

**Every pull request that changes behaviour must update it in the same pull request.** This is a
deliberate rule: a changelog reconstructed from memory at release time is always incomplete, and
always written by whoever remembers least. Writing the entry while the change is fresh costs a minute
and produces something honest.

When a release is cut, `[Unreleased]` is renamed to the new version number, given a date, and a fresh
empty `[Unreleased]` section is opened above it.

---

## [Unreleased]

### Added

- **The service now does something.** `v0.1.0` contained no application functionality at all — every
  path returned a 404 because there was not a single controller in the codebase. There is now a
  working REST endpoint.
- **`GET /api/users`** returns every user the service currently holds, as a JSON array.

  When there are no users it returns `200 OK` with `[]`, rather than a `404` or an empty body.
  *"Show me all users"* is a question with a valid answer when the answer is none, and returning an
  error for it would force every client to special-case something that is not exceptional.
- **`GET /api/users/{id}`** returns a single user by id, so callers no longer have to download the
  whole collection and search it themselves. A non-numeric id such as `/api/users/abc` is rejected
  with `400` by the framework before it reaches any application code.

  ⚠️ **A user that does not exist currently returns `200 OK` with an empty body, not `404`.** The
  lookup returns `null`, Spring has nothing to serialise, and the caller receives a success status
  for a request that did not succeed — indistinguishable from a genuine empty response. This is
  documented in the code rather than fixed here, so the endpoint lands as one reviewable change.

  Note this is the opposite call from `GET /api/users`, deliberately: asking for *all* users when
  there are none has a valid answer (`[]`); asking for *one specific* user that does not exist does
  not.
- **`POST /api/users`** accepts a JSON body and adds a user, so the listing endpoint can actually be
  exercised. It currently replies `200 OK` with the plain-text sentence `User Added Successfully` —
  see **Changed** below for why that is recorded as interim rather than intended.
- **A `User` model** holding `id`, `firstName` and `lastName`, with Lombok's `@Data` generating the
  getters, setters, `equals`, `hashCode` and `toString`.
- **A `UserService` layer.** The user collection and the operations on it moved out of the controller
  into a dedicated `@Service`. `UserController` now handles only HTTP and delegates the actual work.

  The payoff is not tidiness for its own sake: when the database arrives, replacing the in-memory
  list becomes a change to **one file**. The controller does not know the difference, and neither
  does any client.

  The service is injected through the constructor — Lombok's `@RequiredArgsConstructor` on a `final`
  field — rather than with `@Autowired` on the field. That means the dependency can never be null or
  reassigned, and a plain unit test can build the controller with `new UserController(service)`
  without starting Spring at all.

- **User ids are now assigned by the server.** `POST /api/users` previously stored whatever `id` the
  caller sent, and stored `null` when they sent none. The service now overwrites it with the next
  value in its own sequence, so a request carrying `{"id":999,...}` is stored with the next sequential
  id instead.

  This is the right way round. An id is how a resource is addressed, so letting clients choose means
  two of them eventually choose the same one and neither record can be identified afterwards.
  Settling it now also matters because once a database holds rows with client-chosen ids, correcting
  it needs a data migration rather than a code change.

### Changed

- **`UserService.addUser` returns nothing instead of the full user list.** The controller never read
  the returned list, and a return value nobody reads is a small lie about what a method is for.
- **⚠️ `POST /api/users` no longer returns JSON.** It previously replied `200 OK` with the full user
  collection as a JSON array. It now replies `200 OK` with the plain-text sentence
  `User Added Successfully`, sent as `text/plain;charset=UTF-8`.

  Two consequences worth stating plainly, because both are recorded as interim rather than intended:

  - **The two endpoints on this path now disagree about content type.** `GET` answers with
    `application/json`, `POST` with `text/plain`, so a caller cannot parse every response from this
    API the same way.
  - **The response tells a program nothing useful.** It confirms something worked, but does not say
    *which* user was created or where to find it, so a client needing the new id has no way to get it
    short of re-fetching the whole collection and guessing.

  The intended shape remains `201 Created` with the created user as the body and a `Location` header.
  Changing it is free right now and stops being free the moment anything consumes this endpoint.

### Known limitations

These are **deliberate scope limits** on an early vertical slice, not oversights. Each is tracked as
its own follow-up issue:

- **Nothing is persisted.** Users are held in an in-memory list and are lost when the application
  stops. There is no database yet.
- **Not safe under concurrent load — and this is measured, not theoretical.** Spring shares a single
  `UserService` instance across all requests. Neither the backing `ArrayList` nor the id counter is
  built to be written to from several threads at once, because both perform operations that look
  like one step in the source but are several once they run.

  Firing 300 simultaneous `POST` requests at a running instance produced:

  ```text
  Users stored  : 294  (expected 300)  -- 6 users vanished
  Duplicate ids : 16                   -- e.g. id 18 given to 2 different users
  ```

  The lost users come from `ArrayList.add` (read the size, write the slot, store the new size). The
  duplicate ids come from `nextId++` (read, add one, write back). Of the two, duplicate ids are the
  more serious: an id is how a user is addressed, so once two share one, there is no way to say which
  was meant.
- **`fetchAllUsers` returns the live internal list**, not a copy, so a caller ends up holding the
  service's own data.
- **No validation.** A request with no first name, no last name, or empty strings for both is
  accepted and stored exactly as sent.
- **Ids do not survive a restart.** The counter resets to 1 on every boot and nothing is persisted,
  so a restarted application will re-issue ids a previous run already used.
- **Layering is only half done.** A service now sits between the controller and the data, but
  everything still lives in one package, there are no DTOs separating the API contract from the
  stored model, and storage is not yet behind a repository interface.

---

## [0.1.0] — 2026-08-07

**Repository Foundation.**

This first tagged release contains **no application functionality**. Its entire purpose is to turn a
bare Spring Initializr scaffold into a repository that can be worked on properly — with a defined
branching model, enforced review, structured collaboration, community policies, and documentation
that explains itself.

Everything built from this point forward inherits these rules. There is nothing here for an end user
to install or call; the audience for this release is anyone who will contribute to the project,
including its future maintainer.

### Added

- **Spring Boot 4.1.0 application scaffold** targeting **Java 25 (LTS)**, with Spring Web MVC for building
  the REST API, Lombok to remove boilerplate, and JUnit 5 with Spring Test for automated testing.
  The **Maven Wrapper** is included so the project builds with an identical Maven version on every
  machine without anyone installing Maven first.
- **`README.md`** — the front door of the project: what it is, the full tech stack with the reason
  each piece is present, prerequisites, build and run instructions for both Windows and Unix shells,
  an annotated directory tree, a summary of the development workflow, and a roadmap.
- **`CHANGELOG.md`** — this file, created on day one rather than reconstructed later.
- **`LICENSE`** — the **MIT Licence**, giving anyone explicit permission to use, modify and
  distribute the code. Without it the code would legally be all-rights-reserved by default, meaning
  nobody could use it at all.
- **`CONTRIBUTING.md`** — the single entry point for anyone changing this codebase. Covers
  environment and IDE setup, the full life of a change from issue to merge, branch naming, commit
  rules, changelog expectations, pull request quality, review etiquette for both author and reviewer,
  merge strategies, and a definition of done.
- **`docs/BRANCHING.md`** — the Git Flow model as this project applies it: both permanent branches
  and what each guarantees, all four supporting branch types with where they are cut from and merge
  back to, a flow diagram, exact command sequences for building a feature, cutting a release and
  shipping a hotfix, merge strategy per target branch, the protection rules currently in force, and
  the rules that are never broken.
- **`docs/COMMIT_CONVENTION.md`** — the Conventional Commits specification adopted here, with one
  deliberate addition: commit bodies are expected to be substantial. Documents the message anatomy,
  every allowed type and scope, subject and body rules, how breaking changes are signalled twice,
  issue-linking footers, four fully worked examples, and how to correct a bad message safely.
- **`docs/VERSIONING.md`** — how versions are chosen and releases are produced. Defines exactly what
  counts as the public API of this service, what makes a change major, minor or patch, the special
  rules of the `0.x` phase, how commit types map to version bumps, the annotated-tag convention, a
  nine-step release checklist, the hotfix path, and the release-notes structure.
- **`.gitmessage`** — a commit template wired into the editor via `git config commit.template`, so
  the rules appear at the moment a commit is written rather than in a document nobody re-reads. It
  includes a 72-column ruler, the full type and scope reference, and a pre-commit checklist.
- **Four structured GitHub issue forms** — bug report, feature request, engineering task and
  documentation issue. These are YAML *forms* rather than Markdown templates, because forms can mark
  fields as required and therefore actually guarantee that a report arrives with reproduction steps,
  an environment and a version rather than a sentence. Each applies its own default labels.
- **Issue template configuration** that disables blank issues and routes open-ended questions to
  Discussions, vulnerability reports to GitHub Security Advisories, and setup questions to the
  documentation — so the issue tracker holds actionable work only.
- **Pull request template** prompting for a plain-language summary, the linked issue, the type of
  change, what was actually run to test it, breaking-change impact with a migration path, and a
  three-part self-review checklist covering the change, the process and quality.
- **`CODEOWNERS`** so reviewers are requested automatically, with separate ownership for build
  configuration, application source, governance documents, GitHub configuration and shared tooling.
  It documents the rule people most often get wrong — that the **last** matching rule wins, the
  opposite of `.gitignore` — and sketches how ownership splits by area as the project grows.
- **`CODE_OF_CONDUCT.md`** — the Contributor Covenant v2.1, with a four-stage enforcement ladder and
  a reporting route that stays private without publishing anyone's personal email address.
- **`SECURITY.md`** — a responsible disclosure policy. Vulnerabilities are reported through **GitHub
  Private Vulnerability Reporting**, never a public issue, because a public report tells everyone how
  to exploit the problem before a fix exists. States which versions are supported, what to include in
  a report, target response times, the coordinated disclosure process, and an explicit scope list so
  reporters know in advance what will and will not be accepted. Closes with security rules for
  contributors — most importantly that a secret committed to Git is permanently in the history and
  must be **rotated**, not merely deleted.
- **`SUPPORT.md`** — routes people to documentation, Discussions or the issue tracker depending on
  what they actually need, and explains what makes a question answerable.
- **`.editorconfig`** — a shared formatting baseline honoured automatically by IntelliJ, VS Code,
  Visual Studio, Vim and most other editors: UTF-8, LF endings, a final newline, trimmed trailing
  whitespace, four-space indentation for Java and two for XML, YAML and JSON. Markdown is deliberately
  exempt from whitespace trimming, because two trailing spaces are its hard line break and trimming
  them silently changes how a document renders.

### Changed

- **Maven project metadata completed.** Spring Initializr leaves `name`, `description`, `url`,
  `licenses`, `developers` and `scm` behind as empty placeholder elements. All are now filled in with
  real values, and `inceptionYear`, `organization` and `issueManagement` were added.

  The empty `<license/>` element mattered most: it was **worse than declaring nothing at all**,
  because licence-scanning tooling reads an empty licence as *unknown* rather than *unspecified*, and
  an unknown licence fails compliance checks. The declared MIT licence now matches the `LICENSE` file
  exactly.

  No dependency, version or plugin behaviour was altered.
- **Project version corrected from `0.0.1-SNAPSHOT` to `0.1.0-SNAPSHOT`.** The generator's default
  did not match the version this work is building towards, and `docs/VERSIONING.md` requires
  `develop` to carry the `-SNAPSHOT` of its target version.
- **`.gitattributes` replaced with a comprehensive ruleset.** The generated file was two lines. It
  now normalises all text to LF inside the repository so the same file never appears rewritten purely
  because of line endings, and pins the cases where endings are *functionally* significant: `.sh` and
  `mvnw` are forced to LF, because a shell script with CRLF fails on Linux with a `bad interpreter`
  error caused by the carriage return becoming part of the interpreter path; `.bat` and `.cmd` are
  forced to CRLF, because `cmd.exe` can misparse LF-only batch files. Binary types are marked so Git
  stops trying to diff or line-ending-convert them, the Maven Wrapper is marked as generated so it
  collapses in pull request reviews, and `CHANGELOG.md` uses a union merge so the file that every
  branch edits stops producing conflicts that never carry real disagreement.

### Fixed

- **The project would not compile at all.** Spring Initializr had set the build to target **Java 26**,
  which was simply the newest entry in the generator's dropdown rather than a deliberate choice.
  No Java 26 runtime was installed, and a compiler can only target its own version or older — never
  newer — so every build failed before it began.

  The build now targets **Java 25**, the current Long Term Support release. LTS was chosen over
  staying on 26 because a short-term release is supported for roughly six months, and a project that
  expects to reach production should not need a runtime upgrade twice a year to stay supported.
  Verified against **Eclipse Temurin 25.0.4+7**, a vendor-neutral build that is free for production
  use and is what most CI images ship by default.

### Security

- **`main`, `develop` and every `v*` tag are protected by GitHub rulesets.** Force pushes and branch
  deletion are blocked outright, every change must arrive through a pull request, review
  conversations must be resolved before merging, stale approvals are dismissed when new commits are
  pushed, and published release tags can never be moved or deleted. This is what makes the history
  genuinely append-only and makes a version tag mean the same thing permanently.
- **A private vulnerability disclosure route now exists.** Before this release there was no way to
  report a security problem except a public issue, which would have told everyone how to exploit it
  before a fix could ship.

---

<!-- Comparison links -->

[Unreleased]: https://github.com/RISHII7/E-commerce/compare/v0.1.0...develop
[0.1.0]: https://github.com/RISHII7/E-commerce/releases/tag/v0.1.0
