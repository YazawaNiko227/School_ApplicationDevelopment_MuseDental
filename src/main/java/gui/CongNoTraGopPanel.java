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
import java.text.DecimalFormat;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class CongNoTraGopPanel extends JPanel {

    // Màu sắc theo chuẩn hệ thống Muse Dental
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);

    private JTextField txtSearch;
    private JComboBox<String> cbStatusFilter;
    private JComboBox<String> cbDueFilter;
    private JTable debtTable;
    private DefaultTableModel tableModel;

    private JLabel lblTotalDebt;
    private JLabel lblActiveCount;
    private JLabel lblOverdueCount;
    private JLabel lblCollectedMonth;

    private final DecimalFormat moneyFormat = new DecimalFormat("#,### đ");

    public CongNoTraGopPanel() {
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

        // 1. Thẻ chỉ số tổng quan công nợ
        topContainer.add(createSummaryCards());
        topContainer.add(Box.createVerticalStrut(14));

        // 2. Bộ lọc và tìm kiếm hồ sơ trả góp
        topContainer.add(createFilterCard());
        add(topContainer, BorderLayout.NORTH);

        // 3. Bảng danh sách công nợ & trả góp
        add(createTableSection(), BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);

        lblTotalDebt = new JLabel("0 đ", SwingConstants.LEFT);
        lblActiveCount = new JLabel("0", SwingConstants.LEFT);
        lblOverdueCount = new JLabel("0", SwingConstants.LEFT);
        lblCollectedMonth = new JLabel("0 đ", SwingConstants.LEFT);

        panel.add(createMetricCard("TỔNG CÔNG NỢ ĐANG THU", lblTotalDebt, "Tất cả hợp đồng chưa tất toán", "CN",
                new Color(253, 237, 244)));
        panel.add(createMetricCard("HỒ SƠ ĐANG TRẢ GÓP", lblActiveCount, "Hợp đồng hoạt động", "TG",
                new Color(231, 246, 245)));
        panel.add(createMetricCard("KỲ ĐẾN HẠN / QUÁ HẠN", lblOverdueCount, "Cần gọi nhắc lịch hẹn", "QH",
                new Color(255, 246, 232)));
        panel.add(createMetricCard("ĐÃ THU TRONG THÁNG", lblCollectedMonth, "Tổng các đợt đã đóng", "TT",
                new Color(237, 242, 255)));
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

        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
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

        // Hàng 1: Bộ lọc trạng thái & hạn kỳ
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        cbStatusFilter = new JComboBox<>(
                new String[] { "Tất cả trạng thái", "Đang trả góp", "Quá hạn kỳ", "Đã tất toán" });
        cbDueFilter = new JComboBox<>(
                new String[] { "Tất cả hạn đóng", "Hôm nay", "Trong tuần này", "Trong tháng này" });

        cbStatusFilter.setPreferredSize(new Dimension(170, 32));
        cbDueFilter.setPreferredSize(new Dimension(170, 32));

        filterRow.add(new JLabel("Trạng thái nợ:"));
        filterRow.add(cbStatusFilter);
        filterRow.add(Box.createHorizontalStrut(10));
        filterRow.add(new JLabel("Hạn thanh toán:"));
        filterRow.add(cbDueFilter);

        // Hàng 2: Tìm kiếm & Thao tác
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm bệnh nhân:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(280, 32));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Mã hợp đồng, mã BN, tên hoặc SĐT...");
        txtSearch.addActionListener(e -> filterData());

        JButton btnFilter = new JButton("Lọc");
        btnFilter.setFont(new Font("Arial", Font.BOLD, 12));
        btnFilter.setBackground(TEAL);
        btnFilter.setForeground(Color.BLACK);
        btnFilter.setFocusPainted(false);
        btnFilter.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFilter.setPreferredSize(new Dimension(80, 32));
        btnFilter.addActionListener(e -> filterData());

        JButton btnReset = new JButton("Đặt lại");
        btnReset.setFont(new Font("Arial", Font.PLAIN, 12));
        btnReset.setBackground(CANVAS);
        btnReset.setForeground(INK);
        btnReset.setFocusPainted(false);
        btnReset.setPreferredSize(new Dimension(80, 32));
        btnReset.addActionListener(e -> resetFilter());

        JButton btnCollect = new JButton("Ghi nhận thu tiền đợt");
        btnCollect.setFont(new Font("Arial", Font.BOLD, 12));
        btnCollect.setBackground(new Color(40, 167, 69));
        btnCollect.setForeground(Color.BLACK);
        btnCollect.setFocusPainted(false);
        btnCollect.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCollect.setPreferredSize(new Dimension(170, 32));
        btnCollect.addActionListener(e -> openPaymentDialog());

        actionRow.add(lblSearch);
        actionRow.add(txtSearch);
        actionRow.add(btnFilter);
        actionRow.add(btnReset);
        actionRow.add(Box.createHorizontalStrut(15));
        actionRow.add(btnCollect);

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
                "Mã HĐ", "Mã BN", "Họ và tên bệnh nhân", "Dịch vụ điều trị",
                "Tổng chi phí", "Đã thanh toán", "Còn nợ", "Kỳ hạn tiếp", "Hạn thanh toán", "Trạng thái"
        };

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        debtTable = new JTable(tableModel);
        debtTable.setFont(new Font("Arial", Font.PLAIN, 12));
        debtTable.setRowHeight(34);
        debtTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        debtTable.setShowGrid(true);
        debtTable.setGridColor(new Color(240, 243, 245));

        JTableHeader tableHeader = debtTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 11));
        tableHeader.setBackground(new Color(245, 248, 250));
        tableHeader.setForeground(INK);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        debtTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        debtTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        debtTable.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);
        debtTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        debtTable.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        debtTable.getColumnModel().getColumn(5).setCellRenderer(rightRenderer);
        debtTable.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        // Hiển thị màu cho cột Trạng thái
        debtTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 11));

                String st = value == null ? "" : value.toString();
                if ("Đang trả góp".equals(st)) {
                    label.setForeground(new Color(15, 125, 125));
                } else if ("Quá hạn kỳ".equals(st)) {
                    label.setForeground(new Color(210, 45, 45));
                } else if ("Đã tất toán".equals(st)) {
                    label.setForeground(new Color(40, 140, 60));
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(debtTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private Object[][] getRawDebtData() {
        return new Object[][] {
                { "HD-001", "BN000001", "Nguyễn Thị Minh Anh", "Niềng răng mắc cài kim loại", 35000000.0, 15000000.0,
                        20000000.0, "Đợt 3 / 6", "25/10/2026", "Đang trả góp" },
                { "HD-002", "BN000002", "Trần Hoàng Nam", "Trồng răng Implant Straumann", 42000000.0, 30000000.0,
                        12000000.0, "Đợt 2 / 3", "18/10/2026", "Quá hạn kỳ" },
                { "HD-003", "BN000003", "Lê Ngọc Hà", "Bọc răng sứ Cercon HT (4 răng)", 24000000.0, 24000000.0, 0.0,
                        "Hoàn tất", "10/09/2026", "Đã tất toán" },
                { "HD-004", "BN000004", "Phạm Gia Bảo", "Chỉnh nha tiền tăng trưởng", 18000000.0, 6000000.0, 12000000.0,
                        "Đợt 2 / 4", "05/11/2026", "Đang trả góp" },
                { "HD-005", "BN000005", "Đỗ Thanh Bình", "Cấy ghép Implant phục hình", 55000000.0, 25000000.0,
                        30000000.0, "Đợt 2 / 5", "30/10/2026", "Đang trả góp" }
        };
    }

    private void loadMockData() {
        tableModel.setRowCount(0);
        Object[][] data = getRawDebtData();
        for (Object[] row : data) {
            tableModel.addRow(formatRow(row));
        }
        recalculateMetrics();
    }

    private Object[] formatRow(Object[] row) {
        return new Object[] {
                row[0], row[1], row[2], row[3],
                moneyFormat.format((Double) row[4]),
                moneyFormat.format((Double) row[5]),
                moneyFormat.format((Double) row[6]),
                row[7], row[8], row[9]
        };
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedStatus = cbStatusFilter.getSelectedItem().toString();

        tableModel.setRowCount(0);
        Object[][] raw = getRawDebtData();

        for (Object[] row : raw) {
            String hd = row[0].toString().toLowerCase();
            String maBn = row[1].toString().toLowerCase();
            String tenBn = row[2].toString().toLowerCase();
            String service = row[3].toString().toLowerCase();
            String status = row[9].toString();

            boolean matchKeyword = keyword.isEmpty() || hd.contains(keyword) || maBn.contains(keyword)
                    || tenBn.contains(keyword) || service.contains(keyword);

            boolean matchStatus = selectedStatus.startsWith("Tất cả") || status.equals(selectedStatus);

            if (matchKeyword && matchStatus) {
                tableModel.addRow(formatRow(row));
            }
        }
        recalculateMetrics();
    }

    private void resetFilter() {
        txtSearch.setText("");
        cbStatusFilter.setSelectedIndex(0);
        cbDueFilter.setSelectedIndex(0);
        loadMockData();
    }

    private void recalculateMetrics() {
        double totalDebt = 0;
        int activeCount = 0;
        int overdueCount = 0;
        double collected = 0;

        Object[][] raw = getRawDebtData();
        for (Object[] row : raw) {
            double debt = (Double) row[6];
            double paid = (Double) row[5];
            String st = row[9].toString();

            totalDebt += debt;
            collected += paid;

            if ("Đang trả góp".equals(st)) {
                activeCount++;
            } else if ("Quá hạn kỳ".equals(st)) {
                overdueCount++;
                activeCount++;
            }
        }

        lblTotalDebt.setText(moneyFormat.format(totalDebt));
        lblActiveCount.setText(String.format("%02d hồ sơ", activeCount));
        lblOverdueCount.setText(String.format("%02d kỳ", overdueCount));
        lblCollectedMonth.setText(moneyFormat.format(collected));
    }

    private void openPaymentDialog() {
        int selectedRow = debtTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Vui lòng chọn một hợp đồng trên bảng để ghi nhận thu tiền đợt!",
                    "Chưa chọn hồ sơ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String contractId = tableModel.getValueAt(selectedRow, 0).toString();
        String patientName = tableModel.getValueAt(selectedRow, 2).toString();
        String remainingDebt = tableModel.getValueAt(selectedRow, 6).toString();
        String nextInstallment = tableModel.getValueAt(selectedRow, 7).toString();

        String inputAmount = JOptionPane.showInputDialog(this,
                "Ghi nhận thanh toán cho: " + patientName + " (" + contractId + ")\n"
                        + "Kỳ thanh toán: " + nextInstallment + "\n"
                        + "Số tiền còn nợ: " + remainingDebt + "\n\n"
                        + "Nhập số tiền thu đợt này (VNĐ):",
                "Ghi nhận đợt thanh toán", JOptionPane.QUESTION_MESSAGE);

        if (inputAmount != null && !inputAmount.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Ghi nhận thanh toán thành công " + inputAmount.trim() + " đ cho hợp đồng " + contractId + "!",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
/* end trinhKiet */