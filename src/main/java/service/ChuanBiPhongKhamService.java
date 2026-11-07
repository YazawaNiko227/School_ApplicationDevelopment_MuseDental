package service;

import dao.PhongKhamDAO;
import entity.PhongKham;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ChuanBiPhongKhamService {
    public static final String SAN_SANG = "Sẵn sàng";
    public static final String DANG_KHAM = "Đang khám";
    public static final String CAN_VE_SINH = "Cần vệ sinh";
    public static final String BAO_TRI = "Bảo trì";

    private final PhongKhamDAO phongKhamDAO = new PhongKhamDAO();

    public List<PhongKham> layTatCaPhong() throws SQLException {
        return phongKhamDAO.layTatCa();
    }

    public Map<String, String> layLichHenKeTiep() throws SQLException {
        return phongKhamDAO.layLichHenKeTiep();
    }

    public String trangThaiHienThi(String trangThai) {
        if ("San sang".equals(trangThai)) return SAN_SANG;
        if ("Dang kham".equals(trangThai)) return DANG_KHAM;
        if ("Can ve sinh".equals(trangThai)) return CAN_VE_SINH;
        if ("Bao tri".equals(trangThai)) return BAO_TRI;
        throw new IllegalArgumentException("Trạng thái phòng khám không hợp lệ: " + trangThai);
    }

    public boolean capNhatTrangThai(String maPhong, String trangThaiHienThi) throws SQLException {
        if (maPhong == null || maPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phòng không được để trống.");
        }
        String trangThaiDb = trangThaiTrongDb(trangThaiHienThi);
        return phongKhamDAO.capNhatTrangThai(maPhong, trangThaiDb);
    }

    private String trangThaiTrongDb(String trangThai) {
        if (SAN_SANG.equals(trangThai)) return "San sang";
        if (DANG_KHAM.equals(trangThai)) return "Dang kham";
        if (CAN_VE_SINH.equals(trangThai)) return "Can ve sinh";
        if (BAO_TRI.equals(trangThai)) return "Bao tri";
        throw new IllegalArgumentException("Trạng thái phòng khám không hợp lệ: " + trangThai);
    }
}
