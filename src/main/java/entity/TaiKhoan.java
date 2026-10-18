package entity;

import java.time.LocalDateTime;

/** Entity ánh xạ bảng TaiKhoan trong QuanLyPhongKhamMuse. */
public class TaiKhoan {
    private String tenDangNhap;
    private String matKhauMaHoa;
    private String trangThaiTaiKhoan;
    private LocalDateTime lanDangNhapCuoi;
    private String maNhanVien;

    public TaiKhoan() {
        this.trangThaiTaiKhoan = "Kich hoat";
    }

    public TaiKhoan(String tenDangNhap, String matKhauMaHoa, String trangThaiTaiKhoan, LocalDateTime lanDangNhapCuoi, String maNhanVien) {
        setTenDangNhap(tenDangNhap);
        setMatKhauMaHoa(matKhauMaHoa);
        setTrangThaiTaiKhoan(trangThaiTaiKhoan);
        setLanDangNhapCuoi(lanDangNhapCuoi);
        setMaNhanVien(maNhanVien);
    }

    public String getTenDangNhap() {
        return tenDangNhap;
    }

    public void setTenDangNhap(String tenDangNhap) {
        if (tenDangNhap == null) throw new IllegalArgumentException("TenDangNhap không được null.");
        if (tenDangNhap != null && tenDangNhap.length() > 50) throw new IllegalArgumentException("TenDangNhap tối đa 50 ký tự.");
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhauMaHoa() {
        return matKhauMaHoa;
    }

    public void setMatKhauMaHoa(String matKhauMaHoa) {
        if (matKhauMaHoa == null) throw new IllegalArgumentException("MatKhauMaHoa không được null.");
        if (matKhauMaHoa != null && matKhauMaHoa.length() > 255) throw new IllegalArgumentException("MatKhauMaHoa tối đa 255 ký tự.");
        this.matKhauMaHoa = matKhauMaHoa;
    }

    public String getTrangThaiTaiKhoan() {
        return trangThaiTaiKhoan;
    }

    public void setTrangThaiTaiKhoan(String trangThaiTaiKhoan) {
        if (trangThaiTaiKhoan == null) throw new IllegalArgumentException("TrangThaiTaiKhoan không được null.");
        if (trangThaiTaiKhoan != null && trangThaiTaiKhoan.length() > 20) throw new IllegalArgumentException("TrangThaiTaiKhoan tối đa 20 ký tự.");
        this.trangThaiTaiKhoan = trangThaiTaiKhoan;
    }

    public LocalDateTime getLanDangNhapCuoi() {
        return lanDangNhapCuoi;
    }

    public void setLanDangNhapCuoi(LocalDateTime lanDangNhapCuoi) {
        this.lanDangNhapCuoi = lanDangNhapCuoi;
    }

    public String getMaNhanVien() {
        return maNhanVien;
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null) throw new IllegalArgumentException("MaNhanVien không được null.");
        if (maNhanVien != null && maNhanVien.length() > 20) throw new IllegalArgumentException("MaNhanVien tối đa 20 ký tự.");
        this.maNhanVien = maNhanVien;
    }

}
