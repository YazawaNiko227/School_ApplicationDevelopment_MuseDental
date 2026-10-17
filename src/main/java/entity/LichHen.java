package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;


public class LichHen {
    private String maLichHen;
    private LocalDateTime thoiGianHen;
    private String trangThai;
    private LocalDate ngayTao;
    private String maNhanVienLap;
    private String maNhaSiPhuTrach;
    private String maBenhNhan;
    private String maPhong;
    private String maKeHoach;

    public LichHen() {
        this.trangThai = "Cho xac nhan";
        this.ngayTao = LocalDate.now();
    }

    public LichHen(String maLichHen, LocalDateTime thoiGianHen, String trangThai, LocalDate ngayTao, String maNhanVienLap, String maNhaSiPhuTrach, String maBenhNhan, String maPhong, String maKeHoach) {
        setMaLichHen(maLichHen);
        setThoiGianHen(thoiGianHen);
        setTrangThai(trangThai);
        setNgayTao(ngayTao);
        setMaNhanVienLap(maNhanVienLap);
        setMaNhaSiPhuTrach(maNhaSiPhuTrach);
        setMaBenhNhan(maBenhNhan);
        setMaPhong(maPhong);
        setMaKeHoach(maKeHoach);
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen == null) throw new IllegalArgumentException("MaLichHen không được null.");
        if (maLichHen != null && maLichHen.length() > 20) throw new IllegalArgumentException("MaLichHen tối đa 20 ký tự.");
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        if (thoiGianHen == null) throw new IllegalArgumentException("ThoiGianHen không được null.");
        this.thoiGianHen = thoiGianHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null) throw new IllegalArgumentException("TrangThai không được null.");
        if (trangThai != null && trangThai.length() > 30) throw new IllegalArgumentException("TrangThai tối đa 30 ký tự.");
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        if (ngayTao == null) throw new IllegalArgumentException("NgayTao không được null.");
        this.ngayTao = ngayTao;
    }

    public String getMaNhanVienLap() {
        return maNhanVienLap;
    }

    public void setMaNhanVienLap(String maNhanVienLap) {
        if (maNhanVienLap == null) throw new IllegalArgumentException("MaNhanVienLap không được null.");
        if (maNhanVienLap != null && maNhanVienLap.length() > 20) throw new IllegalArgumentException("MaNhanVienLap tối đa 20 ký tự.");
        this.maNhanVienLap = maNhanVienLap;
    }

    public String getMaNhaSiPhuTrach() {
        return maNhaSiPhuTrach;
    }

    public void setMaNhaSiPhuTrach(String maNhaSiPhuTrach) {
        if (maNhaSiPhuTrach == null) throw new IllegalArgumentException("MaNhaSiPhuTrach không được null.");
        if (maNhaSiPhuTrach != null && maNhaSiPhuTrach.length() > 20) throw new IllegalArgumentException("MaNhaSiPhuTrach tối đa 20 ký tự.");
        this.maNhaSiPhuTrach = maNhaSiPhuTrach;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        this.maBenhNhan = maBenhNhan;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        if (maPhong == null) throw new IllegalArgumentException("MaPhong không được null.");
        if (maPhong != null && maPhong.length() > 20) throw new IllegalArgumentException("MaPhong tối đa 20 ký tự.");
        this.maPhong = maPhong;
    }

    public String getMaKeHoach() {
        return maKeHoach;
    }

    public void setMaKeHoach(String maKeHoach) {
        if (maKeHoach != null && maKeHoach.length() > 20) throw new IllegalArgumentException("MaKeHoach tối đa 20 ký tự.");
        this.maKeHoach = maKeHoach;
    }

}
