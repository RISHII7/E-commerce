# Security Policy

> **Never report a security vulnerability in a public issue.**
> A public report tells everyone how to exploit the problem before there is a fix available.

---

## Supported versions

| Version | Supported | Notes |
| :--- | :---: | :--- |
| `0.1.x` | ✅ | Current release line. |
| `< 0.1.0` | ❌ | Pre-release development, never published. |

While the project is in `0.x`, **only the most recent minor release line receives security fixes.**
Once `1.0.0` is released, this table will be extended with a defined support window for older lines.

---

## Reporting a vulnerability

### Use GitHub Private Vulnerability Reporting

**→ [Report a vulnerability privately](https://github.com/RISHII7/E-commerce/security/advisories/new)**

This is the preferred channel. It creates a private advisory visible only to you and the maintainers,
where the problem can be discussed, a fix developed and tested, and a CVE requested if warranted —
all before anything becomes public.

If for any reason you cannot use that form, open a
[Discussion](https://github.com/RISHII7/E-commerce/discussions) asking a maintainer to contact you
privately. **Do not include any detail of the vulnerability in that message.**

### What to include

The more of this you can provide, the faster it can be fixed:

| | |
| :--- | :--- |
| **Type of issue** | Injection, authentication bypass, privilege escalation, data exposure, denial of service… |
| **Affected version** | Release tag or commit SHA. |
| **Affected component** | File paths, endpoints or configuration involved. |
| **Reproduction steps** | Step by step, from a clean state. This is the single most valuable thing you can provide. |
| **Proof of concept** | Any code, request or payload that demonstrates it. |
| **Impact** | What can an attacker actually do? Read data, modify it, take over an account, take down the service? |
| **Suggested fix** | If you have one. Entirely optional. |
| **Your setup** | OS, Java version, deployment configuration, anything unusual. |

### What to expect

| Stage | Target |
| :--- | :--- |
| **Acknowledgement** | Within **48 hours** |
| **Initial assessment** | Within **7 days** — confirmed or not, with severity |
| **Progress updates** | At least every **7 days** while work is ongoing |
| **Fix for critical issues** | Target **14 days** from confirmation |
| **Fix for other severities** | Target **90 days** from confirmation |

If a report is declined, you will be told **why**, not simply ignored.

> [!NOTE]
> This is a personal open-source project maintained in spare time, not a commercial product with a
> staffed security team. These targets are honest intentions rather than a contractual guarantee.
> They are stated because a policy with no timeframes tells a reporter nothing about whether they
> have been forgotten.

---

## Disclosure process

1. You report privately through the advisory form.
2. Receipt is acknowledged and the report is assessed.
3. If confirmed, a fix is developed **in a private fork**, so the work does not appear in the public
   commit history before release.
4. The fix ships as a new patch version, released through the
   [hotfix path](docs/BRANCHING.md#scenario-emergency-hotfix).
5. A security advisory is published, crediting you unless you prefer to stay anonymous.
6. The `Security` section of [CHANGELOG.md](CHANGELOG.md) records the fix.

**Coordinated disclosure.** Please give a reasonable window to ship a fix before publishing details.
Ninety days is the widely accepted norm, and shorter is reasonable for something already being
exploited. Working together protects the people running this software; racing to publish does not.

---

## Recognition

Anyone who reports a valid vulnerability is credited in the published advisory and in the release
notes, unless they ask not to be. There is no bug bounty — this is an unfunded open-source project —
but the contribution is genuinely valued and will be acknowledged publicly.

---

## Scope

### In scope

- Anything in this repository: application source, build configuration, dependency declarations.
- Vulnerabilities in dependencies that are **exploitable through this application**.
- Authentication and authorisation flaws.
- Injection of any kind: SQL, command, template, expression.
- Exposure of sensitive data through the API, logs or error responses.
- Insecure defaults in shipped configuration.

### Out of scope

- Vulnerabilities in dependencies that this application does not expose. Report those upstream.
- Issues that require an attacker to already have server or database access.
- Findings from an automated scanner with **no demonstrated exploit path**. Please verify before
  reporting.
- Missing hardening headers with no demonstrable impact.
- Social engineering, physical attacks, or attacks against contributors.
- Denial of service through sheer volume of traffic.
- Anything in a third-party service the project merely links to.

---

## For contributors

Security is part of ordinary review, not a separate exercise:

- **Never commit secrets.** No credentials, API keys, tokens, private keys or connection strings.
  Anything committed to Git is permanently in the history even after it is deleted, so a leaked
  secret must be **rotated**, not just removed.
- **Validate all input** at the boundary. Treat every request field as hostile until proven otherwise.
- **Use parameterised queries.** Never build SQL by string concatenation.
- **Do not log sensitive data.** Passwords, tokens, full card numbers and personal data must never
  reach a log file.
- **Return generic errors to clients.** Stack traces and internal messages tell an attacker how the
  system is built. Log the detail; return something safe.
- **Keep dependencies current.** Most real-world vulnerabilities arrive through a dependency that was
  known-vulnerable and simply never updated.

---

## Maintainer note

The contact route above is deliberately GitHub-native rather than an email address, so that no
personal address is published on a public repository. If you would prefer a dedicated security email
here, add it to this section — the rest of the policy needs no change.
