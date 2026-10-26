package entity;

public class PhongKham {
    private String maPhong;
    private String tenPhong;
    private String trangThai;

    public PhongKham() {
        this.trangThai = "San sang";
    }

    public PhongKham(String maPhong, String tenPhong, String trangThai) {
        setMaPhong(maPhong);
        setTenPhong(tenPhong);
        setTrangThai(trangThai);
    }

    public String getMaPhong() {
        return maPhong;
    }

    public void setMaPhong(String maPhong) {
        if (maPhong == null || maPhong.trim().isEmpty())
            throw new IllegalArgumentException("maPhong không được để trống");
        if (maPhong != null && maPhong.length() > 20)
            throw new IllegalArgumentException("maPhong tối đa 20 ký tự");
        this.maPhong = maPhong;
    }

    public String getTenPhong() {
        return tenPhong;
    }

    public void setTenPhong(String tenPhong) {
        if (tenPhong == null || tenPhong.trim().isEmpty())
            throw new IllegalArgumentException("tenPhong không được để trống");
        if (tenPhong != null && tenPhong.length() > 100)
            throw new IllegalArgumentException("tenPhong tối đa 100 ký tự");
        this.tenPhong = tenPhong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        if (trangThai == null || trangThai.trim().isEmpty())
            throw new IllegalArgumentException("trangThai không được để trống");
        if (trangThai != null && trangThai.length() > 30)
            throw new IllegalArgumentException("trangThai tối đa 30 ký tự");
        if (!"San sang".equals(trangThai) && !"Dang kham".equals(trangThai) && !"Can ve sinh".equals(trangThai)
                && !"Bao tri".equals(trangThai))
            throw new IllegalArgumentException("trangThai không thuộc trạng thái hợp lệ");
        this.trangThai = trangThai;
    }

}
