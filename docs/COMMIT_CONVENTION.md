# Commit Convention

> How every commit message in this repository must be written.
> Companion documents: [Branching](BRANCHING.md) · [Versioning](VERSIONING.md) · [Contributing](../CONTRIBUTING.md)

---

## Table of contents

- [Why this matters](#why-this-matters)
- [The format](#the-format)
- [Allowed types](#allowed-types)
- [Allowed scopes](#allowed-scopes)
- [Writing the subject line](#writing-the-subject-line)
- [Writing the body](#writing-the-body)
- [Footers](#footers)
- [Breaking changes](#breaking-changes)
- [Worked examples](#worked-examples)
- [Using the commit template](#using-the-commit-template)
- [Fixing a bad commit message](#fixing-a-bad-commit-message)

---

## Why this matters

A commit message is the only place where the **reasoning** behind a change can survive. The diff
records what changed; it can never record what else was tried, what constraint forced the design, or
what would break if someone "simplified" it later.

Six months from now, when something behaves strangely, the fastest route to an answer is
`git log` followed by `git blame`. That route only works if the messages say something.

This project follows [**Conventional Commits 1.0.0**](https://www.conventionalcommits.org/en/v1.0.0/),
with one deliberate addition: **bodies are expected to be substantial.** A one-line commit is
acceptable only for genuinely trivial changes such as a typo fix.

---

## The format

```text
<type>(<scope>): <subject>
<BLANK LINE>
<body>
<BLANK LINE>
<footers>
```

| Part | Required | Rules |
| :--- | :---: | :--- |
| **type** | ✅ | One of the [allowed types](#allowed-types). Lower case. |
| **scope** | ⬜ | One of the [allowed scopes](#allowed-scopes), in parentheses. Omit if the change is project-wide. |
| **subject** | ✅ | Imperative mood, lower case, no full stop, **≤ 72 characters** including the type and scope. |
| **body** | ⚠️ | Expected for anything non-trivial. Wrapped at **72 columns**. Explains *why*. |
| **footers** | ⬜ | Issue references and breaking-change notices. |

---

## Allowed types

| Type | Use it when | Release impact |
| :--- | :--- | :--- |
| `feat` | Adding a capability that did not exist before. | **MINOR** |
| `fix` | Correcting behaviour that was wrong. | **PATCH** |
| `docs` | Documentation only — README, guides, Javadoc, comments. | none |
| `style` | Formatting, whitespace, import order. **No behaviour change whatsoever.** | none |
| `refactor` | Restructuring code without changing what it does or fixing a bug. | none |
| `perf` | Making something measurably faster or lighter. | **PATCH** |
| `test` | Adding or correcting tests. No production code change. | none |
| `build` | Build system, Maven configuration, dependency versions, packaging. | none |
| `ci` | Continuous integration and delivery pipeline configuration. | none |
| `chore` | Routine maintenance with no effect on source or tests. | none |
| `revert` | Undoing a previous commit. Body must state which one and why. | varies |

> [!IMPORTANT]
> `refactor` means the observable behaviour is **identical**. If behaviour changed even slightly, it
> is a `feat` or a `fix`, not a refactor. Getting this wrong makes the changelog lie.

---

## Allowed scopes

The scope names the area of the system the change touches. It is optional, but it makes
`git log --oneline` far more scannable.

| Scope | Covers |
| :--- | :--- |
| `catalog` | Products, categories, search, browsing |
| `inventory` | Stock levels, availability, reservations |
| `cart` | Shopping cart behaviour |
| `order` | Checkout, order lifecycle, fulfilment |
| `payment` | Payment processing and refunds |
| `account` | Customer accounts, profiles, addresses |
| `auth` | Authentication, authorisation, roles, tokens |
| `api` | Cross-cutting REST concerns — error format, versioning, pagination |
| `persistence` | Repositories, database migrations, query performance |
| `config` | Application configuration and profiles |
| `deps` | Dependency additions, removals and upgrades |
| `docs` | Documentation structure itself |
| `release` | Release preparation commits |

Omit the scope when the change is genuinely project-wide.

---

## Writing the subject line

**Imperative mood.** Write it as an instruction to the codebase — the way Git itself does
("Merge branch", "Revert commit"). A useful test: the subject should complete the sentence
*"If applied, this commit will…"*.

| ✅ Good | ❌ Avoid | Why |
| :--- | :--- | :--- |
| `add price range filtering to product search` | `added price range filtering` | Past tense |
| `stop the cart total rounding down` | `fixes rounding bug` | Third person |
| `move order validation into the service layer` | `refactoring` | Says nothing |
| `reject orders with no line items` | `Update OrderService.java` | Names the file, not the change |

**Other rules:**

- Lower case first letter — the type prefix already opens the line.
- **No full stop** at the end. It is a title, not a sentence.
- Keep the whole line to **72 characters**, so `git log --oneline` never truncates it.
- Describe the **outcome**, not the activity.

---

## Writing the body

This is where this project differs from a default Conventional Commits setup. **The body is the
point.**

Separate it from the subject with one blank line, and wrap it at **72 columns** so it reads correctly
in every terminal and in `git log` output, which indents by four spaces.

A good body answers, in roughly this order:

1. **What was the situation before?** What was broken, missing or awkward.
2. **Why does it need to change?** The user impact or engineering cost of leaving it alone.
3. **What approach was taken, and why that one?** Including alternatives considered and rejected.
4. **What are the consequences?** Anything a future reader would be surprised by.

Explain **why**, not **what**. The diff already shows what changed. It can never show why.

> [!TIP]
> If you cannot think of anything to write in the body, ask whether the subject line really is
> self-evident. Sometimes it is — a typo fix needs no essay. Usually it is not.

---

## Footers

Footers go last, after a blank line.

### Linking to issues

| Footer | Effect |
| :--- | :--- |
| `Closes #12` | Closes issue 12 automatically when merged into the default branch. |
| `Fixes #12` | Same, conventionally used for bugs. |
| `Refs #12` | References the issue **without** closing it. |

Use a closing keyword only on the commit that genuinely completes the issue.

### Other footers

```text
Co-authored-by: Name <email@example.com>
Reviewed-by: Name <email@example.com>
BREAKING CHANGE: <description>
```

---

## Breaking changes

A breaking change is anything that forces someone consuming this project to change what they do.

Signal it **twice**:

1. A `!` immediately before the colon in the subject line.
2. A `BREAKING CHANGE:` footer explaining the impact and the migration path.

```text
feat(api)!: return validation errors as a problem detail document

Validation failures previously returned an ad-hoc JSON object with a
single message field. That gave clients no way to tell which field was
invalid without parsing English text, so every consumer had written its
own fragile string matching.

Responses now follow RFC 9457 Problem Details, with a machine-readable
errors array naming each rejected field and the rule it violated.

BREAKING CHANGE: The error response body has changed shape. Clients
reading `error.message` must now read `detail`, and per-field errors
move from the flat message string into the `errors` array. The HTTP
status codes are unchanged.

Closes #88
```

Breaking changes force a **MAJOR** version bump — except during `0.x`, where they bump MINOR. See
[VERSIONING.md](VERSIONING.md).

---

## Worked examples

### A feature

```text
feat(catalog): let customers filter products by price range

Customers browsing a large catalogue currently have to page through
every product to find one within their budget. Support asked for this
after it came up repeatedly in feedback.

Adds optional minPrice and maxPrice query parameters to the product
listing endpoint. Both are optional and validated independently, so
passing only one still narrows the result set.

An invalid range, where the minimum exceeds the maximum, returns 400
rather than an empty list. An empty list would be indistinguishable
from a genuine no-results case and would look like a data problem to
the caller.

Filtering is applied in the database query rather than in memory, so
the cost stays flat as the catalogue grows.

Closes #42
```

### A bug fix

```text
fix(cart): stop the cart total rounding down on every line

The cart total was calculated by summing line totals as doubles and
rounding once at the end. Because doubles cannot represent most decimal
fractions exactly, a cart of thirty items priced at 19.99 came out one
paisa short. Small, but it is money, and it did not reconcile against
the payment provider.

All monetary arithmetic now uses BigDecimal with an explicit scale of
two and HALF_UP rounding, matching the rounding rule the payment
provider applies on their side.

Added a regression test with the exact basket from the customer report
that surfaced this.

Fixes #67
```

### A dependency upgrade

```text
build(deps): upgrade Spring Boot from 4.1.0 to 4.1.1

Patch release containing a fix for a request mapping issue that could
route requests with a trailing slash to the wrong handler. We do not
currently expose any endpoint pairs where this would matter, so this is
preventative rather than urgent.

No API, configuration or behavioural changes on our side. The full
build and test suite passes unchanged.

Refs #91
```

### A trivial change — short body is fine

```text
docs: fix the broken link to the branching guide in the README

The link pointed at docs/BRANCHING.MD with the extension in upper case,
which resolves on Windows but 404s on GitHub.
```

---

## Using the commit template

The repository ships a [`.gitmessage`](../.gitmessage) template that puts these rules in your editor
at the moment you write a commit. Enable it once, per clone:

```bash
git config commit.template .gitmessage
```

Then use `git commit` **without** `-m` so your editor opens with the template loaded:

```bash
git commit
```

The template's guidance lines all begin with `#` and are stripped automatically.

---

## Fixing a bad commit message

### The commit is not pushed yet

```bash
git commit --amend            # rewrite the most recent message
```

For an older commit on your branch, use an interactive rebase and mark it `reword`:

```bash
git rebase -i HEAD~3
```

### The commit is already pushed

If the branch is **yours alone** and has no reviewed pull request, amend or rebase and force-push
**with a lease**, which refuses to overwrite work you have not seen:

```bash
git push --force-with-lease
```

If the branch is shared, or has an open pull request others have reviewed, **leave it alone**. A
rewrite invalidates every review comment anchored to those commits. Write a better message on the
next commit and move on.

> [!CAUTION]
> Never rewrite history on `main` or `develop`. Both are protected against force pushes, so the
> attempt will be rejected — but do not try.