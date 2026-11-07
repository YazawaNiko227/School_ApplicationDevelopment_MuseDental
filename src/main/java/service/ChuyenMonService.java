package service;

import dao.ChuyenMonDAO;
import entity.ChuyenMon;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Nghiệp vụ & kiểm tra dữ liệu cho chuyên môn. Lỗi nghiệp vụ ném
 * IllegalArgumentException.
 */
public class ChuyenMonService {
    private static final int MAX_TEN = 100;

    private final ChuyenMonDAO chuyenMonDAO = new ChuyenMonDAO();

    public List<ChuyenMon> layTatCa() throws SQLException {
        return chuyenMonDAO.layTatCa();
    }

    public Map<String, Integer> demNhanVienTheoChuyenMon() throws SQLException {
        return chuyenMonDAO.demNhanVienTheoChuyenMon();
    }

    /** Sinh mã dạng CM### kế tiếp (CM001, CM002...). */
    public String sinhMa(List<ChuyenMon> danhSach) {
        int max = 0;
        for (ChuyenMon chuyenMon : danhSach) {
            String ma = chuyenMon.getMaChuyenMon();
            if (ma != null && ma.toUpperCase().startsWith("CM")) {
                try {
                    max = Math.max(max, Integer.parseInt(ma.substring(2)));
                } catch (NumberFormatException ignored) {
                    // Bỏ qua mã không theo định dạng CM###.
                }
            }
        }
        return String.format("CM%03d", max + 1);
    }

    /** Kiểm tra dữ liệu nhập và dựng đối tượng ChuyenMon. */
    public ChuyenMon taoChuyenMon(String ma, String ten) {
        ma = trim(ma);
        ten = trim(ten);
        if (ma.isEmpty()) {
            throw new IllegalArgumentException("Mã chuyên môn không được để trống.");
        }
        if (ma.length() > 20) {
            throw new IllegalArgumentException("Mã chuyên môn tối đa 20 ký tự.");
        }
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên chuyên môn không được để trống.");
        }
        if (ten.length() > MAX_TEN) {
            throw new IllegalArgumentException("Tên chuyên môn tối đa " + MAX_TEN + " ký tự.");
        }
        return new ChuyenMon(ma, ten);
    }

    public boolean them(ChuyenMon chuyenMon) throws SQLException {
        if (chuyenMonDAO.timTheoMa(chuyenMon.getMaChuyenMon()) != null) {
            throw new IllegalArgumentException("Mã chuyên môn " + chuyenMon.getMaChuyenMon() + " đã tồn tại.");
        }
        if (chuyenMonDAO.timTheoTen(chuyenMon.getTenChuyenMon()) != null) {
            throw new IllegalArgumentException("Tên chuyên môn \"" + chuyenMon.getTenChuyenMon() + "\" đã tồn tại.");
        }
        return chuyenMonDAO.them(chuyenMon);
    }

    public boolean capNhat(ChuyenMon chuyenMon) throws SQLException {
        if (chuyenMonDAO.timTheoMa(chuyenMon.getMaChuyenMon()) == null) {
            throw new IllegalArgumentException("Không tìm thấy chuyên môn cần cập nhật.");
        }
        ChuyenMon trungTen = chuyenMonDAO.timTheoTen(chuyenMon.getTenChuyenMon());
        if (trungTen != null && !trungTen.getMaChuyenMon().equalsIgnoreCase(chuyenMon.getMaChuyenMon())) {
            throw new IllegalArgumentException("Tên chuyên môn \"" + chuyenMon.getTenChuyenMon() + "\" đã tồn tại.");
        }
        return chuyenMonDAO.capNhat(chuyenMon);
    }

    /** Chỉ cho xóa khi chưa có nhân viên nào thuộc chuyên môn này. */
    public boolean xoa(String ma) throws SQLException {
        int soNhanVien = chuyenMonDAO.demNhanVien(ma);
        if (soNhanVien > 0) {
            throw new IllegalArgumentException("Không thể xóa: đang có " + soNhanVien
                    + " nhân viên thuộc chuyên môn này.\nHãy chuyển các nhân viên sang chuyên môn khác trước.");
        }
        return chuyenMonDAO.xoa(ma);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}