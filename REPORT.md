# Fridge Accountant Report

## 0. AI Attribution

This report was started with the help of Chat GPT on 10 May 2026, based on my original idea in project-description.txt (which is 100% original). Further edits were done after the initial write-up by Chat GPT, to remove superfluous content and add in my thoughts that were missed by Chat GPT.

## 1. Purpose of the Application

Fridge Accountant is a shared-fridge management application designed for households such as shared apartments, dormitories, or other communal living spaces. In these environments, multiple occupants use the same fridge, which can make it difficult to know who owns which food, which spaces are still available, and which items are close to expiry.

The purpose of the application is to provide a simple digital record of the fridge's current state. Users can create fridges, define their dimensions, create owners, create food or drink items, and place those items into specific fridge coordinates. The application then shows the current contents of the fridge, including each item's owner and position.

The application also supports basic analysis of fridge usage. It reports how much space is occupied or free, how much fridge space is used by each owner, and which placed items are expired or expiring soon. This helps occupants plan grocery purchases, avoid buying duplicate items, reduce food waste, and remove expired items before they become a hygiene issue.

In a real-world deployment, Fridge Accountant could be used on a tablet placed near the fridge door or integrated into a smart fridge interface. A future version could separate the system into two front ends: a fridge-facing interface that allows insertion and removal of items, and a remote monitoring interface that allows users to check the fridge contents while away from home.

## 2. Design Decisions and Justification

### 2.1 Overall Architecture

The frontend & backend is developed from the boilerplate provided by https://github.com/initialcapacity/kotlin-ktor-starter to maximize transferrable knowledge from previous project submissions in the course.

### 2.2 Grid-Based Fridge Model

The fridge is modeled as a three-dimensional grid using width, height, and depth. Each item is placed at an `(x, y, z)` coordinate. 

As the goal is to deliver a MVP, this design was chosen because it is simple enough to implement (simpler than modeling real physical item size or irregular fridge compartments), while still easy and intuitive enough for users to understand. The coordinate system allows the application to validate whether a selected position is within the fridge bounds and whether that position is already occupied.

For the current project scope, one item occupying one grid slot is enough to demonstrate ownership tracking, space usage, and expiry analysis without adding unnecessary complexity.

### 2.3 Relational Database Design

The application uses a PostgreSQL relational database with separate tables for:

- `fridge`
- `owner`
- `item`
- `fridge_record`

The relational DB is chosen (instead of non-relational DB) because the data has clear relationships: items belong to owners, and fridge records place items into fridges.

For more details, the DDLs for the tables above can be found under `databases/fridge-db/src/main/resources/db/migration/V1__create_tables.sql`. The tables are designed to conform to database normalization rules up to the 3rd normal form to ensure data integrity and avoid insertion/deletion/update anomalies.

### 2.4 Modular Architecture

The codebase is organized into applications, components, databases, and support modules, just like the examples & exercises throughout the course series. The main web application is located in `applications/basic-server`, while reusable domain and database logic is placed in `components/data-model` and `components/database-support`.

This structure was chosen to separate responsibilities, as we have learned in this & previous courses. This allows new features or other improvements to be implemented in the project easily.

The web application handles HTTP routes and templates. The data model contains fridge, item, owner, record, and analysis logic. Database support contains reusable database access utilities. 

The data analyzer server schedules a periodic analysis task. There's a potential new feature here: the analysis output can be piped into a message queue to be consumed by a notifier to alert users of their expiring items.

### 2.5 Validation at the Data Access Layer

Insertion into a fridge validates that the item exists, the fridge exists, the selected coordinate is within the fridge dimensions, and the position is not already occupied.

This validation is placed close to the database operation so that invalid records are prevented even if future user interfaces are added. This is important because the project description anticipates multiple front ends. Keeping validation in shared backend logic reduces the chance that one interface enforces rules while another bypasses them.

### 2.6 Built-In Data Analysis

The application includes three useful analyses:

- space utilization
- owner usage
- expiry risk

Space utilization calculates total capacity, occupied slots, free slots, and occupancy percentage. Owner usage calculates how many slots each owner uses. Expiry risk groups placed items into expired, expiring today, expiring within three days, and expiring within seven days.

These analyses were chosen because they directly support the application's purpose. They help users understand whether there is enough space before shopping, whether fridge usage is balanced among occupants, and which items need attention soon.

### 2.7 More Complex Features are Deferred

This is a deliberate scope decision. The current project focuses on the core architecture, data model, fridge operations, and analysis features. More complex features are left out for now to allow the MVP to be rolled out sooner.

Some of the deferred features:
- Authentication and detailed permission control are not implemented in the current version. Authentication would be important in a production system, especially if different interfaces had different permissions.
- Alert/notification system to inform users of their expiring items.

## 3. System Requirements

### 3.1 Functional Requirements

The system shall allow users to create a fridge with a name, width, height, and depth.

The system shall allow users to view all fridges.

The system shall allow users to create owners.

The system shall allow users to view all owners.

The system shall allow users to create items with a name, expiry date, and owner.

The system shall allow users to view all items.

The system shall allow users to place an item into a specific fridge coordinate.

The system shall prevent users from placing an item outside the fridge dimensions.

The system shall prevent users from placing an item into an already occupied fridge position.

The system shall allow users to view the contents of a fridge, including item name, owner, and position.

The system shall allow users to remove an item record from a fridge.

The system shall show space utilization analysis for all fridges on the landing page.

The system shall show owner usage analysis for all fridges on the landing page.

The system shall show expiry risk analysis for all fridges on the landing page.

The system shall show the same three analyses for an individual fridge on that fridge's detail page.

The system shall identify placed items that are expired or expiring within seven days.

### 3.2 Non-Functional Requirements

The system should be simple enough to run locally for development and demonstration.

The system should persist fridge, owner, item, and placement data in a database.

The system should use server-side validation to protect important data rules.

The system should provide a clear browser-based interface for managing fridge data.

The system should separate web routing, domain logic, database logic, and analysis logic into different modules where practical.

The system should be testable using automated tests.

The system should be buildable as a Java archive using Gradle.

The system should be deployable either directly on the JVM or through Docker.

The system should be extensible so that future front ends, background workers, notifications, or smart-fridge integrations can reuse the same core data model.

### 3.3 Software Requirements

The application requires a Java runtime compatible with the configured Gradle/Kotlin toolchain.

The application requires Gradle for building and testing.

The application uses Kotlin as the programming language.

The application uses Ktor as the web framework.

The application uses Netty as the embedded web server.

The application uses Freemarker for HTML templates.

The application uses PostgreSQL for persistent storage.

The application uses Flyway database migrations for schema setup.

The application uses JUnit/Kotlin test tooling for automated tests.

Docker can be used as an optional deployment environment.

### 3.4 Hardware and Runtime Requirements

For local development, the system can run on a standard laptop or desktop capable of running the JVM, PostgreSQL, and a web browser.

For a household deployment, the system could run on a small server, mini PC, or single-board computer connected to the home network. Users would access it through a browser. A tablet mounted near the fridge could serve as the fridge-facing interface.

For a future smart-fridge deployment, the system would require integration with fridge hardware, such as a touch display, door lock, or item scanning mechanism. Those hardware integrations are outside the current implementation scope.

### 3.5 Data Requirements

Each fridge must have a name and positive dimensions.

Each owner must have a name.

Each item must have a name, expiry date, and valid owner.

Each fridge placement must reference an existing fridge and item.

Each fridge placement must contain valid `x`, `y`, and `z` coordinates.

A fridge coordinate should contain at most one item at a time.

Expiry analysis requires item expiry dates to be stored accurately.

Owner usage analysis requires items to be associated with the correct owner.

Space utilization analysis requires fridge dimensions and current fridge placement records.

## 4. Current Limitations and Future Improvements

The current version does not include authentication or role-based authorization. In a future version, users should log in, and only authorized users should be able to modify fridge contents.

The current version uses one web interface for both management and monitoring. A future version could provide separate interfaces for fridge-side operation and remote monitoring.

The current placement model allows one item to occupy more than one grid slot even if the slots are not continuous/neighboring. A future version should enforce checks to prevent this.

The current expiry feature displays risk analysis but does not send notifications. A future version could send email, push, or in-app notifications when items are near expiry.

The current system does not store insert and removal timestamps. Adding an event history would allow trend analysis, such as average item storage time, peak fridge usage, and prediction of when fridge space will run out.

The current system relies on users to keep the digital record accurate. A smart-fridge integration, barcode scanner, weight sensor, or door-lock workflow could reduce mismatches between the application state and the real fridge contents.

Future versions could add a feature to book the space in the fridge in advance to avoid conflicts between fridge users.