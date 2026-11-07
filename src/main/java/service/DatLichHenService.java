package service;

import dao.DatLichHenDAO;
import entity.BenhNhan;
import entity.LichHenThongTin;
import entity.NhanVien;
import entity.PhongKham;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class DatLichHenService {
    public static final String TAT_CA = "Tất cả";
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_CHECK_IN = "Đã check-in";
    public static final String DANG_KHAM = "Đang khám";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String HUY = "Hủy";
    public static final String VANG = "Vắng";

    private static final String DB_CHO_XAC_NHAN = "Cho xac nhan";
    private static final String DB_DA_XAC_NHAN = "Da xac nhan";
    private static final String DB_DA_CHECK_IN = "Da check-in";
    private static final String DB_DANG_KHAM = "Dang kham";
    private static final String DB_HOAN_THANH = "Hoan thanh";
    private static final String DB_HUY = "Huy";
    private static final String DB_VANG = "Vang";

    private final DatLichHenDAO datLichHenDAO;

    public DatLichHenService() {
        this(new DatLichHenDAO());
    }

    DatLichHenService(DatLichHenDAO datLichHenDAO) {
        if (datLichHenDAO == null) {
            throw new IllegalArgumentException("DAO đặt lịch không được null.");
        }
        this.datLichHenDAO = datLichHenDAO;
    }

    public List<BenhNhan> timBenhNhan(String tuKhoa) throws SQLException {
        if (tuKhoa == null || tuKhoa.trim().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập mã, họ tên hoặc số điện thoại để tìm.");
        }
        return datLichHenDAO.timBenhNhan(tuKhoa.trim());
    }

    public List<NhanVien> layNhaSiDangLamViec() throws SQLException {
        return datLichHenDAO.layNhaSiDangLamViec();
    }

    public List<NhanVien> layNhaSiRanh(LocalDateTime thoiGianHen) throws SQLException {
        if (thoiGianHen == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày và giờ hẹn.");
        }
        return datLichHenDAO.layNhaSiRanh(thoiGianHen);
    }

    public List<PhongKham> layPhongKhongBaoTri() throws SQLException {
        return datLichHenDAO.layPhongKhongBaoTri();
    }

    public List<LichHenThongTin> layDanhSachLichHen() throws SQLException {
        return datLichHenDAO.layDanhSachLichHen();
    }

    public String datLichHen(BenhNhan benhNhanMoi, String maBenhNhan, LocalDateTime thoiGianHen,
            String maNhaSi, String maPhong, String tenDangNhap) throws SQLException {
        if (benhNhanMoi == null && (maBenhNhan == null || !maBenhNhan.trim().matches("BN\\d+"))) {
            throw new IllegalArgumentException("Vui lòng tìm bệnh nhân hoặc chọn \"Bệnh nhân mới\".");
        }
        if (benhNhanMoi != null
                && (isBlank(benhNhanMoi.getHoTen()) || isBlank(benhNhanMoi.getSoDienThoai()))) {
            throw new IllegalArgumentException("Bệnh nhân mới cần nhập tối thiểu họ tên và số điện thoại.");
        }
        if (thoiGianHen == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày hẹn từ lịch.");
        }
        if (thoiGianHen.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Thời gian hẹn phải sau thời điểm hiện tại.");
        }
        if (isBlank(maNhaSi) || isBlank(maPhong)) {
            throw new IllegalArgumentException("Không có nha sĩ hoặc phòng khám phù hợp. Vui lòng kiểm tra danh mục.");
        }
        return datLichHenDAO.themLichHen(benhNhanMoi,
                benhNhanMoi == null ? maBenhNhan.trim() : null,
                thoiGianHen, maNhaSi, maPhong, tenDangNhap);
    }

    public boolean xacNhanLichHen(String maLichHen, String trangThaiHienThi) throws SQLException {
        if (!CHO_XAC_NHAN.equals(trangThaiHienThi)) {
            throw new IllegalArgumentException("Chỉ xác nhận được lịch đang ở trạng thái \"Chờ xác nhận\".");
        }
        return datLichHenDAO.capNhatTrangThai(maLichHen, DB_CHO_XAC_NHAN, DB_DA_XAC_NHAN);
    }

    public boolean coTheHuy(String trangThaiHienThi) {
        return CHO_XAC_NHAN.equals(trangThaiHienThi) || DA_XAC_NHAN.equals(trangThaiHienThi);
    }

    public boolean huyLichHen(String maLichHen, String trangThaiHienThi) throws SQLException {
        String trangThaiTrongDb;
        if (CHO_XAC_NHAN.equals(trangThaiHienThi)) {
            trangThaiTrongDb = DB_CHO_XAC_NHAN;
        } else if (DA_XAC_NHAN.equals(trangThaiHienThi)) {
            trangThaiTrongDb = DB_DA_XAC_NHAN;
        } else {
            throw new IllegalArgumentException("Chỉ hủy được lịch \"Chờ xác nhận\" hoặc \"Đã xác nhận\".");
        }
        return datLichHenDAO.capNhatTrangThai(maLichHen, trangThaiTrongDb, DB_HUY);
    }

    public String trangThaiHienThi(String trangThaiTrongDb) {
        switch (trangThaiTrongDb) {
            case DB_CHO_XAC_NHAN:
                return CHO_XAC_NHAN;
            case DB_DA_XAC_NHAN:
                return DA_XAC_NHAN;
            case DB_DA_CHECK_IN:
                return DA_CHECK_IN;
            case DB_DANG_KHAM:
                return DANG_KHAM;
            case DB_HOAN_THANH:
                return HOAN_THANH;
            case DB_HUY:
                return HUY;
            case DB_VANG:
                return VANG;
            default:
                throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ: " + trangThaiTrongDb);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
