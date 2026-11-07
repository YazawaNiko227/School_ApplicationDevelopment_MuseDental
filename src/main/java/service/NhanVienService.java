package service;

import dao.NhanVienDAO;
import entity.ChuyenMon;
import entity.NhanVien;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class NhanVienService {
    public static final String TT_DANG_LAM = "Dang lam viec";
    public static final String TT_TAM_NGHI = "Tam nghi";
    public static final String TT_DA_NGHI = "Da nghi viec";
    public static final String TT_DANG_LAM_HIEN_THI = "Đang làm việc";
    public static final String TT_TAM_NGHI_HIEN_THI = "Tạm nghỉ";
    public static final String TT_DA_NGHI_HIEN_THI = "Đã nghỉ việc";

    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    public List<NhanVien> layTatCa() throws SQLException {
        return nhanVienDAO.layTatCa();
    }

    public NhanVien timTheoMa(String ma) throws SQLException {
        return nhanVienDAO.timTheoMa(ma);
    }

    public String sinhMa(List<NhanVien> danhSach) {
        int max = 0;
        for (NhanVien nhanVien : danhSach) {
            String ma = nhanVien.getMaNhanVien();
            if (ma != null && ma.toUpperCase().startsWith("NV")) {
                try {
                    max = Math.max(max, Integer.parseInt(ma.substring(2)));
                } catch (NumberFormatException ignored) {
                    // Bỏ qua mã không theo định dạng NV###.
                }
            }
        }
        return String.format("NV%03d", max + 1);
    }

    public NhanVien taoNhanVien(String ma, String hoTen, LocalDate ngaySinh, String gioiTinh,
            String soDienThoai, String email, String diaChi, LocalDate ngayVaoLam,
            String trangThaiHienThi, ChuyenMon chuyenMon, String chungChi) {
        hoTen = trim(hoTen);
        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (hoTen.length() > 100) {
            throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        }
        if (ngaySinh != null && !ngaySinh.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh phải nhỏ hơn ngày hiện tại.");
        }
        if (ngaySinh != null && ngayVaoLam != null && ngayVaoLam.isBefore(ngaySinh)) {
            throw new IllegalArgumentException("Ngày vào làm không được trước ngày sinh.");
        }

        soDienThoai = trim(soDienThoai);
        if (soDienThoai.isEmpty() || !PHONE_PATTERN.matcher(soDienThoai).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.");
        }
        email = trim(email);
        if (!email.isEmpty() && (email.length() > 100 || !EMAIL_PATTERN.matcher(email).matches())) {
            throw new IllegalArgumentException("Email không đúng định dạng.");
        }
        diaChi = trim(diaChi);
        if (diaChi.length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }

        chungChi = trim(chungChi);
        String tenChuyenMon = chuyenMon == null ? "" : trim(chuyenMon.getTenChuyenMon());
        String lowerChuyenMon = tenChuyenMon.toLowerCase();
        if ((lowerChuyenMon.startsWith("nha sĩ") || lowerChuyenMon.startsWith("nha si"))
                && chungChi.isEmpty()) {
            throw new IllegalArgumentException("Nha sĩ bắt buộc nhập số chứng chỉ hành nghề.");
        }
        if (chungChi.length() > 50) {
            throw new IllegalArgumentException("Số chứng chỉ hành nghề tối đa 50 ký tự.");
        }

        return new NhanVien(trim(ma), hoTen, ngaySinh, gioiTinh, soDienThoai,
                emptyToNull(email), emptyToNull(diaChi), ngayVaoLam,
                trangThaiTrongDb(trangThaiHienThi), chuyenMon, emptyToNull(chungChi));
    }

    public boolean them(NhanVien nhanVien) throws SQLException {
        if (nhanVienDAO.timTheoMa(nhanVien.getMaNhanVien()) != null) {
            throw new IllegalArgumentException("Mã nhân viên " + nhanVien.getMaNhanVien() + " đã tồn tại.");
        }
        return nhanVienDAO.them(nhanVien);
    }

    public boolean capNhat(NhanVien nhanVien) throws SQLException {
        if (nhanVienDAO.timTheoMa(nhanVien.getMaNhanVien()) == null) {
            throw new IllegalArgumentException("Không tìm thấy nhân viên cần cập nhật.");
        }
        return nhanVienDAO.capNhat(nhanVien);
    }

    public boolean xoa(String ma) throws SQLException {
        return nhanVienDAO.xoa(ma);
    }

    public boolean capNhatTrangThai(String ma, String trangThai) throws SQLException {
        return nhanVienDAO.capNhatTrangThai(ma, trangThai);
    }

    public String hienThiTrangThai(String trangThaiDb) {
        if (TT_DANG_LAM.equals(trangThaiDb)) {
            return TT_DANG_LAM_HIEN_THI;
        }
        if (TT_TAM_NGHI.equals(trangThaiDb)) {
            return TT_TAM_NGHI_HIEN_THI;
        }
        if (TT_DA_NGHI.equals(trangThaiDb)) {
            return TT_DA_NGHI_HIEN_THI;
        }
        return trangThaiDb == null ? "" : trangThaiDb;
    }

    private static String trangThaiTrongDb(String trangThaiHienThi) {
        if (TT_DANG_LAM_HIEN_THI.equals(trangThaiHienThi)) {
            return TT_DANG_LAM;
        }
        if (TT_TAM_NGHI_HIEN_THI.equals(trangThaiHienThi)) {
            return TT_TAM_NGHI;
        }
        if (TT_DA_NGHI_HIEN_THI.equals(trangThaiHienThi)) {
            return TT_DA_NGHI;
        }
        return trangThaiHienThi;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
