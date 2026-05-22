# Personal Contact Directory (Java)

A Java Swing application to store, view, search, filter, and export contacts. Data is stored in a **SQLite** database (`contacts.db`).

## Features

- **View All Contacts** — Table with name, phone, extension, email, department, organization, and group
- **Add / Update Contact** — Sidebar fields with validation; double-click a row to edit
- **Search Contacts** — Filter by name (partial match)
- **Filter by Group** — Work, Family, Friends
- **Sort contacts** — Name, group, email, phone, department, or organization
- **Input validation** — Phone cannot contain letters; email must include `@`; extension allows digits and `x`
- **Import CSV** — Load contacts from a `.csv` file (supports old 4-column exports and the new 7-column format)
- **Export CSV** — Save to `.csv` for Excel or Google Sheets
- **SQLite database** — Persistent storage in `contacts.db`

## Requirements

- Java 17 or later

## Build and run

**Windows (recommended — downloads SQLite driver automatically):**

```bat
compile.bat
run-dev.bat
```

Production (separate database under your user profile):

```bat
run-prod.bat
```

**Maven (optional):**

Requires [Apache Maven](https://maven.apache.org/).


```bash
mvn compile
mvn exec:java -Dexec.mainClass=ContactDirectoryApp
```

**CLI mode:**

```bash
mvn exec:java -Dexec.mainClass=ContactDirectoryApp -Dexec.args="--cli"
```

**Fat JAR (optional):**

```bash
mvn package
java -jar target/contact-directory-1.0.0.jar
```

## Database

- **File:** `contacts.db` (SQLite, created automatically)
- **Table:** `contacts` — `id`, `name`, `phone`, `email`, `group_name`, `department`, `organization`, `phone_extension`

**CSV columns (import/export):** Name, Phone, Extension, Email, Department, Organization, Group

On first run, if `contacts.txt` exists and the database is empty, records are imported into the database and the text file is renamed to `contacts.txt.bak`.

## Project layout

```
src/
  Contact.java
  ContactValidator.java
  ContactDatabase.java      - SQLite JDBC layer
  ContactStorage.java       - facade + CSV import/export
  ContactCsv.java           - CSV parsing and formatting
  ContactDirectoryApp.java  - Swing GUI
  ContactDirectoryCli.java  - optional CLI
  AppEnvironment.java       - dev/prod configuration
config/
  application-dev.properties
  application-prod.properties
pom.xml                     - Maven + sqlite-jdbc dependency
contacts-dev.db             - dev database (created at runtime)
```

## Environments

| Environment | Run command | Database |
|-------------|-------------|----------|
| **Development** (default) | `run-dev.bat` | `contacts-dev.db` in project folder |
| **Production** | `run-prod.bat` | `%LOCALAPPDATA%\ContactDirectory\contacts.db` |

Set `CONTACT_ENV=dev` or `CONTACT_ENV=prod`, or pass `-Dcontact.env=prod`. See [docs/ENVIRONMENTS.md](docs/ENVIRONMENTS.md).

Maven: `mvn compile -Pdev` or `mvn package -Pprod`.

## CI/CD

GitHub Actions workflows build on every push/PR and publish a JAR when you push a version tag (`v1.0.0`).

| Document / file | Description |
|-----------------|-------------|
| [docs/CICD_OPTIONS.md](docs/CICD_OPTIONS.md) | GitHub Actions, GitLab, Azure DevOps, Jenkins comparison |
| [.github/workflows/ci.yml](.github/workflows/ci.yml) | Compile dev + prod profiles |
| [.github/workflows/release.yml](.github/workflows/release.yml) | Release JAR on tag |

## Documentation

| Document | Description |
|----------|-------------|
| [docs/PROJECT_DESIGN.md](docs/PROJECT_DESIGN.md) | Architecture, data model, workflows, UI design |
| [docs/TEST_CASES.md](docs/TEST_CASES.md) | Manual test cases and sample CSV data |
| [docs/ENVIRONMENTS.md](docs/ENVIRONMENTS.md) | Development vs production setup |
| [docs/CICD_OPTIONS.md](docs/CICD_OPTIONS.md) | CI/CD platform options |

## GUI layout

- Default **980×560** window (resizable)
- **Left sidebar** (scrollable): contact form, filter, sort, search, Import/Export CSV
- **Right pane**: scrollable contact table (7 columns)
- **Double-click** a row to load that contact for editing
