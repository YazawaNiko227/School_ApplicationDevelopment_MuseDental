package entity;

import java.time.LocalDate;

public class DotThanhToan {
    private String maDot;
    private LocalDate ngayDenHan;
    private double soTienDot;
    private String trangThai;
    private LocalDate ngayThanhToanThucTe;
    private String maCongNo;

    public DotThanhToan() {
        this.trangThai = "Chua den han";
    }

    public DotThanhToan(String maDot, LocalDate ngayDenHan, double soTienDot, String trangThai, LocalDate ngayThanhToanThucTe, String maCongNo) {
        setMaDot(maDot);
        setNgayDenHan(ngayDenHan);
        setSoTienDot(soTienDot);
        setTrangThai(trangThai);
        setNgayThanhToanThucTe(ngayThanhToanThucTe);
        setMaCongNo(maCongNo);
    }

    public String getMaDot() {
        return maDot;
    }

    public void setMaDot(String maDot) {
        if (maDot == null) throw new IllegalArgumentException("MaDot không được null.");
        if (maDot != null && maDot.length() > 20) throw new IllegalArgumentException("MaDot tối đa 20 ký tự.");
        this.maDot = maDot;
    }

    public LocalDate getNgayDenHan() {
        return ngayDenHan;
    }

    public void setNgayDenHan(LocalDate ngayDenHan) {
        if (ngayDenHan == null) throw new IllegalArgumentException("NgayDenHan không được null.");
        this.ngayDenHan = ngayDenHan;
    }

    public double getSoTienDot() {
        return soTienDot;
    }

    public void setSoTienDot(double soTienDot) {
        if (!Double.isFinite(soTienDot)) throw new IllegalArgumentException("SoTienDot phải là số hữu hạn.");
        if (soTienDot < 0) throw new IllegalArgumentException("SoTienDot phải >= 0.");
        this.soTienDot = soTienDot;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null) throw new IllegalArgumentException("TrangThai không được null.");
        if (trangThai != null && trangThai.length() > 30) throw new IllegalArgumentException("TrangThai tối đa 30 ký tự.");
        this.trangThai = trangThai;
    }

    public LocalDate getNgayThanhToanThucTe() {
        return ngayThanhToanThucTe;
    }

    public void setNgayThanhToanThucTe(LocalDate ngayThanhToanThucTe) {
        this.ngayThanhToanThucTe = ngayThanhToanThucTe;
    }

    public String getMaCongNo() {
        return maCongNo;
    }

    public void setMaCongNo(String maCongNo) {
        if (maCongNo == null) throw new IllegalArgumentException("MaCongNo không được null.");
        if (maCongNo != null && maCongNo.length() > 20) throw new IllegalArgumentException("MaCongNo tối đa 20 ký tự.");
        this.maCongNo = maCongNo;
    }

}
