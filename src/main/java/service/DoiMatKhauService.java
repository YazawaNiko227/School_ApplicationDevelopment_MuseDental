package service;

import dao.DoiMatKhauDAO;
import java.sql.SQLException;
import java.util.Arrays;

public class DoiMatKhauService {
    public enum TruongLoi {
        MAT_KHAU_HIEN_TAI,
        MAT_KHAU_MOI,
        XAC_NHAN
    }

    public static class LoiDuLieu extends IllegalArgumentException {
        private final TruongLoi truong;

        public LoiDuLieu(String message, TruongLoi truong) {
            super(message);
            this.truong = truong;
        }

        public TruongLoi getTruong() {
            return truong;
        }
    }

    private final DoiMatKhauDAO doiMatKhauDAO = new DoiMatKhauDAO();

    public boolean doiMatKhau(String username, char[] current, char[] next, char[] confirmation)
            throws SQLException {
        if (current == null || current.length == 0) {
            throw new LoiDuLieu("Vui lòng nhập mật khẩu hiện tại.", TruongLoi.MAT_KHAU_HIEN_TAI);
        }
        if (next == null || next.length < 8) {
            throw new LoiDuLieu("Mật khẩu mới phải có ít nhất 8 ký tự.", TruongLoi.MAT_KHAU_MOI);
        }
        if (Arrays.equals(current, next)) {
            throw new LoiDuLieu("Mật khẩu mới phải khác mật khẩu hiện tại.", TruongLoi.MAT_KHAU_MOI);
        }
        if (!Arrays.equals(next, confirmation)) {
            throw new LoiDuLieu("Mật khẩu xác nhận không khớp.", TruongLoi.XAC_NHAN);
        }
        return doiMatKhauDAO.doiMatKhau(username, current, next);
    }
}
