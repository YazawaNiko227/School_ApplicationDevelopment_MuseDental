# School Application Development - Muse Dental

## Build and run

Run `build.bat` to compile every Java source under `src/main/java`, then run
`run.bat` to launch the application.

After confirming an invoice in the billing screen, choose to export it as a PDF
and select where to save the file.

## SQL Server connection

The database connection uses these environment variables:

- `MUSE_DB_USER` (required)
- `MUSE_DB_PASSWORD` (required)
- `MUSE_DB_URL` (optional; defaults to the local `QuanLyPhongKhamMuse` database)

The Microsoft SQL Server JDBC driver is included in `lib`. Configure the SQL
Server TCP/IP endpoint and create the database using
`database/QuanLyPhongKhamMuse_Schema .sql`.

Set the SQL Server login in PowerShell before starting the application:

```powershell
$env:MUSE_DB_USER = "muse_app"
$env:MUSE_DB_PASSWORD = "your SQL login password"
$env:MUSE_DB_URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamMuse;encrypt=true;trustServerCertificate=true"
.\run.bat
```

The login needs read/write access to the application tables. Run database
schema and migration scripts using a SQL Server account with DDL permissions.

For an existing database, run `database/Migration_RBAC.sql` once in SSMS before
signing in. The application creates the default menu grants on its first
successful database login after the migration.

The sample `TaiKhoan` rows in the schema contain placeholder password hashes.
After building and configuring the database connection, initialize one sample
account password from a terminal:

```powershell
java -cp "out;lib\*" util.TaoMatKhauBanDau
```

The tool prompts for a new password without echoing it and only updates a sample
row that still has the schema's placeholder hash. It cannot reset an account
that already has a real password hash.

## Database-backed screens

- Login verifies active account credentials with PBKDF2-HMAC-SHA256 hashes and
  loads the employee and role.
- Dashboard reads today's appointment and paid-invoice totals, patient count,
  and upcoming appointments from SQL Server.
- Change password verifies the current password and stores a new salted hash.
- RBAC stores menu-view grants per role in `QuyenVaiTro`; changes take effect
  after users sign in again.
- Employee management reads and performs CRUD operations on `NhanVien`.
