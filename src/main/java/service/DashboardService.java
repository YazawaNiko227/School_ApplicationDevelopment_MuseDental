package service;

import dao.DashboardDAO;
import entity.LichHenTongQuan;
import entity.ThongKeTongQuan;
import java.sql.SQLException;
import java.util.List;

public class DashboardService {
    private static final int SO_LICH_HEN_SAP_TOI = 4;

    private final DashboardDAO dashboardDAO = new DashboardDAO();

    public ThongKeTongQuan layThongKeHomNay() throws SQLException {
        return dashboardDAO.layThongKeHomNay();
    }

    public List<LichHenTongQuan> layLichHenSapToi() throws SQLException {
        return dashboardDAO.layLichHenSapToi(SO_LICH_HEN_SAP_TOI);
    }
}
