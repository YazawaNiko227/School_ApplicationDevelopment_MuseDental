package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
	private static Connection con = null;
	private static ConnectDB instance = new ConnectDB();

	public static ConnectDB getInstance() {
		return instance;
	}

	public void connect() {
		String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamMuse;encrypt=true;trustServerCertificate=true";
		String user = "sa";
		String password = "123456789";

		try {
			con = DriverManager.getConnection(url, user, password);

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void disconection() {
		if (con != null) {
			try {
				con.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	public static Connection getConnection() {
		return con;
	}
}