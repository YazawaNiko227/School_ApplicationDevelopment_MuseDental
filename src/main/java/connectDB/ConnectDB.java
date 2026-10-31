package connectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Nguồn kết nối duy nhất (Singleton) đến SQL Server cho toàn bộ DAO.
 *
 * Thông tin đăng nhập đọc từ biến môi trường (xem README):
 * <ul>
 * <li>MUSE_DB_URL: chuỗi kết nối JDBC</li>
 * <li>MUSE_DB_USER: tên đăng nhập SQL Server</li>
 * <li>MUSE_DB_PASSWORD: mật khẩu</li>
 * </ul>
 */
public class ConnectDB {

    private static final String URL_MAC_DINH = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyPhongKhamMuse"
            + ";encrypt=true;trustServerCertificate=true";

    private static Connection con;
    private static final ConnectDB INSTANCE = new ConnectDB();

    private ConnectDB() {
    }

    public static ConnectDB getInstance() {
        return INSTANCE;
    }

    /** Mở kết nối; nếu kết nối cũ còn mở thì dùng lại. */
    public void connect() throws SQLException {
        if (con != null && !con.isClosed()) {
            return;
        }
        String url = bienMoiTruongHoacMacDinh("MUSE_DB_URL", URL_MAC_DINH);
        String user = bienMoiTruongBatBuoc("MUSE_DB_USER");
        String password = bienMoiTruongBatBuoc("MUSE_DB_PASSWORD");
        con = DriverManager.getConnection(url, user, password);
    }

    /** Kết nối cho DAO: tự mở lại nếu chưa kết nối hoặc kết nối đã đứt. */
    public static Connection getConnection() throws SQLException {
        getInstance().connect();
        return con;
    }

    /** Đóng kết nối (gọi khi thoát ứng dụng). */
    public static void dongKetNoi() {
        if (con == null) {
            return;
        }
        try {
            con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String bienMoiTruongHoacMacDinh(String tenBien, String macDinh) {
        String giaTri = System.getenv(tenBien);
        return giaTri == null || giaTri.trim().isEmpty() ? macDinh : giaTri.trim();
    }

    private static String bienMoiTruongBatBuoc(String tenBien) throws SQLException {
        String giaTri = System.getenv(tenBien);
        if (giaTri == null || giaTri.trim().isEmpty()) {
            throw new SQLException("Chưa cấu hình biến môi trường " + tenBien + ".");
        }
        return giaTri.trim();
    }
}
