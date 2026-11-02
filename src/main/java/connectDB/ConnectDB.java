package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Nguồn kết nối duy nhất đến SQL Server cho toàn bộ DAO.
 */
public final class ConnectDB {
    private static final ConnectDB INSTANCE = new ConnectDB();
    private static final String URL = "jdbc:sqlserver://localhost:1433;"
            + "databaseName=QuanLyPhongKhamMuse;encrypt=true;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456789";

    private static Connection connection;

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return INSTANCE;
    }

    public void connect() throws SQLException {
        synchronized (ConnectDB.class) {
            if (connection != null && !connection.isClosed()) {
                return;
            }

            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                throw new IllegalStateException("Chưa kết nối SQL Server. Hãy gọi ConnectDB.getInstance().connect().");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể kiểm tra kết nối SQL Server.", exception);
        }
        return connection;
    }

    public static synchronized void dongKetNoi() {
        if (connection == null) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException exception) {
            throw new IllegalStateException("Không thể đóng kết nối SQL Server.", exception);
        } finally {
            connection = null;
        }
    }

    public static void disconection() {
        dongKetNoi();
    }
}
