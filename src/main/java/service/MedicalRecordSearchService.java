package service;

import dao.BanGhiBenhAnDAO;
import entity.BanGhiBenhAn;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MedicalRecordSearchService {
    private final BanGhiBenhAnDAO benhAnDAO = new BanGhiBenhAnDAO();

    public List<String> layDanhSachNhaSi() throws SQLException {
        return benhAnDAO.layDanhSachNhaSi();
    }

    public String validateDateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            return "Ngày bắt đầu không được sau ngày kết thúc.";
        }
        return null;
    }

    public List<BanGhiBenhAn> timKiem(String keyword, String dentist, LocalDate from, LocalDate to)
            throws SQLException {
        String validationError = validateDateRange(from, to);
        if (validationError != null) {
            throw new IllegalArgumentException(validationError);
        }
        return benhAnDAO.timKiem(keyword, dentist, from, to);
    }
}
