package service;

import dao.PhacDoDaLuuDAO;
import dto.DichVuPhacDoDaLuu;
import dto.PhacDoDaLuu;
import java.sql.SQLException;
import java.util.List;

public class PhacDoDaLuuService {
    private final PhacDoDaLuuDAO phacDoDaLuuDAO = new PhacDoDaLuuDAO();

    public List<PhacDoDaLuu> layDanhSach() throws SQLException {
        return phacDoDaLuuDAO.layDanhSach();
    }

    public List<DichVuPhacDoDaLuu> layDichVu(String maKeHoach) throws SQLException {
        if (maKeHoach == null || maKeHoach.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã phác đồ không hợp lệ.");
        }
        return phacDoDaLuuDAO.layDichVu(maKeHoach.trim());
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
