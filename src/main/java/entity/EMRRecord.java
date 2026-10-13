package entity;

import java.time.LocalDate;

public class EMRRecord {
    private String maHoSo;
    private String maChanDoan;
    private String maBenhNhan;
    private String tenBenhNhan;
    private LocalDate ngayKham;
    private String maNhaSi;
    private String tenNhaSi;
    private String lyDoKham;
    private String trieuChung;
    private String ketLuanChanDoan;

    public EMRRecord() {
    }

    public EMRRecord(String maHoSo, String maChanDoan, String maBenhNhan, String tenBenhNhan,
            LocalDate ngayKham, String maNhaSi, String tenNhaSi,
            String lyDoKham, String trieuChung, String ketLuanChanDoan) {
        this.maHoSo = maHoSo;
        this.maChanDoan = maChanDoan;
        this.maBenhNhan = maBenhNhan;
        this.tenBenhNhan = tenBenhNhan;
        this.ngayKham = ngayKham;
        this.maNhaSi = maNhaSi;
        this.tenNhaSi = tenNhaSi;
        this.lyDoKham = lyDoKham;
        this.trieuChung = trieuChung;
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        this.maHoSo = maHoSo;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        this.maChanDoan = maChanDoan;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        this.maBenhNhan = maBenhNhan;
    }

    public String getTenBenhNhan() {
        return tenBenhNhan;
    }

    public void setTenBenhNhan(String tenBenhNhan) {
        this.tenBenhNhan = tenBenhNhan;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        this.ngayKham = ngayKham;
    }

    public String getMaNhaSi() {
        return maNhaSi;
    }

    public void setMaNhaSi(String maNhaSi) {
        this.maNhaSi = maNhaSi;
    }

    public String getTenNhaSi() {
        return tenNhaSi;
    }

    public void setTenNhaSi(String tenNhaSi) {
        this.tenNhaSi = tenNhaSi;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        this.lyDoKham = lyDoKham;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        this.trieuChung = trieuChung;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public void setKetLuanChanDoan(String ketLuanChanDoan) {
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    @Override
    public String toString() {
        return "EMRRecord{" + maChanDoan + ", " + maHoSo + ", " + tenBenhNhan + ", " + ngayKham + "}";
    }
}