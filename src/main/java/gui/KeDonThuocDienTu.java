/* Vy */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * KeDonThuocDienTu là giao diện Kê đơn thuốc điện tử (UC013) trong phân hệ Cập
 * nhật.
 * Cho phép bác sĩ tra cứu thuốc, kiểm tra cảnh báo dị ứng, thêm thuốc vào đơn,
 * nhập lời dặn và phát hành/in đơn thuốc điện tử.
 */
public final class KeDonThuocDienTu extends JPanel {

    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color WARNING_BG = new Color(255, 243, 205);
    private static final Color WARNING_FG = new Color(133, 100, 4);

    private final JTextField txtSearchMedicine = new JTextField();
    private final JComboBox<String> cbGroup = new JComboBox<>(new String[] {
            "Tất cả nhóm", "Giảm đau - kháng viêm", "Kháng sinh", "Vitamin & Khoáng chất"
    });
    private final JTextField txtQuantity = new JTextField("10");
    private final DefaultTableModel tableModel;
    private final JTable prescriptionTable;
    private final JTextArea txtGeneralAdvice = new JTextArea();

    public KeDonThuocDienTu() {
        setLayout(new BorderLayout(0, 15));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        String[] columns = { "STT", "Tên thuốc - Hàm lượng", "Số lượng", "Đường dùng", "Liều dùng", "Lời dặn",
                "Thao tác" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6;
            }
        };
        prescriptionTable = new JTable(tableModel);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadSamplePrescription();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        // 1. Banner cảnh báo dị ứng[cite: 9, 13]
        JPanel warningBanner = new JPanel(new BorderLayout());
        warningBanner.setBackground(WARNING_BG);
        warningBanner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(238, 210, 140)),
                new EmptyBorder(10, 15, 10, 15)));
        warningBanner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));

        JLabel lblWarning = new JLabel(
                "CẢNH BÁO DỊ ỨNG: Bệnh nhân có tiền sử dị ứng Penicillin! Kiểm tra kỹ hoạt chất khi kê đơn.");
        lblWarning.setFont(new Font("Arial", Font.BOLD, 12));
        lblWarning.setForeground(WARNING_FG);
        warningBanner.add(lblWarning, BorderLayout.CENTER);

        panel.add(warningBanner);
        panel.add(Box.createVerticalStrut(12));

        // 2. Toolbar tìm kiếm và thêm thuốc[cite: 9, 13]
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setBackground(Color.WHITE);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 15, 12, 15)));

        JPanel searchGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchGroup.setOpaque(false);

        JLabel lblSearch = new JLabel("Tìm kiếm thuốc:");
        lblSearch.setFont(new Font("Arial", Font.BOLD, 12));
        lblSearch.setForeground(INK);

        txtSearchMedicine.setPreferredSize(new Dimension(280, 36));
        txtSearchMedicine.setFont(new Font("Arial", Font.PLAIN, 12));
        txtSearchMedicine.setToolTipText("Tìm thuốc theo tên hoặc hoạt chất...");

        JLabel lblGroup = new JLabel("Nhóm:");
        lblGroup.setFont(new Font("Arial", Font.BOLD, 12));
        lblGroup.setForeground(INK);

        cbGroup.setPreferredSize(new Dimension(160, 36));
        cbGroup.setFont(new Font("Arial", Font.PLAIN, 12));
        cbGroup.setBackground(Color.WHITE);

        JLabel lblQty = new JLabel("SL:");
        lblQty.setFont(new Font("Arial", Font.BOLD, 12));
        lblQty.setForeground(INK);

        txtQuantity.setPreferredSize(new Dimension(60, 36));
        txtQuantity.setFont(new Font("Arial", Font.PLAIN, 12));
        txtQuantity.setHorizontalAlignment(JTextField.CENTER);

        JButton btnAdd = new JButton("+ Thêm vào");
        btnAdd.setFont(new Font("Arial", Font.BOLD, 12));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setBackground(TEAL);
        btnAdd.setFocusPainted(false);
        btnAdd.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAdd.setPreferredSize(new Dimension(100, 36));
        btnAdd.addActionListener(e -> addMedicineToTable());

        searchGroup.add(lblSearch);
        searchGroup.add(txtSearchMedicine);
        searchGroup.add(lblGroup);
        searchGroup.add(cbGroup);
        searchGroup.add(lblQty);
        searchGroup.add(txtQuantity);
        searchGroup.add(btnAdd);

        toolbar.add(searchGroup, BorderLayout.CENTER);
        panel.add(toolbar);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel tableTitle = new JLabel("Danh sách thuốc trong đơn");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(INK);
        // Thêm khoảng trống bên dưới tiêu đề bằng EmptyBorder thay vì Box
        tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        prescriptionTable.setRowHeight(35);
        prescriptionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        prescriptionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = prescriptionTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(248, 250, 251));
        header.setForeground(MUTED);

        JScrollPane scrollPane = new JScrollPane(prescriptionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // SỬA LẠI: Đưa title lên NORTH, scrollPane vào CENTER để nó tự động co giãn
        panel.add(tableTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0)); // Khoảng cách 15px ngang
        panel.setOpaque(false);

        // Lời dặn chung của BS
        JPanel advicePanel = new JPanel(new BorderLayout());
        advicePanel.setBackground(Color.WHITE);
        advicePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 12, 12, 12)));
        // Bỏ setPreferredSize cứng để nó tự co giãn theo nội dung
        // advicePanel.setPreferredSize(new Dimension(500, 110));

        JLabel lblAdvice = new JLabel("Lời dặn chung của BS:");
        lblAdvice.setFont(new Font("Arial", Font.BOLD, 12));
        lblAdvice.setForeground(INK);
        lblAdvice.setBorder(new EmptyBorder(0, 0, 5, 0));

        txtGeneralAdvice.setText(
                "Uống thuốc đúng giờ, không uống rượu bia trong thời gian uống kháng sinh. Tái khám sau 5 ngày hoặc khi có dấu hiệu bất thường.");
        txtGeneralAdvice.setFont(new Font("Arial", Font.PLAIN, 12));
        txtGeneralAdvice.setLineWrap(true);
        txtGeneralAdvice.setWrapStyleWord(true);
        txtGeneralAdvice.setBorder(BorderFactory.createLineBorder(LINE));
        txtGeneralAdvice.setRows(3); // Đặt số dòng cố định cho đẹp

        advicePanel.add(lblAdvice, BorderLayout.NORTH);
        advicePanel.add(new JScrollPane(txtGeneralAdvice), BorderLayout.CENTER); // Đưa text vào CENTER

        // Nút tác vụ
        // SỬA LẠI: Bỏ vgap 30 trong FlowLayout, để nó tự căn giữa
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnPrint = new JButton("In đơn thuốc");
        styleSecondaryButton(btnPrint);
        btnPrint.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đang kết nối máy in để xuất đơn thuốc điện tử...", "In đơn",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnIssue = new JButton("Phát hành đơn");
        btnIssue.setFont(new Font("Arial", Font.BOLD, 12));
        btnIssue.setForeground(Color.WHITE);
        btnIssue.setBackground(TEAL);
        btnIssue.setBorder(new EmptyBorder(11, 20, 11, 20));
        btnIssue.setFocusPainted(false);
        btnIssue.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIssue.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Phát hành đơn thuốc thành công! Đã lưu vào CSDL hệ thống.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        });

        actionPanel.add(btnPrint);
        actionPanel.add(btnIssue);

        panel.add(advicePanel, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.EAST);

        return panel;
    }

    private void styleSecondaryButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setForeground(INK);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(11, 17, 11, 17)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private void loadSamplePrescription() {
        tableModel.addRow(new Object[] { 1, "Paracetamol 500mg", "10 viên", "Uống", "1 viên/lần x 2 lần/ngày",
                "Uống khi đau sau ăn", "Xóa" });
        tableModel.addRow(new Object[] { 2, "Rodogyl (Spiramycin + Metronidazol)", "14 viên", "Uống",
                "2 viên/lần x 2 lần/ngày", "Uống trong bữa cơm", "Xóa" });
        tableModel.addRow(new Object[] { 3, "Alphachymotrypsine 4.2mg", "10 viên", "Ngậm dưới lưỡi",
                "2 viên/lần x 2 lần/ngày", "Chống phù nề", "Xóa" });
    }

    private void addMedicineToTable() {
        String medName = txtSearchMedicine.getText().trim();
        if (medName.isEmpty()) {
            medName = "Amoxicillin 500mg (Mẫu)";
        }
        String qty = txtQuantity.getText().trim() + " viên";
        int rowNum = tableModel.getRowCount() + 1;
        tableModel.addRow(
                new Object[] { rowNum, medName, qty, "Uống", "1 viên/lần x 2 lần/ngày", "Theo dõi dị ứng", "Xóa" });
        txtSearchMedicine.setText("");
        JOptionPane.showMessageDialog(this, "Đã thêm thuốc vào đơn thành công!", "Thông báo",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
/* end Vy */