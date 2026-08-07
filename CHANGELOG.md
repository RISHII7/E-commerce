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

---

<!-- Comparison links -->

[Unreleased]: https://github.com/RISHII7/E-commerce/commits/develop
