package service;

import dao.DangNhapDAO;
import dao.PhanQuyenDAO;
import entity.TaiKhoan;
import java.sql.SQLException;
import java.util.Set;

public class LoginService {
    public enum ValidationError {
        NONE,
        USERNAME_REQUIRED,
        PASSWORD_REQUIRED
    }

    private final DangNhapDAO dangNhapDAO = new DangNhapDAO();
    private final PhanQuyenDAO phanQuyenDAO = new PhanQuyenDAO();

    public ValidationError validateCredentials(String username, char[] password) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationError.USERNAME_REQUIRED;
        }
        if (password == null || password.length == 0) {
            return ValidationError.PASSWORD_REQUIRED;
        }
        return ValidationError.NONE;
    }

    public TaiKhoan dangNhap(String username, char[] password) throws SQLException {
        return dangNhapDAO.dangNhap(username, password);
    }

    public Set<String> layChucNangDuocXem(TaiKhoan account) throws SQLException {
        if (account == null || account.getVaiTro() == null) {
            throw new IllegalArgumentException("Tài khoản đăng nhập không hợp lệ.");
        }
        phanQuyenDAO.khoiTaoQuyenMacDinhNeuChuaCo();
        return phanQuyenDAO.layChucNangDuocXem(account.getVaiTro().getMaVaiTro());
    }
}
