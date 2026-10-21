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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class GhiNhanVatTuTieuHaoPanel extends JPanel {

    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);

    private JTextField txtSearch;
    private JComboBox<String> cbRoomFilter;
    private JComboBox<String> cbTypeFilter;
    private JTable usageTable;
    private DefaultTableModel tableModel;

    private JLabel lblTotalUsage;
    private JLabel lblTotalItems;
    private JLabel lblWarningStock;
    private JLabel lblTopUsed;

    public GhiNhanVatTuTieuHaoPanel() {
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

        topContainer.add(createSummaryCards());
        topContainer.add(Box.createVerticalStrut(14));
        topContainer.add(createFilterCard());
        add(topContainer, BorderLayout.NORTH);

        add(createTableSection(), BorderLayout.CENTER);
    }

    private JPanel createSummaryCards() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 14, 0));
        panel.setOpaque(false);

        lblTotalUsage = new JLabel("0", SwingConstants.LEFT);
        lblTotalItems = new JLabel("0", SwingConstants.LEFT);
        lblWarningStock = new JLabel("0", SwingConstants.LEFT);
        lblTopUsed = new JLabel("-", SwingConstants.LEFT);

        panel.add(createMetricCard("LƯỢT GHI NHẬN HÔM NAY", lblTotalUsage, "Phiếu xuất phòng khám", "GN",
                new Color(231, 246, 245)));
        panel.add(createMetricCard("SỐ LƯỢNG VẬT TƯ TIÊU HAO", lblTotalItems, "Đơn vị vật tư sử dụng", "SL",
                new Color(237, 242, 255)));
        panel.add(createMetricCard("CẢNH BÁO TỒN KHO THẤP", lblWarningStock, "Cần đề xuất nhập kho", "CB",
                new Color(255, 246, 232)));
        panel.add(
                createMetricCard("DÙNG NHIỀU NHẤT", lblTopUsed, "Vật tư tiêu thụ cao", "VT", new Color(253, 237, 244)));
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

        // Hàng 1: Bộ lọc
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filterRow.setOpaque(false);

        JLabel lblDate = new JLabel("Ngày: " + new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
        lblDate.setFont(new Font("Arial", Font.BOLD, 12));
        lblDate.setForeground(INK);

        cbRoomFilter = new JComboBox<>(new String[] { "Tất cả phòng khám", "Phòng 01 (Tổng quát)",
                "Phòng 02 (Chỉnh nha)", "Phòng 03 (Tiểu phẫu)", "Phòng Vô trùng" });
        cbTypeFilter = new JComboBox<>(new String[] { "Tất cả phân loại", "Dụng cụ 1 lần", "Thuốc tê & kim",
                "Vật liệu trám", "Chỉnh nha", "Vật tư vô trùng" });

        cbRoomFilter.setPreferredSize(new Dimension(175, 32));
        cbTypeFilter.setPreferredSize(new Dimension(170, 32));

        filterRow.add(lblDate);
        filterRow.add(Box.createHorizontalStrut(8));
        filterRow.add(new JLabel("Phòng sử dụng:"));
        filterRow.add(cbRoomFilter);
        filterRow.add(new JLabel("Phân loại:"));
        filterRow.add(cbTypeFilter);

        // Hàng 2: Tìm kiếm & Thao tác
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actionRow.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm vật tư / ca:");
        lblSearch.setFont(new Font("Arial", Font.PLAIN, 12));

        txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(270, 32));
        txtSearch.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearch.putClientProperty("JTextField.placeholderText", "Mã vật tư, tên vật tư hoặc mã ca khám...");
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

        JButton btnAddUsage = new JButton("+ Ghi nhận xuất dùng");
        btnAddUsage.setFont(new Font("Arial", Font.BOLD, 12));
        btnAddUsage.setBackground(TEAL);
        btnAddUsage.setForeground(Color.BLACK);
        btnAddUsage.setFocusPainted(false);
        btnAddUsage.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAddUsage.setPreferredSize(new Dimension(175, 32));
        btnAddUsage.addActionListener(e -> openAddUsageDialog());

        actionRow.add(lblSearch);
        actionRow.add(txtSearch);
        actionRow.add(btnFilter);
        actionRow.add(btnReset);
        actionRow.add(Box.createHorizontalStrut(15));
        actionRow.add(btnAddUsage);

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
                "Mã Phiếu", "Mã Vật Tư", "Tên vật tư y tế", "Phân loại", "Số lượng", "Đơn vị tính", "Phòng sử dụng",
                "Người ghi nhận", "Thời gian ghi", "Trạng thái kho"
        };

        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        usageTable = new JTable(tableModel);
        usageTable.setFont(new Font("Arial", Font.PLAIN, 12));
        usageTable.setRowHeight(34);
        usageTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usageTable.setShowGrid(true);
        usageTable.setGridColor(new Color(240, 243, 245));

        JTableHeader tableHeader = usageTable.getTableHeader();
        tableHeader.setFont(new Font("Arial", Font.BOLD, 11));
        tableHeader.setBackground(new Color(245, 248, 250));
        tableHeader.setForeground(INK);
        tableHeader.setPreferredSize(new Dimension(tableHeader.getWidth(), 38));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        usageTable.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        usageTable.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        usageTable.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        usageTable.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        usageTable.getColumnModel().getColumn(8).setCellRenderer(centerRenderer);

        // Hiển thị nhãn cảnh báo trạng thái kho
        usageTable.getColumnModel().getColumn(9).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 11));

                String st = value == null ? "" : value.toString();
                if ("Còn dồi dào".equals(st)) {
                    label.setForeground(new Color(40, 140, 60));
                } else if ("Sắp hết".equals(st)) {
                    label.setForeground(new Color(210, 45, 45));
                } else {
                    label.setForeground(new Color(180, 115, 20));
                }
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(usageTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private Object[][] getRawUsageData() {
        return new Object[][] {
                { "PX-001", "VT001", "Găng tay y tế Nitrile (Hộp 100 cái)", "Dụng cụ 1 lần", 2, "Hộp",
                        "Phòng 01 (Tổng quát)", "YT. Trần Thị Mai", "08:15", "Còn dồi dào" },
                { "PX-002", "VT002", "Thuốc tê Septanest 1/100.000", "Thuốc tê & kim", 6, "Ống", "Phòng 03 (Tiểu phẫu)",
                        "YT. Đỗ Kim Ngân", "08:45", "Sắp hết" },
                { "PX-003", "VT003", "Composite đặc quang trùng hợp 3M Z250", "Vật liệu trám", 1, "Tuýp",
                        "Phòng 01 (Tổng quát)", "YT. Trần Thị Mai", "09:20", "Ổn định" },
                { "PX-004", "VT004", "Chun chuỗi chỉnh nha Ortho Classic", "Chỉnh nha", 3, "Cuộn",
                        "Phòng 02 (Chỉnh nha)", "YT. Nguyễn Thị Hương", "09:50", "Ổn định" },
                { "PX-005", "VT005", "Mũi khoan kim cương mở tủy Mani", "Dụng cụ 1 lần", 4, "Cái",
                        "Phòng 01 (Tổng quát)", "YT. Phạm Bích Trâm", "10:10", "Sắp hết" },
                { "PX-006", "VT006", "Túi ép tiệt trùng dụng cụ nha khoa", "Vật tư vô trùng", 25, "Túi",
                        "Phòng Vô trùng", "YT. Lê Thanh Tuyền", "10:30", "Còn dồi dào" }
        };
    }

    private void loadMockData() {
        tableModel.setRowCount(0);
        Object[][] data = getRawUsageData();
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
        recalculateMetrics();
    }

    private void filterData() {
        String keyword = txtSearch.getText().trim().toLowerCase();
        String selectedRoom = cbRoomFilter.getSelectedItem().toString();
        String selectedType = cbTypeFilter.getSelectedItem().toString();

        tableModel.setRowCount(0);
        Object[][] raw = getRawUsageData();

        for (Object[] row : raw) {
            String px = row[0].toString().toLowerCase();
            String maVt = row[1].toString().toLowerCase();
            String tenVt = row[2].toString().toLowerCase();
            String type = row[3].toString();
            String room = row[6].toString();

            boolean matchKeyword = keyword.isEmpty() || px.contains(keyword) || maVt.contains(keyword)
                    || tenVt.contains(keyword);
            boolean matchRoom = selectedRoom.startsWith("Tất cả") || room.equals(selectedRoom);
            boolean matchType = selectedType.startsWith("Tất cả") || type.equals(selectedType);

            if (matchKeyword && matchRoom && matchType) {
                tableModel.addRow(row);
            }
        }
        recalculateMetrics();
    }

    private void resetFilter() {
        txtSearch.setText("");
        cbRoomFilter.setSelectedIndex(0);
        cbTypeFilter.setSelectedIndex(0);
        loadMockData();
    }

    private void recalculateMetrics() {
        int totalUsage = tableModel.getRowCount();
        int totalItems = 0;
        int warningStock = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            totalItems += Integer.parseInt(tableModel.getValueAt(i, 4).toString());
            String stock = tableModel.getValueAt(i, 9).toString();
            if ("Sắp hết".equals(stock)) {
                warningStock++;
            }
        }

        lblTotalUsage.setText(String.format("%02d phiếu", totalUsage));
        lblTotalItems.setText(String.format("%02d đơn vị", totalItems));
        lblWarningStock.setText(String.format("%02d mặt hàng", warningStock));
        lblTopUsed.setText("Thuốc tê Septanest");
    }

    private void openAddUsageDialog() {
        JPanel dialogPanel = new JPanel(new GridLayout(4, 2, 8, 8));

        JComboBox<String> cbMaterial = new JComboBox<>(new String[] {
                "VT001 - Găng tay y tế Nitrile",
                "VT002 - Thuốc tê Septanest 1/100.000",
                "VT003 - Composite đặc quang trùng hợp 3M Z250",
                "VT004 - Chun chuỗi chỉnh nha Ortho Classic",
                "VT005 - Mũi khoan kim cương mở tủy Mani",
                "VT006 - Túi ép tiệt trùng dụng cụ"
        });

        JSpinner spQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        JComboBox<String> cbRoom = new JComboBox<>(new String[] {
                "Phòng 01 (Tổng quát)", "Phòng 02 (Chỉnh nha)", "Phòng 03 (Tiểu phẫu)", "Phòng Vô trùng"
        });
        JTextField txtNurse = new JTextField("YT. Trịnh Tuấn Kiệt");

        dialogPanel.add(new JLabel("Chọn vật tư:"));
        dialogPanel.add(cbMaterial);
        dialogPanel.add(new JLabel("Số lượng xuất dùng:"));
        dialogPanel.add(spQuantity);
        dialogPanel.add(new JLabel("Phòng sử dụng:"));
        dialogPanel.add(cbRoom);
        dialogPanel.add(new JLabel("Người ghi nhận:"));
        dialogPanel.add(txtNurse);

        int result = JOptionPane.showConfirmDialog(this, dialogPanel,
                "Ghi nhận vật tư tiêu hao phòng khám", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String selectedMat = cbMaterial.getSelectedItem().toString().split(" - ")[1];
            String selectedCode = cbMaterial.getSelectedItem().toString().split(" - ")[0];
            int qty = (Integer) spQuantity.getValue();
            String room = cbRoom.getSelectedItem().toString();
            String nurse = txtNurse.getText().trim();
            String time = new SimpleDateFormat("HH:mm").format(new Date());

            tableModel.insertRow(0, new Object[] {
                    "PX-00" + (tableModel.getRowCount() + 1), selectedCode, selectedMat, "Vật tư tiêu hao", qty,
                    "Đơn vị", room, nurse, time, "Ổn định"
            });
            recalculateMetrics();
            JOptionPane.showMessageDialog(this, "Đã ghi nhận tiêu hao thành công!", "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
/* end trinhKiet */