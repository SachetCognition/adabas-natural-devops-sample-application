# NaturalCruise End-to-End Test Plan

This document defines the comprehensive end-to-end test cases for the NaturalCruise application. Each test case covers a specific functional area of the application and can be executed manually via the Natural terminal or automated using the `NCTESTP` test program where applicable.

---

## Test Environment Prerequisites

- Adabas database DBID **012** running with files **041** (NCCRUISE) and **042** (NCYACHT) loaded
- Natural Development Server (NDV) running on port **2700**
- NTCRUISE library cataloged and available
- Test data loaded per `test-data-setup.md`

---

## TC-01: Main Menu Navigation

**Objective:** Verify that the main menu program `NCMENUP` loads correctly and all navigation options work as expected.

**Preconditions:** NTCRUISE library is active; `NCMENUP` is cataloged.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCMENUP` from the Natural command line | The menu map `NCMENUM` is displayed with three options: (1) Cruise Input, (2) Cruise Report, (3) Exit |
| 2 | Enter `1` and press ENTER | Program `NCINMAPP` is invoked; the cruise detail input map `NCDEMAPM` is displayed |
| 3 | Press PF3 to return, then re-enter `NCMENUP` | Menu is displayed again |
| 4 | Enter `2` and press ENTER | Program `NCATENDP` is invoked; a paginated cruise report is displayed |
| 5 | Re-enter `NCMENUP`, enter `3` and press ENTER | The application terminates (TERMINATE command executed) |
| 6 | Re-enter `NCMENUP`, press PF3 | The application terminates (PF3 mapped to TERMINATE) |
| 7 | Re-enter `NCMENUP`, enter an invalid value (e.g., `9`) and press ENTER | Message `Sorry - Selection not available` is displayed via REINPUT |
| 8 | Press PF2 with a valid selection | Same behavior as pressing ENTER (PF2 is mapped alongside ENTR) |

**Pass Criteria:** All navigation paths behave as described. Invalid selections produce the error message without crashing.

---

## TC-02: Cruise Lookup - Valid ID

**Objective:** Verify that entering a known cruise ID in `NCINMAPP` returns correct cruise details via `NCFINDCR`.

**Preconditions:** At least one cruise record exists in NCCRUISE with a matching yacht in NCYACHT.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCINMAPP` (or select option 1 from menu) | Cruise detail map `NCDEMAPM` is displayed with message `Please enter Id to show cruise data` |
| 2 | Enter a known cruise ID (e.g., `10000001`) and press ENTER | `NCFINDCR` is called; map redisplays with populated fields |
| 3 | Verify Yacht Name field | Displays the yacht name from NCYACHT matching the cruise's `ID-YACHT` foreign key |
| 4 | Verify Start Date field | Displays in `YYYY-MM-DD` format (e.g., `2024-06-15`) |
| 5 | Verify End Date field | Displays in `YYYY-MM-DD` format |
| 6 | Verify Start Time field | Displays with `h` suffix (e.g., `100000 h`) |
| 7 | Verify End Time field | Displays with `h` suffix |
| 8 | Verify Start Harbor field | Displays the departure harbor name (up to 20 chars) |
| 9 | Verify Destination Harbor field | Displays the arrival harbor name |
| 10 | Verify Price 1W field | Displays in EUR format: `EUR  ZZZZ9.99` (e.g., `EUR  1000.00`) |
| 11 | Verify Price 2W field | Displays in EUR format |
| 12 | Verify Price 3W field | Displays in EUR format |
| 13 | Verify Status field | Displays the text mapping of the numeric status (e.g., `available`) |
| 14 | Verify confirmation message | Message `OK - Cruise shown for Id` is displayed |

**Pass Criteria:** All fields are populated correctly. Yacht name is resolved. Dates, prices, and status are properly formatted.

---

## TC-03: Cruise Lookup - Invalid ID

**Objective:** Verify that entering a non-existent cruise ID produces the appropriate error message.

**Preconditions:** Cruise ID `99999999` does not exist in the NCCRUISE file.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCINMAPP` | Cruise detail map is displayed |
| 2 | Enter `99999999` and press ENTER | `NCFINDCR` is called; `IF NO RECORDS FOUND` block executes; `RESET NC-PARMS` clears all parameter fields |
| 3 | Verify error message | Message `Sorry - No Cruise found for Id` is displayed via REINPUT |
| 4 | Verify all output fields | All cruise detail fields are empty/reset (because `RESET NC-PARMS` was executed) |
| 5 | Verify cursor position | Cursor is repositioned to the `#CR-ID-FIND` field (MARK statement) |

**Pass Criteria:** The error message is displayed. No data appears in the output fields. The cursor returns to the ID input field.

---

## TC-04: Cruise Lookup - Status Mapping

**Objective:** Verify that all cruise status values are correctly mapped to their text equivalents in `NCFINDCR`.

**Preconditions:** Test data includes cruise records with status values 0, 1, 2, 3, and an invalid status (e.g., 9).

| Status Value | Expected Text | Test Cruise ID |
|---|---|---|
| `0` | `removed` | Use a cruise with CRUISE-STATUS = '0' |
| `1` | `planned` | Use a cruise with CRUISE-STATUS = '1' |
| `2` | `available` | Use a cruise with CRUISE-STATUS = '2' |
| `3` | `sold` | Use a cruise with CRUISE-STATUS = '3' |
| `9` (or any other) | `unknown` | Use a cruise with CRUISE-STATUS = '9' |

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Look up cruise with status `0` | Status field shows `removed` |
| 2 | Look up cruise with status `1` | Status field shows `planned` |
| 3 | Look up cruise with status `2` | Status field shows `available` |
| 4 | Look up cruise with status `3` | Status field shows `sold` |
| 5 | Look up cruise with status `9` | Status field shows `unknown` |

**Pass Criteria:** Each status value maps to its correct text label per the `DECIDE ON FIRST VALUE` logic in `NCFINDCR`.

---

## TC-05: Cruise Lookup - Price Formatting

**Objective:** Verify that cruise prices are displayed in EUR currency format.

**Preconditions:** Test cruise has known price values.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Look up a cruise with PRICE-1W = 1000.000 | `#CR-P1W` displays `EUR  1000.00` |
| 2 | Verify PRICE-2W formatting | `#CR-P2W` displays in `EUR  ZZZZ9.99` format |
| 3 | Verify PRICE-3W formatting | `#CR-P3W` displays in `EUR  ZZZZ9.99` format |
| 4 | Look up a cruise with PRICE-1W = 0.000 | `#CR-P1W` displays `EUR      0.00` |
| 5 | Look up a cruise with large price (e.g., 99999.999) | `#CR-P1W` displays `EUR 99999.99` (truncated to edit mask width) |

**Pass Criteria:** All prices use the `*EUR' 'ZZZZ9.99` edit mask consistently.

**Source Reference:** `NCFINDCR.NSN` lines 31-33:
```
MOVE EDITED NCCRUISE.PRICE-1W  (EM=*EUR' 'ZZZZ9.99)  TO #CR-P1W
MOVE EDITED NCCRUISE.PRICE-2W  (EM=*EUR' 'ZZZZ9.99)  TO #CR-P2W
MOVE EDITED NCCRUISE.PRICE-3W  (EM=*EUR' 'ZZZZ9.99)  TO #CR-P3W
```

---

## TC-06: Cruise Lookup - Date Formatting

**Objective:** Verify that cruise dates are displayed in `YYYY-MM-DD` format.

**Preconditions:** Test cruise has known date values.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Look up a cruise with START-DATE = 20240615 | `#CR-SD` displays `2024-06-15` |
| 2 | Verify END-DATE formatting | `#CR-ED` displays in `YYYY-MM-DD` format |
| 3 | Look up a cruise with START-DATE = 20251231 | `#CR-SD` displays `2025-12-31` |

**Pass Criteria:** Dates use the `9999'-'99'-'99` edit mask producing `YYYY-MM-DD` output.

**Source Reference:** `NCFINDCR.NSN` lines 23, 25:
```
MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
MOVE EDITED NCCRUISE.END-DATE  (EM=9999'-'99'-'99)  TO #CR-ED
```

---

## TC-07: Batch Report NCATENDP

**Objective:** Verify the AT END OF PAGE / AT TOP OF PAGE report program produces correct paginated output.

**Preconditions:** Multiple cruise records exist in NCCRUISE (at least 15 to trigger page breaks with PS=15). Matching yacht records exist in NCYACHT.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCATENDP` | Report output begins |
| 2 | Verify page header (AT TOP OF PAGE) | First line shows `----- Page: 1` |
| 3 | Verify record lines | Each line shows: Yacht Name (15 chars), Start Date (YYYY-MM-DD), End Date (YYYY-MM-DD), Start Harbor (15 chars), Destination Harbor (15 chars), dash, line count |
| 4 | Verify page footer (AT END OF PAGE) | Footer shows `----- Cruise Records displayed: NNNNN` with the running record count |
| 5 | Verify pagination | Page size is 15 lines (FORMAT PS=15). If >15 records, page 2 header shows `----- Page: 2` |
| 6 | Verify record limit | Maximum 40 records are read (READ (40) NCCRUISE) |
| 7 | Verify yacht name resolution | Each record shows the yacht name from NCYACHT via `FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT` |

**Pass Criteria:** Report displays correctly paginated output with proper headers, footers, and up to 40 records with resolved yacht names.

---

## TC-08: Batch Report NCDEDISP

**Objective:** Verify the DISPLAY with edit masks report program produces correct tabular output.

**Preconditions:** Cruise and yacht records exist in the database.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCDEDISP` | Tabular report output is displayed |
| 2 | Verify column headers | Default DISPLAY headers: YACHT-NAME, START-DATE, START-HARBOR, END-DATE, DESTINATION-HARBOR, PRICE-1W |
| 3 | Verify date formatting | START-DATE and END-DATE use `9999'-'99'-'99` edit mask |
| 4 | Verify harbor column widths | START-HARBOR and DESTINATION-HARBOR limited to 10 characters (AL=10) |
| 5 | Verify price formatting | PRICE-1W displays with `*EUR' 'ZZZZ9.99` edit mask |
| 6 | Verify record limit | Maximum 100 records are read (READ (100) NCCRUISE) |
| 7 | Verify line size | FORMAT LS=100 limits line width to 100 characters |

**Pass Criteria:** All columns display with correct formatting and edit masks. Up to 100 records are shown.

---

## TC-09: Batch Report NCWRFORP

**Objective:** Verify the WRITE USING FORM program produces output using the `NCDEFORM` map with hardcoded sample data.

**Preconditions:** Map `NCDEFORM` is cataloged.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Execute `NCWRFORP` | Form output is rendered using `NCDEFORM` map |
| 2 | Verify Cruise ID | Shows `12345678` |
| 3 | Verify Status | Shows `available` |
| 4 | Verify Start Date | Shows `2015-08-20` |
| 5 | Verify Start Time | Shows `10 h` |
| 6 | Verify End Date | Shows `2015-08-01` |
| 7 | Verify End Time | Shows `7 h` |
| 8 | Verify Start Harbor | Shows `Samos` |
| 9 | Verify Destination Harbor | Shows `Santorini` |
| 10 | Verify Yacht Name | Shows `Cassandra` |
| 11 | Verify Price 1W | Shows `1000.00` |
| 12 | Verify Price 2W | Shows `2000.00` |
| 13 | Verify Price 3W | Shows `3000.00` |

**Pass Criteria:** All hardcoded values appear in the form output exactly as initialized in the program's LOCAL data.

**Source Reference:** `NCWRFORP.NSP` lines 14-27 define the hardcoded INIT values.

---

## TC-10: Error Handling

**Objective:** Verify that runtime errors in `NCINMAPP` trigger the ON ERROR block, perform a BACKOUT TRANSACTION, and capture/redisplay error details.

**Preconditions:** To provoke a runtime error, uncomment the division-by-zero line in `NCFINDCR.NSN`:
```natural
COMPUTE NCCRUISE.CRUISE-ID = NCCRUISE.CRUISE-ID / 0
```

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Modify `NCFINDCR` to include the deliberate error, recompile | Build succeeds |
| 2 | Execute `NCINMAPP` and enter a valid cruise ID | Runtime error occurs during FIND |
| 3 | Verify BACKOUT TRANSACTION | The ON ERROR block in `NCINMAPP` executes `BACKOUT TRANSACTION` — any pending database changes are rolled back |
| 4 | Verify error capture | `*ERROR-NR` is stored in `#IN-ERRNR`, `*PROGRAM` in `#IN-ERRPRG`, `*ERROR-LINE` in `#IN-ERRLINE` |
| 5 | Verify STACK behavior | Error data is pushed onto the stack via `STACK TOP DATA`, and `NCINMAPP` is re-invoked via `STACK TOP COMMAND` |
| 6 | Verify error message display | On re-entry, `*DATA > 0` is true; the error info is read from the stack and displayed as: `Error: {short-text} ({error-nr},{program},{line})` |
| 7 | Revert the deliberate error in `NCFINDCR` | Restore the comment on the division-by-zero line |

**Pass Criteria:** The error is caught, the transaction is backed out, and the user sees an informative error message with error number, program name, and line number.

---

## TC-11: Yacht Resolution

**Objective:** Verify that for each cruise record, the associated yacht name is correctly resolved from NCYACHT using the `ID-YACHT` foreign key.

**Preconditions:** Cruise records have valid `ID-YACHT` values that correspond to records in NCYACHT.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | Look up cruise with ID-YACHT = yacht_id_1 | `#CR-YACHT-NAME` shows the correct yacht name from NCYACHT |
| 2 | Look up a different cruise with ID-YACHT = yacht_id_2 | Different yacht name is displayed |
| 3 | Look up a cruise where ID-YACHT has no matching NCYACHT record | `#CR-YACHT-NAME` is empty (the inner FIND returns no records, so MOVE YACHT-NAME is never executed) |
| 4 | Run `NCATENDP` report | Each line shows the correct yacht name resolved via `FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT` |

**Pass Criteria:** Yacht names are correctly resolved in both interactive lookup and batch reports. Missing yacht records result in blank yacht name fields.

---

## TC-12: Invalid Function Key

**Objective:** Verify that pressing an unallocated PF key on any screen produces the `Function key not allocated` error message.

**Preconditions:** Application is running.

| Step | Action | Expected Result |
|------|--------|-----------------|
| 1 | On `NCMENUP` menu screen, press PF5 | Message `Sorry - Function key not allocated` displayed via REINPUT |
| 2 | On `NCMENUP` menu screen, press PF7 | Same error message |
| 3 | On `NCINMAPP` cruise detail screen, press PF5 | Message `Sorry - Function key not allocated` displayed |
| 4 | On `NCINMAPP` cruise detail screen, press PF8 | Same error message |
| 5 | Verify PF1 behavior | PF1 invokes HELP (SET KEY PF1=HELP) — should display help, not error |
| 6 | Verify PF3 behavior | PF3 terminates or stops — should not show error |

**Pass Criteria:** All unallocated PF keys trigger the NONE clause in the DECIDE statement, producing the appropriate REINPUT error message. Allocated keys (PF1=HELP, PF2=ON, PF3=ON) function normally.

**Source Reference:**
- `NCMENUP.NSP` line 34: `NONE REINPUT 'Sorry - Function key not allocated'`
- `NCINMAPP.NSP` line 86: `NONE  REINPUT 'Sorry - Function key not allocated'`

---

## Test Execution Summary Template

| Test Case | Description | Status | Tester | Date | Notes |
|---|---|---|---|---|---|
| TC-01 | Main Menu Navigation | | | | |
| TC-02 | Cruise Lookup - Valid ID | | | | |
| TC-03 | Cruise Lookup - Invalid ID | | | | |
| TC-04 | Cruise Lookup - Status Mapping | | | | |
| TC-05 | Cruise Lookup - Price Formatting | | | | |
| TC-06 | Cruise Lookup - Date Formatting | | | | |
| TC-07 | Batch Report NCATENDP | | | | |
| TC-08 | Batch Report NCDEDISP | | | | |
| TC-09 | Batch Report NCWRFORP | | | | |
| TC-10 | Error Handling | | | | |
| TC-11 | Yacht Resolution | | | | |
| TC-12 | Invalid Function Key | | | | |

---

## Traceability Matrix

| Test Case | Natural Programs | Natural Subprograms | Maps | DDMs | Data Areas |
|---|---|---|---|---|---|
| TC-01 | NCMENUP | - | NCMENUM | - | - |
| TC-02 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE, NCYACHT | NCDEMAPP |
| TC-03 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE | NCDEMAPP |
| TC-04 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE | NCDEMAPP |
| TC-05 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE | NCDEMAPP |
| TC-06 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE | NCDEMAPP |
| TC-07 | NCATENDP | - | - | NCCRUISE, NCYACHT | NCDEMAPL |
| TC-08 | NCDEDISP | - | - | NCCRUISE, NCYACHT | NCDEMAPL |
| TC-09 | NCWRFORP | - | NCDEFORM | - | - |
| TC-10 | NCINMAPP | NCFINDCR | NCDEMAPM | NCCRUISE | NCDEMAPP |
| TC-11 | NCINMAPP, NCATENDP | NCFINDCR | NCDEMAPM | NCCRUISE, NCYACHT | NCDEMAPP, NCDEMAPL |
| TC-12 | NCMENUP, NCINMAPP | - | NCMENUM, NCDEMAPM | - | - |
