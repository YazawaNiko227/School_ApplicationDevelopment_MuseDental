package dao;

import connectDB.ConnectDB;
import entity.DichVu;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DichVuDAO {

    public List<DichVu> getAllDichVu() throws SQLException {
        String sql = "SELECT MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa FROM DichVu";
        List<DichVu> ds = new ArrayList<>();
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int thoiGianBaoHanh = rs.getInt("ThoiGianBaoHanh");
                Integer baoHanh = rs.wasNull() ? null : thoiGianBaoHanh;
                ds.add(new DichVu(
                        rs.getString("MaDichVu"),
                        rs.getString("TenDichVu"),
                        rs.getDouble("DonGia"),
                        baoHanh,
                        rs.getString("MoTa")));
            }
        }
        return ds;
    }

    public boolean insertDichVu(DichVu dichVu) throws SQLException {
        Objects.requireNonNull(dichVu, "Dịch vụ không được null.");
        String sql = "INSERT INTO DichVu (MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh, MoTa) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, dichVu.getMaDichVu());
            stmt.setString(2, dichVu.getTenDichVu());
            stmt.setDouble(3, dichVu.getDonGia());
            if (dichVu.getThoiGianBaoHanh() == null) {
                stmt.setNull(4, Types.INTEGER);
            } else {
                stmt.setInt(4, dichVu.getThoiGianBaoHanh());
            }
            stmt.setString(5, dichVu.getMoTa());
            return stmt.executeUpdate() == 1;
        }
    }
}
