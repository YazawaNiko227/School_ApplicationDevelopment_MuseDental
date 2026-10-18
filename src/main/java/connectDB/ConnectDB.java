package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
	private static volatile Connection con;
	private static final ConnectDB instance = new ConnectDB();

	public static ConnectDB getInstance() {
		return instance;
	}

	public synchronized void connect() {
		if (con != null) {
			try {
				if (!con.isClosed()) {
					return;
				}
			} catch (SQLException exception) {
				con = null;
				throw new IllegalStateException("Could not check the database connection state.", exception);
			}
		}

		String url = environmentValue("MUSE_DB_URL",
				"jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamMuse;encrypt=true;trustServerCertificate=true");
		String user = requiredEnvironmentValue("MUSE_DB_USER");
		String password = requiredEnvironmentValue("MUSE_DB_PASSWORD");
		try {
			con = DriverManager.getConnection(url, user, password);
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not connect to the Muse Dental database.", exception);
		}
	}

	public static synchronized void disconnect() {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException exception) {
				throw new IllegalStateException("Could not close the Muse Dental database connection.", exception);
			} finally {
				con = null;
			}
		}
	}

	@Deprecated
	public static void disconection() {
		disconnect();
	}

	public static Connection getConnection() {
		if (con == null) {
			throw new IllegalStateException("Database is not connected. Call connect() first.");
		}
		try {
			if (con.isClosed()) {
				throw new IllegalStateException("Database connection is closed. Call connect() first.");
			}
		} catch (SQLException exception) {
			throw new IllegalStateException("Could not check the database connection state.", exception);
		}
		return con;
	}

	private static String environmentValue(String name, String defaultValue) {
		String value = System.getenv(name);
		return value == null || value.trim().isEmpty() ? defaultValue : value.trim();
	}

	private static String requiredEnvironmentValue(String name) {
		String value = System.getenv(name);
		if (value == null || value.trim().isEmpty()) {
			throw new IllegalStateException("Set the " + name + " environment variable to connect to the database.");
		}
		return value;
	}
}
