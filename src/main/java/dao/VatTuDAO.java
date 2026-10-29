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
