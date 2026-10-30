/* trinhKiet */
package dao;

import connectDB.ConnectDB;
import entity.DichVu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    public List<DichVu> getAllDichVu() {
        List<DichVu> ds = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return ds;

        String sql = "SELECT * FROM DichVu";
        try {
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                DichVu dv = new DichVu();
                dv.setMaDichVu(rs.getString("maDichVu"));
                dv.setTenDichVu(rs.getString("tenDichVu"));
                dv.setDonGia(rs.getDouble("donGia"));
                ds.add(dv);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean insertDichVu(String maDV, String tenDV, double donGia, String donViTinh, String moTa,
            boolean trangThai) {
        Connection con = ConnectDB.getConnection();
        if (con == null)
            return false;

        String sql = "INSERT INTO DichVu (maDichVu, tenDichVu, donGia, donViTinh, moTa, trangThai) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maDV);
            stmt.setString(2, tenDV);
            stmt.setDouble(3, donGia);
            stmt.setString(4, donViTinh);
            stmt.setString(5, moTa);
            stmt.setBoolean(6, trangThai);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
/* end trinhKiet */