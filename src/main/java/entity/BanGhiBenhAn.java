package entity;

import java.time.LocalDate;

public class BanGhiBenhAn {
    private HoSoBenhAn maHoSo;
    private ChanDoan maChanDoan;
    private BenhNhan maBenhNhan;
    private String tenBenhNhan;
    private LocalDate ngayKham;
    private NhanVien maNhaSi;
    private String tenNhaSi;
    private String lyDoKham;
    private String trieuChung;
    private String ketLuanChanDoan;

    public BanGhiBenhAn() {
    }

    public BanGhiBenhAn(String maHoSo, String maChanDoan, String maBenhNhan, String tenBenhNhan,
            LocalDate ngayKham, String maNhaSi, String tenNhaSi,
            String lyDoKham, String trieuChung, String ketLuanChanDoan) {
        setMaHoSo(maHoSo);
        setMaChanDoan(maChanDoan);
        setMaBenhNhan(maBenhNhan);
        this.tenBenhNhan = tenBenhNhan;
        this.ngayKham = ngayKham;
        setMaNhaSi(maNhaSi);
        this.tenNhaSi = tenNhaSi;
        this.lyDoKham = lyDoKham;
        this.trieuChung = trieuChung;
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public HoSoBenhAn getMaHoSo() {
        return maHoSo;
    }

    public String getMaHoSoId() {
        return maHoSo == null ? null : maHoSo.getMaHoSo();
    }

    public HoSoBenhAn getHoSoBenhAn() {
        return maHoSo;
    }

    public void setHoSoBenhAn(HoSoBenhAn hoSoBenhAn) {
        this.maHoSo = hoSoBenhAn;
    }

    public void setMaHoSo(HoSoBenhAn hoSoBenhAn) { setHoSoBenhAn(hoSoBenhAn); }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null) {
            this.maHoSo = null;
        } else {
            HoSoBenhAn hoSo = new HoSoBenhAn();
            hoSo.setMaHoSo(maHoSo);
            this.maHoSo = hoSo;
        }
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

    public void setMaChanDoan(ChanDoan chanDoan) { setChanDoan(chanDoan); }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null) {
            this.maChanDoan = null;
        } else {
            ChanDoan chanDoan = new ChanDoan();
            chanDoan.setMaChanDoan(maChanDoan);
            this.maChanDoan = chanDoan;
        }
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
        this.maBenhNhan = benhNhan;
    }

    public void setMaBenhNhan(BenhNhan benhNhan) { setBenhNhan(benhNhan); }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) {
            this.maBenhNhan = null;
        } else {
            BenhNhan benhNhan = new BenhNhan();
            benhNhan.setMaBenhNhan(maBenhNhan);
            this.maBenhNhan = benhNhan;
        }
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

    public NhanVien getMaNhaSi() {
        return maNhaSi;
    }

    public String getMaNhaSiId() {
        return maNhaSi == null ? null : maNhaSi.getMaNhanVien();
    }

    public NhanVien getNhaSi() {
        return maNhaSi;
    }

    public void setNhaSi(NhanVien nhaSi) {
        this.maNhaSi = nhaSi;
    }

    public void setMaNhaSi(NhanVien nhaSi) { setNhaSi(nhaSi); }

    public void setMaNhaSi(String maNhaSi) {
        if (maNhaSi == null) {
            this.maNhaSi = null;
        } else {
            NhanVien nhaSi = new NhanVien();
            nhaSi.setMaNhanVien(maNhaSi);
            this.maNhaSi = nhaSi;
        }
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
        return "BanGhiBenhAn{" + getMaChanDoanId() + ", " + getMaHoSoId() + ", " + tenBenhNhan + ", "
                + ngayKham + "}";
    }
}