# Versioning and Releases

> How version numbers are decided, and exactly how a release is produced.
> Companion documents: [Branching](BRANCHING.md) · [Commit Convention](COMMIT_CONVENTION.md) · [Contributing](../CONTRIBUTING.md)

---

## Table of contents

- [Semantic Versioning](#semantic-versioning)
- [What counts as major, minor and patch here](#what-counts-as-major-minor-and-patch-here)
- [The 0.x phase](#the-0x-phase)
- [Commit types map to version bumps](#commit-types-map-to-version-bumps)
- [Version numbers in the build](#version-numbers-in-the-build)
- [Tagging](#tagging)
- [The release checklist](#the-release-checklist)
- [The hotfix path](#the-hotfix-path)
- [GitHub Releases](#github-releases)
- [Release cadence](#release-cadence)

---

## Semantic Versioning

This project follows [**Semantic Versioning 2.0.0**](https://semver.org/spec/v2.0.0.html).

```text
MAJOR . MINOR . PATCH
  │       │       │
  │       │       └── Backwards-compatible bug fixes
  │       └────────── Backwards-compatible new capability
  └────────────────── Incompatible changes that break consumers
```

A version number is a **promise to the people using the software**. Reading `1.4.2 → 1.5.0` should be
enough to know that upgrading is safe and something new is available, without reading a single line
of the diff. That promise is only worth anything if it is kept every time.

---

## What counts as major, minor and patch here

Semantic versioning only means something once "the public API" is defined. For this service, the
public API is **everything a consumer can observe**:

- HTTP endpoints: paths, methods, query parameters, headers.
- Request and response body shapes, including field names, types and nullability.
- HTTP status codes returned for a given situation.
- Error response format.
- Configuration property names and their accepted values.
- Documented, guaranteed behaviour — ordering, pagination rules, idempotency.

Internal class structure, package layout, private method signatures and database schema are **not**
part of the public API. They can change freely in any release.

### MAJOR — breaks consumers

| Example |
| :--- |
| Removing or renaming an endpoint |
| Removing or renaming a field in a response |
| Making a previously optional request field required |
| Narrowing an accepted value range |
| Changing the HTTP status returned for an existing situation |
| Changing the error response format |
| Removing or renaming a configuration property |

### MINOR — adds capability, breaks nothing

| Example |
| :--- |
| A new endpoint |
| A new **optional** request parameter |
| A new field in a response body (additive) |
| A new configuration property with a sensible default |
| Deprecating something, while it still works |

### PATCH — fixes behaviour, changes nothing intentional

| Example |
| :--- |
| Correcting a calculation that produced wrong results |
| Fixing a crash |
| Correcting behaviour that already contradicted the documentation |
| A performance improvement with identical observable behaviour |
| Security fixes with no API change |

> [!WARNING]
> A bug fix that changes behaviour consumers may have relied on is a judgement call. If people are
> plausibly depending on the broken behaviour, treat it as **MAJOR** and say so loudly. Being right
> about the bug is no comfort to someone whose integration broke on a patch upgrade.

---

## The 0.x phase

The project is currently in `0.x`. Under semantic versioning, this signals that the public API is
**not yet stable**.

While the major version is `0`:

| Change | Bump |
| :--- | :--- |
| Breaking change | **MINOR** — `0.4.2 → 0.5.0` |
| New capability | **MINOR** — `0.4.2 → 0.5.0` |
| Bug fix | **PATCH** — `0.4.2 → 0.4.3` |

Breaking changes are still recorded loudly in the changelog and release notes. `0.x` means the API is
allowed to move; it does not mean people should be surprised when it does.

**`1.0.0` is reached when the API is considered stable enough to promise compatibility.** That is a
deliberate decision, not something that happens by accident because the version counter got high.

---

## Commit types map to version bumps

The [commit convention](COMMIT_CONVENTION.md) makes the next version largely mechanical:

| Commits since the last release | Next version |
| :--- | :--- |
| Any commit with `!` or a `BREAKING CHANGE:` footer | **MAJOR** (MINOR during `0.x`) |
| Any `feat:` | **MINOR** |
| Only `fix:` and/or `perf:` | **PATCH** |
| Only `docs:`, `style:`, `refactor:`, `test:`, `build:`, `ci:`, `chore:` | **No release needed** |

This is a strong default, not a substitute for judgement. The final call belongs to whoever cuts the
release.

---

## Version numbers in the build

The version lives in exactly one place: the `<version>` element in `pom.xml`.

| Branch | Version form | Example |
| :--- | :--- | :--- |
| `develop` | `X.Y.Z-SNAPSHOT` — the version being worked *towards* | `0.2.0-SNAPSHOT` |
| `release/*` | `X.Y.Z` — the exact version being shipped | `0.2.0` |
| `main` | `X.Y.Z` — matches the tag on that commit | `0.2.0` |
| `hotfix/*` | `X.Y.Z` — the patch being shipped | `0.1.1` |

The `-SNAPSHOT` suffix is Maven's marker for "in development, contents may change". A snapshot is
**never** released or tagged.

---

## Tagging

```text
v<MAJOR>.<MINOR>.<PATCH>
```

Examples: `v0.1.0`, `v0.2.0`, `v1.0.0`, `v1.0.1`

### Rules

| Rule | Reason |
| :--- | :--- |
| Always prefixed with `v` | Distinguishes tags from branches at a glance and matches the `v*` protection rule. |
| **Annotated**, never lightweight | An annotated tag is a real object with an author, a date and a message. A lightweight tag is just a pointer with no record of who made it or when. |
| Only ever on `main` | A tag must point at released code. |
| **Never moved. Never deleted.** | Enforced by a GitHub ruleset. |

```bash
# Correct — annotated
git tag -a v0.2.0 -m "Release 0.2.0 - product catalogue"

# Wrong — lightweight, carries no metadata
git tag v0.2.0
```

> [!CAUTION]
> Moving a published tag silently changes what everyone believes a version contains. Anyone who
> already pulled it keeps the old commit; anyone who pulls later gets a different one, and both
> believe they have `v0.2.0`. If a release is wrong, ship `v0.2.1`. Never rewrite `v0.2.0`.

---

## The release checklist

### 1. Confirm `develop` is ready

- [ ] Every issue in the milestone is closed
- [ ] `./mvnw clean verify` passes on a clean checkout
- [ ] `[Unreleased]` in `CHANGELOG.md` accurately describes everything merged
- [ ] No internal documentation links are broken

### 2. Cut the release branch

```bash
git checkout develop
git pull origin develop
git checkout -b release/0.2.0
```

From here, `develop` is free to accept the *next* release's work.

### 3. Set the version and finalise the changelog

- `pom.xml`: `0.2.0-SNAPSHOT` → `0.2.0`
- `CHANGELOG.md`: rename `[Unreleased]` to `[0.2.0] — YYYY-MM-DD`, open a fresh empty `[Unreleased]`
  above it, and update the comparison links at the bottom.

```bash
git commit -am "chore(release): prepare the 0.2.0 release"
git push -u origin release/0.2.0
```

### 4. Pull request into `main`

```bash
gh pr create --base main --title "release: 0.2.0"
```

Only version bumps, changelog finalisation and stabilisation bug fixes belong on this branch.
**Never a new feature.**

```bash
gh pr merge --merge      # merge commit, so the release is one identifiable point on main
```

### 5. Tag

```bash
git checkout main
git pull origin main
git tag -a v0.2.0 -m "Release 0.2.0 - product catalogue"
git push origin v0.2.0
```

### 6. Publish the GitHub Release

```bash
gh release create v0.2.0 --title "v0.2.0 - Product Catalogue" --notes-file notes.md
```

### 7. Back-merge into `develop` — **do not skip this**

```bash
git checkout develop
git pull origin develop
git merge --no-ff main -m "chore: back-merge the 0.2.0 release into develop"
```

Without this, the version bump and every stabilisation fix exist only on `main`, and the next release
quietly reintroduces the bugs you just fixed.

> [!IMPORTANT]
> `develop` is protected, so this cannot be pushed directly. In practice the back-merge is carried on
> a short-lived `chore/back-merge-vX.Y.Z` branch cut from `main`, which also carries the next
> `-SNAPSHOT` bump from step 8, and is merged into `develop` through a pull request. See
> [BRANCHING.md](BRANCHING.md#scenario-cutting-a-release).

### 8. Open the next iteration

- `pom.xml`: `0.2.0` → `0.3.0-SNAPSHOT`

```bash
git commit -am "chore: open the 0.3.0 development iteration"
git push origin develop
```

### 9. Close out

- [ ] Close the milestone
- [ ] Open the next milestone

---

## The hotfix path

For a defect in a released version that cannot wait for the next cycle. Full commands are in
[BRANCHING.md](BRANCHING.md#scenario-emergency-hotfix). The shape:

```text
main ──●──────────────────●  v0.1.1
        ╲                ╱
         hotfix/0.1.1───●
                         ╲
develop ──●───●───●───●───●   back-merge
```

1. Branch from `main`, not `develop`.
2. Fix the defect and bump the **patch** version only.
3. Pull request into `main`, review, merge.
4. Tag and publish.
5. Back-merge into `develop`, and into any open `release/*` branch.

> [!WARNING]
> If a release branch is open when a hotfix ships, back-merge into it too. Otherwise the release you
> are about to cut will regress the fix you just shipped.

---

## GitHub Releases

Every tag gets a published GitHub Release. The tag records *that* a version exists; the Release
explains *what it is*.

Release notes should contain:

| Section | Content |
| :--- | :--- |
| **Headline** | One or two sentences: what this release is for. |
| **Highlights** | The two or three things people actually care about. |
| **Full changes** | The `CHANGELOG.md` section for this version. |
| **Upgrade notes** | Anything a consumer must do. Say "nothing required" explicitly when true. |
| **Breaking changes** | Prominent, with a migration path. Omit the section entirely if there are none. |
| **Comparison link** | `https://github.com/RISHII7/E-commerce/compare/v0.1.0...v0.2.0` |

Pre-`1.0.0` releases are marked as **pre-release** on GitHub, so tooling that follows "latest stable"
does not pick up an API that is still moving.

---

## Release cadence

Releases are cut **when a milestone is complete**, not on a calendar.

A release exists to deliver a coherent set of changes. Shipping on a fixed date regardless of
readiness produces either half-finished features or padded releases. Shipping when the milestone
closes produces releases that mean something.

Security fixes are the exception and ship as soon as they are ready, on their own patch version.