# Java Balance Sheet Manager

A simple Java desktop application for managing daily financial entries (revenues and expenses), storing them in a **MariaDB** database, and organizing them into **monthly tables** (e.g., `june_25`). The GUI is built with **Swing**, and includes features like:

- Inserting or updating daily entries with a date picker
- Viewing monthly balances in a table
- Auto-creating monthly tables if they don’t exist
- Overwriting entries for the same day instead of duplicating

## 🧰 Features

- 📅 Insert balance entries using a calendar popup
- 📊 Display monthly summary with profit calculation
- 💾 Auto-create SQL tables per month like `may_25`, `june_25`, etc.
- 🔄 Automatically updates existing entries for the same date
- ❗ Warning dialog if selected table does not exist

---

## 🚀 Getting Started

### Prerequisites

- Java 8 or newer
- [MariaDB](https://mariadb.org/) or MySQL installed locally
- JDBC driver for MySQL (`mysql-connector-java-x.x.xx.jar`)
- Optional: IDE (e.g., IntelliJ, Eclipse)

### Database Setup

1. Create a database named `balance_db`:

sql
CREATE DATABASE balance_db;
You don't need to create monthly tables manually — the app creates them automatically in the format:
june_25, july_25, etc.

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

### Dependencies

Swing – GUI framework
JDatePicker – for calendar input
MySQL JDBC Driver – for DB connection
java.time.* – for date handling
To use the date picker, make sure you include this dependency in your project (or add the .jar manually):
jdatepicker-1.3.4.jar



