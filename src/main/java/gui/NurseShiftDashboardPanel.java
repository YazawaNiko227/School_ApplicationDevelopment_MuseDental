/* trinhKiet */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class NurseShiftDashboardPanel extends JPanel {

    // Màu sắc chuẩn theo hệ thống Muse Dental
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);

    private JComboBox<String> cbShiftFilter;
    private JComboBox<String> cbRoomFilter;
    private JComboBox<String> cbStatusFilter;
    private JTextField txtSearch;
    private JTable shiftTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotalShifts;
    private JLabel lblMorningCount;
    private JLabel lblAfternoonCount;
    private JLabel lblNightCount;

    public NurseShiftDashboardPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        initUI();
        loadMockData();
    }

    private void initUI() {
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // 1. CÁC THẺ KPI TỔNG QUAN CA TRỰC
        topContainer.add(createSummaryMetrics());
        topContainer.add(Box.createVerticalStrut(14));

        // 2. THANH LỌC VÀ TÌM KIẾM
        topContainer.add(createFilterCard());
        add(topContainer, BorderLayout.NORTH);

        // 3. BẢNG PHÂN CÔNG CA TRỰC
        add(createTableSection(), BorderLayout.CENTER);
    }

    private JPanel createSummaryMetrics() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);

        lblTotalShifts = new JLabel("0", SwingConstants.LEFT);
        lblMorningCount = new JLabel("0", SwingConstants.LEFT);
        lblAfternoonCount = new JLabel("0", SwingConstants.LEFT);
        lblNightCount = new JLabel("0", SwingConstants.LEFT);

        panel.add(createMetricCard("TỔNG CA TRỰC HÔM NAY", lblTotalShifts, "Tất cả các phòng", "TC",
                new Color(231, 246, 245)));
        panel.add(createMetricCard("CA SÁNG (07:30 - 11:30)", lblMorningCount, "Đang vận hành", "CS",
                new Color(237, 242, 255)));
        panel.add(createMetricCard("CA CHIỀU (13:30 - 17:30)", lblAfternoonCount, "Sắp diễn ra", "CC",
                new Color(255, 246, 232)));
        panel.add(createMetricCard("CA TỐI (17:30 - 20:30)", lblNightCount, "Đã xếp lịch", "CT",
                new Color(253, 237, 244)));
        return panel;
    }

    private JPanel createMetricCard(String title, JLabel valueLabel, String note, String badge, Color badgeBg) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 10));
        lblTitle.setForeground(MUTED);

        JLabel lblBadge = new JLabel(badge, SwingConstants.CENTER);
        lblBadge.setFont(new Font("Arial", Font.BOLD, 12));
        lblBadge.setForeground(TEAL);
        lblBadge.setOpaque(true);
        lblBadge.setBackground(badgeBg);
        lblBadge.setPreferredSize(new Dimension(30, 30));

        top.add(lblTitle, BorderLayout.WEST);
        top.add(lblBadge, BorderLayout.EAST);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));

        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));
        valueLabel.setForeground(INK);

        JLabel lblNote = new JLabel(note);
        lblNote.setFont(new Font("Arial", Font.PLAIN, 10));
        lblNote.setForeground(MUTED);

        bottom.add(valueLabel);
        bottom.add(Box.createVerticalStrut(3));
        bottom.add(lblNote);

        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFilterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 16, 12, 16)));

        // HÀNG 1: BỘ LỌC CHÍNH (Ngày, Ca, Phòng, Trạng thái)
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        JLabel lblDate = new JLabel("Ngày: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        lblDate.setFont(new Font("Arial", Font.BOLD, 12));
        lblDate.setForeground(INK);

        cbShiftFilter = new JComboBox<>(new String[] { "Tất cả ca trực", "Ca sáng (07:30 - 11:30)",
                "Ca chiều (13:30 - 17:30)", "Ca tối (17:30 - 20:30)" });
        cbRoomFilter = new JComboBox<>(new String[] { "Tất cả phòng khám", "Phòng 01 (Tổng quát)",
                "Phòng 02 (Chỉnh nha)", "Phòng 03 (Tiểu phẫu)", "Phòng Vô trùng" });
        cbStatusFilter = new JComboBox<>(
                new String[] { "Tất cả trạng thái", "Đang trực", "Chờ nhận ca", "Hoàn thành", "Vắng / Nghỉ phép" });

        cbShiftFilter.setPreferredSize(new Dimension(170, 32));
        cbRoomFilter.setPreferredSize(new Dimension(165, 32));
        cbStatusFilter.setPreferredSize(new Dimension(145, 32));

        filterRow.add(lblDate);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(new JLabel("Ca:"));
        filterRow.add(cbShiftFilter);
        filterRow.add(new JLabel("Phòng:"));
        filterRow.add(cbRoomFilter);
        filterRow.add(new JLabel("Trạng thái:"));
        filterRow.add(cbStatusFilter);

        // HÀNG 2: TÌM KIẾM VÀ NÚT THAO TÁC
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(260, 32));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Nhập mã hoặc tên Y tá cần tìm...");

        JButton btnFilter = new JButton("Lọc");
        btnFilter.setFont(new Font("Arial", Font.BOLD, 11));
        btnFilter.setBackground(TEAL);
        btnFilter.setForeground(Color.BLACK);
        btnFilter.setFocusPainted(false);
        btnFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFilter.setPreferredSize(new Dimension(110, 32));
        btnFilter.addActionListener(e -> filterData());

        JButton btnReset = new JButton("Đặt lại");
        btnReset.setFont(new Font("Arial", Font.PLAIN, 11));
        btnReset.setBackground(CANVAS);
        btnReset.setForeground(INK);
        btnReset.setFocusPainted(false);
        btnReset.setPreferredSize(new Dimension(80, 32));
        btnReset.addActionListener(e -> resetFilter());

        actionRow.add(lblSearch);
        actionRow.add(txtSearch);
        actionRow.add(btnFilter);
        actionRow.add(btnReset);

        // Gom 2 hàng vào card, cách nhau 10px
        card.add(filterRow);
        card.add(Box.createVerticalStrut(10));
        card.add(actionRow);

        return card;
    }

    private JPanel createTableSection() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(LINE));

        String[] headers = {
                "Mã Ca", "Mã Y tá", "Họ và tên Y tá", "Ca trực", "Thời gian", "Phòng phân công", "Bác sĩ phụ trách",
                "Trạng thái ca"
        };

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        shiftTable = new JTable(tableModel);
        shiftTable.setFont(new Font("Arial", Font.PLAIN, 12));
        shiftTable.setRowHeight(34);
        shiftTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        shiftTable.setShowGrid(true);
        shiftTable.setGridColor(new Color(240, 243, 245));

        JTableHeader tableHeader = shiftTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 11));
        tableHeader.setBackground(new Color(245, 248, 250));
        tableHeader.setForeground(INK);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        shiftTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        shiftTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        shiftTable.getColumnModel().getColumn(3).setCellRenderer(centerRenderer);
        shiftTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);

        // Hiển thị nhãn màu cho cột Trạng thái
        shiftTable.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 11));

                String status = value == null ? "" : value.toString();
                if ("Đang trực".equals(status)) {
                    label.setForeground(new Color(15, 125, 125));
                } else if ("Chờ nhận ca".equals(status)) {
                    label.setForeground(new Color(180, 115, 20));
                } else if ("Hoàn thành".equals(status)) {
                    label.setForeground(new Color(40, 140, 60));
                } else {
                    label.setForeground(new Color(180, 50, 50));
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(shiftTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private Object[][] getRawShiftData() {
        return new Object[][] {
                { "CA-01", "YT001", "Trần Thị Mai", "Ca sáng", "07:30 - 11:30", "Phòng khám 01 (Tổng quát)",
                        "BS. Nguyễn Văn Hùng", "Đang trực" },
                { "CA-02", "YT002", "Đỗ Kim Ngân", "Ca sáng", "07:30 - 11:30", "Phòng khám 02 (Chỉnh nha)",
                        "BS. Lê Hoàng Linh", "Đang trực" },
                { "CA-03", "YT003", "Nguyễn Thị Hương", "Ca sáng", "07:30 - 11:30", "Phòng Vô trùng", "BS. Phan Tuấn",
                        "Đang trực" },
                { "CA-04", "YT004", "Phạm Bích Trâm", "Ca chiều", "13:30 - 17:30", "Phòng khám 01 (Tổng quát)",
                        "BS. Nguyễn Văn Hùng", "Chờ nhận ca" },
                { "CA-05", "YT005", "Vũ Hoàng Yến", "Ca chiều", "13:30 - 17:30", "Phòng khám 03 (Tiểu phẫu)",
                        "BS. Trần Minh", "Chờ nhận ca" },
                { "CA-06", "YT006", "Hoàng Thu Thảo", "Ca chiều", "13:30 - 17:30", "Phòng khám 02 (Chỉnh nha)",
                        "BS. Lê Hoàng Linh", "Chờ nhận ca" },
                { "CA-07", "YT001", "Trần Thị Mai", "Ca tối", "17:30 - 20:30", "Phòng khám 01 (Tổng quát)",
                        "BS. Phan Tuấn", "Chờ nhận ca" },
                { "CA-08", "YT007", "Lê Thanh Tuyền", "Ca tối", "17:30 - 20:30", "Phòng Vô trùng", "BS. Trần Minh",
                        "Chờ nhận ca" }
        };
    }

    private void loadMockData() {
        tableModel.setRowCount(0);
        Object[][] data = getRawShiftData();
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        recalculateMetrics();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedShift = cbShiftFilter.getSelectedItem().toString();
        String selectedRoom = cbRoomFilter.getSelectedItem().toString();
        String selectedStatus = cbStatusFilter.getSelectedItem().toString();

        tableModel.setRowCount(0);
        Object[][] data = getRawShiftData();

        for (Object[] row : data) {
            String nurseId = row[1].toString().toLowerCase();
            String nurseName = row[2].toString().toLowerCase();
            String shiftName = row[3].toString();
            String room = row[5].toString();
            String status = row[7].toString();

            boolean matchKeyword = keyword.isEmpty() || nurseId.contains(keyword) || nurseName.contains(keyword);
            boolean matchShift = selectedShift.startsWith("Tất cả") || selectedShift.contains(shiftName);
            boolean matchRoom = selectedRoom.startsWith("Tất cả") || room.equals(selectedRoom);
            boolean matchStatus = selectedStatus.startsWith("Tất cả") || status.equals(selectedStatus);

            if (matchKeyword && matchShift && matchRoom && matchStatus) {
                tableModel.addRow(row);
            }
        }
        recalculateMetrics();
    }

    private void resetFilter() {
        txtSearch.setText("");
        cbShiftFilter.setSelectedIndex(0);
        cbRoomFilter.setSelectedIndex(0);
        cbStatusFilter.setSelectedIndex(0);
        loadMockData();
    }

    private void recalculateMetrics() {
        int morning = 0;
        int afternoon = 0;
        int night = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String shift = tableModel.getValueAt(i, 3).toString();
            if ("Ca sáng".equals(shift)) {
                morning++;
            } else if ("Ca chiều".equals(shift)) {
                afternoon++;
            } else if ("Ca tối".equals(shift)) {
                night++;
            }
        }

        lblTotalShifts.setText(String.format("%02d", tableModel.getRowCount()));
        lblMorningCount.setText(String.format("%02d", morning));
        lblAfternoonCount.setText(String.format("%02d", afternoon));
        lblNightCount.setText(String.format("%02d", night));
    }
}
/* end trinhKiet */