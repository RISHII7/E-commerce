<!--
  Thanks for opening a pull request.

  Please fill this in properly rather than deleting it. A reviewer reading a well-described pull
  request finds real problems; a reviewer facing a bare diff approves it.

  Delete any section that genuinely does not apply — but do not delete a section just because
  filling it in takes a moment.
-->

## 📌 Summary

<!--
  What changed, and WHY. Plain language, not a restatement of the diff.
  The reviewer can see what changed; only you can explain why it needed to.
-->



## 🔗 Related issue

<!--
  Use a closing keyword so the issue closes automatically on merge:
    Closes #12    Fixes #12    Resolves #12
  Use "Refs #12" if this only relates to an issue without completing it.
-->

Closes #

## 🏷 Type of change

<!-- Tick everything that applies. -->

- [ ] ✨ **Feature** — adds a capability that did not exist before
- [ ] 🐛 **Bug fix** — corrects behaviour that was wrong
- [ ] 📝 **Documentation** — documentation only
- [ ] ♻️ **Refactor** — restructuring with *identical* observable behaviour
- [ ] ⚡ **Performance** — measurably faster or lighter
- [ ] 🧪 **Tests** — adding or correcting tests
- [ ] 🏗 **Build** — Maven, dependencies, packaging
- [ ] 🔧 **Chore** — housekeeping, no source or test change
- [ ] ⚠️ **Breaking change** — requires consumers to change something

## 🔍 What changed

<!--
  A short walkthrough of the significant changes. Point the reviewer at the parts that need
  attention, and at anything that looks odd but is deliberate.
-->

| File / area | Change |
| :--- | :--- |
|  |  |

## 🧪 How this was tested

<!--
  Be specific. "It works" is not testing.
  What did you actually run, and what did you observe?
-->

- [ ] `./mvnw clean verify` passes locally
- [ ] New or changed behaviour is covered by tests
- [ ] Tested manually — describe how:

```console

```

## ⚠️ Breaking changes

<!--
  Delete this section if there are none.
  If there are: what breaks, and exactly what does a consumer have to do about it?
-->

**Does this break existing consumers?** No / Yes — details below.

<!--
  Migration path:
  1.
  2.
-->

## 📸 Screenshots or output

<!-- Delete if not relevant. API responses, log output, before/after. -->

## 📋 Additional notes

<!--
  Anything the reviewer should know: decisions you were unsure about, follow-up work you have
  deliberately left out (link the issue), or areas where you would especially like a second opinion.
-->

---

## ✅ Self-review checklist

<!-- Please genuinely check these rather than ticking them all at once. -->

**The change itself**

- [ ] I have read my own diff on the **Files changed** tab, line by line
- [ ] It does what the linked issue asked, and nothing unrelated
- [ ] No leftover debug output, commented-out code, or `TODO`s without an issue
- [ ] No secrets, credentials, tokens or personal data
- [ ] No unrelated files are included

**Process**

- [ ] Commit messages follow [Conventional Commits](../docs/COMMIT_CONVENTION.md) and the bodies explain *why*
- [ ] Branch is named `<type>/<issue-number>-<description>`
- [ ] Targeting the correct branch — `develop` for features and bug fixes, `main` only for releases and hotfixes
- [ ] `CHANGELOG.md` `[Unreleased]` is updated, or this change does not affect behaviour

**Quality**

- [ ] Public types and non-obvious logic are documented
- [ ] Documentation is updated if this changes how something is used
- [ ] The branch is up to date with its target branch
