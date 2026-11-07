package entity;

public class ChiTietKeHoachDieuTriDichVu {
    private KeHoachDieuTri maKeHoach;
    private DichVu maDichVu;
    private int soLuong;

    public ChiTietKeHoachDieuTriDichVu() {
        this.soLuong = 1;
    }

    public ChiTietKeHoachDieuTriDichVu(String maKeHoach, String maDichVu, int soLuong) {
        setMaKeHoach(maKeHoach);
        setMaDichVu(maDichVu);
        setSoLuong(soLuong);
    }

    public KeHoachDieuTri getMaKeHoach() {
        return maKeHoach;
    }

    public String getMaKeHoachId() {
        return maKeHoach == null ? null : maKeHoach.getMaKeHoach();
    }

    public KeHoachDieuTri getKeHoachDieuTri() {
        return maKeHoach;
    }

    public void setKeHoachDieuTri(KeHoachDieuTri keHoachDieuTri) {
        if (keHoachDieuTri == null)
            throw new IllegalArgumentException("KeHoachDieuTri không được null.");
        this.maKeHoach = keHoachDieuTri;
    }

    public void setMaKeHoach(KeHoachDieuTri keHoachDieuTri) { setKeHoachDieuTri(keHoachDieuTri); }

    public void setMaKeHoach(String maKeHoach) {
        if (maKeHoach == null)
            throw new IllegalArgumentException("MaKeHoach không được null.");
        if (maKeHoach != null && maKeHoach.length() > 20)
            throw new IllegalArgumentException("MaKeHoach tối đa 20 ký tự.");
        KeHoachDieuTri keHoach = new KeHoachDieuTri();
        keHoach.setMaKeHoach(maKeHoach);
        this.maKeHoach = keHoach;
    }

    public DichVu getMaDichVu() {
        return maDichVu;
    }

    public String getMaDichVuId() {
        return maDichVu == null ? null : maDichVu.getMaDichVu();
    }

    public DichVu getDichVu() {
        return maDichVu;
    }

    public void setDichVu(DichVu dichVu) {
        if (dichVu == null)
            throw new IllegalArgumentException("DichVu không được null.");
        this.maDichVu = dichVu;
    }

    public void setMaDichVu(DichVu dichVu) { setDichVu(dichVu); }

    public void setMaDichVu(String maDichVu) {
        if (maDichVu == null)
            throw new IllegalArgumentException("MaDichVu không được null.");
        if (maDichVu != null && maDichVu.length() > 20)
            throw new IllegalArgumentException("MaDichVu tối đa 20 ký tự.");
        DichVu dichVu = new DichVu();
        dichVu.setMaDichVu(maDichVu);
        this.maDichVu = dichVu;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        if (soLuong <= 0)
            throw new IllegalArgumentException("SoLuong phải > 0.");
        this.soLuong = soLuong;
    }

}
