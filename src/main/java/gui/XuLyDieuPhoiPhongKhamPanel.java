package gui;

import dao.DieuPhoiPhongKhamDAO;
import dao.DieuPhoiPhongKhamDAO.LichHenDieuPhoi;
import dao.DieuPhoiPhongKhamDAO.PhongDieuPhoi;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Dashboard điều phối phòng khám" (bước 4 trong luồng khám bệnh chính).
 *
 * Lễ tân thực hiện:
 * 1. Bệnh nhân đến -> chọn lịch "Đã xác nhận", bấm "Check-in"
 * -> LichHen = "Đã check-in", ghi ThoiGianCheckIn.
 * 2. Bấm "Điều phối vào phòng" -> chọn một phòng "Sẵn sàng" cho lịch đó.
 * 3. Bệnh nhân không đến -> bấm "Đánh dấu vắng" -> LichHen = "Vắng".
 *
 * Việc chuyển sang "Đang khám" do nha sĩ làm ở màn hình khám lâm sàng,
 * nên panel này KHÔNG đổi trạng thái phòng.
 *
 * Lịch hẹn và tình trạng phòng được đọc, cập nhật qua DieuPhoiPhongKhamDAO.
 */
public class XuLyDieuPhoiPhongKhamPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");

    // Trạng thái lịch hẹn (hiển thị có dấu, DB lưu không dấu).
    private static final String CHO_XAC_NHAN = "Chờ xác nhận";
    private static final String DA_XAC_NHAN = "Đã xác nhận";
    private static final String DA_CHECK_IN = "Đã check-in";
    private static final String DANG_KHAM = "Đang khám";
    private static final String HOAN_THANH = "Hoàn thành";
    private static final String HUY = "Hủy";
    private static final String VANG = "Vắng";
    private static final String TAT_CA = "Tất cả";

    // Trạng thái phòng.
    private static final String SAN_SANG = "Sẵn sàng";
    private static final String CAN_VE_SINH = "Cần vệ sinh";
    private static final String BAO_TRI = "Bảo trì";

    // Chỉ số cột trong mỗi dòng lịch hẹn (Object[]).
    private static final int C_MA = 0;
    private static final int C_GIO_HEN = 1; // LocalDateTime
    private static final int C_BENH_NHAN = 2;
    private static final int C_NHA_SI = 3;
    private static final int C_PHONG = 4;
    private static final int C_TRANG_THAI = 5;
    private static final int C_CHECK_IN = 6; // LocalDateTime hoặc null

    private final List<Object[]> dsLichHen = new ArrayList<>();
    private final List<Object[]> dsPhong = new ArrayList<>(); // {mã, tên, trạng thái}
    private final DieuPhoiPhongKhamDAO dieuPhoiDAO = new DieuPhoiPhongKhamDAO();

    private final DefaultTableModel lichModel = new DefaultTableModel(
            new String[] { "Mã lịch", "Giờ hẹn", "Bệnh nhân", "Nha sĩ", "Phòng", "Trạng thái", "Check-in lúc",
                    "Chờ (phút)" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lichTable = new JTable(lichModel);

    private final DefaultTableModel phongModel = new DefaultTableModel(
            new String[] { "Mã phòng", "Tên phòng", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable phongTable = new JTable(phongModel);

    private final JComboBox<String> cboLoc = new JComboBox<>(new String[] {
            TAT_CA, CHO_XAC_NHAN, DA_XAC_NHAN, DA_CHECK_IN, DANG_KHAM, HOAN_THANH, HUY, VANG });

    // 4 thẻ thống kê: Chưa đến, Đang chờ, Đang khám, Hoàn thành.
    private final JLabel[] summaryValues = new JLabel[4];

    public XuLyDieuPhoiPhongKhamPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createLichPanel(), BorderLayout.CENTER);
        center.add(createPhongPanel(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        lamMoi();

        // Tự cập nhật thời gian chờ mỗi 30 giây.
        Timer timer = new Timer(30000, event -> lamMoi());
        timer.start();
    }

    // ===== Giao diện =====
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard(0, "CHƯA ĐẾN", MUTED));
        panel.add(createSummaryCard(1, "ĐANG CHỜ", ORANGE));
        panel.add(createSummaryCard(2, "ĐANG KHÁM", BLUE));
        panel.add(createSummaryCard(3, "HOÀN THÀNH", TEAL));
        return panel;
    }

    private JPanel createSummaryCard(int index, String title, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(180, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        summaryValues[index] = new JLabel("0");
        summaryValues[index].setFont(new Font("Arial", Font.BOLD, 24));
        summaryValues[index].setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(summaryValues[index]);
        return card;
    }

    // Bảng lịch hẹn hôm nay + các nút thao tác.
    private JPanel createLichPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Lịch hẹn hôm nay");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        filterLabel.setForeground(MUTED);
        cboLoc.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLoc.addActionListener(event -> lamMoi());
        filterPanel.add(filterLabel);
        filterPanel.add(cboLoc);
        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        panel.add(header, BorderLayout.NORTH);

        styleTable(lichTable);
        lichTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(lichTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnVang = createButton("Đánh dấu vắng", false);
        JButton btnDieuPhoi = createButton("Điều phối vào phòng", false);
        JButton btnCheckIn = createButton("Check-in", true);
        btnVang.addActionListener(event -> danhDauVang());
        btnDieuPhoi.addActionListener(event -> dieuPhoiVaoPhong());
        btnCheckIn.addActionListener(event -> checkIn());
        actions.add(btnVang);
        actions.add(btnDieuPhoi);
        actions.add(btnCheckIn);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    // Bảng trạng thái phòng (chỉ xem, để lễ tân biết phòng nào còn trống).
    private JPanel createPhongPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 16, 18)));
        panel.setPreferredSize(new Dimension(380, 0));

        JLabel title = new JLabel("Tình trạng phòng");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        panel.add(title, BorderLayout.NORTH);

        styleTable(phongTable);
        phongTable.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(phongTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        panel.add(scroll, BorderLayout.CENTER);

        JLabel hint = new JLabel("<html>Phòng chuyển sang <b>Đang khám</b> khi nha sĩ bấm \"Vào khám\".</html>");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        panel.add(hint, BorderLayout.SOUTH);
        return panel;
    }

    // ===== Xử lý nghiệp vụ =====

    /** Bệnh nhân đến: "Đã xác nhận" -> "Đã check-in", ghi giờ đến. */
    private void checkIn() {
        Object[] lich = layLichDangChon();
        if (lich == null) {
            return;
        }
        if (CHO_XAC_NHAN.equals(lich[C_TRANG_THAI])) {
            showMessage("Lịch này chưa được xác nhận. Hãy xác nhận ở màn hình Đặt lịch hẹn khám trước.");
            return;
        }
        if (!DA_XAC_NHAN.equals(lich[C_TRANG_THAI])) {
            showMessage("Chỉ check-in được lịch đang ở trạng thái \"Đã xác nhận\".");
            return;
        }
        String maLichHen = (String) lich[C_MA];
        String maPhong = (String) lich[C_PHONG];
        try {
            if (!dieuPhoiDAO.checkIn(maLichHen)) {
                showMessage("Lịch hẹn đã được cập nhật ở nơi khác. Danh sách sẽ được tải lại.");
                lamMoi();
                return;
            }
            lamMoi();
            chonLaiDong(maLichHen);
        } catch (SQLException ex) {
            showDatabaseError("Không thể check-in lịch hẹn.", ex);
            return;
        }

        // Nhắc nhẹ nếu phòng đã hẹn chưa sẵn sàng.
        String trangThaiPhong = layTrangThaiPhong(maPhong);
        if (!SAN_SANG.equals(trangThaiPhong)) {
            showMessage("Đã check-in. Lưu ý: phòng " + maPhong + " đang \"" + trangThaiPhong
                    + "\", hãy bấm \"Điều phối vào phòng\" để chuyển sang phòng khác.");
        }
    }

    /** Chọn phòng "Sẵn sàng" cho bệnh nhân đã check-in. */
    private void dieuPhoiVaoPhong() {
        Object[] lich = layLichDangChon();
        if (lich == null) {
            return;
        }
        if (!DA_CHECK_IN.equals(lich[C_TRANG_THAI])) {
            showMessage("Chỉ điều phối được bệnh nhân đang ở trạng thái \"Đã check-in\".");
            return;
        }

        List<String> phongTrong = new ArrayList<>();
        for (Object[] phong : dsPhong) {
            if (SAN_SANG.equals(phong[2])) {
                phongTrong.add(phong[0] + " - " + phong[1]);
            }
        }
        if (phongTrong.isEmpty()) {
            showMessage("Hiện không có phòng nào ở trạng thái \"Sẵn sàng\".");
            return;
        }

        Object chon = JOptionPane.showInputDialog(this,
                "Chọn phòng cho bệnh nhân " + lich[C_BENH_NHAN] + ":",
                "Điều phối vào phòng", JOptionPane.QUESTION_MESSAGE, null,
                phongTrong.toArray(), phongTrong.get(0));
        if (chon == null) {
            return;
        }
        String maPhong = chon.toString().split(" - ")[0];
        String maLichHen = (String) lich[C_MA];
        String tenBenhNhan = (String) lich[C_BENH_NHAN];
        try {
            dieuPhoiDAO.doiPhong(maLichHen, maPhong);
            lamMoi();
            chonLaiDong(maLichHen);
            showMessage("Đã điều phối " + tenBenhNhan + " vào phòng " + maPhong
                    + ". Nha sĩ sẽ bấm \"Vào khám\" để bắt đầu.");
        } catch (SQLException ex) {
            showDatabaseError("Không thể điều phối lịch hẹn vào phòng.", ex);
            lamMoi();
        }
    }

    /** Bệnh nhân không đến: chuyển lịch sang "Vắng". */
    private void danhDauVang() {
        Object[] lich = layLichDangChon();
        if (lich == null) {
            return;
        }
        if (!DA_XAC_NHAN.equals(lich[C_TRANG_THAI]) && !CHO_XAC_NHAN.equals(lich[C_TRANG_THAI])) {
            showMessage("Chỉ đánh dấu vắng được lịch chưa check-in.");
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Đánh dấu bệnh nhân " + lich[C_BENH_NHAN] + " vắng mặt?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            String trangThaiHienTai = CHO_XAC_NHAN.equals(lich[C_TRANG_THAI])
                    ? "Cho xac nhan"
                    : "Da xac nhan";
            try {
                if (!dieuPhoiDAO.danhDauVang((String) lich[C_MA], trangThaiHienTai)) {
                    showMessage("Lịch hẹn đã được cập nhật ở nơi khác. Danh sách sẽ được tải lại.");
                }
                lamMoi();
            } catch (SQLException ex) {
                showDatabaseError("Không thể đánh dấu vắng lịch hẹn.", ex);
            }
        }
    }

    // ===== Hàm hỗ trợ =====

    // Nạp lại cả hai bảng và các thẻ thống kê.
    private void lamMoi() {
        String maDangChon = null;
        Object[] dangChon = layLichDangChonKhongBaoLoi();
        if (dangChon != null) {
            maDangChon = (String) dangChon[C_MA];
        }

        try {
            List<LichHenDieuPhoi> lichHomNay = dieuPhoiDAO.layLichHomNay();
            List<PhongDieuPhoi> phongKhamList = dieuPhoiDAO.layTatCaPhong();
            dsLichHen.clear();
            for (LichHenDieuPhoi lich : lichHomNay) {
                dsLichHen.add(new Object[] {
                        lich.getMaLichHen(),
                        lich.getThoiGianHen(),
                        lich.getTenBenhNhan(),
                        lich.getNhaSi(),
                        lich.getMaPhong(),
                        trangThaiHienThi(lich.getTrangThai()),
                        lich.getThoiGianCheckIn()
                });
            }
            dsPhong.clear();
            for (PhongDieuPhoi phong : phongKhamList) {
                dsPhong.add(new Object[] {
                        phong.getMaPhong(),
                        phong.getTenPhong(),
                        trangThaiPhongHienThi(phong.getTrangThai())
                });
            }
        } catch (SQLException ex) {
            showDatabaseError("Không thể tải dữ liệu điều phối phòng khám.", ex);
            return;
        }

        String loc = (String) cboLoc.getSelectedItem();
        lichModel.setRowCount(0);
        int chuaDen = 0;
        int dangCho = 0;
        int dangKham = 0;
        int hoanThanh = 0;

        for (Object[] lich : dsLichHen) {
            String trangThai = (String) lich[C_TRANG_THAI];
            if (DA_XAC_NHAN.equals(trangThai) || CHO_XAC_NHAN.equals(trangThai)) {
                chuaDen++;
            } else if (DA_CHECK_IN.equals(trangThai)) {
                dangCho++;
            } else if (DANG_KHAM.equals(trangThai)) {
                dangKham++;
            } else if (HOAN_THANH.equals(trangThai)) {
                hoanThanh++;
            }

            if (TAT_CA.equals(loc) || trangThai.equals(loc)) {
                LocalDateTime checkIn = (LocalDateTime) lich[C_CHECK_IN];
                String gioCheckIn = checkIn == null ? "-" : checkIn.format(GIO);
                // Chỉ tính thời gian chờ khi bệnh nhân đang chờ.
                String phutCho = "-";
                if (DA_CHECK_IN.equals(trangThai) && checkIn != null) {
                    phutCho = String.valueOf(Duration.between(checkIn, LocalDateTime.now()).toMinutes());
                }
                lichModel.addRow(new Object[] {
                        lich[C_MA],
                        ((LocalDateTime) lich[C_GIO_HEN]).format(GIO),
                        lich[C_BENH_NHAN],
                        lich[C_NHA_SI],
                        lich[C_PHONG],
                        trangThai,
                        gioCheckIn,
                        phutCho });
            }
        }

        summaryValues[0].setText(String.valueOf(chuaDen));
        summaryValues[1].setText(String.valueOf(dangCho));
        summaryValues[2].setText(String.valueOf(dangKham));
        summaryValues[3].setText(String.valueOf(hoanThanh));

        phongModel.setRowCount(0);
        for (Object[] phong : dsPhong) {
            phongModel.addRow(phong);
        }

        if (maDangChon != null) {
            chonLaiDong(maDangChon);
        }
    }

    private void chonLaiDong(String maLich) {
        for (int i = 0; i < lichModel.getRowCount(); i++) {
            if (maLich.equals(lichModel.getValueAt(i, 0))) {
                lichTable.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private Object[] layLichDangChon() {
        Object[] lich = layLichDangChonKhongBaoLoi();
        if (lich == null) {
            showMessage("Vui lòng chọn một lịch hẹn trong bảng.");
        }
        return lich;
    }

    private Object[] layLichDangChonKhongBaoLoi() {
        int row = lichTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String ma = (String) lichModel.getValueAt(row, 0);
        for (Object[] lich : dsLichHen) {
            if (lich[C_MA].equals(ma)) {
                return lich;
            }
        }
        return null;
    }

    private String layTrangThaiPhong(String maPhong) {
        for (Object[] phong : dsPhong) {
            if (phong[0].equals(maPhong)) {
                return (String) phong[2];
            }
        }
        return "";
    }

    private String trangThaiHienThi(String trangThai) {
        switch (trangThai) {
            case "Cho xac nhan":
                return CHO_XAC_NHAN;
            case "Da xac nhan":
                return DA_XAC_NHAN;
            case "Da check-in":
                return DA_CHECK_IN;
            case "Dang kham":
                return DANG_KHAM;
            case "Hoan thanh":
                return HOAN_THANH;
            case "Huy":
                return HUY;
            case "Vang":
                return VANG;
            default:
                throw new IllegalArgumentException("Trạng thái lịch hẹn không hợp lệ: " + trangThai);
        }
    }

    private String trangThaiPhongHienThi(String trangThai) {
        switch (trangThai) {
            case "San sang":
                return SAN_SANG;
            case "Dang kham":
                return "Đang khám";
            case "Can ve sinh":
                return CAN_VE_SINH;
            case "Bao tri":
                return BAO_TRI;
            default:
                throw new IllegalArgumentException("Trạng thái phòng không hợp lệ: " + trangThai);
        }
    }

    private void showDatabaseError(String message, SQLException ex) {
        String detail = ex.getMessage();
        JOptionPane.showMessageDialog(this,
                detail == null || detail.trim().isEmpty() ? message : message + "\n" + detail,
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        if (primary) {
            button.setBackground(TEAL);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(7, 15, 7, 15)));
        }
        return button;
    }

    // Tô màu chữ cho cột trạng thái (dùng cho cả lịch hẹn và phòng).
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (SAN_SANG.equals(text) || HOAN_THANH.equals(text) || DA_XAC_NHAN.equals(text)) {
                label.setForeground(TEAL);
            } else if (DA_CHECK_IN.equals(text) || CAN_VE_SINH.equals(text) || CHO_XAC_NHAN.equals(text)) {
                label.setForeground(ORANGE);
            } else if (DANG_KHAM.equals(text)) {
                label.setForeground(BLUE);
            } else if (BAO_TRI.equals(text) || VANG.equals(text) || HUY.equals(text)) {
                label.setForeground(RED);
            } else {
                label.setForeground(INK);
            }
            return label;
        }
    }
}