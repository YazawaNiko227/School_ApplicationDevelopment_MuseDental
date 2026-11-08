package entity;

import java.time.LocalDate;

public class ChanDoan {
    private String maChanDoan;
    private LocalDate ngayKham;
    private String lyDoKham;
    private String trieuChung;
    private String ketLuanChanDoan;
    private String trangThai;
    private LichHen maLichHen;
    private HoSoBenhAn maHoSo;
    private NhanVien maNhaSi;

    public ChanDoan() {
        this.trangThai = "Nhap";
    }

    public ChanDoan(String maChanDoan, LocalDate ngayKham, String lyDoKham, String trieuChung,
            String ketLuanChanDoan, String trangThai, String maLichHen, String maHoSo, String maNhaSi) {
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
        if (maChanDoan == null || maChanDoan.trim().isEmpty())
            throw new IllegalArgumentException("maChanDoan không được để trống");
        if (maChanDoan.trim().length() > 20)
            throw new IllegalArgumentException("maChanDoan tối đa 20 ký tự");
        this.maChanDoan = maChanDoan.trim();
    }

    public LocalDate getNgayKham() {
        return ngayKham;
    }

    public void setNgayKham(LocalDate ngayKham) {
        if (ngayKham == null)
            throw new IllegalArgumentException("ngayKham không được null");
        this.ngayKham = ngayKham;
    }

    public String getLyDoKham() {
        return lyDoKham;
    }

    public void setLyDoKham(String lyDoKham) {
        if (lyDoKham != null && lyDoKham.length() > 500)
            throw new IllegalArgumentException("lyDoKham tối đa 500 ký tự");
        this.lyDoKham = lyDoKham;
    }

    public String getTrieuChung() {
        return trieuChung;
    }

    public void setTrieuChung(String trieuChung) {
        if (trieuChung != null && trieuChung.length() > 1000)
            throw new IllegalArgumentException("trieuChung tối đa 1000 ký tự");
        this.trieuChung = trieuChung;
    }

    public String getKetLuanChanDoan() {
        return ketLuanChanDoan;
    }

    public void setKetLuanChanDoan(String ketLuanChanDoan) {
        if (ketLuanChanDoan != null && ketLuanChanDoan.length() > 1000)
            throw new IllegalArgumentException("ketLuanChanDoan tối đa 1000 ký tự");
        this.ketLuanChanDoan = ketLuanChanDoan;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty())
            throw new IllegalArgumentException("trangThai không được để trống");
        if (!"Nhap".equals(trangThai) && !"Da chot".equals(trangThai))
            throw new IllegalArgumentException("trangThai không thuộc trạng thái hợp lệ");
        this.trangThai = trangThai;
    }

    public LichHen getMaLichHen() {
        return maLichHen;
    }

    public String getMaLichHenId() {
        return maLichHen == null ? null : maLichHen.getMaLichHen();
    }

    public LichHen getLichHen() {
        return maLichHen;
    }

    public void setLichHen(LichHen lichHen) {
        this.maLichHen = lichHen;
    }

    public void setMaLichHen(LichHen lichHen) {
        setLichHen(lichHen);
    }

    public void setMaLichHen(String maLichHen) {
        String v = (maLichHen == null || maLichHen.trim().isEmpty()) ? null : maLichHen.trim();
        if (v != null && v.length() > 20)
            throw new IllegalArgumentException("maLichHen tối đa 20 ký tự");
        if (v == null) {
            this.maLichHen = null;
        } else {
            LichHen lichHen = new LichHen();
            lichHen.setMaLichHen(v);
            this.maLichHen = lichHen;
        }
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
        if (hoSoBenhAn == null)
            throw new IllegalArgumentException("hoSoBenhAn không được null");
        this.maHoSo = hoSoBenhAn;
    }

    public void setMaHoSo(HoSoBenhAn hoSoBenhAn) {
        setHoSoBenhAn(hoSoBenhAn);
    }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null || maHoSo.trim().isEmpty())
            throw new IllegalArgumentException("maHoSo không được để trống");
        if (maHoSo.trim().length() > 20)
            throw new IllegalArgumentException("maHoSo tối đa 20 ký tự");
        HoSoBenhAn hoSoBenhAn = new HoSoBenhAn();
        hoSoBenhAn.setMaHoSo(maHoSo.trim());
        this.maHoSo = hoSoBenhAn;
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

    public String getMaBenhNhanId() {
        return maHoSo == null ? null : maHoSo.getMaBenhNhanId();
    }

    public BenhNhan getBenhNhan() {
        return maHoSo == null ? null : maHoSo.getBenhNhan();
    }

    public void setNhaSi(NhanVien nhaSi) {
        if (nhaSi == null)
            throw new IllegalArgumentException("nhaSi không được null");
        this.maNhaSi = nhaSi;
    }

    public void setMaNhaSi(NhanVien nhaSi) {
        setNhaSi(nhaSi);
    }

    public void setMaNhaSi(String maNhaSi) {
        if (maNhaSi == null || maNhaSi.trim().isEmpty())
            throw new IllegalArgumentException("maNhaSi không được để trống");
        if (maNhaSi.trim().length() > 20)
            throw new IllegalArgumentException("maNhaSi tối đa 20 ký tự");
        NhanVien nhaSi = new NhanVien();
        nhaSi.setMaNhanVien(maNhaSi.trim());
        this.maNhaSi = nhaSi;
    }
}