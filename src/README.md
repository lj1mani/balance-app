# Java Balance Sheet Manager

A simple Java desktop application for managing daily financial entries (revenues and expenses), storing them in a **MariaDB** database, and organizing them into **monthly tables** (e.g., `june_25`). The GUI is built with **Swing**, and includes features like:

- Inserting or updating daily entries with a date picker
- Viewing monthly balances in a table
- Auto-creating monthly tables if they don’t exist
- Overwriting entries for the same day instead of duplicating

## 🧰 Features

📅 Insert Daily Revenue and Expense

📈 Automatic Profit Calculation (profit = revenue - expense)

🧠 Avoid Duplicate Entries (updates entry for the same date)

📋 View Monthly Entries in a Table

📆 Select Month & Year to View Entries

💰 NEW: Monthly Profit Summary – shows the total monthly profit!

🗂️ Tables are Automatically Created per Month

---

### 🚀 How to Run

Make sure you have Java and MariaDB installed.

Create the balance_db database in MariaDB.

Set your database credentials in DatabaseManager.java.

Run the application via your IDE or java Main.

### Prerequisites

- Java 8 or newer
- [MariaDB](https://mariadb.org/) or MySQL installed locally
- JDBC driver for MySQL (`mysql-connector-java-x.x.xx.jar`)
- Optional: IDE (e.g., IntelliJ, Eclipse)

### Database Setup

1. Create a database named `balance_db`:


CREATE DATABASE balance_db;
You don't need to create monthly tables manually — the app creates them automatically in the format:
june_25, july_25, etc.

💾 Database Structure
Each table is named based on the month and year (e.g., june_25) and contains:

Column	Type	Description

id	INT (Auto Inc.)	Primary key

entry_date	DATE	Unique daily entry date

revenue	DECIMAL(10,2)	Daily revenue

expense	DECIMAL(10,2)	Daily expense

profit	DECIMAL(10,2)	Auto-calculated: revenue - expense

### Configuration

Make sure your DatabaseManager has the correct DB connection settings:

String url = "jdbc:mysql://localhost:3306/balance_db";
String user = "your_username";
String password = "your_password";

### How to Use
Inserting a Daily Entry
Click "Insert Entry"
Choose a date using the calendar picker
Enter revenue and expense
Click OK

If an entry for that date already exists, it will be updated, not duplicated.

Viewing Monthly Summary
Click "Show Monthly Balance"
Select month and year
A table will appear with one row per day and calculated profit

## Database Configuration

For security reasons, database credentials are not stored directly in the source code.  
Instead, they are loaded from a configuration file (`db.properties`).

### 1. Create the Configuration File
1. In the project root folder, you will find a file named `db.properties.example`.
2. Make a copy of it and rename it to `db.properties`.

### Dependencies

Swing – GUI framework
JDatePicker – for calendar input
MySQL JDBC Driver – for DB connection
java.time.* – for date handling
To use the date picker, make sure you include this dependency in your project (or add the .jar manually):
jdatepicker-1.3.4.jar

### Methods used

#### BalanceAppGUI Methods

- **showInsertBalanceDialog()**
Displays a dialog for inserting a new daily balance entry with date, revenue, and expense. Validates input and saves it to the database.
----
- **DateLabelFormatter (inner class)**
Custom formatter for date pickers, converting between String and Date in the format dd.MM.yyyy.
-----
- **showMonthlyBalanceTable()**
Shows a fullscreen table with all daily balance entries for a selected month and year. Colors profit values green/red depending on positive/negative.
----
- **showMonthlyBalanceTable2(String tableName)**
Shows a fullscreen table for a specific monthly table name. Similar to showMonthlyBalanceTable() but accepts a table name directly.
----
- **showMonthlyProfitSummary()**
Displays a summary of total profit for a selected month and year. Positive profits are shown in green, negative in red.
----
- **showUpdateDailyEntryDialog()**
Displays a dialog to update an existing daily balance entry for a selected date. Validates input and updates the database.
-----
- **showAvailableMonthsPanel()**
Shows a list of months for which balance data exists. Allows the user to select one and view the corresponding table.
----
- **showLoginFrame()**
Displays a fullscreen login frame with fields for username and password. Includes buttons to log in or add a new user. Returns true if login succeeds.
-----
- **showAddUserDialog(JFrame parent)**
-------

#### DatabaseManager Methods

- **getConnection()**
Returns a new database connection using credentials from db.properties.
-----
- **ensureMonthlyTable(LocalDate date)**
Ensures a monthly table exists for the given date; creates it if it doesn’t exist.
-----
- **formatMonthName(LocalDate date)**
Formats a date into a table name like "june_25" for storing monthly entries.
-----
- **getMonthlyTableName(LocalDate date)**
Static method to generate a monthly table name like "june_25" for a given date.
-----
- **insertDailyEntry(DailyEntry entry)**
Inserts or updates a daily entry (date, revenue, expense) in the monthly table.
-----
- **insertDailyEntry2(DailyEntry entry)**
Dummy/test method returning true/false to simulate insertion success.
------
- **getEntriesFromMonthlyTable(String tableName, Component parent)**
Retrieves all daily entries from a specified monthly table; shows a warning if the table doesn’t exist.
----
- **doesMonthlyTableExist(String tableName)**
Checks whether a specific monthly table exists in the database.
----
- **getTotalProfitFromTable(String tableName)**
Returns the total profit for a given monthly table.
------
- **getDailyEntry(LocalDate date)**
Fetches a daily entry for a given date from the current month’s table.
-----
- **updateEntryInMonthlyTable(DailyEntry entry, String tableName)**
Updates revenue and expense values for a given daily entry in the specified table.
-----
- **formatTableName(String tableName)**
Converts a table name like "may_25" into a readable format "May 2025".
------
- **getExistingMonthlyTables()**
Returns a list of all existing monthly tables in the database.
------
- **validateUser(String username, String password)**
Verifies if the provided username and password match a user in the database.
-----
- **hashPassword(String password)**
Hashes a password using SHA-256 for secure storage.
-----
- **addUser(String username, String password)**
Adds a new user to the database with a hashed password.
--------


