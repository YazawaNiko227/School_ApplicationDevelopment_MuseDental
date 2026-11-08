package service;

import dao.DonThuocDAO;
import dao.VatTuDAO;
import entity.ChanDoan;
import entity.ChiTietDonThuoc;
import entity.DonThuoc;
import entity.VatTu;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class KeDonThuocService {
    private final DonThuocDAO donThuocDAO = new DonThuocDAO();
    private final VatTuDAO vatTuDAO = new VatTuDAO();

    public List<ChanDoan> layLanKhamChoKeDon() throws SQLException {
        return donThuocDAO.layLanKhamChoKeDon();
    }

    public List<DonThuoc> layDanhSachDonThuocDaLuu() throws SQLException {
        return donThuocDAO.layDanhSachDonThuocDaLuu();
    }

    public List<ChiTietDonThuoc> layThuocDaKe(String maDonThuoc) throws SQLException {
        if (maDonThuoc == null || maDonThuoc.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn thuốc không hợp lệ.");
        }
        return donThuocDAO.layThuocDaKe(maDonThuoc.trim());
    }

    public List<VatTu> timThuoc(String tuKhoa) throws SQLException {
        return vatTuDAO.timKiem(tuKhoa, VatTuDAO.LOAI_THUOC);
    }

    public DonThuoc timTheoChanDoan(String maChanDoan) throws SQLException {
        return donThuocDAO.timTheoChanDoan(maChanDoan);
    }

    public String sinhMaDonThuoc() throws SQLException {
        return donThuocDAO.sinhMa();
    }

    public List<ChiTietDonThuoc> layChiTiet(String maDonThuoc) throws SQLException {
        return donThuocDAO.layChiTiet(maDonThuoc);
    }

    public String kiemTraThuoc(VatTu thuoc, String lieuDung, int soLuong) {
        if (thuoc == null) {
            return "Không tìm thấy thuốc đã chọn.";
        }
        if (lieuDung == null || lieuDung.trim().isEmpty()) {
            return "Liều dùng không được để trống (ví dụ: 1 viên x 2 lần/ngày).";
        }
        if (soLuong <= 0) {
            return "Số lượng kê phải lớn hơn 0.";
        }
        if (soLuong > thuoc.getSoLuongTon()) {
            return "Số lượng kê (" + soLuong + ") vượt quá tồn kho hiện có ("
                    + thuoc.getSoLuongTon() + ").";
        }
        if (thuoc.getHanSuDung() != null && thuoc.getHanSuDung().isBefore(LocalDate.now())) {
            return "Thuốc " + thuoc.getTenVatTu() + " đã hết hạn sử dụng, không thể kê đơn!";
        }
        return null;
    }

    public String kiemTraLuuDon(boolean coLanKham, boolean coChiTiet, String loiDan) {
        if (!coLanKham) {
            return "Vui lòng chọn lần khám cần kê đơn.";
        }
        if (!coChiTiet) {
            return "Đơn thuốc phải có ít nhất một loại thuốc.";
        }
        if (loiDan != null && loiDan.length() > 500) {
            return "Lời dặn chăm sóc tối đa 500 ký tự.";
        }
        return null;
    }

    public DonThuoc luuDonThuoc(DonThuoc donThuoc, List<ChiTietDonThuoc> chiTietList) throws SQLException {
        if (donThuoc == null || chiTietList == null || chiTietList.isEmpty()) {
            throw new IllegalArgumentException("Đơn thuốc phải có ít nhất một loại thuốc.");
        }
        return donThuocDAO.luuDonThuoc(donThuoc, chiTietList);
    }
}
