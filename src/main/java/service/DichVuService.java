package service;

import dao.DichVuDAO;
import entity.DichVu;
import java.sql.SQLException;
import java.util.List;

public class DichVuService {
    private final DichVuDAO dichVuDAO = new DichVuDAO();

    public List<DichVu> layTatCa() throws SQLException {
        return dichVuDAO.getAllDichVu();
    }

    public String sinhMa(List<DichVu> danhSach) {
        int max = 0;
        for (DichVu dichVu : danhSach) {
            try {
                max = Math.max(max, Integer.parseInt(dichVu.getMaDichVu().substring(2)));
            } catch (RuntimeException ignored) {
                // Bỏ qua mã không theo định dạng DV###.
            }
        }
        return String.format("DV%03d", max + 1);
    }

    public DichVu taoDichVu(String ma, String ten, String donGiaText, String baoHanhText, String moTaText) {
        ten = trim(ten);
        if (ten.isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (ten.length() > 150) {
            throw new IllegalArgumentException("Tên dịch vụ tối đa 150 ký tự.");
        }

        String digits = donGiaText == null ? "" : donGiaText.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng nhập đơn giá hợp lệ (số tiền không âm).");
        }
        double donGia;
        try {
            donGia = Double.parseDouble(digits);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Vui lòng nhập đơn giá hợp lệ (số tiền không âm).");
        }
        if (!Double.isFinite(donGia)) {
            throw new IllegalArgumentException("Vui lòng nhập đơn giá hợp lệ (số tiền không âm).");
        }

        int baoHanh;
        try {
            String value = trim(baoHanhText);
            baoHanh = value.isEmpty() ? 0 : Integer.parseInt(value);
            if (baoHanh < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Thời gian bảo hành phải là số tháng không âm.");
        }

        String moTa = trim(moTaText);
        if (moTa.length() > 500) {
            throw new IllegalArgumentException("Mô tả tối đa 500 ký tự.");
        }
        return new DichVu(trim(ma), ten, donGia, baoHanh, moTa.isEmpty() ? null : moTa);
    }

    public boolean them(DichVu dichVu) throws SQLException {
        if (exists(dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Mã dịch vụ đã tồn tại.");
        }
        return dichVuDAO.insertDichVu(dichVu);
    }

    public boolean capNhat(DichVu dichVu) throws SQLException {
        if (!exists(dichVu.getMaDichVu())) {
            throw new IllegalArgumentException("Không tìm thấy dịch vụ cần cập nhật.");
        }
        return dichVuDAO.updateDichVu(dichVu);
    }

    public boolean xoa(String ma) throws SQLException {
        return dichVuDAO.deleteDichVu(ma);
    }

    private boolean exists(String ma) throws SQLException {
        for (DichVu dichVu : dichVuDAO.getAllDichVu()) {
            if (dichVu.getMaDichVu().equalsIgnoreCase(ma)) {
                return true;
            }
        }
        return false;
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
