package dao;

import connectDB.ConnectDB;
import entity.HoaDon;
import java.sql.Date;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {
    public static final class LanKhamHoaDon {
        private final String maChanDoan;
        private final String maBenhNhan;
        private final String hoTen;
        private final String ketLuan;

        public LanKhamHoaDon(String maChanDoan, String maBenhNhan, String hoTen, String ketLuan) {
            this.maChanDoan = maChanDoan;
            this.maBenhNhan = maBenhNhan;
            this.hoTen = hoTen;
            this.ketLuan = ketLuan;
        }

        public String maChanDoan() { return maChanDoan; }
        public String maBenhNhan() { return maBenhNhan; }
        public String hoTen() { return hoTen; }
        public String ketLuan() { return ketLuan; }
    }

    public static final class DongTinhTien {
        private final String ma;
        private final String ten;
        private final int soLuong;
        private final double donGia;

        public DongTinhTien(String ma, String ten, int soLuong, double donGia) {
            this.ma = ma;
            this.ten = ten;
            this.soLuong = soLuong;
            this.donGia = donGia;
        }

        public String ma() { return ma; }
        public String ten() { return ten; }
        public int soLuong() { return soLuong; }
        public double donGia() { return donGia; }
    }

    public static final class HoaDonTraCuu {
        private final String maHoaDon;
        private final LocalDate ngayLap;
        private final String maBenhNhan;
        private final String tenBenhNhan;
        private final String maNhanVien;
        private final String tenNhanVien;
        private final String phuongThuc;
        private final String trangThai;
        private final double tongTien;

        public HoaDonTraCuu(String maHoaDon, LocalDate ngayLap, String maBenhNhan, String tenBenhNhan,
                String maNhanVien, String tenNhanVien, String phuongThuc, String trangThai, double tongTien) {
            this.maHoaDon = maHoaDon;
            this.ngayLap = ngayLap;
            this.maBenhNhan = maBenhNhan;
            this.tenBenhNhan = tenBenhNhan;
            this.maNhanVien = maNhanVien;
            this.tenNhanVien = tenNhanVien;
            this.phuongThuc = phuongThuc;
            this.trangThai = trangThai;
            this.tongTien = tongTien;
        }

        public String maHoaDon() { return maHoaDon; }
        public LocalDate ngayLap() { return ngayLap; }
        public String maBenhNhan() { return maBenhNhan; }
        public String tenBenhNhan() { return tenBenhNhan; }
        public String maNhanVien() { return maNhanVien; }
        public String tenNhanVien() { return tenNhanVien; }
        public String phuongThuc() { return phuongThuc; }
        public String trangThai() { return trangThai; }
        public double tongTien() { return tongTien; }
    }

    public static final class DotHoaDonTraCuu {
        private final String maDot;
        private final LocalDate ngayDenHan;
        private final double soTien;
        private final String trangThai;
        private final LocalDate ngayThanhToan;

        public DotHoaDonTraCuu(String maDot, LocalDate ngayDenHan, double soTien,
                String trangThai, LocalDate ngayThanhToan) {
            this.maDot = maDot;
            this.ngayDenHan = ngayDenHan;
            this.soTien = soTien;
            this.trangThai = trangThai;
            this.ngayThanhToan = ngayThanhToan;
        }

        public String maDot() { return maDot; }
        public LocalDate ngayDenHan() { return ngayDenHan; }
        public double soTien() { return soTien; }
        public String trangThai() { return trangThai; }
        public LocalDate ngayThanhToan() { return ngayThanhToan; }
    }

    /** Tra cứu hóa đơn theo từ khóa, trạng thái, phương thức và khoảng ngày. */
    public List<HoaDonTraCuu> timKiemHoaDon(String tuKhoa, String phuongThuc,
            String trangThai, LocalDate tuNgay, LocalDate denNgay) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT hd.MaHoaDon, hd.NgayLap, bn.MaBenhNhan, bn.HoTen AS TenBenhNhan, "
                        + "nv.MaNhanVien, nv.HoTen AS TenNhanVien, hd.PhuongThucThanhToan, "
                        + "hd.TrangThaiThanhToan, "
                        + "COALESCE((SELECT SUM(ct.SoLuong * ct.DonGiaTaiThoiDiem) "
                        + "FROM ChiTietHoaDon ct WHERE ct.MaHoaDon = hd.MaHoaDon), 0) + "
                        + "COALESCE((SELECT SUM(ctt.SoLuong * ctt.DonGiaTaiThoiDiem) "
                        + "FROM ChiTietHoaDonThuoc ctt WHERE ctt.MaHoaDon = hd.MaHoaDon), 0) AS TongTien "
                        + "FROM HoaDon hd "
                        + "JOIN BenhNhan bn ON bn.MaBenhNhan = hd.MaBenhNhan "
                        + "JOIN NhanVien nv ON nv.MaNhanVien = hd.MaNhanVien "
                        + "WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (tuKhoa != null && !tuKhoa.trim().isEmpty()) {
            sql.append("AND (hd.MaHoaDon LIKE ? OR bn.MaBenhNhan LIKE ? OR bn.HoTen LIKE ?) ");
            String pattern = "%" + tuKhoa.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }
        if (phuongThuc != null && !phuongThuc.trim().isEmpty()) {
            sql.append("AND hd.PhuongThucThanhToan = ? ");
            params.add(phuongThuc);
        }
        if (trangThai != null && !trangThai.trim().isEmpty()) {
            sql.append("AND hd.TrangThaiThanhToan = ? ");
            params.add(trangThai);
        }
        if (tuNgay != null) {
            sql.append("AND hd.NgayLap >= ? ");
            params.add(Date.valueOf(tuNgay));
        }
        if (denNgay != null) {
            sql.append("AND hd.NgayLap <= ? ");
            params.add(Date.valueOf(denNgay));
        }
        sql.append("ORDER BY hd.NgayLap DESC, hd.MaHoaDon DESC");

        List<HoaDonTraCuu> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new HoaDonTraCuu(
                            rs.getString("MaHoaDon"), rs.getDate("NgayLap").toLocalDate(),
                            rs.getString("MaBenhNhan"), rs.getString("TenBenhNhan"),
                            rs.getString("MaNhanVien"), rs.getString("TenNhanVien"),
                            rs.getString("PhuongThucThanhToan"), rs.getString("TrangThaiThanhToan"),
                            rs.getDouble("TongTien")));
                }
            }
        }
        return ds;
    }

    public List<DongTinhTien> layChiTietDichVuHoaDon(String maHoaDon) throws SQLException {
        String sql = "SELECT ct.MaDichVu, dv.TenDichVu, ct.SoLuong, ct.DonGiaTaiThoiDiem "
                + "FROM ChiTietHoaDon ct JOIN DichVu dv ON dv.MaDichVu = ct.MaDichVu "
                + "WHERE ct.MaHoaDon = ? ORDER BY ct.MaDichVu";
        return layChiTietTinhTien(sql, maHoaDon, "DonGiaTaiThoiDiem", true);
    }

    public List<DongTinhTien> layChiTietThuocHoaDon(String maHoaDon) throws SQLException {
        String sql = "SELECT ct.MaVatTu, vt.TenVatTu, ct.SoLuong, ct.DonGiaTaiThoiDiem "
                + "FROM ChiTietHoaDonThuoc ct JOIN VatTu vt ON vt.MaVatTu = ct.MaVatTu "
                + "WHERE ct.MaHoaDon = ? ORDER BY ct.MaVatTu";
        return layChiTietTinhTien(sql, maHoaDon, "DonGiaTaiThoiDiem", false);
    }

    private List<DongTinhTien> layChiTietTinhTien(String sql, String maHoaDon,
            String cotDonGia, boolean dichVu) throws SQLException {
        List<DongTinhTien> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DongTinhTien(
                            rs.getString(dichVu ? "MaDichVu" : "MaVatTu"),
                            rs.getString(dichVu ? "TenDichVu" : "TenVatTu"),
                            rs.getInt("SoLuong"), rs.getDouble(cotDonGia)));
                }
            }
        }
        return ds;
    }

    public List<DotHoaDonTraCuu> layDotThanhToanHoaDon(String maHoaDon) throws SQLException {
        String sql = "SELECT dt.MaDot, dt.NgayDenHan, dt.SoTienDot, dt.TrangThai, dt.NgayThanhToanThucTe "
                + "FROM CongNoTraGop cn JOIN DotThanhToan dt ON dt.MaCongNo = cn.MaCongNo "
                + "WHERE cn.MaHoaDon = ? ORDER BY dt.NgayDenHan, dt.MaDot";
        List<DotHoaDonTraCuu> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Date ngayThu = rs.getDate("NgayThanhToanThucTe");
                    ds.add(new DotHoaDonTraCuu(
                            rs.getString("MaDot"), rs.getDate("NgayDenHan").toLocalDate(),
                            rs.getDouble("SoTienDot"), rs.getString("TrangThai"),
                            ngayThu == null ? null : ngayThu.toLocalDate()));
                }
            }
        }
        return ds;
    }

    /** Lần khám đã chốt, chưa có hóa đơn, kèm thông tin bệnh nhân. */
    public List<LanKhamHoaDon> layLanKhamChuaLapHoaDon() throws SQLException {
        String sql = "SELECT cd.MaChanDoan, bn.MaBenhNhan, bn.HoTen, cd.KetLuanChanDoan "
                + "FROM ChanDoan cd "
                + "JOIN HoSoBenhAn hs ON hs.MaHoSo = cd.MaHoSo "
                + "JOIN BenhNhan bn ON bn.MaBenhNhan = hs.MaBenhNhan "
                + "WHERE cd.TrangThai = N'Da chot' "
                + "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.MaChanDoan = cd.MaChanDoan) "
                + "ORDER BY cd.NgayKham DESC, cd.MaChanDoan DESC";
        List<LanKhamHoaDon> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                ds.add(new LanKhamHoaDon(rs.getString("MaChanDoan"), rs.getString("MaBenhNhan"),
                        rs.getString("HoTen"), rs.getString("KetLuanChanDoan")));
            }
        }
        return ds;
    }

    /** Dịch vụ được ghi trong phác đồ, tính theo đơn giá hiện tại. */
    public List<DongTinhTien> layDichVuTheoChanDoan(String maChanDoan) throws SQLException {
        String sql = "SELECT dv.MaDichVu, dv.TenDichVu, khdv.SoLuong, dv.DonGia "
                + "FROM KeHoachDieuTri kh "
                + "JOIN KeHoachDieuTri_DichVu khdv ON khdv.MaKeHoach = kh.MaKeHoach "
                + "JOIN DichVu dv ON dv.MaDichVu = khdv.MaDichVu "
                + "WHERE kh.MaChanDoan = ? ORDER BY dv.MaDichVu";
        List<DongTinhTien> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DongTinhTien(rs.getString("MaDichVu"), rs.getString("TenDichVu"),
                            rs.getInt("SoLuong"), rs.getDouble("DonGia")));
                }
            }
        }
        return ds;
    }

    /** Thuốc trong đơn đã lưu, tính theo giá bán hiện tại. */
    public List<DongTinhTien> layThuocTheoChanDoan(String maChanDoan) throws SQLException {
        String sql = "SELECT vt.MaVatTu, vt.TenVatTu, ctdt.SoLuong, vt.DonGiaBan "
                + "FROM DonThuoc dt "
                + "JOIN ChiTietDonThuoc ctdt ON ctdt.MaDonThuoc = dt.MaDonThuoc "
                + "JOIN VatTu vt ON vt.MaVatTu = ctdt.MaVatTu "
                + "WHERE dt.MaChanDoan = ? ORDER BY vt.MaVatTu";
        List<DongTinhTien> ds = new ArrayList<>();
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ds.add(new DongTinhTien(rs.getString("MaVatTu"), rs.getString("TenVatTu"),
                            rs.getInt("SoLuong"), rs.getDouble("DonGiaBan")));
                }
            }
        }
        return ds;
    }

    /** Lấy mã nhân viên từ tài khoản đang đăng nhập để ghi đúng người lập hóa đơn. */
    public String layMaNhanVienTheoTenDangNhap(String tenDangNhap) throws SQLException {
        String sql = "SELECT MaNhanVien FROM TaiKhoan "
                + "WHERE TenDangNhap = ? AND TrangThaiTaiKhoan = N'Kich hoat'";
        try (PreparedStatement ps = ConnectDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MaNhanVien") : null;
            }
        }
    }

    /**
     * Lưu hóa đơn, các dòng tính tiền và lịch trả góp (nếu có) trong một transaction.
     * Với trả góp, đợt đầu được ghi nhận đã thu ngay khi lập hóa đơn.
     *
     * @return mã hóa đơn vừa tạo
     */
    public String lapHoaDon(LocalDate ngayLap, String trangThaiThanhToan, String phuongThucThanhToan,
            String maNhanVien, String maBenhNhan, String maChanDoan, int soDotTraGop) throws SQLException {
        if (ngayLap == null || maNhanVien == null || maBenhNhan == null || maChanDoan == null
                || maNhanVien.trim().isEmpty() || maBenhNhan.trim().isEmpty() || maChanDoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Thiếu dữ liệu bắt buộc để lập hóa đơn.");
        }
        boolean traGop = soDotTraGop > 0;
        boolean trangThaiTraGop = HoaDon.DANG_TRA_GOP.equals(trangThaiThanhToan);
        boolean phuongThucTraGop = "Tra gop".equals(phuongThucThanhToan);
        if (traGop != trangThaiTraGop || traGop != phuongThucTraGop) {
            throw new IllegalArgumentException("Trạng thái và phương thức thanh toán không khớp.");
        }
        if (traGop && (soDotTraGop < 2 || soDotTraGop > 12)) {
            throw new IllegalArgumentException("Số đợt trả góp phải từ 2 đến 12.");
        }
        if (!traGop && !HoaDon.DA_THANH_TOAN.equals(trangThaiThanhToan)) {
            throw new IllegalArgumentException("Hóa đơn thanh toán một lần phải ở trạng thái đã thanh toán.");
        }
        Connection con = ConnectDB.getConnection();
        boolean autoCommitCu = con.getAutoCommit();
        double tongTien;
        try {
            con.setAutoCommit(false);
            String maBenhNhanTrongChanDoan = layMaBenhNhanDaChot(con, maChanDoan);
            if (!maBenhNhan.equals(maBenhNhanTrongChanDoan)) {
                throw new SQLException("Bệnh nhân trên hóa đơn không khớp với lần khám.");
            }
            if (tonTaiHoaDon(con, maChanDoan)) {
                throw new SQLException("Lần khám này đã có hóa đơn.");
            }
            tongTien = layTongTien(con, maChanDoan);
            if (!Double.isFinite(tongTien) || tongTien <= 0) {
                throw new SQLException("Lần khám này không có khoản dịch vụ hoặc thuốc để tính tiền.");
            }
            String maHoaDon = sinhMa(con, "HoaDon", "MaHoaDon", "HD");
            themHoaDon(con, maHoaDon, ngayLap, trangThaiThanhToan, phuongThucThanhToan,
                    maNhanVien, maBenhNhan, maChanDoan);
            themChiTietDichVu(con, maHoaDon, maChanDoan);
            themChiTietThuoc(con, maHoaDon, maChanDoan);
            if (traGop) {
                taoCongNoVaDot(con, maHoaDon, tongTien, soDotTraGop, ngayLap);
            }
            con.commit();
            return maHoaDon;
        } catch (SQLException | RuntimeException e) {
            con.rollback();
            throw e;
        } finally {
            con.setAutoCommit(autoCommitCu);
        }
    }

    private static String layMaBenhNhanDaChot(Connection con, String maChanDoan) throws SQLException {
        String sql = "SELECT hs.MaBenhNhan, cd.TrangThai FROM ChanDoan cd WITH (UPDLOCK, HOLDLOCK) "
                + "JOIN HoSoBenhAn hs ON hs.MaHoSo = cd.MaHoSo WHERE cd.MaChanDoan = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Không tìm thấy lần khám " + maChanDoan + ".");
                }
                if (!"Da chot".equals(rs.getString("TrangThai"))) {
                    throw new SQLException("Chỉ được lập hóa đơn cho lần khám đã chốt.");
                }
                return rs.getString("MaBenhNhan");
            }
        }
    }

    private static boolean tonTaiHoaDon(Connection con, String maChanDoan) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                "SELECT 1 FROM HoaDon WITH (UPDLOCK, HOLDLOCK) WHERE MaChanDoan = ?")) {
            ps.setString(1, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void themHoaDon(Connection con, String maHoaDon, LocalDate ngayLap,
            String trangThai, String phuongThuc, String maNhanVien,
            String maBenhNhan, String maChanDoan) throws SQLException {
        String sql = "INSERT INTO HoaDon (MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, "
                + "MaNhanVien, MaBenhNhan, MaChanDoan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setDate(2, Date.valueOf(ngayLap));
            ps.setString(3, trangThai);
            ps.setString(4, phuongThuc);
            ps.setString(5, maNhanVien);
            ps.setString(6, maBenhNhan);
            ps.setString(7, maChanDoan);
            ps.executeUpdate();
        }
    }

    private static double layTongTien(Connection con, String maChanDoan) throws SQLException {
        String sql = "SELECT "
                + "COALESCE((SELECT SUM(khdv.SoLuong * dv.DonGia) "
                + "FROM KeHoachDieuTri kh "
                + "JOIN KeHoachDieuTri_DichVu khdv ON khdv.MaKeHoach = kh.MaKeHoach "
                + "JOIN DichVu dv ON dv.MaDichVu = khdv.MaDichVu "
                + "WHERE kh.MaChanDoan = ?), 0) + "
                + "COALESCE((SELECT SUM(ctdt.SoLuong * vt.DonGiaBan) "
                + "FROM DonThuoc dt "
                + "JOIN ChiTietDonThuoc ctdt ON ctdt.MaDonThuoc = dt.MaDonThuoc "
                + "JOIN VatTu vt ON vt.MaVatTu = ctdt.MaVatTu "
                + "WHERE dt.MaChanDoan = ?), 0) AS TongTien";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maChanDoan);
            ps.setString(2, maChanDoan);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getDouble("TongTien");
            }
        }
    }

    private static void themChiTietDichVu(Connection con, String maHoaDon, String maChanDoan)
            throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (MaHoaDon, MaDichVu, SoLuong, DonGiaTaiThoiDiem) "
                + "SELECT ?, khdv.MaDichVu, khdv.SoLuong, dv.DonGia "
                + "FROM KeHoachDieuTri kh "
                + "JOIN KeHoachDieuTri_DichVu khdv ON khdv.MaKeHoach = kh.MaKeHoach "
                + "JOIN DichVu dv ON dv.MaDichVu = khdv.MaDichVu "
                + "WHERE kh.MaChanDoan = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maChanDoan);
            ps.executeUpdate();
        }
    }

    private static void themChiTietThuoc(Connection con, String maHoaDon, String maChanDoan)
            throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDonThuoc (MaHoaDon, MaVatTu, SoLuong, DonGiaTaiThoiDiem) "
                + "SELECT ?, ctdt.MaVatTu, ctdt.SoLuong, vt.DonGiaBan "
                + "FROM DonThuoc dt "
                + "JOIN ChiTietDonThuoc ctdt ON ctdt.MaDonThuoc = dt.MaDonThuoc "
                + "JOIN VatTu vt ON vt.MaVatTu = ctdt.MaVatTu "
                + "WHERE dt.MaChanDoan = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHoaDon);
            ps.setString(2, maChanDoan);
            ps.executeUpdate();
        }
    }

    private static void taoCongNoVaDot(Connection con, String maHoaDon, double tongTien,
            int soDot, LocalDate ngayLap) throws SQLException {
        String maCongNo = sinhMa(con, "CongNoTraGop", "MaCongNo", "CN");
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO CongNoTraGop (MaCongNo, TongSoTien, SoDotConLai, MaHoaDon) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, maCongNo);
            ps.setDouble(2, tongTien);
            ps.setInt(3, soDot - 1);
            ps.setString(4, maHoaDon);
            ps.executeUpdate();
        }

        double soTienMoiDot = Math.floor(tongTien / soDot);
        try (PreparedStatement ps = con.prepareStatement(
                "INSERT INTO DotThanhToan (MaDot, NgayDenHan, SoTienDot, TrangThai, "
                        + "NgayThanhToanThucTe, MaCongNo) VALUES (?, ?, ?, ?, ?, ?)")) {
            for (int i = 0; i < soDot; i++) {
                double soTienDot = i == soDot - 1
                        ? tongTien - soTienMoiDot * (soDot - 1)
                        : soTienMoiDot;
                ps.setString(1, sinhMa(con, "DotThanhToan", "MaDot", "DOT"));
                ps.setDate(2, Date.valueOf(ngayLap.plusMonths(i)));
                ps.setDouble(3, soTienDot);
                ps.setString(4, i == 0 ? "Da thanh toan" : "Chua den han");
                if (i == 0) {
                    ps.setDate(5, Date.valueOf(ngayLap));
                } else {
                    ps.setNull(5, java.sql.Types.DATE);
                }
                ps.setString(6, maCongNo);
                ps.executeUpdate();
            }
        }
    }

    private static String sinhMa(Connection con, String bang, String cot, String tienTo) throws SQLException {
        String sql = "SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(" + cot
                + ", LEN(?) + 1, 18))), 0) + 1 FROM " + bang + " WITH (UPDLOCK, HOLDLOCK) "
                + "WHERE " + cot + " LIKE ? AND TRY_CONVERT(int, SUBSTRING(" + cot + ", LEN(?) + 1, 18)) IS NOT NULL";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, tienTo);
            ps.setString(2, tienTo + "%");
            ps.setString(3, tienTo);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return String.format("%s%03d", tienTo, rs.getInt(1));
            }
        }
    }

    public boolean lapHoaDon(String maHD, LocalDate ngayLap, String trangThaiThanhToan,
            String pttt, String maNV, String maBN, String maChanDoan) throws SQLException {
        String sql = "INSERT INTO HoaDon "
                + "(MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan, "
                + "MaNhanVien, MaBenhNhan, MaChanDoan) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, maHD);
            stmt.setDate(2, Date.valueOf(ngayLap));
            stmt.setString(3, trangThaiThanhToan);
            stmt.setString(4, pttt);
            stmt.setString(5, maNV);
            stmt.setString(6, maBN);
            stmt.setString(7, maChanDoan);
            return stmt.executeUpdate() == 1;
        }
    }

    public List<String[]> traCuuHoaDon(String tuKhoa) throws SQLException {
        String sql = "SELECT hd.MaHoaDon, bn.HoTen, hd.NgayLap, "
                + "COALESCE((SELECT SUM(ct.SoLuong * ct.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDon ct WHERE ct.MaHoaDon = hd.MaHoaDon), 0) "
                + "+ COALESCE((SELECT SUM(ctt.SoLuong * ctt.DonGiaTaiThoiDiem) "
                + "FROM ChiTietHoaDonThuoc ctt WHERE ctt.MaHoaDon = hd.MaHoaDon), 0) AS ThanhTien "
                + "FROM HoaDon hd LEFT JOIN BenhNhan bn ON hd.MaBenhNhan = bn.MaBenhNhan "
                + "WHERE hd.MaHoaDon LIKE ? OR bn.MaBenhNhan LIKE ? OR bn.HoTen LIKE ?";
        String keyword = tuKhoa == null ? "" : tuKhoa.trim();
        String pattern = "%" + keyword + "%";
        List<String[]> ds = new ArrayList<>();
        try (PreparedStatement stmt = ConnectDB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            stmt.setString(3, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(new String[] {
                            rs.getString("MaHoaDon"),
                            rs.getString("HoTen"),
                            rs.getString("NgayLap"),
                            String.valueOf(rs.getDouble("ThanhTien"))
                    });
                }
            }
        }
        return ds;
    }
}
