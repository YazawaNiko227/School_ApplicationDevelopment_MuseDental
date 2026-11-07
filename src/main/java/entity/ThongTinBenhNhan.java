package entity;

public class ThongTinBenhNhan {
    private final String maBenhNhan;
    private final String hoTen;

    public ThongTinBenhNhan(String maBenhNhan, String hoTen) {
        this.maBenhNhan = maBenhNhan;
        this.hoTen = hoTen;
    }

    public String getMaBenhNhan() { return maBenhNhan; }
    public String getHoTen() { return hoTen; }
}
