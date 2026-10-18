# School Application Development - Muse Dental

## Build and run

Run `build.bat` to compile every Java source under `src/main/java`, then run
`run.bat` to launch the application.

## SQL Server connection

The database connection uses these environment variables:

- `MUSE_DB_USER` (required)
- `MUSE_DB_PASSWORD` (required)
- `MUSE_DB_URL` (optional; defaults to the local `QuanLyPhongKhamMuse` database)

The Microsoft SQL Server JDBC driver must be available on the runtime classpath
for database connections. The current project does not include that driver.
