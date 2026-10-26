package entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Tài khoản đăng nhập (bảng TaiKhoan).
 * Mỗi tài khoản gắn với đúng một nhân viên và đúng một vai trò.
 */
public class TaiKhoan {
    private String tenDangNhap;
    private String matKhauMaHoa;
    private String trangThaiTaiKhoan;
    private LocalDateTime lanDangNhapCuoi;
    private NhanVien nhanVien;
    private VaiTro vaiTro;

    public TaiKhoan() {
    }

    public TaiKhoan(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public TaiKhoan(String tenDangNhap, String matKhauMaHoa, String trangThaiTaiKhoan,
            LocalDateTime lanDangNhapCuoi, NhanVien nhanVien, VaiTro vaiTro) {
        this.tenDangNhap = tenDangNhap;
        this.matKhauMaHoa = matKhauMaHoa;
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
        this.lanDangNhapCuoi = lanDangNhapCuoi;
        this.nhanVien = nhanVien;
        this.vaiTro = vaiTro;
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhauMaHoa() {
        return matKhauMaHoa;
    }

    public void setMatKhauMaHoa(String matKhauMaHoa) {
        this.matKhauMaHoa = matKhauMaHoa;
    }

    public String getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(String trangThaiTaiKhoan) {
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public LocalDateTime getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(LocalDateTime lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public VaiTro getVaiTro() {
        return vaiTro;
    }

    public void setVaiTro(VaiTro vaiTro) {
        this.vaiTro = vaiTro;
    }

    /**
     * Tài khoản có đang được kích hoạt không (giá trị mặc định trong DB là 'Kich
     * hoat').
     */
    public boolean isKichHoat() {
        return "Kich hoat".equals(trangThaiTaiKhoan);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaiKhoan)) {
            return false;
        }
        TaiKhoan other = (TaiKhoan) o;
        return Objects.equals(tenDangNhap, other.tenDangNhap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenDangNhap);
    }

    @Override
    public String toString() {
        return "TaiKhoan{tenDangNhap='" + tenDangNhap + "', trangThaiTaiKhoan='" + trangThaiTaiKhoan
                + "', vaiTro=" + vaiTro + "}";
    }
}