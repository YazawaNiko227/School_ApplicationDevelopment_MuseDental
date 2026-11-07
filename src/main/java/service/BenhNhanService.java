package service;

import dao.BenhNhanDAO;
import entity.BenhNhan;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.regex.Pattern;

public class BenhNhanService {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");
    private static final Pattern CCCD_PATTERN = Pattern.compile("^\\d{12}$");

    private final BenhNhanDAO benhNhanDAO = new BenhNhanDAO();

    public List<BenhNhan> layTatCa() throws SQLException {
        return benhNhanDAO.layTatCa();
    }

    public BenhNhan timTheoMa(String ma) throws SQLException {
        return benhNhanDAO.timTheoMa(ma);
    }

    public String sinhMa(List<BenhNhan> danhSach) {
        int max = 0;
        for (BenhNhan benhNhan : danhSach) {
            try {
                max = Math.max(max, Integer.parseInt(benhNhan.getMaBenhNhan().substring(2)));
            } catch (RuntimeException ignored) {
                // Bỏ qua mã không theo định dạng BN###.
            }
        }
        return String.format("BN%03d", max + 1);
    }

    public BenhNhan taoBenhNhan(String ma, String hoTen, LocalDate ngaySinh, String gioiTinh,
            String soDienThoai, String soCCCD, String diaChi, String nguoiBaoHo,
            String tienSuBenhLy, String thongTinDiUng) {
        hoTen = trim(hoTen);
        if (hoTen.isEmpty()) {
            throw new IllegalArgumentException("Họ tên không được để trống.");
        }
        if (hoTen.length() > 100) {
            throw new IllegalArgumentException("Họ tên tối đa 100 ký tự.");
        }
        if (ngaySinh != null && ngaySinh.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Ngày sinh không được sau ngày hiện tại.");
        }

        soDienThoai = trim(soDienThoai);
        if (!PHONE_PATTERN.matcher(soDienThoai).matches()) {
            throw new IllegalArgumentException("Số điện thoại phải bắt đầu bằng 0 và có 10-11 chữ số.");
        }
        soCCCD = trim(soCCCD);
        if (!soCCCD.isEmpty() && !CCCD_PATTERN.matcher(soCCCD).matches()) {
            throw new IllegalArgumentException("Số CCCD phải gồm đúng 12 chữ số.");
        }

        diaChi = trim(diaChi);
        if (diaChi.length() > 200) {
            throw new IllegalArgumentException("Địa chỉ tối đa 200 ký tự.");
        }
        nguoiBaoHo = trim(nguoiBaoHo);
        if (ngaySinh != null && Period.between(ngaySinh, LocalDate.now()).getYears() < 18
                && nguoiBaoHo.isEmpty()) {
            throw new IllegalArgumentException("Bệnh nhân dưới 18 tuổi bắt buộc nhập người bảo hộ.");
        }
        if (nguoiBaoHo.length() > 100) {
            throw new IllegalArgumentException("Người bảo hộ tối đa 100 ký tự.");
        }

        tienSuBenhLy = trim(tienSuBenhLy);
        if (tienSuBenhLy.length() > 1000) {
            throw new IllegalArgumentException("Tiền sử bệnh lý tối đa 1000 ký tự.");
        }
        thongTinDiUng = trim(thongTinDiUng);
        if (thongTinDiUng.length() > 500) {
            throw new IllegalArgumentException("Thông tin dị ứng tối đa 500 ký tự.");
        }

        return new BenhNhan(trim(ma), hoTen, ngaySinh, gioiTinh, soDienThoai,
                emptyToNull(diaChi), emptyToNull(soCCCD), emptyToNull(tienSuBenhLy),
                emptyToNull(thongTinDiUng), emptyToNull(nguoiBaoHo));
    }

    public boolean them(BenhNhan benhNhan) throws SQLException {
        if (benhNhanDAO.timTheoMa(benhNhan.getMaBenhNhan()) != null) {
            throw new IllegalArgumentException("Mã bệnh nhân đã tồn tại.");
        }
        return benhNhanDAO.them(benhNhan);
    }

    public boolean capNhat(BenhNhan benhNhan) throws SQLException {
        if (benhNhanDAO.timTheoMa(benhNhan.getMaBenhNhan()) == null) {
            throw new IllegalArgumentException("Không tìm thấy bệnh nhân cần cập nhật.");
        }
        return benhNhanDAO.capNhat(benhNhan);
    }

    public boolean xoa(String ma) throws SQLException {
        return benhNhanDAO.xoa(ma);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }
}
