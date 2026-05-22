# Personal Contact Directory (Java)

A Java Swing application to store, view, search, filter, and export contacts. Data is stored in a **SQLite** database (`contacts.db`).

## Features

- **View All Contacts** — Table with Name, Phone, Email, and Group
- **Add / Update Contact** — Sidebar fields with validation; double-click a row to edit
- **Search Contacts** — Filter by name (partial match)
- **Filter by Group** — Work, Family, Friends
- **Input validation** — Phone cannot contain letters; email must include `@`
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
- **Table:** `contacts` — `id`, `name`, `phone`, `email`, `group_name`

On first run, if `contacts.txt` exists and the database is empty, records are imported into the database and the text file is renamed to `contacts.txt.bak`.

## Project layout

```
src/
  Contact.java
  ContactValidator.java
  ContactDatabase.java      - SQLite JDBC layer
  ContactStorage.java       - facade + CSV export
  ContactDirectoryApp.java  - Swing GUI
  ContactDirectoryCli.java  - optional CLI
pom.xml                     - Maven + sqlite-jdbc dependency
contacts.db                 - database (created at runtime)
```

## GUI layout

- Fixed **600×480** window
- **Left sidebar**: inputs, group dropdown, Add/Update & Clear, group filter, search, Export CSV
- **Right pane**: contact table
- **Double-click** a row to load that contact for editing
