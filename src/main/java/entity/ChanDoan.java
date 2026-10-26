package entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ChanDoan {
    private String maChanDoan;
    private LocalDate ngayKham;
    private String lyDoKham;
    private String trieuChung;
    private String ketLuanChanDoan;
    private String trangThai;
    private String maLichHen;
    private String maHoSo;
    private String maNhaSi;

    public ChanDoan() {
        this.trangThai = "Nhap";
    }

    public ChanDoan(String maChanDoan, LocalDate ngayKham, String lyDoKham, String trieuChung, String ketLuanChanDoan, String trangThai, String maLichHen, String maHoSo, String maNhaSi) {
        setMaChanDoan(maChanDoan);
        setNgayKham(ngayKham);
        setLyDoKham(lyDoKham);
        setTrieuChung(trieuChung);
        setKetLuanChanDoan(ketLuanChanDoan);
        setTrangThai(trangThai);
        setMaLichHen(maLichHen);
        setMaHoSo(maHoSo);
        setMaNhaSi(maNhaSi);
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null || maChanDoan.trim().isEmpty()) throw new IllegalArgumentException("maChanDoan không được để trống");
        if (maChanDoan != null && maChanDoan.length() > 20) throw new IllegalArgumentException("maChanDoan tối đa 20 ký tự");
        this.maChanDoan = maChanDoan;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        if (ngayKham == null) throw new IllegalArgumentException("ngayKham không được null");
        this.ngayKham = ngayKham;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        if (lyDoKham != null && lyDoKham.length() > 500) throw new IllegalArgumentException("lyDoKham tối đa 500 ký tự");
        this.lyDoKham = lyDoKham;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        if (trieuChung != null && trieuChung.length() > 1000) throw new IllegalArgumentException("trieuChung tối đa 1000 ký tự");
        this.trieuChung = trieuChung;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public void setKetLuanChanDoan(String ketLuanChanDoan) {
        if (ketLuanChanDoan != null && ketLuanChanDoan.length() > 1000) throw new IllegalArgumentException("ketLuanChanDoan tối đa 1000 ký tự");
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty()) throw new IllegalArgumentException("trangThai không được để trống");
        if (trangThai != null && trangThai.length() > 20) throw new IllegalArgumentException("trangThai tối đa 20 ký tự");
        if (!"Nhap".equals(trangThai) && !"Da chot".equals(trangThai)) throw new IllegalArgumentException("trangThai không thuộc trạng thái hợp lệ");
        this.trangThai = trangThai;
    }

    public String getMaLichHen() {
        return maLichHen;
    }

    public void setMaLichHen(String maLichHen) {
        if (maLichHen != null && maLichHen.length() > 20) throw new IllegalArgumentException("maLichHen tối đa 20 ký tự");
        this.maLichHen = maLichHen;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null || maHoSo.trim().isEmpty()) throw new IllegalArgumentException("maHoSo không được để trống");
        if (maHoSo != null && maHoSo.length() > 20) throw new IllegalArgumentException("maHoSo tối đa 20 ký tự");
        this.maHoSo = maHoSo;
    }

    public String getMaNhaSi() {
        return maNhaSi;
    }

    public void setMaNhaSi(String maNhaSi) {
        if (maNhaSi == null || maNhaSi.trim().isEmpty()) throw new IllegalArgumentException("maNhaSi không được để trống");
        if (maNhaSi != null && maNhaSi.length() > 20) throw new IllegalArgumentException("maNhaSi tối đa 20 ký tự");
        this.maNhaSi = maNhaSi;
    }

}
