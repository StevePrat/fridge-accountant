# Kotlin ktor starter

An [application continuum](https://www.appcontinuum.io/) style example using Kotlin and Ktor
that includes a single web application with one background workers.

* Basic web application
* Data analyzer

### Technology stack

This codebase is written in a language called [Kotlin](https://kotlinlang.org) that is able to run on the JVM with full
Java compatibility.
It uses the [Ktor](https://ktor.io) web framework, and runs on the [Netty](https://netty.io/) web server.
HTML templates are written using [Freemarker](https://freemarker.apache.org).
The codebase is tested with [JUnit](https://junit.org/) and uses [Gradle](https://gradle.org) to build a jarfile.

## Getting Started

1.  Build a Java Archive (jar) file.
    ```bash
    ./gradlew clean build
    ```

1.  Run the following in the terminal to prepare the DB. You need to have postgres installed for this to work.
    ```bash
    sudo -u postgres psql
    ```
    The terminal will now accept PostgreSQL commands. Run the SQL below
    ```sql
    CREATE USER fridge_user WITH PASSWORD 'fridge_password';
    CREATE DATABASE fridge_dev OWNER fridge_user;
    CREATE DATABASE fridge_test OWNER fridge_user;
    ```
    And then exit the PostgreSQL CLI by entering
    ```sql
    exit;
    ```
    We're now back in the default shell terminal. Next, run the following:
    ```bash
    ./gradlew :databases:fridge-db:devMigrate
    ```

1.  Configure the port that each server runs on.
    ```bash
    export PORT=8881
    ```

1.  Run the server for the web app

    ```bash
    java -jar applications/basic-server/build/libs/basic-server.jar
    ```

1.  (Optional) Run the server for the data analyzer
    Data analyzer
    ```bash
    java -jar applications/data-analyzer-server/build/libs/data-analyzer-server.jar
    ```

    The data analyzer is currently configured to run an analysis every 1 hour and logs the result into the terminal.
