# Java Migration Playbook

## NaturalCruise: Natural/Adabas to Java/Spring Boot Migration

This playbook provides a detailed, actionable guide for migrating the NaturalCruise application from its current Natural/Adabas technology stack to a modern Java-based application using Spring Boot, JPA/Hibernate, and PostgreSQL.

---

## Table of Contents

1. [Architecture Overview](#section-1-architecture-overview)
2. [Database Migration](#section-2-database-migration)
3. [Java Project Structure](#section-3-java-project-structure)
4. [Business Logic Migration](#section-4-business-logic-migration)
5. [API Design](#section-5-api-design)
6. [Error Handling Migration](#section-6-error-handling-migration)
7. [Testing Strategy](#section-7-testing-strategy)
8. [CI/CD Pipeline Migration](#section-8-cicd-pipeline-migration)
9. [Migration Execution Plan](#section-9-migration-execution-plan)
10. [Validation Checklist](#section-10-validation-checklist)

---

## Section 1: Architecture Overview

### Current Architecture (Natural/Adabas)

```
┌─────────────────────────────────────────────────────────────────┐
│                    Character Terminal (3270/VT)                  │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐  │
│  │ NCMENUP  │───>│NCINMAPP  │───>│ NCFINDCR │───>│ NCCRUISE │  │
│  │ (Menu)   │    │(Input)   │    │(Subprog) │    │ (DDM/DB) │  │
│  └──────────┘    └──────────┘    └──────────┘    └──────────┘  │
│       │                               │               │        │
│       │          ┌──────────┐         │          ┌──────────┐  │
│       └─────────>│NCATENDP  │         └─────────>│ NCYACHT  │  │
│                  │(Report)  │                    │ (DDM/DB) │  │
│                  └──────────┘                    └──────────┘  │
│                                                                 │
│  Maps: NCMENUM, NCDEMAPM, NCDEFORM                             │
│  Data Areas: NCDEMAPP (PDA), NCDEMAPL (LDA)                    │
│                                                                 │
├─────────────────────────────────────────────────────────────────┤
│              Natural Runtime / NDV Server (port 2700)           │
├─────────────────────────────────────────────────────────────────┤
│              Adabas Database (DBID 012)                         │
│              File 041: NCCRUISE | File 042: NCYACHT             │
└─────────────────────────────────────────────────────────────────┘
```

### Target Architecture (Java/Spring Boot)

```
┌─────────────────────────────────────────────────────────────────┐
│           Web Browser / React SPA / Thymeleaf SSR               │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────┐    ┌──────────────────┐                  │
│  │ CruiseController │    │ ReportController │                  │
│  │  GET /api/cruises │    │ GET /api/reports │                  │
│  └────────┬─────────┘    └────────┬─────────┘                  │
│           │                       │                             │
│  ┌────────▼─────────┐    ┌───────▼──────────┐                  │
│  │  CruiseService   │    │  ReportService   │                  │
│  │  (Business Logic)│    │  (Report Logic)  │                  │
│  └────────┬─────────┘    └───────┬──────────┘                  │
│           │                       │                             │
│  ┌────────▼─────────┐    ┌───────▼──────────┐                  │
│  │CruiseRepository  │    │ YachtRepository  │                  │
│  │  (Spring Data)   │    │  (Spring Data)   │                  │
│  └────────┬─────────┘    └───────┬──────────┘                  │
│           │                       │                             │
├───────────▼───────────────────────▼─────────────────────────────┤
│              JPA / Hibernate ORM Layer                          │
├─────────────────────────────────────────────────────────────────┤
│              PostgreSQL Database                                │
│              Table: cruise | Table: yacht                        │
└─────────────────────────────────────────────────────────────────┘
```

### Component Mapping Table

| Natural Component | Type | Java Equivalent | Type |
|---|---|---|---|
| `NCMENUP` (main menu program) | Program (.NSP) | Spring MVC Controller or React Router | Controller / Component |
| `NCINMAPP` (data entry / cruise input) | Program (.NSP) | `CruiseController.getCruiseById()` | REST endpoint `GET /api/cruises/{id}` |
| `NCFINDCR` (cruise data retrieval) | Subprogram (.NSN) | `CruiseService.findCruiseById(Long id)` | Service method |
| `NCATENDP` (paginated report) | Program (.NSP) | `ReportService.generatePaginatedReport()` | Service + paginated REST endpoint |
| `NCDEDISP` (display with edit masks) | Program (.NSP) | `ReportService.generateDisplayReport()` | Service + REST endpoint |
| `NCSYSVP` (system variables demo) | Program (.NSP) | `ReportService.generateSystemReport()` | Service method |
| `NCWRFORP` (form output) | Program (.NSP) | `ReportService.generateFormReport()` | PDF/HTML via JasperReports or Thymeleaf |
| `NCCRUISE` DDM (Adabas file 041) | DDM (.NSD) | `Cruise.java` JPA Entity | `@Entity` |
| `NCYACHT` DDM (Adabas file 042) | DDM (.NSD) | `Yacht.java` JPA Entity | `@Entity` |
| `NCDEMAPP` (Parameter Data Area) | PDA (.NSA) | `CruiseDTO.java` | DTO / Record |
| `NCDEMAPL` (Local Data Area) | LDA (.NSL) | JPA entity field mappings | Embedded in entities |
| `NCMENUM` (main menu map) | Map (.NSM) | React component or Thymeleaf template | HTML/JSX |
| `NCDEMAPM` (cruise detail map) | Map (.NSM) | React component or Thymeleaf template | HTML/JSX |
| `NCDEFORM` (cruise info form) | Map (.NSM) | Thymeleaf template or JasperReports | HTML/JRXML |
| `natdeploy-dev.xml` (Ant CI/CD) | Ant XML | GitHub Actions workflow or Jenkinsfile | YAML/Groovy |
| `natdeploy-test.xml` (Ant CI/CD) | Ant XML | GitHub Actions workflow or Jenkinsfile | YAML/Groovy |
| `natdeploy-prod.xml` (Ant CI/CD) | Ant XML | GitHub Actions workflow or Jenkinsfile | YAML/Groovy |

---

## Section 2: Database Migration

### Adabas DDM to SQL Schema Mapping

#### NCCRUISE (Adabas File 041) → `cruise` Table

| Adabas Short Name | DDM Field | SQL Column | SQL Type | Constraints |
|---|---|---|---|---|
| CI | CRUISE-ID | `id` | `BIGINT` | PRIMARY KEY |
| CK | CRUISE-STATUS | `cruise_status` | `VARCHAR(1)` | NOT NULL |
| CM | START-DATE | `start_date` | `DATE` | NOT NULL |
| CN | START-TIME | `start_time` | `TIME` | |
| CP | END-DATE | `end_date` | `DATE` | NOT NULL |
| CQ | END-TIME | `end_time` | `TIME` | |
| CR | START-HARBOR | `start_harbor` | `VARCHAR(20)` | NOT NULL |
| CS | DESTINATION-HARBOR | `destination_harbor` | `VARCHAR(20)` | NOT NULL |
| CT | ID-YACHT | `yacht_id` | `BIGINT` | FOREIGN KEY → yacht(id) |
| CX | PRICE-1W | `price_1w` | `DECIMAL(10,3)` | |
| CY | PRICE-2W | `price_2w` | `DECIMAL(10,3)` | |
| CZ | PRICE-3W | `price_3w` | `DECIMAL(10,3)` | |

#### NCYACHT (Adabas File 042) → `yacht` Table

| Adabas Short Name | DDM Field | SQL Column | SQL Type | Constraints |
|---|---|---|---|---|
| DB | YACHT-ID | `id` | `BIGINT` | PRIMARY KEY |
| DC | YACHT-NAME | `yacht_name` | `VARCHAR(30)` | NOT NULL |
| DD | YACHT-TYPE | `yacht_type` | `VARCHAR(30)` | |
| DF | LENGTH | `length` | `DECIMAL(3,2)` | |
| DG | WIDTH | `width` | `DECIMAL(3,2)` | |
| DH | DRAFT | `draft` | `DECIMAL(3,2)` | |
| DI | SAIL-SURFACE | `sail_surface` | `DECIMAL(3,0)` | |
| DJ | MOTOR | `motor` | `DECIMAL(3,0)` | |
| DK | HEAD-ROOM | `head_room` | `DECIMAL(3,2)` | |
| DL | BUNKS | `bunks` | `DECIMAL(3,0)` | |

### SQL DDL Statements

```sql
-- Create yacht table (must be created first due to FK dependency)
CREATE TABLE yacht (
    id              BIGINT          PRIMARY KEY,
    yacht_name      VARCHAR(30)     NOT NULL,
    yacht_type      VARCHAR(30),
    length          DECIMAL(5,2),
    width           DECIMAL(5,2),
    draft           DECIMAL(5,2),
    sail_surface    DECIMAL(5,0),
    motor           DECIMAL(5,0),
    head_room       DECIMAL(5,2),
    bunks           DECIMAL(5,0)
);

-- Create cruise table with FK to yacht
CREATE TABLE cruise (
    id                  BIGINT          PRIMARY KEY,
    cruise_status       VARCHAR(1)      NOT NULL,
    start_date          DATE            NOT NULL,
    start_time          TIME,
    end_date            DATE            NOT NULL,
    end_time            TIME,
    start_harbor        VARCHAR(20)     NOT NULL,
    destination_harbor  VARCHAR(20)     NOT NULL,
    yacht_id            BIGINT          REFERENCES yacht(id),
    price_1w            DECIMAL(10,3),
    price_2w            DECIMAL(10,3),
    price_3w            DECIMAL(10,3)
);

-- Create indexes matching Adabas descriptors
CREATE INDEX idx_cruise_status ON cruise(cruise_status);
CREATE INDEX idx_cruise_start_date ON cruise(start_date);
CREATE INDEX idx_cruise_end_date ON cruise(end_date);
CREATE INDEX idx_cruise_start_harbor ON cruise(start_harbor);
CREATE INDEX idx_cruise_dest_harbor ON cruise(destination_harbor);
CREATE INDEX idx_cruise_yacht_id ON cruise(yacht_id);

CREATE INDEX idx_yacht_name ON yacht(yacht_name);
CREATE INDEX idx_yacht_type ON yacht(yacht_type);
```

### Flyway Migration Scripts

**`V1__create_yacht_table.sql`**

```sql
CREATE TABLE yacht (
    id              BIGINT          PRIMARY KEY,
    yacht_name      VARCHAR(30)     NOT NULL,
    yacht_type      VARCHAR(30),
    length          DECIMAL(5,2),
    width           DECIMAL(5,2),
    draft           DECIMAL(5,2),
    sail_surface    DECIMAL(5,0),
    motor           DECIMAL(5,0),
    head_room       DECIMAL(5,2),
    bunks           DECIMAL(5,0)
);

CREATE INDEX idx_yacht_name ON yacht(yacht_name);
CREATE INDEX idx_yacht_type ON yacht(yacht_type);
```

**`V2__create_cruise_table.sql`**

```sql
CREATE TABLE cruise (
    id                  BIGINT          PRIMARY KEY,
    cruise_status       VARCHAR(1)      NOT NULL,
    start_date          DATE            NOT NULL,
    start_time          TIME,
    end_date            DATE            NOT NULL,
    end_time            TIME,
    start_harbor        VARCHAR(20)     NOT NULL,
    destination_harbor  VARCHAR(20)     NOT NULL,
    yacht_id            BIGINT          REFERENCES yacht(id),
    price_1w            DECIMAL(10,3),
    price_2w            DECIMAL(10,3),
    price_3w            DECIMAL(10,3)
);

CREATE INDEX idx_cruise_status ON cruise(cruise_status);
CREATE INDEX idx_cruise_start_date ON cruise(start_date);
CREATE INDEX idx_cruise_end_date ON cruise(end_date);
CREATE INDEX idx_cruise_start_harbor ON cruise(start_harbor);
CREATE INDEX idx_cruise_dest_harbor ON cruise(destination_harbor);
CREATE INDEX idx_cruise_yacht_id ON cruise(yacht_id);
```

**`V3__insert_sample_data.sql`**

```sql
-- Sample yacht data
INSERT INTO yacht (id, yacht_name, yacht_type, length, width, draft, sail_surface, motor, head_room, bunks) VALUES
(1, 'Cassandra', 'Sailing Yacht', 15.50, 4.80, 2.10, 120, 50, 1.95, 8),
(2, 'Poseidon', 'Motor Yacht', 22.00, 6.20, 1.80, 0, 350, 2.10, 12),
(3, 'Athena', 'Catamaran', 12.80, 7.50, 1.20, 95, 40, 1.85, 6),
(4, 'Odysseus', 'Sailing Yacht', 18.30, 5.10, 2.50, 180, 75, 2.00, 10),
(5, 'Aphrodite', 'Motor Yacht', 28.00, 7.00, 2.00, 0, 500, 2.20, 16);

-- Sample cruise data
INSERT INTO cruise (id, cruise_status, start_date, start_time, end_date, end_time, start_harbor, destination_harbor, yacht_id, price_1w, price_2w, price_3w) VALUES
(10000001, '2', '2024-06-15', '10:00:00', '2024-06-22', '18:00:00', 'Piraeus', 'Santorini', 1, 1000.000, 1800.000, 2500.000),
(10000002, '1', '2024-07-01', '09:00:00', '2024-07-15', '17:00:00', 'Rhodes', 'Bodrum', 2, 2500.000, 4500.000, 6000.000),
(10000003, '3', '2024-05-01', '08:00:00', '2024-05-08', '16:00:00', 'Mykonos', 'Paros', 3, 800.000, 1400.000, 1900.000),
(10000004, '0', '2024-03-01', '07:00:00', '2024-03-08', '15:00:00', 'Corfu', 'Dubrovnik', 4, 1200.000, 2200.000, 3000.000);
```

### Data Migration Strategy

1. **Export from Adabas** using Natural UNLOAD utility:
   ```natural
   READ NCCRUISE
     WRITE NCCRUISE.CRUISE-ID ',' NCCRUISE.CRUISE-STATUS ',' ...
   END-READ
   ```
   Or use the Adabas ADAULD utility to export data in sequential format.

2. **Transform to CSV**: Convert the exported data to CSV format, applying these transformations:
   - Adabas N8.0 dates (e.g., `20240615`) → SQL DATE (`2024-06-15`)
   - Adabas N6.0 times (e.g., `100000`) → SQL TIME (`10:00:00`)
   - Adabas P10.3 packed decimal → SQL DECIMAL

3. **Import via SQL COPY**:
   ```sql
   COPY yacht FROM '/path/to/yacht.csv' WITH (FORMAT csv, HEADER true);
   COPY cruise FROM '/path/to/cruise.csv' WITH (FORMAT csv, HEADER true);
   ```

4. **Validate row counts** and spot-check key records against the Natural source.

---

## Section 3: Java Project Structure

### Recommended Maven Project Layout

```
cruise-application/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── cruise/
│   │   │           ├── CruiseApplication.java
│   │   │           ├── controller/
│   │   │           │   ├── CruiseController.java
│   │   │           │   └── ReportController.java
│   │   │           ├── service/
│   │   │           │   ├── CruiseService.java
│   │   │           │   └── ReportService.java
│   │   │           ├── repository/
│   │   │           │   ├── CruiseRepository.java
│   │   │           │   └── YachtRepository.java
│   │   │           ├── model/
│   │   │           │   ├── Cruise.java
│   │   │           │   ├── Yacht.java
│   │   │           │   └── CruiseStatus.java
│   │   │           ├── dto/
│   │   │           │   └── CruiseDTO.java
│   │   │           └── exception/
│   │   │               ├── CruiseNotFoundException.java
│   │   │               ├── DatabaseException.java
│   │   │               └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-test.yml
│   │       ├── application-prod.yml
│   │       ├── db/
│   │       │   └── migration/
│   │       │       ├── V1__create_yacht_table.sql
│   │       │       ├── V2__create_cruise_table.sql
│   │       │       └── V3__insert_sample_data.sql
│   │       └── templates/
│   │           └── cruise-form.html (Thymeleaf, replaces NCDEFORM)
│   └── test/
│       └── java/
│           └── com/
│               └── cruise/
│                   ├── CruiseApplicationTests.java
│                   ├── service/
│                   │   └── CruiseServiceTest.java
│                   └── controller/
│                       └── CruiseControllerIntegrationTest.java
├── Dockerfile
├── docker-compose.yml
└── Jenkinsfile (or .github/workflows/ci.yml)
```

### Maven `pom.xml` Dependencies

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
    </parent>

    <groupId>com.cruise</groupId>
    <artifactId>cruise-application</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <name>NaturalCruise Java Migration</name>
    <description>Java/Spring Boot migration of the NaturalCruise Adabas/Natural application</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-thymeleaf</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### `application.yml`

```yaml
spring:
  application:
    name: cruise-application

  datasource:
    url: jdbc:postgresql://localhost:5432/cruisedb
    username: ${DB_USERNAME:cruise_user}
    password: ${DB_PASSWORD:cruise_pass}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

  flyway:
    enabled: true
    locations: classpath:db/migration

server:
  port: 8080

logging:
  level:
    com.cruise: INFO
    org.hibernate.SQL: WARN
```

---

## Section 4: Business Logic Migration

### CruiseStatus Enum

Maps the Natural `DECIDE ON FIRST VALUE OF #CR-STATUS` logic from `NCFINDCR.NSN` lines 36-43.

**Natural source:**
```natural
DECIDE ON FIRST VALUE OF #CR-STATUS
  VALUE '0'  MOVE 'removed'    TO #CR-STATUS
  VALUE '1'  MOVE 'planned'    TO #CR-STATUS
  VALUE '2'  MOVE 'available'  TO #CR-STATUS
  VALUE '3'  MOVE 'sold'       TO #CR-STATUS
  NONE
    MOVE 'unknown' TO #CR-STATUS
END-DECIDE
```

**Java equivalent:**

```java
package com.cruise.model;

public enum CruiseStatus {
    REMOVED("0", "removed"),
    PLANNED("1", "planned"),
    AVAILABLE("2", "available"),
    SOLD("3", "sold");

    private final String code;
    private final String displayName;

    CruiseStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Maps a status code to its enum value.
     * Mirrors the DECIDE ON FIRST VALUE logic in NCFINDCR.NSN.
     * Unknown codes return null; callers should display "unknown".
     */
    public static CruiseStatus fromCode(String code) {
        for (CruiseStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null; // Maps to NONE -> "unknown"
    }

    /**
     * Returns the display name, or "unknown" if the status is null.
     * Preserves the exact behavior of the Natural NONE clause.
     */
    public static String toDisplayName(String code) {
        CruiseStatus status = fromCode(code);
        return status != null ? status.getDisplayName() : "unknown";
    }
}
```

### Cruise Entity

Maps `NCCRUISE` DDM (Adabas file 041, DBID 012).

```java
package com.cruise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "cruise")
public class Cruise {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "cruise_status", nullable = false, length = 1)
    private String cruiseStatus;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "start_harbor", nullable = false, length = 20)
    private String startHarbor;

    @Column(name = "destination_harbor", nullable = false, length = 20)
    private String destinationHarbor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "yacht_id")
    private Yacht yacht;

    @Column(name = "price_1w", precision = 10, scale = 3)
    private BigDecimal price1w;

    @Column(name = "price_2w", precision = 10, scale = 3)
    private BigDecimal price2w;

    @Column(name = "price_3w", precision = 10, scale = 3)
    private BigDecimal price3w;

    // Default constructor required by JPA
    public Cruise() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCruiseStatus() { return cruiseStatus; }
    public void setCruiseStatus(String cruiseStatus) { this.cruiseStatus = cruiseStatus; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getStartHarbor() { return startHarbor; }
    public void setStartHarbor(String startHarbor) { this.startHarbor = startHarbor; }

    public String getDestinationHarbor() { return destinationHarbor; }
    public void setDestinationHarbor(String destinationHarbor) { this.destinationHarbor = destinationHarbor; }

    public Yacht getYacht() { return yacht; }
    public void setYacht(Yacht yacht) { this.yacht = yacht; }

    public BigDecimal getPrice1w() { return price1w; }
    public void setPrice1w(BigDecimal price1w) { this.price1w = price1w; }

    public BigDecimal getPrice2w() { return price2w; }
    public void setPrice2w(BigDecimal price2w) { this.price2w = price2w; }

    public BigDecimal getPrice3w() { return price3w; }
    public void setPrice3w(BigDecimal price3w) { this.price3w = price3w; }
}
```

### Yacht Entity

Maps `NCYACHT` DDM (Adabas file 042, DBID 012).

```java
package com.cruise.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "yacht")
public class Yacht {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "yacht_name", nullable = false, length = 30)
    private String yachtName;

    @Column(name = "yacht_type", length = 30)
    private String yachtType;

    @Column(name = "length", precision = 5, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 5, scale = 2)
    private BigDecimal width;

    @Column(name = "draft", precision = 5, scale = 2)
    private BigDecimal draft;

    @Column(name = "sail_surface", precision = 5, scale = 0)
    private BigDecimal sailSurface;

    @Column(name = "motor", precision = 5, scale = 0)
    private BigDecimal motor;

    @Column(name = "head_room", precision = 5, scale = 2)
    private BigDecimal headRoom;

    @Column(name = "bunks", precision = 5, scale = 0)
    private BigDecimal bunks;

    public Yacht() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getYachtName() { return yachtName; }
    public void setYachtName(String yachtName) { this.yachtName = yachtName; }

    public String getYachtType() { return yachtType; }
    public void setYachtType(String yachtType) { this.yachtType = yachtType; }

    public BigDecimal getLength() { return length; }
    public void setLength(BigDecimal length) { this.length = length; }

    public BigDecimal getWidth() { return width; }
    public void setWidth(BigDecimal width) { this.width = width; }

    public BigDecimal getDraft() { return draft; }
    public void setDraft(BigDecimal draft) { this.draft = draft; }

    public BigDecimal getSailSurface() { return sailSurface; }
    public void setSailSurface(BigDecimal sailSurface) { this.sailSurface = sailSurface; }

    public BigDecimal getMotor() { return motor; }
    public void setMotor(BigDecimal motor) { this.motor = motor; }

    public BigDecimal getHeadRoom() { return headRoom; }
    public void setHeadRoom(BigDecimal headRoom) { this.headRoom = headRoom; }

    public BigDecimal getBunks() { return bunks; }
    public void setBunks(BigDecimal bunks) { this.bunks = bunks; }
}
```

### CruiseDTO

Maps `NCDEMAPP` Parameter Data Area (NC-PARMS). This is the data transfer object that mirrors exactly the fields passed between Natural programs via the PDA.

```java
package com.cruise.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Maps to NCDEMAPP.NSA (NC-PARMS parameter data area).
 * Each field corresponds to a PDA field used in CALLNAT 'NCFINDCR' NC-PARMS.
 *
 * Natural PDA field    -> Java DTO field
 * #CR-ID-FIND (N08.0)  -> searchId
 * #CR-ID (N08.0)       -> id
 * #CR-ED (A013)         -> endDate (formatted)
 * #CR-ET (A007)         -> endTime (formatted)
 * #CR-FROMH (A020)      -> startHarbor
 * #CR-P1W (A020)        -> price1w (formatted EUR string)
 * #CR-P2W (A020)        -> price2w (formatted EUR string)
 * #CR-P3W (A020)        -> price3w (formatted EUR string)
 * #CR-SD (A013)         -> startDate (formatted)
 * #CR-ST (A007)         -> startTime (formatted)
 * #CR-STATUS (A020)     -> statusText
 * #CR-TOH (A020)        -> destinationHarbor
 * #CR-YACHT-NAME (A020) -> yachtName
 */
public class CruiseDTO {

    private Long id;
    private String statusText;
    private String startDate;
    private String startTime;
    private String endDate;
    private String endTime;
    private String startHarbor;
    private String destinationHarbor;
    private String yachtName;
    private String price1w;
    private String price2w;
    private String price3w;

    public CruiseDTO() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getStartHarbor() { return startHarbor; }
    public void setStartHarbor(String startHarbor) { this.startHarbor = startHarbor; }

    public String getDestinationHarbor() { return destinationHarbor; }
    public void setDestinationHarbor(String destinationHarbor) { this.destinationHarbor = destinationHarbor; }

    public String getYachtName() { return yachtName; }
    public void setYachtName(String yachtName) { this.yachtName = yachtName; }

    public String getPrice1w() { return price1w; }
    public void setPrice1w(String price1w) { this.price1w = price1w; }

    public String getPrice2w() { return price2w; }
    public void setPrice2w(String price2w) { this.price2w = price2w; }

    public String getPrice3w() { return price3w; }
    public void setPrice3w(String price3w) { this.price3w = price3w; }
}
```

### CruiseRepository

```java
package com.cruise.repository;

import com.cruise.model.Cruise;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CruiseRepository extends JpaRepository<Cruise, Long> {

    /**
     * Mirrors: FIND NCCRUISE CRUISE-ID = #CR-ID-FIND (NCFINDCR.NSN line 14)
     * Spring Data derives this automatically from the method name.
     */
    // findById is inherited from JpaRepository

    /**
     * Mirrors: READ (40) NCCRUISE (NCATENDP.NSP line 36)
     * Returns a paginated list of cruises with eagerly fetched yacht data.
     */
    @Query("SELECT c FROM Cruise c LEFT JOIN FETCH c.yacht")
    Page<Cruise> findAllWithYacht(Pageable pageable);
}
```

### YachtRepository

```java
package com.cruise.repository;

import com.cruise.model.Yacht;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YachtRepository extends JpaRepository<Yacht, Long> {

    /**
     * Mirrors: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT (NCFINDCR.NSN line 46)
     * findById is inherited from JpaRepository.
     */
}
```

### CruiseService — NCFINDCR Migration

This is the core business logic migration. `CruiseService.findCruiseById()` replaces the entire `NCFINDCR.NSN` subprogram.

```java
package com.cruise.service;

import com.cruise.dto.CruiseDTO;
import com.cruise.exception.CruiseNotFoundException;
import com.cruise.model.Cruise;
import com.cruise.model.CruiseStatus;
import com.cruise.repository.CruiseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Replaces NCFINDCR.NSN subprogram.
 *
 * Natural NCFINDCR logic:
 *   1. FIND NCCRUISE CRUISE-ID = #CR-ID-FIND
 *   2. IF NO RECORDS FOUND -> RESET NC-PARMS, ESCAPE ROUTINE
 *   3. Move fields to NC-PARMS with formatting
 *   4. Map status code to text via DECIDE ON FIRST VALUE
 *   5. FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT -> Move YACHT-NAME
 *
 * All of the above is preserved exactly in this Java implementation.
 */
@Service
@Transactional(readOnly = true)
public class CruiseService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat EUR_FORMAT;

    static {
        // Mirrors: EM=*EUR' 'ZZZZ9.99 edit mask from NCFINDCR.NSN
        EUR_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        EUR_FORMAT.setMinimumFractionDigits(2);
        EUR_FORMAT.setMaximumFractionDigits(2);
    }

    private final CruiseRepository cruiseRepository;

    public CruiseService(CruiseRepository cruiseRepository) {
        this.cruiseRepository = cruiseRepository;
    }

    /**
     * Finds a cruise by ID and returns a formatted DTO.
     *
     * Mirrors NCFINDCR.NSN:
     *   FIND NCCRUISE CRUISE-ID = #CR-ID-FIND
     *     IF NO RECORDS FOUND -> RESET NC-PARMS; ESCAPE ROUTINE
     *     (move fields with formatting)
     *     FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT -> MOVE YACHT-NAME
     *   END-FIND
     *
     * @param id the cruise ID to search for (maps to #CR-ID-FIND)
     * @return CruiseDTO with all formatted fields (maps to NC-PARMS output)
     * @throws CruiseNotFoundException if no cruise is found (maps to IF NO RECORDS FOUND)
     */
    public CruiseDTO findCruiseById(Long id) {
        // FIND NCCRUISE CRUISE-ID = #CR-ID-FIND
        Cruise cruise = cruiseRepository.findById(id)
                .orElseThrow(() -> new CruiseNotFoundException(id));
        // IF NO RECORDS FOUND -> throws exception (equivalent to RESET NC-PARMS + ESCAPE ROUTINE)

        CruiseDTO dto = new CruiseDTO();

        // MOVE NCCRUISE.CRUISE-ID TO #CR-ID
        dto.setId(cruise.getId());

        // MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
        dto.setStartDate(formatDate(cruise.getStartDate()));

        // COMPRESS NCCRUISE.START-TIME 'h' INTO #CR-ST
        dto.setStartTime(formatTime(cruise.getStartTime()));

        // MOVE EDITED NCCRUISE.END-DATE (EM=9999'-'99'-'99) TO #CR-ED
        dto.setEndDate(formatDate(cruise.getEndDate()));

        // COMPRESS NCCRUISE.END-TIME 'h' INTO #CR-ET
        dto.setEndTime(formatTime(cruise.getEndTime()));

        // MOVE NCCRUISE.START-HARBOR TO #CR-FROMH
        dto.setStartHarbor(cruise.getStartHarbor());

        // MOVE NCCRUISE.DESTINATION-HARBOR TO #CR-TOH
        dto.setDestinationHarbor(cruise.getDestinationHarbor());

        // MOVE EDITED NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99) TO #CR-P1W
        dto.setPrice1w(formatPrice(cruise.getPrice1w()));
        dto.setPrice2w(formatPrice(cruise.getPrice2w()));
        dto.setPrice3w(formatPrice(cruise.getPrice3w()));

        // DECIDE ON FIRST VALUE OF #CR-STATUS -> value mapping
        dto.setStatusText(CruiseStatus.toDisplayName(cruise.getCruiseStatus()));

        // FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
        //   MOVE YACHT-NAME TO #CR-YACHT-NAME
        if (cruise.getYacht() != null) {
            dto.setYachtName(cruise.getYacht().getYachtName());
        } else {
            dto.setYachtName(""); // No matching yacht found
        }

        return dto;
    }

    /**
     * Formats a date as YYYY-MM-DD.
     * Mirrors: MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
     */
    private String formatDate(LocalDate date) {
        if (date == null) return "";
        return date.format(DATE_FORMATTER);
    }

    /**
     * Formats a time with 'h' suffix.
     * Mirrors: COMPRESS NCCRUISE.START-TIME 'h' INTO #CR-ST
     */
    private String formatTime(LocalTime time) {
        if (time == null) return "";
        return time.format(DateTimeFormatter.ofPattern("HHmmss")) + " h";
    }

    /**
     * Formats a price in EUR currency format.
     * Mirrors: MOVE EDITED NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99) TO #CR-P1W
     *
     * The Natural edit mask *EUR' 'ZZZZ9.99 produces output like: EUR  1000.00
     * The Java NumberFormat.getCurrencyInstance(Locale.GERMANY) produces similar output.
     */
    private String formatPrice(BigDecimal price) {
        if (price == null) return "";
        return EUR_FORMAT.format(price);
    }
}
```

### ReportService — NCATENDP / NCDEDISP / NCWRFORP Migration

```java
package com.cruise.service;

import com.cruise.dto.CruiseDTO;
import com.cruise.model.Cruise;
import com.cruise.model.CruiseStatus;
import com.cruise.repository.CruiseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Replaces NCATENDP.NSP, NCDEDISP.NSP, and NCWRFORP.NSP report programs.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final NumberFormat EUR_FORMAT;

    static {
        EUR_FORMAT = NumberFormat.getCurrencyInstance(Locale.GERMANY);
        EUR_FORMAT.setMinimumFractionDigits(2);
        EUR_FORMAT.setMaximumFractionDigits(2);
    }

    private final CruiseRepository cruiseRepository;

    public ReportService(CruiseRepository cruiseRepository) {
        this.cruiseRepository = cruiseRepository;
    }

    /**
     * Generates a paginated cruise report.
     *
     * Mirrors NCATENDP.NSP:
     *   FORMAT PS=15 LS=120
     *   READ (40) NCCRUISE
     *     FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
     *     WRITE YACHT-NAME START-DATE END-DATE START-HARBOR DESTINATION-HARBOR
     *   END-READ
     *
     * AT TOP OF PAGE -> page number header (handled by pagination metadata)
     * AT END OF PAGE -> record count footer (handled by Page.getTotalElements())
     *
     * @param limit maximum records (default 40, mirrors READ (40))
     * @return Page of CruiseDTO
     */
    public Page<CruiseDTO> generatePaginatedReport(int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Cruise> cruisePage = cruiseRepository.findAllWithYacht(pageable);
        return cruisePage.map(this::toCruiseDTO);
    }

    /**
     * Generates a display report with edit masks.
     *
     * Mirrors NCDEDISP.NSP:
     *   READ (100) NCCRUISE
     *     FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT
     *     DISPLAY YACHT-NAME START-DATE START-HARBOR END-DATE DESTINATION-HARBOR PRICE-1W
     *   END-READ
     *
     * @param limit maximum records (default 100, mirrors READ (100))
     * @return List of CruiseDTO
     */
    public List<CruiseDTO> generateDisplayReport(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<Cruise> cruisePage = cruiseRepository.findAllWithYacht(pageable);
        return cruisePage.getContent().stream()
                .map(this::toCruiseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generates a form report with hardcoded sample data.
     *
     * Mirrors NCWRFORP.NSP:
     *   Uses hardcoded LOCAL data with INIT values and WRITE USING FORM 'NCDEFORM'
     *
     * In the Java version, this returns a DTO with the same hardcoded values
     * that can be rendered by a Thymeleaf template or JasperReports.
     */
    public CruiseDTO generateFormReport() {
        CruiseDTO dto = new CruiseDTO();
        // Exact values from NCWRFORP.NSP INIT clauses (lines 15-26)
        dto.setId(12345678L);
        dto.setStatusText("available");
        dto.setStartDate("2015-08-20");
        dto.setStartTime("10 h");
        dto.setEndDate("2015-08-01");
        dto.setEndTime("7 h");
        dto.setStartHarbor("Samos");
        dto.setDestinationHarbor("Santorini");
        dto.setYachtName("Cassandra");
        dto.setPrice1w("1000.00");
        dto.setPrice2w("2000.00");
        dto.setPrice3w("3000.00");
        return dto;
    }

    private CruiseDTO toCruiseDTO(Cruise cruise) {
        CruiseDTO dto = new CruiseDTO();
        dto.setId(cruise.getId());
        dto.setStatusText(CruiseStatus.toDisplayName(cruise.getCruiseStatus()));
        dto.setStartDate(cruise.getStartDate() != null
                ? cruise.getStartDate().format(DATE_FORMATTER) : "");
        dto.setEndDate(cruise.getEndDate() != null
                ? cruise.getEndDate().format(DATE_FORMATTER) : "");
        dto.setStartHarbor(cruise.getStartHarbor());
        dto.setDestinationHarbor(cruise.getDestinationHarbor());
        dto.setPrice1w(cruise.getPrice1w() != null
                ? EUR_FORMAT.format(cruise.getPrice1w()) : "");
        dto.setPrice2w(cruise.getPrice2w() != null
                ? EUR_FORMAT.format(cruise.getPrice2w()) : "");
        dto.setPrice3w(cruise.getPrice3w() != null
                ? EUR_FORMAT.format(cruise.getPrice3w()) : "");
        if (cruise.getYacht() != null) {
            dto.setYachtName(cruise.getYacht().getYachtName());
        } else {
            dto.setYachtName("");
        }
        return dto;
    }
}
```

---

## Section 5: API Design

### REST API Specification

| Endpoint | Method | Description | Natural Equivalent |
|---|---|---|---|
| `/api/cruises/{id}` | GET | Find cruise by ID | `NCINMAPP` + `NCFINDCR` |
| `/api/cruises?page=0&size=40` | GET | Paginated cruise list | `NCATENDP` (READ (40)) |
| `/api/cruises/report?format=pdf` | GET | Generate downloadable report | `NCDEDISP` |
| `/api/cruises/display?limit=100` | GET | Display report as JSON | `NCDEDISP` (READ (100)) |
| `/api/cruises/form` | GET | Form report with sample data | `NCWRFORP` |

### CruiseController

```java
package com.cruise.controller;

import com.cruise.dto.CruiseDTO;
import com.cruise.service.CruiseService;
import com.cruise.service.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cruises")
public class CruiseController {

    private final CruiseService cruiseService;
    private final ReportService reportService;

    public CruiseController(CruiseService cruiseService, ReportService reportService) {
        this.cruiseService = cruiseService;
        this.reportService = reportService;
    }

    /**
     * GET /api/cruises/{id}
     *
     * Mirrors: NCINMAPP.NSP + NCFINDCR.NSN
     *   User enters cruise ID -> CALLNAT 'NCFINDCR' NC-PARMS -> display result
     */
    @GetMapping("/{id}")
    public ResponseEntity<CruiseDTO> getCruiseById(@PathVariable Long id) {
        CruiseDTO cruise = cruiseService.findCruiseById(id);
        return ResponseEntity.ok(cruise);
    }

    /**
     * GET /api/cruises?page=0&size=40
     *
     * Mirrors: NCATENDP.NSP
     *   READ (40) NCCRUISE with AT TOP OF PAGE / AT END OF PAGE
     */
    @GetMapping
    public ResponseEntity<Page<CruiseDTO>> listCruises(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "40") int size) {
        Page<CruiseDTO> cruises = reportService.generatePaginatedReport(page, size);
        return ResponseEntity.ok(cruises);
    }

    /**
     * GET /api/cruises/display?limit=100
     *
     * Mirrors: NCDEDISP.NSP
     *   READ (100) NCCRUISE with DISPLAY and edit masks
     */
    @GetMapping("/display")
    public ResponseEntity<List<CruiseDTO>> displayReport(
            @RequestParam(defaultValue = "100") int limit) {
        List<CruiseDTO> cruises = reportService.generateDisplayReport(limit);
        return ResponseEntity.ok(cruises);
    }

    /**
     * GET /api/cruises/form
     *
     * Mirrors: NCWRFORP.NSP
     *   WRITE USING FORM 'NCDEFORM' with hardcoded sample data
     */
    @GetMapping("/form")
    public ResponseEntity<CruiseDTO> formReport() {
        CruiseDTO formData = reportService.generateFormReport();
        return ResponseEntity.ok(formData);
    }
}
```

### Sample Request/Response Payloads

#### `GET /api/cruises/10000001`

**Response (200 OK):**
```json
{
    "id": 10000001,
    "statusText": "available",
    "startDate": "2024-06-15",
    "startTime": "100000 h",
    "endDate": "2024-06-22",
    "endTime": "180000 h",
    "startHarbor": "Piraeus",
    "destinationHarbor": "Santorini",
    "yachtName": "Cassandra",
    "price1w": "EUR 1.000,00",
    "price2w": "EUR 1.800,00",
    "price3w": "EUR 2.500,00"
}
```

#### `GET /api/cruises/99999999`

**Response (404 Not Found):**
```json
{
    "error": "CruiseNotFoundException",
    "message": "No Cruise found for Id 99999999",
    "timestamp": "2024-06-15T10:30:00Z"
}
```

#### `GET /api/cruises?page=0&size=40`

**Response (200 OK):**
```json
{
    "content": [
        {
            "id": 10000001,
            "statusText": "available",
            "startDate": "2024-06-15",
            "startTime": "100000 h",
            "endDate": "2024-06-22",
            "endTime": "180000 h",
            "startHarbor": "Piraeus",
            "destinationHarbor": "Santorini",
            "yachtName": "Cassandra",
            "price1w": "EUR 1.000,00",
            "price2w": "EUR 1.800,00",
            "price3w": "EUR 2.500,00"
        }
    ],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 40
    },
    "totalElements": 8,
    "totalPages": 1,
    "last": true,
    "first": true
}
```

#### `GET /api/cruises/form`

**Response (200 OK):**
```json
{
    "id": 12345678,
    "statusText": "available",
    "startDate": "2015-08-20",
    "startTime": "10 h",
    "endDate": "2015-08-01",
    "endTime": "7 h",
    "startHarbor": "Samos",
    "destinationHarbor": "Santorini",
    "yachtName": "Cassandra",
    "price1w": "1000.00",
    "price2w": "2000.00",
    "price3w": "3000.00"
}
```

---

## Section 6: Error Handling Migration

### Natural Error Handling (NCINMAPP.NSP lines 60-69)

```natural
ON ERROR
  BACKOUT TRANSACTION
  MOVE *ERROR-NR   TO #IN-ERRNR
  MOVE *PROGRAM    TO #IN-ERRPRG
  MOVE *ERROR-LINE TO #IN-ERRLINE
  STACK TOP DATA #IN-ERRNR #IN-ERRPRG #IN-ERRLINE
  STACK TOP COMMAND 'NCINMAPP'
  STOP
ESCAPE ROUTINE
END-ERROR
```

### Java Error Handling Equivalents

#### Custom Exceptions

```java
package com.cruise.exception;

/**
 * Maps to: IF NO RECORDS FOUND -> RESET NC-PARMS; ESCAPE ROUTINE
 * and: REINPUT 'Sorry - No Cruise found for Id'
 */
public class CruiseNotFoundException extends RuntimeException {

    private final Long cruiseId;

    public CruiseNotFoundException(Long cruiseId) {
        super("No Cruise found for Id " + cruiseId);
        this.cruiseId = cruiseId;
    }

    public Long getCruiseId() {
        return cruiseId;
    }
}
```

```java
package com.cruise.exception;

/**
 * Maps to: ON ERROR block in NCINMAPP.NSP
 * Captures error number, program, and line (Natural system variables).
 */
public class DatabaseException extends RuntimeException {

    private final int errorNumber;    // Maps to *ERROR-NR
    private final String program;     // Maps to *PROGRAM
    private final int errorLine;      // Maps to *ERROR-LINE

    public DatabaseException(String message, Throwable cause,
                             int errorNumber, String program, int errorLine) {
        super(message, cause);
        this.errorNumber = errorNumber;
        this.program = program;
        this.errorLine = errorLine;
    }

    public int getErrorNumber() { return errorNumber; }
    public String getProgram() { return program; }
    public int getErrorLine() { return errorLine; }
}
```

#### Global Exception Handler

```java
package com.cruise.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps to the ON ERROR block in NCINMAPP.NSP.
 *
 * Natural behavior:
 *   ON ERROR -> BACKOUT TRANSACTION -> capture error details -> redisplay
 *
 * Spring equivalent:
 *   @ControllerAdvice -> catches exceptions -> @Transactional provides
 *   automatic rollback (BACKOUT TRANSACTION) -> returns error response
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles CruiseNotFoundException.
     *
     * Maps to: REINPUT 'Sorry - No Cruise found for Id' MARK *#CR-ID-FIND
     */
    @ExceptionHandler(CruiseNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCruiseNotFound(
            CruiseNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "CruiseNotFoundException");
        body.put("message", ex.getMessage());
        body.put("cruiseId", ex.getCruiseId());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /**
     * Handles DatabaseException.
     *
     * Maps to the ON ERROR block:
     *   BACKOUT TRANSACTION -> @Transactional handles rollback
     *   MOVE *ERROR-NR TO #IN-ERRNR -> ex.getErrorNumber()
     *   MOVE *PROGRAM TO #IN-ERRPRG -> ex.getProgram()
     *   MOVE *ERROR-LINE TO #IN-ERRLINE -> ex.getErrorLine()
     *   COMPRESS error info INTO #INFO-MESSAGE -> response body
     */
    @ExceptionHandler(DatabaseException.class)
    public ResponseEntity<Map<String, Object>> handleDatabaseError(
            DatabaseException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "DatabaseException");
        body.put("message", ex.getMessage());
        body.put("errorNumber", ex.getErrorNumber());
        body.put("program", ex.getProgram());
        body.put("errorLine", ex.getErrorLine());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    /**
     * Catch-all for unexpected exceptions.
     *
     * Maps to the general ON ERROR behavior where any unhandled Natural
     * error triggers BACKOUT TRANSACTION and error reporting.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", ex.getClass().getSimpleName());
        body.put("message", ex.getMessage());
        body.put("timestamp", Instant.now().toString());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
```

### Transaction Mapping

| Natural Concept | Java/Spring Equivalent |
|---|---|
| `BACKOUT TRANSACTION` | `@Transactional` with automatic rollback on exception |
| `END OF TRANSACTION` | `@Transactional` commit on successful method return |
| `ON ERROR ... END-ERROR` | `@ControllerAdvice` + `@ExceptionHandler` |
| `*ERROR-NR` system variable | `Exception.getMessage()` or custom error codes |
| `*PROGRAM` system variable | Stack trace / `Thread.currentThread().getStackTrace()` |
| `*ERROR-LINE` system variable | Stack trace line numbers |
| `STACK TOP DATA / COMMAND` | HTTP redirect with error parameters or session attributes |

---

## Section 7: Testing Strategy

### Test Framework Stack

| Component | Technology |
|---|---|
| Unit Tests | JUnit 5 + Mockito |
| Integration Tests | `@SpringBootTest` + H2 in-memory database |
| API Tests | `MockMvc` or `WebTestClient` |
| Test Data | Flyway test migrations + `@Sql` annotations |

### Natural Test Case → Java Test Method Mapping

| Natural TC | Description | Java Test Class | Java Test Method |
|---|---|---|---|
| TC-01 | Main Menu Navigation | `CruiseControllerIntegrationTest` | N/A (UI navigation; covered by frontend tests) |
| TC-02 | Cruise Lookup - Valid ID | `CruiseServiceTest` | `testFindCruiseById_ValidId_ReturnsPopulatedDTO()` |
| TC-03 | Cruise Lookup - Invalid ID | `CruiseServiceTest` | `testFindCruiseById_InvalidId_ThrowsNotFoundException()` |
| TC-04 | Status Mapping | `CruiseServiceTest` | `testStatusMapping_AllValues()` |
| TC-05 | Price Formatting | `CruiseServiceTest` | `testPriceFormatting_EurFormat()` |
| TC-06 | Date Formatting | `CruiseServiceTest` | `testDateFormatting_YyyyMmDd()` |
| TC-07 | Batch Report NCATENDP | `ReportServiceTest` | `testPaginatedReport_MaxRecords()` |
| TC-08 | Batch Report NCDEDISP | `ReportServiceTest` | `testDisplayReport_MaxRecords()` |
| TC-09 | Form Report NCWRFORP | `ReportServiceTest` | `testFormReport_HardcodedValues()` |
| TC-10 | Error Handling | `CruiseControllerIntegrationTest` | `testErrorHandling_ReturnsErrorDetails()` |
| TC-11 | Yacht Resolution | `CruiseServiceTest` | `testYachtResolution_ValidYacht()` and `testYachtResolution_MissingYacht()` |
| TC-12 | Invalid Function Key | N/A (terminal UI concept; no REST equivalent) | N/A |

### CruiseServiceTest — Sample Code

```java
package com.cruise.service;

import com.cruise.dto.CruiseDTO;
import com.cruise.exception.CruiseNotFoundException;
import com.cruise.model.Cruise;
import com.cruise.model.CruiseStatus;
import com.cruise.model.Yacht;
import com.cruise.repository.CruiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CruiseServiceTest {

    @Mock
    private CruiseRepository cruiseRepository;

    @InjectMocks
    private CruiseService cruiseService;

    private Cruise sampleCruise;
    private Yacht sampleYacht;

    @BeforeEach
    void setUp() {
        sampleYacht = new Yacht();
        sampleYacht.setId(1L);
        sampleYacht.setYachtName("Cassandra");

        sampleCruise = new Cruise();
        sampleCruise.setId(10000001L);
        sampleCruise.setCruiseStatus("2");
        sampleCruise.setStartDate(LocalDate.of(2024, 6, 15));
        sampleCruise.setStartTime(LocalTime.of(10, 0, 0));
        sampleCruise.setEndDate(LocalDate.of(2024, 6, 22));
        sampleCruise.setEndTime(LocalTime.of(18, 0, 0));
        sampleCruise.setStartHarbor("Piraeus");
        sampleCruise.setDestinationHarbor("Santorini");
        sampleCruise.setYacht(sampleYacht);
        sampleCruise.setPrice1w(new BigDecimal("1000.000"));
        sampleCruise.setPrice2w(new BigDecimal("1800.000"));
        sampleCruise.setPrice3w(new BigDecimal("2500.000"));
    }

    /**
     * TC-02: Valid cruise lookup.
     * Mirrors: FIND NCCRUISE CRUISE-ID = #CR-ID-FIND -> fields populated
     */
    @Test
    void testFindCruiseById_ValidId_ReturnsPopulatedDTO() {
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        assertNotNull(dto);
        assertEquals(10000001L, dto.getId());
        assertNotNull(dto.getStatusText());
        assertNotNull(dto.getStartDate());
        assertNotNull(dto.getEndDate());
        assertNotNull(dto.getStartHarbor());
        assertNotNull(dto.getDestinationHarbor());
        assertNotNull(dto.getPrice1w());
        assertNotNull(dto.getYachtName());
        assertFalse(dto.getStatusText().isEmpty());
        assertFalse(dto.getStartDate().isEmpty());
        assertFalse(dto.getYachtName().isEmpty());
    }

    /**
     * TC-03: Invalid cruise lookup.
     * Mirrors: IF NO RECORDS FOUND -> RESET NC-PARMS; ESCAPE ROUTINE
     */
    @Test
    void testFindCruiseById_InvalidId_ThrowsNotFoundException() {
        when(cruiseRepository.findById(99999999L)).thenReturn(Optional.empty());

        CruiseNotFoundException exception = assertThrows(
                CruiseNotFoundException.class,
                () -> cruiseService.findCruiseById(99999999L)
        );

        assertEquals(99999999L, exception.getCruiseId());
        assertTrue(exception.getMessage().contains("99999999"));
    }

    /**
     * TC-04: Status mapping for all values.
     * Mirrors: DECIDE ON FIRST VALUE OF #CR-STATUS in NCFINDCR.NSN
     */
    @ParameterizedTest
    @CsvSource({
            "0, removed",
            "1, planned",
            "2, available",
            "3, sold",
            "9, unknown",
            "X, unknown"
    })
    void testStatusMapping_AllValues(String statusCode, String expectedText) {
        sampleCruise.setCruiseStatus(statusCode);
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        assertEquals(expectedText, dto.getStatusText());
    }

    /**
     * TC-05: Price formatting in EUR.
     * Mirrors: MOVE EDITED NCCRUISE.PRICE-1W (EM=*EUR' 'ZZZZ9.99) TO #CR-P1W
     */
    @Test
    void testPriceFormatting_EurFormat() {
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        // Price should contain EUR currency formatting
        assertNotNull(dto.getPrice1w());
        assertFalse(dto.getPrice1w().isEmpty());
        // The exact format depends on locale, but should contain the numeric value
        assertTrue(dto.getPrice1w().contains("1.000") || dto.getPrice1w().contains("1,000")
                || dto.getPrice1w().contains("1000"));
    }

    /**
     * TC-06: Date formatting as YYYY-MM-DD.
     * Mirrors: MOVE EDITED NCCRUISE.START-DATE (EM=9999'-'99'-'99) TO #CR-SD
     */
    @Test
    void testDateFormatting_YyyyMmDd() {
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        assertEquals("2024-06-15", dto.getStartDate());
        assertEquals("2024-06-22", dto.getEndDate());
    }

    /**
     * TC-11a: Yacht resolution with valid yacht.
     * Mirrors: FIND NCYACHT YACHT-ID = NCCRUISE.ID-YACHT -> MOVE YACHT-NAME
     */
    @Test
    void testYachtResolution_ValidYacht() {
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        assertEquals("Cassandra", dto.getYachtName());
    }

    /**
     * TC-11b: Yacht resolution with missing yacht.
     * Mirrors: FIND NCYACHT with no matching record -> yacht name stays empty
     */
    @Test
    void testYachtResolution_MissingYacht() {
        sampleCruise.setYacht(null); // No matching yacht
        when(cruiseRepository.findById(10000001L)).thenReturn(Optional.of(sampleCruise));

        CruiseDTO dto = cruiseService.findCruiseById(10000001L);

        assertEquals("", dto.getYachtName());
    }
}
```

### CruiseControllerIntegrationTest — Sample Code

```java
package com.cruise.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests using H2 in-memory database.
 * Test data is loaded via Flyway migrations (V3__insert_sample_data.sql).
 */
@SpringBootTest
@AutoConfigureMockMvc
class CruiseControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * TC-02: Valid cruise lookup via REST API.
     */
    @Test
    void testGetCruiseById_ValidId() throws Exception {
        mockMvc.perform(get("/api/cruises/10000001")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10000001))
                .andExpect(jsonPath("$.statusText").value("available"))
                .andExpect(jsonPath("$.startDate").value("2024-06-15"))
                .andExpect(jsonPath("$.endDate").value("2024-06-22"))
                .andExpect(jsonPath("$.startHarbor").value("Piraeus"))
                .andExpect(jsonPath("$.destinationHarbor").value("Santorini"))
                .andExpect(jsonPath("$.yachtName").value("Cassandra"))
                .andExpect(jsonPath("$.price1w").isNotEmpty());
    }

    /**
     * TC-03: Invalid cruise lookup via REST API.
     */
    @Test
    void testGetCruiseById_InvalidId() throws Exception {
        mockMvc.perform(get("/api/cruises/99999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("CruiseNotFoundException"))
                .andExpect(jsonPath("$.message").value("No Cruise found for Id 99999999"));
    }

    /**
     * TC-07: Paginated cruise list.
     */
    @Test
    void testListCruises_Paginated() throws Exception {
        mockMvc.perform(get("/api/cruises?page=0&size=40")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber())
                .andExpect(jsonPath("$.pageable.pageSize").value(40));
    }

    /**
     * TC-10: Error handling returns proper error response.
     */
    @Test
    void testErrorHandling_ReturnsErrorDetails() throws Exception {
        // Requesting a non-existent cruise should return structured error
        mockMvc.perform(get("/api/cruises/0")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }
}
```

---

## Section 8: CI/CD Pipeline Migration

### Ant Script → CI/CD Mapping

| Ant Script | Environment | CI/CD Equivalent | Key Differences |
|---|---|---|---|
| `natdeploy-dev.xml` | Development | `dev` workflow / stage | Build + deploy to dev server |
| `natdeploy-test.xml` | Test | `test` workflow / stage | Build + deploy to test server |
| `natdeploy-prod.xml` | Production | `prod` workflow / stage | Build + deploy to prod, with cleanup of removed sources (`deploy.delete=YES`) |

### Ant Target → CI/CD Stage Mapping

| Ant Target | CI/CD Stage | Description |
|---|---|---|
| `checkout` | Git checkout step | Clone/checkout the repository |
| `update` | Git pull step | Incremental update from VCS |
| `build` | Build + Test + Deploy | Compile, test, package, deploy |
| `checkts` | N/A | Timestamp conflicts do not apply to Java builds |

### GitHub Actions Workflow

**`.github/workflows/ci.yml`**

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  JAVA_VERSION: '17'
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # ============================================================
  # Build & Test (replaces natdeploy-dev.xml 'build' target)
  # ============================================================
  build:
    name: Build & Test
    runs-on: ubuntu-latest

    services:
      postgres:
        image: postgres:15
        env:
          POSTGRES_DB: cruisedb_test
          POSTGRES_USER: cruise_user
          POSTGRES_PASSWORD: cruise_pass
        ports:
          - 5432:5432
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Set up JDK ${{ env.JAVA_VERSION }}
        uses: actions/setup-java@v4
        with:
          java-version: ${{ env.JAVA_VERSION }}
          distribution: 'temurin'
          cache: maven

      - name: Run unit tests
        run: mvn test -B

      - name: Run integration tests
        run: mvn verify -B -P integration-test
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/cruisedb_test
          SPRING_DATASOURCE_USERNAME: cruise_user
          SPRING_DATASOURCE_PASSWORD: cruise_pass

      - name: Build package
        run: mvn package -B -DskipTests

      - name: Upload artifact
        uses: actions/upload-artifact@v4
        with:
          name: cruise-application
          path: target/*.jar

  # ============================================================
  # Deploy to Dev (replaces natdeploy-dev.xml)
  # ============================================================
  deploy-dev:
    name: Deploy to Dev
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/develop'
    environment: development

    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: cruise-application

      - name: Deploy to Dev server
        run: |
          echo "Deploying to development environment..."
          # Replace with actual deployment command
          # e.g., scp, kubectl apply, docker push, etc.

  # ============================================================
  # Deploy to Test (replaces natdeploy-test.xml)
  # ============================================================
  deploy-test:
    name: Deploy to Test
    needs: deploy-dev
    runs-on: ubuntu-latest
    environment: test

    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: cruise-application

      - name: Deploy to Test server
        run: |
          echo "Deploying to test environment..."
          # Replace with actual deployment command

  # ============================================================
  # Deploy to Prod (replaces natdeploy-prod.xml)
  # Note: natdeploy-prod.xml has deploy.delete=YES for cleanup
  # ============================================================
  deploy-prod:
    name: Deploy to Production
    needs: deploy-test
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    environment:
      name: production
      # Require manual approval for production deployments

    steps:
      - name: Download artifact
        uses: actions/download-artifact@v4
        with:
          name: cruise-application

      - name: Deploy to Production server
        run: |
          echo "Deploying to production environment..."
          # Replace with actual deployment command
          # Note: In prod, removed sources are cleaned up
          # (equivalent to natdeploy-prod.xml deploy.delete=YES)
```

### Jenkinsfile (Alternative)

```groovy
pipeline {
    agent any

    tools {
        jdk 'JDK-17'
        maven 'Maven-3.9'
    }

    environment {
        DB_URL = credentials('cruise-db-url')
        DB_USER = credentials('cruise-db-username')
        DB_PASS = credentials('cruise-db-password')
    }

    stages {
        // Replaces: natdeploy-*.xml 'checkout' target
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // Replaces: natdeploy-*.xml 'build' target (compile + catalog)
        stage('Build & Test') {
            steps {
                sh 'mvn clean verify -B'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    junit '**/target/failsafe-reports/*.xml'
                }
            }
        }

        stage('Package') {
            steps {
                sh 'mvn package -B -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        // Replaces: natdeploy-dev.xml (CI-DEV, NDVDEV, NatCIDev workspace)
        stage('Deploy to Dev') {
            when { branch 'develop' }
            steps {
                echo 'Deploying to Development environment...'
                // sh 'deploy.sh dev'
            }
        }

        // Replaces: natdeploy-test.xml (CI-TEST, NDVTEST, NatCITest workspace)
        stage('Deploy to Test') {
            when { branch 'develop' }
            steps {
                echo 'Deploying to Test environment...'
                // sh 'deploy.sh test'
            }
        }

        // Replaces: natdeploy-prod.xml (CI-PROD, NDVPROD, NatCIProd workspace)
        // Note: deploy.delete=YES in prod -> cleanup step included
        stage('Deploy to Prod') {
            when { branch 'main' }
            steps {
                input message: 'Deploy to Production?', ok: 'Deploy'
                echo 'Deploying to Production environment...'
                // sh 'deploy.sh prod'
                // Cleanup removed sources (equivalent to deploy.delete=YES)
                echo 'Cleaning up removed resources...'
            }
        }
    }

    post {
        success {
            echo 'Pipeline completed successfully.'
        }
        failure {
            echo 'Pipeline failed.'
            // Notification: email, Slack, etc.
        }
    }
}
```

---

## Section 9: Migration Execution Plan

### Phase 1: Project Setup (Week 1-2)

| Task | Description | Deliverable |
|---|---|---|
| 1.1 | Create Java project with Spring Boot Initializr | `pom.xml`, project skeleton |
| 1.2 | Configure `application.yml` for all environments | Config files |
| 1.3 | Set up PostgreSQL database | Database instance |
| 1.4 | Create Flyway migration scripts (V1, V2) | SQL DDL scripts |
| 1.5 | Set up CI/CD pipeline (GitHub Actions or Jenkins) | `ci.yml` or `Jenkinsfile` |
| 1.6 | Configure Docker Compose for local development | `docker-compose.yml` |

### Phase 2: Data Layer (Week 2-3)

| Task | Description | Deliverable |
|---|---|---|
| 2.1 | Create `Yacht.java` JPA entity | Entity class |
| 2.2 | Create `Cruise.java` JPA entity with `@ManyToOne` yacht relationship | Entity class |
| 2.3 | Create `CruiseStatus.java` enum with status code mapping | Enum class |
| 2.4 | Create `CruiseRepository.java` | Repository interface |
| 2.5 | Create `YachtRepository.java` | Repository interface |
| 2.6 | Create `CruiseDTO.java` | DTO class |
| 2.7 | Write repository unit tests | Test classes |

### Phase 3: Service Layer (Week 3-4)

| Task | Description | Natural Source |
|---|---|---|
| 3.1 | Implement `CruiseService.findCruiseById()` | `NCFINDCR.NSN` |
| 3.2 | Implement date formatting (`YYYY-MM-DD`) | `NCFINDCR.NSN` lines 23, 25 |
| 3.3 | Implement time formatting (with `h` suffix) | `NCFINDCR.NSN` lines 24, 26 |
| 3.4 | Implement EUR price formatting | `NCFINDCR.NSN` lines 31-33 |
| 3.5 | Implement status code → text mapping | `NCFINDCR.NSN` lines 36-43 |
| 3.6 | Implement yacht name resolution via JPA relationship | `NCFINDCR.NSN` lines 46-48 |
| 3.7 | Implement `ReportService.generatePaginatedReport()` | `NCATENDP.NSP` |
| 3.8 | Implement `ReportService.generateDisplayReport()` | `NCDEDISP.NSP` |
| 3.9 | Implement `ReportService.generateFormReport()` | `NCWRFORP.NSP` |
| 3.10 | Write comprehensive service unit tests (TC-02 through TC-11) | `NCTESTP.NSP` |

### Phase 4: REST API Controllers (Week 4-5)

| Task | Description | Natural Source |
|---|---|---|
| 4.1 | Implement `CruiseController.getCruiseById()` | `NCINMAPP.NSP` |
| 4.2 | Implement `CruiseController.listCruises()` | `NCATENDP.NSP` |
| 4.3 | Implement `CruiseController.displayReport()` | `NCDEDISP.NSP` |
| 4.4 | Implement `CruiseController.formReport()` | `NCWRFORP.NSP` |
| 4.5 | Implement `GlobalExceptionHandler` | `NCINMAPP.NSP` ON ERROR block |
| 4.6 | Write integration tests with MockMvc | All TCs |

### Phase 5: Frontend (Week 5-7, Optional)

| Task | Description | Natural Source |
|---|---|---|
| 5.1 | Create main menu page (React or Thymeleaf) | `NCMENUM.NSM` |
| 5.2 | Create cruise detail page | `NCDEMAPM.NSM` |
| 5.3 | Create cruise form/report page | `NCDEFORM.NSM` |
| 5.4 | Implement navigation routing | `NCMENUP.NSP` STACK TOP COMMAND logic |
| 5.5 | Implement help pages | `NCDEMAPH.NSH`, `NCDECIDH.NSH` |

### Phase 6: Data Migration (Week 7-8)

| Task | Description |
|---|---|
| 6.1 | Export all NCCRUISE records from Adabas using Natural UNLOAD |
| 6.2 | Export all NCYACHT records from Adabas |
| 6.3 | Transform Adabas data to CSV (date/time/decimal conversion) |
| 6.4 | Import into PostgreSQL using SQL COPY or Flyway migration |
| 6.5 | Validate row counts match between Adabas and PostgreSQL |
| 6.6 | Spot-check key records for data integrity |

### Phase 7: Parallel Running & Validation (Week 8-10)

| Task | Description |
|---|---|
| 7.1 | Run both Natural and Java applications against the same test data |
| 7.2 | Execute all test cases (TC-01 through TC-12) on both systems |
| 7.3 | Compare outputs side-by-side for identical results |
| 7.4 | Document any differences and resolve them |
| 7.5 | Performance benchmarking (response times, throughput) |

### Phase 8: Cutover & Decommission (Week 10-12)

| Task | Description |
|---|---|
| 8.1 | Final data sync from Adabas to PostgreSQL |
| 8.2 | DNS/routing switch from Natural app to Java app |
| 8.3 | Monitor Java application for errors and performance |
| 8.4 | Keep Natural application available as fallback for 2 weeks |
| 8.5 | Decommission Natural application and NDV server |
| 8.6 | Archive Natural source code (this repository) |

---

## Section 10: Validation Checklist

### Side-by-Side Comparison Checklist

For each test case, run the same input through both the Natural application and the Java application and verify identical outputs:

| TC | Test Description | Natural Output | Java Output | Match? | Notes |
|---|---|---|---|---|---|
| TC-02 | Cruise lookup ID=10000001 | Record with Cassandra, Piraeus→Santorini | JSON with same fields | | Compare all 12 fields |
| TC-03 | Cruise lookup ID=99999999 | "No Cruise found for Id" | 404 with error message | | Message text must match |
| TC-04a | Status 0 | "removed" | "removed" | | Exact string match |
| TC-04b | Status 1 | "planned" | "planned" | | Exact string match |
| TC-04c | Status 2 | "available" | "available" | | Exact string match |
| TC-04d | Status 3 | "sold" | "sold" | | Exact string match |
| TC-04e | Status 9 | "unknown" | "unknown" | | Exact string match |
| TC-05 | Price 1000.000 | "EUR  1000.00" | EUR formatted | | Verify decimal precision |
| TC-06 | Date 20240615 | "2024-06-15" | "2024-06-15" | | Exact format match |
| TC-07 | Report 40 records | Paginated with headers | Paginated JSON | | Record count must match |
| TC-08 | Display 100 records | Tabular with edit masks | JSON array | | Field values must match |
| TC-09 | Form hardcoded | Samos, Santorini, Cassandra | Same JSON values | | All 13 fields must match |
| TC-10 | Error handling | BACKOUT + error message | Rollback + 500 response | | Transaction rolled back |
| TC-11 | Yacht resolution | Yacht name from NCYACHT | Yacht name from JOIN | | Names must match |

### Data Integrity Verification Queries

Run these queries to verify data migration completeness:

```sql
-- Total record counts must match Adabas
SELECT 'cruise' AS table_name, COUNT(*) AS row_count FROM cruise
UNION ALL
SELECT 'yacht', COUNT(*) FROM yacht;

-- Verify all status values are preserved
SELECT cruise_status, COUNT(*) FROM cruise GROUP BY cruise_status ORDER BY cruise_status;

-- Verify all yacht foreign keys resolve
SELECT c.id, c.yacht_id, y.yacht_name
FROM cruise c
LEFT JOIN yacht y ON c.yacht_id = y.id
WHERE y.id IS NULL;

-- Verify date range integrity
SELECT MIN(start_date), MAX(start_date), MIN(end_date), MAX(end_date) FROM cruise;

-- Verify price ranges
SELECT MIN(price_1w), MAX(price_1w), AVG(price_1w) FROM cruise;

-- Spot-check specific records
SELECT * FROM cruise WHERE id = 10000001;
SELECT * FROM yacht WHERE id = 1;
```

### Performance Benchmarking Approach

| Metric | Natural Baseline | Java Target | How to Measure |
|---|---|---|---|
| Single cruise lookup latency | Measure via Natural terminal | < 100ms | JMeter or `curl -w` |
| Paginated report (40 records) | Measure via Natural terminal | < 200ms | JMeter |
| Display report (100 records) | Measure via Natural terminal | < 500ms | JMeter |
| Concurrent users (10) | Natural NDV capacity | No degradation | JMeter thread group |
| Database query time | Adabas FIND timing | < 50ms | PostgreSQL `EXPLAIN ANALYZE` |

### Go/No-Go Criteria for Cutover

- [ ] All test cases (TC-01 through TC-12) pass on the Java application
- [ ] Side-by-side comparison shows identical outputs for all test data
- [ ] Data migration verified: row counts match, no orphaned records
- [ ] Performance meets or exceeds Natural application baselines
- [ ] CI/CD pipeline runs green for all environments (dev, test, prod)
- [ ] Error handling produces informative messages (no stack traces to users)
- [ ] Rollback plan documented and tested
- [ ] Team trained on Java application operation and monitoring
