package entity;

public class KeHoachDieuTri {
    public static final String DANG_THUC_HIEN = "Dang thuc hien";
    public static final String HOAN_THANH = "Hoan thanh";
    public static final String HUY = "Huy";
    private String maKeHoach;
    private String tenPhacDo;
    private Integer thoiGianDuKien;
    private String trangThai;
    private ChanDoan maChanDoan;
    private BenhNhan maBenhNhan;

    public KeHoachDieuTri() {
        this.trangThai = "Dang thuc hien";
    }

    public KeHoachDieuTri(String maKeHoach, String tenPhacDo, Integer thoiGianDuKien, String trangThai,
            String maChanDoan, String maBenhNhan) {
        setMaKeHoach(maKeHoach);
        setTenPhacDo(tenPhacDo);
        setThoiGianDuKien(thoiGianDuKien);
        setTrangThai(trangThai);
        setMaChanDoan(maChanDoan);
        setMaBenhNhan(maBenhNhan);
    }

    public String getMaKeHoach() {
        return maKeHoach;
    }

    public void setMaKeHoach(String maKeHoach) {
        if (maKeHoach == null)
            throw new IllegalArgumentException("MaKeHoach không được null.");
        if (maKeHoach != null && maKeHoach.length() > 20)
            throw new IllegalArgumentException("MaKeHoach tối đa 20 ký tự.");
        this.maKeHoach = maKeHoach;
    }

    public String getTenPhacDo() {
        return tenPhacDo;
    }

    public void setTenPhacDo(String tenPhacDo) {
        if (tenPhacDo == null)
            throw new IllegalArgumentException("TenPhacDo không được null.");
        if (tenPhacDo != null && tenPhacDo.length() > 150)
            throw new IllegalArgumentException("TenPhacDo tối đa 150 ký tự.");
        this.tenPhacDo = tenPhacDo;
    }

    public Integer getThoiGianDuKien() {
        return thoiGianDuKien;
    }

    public void setThoiGianDuKien(Integer thoiGianDuKien) {
        this.thoiGianDuKien = thoiGianDuKien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (!DANG_THUC_HIEN.equals(trangThai) && !HOAN_THANH.equals(trangThai) && !HUY.equals(trangThai))
            throw new IllegalArgumentException("TrangThai không thuộc trạng thái hợp lệ.");
        this.trangThai = trangThai;
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
        if (chanDoan == null)
            throw new IllegalArgumentException("ChanDoan không được null.");
        this.maChanDoan = chanDoan;
    }

    public void setMaChanDoan(ChanDoan chanDoan) {
        setChanDoan(chanDoan);
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null)
            throw new IllegalArgumentException("MaChanDoan không được null.");
        if (maChanDoan != null && maChanDoan.length() > 20)
            throw new IllegalArgumentException("MaChanDoan tối đa 20 ký tự.");
        ChanDoan chanDoan = new ChanDoan();
        chanDoan.setMaChanDoan(maChanDoan);
        this.maChanDoan = chanDoan;
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

    public void setMaBenhNhan(BenhNhan benhNhan) {
        setBenhNhan(benhNhan);
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null)
            throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20)
            throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        BenhNhan benhNhan = new BenhNhan();
        benhNhan.setMaBenhNhan(maBenhNhan);
        this.maBenhNhan = benhNhan;
    }

}
