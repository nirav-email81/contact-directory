# CI/CD Options for Personal Contact Directory

This project is a **Java 17 desktop application** with Maven and batch scripts. CI/CD focuses on **build verification**, **release packaging**, and **optional distribution**—not cloud deployment of a server.

---

## 1. What CI/CD should do for this project

| Stage | Goal |
|-------|------|
| **CI (Continuous Integration)** | Every push/PR compiles code and (later) runs tests |
| **CD (Continuous Delivery)** | Publish a versioned JAR when `main` is updated |
| **Release (Continuous Deployment)** | On git tag `v*`, attach installers/JAR to GitHub Releases |

```mermaid
flowchart LR
    Dev[Developer push]
    CI[CI: compile + test]
    Main[main branch]
    Rel[Release workflow]
    Art[JAR artifact]
    User[End user download]

    Dev --> CI
    CI --> Main
    Main --> Rel
    Rel --> Art
    Art --> User
```

---

## 2. Platform comparison

| Platform | Best for | Pros | Cons | Fit for this project |
|----------|----------|------|------|----------------------|
| **GitHub Actions** | Repos already on GitHub | Free for public repos; native Releases; easy secrets | Minutes limits on free private repos | **Recommended** — repo is on GitHub |
| **GitLab CI** | GitLab-hosted repos | Built-in registry; strong pipeline YAML | Another platform if you use GitHub | Good if you migrate to GitLab |
| **Azure DevOps** | Microsoft-centric teams | Boards + Pipelines + Artifacts | Heavier setup for a small desktop app | Use if org standard is Azure |
| **Jenkins** | Self-hosted, full control | Unlimited customization; plugins | You maintain servers/agents | On-prem enterprises |
| **CircleCI** | Fast cloud builds | Strong Docker support | Extra service vs GitHub Actions | Optional alternative |
| **Bitbucket Pipelines** | Atlassian stack | Jira integration | Less common for solo/small Java desktop | Only if using Bitbucket |

**Recommendation:** Start with **GitHub Actions** (workflows included in `.github/workflows/`). Add Jenkins or Azure only if your organization requires it.

---

## 3. Option A — GitHub Actions (implemented)

### Workflows in this repository

| File | Trigger | Actions |
|------|---------|---------|
| `.github/workflows/ci.yml` | Push/PR to `main` or `develop` | Compile with Maven (`dev` + `prod` profiles) |
| `.github/workflows/release.yml` | Push tag `v*` | Build fat JAR with `-Pprod`, upload to GitHub Release |

### Enable

1. Push repository to GitHub (already done).
2. Actions tab → workflows run automatically.
3. Create a release: `git tag v1.1.0 && git push origin v1.1.0`.

### Optional enhancements

- Add JUnit job when automated tests exist
- `actions/upload-artifact` for PR build artifacts
- Code signing for Windows `.exe` installer (advanced)
- Dependabot for `pom.xml` dependency updates

### Sample: run CI locally with [act](https://github.com/nektos/act)

```bash
act push -j build
```

---

## 4. Option B — GitLab CI

Use if the project moves to GitLab.

```yaml
# .gitlab-ci.yml (example — not in repo by default)
stages: [build, release]

build:
  stage: build
  image: eclipse-temurin:17
  script:
    - mvn -B clean compile -Pdev
    - mvn -B compile -Pprod
  rules:
    - if: $CI_PIPELINE_SOURCE == "merge_request_event"
    - if: $CI_COMMIT_BRANCH == "main"

release:
  stage: release
  image: eclipse-temurin:17
  script:
    - mvn -B package -Pprod
  artifacts:
    paths:
      - target/*.jar
  rules:
    - if: $CI_COMMIT_TAG =~ /^v/
```

**Pros:** Single file, integrated registry. **Cons:** Migration from GitHub.

---

## 5. Option C — Azure DevOps Pipelines

Use for corporate environments with Azure Boards and Windows agents.

**Pipeline outline**

1. **CI trigger:** `main`, `develop`
2. **Agent:** `windows-latest`
3. **Steps:** `JavaToolInstaller@0` (JDK 17) → `Maven@4` (`clean compile`) → publish test results
4. **Release:** pipeline triggered by tag; copy `target/*.jar` to Universal Packages or GitHub Releases

**Pros:** Enterprise compliance, hybrid agents. **Cons:** More setup than GitHub Actions for a small repo.

---

## 6. Option D — Jenkins (self-hosted)

Use when builds must run inside your network.

**Typical setup**

| Item | Suggestion |
|------|------------|
| Agent | Windows node with JDK 17 + Maven |
| Job类型 | Multibranch Pipeline |
| Jenkinsfile | Declarative pipeline in repo root |
| Triggers | Poll SCM or webhook from GitHub |

```groovy
// Jenkinsfile (example sketch)
pipeline {
  agent { label 'windows' }
  stages {
    stage('Compile') {
      steps { bat 'mvn -B clean compile -Pdev' }
    }
    stage('Package Prod') {
      when { branch 'main' }
      steps { bat 'mvn -B package -Pprod' }
    }
  }
  post {
    success { archiveArtifacts 'target/*.jar' }
  }
}
```

**Pros:** Full control, internal artifacts. **Cons:** Server maintenance, plugin updates.

---

## 7. Option E — Manual / script-only CD (minimal)

No cloud CI; suitable for solo offline development.

| Step | Command |
|------|---------|
| Verify | `compile.bat` or `mvn compile` |
| Test | Manual `docs/TEST_CASES.md` |
| Package | `mvn package -Pprod` |
| Distribute | Copy JAR + `run-prod.bat` via USB/share |

**Pros:** Zero infrastructure. **Cons:** No automated gate on every change.

---

## 8. Suggested pipeline maturity path

| Phase | CI/CD capability |
|-------|------------------|
| **1 (now)** | GitHub Actions compile on PR; prod JAR on tag |
| **2** | JUnit tests in CI; branch protection on `main` |
| **3** | Signed Windows installer (WiX/Install4j) in release job |
| **4** | Auto-update check or internal package repository |

---

## 9. Secrets and environments (GitHub Actions)

For future needs:

| Secret | Use |
|--------|-----|
| `GITHUB_TOKEN` | Default; uploads release assets |
| Code signing cert | Windows Authenticode (store as base64 secret) |
| `NEXUS_PASSWORD` | Publish to corporate Maven repo (optional) |

Use GitHub **Environments** (`development`, `production`) with protection rules on the `production` environment for release approvals.

---

## 10. Quick reference commands

```bash
# Local dev
run-dev.bat

# Local prod simulation
run-prod.bat

# CI-equivalent compile
mvn -B clean compile -Pdev
mvn -B compile -Pprod

# Release-equivalent package
mvn -B clean package -Pprod

# Tag release (triggers GitHub Actions release workflow)
git tag v1.1.0
git push origin v1.1.0
```

---

## 11. Decision summary

| Your situation | Choose |
|----------------|--------|
| Code on GitHub, small team | **GitHub Actions** (included) |
| Company uses Azure | **Azure DevOps** |
| Air-gapped / on-prem | **Jenkins** or manual CD |
| Already on GitLab | **GitLab CI** |

See [ENVIRONMENTS.md](ENVIRONMENTS.md) for dev vs prod configuration details.
