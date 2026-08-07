# Branching Model

> The complete Git Flow model as applied in this repository.
> Companion documents: [Commit Convention](COMMIT_CONVENTION.md) · [Versioning](VERSIONING.md) · [Contributing](../CONTRIBUTING.md)

---

## Table of contents

- [Why Git Flow](#why-git-flow)
- [The two permanent branches](#the-two-permanent-branches)
- [The four supporting branches](#the-four-supporting-branches)
- [How work flows](#how-work-flows)
- [Branch naming rules](#branch-naming-rules)
- [Scenario: building a feature](#scenario-building-a-feature)
- [Scenario: fixing a bug before release](#scenario-fixing-a-bug-before-release)
- [Scenario: cutting a release](#scenario-cutting-a-release)
- [Scenario: emergency hotfix](#scenario-emergency-hotfix)
- [Keeping a branch up to date](#keeping-a-branch-up-to-date)
- [Merge strategies](#merge-strategies)
- [Branch protection in force](#branch-protection-in-force)
- [Rules that are never broken](#rules-that-are-never-broken)

---

## Why Git Flow

Git Flow separates **what is being built** from **what has been shipped**.

The alternative — everyone merging into a single branch — works well for services that deploy
continuously and never need to support an older version. This project expects to cut discrete,
versioned releases and to be able to patch a released version without shipping half-finished work
alongside the patch. That requirement is exactly what Git Flow exists to serve.

The cost is honest: more branches, more merges, and a longer path from writing code to seeing it in a
release. In exchange, `main` is always a truthful record of what has been released, and any commit on
it can be checked out and run with confidence.

---

## The two permanent branches

These two branches exist for the lifetime of the project and are never deleted.

### `main`

**What it guarantees:** every commit on `main` is a released version.

- Only ever receives merges from `release/*` and `hotfix/*` branches.
- Every merge into `main` is immediately followed by an annotated `vX.Y.Z` tag.
- Nobody commits to `main` directly, ever.
- Checking out any commit on `main` gives you exactly what was published at that point in time.

### `develop`

**What it guarantees:** the integration point for the next release.

- This is the **default branch** on GitHub, because it is where day-to-day work lands. Cloning the
  repository puts you on `develop`.
- Receives merges from `feature/*` and `bugfix/*` branches, and back-merges from `release/*` and
  `hotfix/*`.
- Should always build and pass tests. It may contain features that are incomplete from a product
  point of view, but it should never be broken.
- Nobody commits to `develop` directly either. Everything arrives through a pull request.

---

## The four supporting branches

These are temporary. Each has exactly one job, one place it is cut from, and one or two places it
merges back to.

| Branch | Cut from | Merges into | Naming | Typical lifetime |
| :--- | :--- | :--- | :--- | :--- |
| `feature/*` | `develop` | `develop` | `feature/<issue>-<short-description>` | Hours to a few days |
| `bugfix/*` | `develop` | `develop` | `bugfix/<issue>-<short-description>` | Hours to a day |
| `release/*` | `develop` | `main` **and** `develop` | `release/<x.y.z>` | Hours to a day |
| `hotfix/*` | `main` | `main` **and** `develop` | `hotfix/<x.y.z>-<short-description>` | Minutes to hours |

> [!IMPORTANT]
> `release/*` and `hotfix/*` merge into **two** branches, not one. Forgetting the back-merge into
> `develop` is the single most common Git Flow mistake, and it silently loses the fix from all future
> releases. See [the release scenario](#scenario-cutting-a-release).

### `feature/*` — new capability

Cut from `develop` when starting work on an issue that adds something new. Merged back into `develop`
through a reviewed pull request. Should stay small enough to review in one sitting; if a feature is
growing past that, split it into several issues and several branches.

### `bugfix/*` — defect found before release

Identical in mechanics to `feature/*`, but named differently so the history makes it obvious at a
glance whether a branch added capability or corrected behaviour. Use this for defects found in
`develop` that have **not** yet reached production. Defects that *have* reached production are
hotfixes, not bugfixes.

### `release/*` — stabilise and version

Cut from `develop` when the content planned for a release is complete. From this point, `develop` is
free to accept the *next* release's work while the release branch is stabilised independently.

Only these changes belong on a release branch:

- Version number bumps.
- Finalising the `CHANGELOG.md` section for this version.
- Bug fixes found during release testing.

New features **never** go onto a release branch. If something is missing, it waits for the next
release.

### `hotfix/*` — urgent production defect

The only branch cut from `main`. Used when something is broken in a released version and cannot wait
for the normal cycle. Merged into `main`, tagged with a new patch version, and then back-merged into
`develop` so the fix is not lost.

---

## How work flows

```text
                                                      tag v0.1.0        tag v0.1.1        tag v0.2.0
                                                          │                 │                 │
main        ●─────────────────────────────────────────────●─────────────────●─────────────────●
             ╲                                           ╱ ╲               ╱                 ╱
              ╲                                         ╱   ╲             ╱                 ╱
               ╲                             release/0.1.0   ╲   hotfix/0.1.1              ╱
                ╲                                   ╱         ╲       ╱                   ╱
                 ╲                                 ╱           ╲     ╱          release/0.2.0
                  ╲                               ╱             ╲   ╱                 ╱
develop            ●───●───●───●───●───●───●───●─●───●───●───●───●─●───●───●───●───●──●
                        ╲ ╱     ╲ ╱     ╲ ╱             ╲ ╱     ╲ ╱     ╲ ╱
                         ●       ●       ●               ●       ●       ●
                    feature/1  feature/2  bugfix/3   feature/4  feature/5  feature/6
```

Read it as three lanes:

1. **Feature work** branches off `develop`, does its job, and merges back into `develop`.
2. **Releases** branch off `develop`, land on `main`, get tagged, and are back-merged into `develop`.
3. **Hotfixes** branch off `main`, land back on `main`, get tagged, and are back-merged into `develop`.

---

## Branch naming rules

```text
<type>/<issue-number>-<short-kebab-case-description>
```

| Rule | Reason |
| :--- | :--- |
| Always lower case | Git on Windows and macOS is case-insensitive by default; mixed case causes collisions. |
| Words separated by hyphens | Underscores and spaces read poorly in URLs and terminal output. |
| Include the issue number | Makes the branch, the issue and the pull request findable from any one of them. |
| Keep it under about 50 characters | Long branch names get truncated in GitHub's UI and in `git branch` output. |
| Describe the *outcome*, not the activity | `feature/12-product-search` beats `feature/12-working-on-search`. |

**Good:**

```text
feature/12-product-search-endpoint
feature/34-order-cancellation
bugfix/56-cart-total-rounding
release/0.2.0
hotfix/0.1.1-null-price-crash
```

**Avoid:**

```text
my-branch                  ← says nothing
Feature/Product-Search     ← mixed case
feature/product_search     ← underscores
fix                        ← no type prefix, no issue, no description
feature/12                 ← issue number alone; unreadable in a branch list
```

---

## Scenario: building a feature

```bash
# 1. Start from an up-to-date develop
git checkout develop
git pull origin develop

# 2. Cut the branch, named after the issue you are working on
git checkout -b feature/12-product-search-endpoint

# 3. Do the work, committing as you go (see COMMIT_CONVENTION.md)
git add .
git commit          # opens your editor with the .gitmessage template

# 4. Push and set upstream
git push -u origin feature/12-product-search-endpoint

# 5. Open the pull request against develop
gh pr create --base develop --fill

# 6. After review, merge with a merge commit so the branch stays visible
gh pr merge --merge
```

> [!NOTE]
> The branch is **not** deleted after merging. This repository keeps every branch permanently, so the
> full shape of the work stays inspectable.

---

## Scenario: fixing a bug before release

Identical to a feature, with a `bugfix/` prefix and a `fix:` commit type:

```bash
git checkout develop && git pull origin develop
git checkout -b bugfix/56-cart-total-rounding
# ... fix, commit, push ...
gh pr create --base develop --fill
```

---

## Scenario: cutting a release

This is the sequence people get wrong. Follow it exactly.

```bash
# 1. Cut the release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/0.2.0

# 2. Bump the version and finalise the changelog on this branch
#    - pom.xml: 0.2.0-SNAPSHOT  ->  0.2.0
#    - CHANGELOG.md: rename [Unreleased] to [0.2.0] with today's date,
#      then open a fresh empty [Unreleased] above it
git commit -am "chore(release): prepare the 0.2.0 release"
git push -u origin release/0.2.0

# 3. Pull request into main, review, merge with a merge commit
gh pr create --base main --title "release: 0.2.0"
gh pr merge --merge

# 4. Tag the merge commit on main — annotated, never lightweight
git checkout main
git pull origin main
git tag -a v0.2.0 -m "Release 0.2.0 — <one line describing the release>"
git push origin v0.2.0

# 5. Publish the GitHub Release from the tag
gh release create v0.2.0 --title "v0.2.0 — <name>" --notes-file <notes>

# 6. BACK-MERGE into develop. Do not skip this step.
git checkout develop
git pull origin develop
git merge --no-ff main -m "chore: back-merge the 0.2.0 release into develop"

# 7. Open the next development iteration
#    pom.xml: 0.2.0 -> 0.3.0-SNAPSHOT
git commit -am "chore: open the 0.3.0 development iteration"
git push origin develop
```

**Why step 6 matters.** The version bump and any fixes made during release stabilisation exist only
on `main` at that point. Without the back-merge they are absent from `develop`, which means the next
release quietly reintroduces the bugs you just fixed.

---

## Scenario: emergency hotfix

Something is broken in production. `develop` contains half-finished work and cannot be released.

```bash
# 1. Branch from the released code on main, NOT from develop
git checkout main
git pull origin main
git checkout -b hotfix/0.1.1-null-price-crash

# 2. Fix the defect and bump the patch version
#    pom.xml: 0.1.0 -> 0.1.1
#    CHANGELOG.md: add a [0.1.1] section describing the fix
git commit -am "fix(catalog): stop the listing endpoint crashing on products with no price"

# 3. Pull request into main, review, merge
git push -u origin hotfix/0.1.1-null-price-crash
gh pr create --base main --title "hotfix: 0.1.1 — null price crash"
gh pr merge --merge

# 4. Tag the patch release
git checkout main && git pull origin main
git tag -a v0.1.1 -m "Release 0.1.1 — fix crash on products with no price"
git push origin v0.1.1
gh release create v0.1.1 --title "v0.1.1 — Null price crash fix" --notes-file <notes>

# 5. Back-merge into develop so the fix survives into the next release
git checkout develop && git pull origin develop
git merge --no-ff main -m "chore: back-merge the 0.1.1 hotfix into develop"
git push origin develop
```

> [!WARNING]
> If a `release/*` branch is open at the time of a hotfix, back-merge into that release branch as
> well, otherwise the release you are about to ship will regress the fix.

---

## Keeping a branch up to date

While your branch is open, `develop` moves. Two ways to catch up:

### Merge `develop` into your branch — the default here

```bash
git checkout feature/12-product-search-endpoint
git fetch origin
git merge origin/develop
```

Safe, preserves history exactly as it happened, and never rewrites commits that others may have
pulled. This is the recommended approach for any branch that has been pushed.

### Rebase onto `develop` — only for unpushed work

```bash
git fetch origin
git rebase origin/develop
```

Produces a cleaner, linear branch history, but **rewrites commits**. Only rebase a branch that nobody
else has pulled. Never rebase `main` or `develop`, and never rebase a branch that has an open pull
request others have reviewed — it invalidates every review comment anchored to a commit.

---

## Merge strategies

| Merging into | Strategy | Why |
| :--- | :--- | :--- |
| `develop` from `feature/*` or `bugfix/*` | **Merge commit** (`--no-ff`) | Keeps the branch visible as a unit of work. The merge commit records that a set of commits belonged together. |
| `develop` from a trivial one-commit branch | **Squash** (allowed, not required) | Acceptable when the branch is a single small change and its individual commits carry no value. |
| `main` from `release/*` or `hotfix/*` | **Merge commit only** | A release must be a single identifiable point on `main` that can be tagged. |
| Anything | ~~Rebase merge~~ | **Disabled on this repository.** It discards the branch structure that the whole model depends on. |

---

## Branch protection in force

These are configured as GitHub **rulesets** and are active right now.

### `main` — *Protect main (production line)*

| Rule | Effect |
| :--- | :--- |
| Pull request required | No direct pushes. Every change is reviewed. |
| Review threads must be resolved | An open review conversation blocks the merge. |
| Stale reviews dismissed on push | New commits invalidate prior approvals. |
| Force push blocked | History on `main` can never be rewritten. |
| Deletion blocked | `main` cannot be deleted. |
| Merge method | Merge commit only. |

### `develop` — *Protect develop (integration line)*

Same as `main`, except merge commits **and** squash merges are both permitted.

### `v*` tags — *Protect release tags (immutable history)*

| Rule | Effect |
| :--- | :--- |
| Deletion blocked | A published release tag can never be removed. |
| Update blocked | A tag can never be moved to point at a different commit. |
| Force update blocked | No rewriting tag history. |

This is what makes `v0.1.0` mean the same thing forever. A moved tag silently changes what everyone
believes a version contains.

> [!NOTE]
> Repository administrators can bypass these rules. That exists so a solo maintainer is not locked out
> of their own repository — not as a routine path. Bypassing is a deliberate, visible act and should
> be treated as one.

---

## Rules that are never broken

1. **Never commit directly to `main` or `develop`.** Everything arrives through a pull request.
2. **Never force push to a shared branch.** If you need to undo something on `main` or `develop`, use
   `git revert`, which adds a new commit rather than rewriting history others already have.
3. **Never move or delete a published tag.** Cut a new patch version instead.
4. **Never put a new feature on a release branch.** It waits for the next release.
5. **Never skip the back-merge** after a release or hotfix.
6. **Never merge your own pull request without reading the diff first.** Being the only reviewer is
   not an excuse to skip the review.