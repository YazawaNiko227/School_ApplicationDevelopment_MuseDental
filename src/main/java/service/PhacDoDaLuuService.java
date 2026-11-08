package service;

import dao.KeHoachDieuTriDAO;
import dao.KeHoachDieuTri_DichVuDAO;
import entity.ChiTietKeHoachDieuTriDichVu;
import entity.KeHoachDieuTri;
import java.sql.SQLException;
import java.util.List;

public class PhacDoDaLuuService {
    private final KeHoachDieuTriDAO keHoachDAO = new KeHoachDieuTriDAO();
    private final KeHoachDieuTri_DichVuDAO chiTietDAO = new KeHoachDieuTri_DichVuDAO();

    public List<KeHoachDieuTri> layDanhSach() throws SQLException {
        return keHoachDAO.layDanhSachDaLuu();
    }

    public List<ChiTietKeHoachDieuTriDichVu> layDichVu(String maKeHoach) throws SQLException {
        if (maKeHoach == null || maKeHoach.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phác đồ không hợp lệ.");
        }
        return chiTietDAO.layChiTietKieuHienThi(maKeHoach.trim());
    }

    public String trangThaiHienThi(String trangThai) {
        if ("Dang thuc hien".equals(trangThai)) {
            return "Đang thực hiện";
        }
        if ("Hoan thanh".equals(trangThai)) {
            return "Hoàn thành";
        }
        if ("Huy".equals(trangThai)) {
            return "Hủy";
        }
        return trangThai;
    }
}
