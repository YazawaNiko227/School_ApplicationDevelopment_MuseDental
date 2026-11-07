package service;

import dao.DieuPhoiPhongKhamDAO;
import entity.LichHenDieuPhoi;
import entity.PhongKham;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Nghiệp vụ điều phối lịch hẹn và phòng khám. */
public class DieuPhoiPhongKhamService {
    public static final String CHO_XAC_NHAN = "Chờ xác nhận";
    public static final String DA_XAC_NHAN = "Đã xác nhận";
    public static final String DA_CHECK_IN = "Đã check-in";
    public static final String DANG_KHAM = "Đang khám";
    public static final String HOAN_THANH = "Hoàn thành";
    public static final String HUY = "Hủy";
    public static final String VANG = "Vắng";
    public static final String SAN_SANG = "Sẵn sàng";
    public static final String CAN_VE_SINH = "Cần vệ sinh";
    public static final String BAO_TRI = "Bảo trì";

    private final DieuPhoiPhongKhamDAO dieuPhoiDAO;

    public DieuPhoiPhongKhamService() {
        this(new DieuPhoiPhongKhamDAO());
    }

    DieuPhoiPhongKhamService(DieuPhoiPhongKhamDAO dieuPhoiDAO) {
        if (dieuPhoiDAO == null) {
            throw new IllegalArgumentException("DAO điều phối không được null.");
        }
        this.dieuPhoiDAO = dieuPhoiDAO;
    }

    public List<LichHenDieuPhoi> layLichTheoNgay(LocalDate ngay) throws SQLException {
        if (ngay == null) {
            throw new IllegalArgumentException("Ngày điều phối không được để trống.");
        }
        return dieuPhoiDAO.layLichTheoNgay(ngay);
    }

    public List<PhongKham> layTatCaPhong() throws SQLException {
        return dieuPhoiDAO.layTatCaPhong();
    }

    public boolean checkIn(String maLichHen, String trangThaiHienThi) throws SQLException {
        if (CHO_XAC_NHAN.equals(trangThaiHienThi)) {
            throw new IllegalArgumentException(
                    "Lịch này chưa được xác nhận. Hãy xác nhận ở màn hình Đặt lịch hẹn khám trước.");
        }
        if (!DA_XAC_NHAN.equals(trangThaiHienThi)) {
            throw new IllegalArgumentException("Chỉ check-in được lịch đang ở trạng thái \"Đã xác nhận\".");
        }
        return dieuPhoiDAO.checkIn(maLichHen);
    }

    public boolean doiPhong(String maLichHen, String trangThaiHienThi, String maPhong) throws SQLException {
        if (!DA_CHECK_IN.equals(trangThaiHienThi)) {
            throw new IllegalArgumentException("Chỉ điều phối được bệnh nhân đang ở trạng thái \"Đã check-in\".");
        }
        if (isBlank(maPhong)) {
            throw new IllegalArgumentException("Vui lòng chọn phòng khám.");
        }
        return dieuPhoiDAO.doiPhong(maLichHen, maPhong);
    }

    public boolean danhDauVang(String maLichHen, String trangThaiHienThi) throws SQLException {
        String trangThaiTrongDb;
        if (CHO_XAC_NHAN.equals(trangThaiHienThi)) {
            trangThaiTrongDb = "Cho xac nhan";
        } else if (DA_XAC_NHAN.equals(trangThaiHienThi)) {
            trangThaiTrongDb = "Da xac nhan";
        } else {
            throw new IllegalArgumentException("Chỉ đánh dấu vắng được lịch chưa check-in.");
        }
        return dieuPhoiDAO.danhDauVang(maLichHen, trangThaiTrongDb);
    }

    public String trangThaiLichHienThi(String trangThai) {
        switch (trangThai) {
            case "Cho xac nhan": return CHO_XAC_NHAN;
            case "Da xac nhan": return DA_XAC_NHAN;
            case "Da check-in": return DA_CHECK_IN;
            case "Dang kham": return DANG_KHAM;
            case "Hoan thanh": return HOAN_THANH;
            case "Huy": return HUY;
            case "Vang": return VANG;
            default: throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ: " + trangThai);
        }
    }

    public String trangThaiPhongHienThi(String trangThai) {
        switch (trangThai) {
            case "San sang": return SAN_SANG;
            case "Dang kham": return DANG_KHAM;
            case "Can ve sinh": return CAN_VE_SINH;
            case "Bao tri": return BAO_TRI;
            default: throw new IllegalArgumentException("Trạng thái phòng không hợp lệ: " + trangThai);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
