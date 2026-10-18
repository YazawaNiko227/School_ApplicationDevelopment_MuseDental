package entity;

import java.time.LocalDate;

public class DonThuoc {
    private String maDonThuoc;
    private LocalDate ngayKe;
    private String loiDanChamSoc;
    private String maChanDoan;

    public DonThuoc() {
    }

    public DonThuoc(String maDonThuoc, LocalDate ngayKe, String loiDanChamSoc, String maChanDoan) {
        setMaDonThuoc(maDonThuoc);
        setNgayKe(ngayKe);
        setLoiDanChamSoc(loiDanChamSoc);
        setMaChanDoan(maChanDoan);
    }

    public String getMaDonThuoc() {
        return maDonThuoc;
    }

    public void setMaDonThuoc(String maDonThuoc) {
        if (maDonThuoc == null) throw new IllegalArgumentException("MaDonThuoc không được null.");
        if (maDonThuoc != null && maDonThuoc.length() > 20) throw new IllegalArgumentException("MaDonThuoc tối đa 20 ký tự.");
        this.maDonThuoc = maDonThuoc;
    }

    public LocalDate getNgayKe() {
        return ngayKe;
    }

    public void setNgayKe(LocalDate ngayKe) {
        if (ngayKe == null) throw new IllegalArgumentException("NgayKe không được null.");
        this.ngayKe = ngayKe;
    }

    public String getLoiDanChamSoc() {
        return loiDanChamSoc;
    }

    public void setLoiDanChamSoc(String loiDanChamSoc) {
        if (loiDanChamSoc != null && loiDanChamSoc.length() > 500) throw new IllegalArgumentException("LoiDanChamSoc tối đa 500 ký tự.");
        this.loiDanChamSoc = loiDanChamSoc;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null) throw new IllegalArgumentException("MaChanDoan không được null.");
        if (maChanDoan != null && maChanDoan.length() > 20) throw new IllegalArgumentException("MaChanDoan tối đa 20 ký tự.");
        this.maChanDoan = maChanDoan;
    }

}
