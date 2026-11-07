package service;

import dao.HoaDonDAO;
import entity.DongTinhTien;
import entity.DotHoaDon;
import entity.HoaDonTraCuu;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class InvoiceSearchService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public String validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            return "Ngày bắt đầu không được sau ngày kết thúc.";
        }
        return null;
    }

    public List<HoaDonTraCuu> timKiemHoaDon(String keyword, String paymentMethod,
            String paymentStatus, LocalDate from, LocalDate to) throws SQLException {
        String validationError = validateDateRange(from, to);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        return hoaDonDAO.timKiemHoaDon(keyword, paymentMethod, paymentStatus, from, to);
    }

    public List<DongTinhTien> layChiTietDichVuHoaDon(String invoiceId) throws SQLException {
        return hoaDonDAO.layChiTietDichVuHoaDon(invoiceId);
    }

    public List<DongTinhTien> layChiTietThuocHoaDon(String invoiceId) throws SQLException {
        return hoaDonDAO.layChiTietThuocHoaDon(invoiceId);
    }

    public List<DotHoaDon> layDotThanhToanHoaDon(String invoiceId) throws SQLException {
        return hoaDonDAO.layDotThanhToanHoaDon(invoiceId);
    }
}
