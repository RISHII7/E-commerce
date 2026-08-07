# Contributing to E-Commerce Service

Thank you for taking the time to contribute. This document is the single entry point for anyone
changing this codebase — including future me.

It exists so that nobody has to guess. Every rule here is one the repository's own history already
follows.

---

## Table of contents

- [Ground rules](#ground-rules)
- [Setting up your environment](#setting-up-your-environment)
- [The life of a change](#the-life-of-a-change)
- [Branch naming](#branch-naming)
- [Commit messages](#commit-messages)
- [Updating the changelog](#updating-the-changelog)
- [Opening a pull request](#opening-a-pull-request)
- [Review etiquette](#review-etiquette)
- [Merging](#merging)
- [Definition of done](#definition-of-done)
- [Reporting bugs and requesting features](#reporting-bugs-and-requesting-features)
- [Getting help](#getting-help)

---

## Ground rules

1. **Every change starts with an issue.** If one does not exist, open it first. The issue is where
   the *why* is agreed before effort is spent on the *how*.
2. **Nothing is pushed directly to `main` or `develop`.** Both are protected. Everything arrives
   through a pull request.
3. **Every pull request updates the changelog** if it changes behaviour.
4. **Read your own diff before asking anyone else to.**
5. Be kind. The [Code of Conduct](CODE_OF_CONDUCT.md) applies everywhere in this project.

---

## Setting up your environment

### Prerequisites

| Requirement | Version | Notes |
| :--- | :--- | :--- |
| **JDK** | 25 (LTS) | [Eclipse Temurin 25](https://adoptium.net/temurin/releases/?version=25) is what the project is verified against. |
| **Git** | 2.30 or newer | |
| **Maven** | *not required* | The bundled Maven Wrapper handles it. |

### First-time setup

```bash
# 1. Clone. You will land on develop, which is the default branch.
git clone https://github.com/RISHII7/E-commerce.git
cd E-commerce

# 2. Load the commit message template into your editor.
#    Do this once per clone; it is a local setting and cannot be committed for you.
git config commit.template .gitmessage

# 3. Verify the build before changing anything, so you know a later
#    failure is yours.
./mvnw clean verify          # Windows: .\mvnw.cmd clean verify
```

A successful run ends with `BUILD SUCCESS` and the Spring context test passing.

### IDE setup

The project targets **Java 25**. If your IDE was opened before you installed JDK 25, it will still be
bound to the old SDK and will refuse to compile.

**IntelliJ IDEA:**

1. `Ctrl+Alt+Shift+S` → **Platform Settings → SDKs** → **`+` → Add JDK…** → select your JDK 25
2. **Project Settings → Project** → SDK = 25, Language level = 25
3. **Settings → Build, Execution, Deployment → Build Tools → Maven** → **JDK for importer** = 25
   *(this one is easy to miss and silently resets the others on every reimport)*
4. **Maven → Reload project**, then **Build → Rebuild Project**

Enable **annotation processing** as well, or Lombok-generated methods will appear as errors:
**Settings → Build, Execution, Deployment → Compiler → Annotation Processors → Enable**.

---

## The life of a change

```text
  ┌──────────┐   ┌────────┐   ┌─────────┐   ┌──────────────┐   ┌────────┐   ┌─────────┐
  │  Issue   │──▶│ Branch │──▶│ Commits │──▶│ Pull request │──▶│ Review │──▶│  Merge  │
  └──────────┘   └────────┘   └─────────┘   └──────────────┘   └────────┘   └─────────┘
       │                                                                          │
       └────────────────────── closed automatically ◀─────────────────────────────┘
```

### 1. Pick up or open an issue

Check the [issue tracker](https://github.com/RISHII7/E-commerce/issues) first. Anything labelled
`status: ready` is fully specified and free to start.

If you are opening a new issue, use one of the [issue forms](#reporting-bugs-and-requesting-features).
Assign it to yourself and move it to `status: in progress` so nobody duplicates your work.

### 2. Cut a branch from `develop`

```bash
git checkout develop
git pull origin develop
git checkout -b feature/12-product-search-endpoint
```

Always branch from an **up-to-date** `develop`. Branching from a stale copy creates merge conflicts
that were entirely avoidable.

> Working on an urgent production defect instead? That is a **hotfix**, and it branches from `main`.
> See [BRANCHING.md](docs/BRANCHING.md#scenario-emergency-hotfix).

### 3. Do the work

- Keep the branch focused on **one** issue. If you discover something unrelated, open a new issue
  rather than widening this branch.
- Write tests for behaviour you add or fix.
- Run `./mvnw clean verify` before you push. A broken push wastes reviewer time.

### 4. Commit

```bash
git add .
git commit          # no -m; let your editor open with the template
```

### 5. Push and open a pull request

```bash
git push -u origin feature/12-product-search-endpoint
gh pr create --base develop
```

---

## Branch naming

```text
<type>/<issue-number>-<short-kebab-case-description>
```

| Type | Cut from | For |
| :--- | :--- | :--- |
| `feature/` | `develop` | A new capability |
| `bugfix/` | `develop` | A defect that has not reached production |
| `release/` | `develop` | Stabilising a version — named `release/0.2.0` |
| `hotfix/` | `main` | An urgent defect in a released version |

**Rules:** lower case, hyphen-separated, include the issue number, under ~50 characters, and describe
the outcome rather than the activity.

```text
✅ feature/12-product-search-endpoint
✅ bugfix/56-cart-total-rounding
❌ my-branch                          — says nothing
❌ Feature/Product_Search             — mixed case and underscores
❌ feature/12                         — unreadable in a branch list
```

> [!NOTE]
> **Branches are never deleted in this repository**, including after merge. The full shape of past
> work stays inspectable.

Full detail: [docs/BRANCHING.md](docs/BRANCHING.md).

---

## Commit messages

This project follows [Conventional Commits](https://www.conventionalcommits.org/), with one
deliberate addition: **bodies are expected to be substantial**.

```text
<type>(<scope>): <subject>

<body — wrapped at 72 columns, explaining WHY>

<footers — Closes #12>
```

The diff already records *what* changed. Only the message can record *why*, what else was tried, and
what would break if someone "simplified" it later.

```text
feat(catalog): let customers filter products by price range

Customers browsing a large catalogue currently have to page through
every product to find one within their budget.

Adds optional minPrice and maxPrice query parameters. Both are optional
and validated independently, so passing only one still narrows results.

An invalid range returns 400 rather than an empty list, because an empty
list would be indistinguishable from a genuine no-results case and would
look like a data problem to the caller.

Closes #42
```

**Quick reference:**

| Type | For | Type | For |
| :--- | :--- | :--- | :--- |
| `feat` | New capability | `test` | Tests only |
| `fix` | Corrected behaviour | `build` | Build, Maven, dependencies |
| `docs` | Documentation only | `ci` | Pipelines |
| `refactor` | No behaviour change | `chore` | Housekeeping |
| `perf` | Measurably faster | `revert` | Undoing a commit |

Full specification: [docs/COMMIT_CONVENTION.md](docs/COMMIT_CONVENTION.md).

---

## Updating the changelog

**If your change affects behaviour, update `CHANGELOG.md` in the same pull request.**

Add an entry under the matching heading in `[Unreleased]`:

```markdown
## [Unreleased]

### Added

- **Price range filtering on the product listing endpoint.** Customers can now pass `minPrice` and
  `maxPrice` to narrow results. Both are optional.
```

Write for **users**, not for developers. A changelog entry answers *"does this affect me, and what do
I do about it?"* — not *"which classes changed?"*

A changelog assembled at release time is always written by whoever remembers least about what
happened, and is always missing something. Writing it while the change is fresh costs a minute.

Documentation-only, test-only and pure refactor changes do not need an entry.

---

## Opening a pull request

Target **`develop`** unless you are cutting a release or shipping a hotfix, which target `main`.

The [pull request template](.github/PULL_REQUEST_TEMPLATE.md) loads automatically. Fill it in
properly:

| Section | What reviewers need |
| :--- | :--- |
| **Summary** | What changed and **why**, in plain language. |
| **Linked issue** | `Closes #12`, so the issue closes on merge. |
| **Type of change** | Tick the boxes that apply. |
| **How this was tested** | Not "it works" — what you actually ran. |
| **Breaking change** | If yes, the migration path. |
| **Self-review checklist** | Genuinely tick these, do not tick them blindly. |

### Keep pull requests small

A reviewer reading 80 changed lines finds real problems. A reviewer facing 1,500 changed lines
approves it. If your branch is growing past a comfortable single sitting, split it across several
issues.

---

## Review etiquette

### As the author

- **Review your own diff first.** Read every line on the Files Changed tab before requesting review.
  You will catch leftover debug output, commented code and accidental files.
- Respond to every comment, even if only to say you disagree and why.
- Push fixes as **new commits** during review rather than force-pushing an amended history. Amending
  detaches review comments from the code they refer to.

### As the reviewer

- Distinguish **blocking** concerns from suggestions. Prefix optional ones with `nit:`.
- Explain *why*, not just *what*. "Use a `BigDecimal` here — `double` cannot represent 19.99 exactly
  and the totals will drift" teaches something; "use `BigDecimal`" does not.
- Approve when it is good enough to merge, not when it is what you would have written.
- Say when something is done well. Review is not only fault-finding.

### Working solo

Being the only person on the project does not remove the review step. Open the pull request, walk the
diff on the Files Changed tab, and write down what you checked. Reading your own work in a different
context is genuinely effective, and it leaves a record of what was verified.

---

## Merging

| Merging into | Strategy |
| :--- | :--- |
| `develop` from `feature/*` or `bugfix/*` | **Merge commit** (`--no-ff`) — keeps the branch visible as a unit of work |
| `develop` from a trivial one-commit branch | **Squash** is acceptable |
| `main` from `release/*` or `hotfix/*` | **Merge commit only** — a release must be one identifiable, taggable point |
| Anything | ~~Rebase merge~~ — **disabled on this repository** |

Before merging, confirm all review conversations are resolved and the branch is up to date with its
target.

---

## Definition of done

A change is done when **all** of these are true:

- [ ] It does what the issue asked, and nothing unrelated
- [ ] `./mvnw clean verify` passes locally
- [ ] New or changed behaviour is covered by tests
- [ ] Public types and non-obvious logic are documented
- [ ] `CHANGELOG.md` is updated if behaviour changed
- [ ] Commit messages follow the convention and explain *why*
- [ ] The pull request template is filled in honestly
- [ ] Every review conversation is resolved
- [ ] The linked issue closes automatically on merge

---

## Reporting bugs and requesting features

Use the issue forms — they exist so reports arrive with the information needed to act on them.

| Template | Use it for |
| :--- | :--- |
| 🐛 **Bug report** | Something is broken. Include reproduction steps, expected versus actual behaviour, and your environment. |
| ✨ **Feature request** | A new capability. Describe **the problem first**, then your proposed solution. |
| 🔧 **Task** | Internal engineering work that is neither a bug nor a user-facing feature. |
| 📚 **Documentation** | Documentation that is wrong, missing or unclear. |

Search existing issues before opening a new one.

> [!IMPORTANT]
> **Never report a security vulnerability in a public issue.** Follow [SECURITY.md](SECURITY.md),
> which uses GitHub Private Vulnerability Reporting.

---

## Getting help

| I want to… | Go to |
| :--- | :--- |
| Understand the project | [README.md](README.md) |
| Know which branch to cut | [docs/BRANCHING.md](docs/BRANCHING.md) |
| Write a commit message correctly | [docs/COMMIT_CONVENTION.md](docs/COMMIT_CONVENTION.md) |
| Know what version a change deserves | [docs/VERSIONING.md](docs/VERSIONING.md) |
| Ask an open-ended question | [Discussions](https://github.com/RISHII7/E-commerce/discussions) |
| Report a defect | [Issues](https://github.com/RISHII7/E-commerce/issues) |
| Report a vulnerability | [SECURITY.md](SECURITY.md) |
| Find any other kind of help | [SUPPORT.md](SUPPORT.md) |

---

Thank you for contributing. 🚀