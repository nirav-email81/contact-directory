# Personal Contact Directory — Test Case Documentation

**Version:** 1.1  
**Last updated:** May 2026  
**Test type:** Manual functional testing  
**Application under test:** Personal Contact Directory (GUI and CLI)

---

## 1. Test overview

### 1.1 Objectives

Verify that contact management, validation, persistence, filtering, sorting, search, and CSV import/export behave as specified in the project design.

### 1.2 Test environment

| Item | Requirement |
|------|-------------|
| OS | Windows 10/11 (primary); any OS with Java 17+ for Maven path |
| Java | JDK 17 or later |
| Build | `compile.bat` + `run.bat`, or `mvn compile` |
| Clean DB | Delete `contacts.db` before tests that need empty state (optional) |
| Sample CSV | Create files per Section 6 |

### 1.3 Severity levels

| Level | Meaning |
|-------|---------|
| **Critical** | Data loss, crash, or cannot save/load contacts |
| **High** | Major feature broken (import, export, update) |
| **Medium** | Incorrect filter/sort/validation message |
| **Low** | Cosmetic or minor UX issue |

### 1.4 Test execution log

| Field | Value |
|-------|-------|
| Tester name | |
| Test date | |
| Build / commit | |
| Pass / Fail / Blocked | |

---

## 2. Test case summary

| ID | Module | Cases |
|----|--------|-------|
| TC-START | Startup & persistence | 3 |
| TC-ADD | Add contact | 6 |
| TC-EDIT | Update contact | 3 |
| TC-VAL | Validation | 8 |
| TC-VIEW | View, filter, sort, search | 9 |
| TC-CSV-IMP | CSV import | 8 |
| TC-CSV-EXP | CSV export | 3 |
| TC-CLI | Command-line interface | 4 |
| TC-MIG | Schema & legacy migration | 3 |
| **Total** | | **47** |

---

## 3. Startup and persistence (TC-START)

### TC-START-01 — First launch creates database

| Field | Detail |
|-------|--------|
| **Priority** | Critical |
| **Preconditions** | No `contacts.db` in project folder |
| **Steps** | 1. Run `run.bat`. 2. Close app. |
| **Expected** | `contacts.db` exists; app opens without error dialog |

### TC-START-02 — Reload existing contacts

| Field | Detail |
|-------|--------|
| **Priority** | Critical |
| **Preconditions** | `contacts.db` with at least one contact |
| **Steps** | 1. Launch app. |
| **Expected** | Table shows saved contacts with correct columns |

### TC-START-03 — Startup with corrupt/missing JDBC

| Field | Detail |
|-------|--------|
| **Priority** | High |
| **Preconditions** | Remove `lib/sqlite-jdbc.jar` and `out/sqlite-jdbc.jar`; do not run `compile.bat` |
| **Steps** | 1. Attempt to run without driver on classpath |
| **Expected** | Clear error indicating SQLite driver missing (not silent failure) |

---

## 4. Add contact (TC-ADD)

### TC-ADD-01 — Add contact with all fields

| Field | Detail |
|-------|--------|
| **Priority** | Critical |
| **Steps** | 1. Enter Name, Phone, Extension, Email, Department, Organization, Group=Work. 2. Click **Add**. |
| **Expected** | Success message; row appears in table with all fields; persists after restart |

### TC-ADD-02 — Add contact with required fields only

| Field | Detail |
|-------|--------|
| **Priority** | High |
| **Steps** | 1. Enter Name only. 2. Click **Add**. |
| **Expected** | Contact saved; empty optional fields show blank in table |

### TC-ADD-03 — Empty name rejected

| Field | Detail |
|-------|--------|
| **Priority** | High |
| **Steps** | 1. Leave Name empty. 2. Click **Add**. |
| **Expected** | Warning: name cannot be empty; no new row |

### TC-ADD-04 — Clear form

| Field | Detail |
|-------|--------|
| **Priority** | Low |
| **Steps** | 1. Fill fields. 2. Click **Clear**. |
| **Expected** | All fields empty; Group reset to Friends; button shows **Add** |

### TC-ADD-05 — Default group Friends

| Field | Detail |
|-------|--------|
| **Priority** | Medium |
| **Steps** | 1. Add contact with Name only, default group. |
| **Expected** | Group column shows Friends |

### TC-ADD-06 — Add multiple contacts

| Field | Detail |
|-------|--------|
| **Priority** | High |
| **Steps** | 1. Add three contacts with different names. |
| **Expected** | All three visible; count matches after restart |

---

## 5. Update contact (TC-EDIT)

### TC-EDIT-01 — Load contact for edit

| Field | Detail |
|-------|--------|
| **Priority** | Critical |
| **Steps** | 1. Double-click a table row. |
| **Expected** | Form populated; button text **Update** |

### TC-EDIT-02 — Update contact fields

| Field | Detail |
|-------|--------|
| **Priority** | Critical |
| **Steps** | 1. Double-click row. 2. Change Department and Phone. 3. Click **Update**. |
| **Expected** | Success message; table and DB reflect changes after restart |

### TC-EDIT-03 — Cancel edit by Clear

| Field | Detail |
|-------|--------|
| **Priority** | Medium |
| **Steps** | 1. Double-click row. 2. Change Name. 3. Click **Clear**. 4. Re-open same row. |
| **Expected** | Original name unchanged in table |

---

## 6. Validation (TC-VAL)

### TC-VAL-01 — Phone with letters rejected

| Field | Detail |
|-------|--------|
| **Steps** | Phone = `555-HELP`, Name filled, **Add** |
| **Expected** | Error: phone cannot contain letters |

### TC-VAL-02 — Phone invalid characters

| Field | Detail |
|-------|--------|
| **Steps** | Phone = `555#abc`, **Add** |
| **Expected** | Validation error dialog |

### TC-VAL-03 — Valid phone formats

| Field | Detail |
|-------|--------|
| **Steps** | Try `+1 (555) 010-9999`, `555.0100`, empty phone |
| **Expected** | All accepted when other fields valid |

### TC-VAL-04 — Email without @ rejected

| Field | Detail |
|-------|--------|
| **Steps** | Email = `user.example.com`, **Add** |
| **Expected** | Error: email must contain @ |

### TC-VAL-05 — Valid email

| Field | Detail |
|-------|--------|
| **Steps** | Email = `user@example.com` |
| **Expected** | Saved successfully |

### TC-VAL-06 — Extension invalid

| Field | Detail |
|-------|--------|
| **Steps** | Extension = `ext214`, **Add** |
| **Expected** | Extension validation error |

### TC-VAL-07 — Extension valid

| Field | Detail |
|-------|--------|
| **Steps** | Extension = `214`, `x500`, `#9` (each as separate test) |
| **Expected** | Saved successfully |

### TC-VAL-08 — Empty email and extension allowed

| Field | Detail |
|-------|--------|
| **Steps** | Name only, blank email and extension |
| **Expected** | Saved successfully |

---

## 7. View, filter, sort, search (TC-VIEW)

### TC-VIEW-01 — Filter by Work

| Field | Detail |
|-------|--------|
| **Preconditions** | Contacts in Work and Family groups |
| **Steps** | Filter = Work |
| **Expected** | Only Work contacts shown |

### TC-VIEW-02 — Filter All

| Field | Detail |
|-------|--------|
| **Steps** | Filter = All |
| **Expected** | Every contact shown |

### TC-VIEW-03 — Sort Name A-Z

| Field | Detail |
|-------|--------|
| **Steps** | Sort = Name (A-Z) |
| **Expected** | Table alphabetical by name (case-insensitive) |

### TC-VIEW-04 — Sort Name Z-A

| Field | Detail |
|-------|--------|
| **Steps** | Sort = Name (Z-A) |
| **Expected** | Reverse alphabetical order |

### TC-VIEW-05 — Sort Department A-Z

| Field | Detail |
|-------|--------|
| **Preconditions** | Contacts with different departments |
| **Steps** | Sort = Department (A-Z) |
| **Expected** | Ordered by department, then name |

### TC-VIEW-06 — Search by partial name

| Field | Detail |
|-------|--------|
| **Steps** | 1. Enter partial name. 2. Click **Find**. |
| **Expected** | Only matching names shown |

### TC-VIEW-07 — Search no results

| Field | Detail |
|-------|--------|
| **Steps** | Search for `ZZZNOTFOUND`, **Find** |
| **Expected** | Information dialog: no matches |

### TC-VIEW-08 — Search empty query

| Field | Detail |
|-------|--------|
| **Steps** | Leave search blank, **Find** |
| **Expected** | Warning to enter a name |

### TC-VIEW-09 — Show All resets search and filter

| Field | Detail |
|-------|--------|
| **Steps** | 1. Apply filter and search. 2. **Show All** |
| **Expected** | Search cleared; filter = All; full list displayed |

---

## 8. CSV import (TC-CSV-IMP)

### TC-CSV-IMP-01 — Import new 7-column file

| Field | Detail |
|-------|--------|
| **Sample file** | See Section 6 — `import_full.csv` |
| **Steps** | **Import CSV**, select file |
| **Expected** | All valid rows imported; counts in success dialog |

### TC-CSV-IMP-02 — Import legacy 4-column file

| Field | Detail |
|-------|--------|
| **Sample file** | `import_legacy.csv` |
| **Expected** | Contacts imported; department/org/extension empty |

### TC-CSV-IMP-03 — Header row detection

| Field | Detail |
|-------|--------|
| **Steps** | File with header row matching export format |
| **Expected** | Header not imported as a contact |

### TC-CSV-IMP-04 — Skip row without name

| Field | Detail |
|-------|--------|
| **Sample file** | Row with empty name |
| **Expected** | Row skipped; noted in import summary |

### TC-CSV-IMP-05 — Skip invalid phone row

| Field | Detail |
|-------|--------|
| **Sample file** | Row with phone `BADPHONE` |
| **Expected** | Row skipped with line number in errors |

### TC-CSV-IMP-06 — Invalid group defaults to Friends

| Field | Detail |
|-------|--------|
| **Steps** | Import row with Group = `Other` |
| **Expected** | Contact saved with group Friends |

### TC-CSV-IMP-07 — Quoted CSV fields

| Field | Detail |
|-------|--------|
| **Sample file** | Organization = `"Acme, Inc."` |
| **Expected** | Comma inside quotes preserved correctly |

### TC-CSV-IMP-08 — Cancel file chooser

| Field | Detail |
|-------|--------|
| **Steps** | **Import CSV** → Cancel |
| **Expected** | No changes to contacts |

---

## 9. CSV export (TC-CSV-EXP)

### TC-CSV-EXP-01 — Export all contacts

| Field | Detail |
|-------|--------|
| **Preconditions** | At least two contacts |
| **Steps** | **Export CSV**, save file, open in text editor |
| **Expected** | Header: Name,Phone,Extension,Email,Department,Organization,Group; data rows match DB |

### TC-CSV-EXP-02 — Export with no contacts

| Field | Detail |
|-------|--------|
| **Preconditions** | Empty database |
| **Steps** | **Export CSV** |
| **Expected** | Message: no contacts to export |

### TC-CSV-EXP-03 — Round-trip export then import

| Field | Detail |
|-------|--------|
| **Steps** | 1. Export to `roundtrip.csv`. 2. Clear DB or use fresh DB. 3. Import same file. |
| **Expected** | Contact count and field values match original |

---

## 10. CLI (TC-CLI)

### TC-CLI-01 — Launch CLI mode

| Field | Detail |
|-------|--------|
| **Steps** | `java -cp out;out/sqlite-jdbc.jar ContactDirectoryApp --cli` or Maven equivalent |
| **Expected** | Text menu displayed |

### TC-CLI-02 — View all contacts

| Field | Detail |
|-------|--------|
| **Steps** | Option 1 |
| **Expected** | Lists contacts with phone, extension, email, department, organization when present |

### TC-CLI-03 — Add contact via CLI

| Field | Detail |
|-------|--------|
| **Steps** | Option 2; enter valid data |
| **Expected** | Contact saved; visible in GUI after restart |

### TC-CLI-04 — Search via CLI

| Field | Detail |
|-------|--------|
| **Steps** | Option 3; enter existing name substring |
| **Expected** | Matching contacts printed |

---

## 11. Schema and legacy migration (TC-MIG)

### TC-MIG-01 — Upgrade old database schema

| Field | Detail |
|-------|--------|
| **Preconditions** | Old `contacts.db` without department/organization/phone_extension columns |
| **Steps** | 1. Launch app with new build. 2. Add department to a contact. |
| **Expected** | No SQL errors; new columns persist |

### TC-MIG-02 — Legacy contacts.txt import

| Field | Detail |
|-------|--------|
| **Preconditions** | No `contacts.db`; `contacts.txt` with `Name|phone|email|group` lines |
| **Steps** | Launch app |
| **Expected** | Data in DB; `contacts.txt` renamed to `contacts.txt.bak` |

### TC-MIG-03 — No re-import when DB populated

| Field | Detail |
|-------|--------|
| **Preconditions** | DB has data; `contacts.txt` present |
| **Steps** | Launch app |
| **Expected** | `contacts.txt` not imported again |

---

## 12. Sample test data

### import_full.csv

```csv
Name,Phone,Extension,Email,Department,Organization,Group
Alice Smith,555-0101,101,alice@example.com,Engineering,Acme Corp,Work
Bob Jones,555-0102,,bob@example.com,Sales,Acme Corp,Work
Carol Lee,555-0103,9,carol@example.com,,,Family
```

### import_legacy.csv

```csv
Dave Brown,555-0200,dave@example.com,Friends
Eve White,555-0201,eve@example.com,Work
```

### import_invalid.csv (for negative tests)

```csv
Name,Phone,Extension,Email,Department,Organization,Group
,555-0300,1,missing@example.com,IT,Test Inc,Work
Frank Bad,555-HELP,2,frank@example.com,IT,Test Inc,Work
Grace OK,555-0302,3,grace@example.com,HR,"Company, LLC",Friends
```

---

## 13. Defect report template

| Field | Value |
|-------|-------|
| Defect ID | |
| Related test case | e.g. TC-VAL-01 |
| Summary | |
| Steps to reproduce | |
| Expected | |
| Actual | |
| Severity | Critical / High / Medium / Low |
| Environment | OS, Java version, commit hash |
| Screenshots / logs | |

---

## 14. Traceability matrix

| Requirement (design doc) | Test cases |
|--------------------------|------------|
| CRUD contacts | TC-ADD, TC-EDIT, TC-START |
| Validation | TC-VAL |
| Filter / sort / search | TC-VIEW |
| CSV import | TC-CSV-IMP |
| CSV export | TC-CSV-EXP |
| CLI | TC-CLI |
| Schema migration | TC-MIG |
| Legacy .txt import | TC-MIG-02 |
