package entity;

public class KeHoachDieuTri_DichVu {
    private String maKeHoach;
    private String maDichVu;
    private int soLuong;

    public KeHoachDieuTri_DichVu() {
        this.soLuong = 1;
    }

    public KeHoachDieuTri_DichVu(String maKeHoach, String maDichVu, int soLuong) {
        setMaKeHoach(maKeHoach);
        setMaDichVu(maDichVu);
        setSoLuong(soLuong);
    }

    public String getMaKeHoach() {
        return maKeHoach;
    }

    public void setMaKeHoach(String maKeHoach) {
        if (maKeHoach == null) throw new IllegalArgumentException("MaKeHoach không được null.");
        if (maKeHoach != null && maKeHoach.length() > 20) throw new IllegalArgumentException("MaKeHoach tối đa 20 ký tự.");
        this.maKeHoach = maKeHoach;
    }

    public String getMaDichVu() {
        return maDichVu;
    }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null) throw new IllegalArgumentException("MaDichVu không được null.");
        if (maDichVu != null && maDichVu.length() > 20) throw new IllegalArgumentException("MaDichVu tối đa 20 ký tự.");
        this.maDichVu = maDichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

}
