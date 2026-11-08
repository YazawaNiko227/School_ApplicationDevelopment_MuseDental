package entity;

import java.time.LocalDate;

public class HoaDon {
    public static final String CHUA_THANH_TOAN = "Chua thanh toan";
    public static final String DA_THANH_TOAN = "Da thanh toan";
    public static final String DANG_TRA_GOP = "Dang tra gop";

    private String maHoaDon;
    private LocalDate ngayLap;
    private String trangThaiThanhToan;
    private String phuongThucThanhToan;
    private NhanVien maNhanVien;
    private BenhNhan maBenhNhan;
    private ChanDoan maChanDoan;

    public HoaDon() {
        this.trangThaiThanhToan = CHUA_THANH_TOAN;
        this.ngayLap = LocalDate.now();
    }

    public HoaDon(String maHoaDon, LocalDate ngayLap, String trangThaiThanhToan, String phuongThucThanhToan,
            String maNhanVien, String maBenhNhan, String maChanDoan) {
        setMaHoaDon(maHoaDon);
        setNgayLap(ngayLap);
        setTrangThaiThanhToan(trangThaiThanhToan);
        setPhuongThucThanhToan(phuongThucThanhToan);
        setMaNhanVien(maNhanVien);
        setMaBenhNhan(maBenhNhan);
        setMaChanDoan(maChanDoan);
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty())
            throw new IllegalArgumentException("maHoaDon không được để trống");
        if (maHoaDon.trim().length() > 20)
            throw new IllegalArgumentException("maHoaDon tối đa 20 ký tự");
        this.maHoaDon = maHoaDon.trim();
    }

    public LocalDate getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(LocalDate ngayLap) {
        if (ngayLap == null)
            throw new IllegalArgumentException("ngayLap không được null");
        this.ngayLap = ngayLap;
    }

    public String getTrangThaiThanhToan() {
        return trangThaiThanhToan;
    }

    public void setTrangThaiThanhToan(String trangThaiThanhToan) {
        if (!CHUA_THANH_TOAN.equals(trangThaiThanhToan) && !DA_THANH_TOAN.equals(trangThaiThanhToan)
                && !DANG_TRA_GOP.equals(trangThaiThanhToan))
            throw new IllegalArgumentException("trangThaiThanhToan không thuộc trạng thái hợp lệ");
        this.trangThaiThanhToan = trangThaiThanhToan;
    }

    public String getPhuongThucThanhToan() {
        return phuongThucThanhToan;
    }

    public void setPhuongThucThanhToan(String phuongThucThanhToan) {
        if (phuongThucThanhToan != null && phuongThucThanhToan.length() > 30)
            throw new IllegalArgumentException("phuongThucThanhToan tối đa 30 ký tự");
        this.phuongThucThanhToan = phuongThucThanhToan;
    }

    public NhanVien getMaNhanVien() {
        return maNhanVien;
    }

    public String getMaNhanVienId() {
        return maNhanVien == null ? null : maNhanVien.getMaNhanVien();
    }

    public NhanVien getNhanVien() {
        return maNhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        if (nhanVien == null)
            throw new IllegalArgumentException("nhanVien không được null");
        this.maNhanVien = nhanVien;
    }

    public void setMaNhanVien(NhanVien nhanVien) {
        setNhanVien(nhanVien);
    }

    public void setMaNhanVien(String maNhanVien) {
        if (maNhanVien == null || maNhanVien.trim().isEmpty())
            throw new IllegalArgumentException("maNhanVien không được để trống");
        if (maNhanVien.trim().length() > 20)
            throw new IllegalArgumentException("maNhanVien tối đa 20 ký tự");
        NhanVien nhanVien = new NhanVien();
        nhanVien.setMaNhanVien(maNhanVien.trim());
        this.maNhanVien = nhanVien;
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
            throw new IllegalArgumentException("benhNhan không được null");
        this.maBenhNhan = benhNhan;
    }

    public void setMaBenhNhan(BenhNhan benhNhan) {
        setBenhNhan(benhNhan);
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null || maBenhNhan.trim().isEmpty())
            throw new IllegalArgumentException("maBenhNhan không được để trống");
        if (maBenhNhan.trim().length() > 20)
            throw new IllegalArgumentException("maBenhNhan tối đa 20 ký tự");
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.setMaBenhNhan(maBenhNhan.trim());
        this.maBenhNhan = benhNhan;
    }

    public ChanDoan getMaChanDoan() {
        return maChanDoan;
    }

    public String getMaChanDoanId() {
        return maChanDoan == null ? null : maChanDoan.getMaChanDoan();
    }

    public ChanDoan getChanDoan() {
        return maChanDoan;
    }

    public void setChanDoan(ChanDoan chanDoan) {
        this.maChanDoan = chanDoan;
    }

    public void setMaChanDoan(ChanDoan chanDoan) {
        setChanDoan(chanDoan);
    }

    public void setMaChanDoan(String maChanDoan) {
        String v = (maChanDoan == null || maChanDoan.trim().isEmpty()) ? null : maChanDoan.trim();
        if (v != null && v.length() > 20)
            throw new IllegalArgumentException("maChanDoan tối đa 20 ký tự");
        if (v == null) {
            this.maChanDoan = null;
        } else {
            ChanDoan chanDoan = new ChanDoan();
            chanDoan.setMaChanDoan(v);
            this.maChanDoan = chanDoan;
        }
    }
}