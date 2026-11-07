package service;

import dao.PhanQuyenDAO;
import entity.VaiTro;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.PhanQuyenMenu;

public class PhanQuyenRbacService {
    private final PhanQuyenDAO phanQuyenDAO = new PhanQuyenDAO();

    public void khoiTaoQuyenMacDinhNeuChuaCo() throws SQLException {
        phanQuyenDAO.khoiTaoQuyenMacDinhNeuChuaCo();
    }

    public List<VaiTro> layDanhSachVaiTro() throws SQLException {
        return phanQuyenDAO.layDanhSachVaiTro();
    }

    public Map<String, Set<String>> layTatCaQuyen() throws SQLException {
        return phanQuyenDAO.layTatCaQuyen();
    }

    public void capNhatTatCaQuyen(Map<String, Set<String>> permissions) throws SQLException {
        phanQuyenDAO.capNhatTatCaQuyen(permissions);
    }

    public boolean coQuyenCapNhat(String maVaiTro) {
        return maVaiTro == null || PhanQuyenMenu.QUAN_TRI.equalsIgnoreCase(maVaiTro);
    }
}
