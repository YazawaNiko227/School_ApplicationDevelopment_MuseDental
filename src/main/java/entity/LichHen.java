package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class LichHen {
    private String maLichHen;
    private LocalDateTime thoiGianHen;
    private String trangThai;
    private LocalDate ngayTao;
    private LocalDateTime thoiGianCheckIn;
    private NhanVien maNhanVienLap;
    private NhanVien maNhaSiPhuTrach;
    private BenhNhan maBenhNhan;
    private PhongKham maPhong;
    private KeHoachDieuTri maKeHoach;

    public LichHen() {
        this.trangThai = "Cho xac nhan";
        this.ngayTao = LocalDate.now();
    }

    public LichHen(String maLichHen, LocalDateTime thoiGianHen, String trangThai, LocalDate ngayTao,
            LocalDateTime thoiGianCheckIn, String maNhanVienLap, String maNhaSiPhuTrach, String maBenhNhan,
            String maPhong, String maKeHoach) {
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
        if (maLichHen == null || maLichHen.trim().isEmpty())
            throw new IllegalArgumentException("maLichHen không được để trống");
        if (maLichHen != null && maLichHen.length() > 20)
            throw new IllegalArgumentException("maLichHen tối đa 20 ký tự");
        this.maLichHen = maLichHen;
    }

    public LocalDateTime getThoiGianHen() {
        return thoiGianHen;
    }

    public void setThoiGianHen(LocalDateTime thoiGianHen) {
        if (thoiGianHen == null)
            throw new IllegalArgumentException("thoiGianHen không được null");
        this.thoiGianHen = thoiGianHen;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty())
            throw new IllegalArgumentException("trangThai không được để trống");
        if (trangThai != null && trangThai.length() > 30)
            throw new IllegalArgumentException("trangThai tối đa 30 ký tự");
        if (!"Cho xac nhan".equals(trangThai) && !"Da xac nhan".equals(trangThai) && !"Da check-in".equals(trangThai)
                && !"Dang kham".equals(trangThai) && !"Hoan thanh".equals(trangThai) && !"Huy".equals(trangThai)
                && !"Vang".equals(trangThai))
            throw new IllegalArgumentException("trangThai không thuộc trạng thái hợp lệ");
        this.trangThai = trangThai;
    }

    public LocalDate getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDate ngayTao) {
        if (ngayTao == null)
            throw new IllegalArgumentException("ngayTao không được null");
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getThoiGianCheckIn() {
        return thoiGianCheckIn;
    }

    public void setThoiGianCheckIn(LocalDateTime thoiGianCheckIn) {
        this.thoiGianCheckIn = thoiGianCheckIn;
    }

    public NhanVien getMaNhanVienLap() {
        return maNhanVienLap;
    }

    public String getMaNhanVienLapId() {
        return maNhanVienLap == null ? null : maNhanVienLap.getMaNhanVien();
    }

    public NhanVien getNhanVienLap() {
        return maNhanVienLap;
    }

    public void setNhanVienLap(NhanVien nhanVienLap) {
        if (nhanVienLap == null)
            throw new IllegalArgumentException("NhanVienLap không được null.");
        this.maNhanVienLap = nhanVienLap;
    }

    public void setMaNhanVienLap(NhanVien nhanVienLap) { setNhanVienLap(nhanVienLap); }

    public void setMaNhanVienLap(String maNhanVienLap) {
        if (maNhanVienLap == null || maNhanVienLap.trim().isEmpty())
            throw new IllegalArgumentException("maNhanVienLap không được để trống");
        if (maNhanVienLap != null && maNhanVienLap.length() > 20)
            throw new IllegalArgumentException("maNhanVienLap tối đa 20 ký tự");
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(maNhanVienLap);
        this.maNhanVienLap = nhanVien;
    }

    public NhanVien getMaNhaSiPhuTrach() {
        return maNhaSiPhuTrach;
    }

    public String getMaNhaSiPhuTrachId() {
        return maNhaSiPhuTrach == null ? null : maNhaSiPhuTrach.getMaNhanVien();
    }

    public NhanVien getNhaSiPhuTrach() {
        return maNhaSiPhuTrach;
    }

    public void setNhaSiPhuTrach(NhanVien nhaSiPhuTrach) {
        if (nhaSiPhuTrach == null)
            throw new IllegalArgumentException("NhaSiPhuTrach không được null.");
        this.maNhaSiPhuTrach = nhaSiPhuTrach;
    }

    public void setMaNhaSiPhuTrach(NhanVien nhaSiPhuTrach) { setNhaSiPhuTrach(nhaSiPhuTrach); }

    public void setMaNhaSiPhuTrach(String maNhaSiPhuTrach) {
        if (maNhaSiPhuTrach == null || maNhaSiPhuTrach.trim().isEmpty())
            throw new IllegalArgumentException("maNhaSiPhuTrach không được để trống");
        if (maNhaSiPhuTrach != null && maNhaSiPhuTrach.length() > 20)
            throw new IllegalArgumentException("maNhaSiPhuTrach tối đa 20 ký tự");
        NhanVien nhaSi = new NhanVien();
        nhaSi.setMaNhanVien(maNhaSiPhuTrach);
        this.maNhaSiPhuTrach = nhaSi;
    }

    public BenhNhan getMaBenhNhan() {
        return maBenhNhan;
    }

    public String getMaBenhNhanId() {
        return maBenhNhan == null ? null : maBenhNhan.getMaBenhNhan();
    }

    public BenhNhan getBenhNhan() {
        return maBenhNhan;
    }

    public void setBenhNhan(BenhNhan benhNhan) {
        if (benhNhan == null)
            throw new IllegalArgumentException("BenhNhan không được null.");
        this.maBenhNhan = benhNhan;
    }

    public void setMaBenhNhan(BenhNhan benhNhan) { setBenhNhan(benhNhan); }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null || maBenhNhan.trim().isEmpty())
            throw new IllegalArgumentException("maBenhNhan không được để trống");
        if (maBenhNhan != null && maBenhNhan.length() > 20)
            throw new IllegalArgumentException("maBenhNhan tối đa 20 ký tự");
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.setMaBenhNhan(maBenhNhan);
        this.maBenhNhan = benhNhan;
    }

    public PhongKham getMaPhong() {
        return maPhong;
    }

    public String getMaPhongId() {
        return maPhong == null ? null : maPhong.getMaPhong();
    }

    public PhongKham getPhongKham() {
        return maPhong;
    }

    public void setPhongKham(PhongKham phongKham) {
        if (phongKham == null)
            throw new IllegalArgumentException("PhongKham không được null.");
        this.maPhong = phongKham;
    }

    public void setMaPhong(PhongKham phongKham) { setPhongKham(phongKham); }

    public void setMaPhong(String maPhong) {
        if (maPhong == null || maPhong.trim().isEmpty())
            throw new IllegalArgumentException("maPhong không được để trống");
        if (maPhong != null && maPhong.length() > 20)
            throw new IllegalArgumentException("maPhong tối đa 20 ký tự");
        PhongKham phongKham = new PhongKham();
        phongKham.setMaPhong(maPhong);
        this.maPhong = phongKham;
    }

    public KeHoachDieuTri getMaKeHoach() {
        return maKeHoach;
    }

    public String getMaKeHoachId() {
        return maKeHoach == null ? null : maKeHoach.getMaKeHoach();
    }

    public KeHoachDieuTri getKeHoachDieuTri() {
        return maKeHoach;
    }

    public void setKeHoachDieuTri(KeHoachDieuTri keHoachDieuTri) {
        this.maKeHoach = keHoachDieuTri;
    }

    public void setMaKeHoach(KeHoachDieuTri keHoachDieuTri) { setKeHoachDieuTri(keHoachDieuTri); }

    public void setMaKeHoach(String maKeHoach) {
        String v = (maKeHoach == null || maKeHoach.trim().isEmpty()) ? null : maKeHoach.trim();
        if (v != null && v.length() > 20)
            throw new IllegalArgumentException("maKeHoach tối đa 20 ký tự");
        if (v == null) {
            this.maKeHoach = null;
        } else {
            KeHoachDieuTri keHoach = new KeHoachDieuTri();
            keHoach.setMaKeHoach(v);
            this.maKeHoach = keHoach;
        }
    }

}
