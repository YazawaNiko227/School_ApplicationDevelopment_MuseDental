package dao;

import connectDB.ConnectDB;
import entity.VatTu;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng VatTu (thuốc và vật tư y tế dùng chung bảng, phân biệt qua
 * LoaiVatTu).
 *
 * Chức năng chính: Quản lý thuốc (them / capNhat / xoa / timKiem).
 * Chức năng phụ: Kê đơn điện tử và Ghi nhận vật tư tiêu hao dùng
 * timKiem / timTheoMa / truTon.
 */
public class VatTuDAO {

    /** Giá trị cột LoaiVatTu đánh dấu thuốc. */
    public static final String LOAI_THUOC = "Thuoc";

    /**
     * Tìm kiếm vật tư theo từ khóa (mã hoặc tên) và loại.
     *
     * @param tuKhoa    rỗng/null để không lọc theo từ khóa
     * @param loaiVatTu rỗng/null để lấy cả loại (Thuoc, Tieu hao, Vat lieu)
     */
    public List<VatTu> timKiem(String tuKhoa, String loaiVatTu) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, DonGiaBan "
                        + "FROM VatTu WHERE 1 = 1");
        List<String> thamSo = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append(" AND (UPPER(MaVatTu) LIKE ? OR UPPER(TenVatTu) LIKE ?)");
            String mau = "%" + tuKhoa.trim().toUpperCase() + "%";
            thamSo.add(mau);
            thamSo.add(mau);
        }
        if (loaiVatTu != null && !loaiVatTu.trim().isEmpty()) {
            sql.append(" AND LoaiVatTu = ?");
            thamSo.add(loaiVatTu.trim());
        }

        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < thamSo.size(); i++) {
                ps.setString(i + 1, thamSo.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<VatTu> ketQua = new ArrayList<>();
                while (rs.next()) {
                    ketQua.add(docBanGhi(rs));
                }
                return ketQua;
            }
        }
    }

    /** Danh sách toàn bộ thuốc (LoaiVatTu = 'Thuoc'). */
    public List<VatTu> layDanhThuoc() throws SQLException {
        return timKiem(null, LOAI_THUOC);
    }

    /** Truy một vật tư theo mã, không tìm thấy trả null. */
    public VatTu timTheoMa(String maVatTu) throws SQLException {
        String sql = "SELECT MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, HanSuDung, "
                + "DonGiaBan FROM VatTu WHERE MaVatTu = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maVatTu);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docBanGhi(rs) : null;
            }
        }
    }

    /** Sinh mã VT### tiếp theo (qua mã lớn nhất hiện có để không trùng). */
    public String sinhMa() throws SQLException {
        int max = 0;
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("SELECT MaVatTu FROM VatTu WHERE MaVatTu LIKE 'VT%'");
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                max = Math.max(max, soThuTu(rs.getString(1)));
            }
        }
        return String.format("VT%03d", max + 1);
    }

    /** Thêm một thuốc/vật tư mới. */
    public boolean them(VatTu vt) throws SQLException {
        String sql = "INSERT INTO VatTu (MaVatTu, TenVatTu, LoaiVatTu, DonViTinh, SoLuongTon, NguongToiThieu, "
                + "HanSuDung, DonGiaBan) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ganThamSo(ps, 1, vt);
            return ps.executeUpdate() > 0;
        }
    }

    /** Cập nhật thông tin một thuốc/vật tư theo mã. */
    public boolean capNhat(VatTu vt) throws SQLException {
        String sql = "UPDATE VatTu SET TenVatTu = ?, LoaiVatTu = ?, DonViTinh = ?, SoLuongTon = ?, "
                + "NguongToiThieu = ?, HanSuDung = ?, DonGiaBan = ? WHERE MaVatTu = ?";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ganThamSo(ps, 1, vt);
            ps.setString(8, vt.getMaVatTu());
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa một thuốc/vật tư theo mã. Vật tư đã dùng trong đơn thuốc / hóa đơn /
     * phiếu nhập / phiếu xuất sẽ vướng khóa ngoại và dược từ chối ở đây.
     */
    public boolean xoa(String maVatTu) throws SQLException {
        try (PreparedStatement ps = ConnectDB.getConnection()
                .prepareStatement("DELETE FROM VatTu WHERE MaVatTu = ?")) {
            ps.setString(1, maVatTu);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Trừ tồn kho với điều kiện tồn đủ (SQL Server sẽ chặn số âm do CHECK
     * SoLuongTon >= 0; update không tác động dòng nào cũng là lỗi NHS).
     * Dùng nội bộ trong giao tác (transaction) của PhieuXuatKhoDAO.
     */
    void truTon(Connection con, String maVatTu, int soLuong) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "UPDATE VatTu SET SoLuongTon = SoLuongTon - ? WHERE MaVatTu = ? AND SoLuongTon >= ?")) {
            ps.setInt(1, soLuong);
            ps.setString(2, maVatTu);
            ps.setInt(3, soLuong);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Vật tư " + maVatTu + " không đủ tồn kho để xuất.");
            }
        }
    }

    private void ganThamSo(PreparedStatement ps, int dau, VatTu vt) throws SQLException {
        ps.setString(dau, vt.getMaVatTu());
        ps.setString(dau + 1, vt.getTenVatTu());
        ps.setString(dau + 2, vt.getLoaiVatTu());
        ps.setString(dau + 3, vt.getDonViTinh());
        ps.setInt(dau + 4, vt.getSoLuongTon());
        ps.setInt(dau + 5, vt.getNguongToiThieu());
        ps.setDate(dau + 6, vt.getHanSuDung() == null ? null : java.sql.Date.valueOf(vt.getHanSuDung()));
        ps.setDouble(dau + 7, vt.getDonGiaBan());
    }

    /* docBanGhi: chuyển một dòng ResultSet của bảng VatTu thành entity VatTu. */
    private VatTu docBanGhi(ResultSet rs) throws SQLException {
        LocalDate hanSuDung = rs.getDate("HanSuDung") == null ? null : rs.getDate("HanSuDung").toLocalDate();
        return new VatTu(
                rs.getString("MaVatTu"),
                rs.getString("TenVatTu"),
                rs.getString("LoaiVatTu"),
                rs.getString("DonViTinh"),
                rs.getInt("SoLuongTon"),
                rs.getInt("NguongToiThieu"),
                hanSuDung,
                rs.getDouble("DonGiaBan"));
    }

    private static int soThuTu(String maVatTu) {
        try {
            return Integer.parseInt(maVatTu.substring(2));
        } catch (RuntimeException saiDoi) {
            // Mã không theo định dạng VT###; bỏ qua khi tính mã lớn nhất.
            return 0;
        }
    }
}
