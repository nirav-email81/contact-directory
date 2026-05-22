# Branching Strategy

## Branches

| Branch | Role | Environment | CI on push |
|--------|------|-------------|------------|
| **`develop`** | Integration / daily work | Development (`CONTACT_ENV=dev`) | Yes — compile dev + prod |
| **`master`** | Production-ready code | Production (`CONTACT_ENV=prod`) | Yes — compile + package JAR |

## Workflow

```mermaid
gitGraph
   commit id: "feature"
   branch develop
   checkout develop
   commit id: "integrate on develop"
   checkout master
   merge develop id: "PR merge"
   commit id: "tag v1.x" type: HIGHLIGHT
```

1. Create a feature branch from `develop` (optional): `feature/my-change`
2. Merge feature → `develop` via pull request
3. When ready for release, open PR: **`develop` → `master`**
4. After merge to `master`, tag a release: `git tag v1.1.0 && git push origin v1.1.0`
5. GitHub Actions **Release** workflow publishes the JAR and ZIP

## GitHub Actions

| Workflow | File | When it runs |
|----------|------|--------------|
| **CI** | `.github/workflows/ci.yml` | Push/PR to `develop` or `master`; manual dispatch |
| **Release** | `.github/workflows/release.yml` | Push tag `v*` on `master` |

### CI jobs

- **compile** — Maven `compile` with `-Pdev` and `-Pprod`
- **package-prod** — Fat JAR on pushes to `master` only
- **ci-summary** — Fails if compile failed

### Release artifacts

- `contact-directory-*.jar` — runnable fat JAR (`-Pprod`)
- `contact-directory-release.zip` — JAR + `run-prod.bat`, `config/`, scripts

## Local commands

```bash
# First-time: track develop
git checkout develop
git pull origin develop

# Feature work
git checkout develop
git checkout -b feature/add-export-filter
# ... commit ...
git push -u origin feature/add-export-filter

# Release tag (from master after merge)
git checkout master
git pull origin master
git tag v1.1.0
git push origin v1.1.0
```

## Branch protection (recommended on GitHub)

Configure in **Settings → Branches** for `master`:

- Require pull request before merging
- Require status check: **CI / compile** (and **CI summary**)
- Require branches to be up to date
- Do not allow bypassing (optional)

For `develop`:

- Require PR for merges from feature branches (optional)
- Require CI status check
