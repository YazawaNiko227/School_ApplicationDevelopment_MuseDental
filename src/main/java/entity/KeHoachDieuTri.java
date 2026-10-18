package entity;


public class KeHoachDieuTri {
    private String maKeHoach;
    private String tenPhacDo;
    private Integer thoiGianDuKien;
    private String trangThai;
    private String maChanDoan;
    private String maBenhNhan;

    public KeHoachDieuTri() {
        this.trangThai = "Dang thuc hien";
    }

    public KeHoachDieuTri(String maKeHoach, String tenPhacDo, Integer thoiGianDuKien, String trangThai, String maChanDoan, String maBenhNhan) {
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
        if (maKeHoach == null) throw new IllegalArgumentException("MaKeHoach không được null.");
        if (maKeHoach != null && maKeHoach.length() > 20) throw new IllegalArgumentException("MaKeHoach tối đa 20 ký tự.");
        this.maKeHoach = maKeHoach;
    }

    public String getTenPhacDo() {
        return tenPhacDo;
    }

    public void setTenPhacDo(String tenPhacDo) {
        if (tenPhacDo == null) throw new IllegalArgumentException("TenPhacDo không được null.");
        if (tenPhacDo != null && tenPhacDo.length() > 150) throw new IllegalArgumentException("TenPhacDo tối đa 150 ký tự.");
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
        if (trangThai == null) throw new IllegalArgumentException("TrangThai không được null.");
        if (trangThai != null && trangThai.length() > 30) throw new IllegalArgumentException("TrangThai tối đa 30 ký tự.");
        this.trangThai = trangThai;
    }

    public String getMaChanDoan() {
        return maChanDoan;
    }

    public void setMaChanDoan(String maChanDoan) {
        if (maChanDoan == null) throw new IllegalArgumentException("MaChanDoan không được null.");
        if (maChanDoan != null && maChanDoan.length() > 20) throw new IllegalArgumentException("MaChanDoan tối đa 20 ký tự.");
        this.maChanDoan = maChanDoan;
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        this.maBenhNhan = maBenhNhan;
    }

}
