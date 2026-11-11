package dao;

import connectDB.ConnectDB;
import entity.VatTu;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VatTuDAO {
    public static final String LOAI_THUOC = "Thuốc";

    public List<VatTu> getAllExceptLoai(String loaiLoaiTru) throws SQLException {
        String sql = "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, "
                + "NguongToiThieu, HanSuDung, DonGiaBan FROM VatTu "
                + "WHERE LoaiVatTu IS NULL OR LoaiVatTu <> ? ORDER BY MaVatTu";
        List<VatTu> vatTuList = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, loaiLoaiTru);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Date hanSuDung = result.getDate("HanSuDung");
                    vatTuList.add(new VatTu(
                            result.getString("MaVatTu"),
                            result.getString("TenVatTu"),
                            result.getString("LoaiVatTu"),
                            result.getString("DonViTinh"),
                            result.getInt("SoLuongTon"),
                            result.getInt("NguongToiThieu"),
                            hanSuDung == null ? null : hanSuDung.toLocalDate(),
                            result.getDouble("DonGiaBan")));
                }
            }
        }
        return vatTuList;
    }

    public List<VatTu> timKiem(String tuKhoa, String loaiVatTu) throws SQLException {
        // Làm sạch từ khóa: null thì thành chuỗi rỗng, rồi cắt khoảng trắng
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        tuKhoa = tuKhoa.trim();

        boolean coLoai = (loaiVatTu != null);
        boolean coTuKhoa = !tuKhoa.isEmpty();

        // Câu SQL gốc
        String sql = "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, "
                + "NguongToiThieu, HanSuDung, DonGiaBan FROM VatTu WHERE 1 = 1";

        // Có điều kiện nào thì nối thêm điều kiện đó
        if (coLoai) {
            sql = sql + " AND LoaiVatTu = ?";
        }
        if (coTuKhoa) {
            sql = sql + " AND (MaVatTu LIKE ? OR TenVatTu LIKE ?)";
        }
        sql = sql + " ORDER BY MaVatTu";

        List<VatTu> danhSach = new ArrayList<>();

        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            // Điền giá trị vào các dấu ? theo đúng thứ tự đã nối ở trên
            int viTri = 1;

            if (coLoai) {
                statement.setString(viTri, loaiVatTu);
                viTri = viTri + 1;
            }
            if (coTuKhoa) {
                String mau = "%" + tuKhoa + "%";
                statement.setString(viTri, mau); // ? của MaVatTu
                statement.setString(viTri + 1, mau); // ? của TenVatTu
            }

            // Chạy câu lệnh và đọc từng dòng kết quả
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(docVatTu(result));
                }
            }
        }
        return danhSach;
    }

    public VatTu timTheoMa(String maVatTu) throws SQLException {
        String sql = "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, "
                + "NguongToiThieu, HanSuDung, DonGiaBan FROM VatTu WHERE MaVatTu = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, maVatTu);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? docVatTu(result) : null;
            }
        }
    }

    public List<VatTu> timKiemKhongThuoc(String tuKhoa) throws SQLException {
        if (tuKhoa == null) {
            tuKhoa = "";
        }
        String mau = "%" + tuKhoa.trim() + "%";

        String sql = "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, "
                + "NguongToiThieu, HanSuDung, DonGiaBan FROM VatTu "
                + "WHERE (LoaiVatTu IS NULL OR LoaiVatTu <> ?) "
                + "AND (MaVatTu LIKE ? OR TenVatTu LIKE ?) ORDER BY MaVatTu";

        List<VatTu> danhSach = new ArrayList<>();
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, LOAI_THUOC);
            statement.setString(2, mau);
            statement.setString(3, mau);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    danhSach.add(docVatTu(result));
                }
            }
        }
        return danhSach;
    }

    public String getNextId() throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(MaVatTu, 3, 18))), 0) + 1 "
                + "FROM VatTu WHERE MaVatTu LIKE 'VT%' "
                + "AND TRY_CONVERT(int, SUBSTRING(MaVatTu, 3, 18)) IS NOT NULL";
        try (Statement statement = getConnection().createStatement();
                ResultSet result = statement.executeQuery(sql)) {
            result.next();
            return String.format("VT%03d", result.getInt(1));
        }
    }

    public String sinhMa() throws SQLException {
        return getNextId();
    }

    public boolean them(VatTu vatTu) throws SQLException {
        return insert(vatTu);
    }

    public boolean capNhat(VatTu vatTu) throws SQLException {
        return update(vatTu);
    }

    public boolean xoa(String maVatTu) throws SQLException {
        return delete(maVatTu);
    }

    public boolean insert(VatTu vatTu) throws SQLException {
        String sql = "INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, "
                + "SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            setVatTuParameters(statement, vatTu);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean update(VatTu vatTu) throws SQLException {
        String sql = "UPDATE VatTu SET TenVatTu = ?, LoaiVatTu = ?, DonViTinh = ?, "
                + "SoLuongTon = ?, NguongToiThieu = ?, HanSuDung = ?, DonGiaBan = ? WHERE MaVatTu = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, vatTu.getTenVatTu());
            statement.setString(2, vatTu.getLoaiVatTu());
            statement.setString(3, vatTu.getDonViTinh());
            statement.setInt(4, vatTu.getSoLuongTon());
            statement.setInt(5, vatTu.getNguongToiThieu());
            setDate(statement, 6, vatTu);
            statement.setDouble(7, vatTu.getDonGiaBan());
            statement.setString(8, vatTu.getMaVatTu());
            return statement.executeUpdate() == 1;
        }
    }

    public boolean delete(String maVatTu) throws SQLException {
        String sql = "DELETE FROM VatTu WHERE MaVatTu = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(sql)) {
            statement.setString(1, maVatTu);
            return statement.executeUpdate() == 1;
        }
    }

    public void congTon(Connection connection, String maVatTu, int soLuong) throws SQLException {
        String sql = "UPDATE VatTu SET SoLuongTon = SoLuongTon + ? WHERE MaVatTu = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, soLuong);
            statement.setString(2, maVatTu);
            statement.executeUpdate();
        }
    }

    public void truTon(Connection connection, String maVatTu, int soLuong) throws SQLException {
        if (soLuong <= 0) {
            throw new SQLException("Số lượng xuất kho phải lớn hơn 0.");
        }
        String sql = "UPDATE VatTu SET SoLuongTon = SoLuongTon - ? "
                + "WHERE MaVatTu = ? AND SoLuongTon >= ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, soLuong);
            statement.setString(2, maVatTu);
            statement.setInt(3, soLuong);
            if (statement.executeUpdate() != 1) {
                throw new SQLException("Vật tư không tồn tại hoặc số lượng tồn không đủ: " + maVatTu);
            }
        }
    }

    private void setVatTuParameters(PreparedStatement statement, VatTu vatTu) throws SQLException {
        statement.setString(1, vatTu.getMaVatTu());
        statement.setString(2, vatTu.getTenVatTu());
        statement.setString(3, vatTu.getLoaiVatTu());
        statement.setString(4, vatTu.getDonViTinh());
        statement.setInt(5, vatTu.getSoLuongTon());
        statement.setInt(6, vatTu.getNguongToiThieu());
        setDate(statement, 7, vatTu);
        statement.setDouble(8, vatTu.getDonGiaBan());
    }

    private VatTu docVatTu(ResultSet result) throws SQLException {
        Date hanSuDung = result.getDate("HanSuDung");
        return new VatTu(
                result.getString("MaVatTu"),
                result.getString("TenVatTu"),
                result.getString("LoaiVatTu"),
                result.getString("DonViTinh"),
                result.getInt("SoLuongTon"),
                result.getInt("NguongToiThieu"),
                hanSuDung == null ? null : hanSuDung.toLocalDate(),
                result.getDouble("DonGiaBan"));
    }

    private void setDate(PreparedStatement statement, int parameterIndex, VatTu vatTu) throws SQLException {
        if (vatTu.getHanSuDung() == null) {
            statement.setNull(parameterIndex, java.sql.Types.DATE);
        } else {
            statement.setDate(parameterIndex, Date.valueOf(vatTu.getHanSuDung()));
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = ConnectDB.getConnection();
        if (connection == null || connection.isClosed()) {
            throw new SQLException("Chưa có kết nối cơ sở dữ liệu.");
        }
        return connection;
    }
}
