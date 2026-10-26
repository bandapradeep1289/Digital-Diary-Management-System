# Digital Diary Management System

Java Swing + JDBC + MySQL desktop application (Object Oriented Programming Through Java course project).

## Folder structure
```
DigitalDiary/
├── sql/schema.sql
└── src/
    ├── model/      User, Entry (abstract), PersonalEntry, WorkEntry, TravelEntry
    ├── dao/        DiaryOperations (interface), UserDAO, EntryDAO
    ├── util/       DBConnection, FileUtil, EntryDateComparator
    ├── exception/  InvalidLoginException, EmptyEntryException, DuplicateUserException
    ├── thread/     ClockThread, AutoSaveThread
    └── ui/         MainFrame (main), LoginPanel, RegisterPanel, DashboardPanel, EntryDialog
```

## Setup
1. Install JDK 17+ and MySQL 8.0.
2. Run `sql/schema.sql` in MySQL Workbench.
3. Download MySQL Connector/J (e.g. `mysql-connector-j-8.x.jar`).
4. Set your MySQL password in `src/util/DBConnection.java`
   (or set env vars `DIARY_DB_USER`, `DIARY_DB_PASS`, `DIARY_DB_URL`).

## Compile and run (Windows, from the DigitalDiary folder)
```
mkdir out
javac -d out -cp "mysql-connector-j-8.x.jar" src\model\*.java src\exception\*.java src\util\*.java src\dao\*.java src\thread\*.java src\ui\*.java
java -cp "out;mysql-connector-j-8.x.jar" ui.MainFrame
```
(On Linux/macOS use `/` in paths and `:` instead of `;` in the classpath.)

In Eclipse / IntelliJ / VS Code: create a Java project, copy the `src` folder, and add the Connector/J jar to the build path. Run `ui.MainFrame`.
