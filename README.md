<div align="center">

# 🛒 E-Commerce Service

**A production-minded e-commerce backend built with Spring Boot 4 and Java 26.**

*Engineered under a strict Git Flow workflow, Conventional Commits, semantic versioning and a hand-curated changelog — because how software is built matters as much as what is built.*

<br />

[![Java](https://img.shields.io/badge/Java-26-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/26/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Maven](https://img.shields.io/badge/Maven-Wrapper-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/wrapper/)
[![License](https://img.shields.io/badge/License-MIT-3DA639?style=for-the-badge)](LICENSE)

[![Release](https://img.shields.io/github/v/release/RISHII7/E-commerce?style=flat-square&label=release&color=blue&include_prereleases&sort=semver)](https://github.com/RISHII7/E-commerce/releases)
[![Conventional Commits](https://img.shields.io/badge/commits-conventional-FE5196?style=flat-square&logo=conventionalcommits&logoColor=white)](docs/COMMIT_CONVENTION.md)
[![Changelog](https://img.shields.io/badge/changelog-keep%20a%20changelog-E05735?style=flat-square)](CHANGELOG.md)
[![Workflow](https://img.shields.io/badge/workflow-git%20flow-F05032?style=flat-square&logo=git&logoColor=white)](docs/BRANCHING.md)
[![SemVer](https://img.shields.io/badge/versioning-semver-3F4551?style=flat-square)](docs/VERSIONING.md)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen?style=flat-square)](CONTRIBUTING.md)
[![Last Commit](https://img.shields.io/github/last-commit/RISHII7/E-commerce/develop?style=flat-square&label=last%20commit)](https://github.com/RISHII7/E-commerce/commits/develop)

<br />

[**Getting Started**](#-getting-started) &nbsp;·&nbsp;
[**Tech Stack**](#-tech-stack) &nbsp;·&nbsp;
[**Project Structure**](#-project-structure) &nbsp;·&nbsp;
[**Workflow**](#-development-workflow) &nbsp;·&nbsp;
[**Roadmap**](#-roadmap) &nbsp;·&nbsp;
[**Contributing**](#-contributing)

</div>

---

## 📖 About

**E-Commerce Service** is the backend for an online store. It is being built as a REST API that will
eventually own the full commerce lifecycle — product catalogue, inventory, carts, orders, payments
and customer accounts.

The project has a second, equally deliberate purpose: to be an example of a repository that is
**maintained properly from the very first commit**. Every change enters through an issue, travels on
its own branch, arrives via a reviewed pull request, and is recorded in a changelog written for
humans. Nothing is pushed straight to a long-lived branch. The Git history is treated as
documentation, not as an afterthought.

> [!NOTE]
> The project is in early development. The repository foundation — workflow, governance, templates
> and documentation — is complete. Business functionality begins in the
> [v0.2.0 milestone](https://github.com/RISHII7/E-commerce/milestone/2).

### Guiding principles

| Principle | What it means in practice |
| :--- | :--- |
| **Readable history** | Every commit explains *why* the change was made, not just what changed. |
| **Nothing unreviewed** | `main` and `develop` are protected. All changes arrive through pull requests. |
| **Traceability** | Issue → branch → commits → pull request → changelog entry → release tag. |
| **Small, honest releases** | Semantic versions, annotated tags, and release notes that state the truth. |
| **Convention over memory** | Rules are written down and enforced by tooling, not held in someone's head. |

---

## 🧰 Tech Stack

| Technology | Version | Why it is here |
| :--- | :--- | :--- |
| **Java** | 26 | The language and runtime. A current LTS-track release gives access to modern language features and the newest JVM performance work. |
| **Spring Boot** | 4.1.0 | Application framework. Handles dependency injection, configuration, auto-wiring and packaging so the project can focus on domain logic. |
| **Spring Web MVC** | via starter | Builds the REST API — controllers, request mapping, content negotiation and the embedded web server. |
| **Lombok** | managed by Spring Boot | Removes boilerplate such as getters, setters, constructors and builders, keeping domain classes readable. |
| **Maven** | via Wrapper | Build, dependency management and test execution. The Wrapper pins the exact Maven version so every machine builds identically. |
| **JUnit 5 + Spring Test** | via starter | Automated testing, from plain unit tests up to full application context tests. |

---

## 🚀 Getting Started

### Prerequisites

| Requirement | Minimum | How to check |
| :--- | :--- | :--- |
| **JDK** | 26 | `java -version` |
| **Git** | 2.30+ | `git --version` |
| **Maven** | *not required* | The bundled Maven Wrapper downloads the correct version automatically. |

### 1. Clone the repository

```bash
git clone https://github.com/RISHII7/E-commerce.git
cd E-commerce
```

> [!TIP]
> The default branch is **`develop`**, which is the integration branch where day-to-day work lands.
> `main` only ever contains released, tagged code. See [docs/BRANCHING.md](docs/BRANCHING.md).

### 2. Build the project

<table>
<tr><th align="left">Linux / macOS</th><th align="left">Windows</th></tr>
<tr>
<td>

```bash
./mvnw clean install
```

</td>
<td>

```powershell
.\mvnw.cmd clean install
```

</td>
</tr>
</table>

### 3. Run the application

<table>
<tr><th align="left">Linux / macOS</th><th align="left">Windows</th></tr>
<tr>
<td>

```bash
./mvnw spring-boot:run
```

</td>
<td>

```powershell
.\mvnw.cmd spring-boot:run
```

</td>
</tr>
</table>

The service starts on **`http://localhost:8080`** by default.

### 4. Run the tests

```bash
./mvnw test          # unit and context tests
./mvnw verify        # the full build, exactly as a pipeline would run it
```

---

## 📂 Project Structure

```text
E-commerce/
│
├── .github/                          # Everything GitHub reads to run the collaboration process
│   ├── ISSUE_TEMPLATE/               #   Structured issue forms (bug, feature, task, docs)
│   ├── PULL_REQUEST_TEMPLATE.md      #   Checklist every pull request is opened with
│   └── CODEOWNERS                    #   Who is automatically asked to review which paths
│
├── .mvn/wrapper/                     # Maven Wrapper — pins the build tool version
│
├── docs/                             # The project's engineering handbook
│   ├── BRANCHING.md                  #   The Git Flow model, in full
│   ├── COMMIT_CONVENTION.md          #   How commit messages must be written
│   └── VERSIONING.md                 #   Semantic versioning and the release process
│
├── src/
│   ├── main/
│   │   ├── java/com/app/ecom/        # Application source
│   │   │   └── EcomApplication.java  #   Spring Boot entry point
│   │   └── resources/
│   │       └── application.properties#   Configuration
│   └── test/
│       └── java/com/app/ecom/        # Test source
│           └── EcomApplicationTests.java
│
├── .editorconfig                     # Shared editor settings across IDEs and operating systems
├── .gitattributes                    # Line-ending normalisation and diff behaviour
├── .gitmessage                       # Commit message template loaded into your editor
├── CHANGELOG.md                      # Hand-curated, Keep a Changelog format
├── CODE_OF_CONDUCT.md                # Contributor Covenant
├── CONTRIBUTING.md                   # How to work on this project
├── LICENSE                           # MIT
├── SECURITY.md                       # How to report a vulnerability privately
├── SUPPORT.md                        # Where to ask for help
└── pom.xml                           # Maven build definition
```

---

## 🔀 Development Workflow

This repository follows **Git Flow**. Two branches live forever; everything else is temporary.

```text
main      ──●─────────────────────●──────────────●        tagged, released code only
             ╲                   ╱ ╲            ╱
              ╲            release/ ╲     hotfix/
               ╲               ╱     ╲        ╱
develop   ──●───●───●───●───●─●───●───●──────●───●        integration branch
             ╲     ╱ ╲     ╱   ╲     ╱
              feature/  feature/  bugfix/
```

| Branch | Cut from | Merges into | Purpose |
| :--- | :--- | :--- | :--- |
| `main` | — | — | Production. Every commit here is a tagged release. |
| `develop` | `main` | — | Integration. The next release accumulates here. |
| `feature/*` | `develop` | `develop` | A new capability. |
| `bugfix/*` | `develop` | `develop` | A defect found before release. |
| `release/*` | `develop` | `main` **and** `develop` | Stabilise and version a release. |
| `hotfix/*` | `main` | `main` **and** `develop` | Urgent fix for a live defect. |

### Commit messages

Commits follow [**Conventional Commits**](https://www.conventionalcommits.org/) with a subject line
that states the change and a body that explains the reasoning behind it.

```text
feat(catalog): let customers filter products by price range

Customers browsing a large catalogue currently have to scroll through
every product to find one within their budget. This adds optional
minPrice and maxPrice query parameters to the product listing endpoint.

Both parameters are optional and validated independently, so passing
only one still narrows the results. Invalid ranges — where the minimum
exceeds the maximum — return 400 rather than silently returning nothing,
because an empty result would look like a data problem to the caller.

Closes #42
```

The full specification lives in [**docs/COMMIT_CONVENTION.md**](docs/COMMIT_CONVENTION.md).

### Versioning and releases

Versions follow [**Semantic Versioning**](https://semver.org/): `MAJOR.MINOR.PATCH`. Releases are cut
on a `release/*` branch, merged into `main`, marked with an **annotated** `vX.Y.Z` tag, published as
a GitHub Release, and back-merged into `develop`. Tags are never moved or deleted.

Full detail in [**docs/VERSIONING.md**](docs/VERSIONING.md).

---

## 🗺 Roadmap

| Milestone | Focus | Status |
| :--- | :--- | :--- |
| [**v0.1.0** — Repository Foundation](https://github.com/RISHII7/E-commerce/milestone/1) | Branching model, commit convention, contribution guide, templates, code ownership, branch protection, changelog | 🚧 In progress |
| [**v0.2.0** — Domain Foundation](https://github.com/RISHII7/E-commerce/milestone/2) | Layered architecture, persistence and migrations, the Product vertical slice, error handling, API documentation | 📋 Planned |
| **v0.3.0** — Catalogue & Inventory | Categories, search and filtering, stock tracking | 💭 Proposed |
| **v0.4.0** — Cart & Orders | Shopping cart, checkout, order lifecycle | 💭 Proposed |
| **v0.5.0** — Accounts & Security | Registration, authentication, roles and permissions | 💭 Proposed |
| **v1.0.0** — First stable release | A frozen public API with a compatibility guarantee | 🎯 Goal |

---

## 🤝 Contributing

Contributions are welcome. Start with [**CONTRIBUTING.md**](CONTRIBUTING.md) — it covers environment
setup, branch naming, commit rules, the pull request process and what reviewers look for.

| Document | What it answers |
| :--- | :--- |
| [CONTRIBUTING.md](CONTRIBUTING.md) | How do I make a change and get it merged? |
| [docs/BRANCHING.md](docs/BRANCHING.md) | Which branch do I cut from, and where does it go back? |
| [docs/COMMIT_CONVENTION.md](docs/COMMIT_CONVENTION.md) | How exactly do I write this commit message? |
| [docs/VERSIONING.md](docs/VERSIONING.md) | What version number does this change deserve? |
| [CHANGELOG.md](CHANGELOG.md) | What actually changed, and when? |
| [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md) | How are people expected to treat each other here? |
| [SECURITY.md](SECURITY.md) | I found a vulnerability — who do I tell, privately? |
| [SUPPORT.md](SUPPORT.md) | I have a question. Where do I ask it? |

---

## 📜 License

Released under the [**MIT License**](LICENSE). You may use, copy, modify and distribute this software,
including commercially, provided the copyright notice and licence text are retained.

---

<div align="center">

**Built by [RISHII7](https://github.com/RISHII7)**

If this repository is useful to you, a ⭐ is genuinely appreciated.

</div>