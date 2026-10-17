package entity;

/** Entity ánh xạ bảng SoDoRang trong QuanLyPhongKhamMuse. */
public class SoDoRang {
    private String maHoSo;
    private int maRang;
    private String loaiRang;
    private String tinhTrangBenhLy;
    private String ghiChu;

    public SoDoRang() {
    }

    public SoDoRang(String maHoSo, int maRang, String loaiRang, String tinhTrangBenhLy, String ghiChu) {
        setMaHoSo(maHoSo);
        setMaRang(maRang);
        setLoaiRang(loaiRang);
        setTinhTrangBenhLy(tinhTrangBenhLy);
        setGhiChu(ghiChu);
    }

    public String getMaHoSo() {
        return maHoSo;
    }

    public void setMaHoSo(String maHoSo) {
        if (maHoSo == null) throw new IllegalArgumentException("MaHoSo không được null.");
        if (maHoSo != null && maHoSo.length() > 20) throw new IllegalArgumentException("MaHoSo tối đa 20 ký tự.");
        this.maHoSo = maHoSo;
    }

    public int getMaRang() {
        return maRang;
    }

    public void setMaRang(int maRang) {
        this.maRang = maRang;
    }

    public String getLoaiRang() {
        return loaiRang;
    }

    public void setLoaiRang(String loaiRang) {
        if (loaiRang != null && loaiRang.length() > 20) throw new IllegalArgumentException("LoaiRang tối đa 20 ký tự.");
        this.loaiRang = loaiRang;
    }

    public String getTinhTrangBenhLy() {
        return tinhTrangBenhLy;
    }

    public void setTinhTrangBenhLy(String tinhTrangBenhLy) {
        if (tinhTrangBenhLy != null && tinhTrangBenhLy.length() > 100) throw new IllegalArgumentException("TinhTrangBenhLy tối đa 100 ký tự.");
        this.tinhTrangBenhLy = tinhTrangBenhLy;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        if (ghiChu != null && ghiChu.length() > 500) throw new IllegalArgumentException("GhiChu tối đa 500 ký tự.");
        this.ghiChu = ghiChu;
    }

}
