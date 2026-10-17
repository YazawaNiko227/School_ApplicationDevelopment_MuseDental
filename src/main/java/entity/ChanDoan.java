package entity;

import java.time.LocalDate;


public class ChanDoan {
    private String maChanDoan;
    private LocalDate ngayKham;
    private String lyDoKham;
    private String trieuChung;
    private String ketLuanChanDoan;
    private String maHoSo;
    private String maNhaSi;

    public ChanDoan() {
    }

    public ChanDoan(String maChanDoan, LocalDate ngayKham, String lyDoKham, String trieuChung, String ketLuanChanDoan, String maHoSo, String maNhaSi) {
        setMaChanDoan(maChanDoan);
        setNgayKham(ngayKham);
        setLyDoKham(lyDoKham);
        setTrieuChung(trieuChung);
        setKetLuanChanDoan(ketLuanChanDoan);
        setMaHoSo(maHoSo);
        setMaNhaSi(maNhaSi);
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null) throw new IllegalArgumentException("MaChanDoan không được null.");
        if (maChanDoan != null && maChanDoan.length() > 20) throw new IllegalArgumentException("MaChanDoan tối đa 20 ký tự.");
        this.maChanDoan = maChanDoan;
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        if (ngayKham == null) throw new IllegalArgumentException("NgayKham không được null.");
        this.ngayKham = ngayKham;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        if (lyDoKham != null && lyDoKham.length() > 500) throw new IllegalArgumentException("LyDoKham tối đa 500 ký tự.");
        this.lyDoKham = lyDoKham;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        if (trieuChung != null && trieuChung.length() > 1000) throw new IllegalArgumentException("TrieuChung tối đa 1000 ký tự.");
        this.trieuChung = trieuChung;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public void setKetLuanChanDoan(String ketLuanChanDoan) {
        if (ketLuanChanDoan != null && ketLuanChanDoan.length() > 1000) throw new IllegalArgumentException("KetLuanChanDoan tối đa 1000 ký tự.");
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null) throw new IllegalArgumentException("MaHoSo không được null.");
        if (maHoSo != null && maHoSo.length() > 20) throw new IllegalArgumentException("MaHoSo tối đa 20 ký tự.");
        this.maHoSo = maHoSo;
    }

    public String getMaNhaSi() {
        return maNhaSi;
    }

    public void setMaNhaSi(String maNhaSi) {
        if (maNhaSi == null) throw new IllegalArgumentException("MaNhaSi không được null.");
        if (maNhaSi != null && maNhaSi.length() > 20) throw new IllegalArgumentException("MaNhaSi tối đa 20 ký tự.");
        this.maNhaSi = maNhaSi;
    }

}
