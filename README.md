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
run.bat
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
pom.xml                     - Maven + sqlite-jdbc dependency
contacts.db                 - database (created at runtime)
```

## Documentation

| Document | Description |
|----------|-------------|
| [docs/PROJECT_DESIGN.md](docs/PROJECT_DESIGN.md) | Architecture, data model, workflows, UI design |
| [docs/TEST_CASES.md](docs/TEST_CASES.md) | Manual test cases and sample CSV data |

## GUI layout

- Default **980×560** window (resizable)
- **Left sidebar** (scrollable): contact form, filter, sort, search, Import/Export CSV
- **Right pane**: scrollable contact table (7 columns)
- **Double-click** a row to load that contact for editing
