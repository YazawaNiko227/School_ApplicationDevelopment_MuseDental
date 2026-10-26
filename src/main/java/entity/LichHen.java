package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LichHen {
    private String maLichHen;
    private LocalDateTime thoiGianHen;
    private String trangThai;
    private LocalDate ngayTao;
    private LocalDateTime thoiGianCheckIn;
    private String maNhanVienLap;
    private String maNhaSiPhuTrach;
    private String maBenhNhan;
    private String maPhong;
    private String maKeHoach;

    public LichHen() {
        this.trangThai = "Cho xac nhan";
        this.ngayTao = LocalDate.now();
    }

    public LichHen(String maLichHen, LocalDateTime thoiGianHen, String trangThai, LocalDate ngayTao, LocalDateTime thoiGianCheckIn, String maNhanVienLap, String maNhaSiPhuTrach, String maBenhNhan, String maPhong, String maKeHoach) {
        setMaLichHen(maLichHen);
        setThoiGianHen(thoiGianHen);
        setTrangThai(trangThai);
        setNgayTao(ngayTao);
        setThoiGianCheckIn(thoiGianCheckIn);
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
        if (maLichHen == null || maLichHen.trim().isEmpty()) throw new IllegalArgumentException("maLichHen không được để trống");
        if (maLichHen != null && maLichHen.length() > 20) throw new IllegalArgumentException("maLichHen tối đa 20 ký tự");
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        if (thoiGianHen == null) throw new IllegalArgumentException("thoiGianHen không được null");
        this.thoiGianHen = thoiGianHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty()) throw new IllegalArgumentException("trangThai không được để trống");
        if (trangThai != null && trangThai.length() > 30) throw new IllegalArgumentException("trangThai tối đa 30 ký tự");
        if (!"Cho xac nhan".equals(trangThai) && !"Da xac nhan".equals(trangThai) && !"Da check-in".equals(trangThai) && !"Dang kham".equals(trangThai) && !"Hoan thanh".equals(trangThai) && !"Huy".equals(trangThai) && !"Vang".equals(trangThai)) throw new IllegalArgumentException("trangThai không thuộc trạng thái hợp lệ");
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        if (ngayTao == null) throw new IllegalArgumentException("ngayTao không được null");
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getThoiGianCheckIn() {
        return thoiGianCheckIn;
    }

    public void setThoiGianCheckIn(LocalDateTime thoiGianCheckIn) {
        this.thoiGianCheckIn = thoiGianCheckIn;
    }

    public String getMaNhanVienLap() {
        return maNhanVienLap;
    }

    public void setMaNhanVienLap(String maNhanVienLap) {
        if (maNhanVienLap == null || maNhanVienLap.trim().isEmpty()) throw new IllegalArgumentException("maNhanVienLap không được để trống");
        if (maNhanVienLap != null && maNhanVienLap.length() > 20) throw new IllegalArgumentException("maNhanVienLap tối đa 20 ký tự");
        this.maNhanVienLap = maNhanVienLap;
    }

    public String getMaNhaSiPhuTrach() {
        return maNhaSiPhuTrach;
    }

    public void setMaNhaSiPhuTrach(String maNhaSiPhuTrach) {
        if (maNhaSiPhuTrach == null || maNhaSiPhuTrach.trim().isEmpty()) throw new IllegalArgumentException("maNhaSiPhuTrach không được để trống");
        if (maNhaSiPhuTrach != null && maNhaSiPhuTrach.length() > 20) throw new IllegalArgumentException("maNhaSiPhuTrach tối đa 20 ký tự");
        this.maNhaSiPhuTrach = maNhaSiPhuTrach;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null || maBenhNhan.trim().isEmpty()) throw new IllegalArgumentException("maBenhNhan không được để trống");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("maBenhNhan tối đa 20 ký tự");
        this.maBenhNhan = maBenhNhan;
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        if (maPhong == null || maPhong.trim().isEmpty()) throw new IllegalArgumentException("maPhong không được để trống");
        if (maPhong != null && maPhong.length() > 20) throw new IllegalArgumentException("maPhong tối đa 20 ký tự");
        this.maPhong = maPhong;
    }

    public String getMaKeHoach() {
        return maKeHoach;
    }

    public void setMaKeHoach(String maKeHoach) {
        if (maKeHoach != null && maKeHoach.length() > 20) throw new IllegalArgumentException("maKeHoach tối đa 20 ký tự");
        this.maKeHoach = maKeHoach;
    }

}
