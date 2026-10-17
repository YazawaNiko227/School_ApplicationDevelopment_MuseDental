package entity;

import java.time.LocalDate;


public class BenhNhan {
    private String maBenhNhan;
    private String hoTen;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String soDienThoai;
    private String diaChi;
    private String soCCCD;
    private String tienSuBenhLy;
    private String thongTinDiUng;
    private String nguoiBaoHo;

    public BenhNhan() {
    }

    public BenhNhan(String maBenhNhan, String hoTen, LocalDate ngaySinh, String gioiTinh, String soDienThoai, String diaChi, String soCCCD, String tienSuBenhLy, String thongTinDiUng, String nguoiBaoHo) {
        setMaBenhNhan(maBenhNhan);
        setHoTen(hoTen);
        setNgaySinh(ngaySinh);
        setGioiTinh(gioiTinh);
        setSoDienThoai(soDienThoai);
        setDiaChi(diaChi);
        setSoCCCD(soCCCD);
        setTienSuBenhLy(tienSuBenhLy);
        setThongTinDiUng(thongTinDiUng);
        setNguoiBaoHo(nguoiBaoHo);
    }

    public String getMaBenhNhan() {
        return maBenhNhan;
    }

    public void setMaBenhNhan(String maBenhNhan) {
        if (maBenhNhan == null) throw new IllegalArgumentException("MaBenhNhan không được null.");
        if (maBenhNhan != null && maBenhNhan.length() > 20) throw new IllegalArgumentException("MaBenhNhan tối đa 20 ký tự.");
        this.maBenhNhan = maBenhNhan;
    }

    public String getHoTen() {
        return hoTen;
    }

    public void setHoTen(String hoTen) {
        if (hoTen == null) throw new IllegalArgumentException("HoTen không được null.");
        if (hoTen != null && hoTen.length() > 100) throw new IllegalArgumentException("HoTen tối đa 100 ký tự.");
        this.hoTen = hoTen;
    }

    public LocalDate getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(LocalDate ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public String getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(String gioiTinh) {
        if (gioiTinh != null && gioiTinh.length() > 10) throw new IllegalArgumentException("GioiTinh tối đa 10 ký tự.");
        this.gioiTinh = gioiTinh;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        if (soDienThoai != null && soDienThoai.length() > 15) throw new IllegalArgumentException("SoDienThoai tối đa 15 ký tự.");
        this.soDienThoai = soDienThoai;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        if (diaChi != null && diaChi.length() > 200) throw new IllegalArgumentException("DiaChi tối đa 200 ký tự.");
        this.diaChi = diaChi;
    }

    public String getSoCCCD() {
        return soCCCD;
    }

    public void setSoCCCD(String soCCCD) {
        if (soCCCD != null && soCCCD.length() > 20) throw new IllegalArgumentException("SoCCCD tối đa 20 ký tự.");
        this.soCCCD = soCCCD;
    }

    public String getTienSuBenhLy() {
        return tienSuBenhLy;
    }

    public void setTienSuBenhLy(String tienSuBenhLy) {
        if (tienSuBenhLy != null && tienSuBenhLy.length() > 1000) throw new IllegalArgumentException("TienSuBenhLy tối đa 1000 ký tự.");
        this.tienSuBenhLy = tienSuBenhLy;
    }

    public String getThongTinDiUng() {
        return thongTinDiUng;
    }

    public void setThongTinDiUng(String thongTinDiUng) {
        if (thongTinDiUng != null && thongTinDiUng.length() > 500) throw new IllegalArgumentException("ThongTinDiUng tối đa 500 ký tự.");
        this.thongTinDiUng = thongTinDiUng;
    }

    public String getNguoiBaoHo() {
        return nguoiBaoHo;
    }

    public void setNguoiBaoHo(String nguoiBaoHo) {
        if (nguoiBaoHo != null && nguoiBaoHo.length() > 100) throw new IllegalArgumentException("NguoiBaoHo tối đa 100 ký tự.");
        this.nguoiBaoHo = nguoiBaoHo;
    }

}
