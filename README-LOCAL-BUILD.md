# Local Build & Compilation Guide

This document provides step-by-step instructions for building and compiling the **NaturalCruise** application locally on a developer workstation.

---

## Prerequisites

Before you begin, ensure the following software is installed and configured:

| Prerequisite | Version / Notes |
|---|---|
| **Adabas Database Instance** | Adabas CE or Adabas LUW; DBID **012** must be available on your local machine or a reachable network host. |
| **Natural Runtime / Compiler** | Natural CE (Community Edition) or Natural for Linux/Windows with a Natural Development Server (NDV) listening on port **2700**. |
| **NaturalONE IDE** (optional) | Eclipse-based IDE from Software AG. Required only if you want to edit maps and data areas interactively. |
| **Apache Ant** | Version 1.9+ recommended. Used to execute the deployment scripts. |
| **Java JDK** | JDK 8 or later (required by Ant and the NaturalONE Ant task libraries). |
| **Git** | Any recent version for cloning the repository. |

---

## 1. Set Up the Local Adabas Database (DB 012)

The NaturalCruise application requires an Adabas database with **DBID 012** containing two files:

| Logical File | File Number | DDM Name | Description |
|---|---|---|---|
| NCCRUISE | **041** | `NCCRUISE` | Cruise records (ID, status, schedule, harbors, prices, yacht FK) |
| NCYACHT | **042** | `NCYACHT` | Yacht records (ID, name, type, dimensions) |

### 1.1 Using Docker (Recommended)

If you have Docker installed, the simplest approach is to run Adabas CE and Natural CE containers:

```bash
# Start Adabas CE with DBID 12
docker run -d --name adabas-db \
  -p 60001:60001 -p 8190:8190 \
  -e DBID=12 \
  store/softwareag/adabas-ce:latest

# Verify the database is running
docker exec adabas-db adainfo.sh 12
```

### 1.2 Create Adabas Files

Once the database is running, you need to create the two Adabas files using the FDT (File Definition Table) definitions that correspond to the DDMs.

**File 041 (NCCRUISE)** fields:
- `CI` CRUISE-ID (N8.0) - descriptor, unique
- `CK` CRUISE-STATUS (A1)
- `CL` CRUISE-START (group)
  - `CM` START-DATE (N8.0) - descriptor
  - `CN` START-TIME (N6.0)
- `CO` CRUISE-END (group)
  - `CP` END-DATE (N8.0) - descriptor
  - `CQ` END-TIME (N6.0)
- `CR` START-HARBOR (A20) - descriptor
- `CS` DESTINATION-HARBOR (A20) - descriptor
- `CT` ID-YACHT (N8.0) - descriptor
- `CW` PRICES (group)
  - `CX` PRICE-1W (P10.3)
  - `CY` PRICE-2W (P10.3)
  - `CZ` PRICE-3W (P10.3)

**File 042 (NCYACHT)** fields:
- `DB` YACHT-ID (N8.0) - descriptor, unique
- `DC` YACHT-NAME (A30) - descriptor
- `DD` YACHT-TYPE (A30) - descriptor
- `DF` LENGTH (P3.2)
- `DG` WIDTH (P3.2)
- `DH` DRAFT (P3.2)
- `DI` SAIL-SURFACE (P3.0)
- `DJ` MOTOR (P3.0)
- `DK` HEAD-ROOM (P3.2)
- `DL` BUNKS (P3.0)

### 1.3 Load DDM Definitions

The DDM source files are located in the repository:

```
NaturalCruise/Natural-Libraries/NTCRUISE/DDMs/NCCRUISE.NSD
NaturalCruise/Natural-Libraries/NTCRUISE/DDMs/NCYACHT.NSD
```

These DDMs map Natural field names to the Adabas short names (CI, CK, CM, etc.) and specify the database/file coordinates (`DB: 012 FILE: 041` and `DB: 012 FILE: 042`).

To load DDMs into the Natural server:
1. **Via NaturalONE IDE**: Import the project into Eclipse, then use the NaturalONE builder to upload DDMs to the NDV server.
2. **Via Ant build**: The deployment script automatically uploads DDMs as part of the `build` target (see Section 3 below).

---

## 2. Start the Natural Development Server

Ensure the Natural Development Server (NDV) is running and accessible:

```bash
# If using Docker:
docker run -d --name natural-ce \
  -p 2700:2700 \
  --link adabas-db:adabas-db \
  store/softwareag/natural-ce:latest

# Verify Natural is accessible
docker exec -it natural-ce bash
```

The NDV should be listening on **port 2700** (the default configured in the deployment scripts).

---

## 3. Run the Ant Build

### 3.1 Using the Local Deployment Descriptor

A local-specific deployment descriptor is provided for developer workstations:

```bash
ant -f NaturalCruise/natdeploy-local.xml build
```

This script is pre-configured with:
- `hostname = localhost`
- `port = 2700`
- `username = CI-LOCAL`
- `parm = NDVLOCAL`
- `rootdir = ${basedir}/workspace`

### 3.2 Using the Dev Deployment Descriptor

Alternatively, you can use the existing dev descriptor with overrides:

```bash
ant -f NaturalCruise/natdeploy-dev.xml build
```

### 3.3 Configuration Overrides

All properties can be overridden on the command line using `-D` flags. The most common overrides for local execution are:

| Property | Default (dev) | Local Override | Description |
|---|---|---|---|
| `natural.ant.server.hostname` | `127.0.0.1` | `localhost` | NDV server hostname |
| `natural.ant.server.port` | `2700` | `2700` | NDV server port |
| `natural.ant.server.username` | `CI-DEV` | `CI-LOCAL` | Server login username |
| `natural.ant.server.password` | (empty) | Your password | Server login password |
| `natural.ant.server.parameters` | `webio=on parm=NDVDEV` | `webio=on parm=NDVLOCAL` | NDV parameter string |
| `natural.ant.project.rootdir` | `/var/lib/jenkins/workspace/NatCIDev` | `/path/to/your/workspace` | Root directory containing the project |
| `natural.ant.deploy.full` | `NO` | `YES` (first time) | Full deploy on first build |
| `natural.ant.deploy.scope` | `3` | `3` | Scope: 1=changed, 2=+dependents, 3=all |

**Example with overrides:**

```bash
ant -f NaturalCruise/natdeploy-dev.xml build \
  -Dnatural.ant.server.hostname=localhost \
  -Dnatural.ant.server.username=CI-LOCAL \
  -Dnatural.ant.server.parameters="webio=on parm=NDVLOCAL" \
  -Dnatural.ant.project.rootdir=/home/myuser/workspace \
  -Dnatural.ant.deploy.full=YES
```

### 3.4 Available Ant Targets

| Target | Description |
|---|---|
| `build` | Compile and catalog all Natural objects on the NDV server (default target) |
| `checkout` | Initial clone/checkout from the configured VCS (Git, SVN, or CVS) |
| `update` | Incremental pull/update from VCS |
| `checkts` | Check for timestamp conflicts between local sources and server |
| `help` | Display all available targets and options |

---

## 4. Build Order

The Natural objects are compiled in a specific order defined by the NaturalONE build sequence:

```
D,G,L,A,4,M,8,3,S,N,7,H,P
```

This translates to:
1. **D** - DDMs (Data Definition Modules): `NCCRUISE.NSD`, `NCYACHT.NSD`
2. **G** - GDAs (Global Data Areas)
3. **L** - LDAs (Local Data Areas): `NCDEMAPL.NSL`
4. **A** - PDAs (Parameter Data Areas): `NCDEMAPP.NSA`
5. **M** - Maps: `NCMENUM.NSM`, `NCDEMAPM.NSM`, `NCDEFORM.NSM`
6. **S** - Subprograms: `NCFINDCR.NSN`
7. **H** - Help Routines: `NCDEMAPH.NSH`, `NCDECIDH.NSH`
8. **P** - Programs: `NCMENUP.NSP`, `NCINMAPP.NSP`, `NCATENDP.NSP`, etc.

The Ant `natantbuild` task handles this ordering automatically.

---

## 5. Verifying the Build

After a successful build:

1. **Check the Ant output** for `BUILD SUCCESSFUL` at the end.
2. **Connect to the NDV** using NaturalONE or a terminal emulator.
3. **Run the application** by executing `NCMENUP` in the NTCRUISE library.
4. **Test interactively**:
   - Option 1: Enter a cruise ID to look up cruise details
   - Option 2: View the cruise report listing
   - Option 3: Terminate the application

---

## 6. Troubleshooting

| Issue | Solution |
|---|---|
| `Connection refused on port 2700` | Ensure the NDV container/service is running and the port is not blocked by a firewall. |
| `ERROR: Root directory does not exist` | Set `-Dnatural.ant.project.rootdir` to an existing directory on your machine. |
| `BUILD FAILED - taskdef class not found` | Ensure the NaturalONE Ant plugin JARs are on the classpath, or run from within NaturalONE IDE. |
| `No DDM found for NCCRUISE` | The DDMs need to be uploaded first. Run with `-Dnatural.ant.deploy.full=YES`. |
| `Adabas response code 148` | The Adabas file does not exist. Create files 041 and 042 in database 012. |
| `Password required` | Set `-Dnatural.ant.server.password=yourpassword` or use the `natantcrypt` task to encrypt it. |

---

## 7. Project Structure Reference

```
NaturalCruise/
  natdeploy-dev.xml          # Ant script for dev environment (CI-DEV)
  natdeploy-test.xml         # Ant script for test environment (CI-TEST)
  natdeploy-prod.xml         # Ant script for prod environment (CI-PROD)
  natdeploy-local.xml        # Ant script for local developer workstation (CI-LOCAL)
  .natural                   # NaturalONE IDE configuration
  .project                   # Eclipse project descriptor
  Natural-Libraries/
    NTCRUISE/                 # The Natural library
      DDMs/                   # Data Definition Modules (.NSD)
      Programs/               # Programs (.NSP) - entry points
      Subprograms/            # Subprograms (.NSN) - reusable logic
      Maps/                   # Screen maps (.NSM) - UI definitions
      Parameter Data Areas/   # PDAs (.NSA) - inter-program data contracts
      Local Data Areas/       # LDAs (.NSL) - local data structures
      Helproutines/           # Help routines (.NSH)
      Resources/              # Version.txt, CruiseList.xml
```
