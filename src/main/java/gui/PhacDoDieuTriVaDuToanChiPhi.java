/* Vy */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

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
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * PhacDoDieuTriVaDuToanChiPhi là giao diện lập phác đồ điều trị và dự toán chi
 * phí (UC liên quan).
 * Cho phép bác sĩ thiết lập phác đồ đa đợt, chỉ định dịch vụ, răng, cận lâm
 * sàng,
 * lên lịch tái khám và dự toán tổng kinh phí trước khi lưu vào hệ thống.
 */
public final class PhacDoDieuTriVaDuToanChiPhi extends JPanel {

    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color TEAL = new Color(26, 151, 151);

    private final JTextField txtPlanName = new JTextField("Điều trị tủy và Bọc sứ thẩm mỹ răng 46");
    private final JComboBox<String> cbPriority = new JComboBox<>(new String[] {
            "Cao (Ưu tiên)", "Trung bình", "Thấp"
    });

    private final JComboBox<String> cbTimeSlot = new JComboBox<>(new String[] {
            "09:00 - 10:00", "10:00 - 11:00", "14:00 - 15:00", "15:00 - 16:00"
    });
    private final JTextField txtAppointmentDate = new JTextField("19/10/2026");
    private final JTextField txtAppointmentContent = new JTextField("Trám bít ống tủy và lấy dấu răng sứ");

    private final JComboBox<String> cbPaymentMethod = new JComboBox<>(new String[] {
            "Theo từng đợt khám", "Thanh toán toàn bộ", "Trả góp theo đợt"
    });
    private final JLabel lblTotalCost = new JLabel("5.800.000 VND");

    private final DefaultTableModel tableModel;
    private final JTable planTable;

    public PhacDoDieuTriVaDuToanChiPhi() {
        setLayout(new BorderLayout(0, 15));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        String[] columns = { "Đợt", "Dịch vụ chỉ định", "Răng", "Cận lâm sàng", "Đơn giá (VND)", "Thành tiền (VND)",
                "Trạng thái" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        planTable = new JTable(tableModel);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        loadSampleData();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JPanel group1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        group1.setOpaque(false);

        JLabel lblName = new JLabel("Tên phác đồ:");
        lblName.setFont(new Font("Arial", Font.BOLD, 12));
        lblName.setForeground(INK);

        txtPlanName.setPreferredSize(new Dimension(320, 36));
        txtPlanName.setFont(new Font("Arial", Font.PLAIN, 12));

        JLabel lblPriority = new JLabel("Mức độ ưu tiên:");
        lblPriority.setFont(new Font("Arial", Font.BOLD, 12));
        lblPriority.setForeground(INK);

        cbPriority.setPreferredSize(new Dimension(160, 36));
        cbPriority.setFont(new Font("Arial", Font.PLAIN, 12));
        cbPriority.setBackground(Color.WHITE);

        group1.add(lblName);
        group1.add(txtPlanName);
        group1.add(lblPriority);
        group1.add(cbPriority);

        panel.add(group1, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel tableTitle = new JLabel("Chi tiết các đợt điều trị trong phác đồ");
        tableTitle.setFont(new Font("Arial", Font.BOLD, 14));
        tableTitle.setForeground(INK);
        // SỬA LẠI: Dùng EmptyBorder để tạo khoảng trống bên dưới tiêu đề thay vì Box
        tableTitle.setBorder(new EmptyBorder(0, 0, 10, 0));

        planTable.setRowHeight(35);
        planTable.setFont(new Font("Arial", Font.PLAIN, 12));
        planTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader header = planTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(248, 250, 251));
        header.setForeground(MUTED);

        JScrollPane scrollPane = new JScrollPane(planTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        // SỬA LẠI: Đưa title lên NORTH, scrollPane vào CENTER để nó tự động chiếm hết
        // phần còn lại
        panel.add(tableTitle, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(15, 0));
        panel.setOpaque(false);

        // 1. Khung Hẹn lịch tái khám (Bên trái)
        JPanel appointmentPanel = new JPanel();
        appointmentPanel.setLayout(new BoxLayout(appointmentPanel, BoxLayout.Y_AXIS));
        appointmentPanel.setBackground(Color.WHITE);
        appointmentPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblAppTitle = new JLabel("Hẹn lịch tái khám");
        lblAppTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblAppTitle.setForeground(INK);
        lblAppTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        appointmentPanel.add(lblAppTitle);
        appointmentPanel.add(Box.createVerticalStrut(10));
        appointmentPanel.add(createFormRow("Ngày tái khám:", txtAppointmentDate));
        appointmentPanel.add(Box.createVerticalStrut(8));
        appointmentPanel.add(createFormRow("Khung giờ:", cbTimeSlot));
        appointmentPanel.add(Box.createVerticalStrut(8));
        appointmentPanel.add(createFormRow("Nội dung:", txtAppointmentContent));

        // 2. Khung Dự toán chi phí & Nút xác nhận (Bên phải)
        JPanel costPanel = new JPanel();
        costPanel.setLayout(new BoxLayout(costPanel, BoxLayout.Y_AXIS));
        costPanel.setBackground(Color.WHITE);
        costPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblCostTitle = new JLabel("Dự toán chi phí");
        lblCostTitle.setFont(new Font("Arial", Font.BOLD, 13));
        lblCostTitle.setForeground(INK);
        lblCostTitle.setAlignmentX(JLabel.LEFT_ALIGNMENT);

        JPanel totalCostRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        totalCostRow.setOpaque(false);
        totalCostRow.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        JLabel lblTotalLabel = new JLabel("Tổng kinh phí dự kiến: ");
        lblTotalLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTotalLabel.setForeground(INK);
        lblTotalCost.setFont(new Font("Arial", Font.BOLD, 15));
        lblTotalCost.setForeground(new Color(217, 83, 79));
        totalCostRow.add(lblTotalLabel);
        totalCostRow.add(lblTotalCost);

        costPanel.add(lblCostTitle);
        costPanel.add(Box.createVerticalStrut(12));
        costPanel.add(totalCostRow);
        costPanel.add(Box.createVerticalStrut(12));
        costPanel.add(createFormRow("Hình thức thanh toán:", cbPaymentMethod));
        costPanel.add(Box.createVerticalStrut(15));

        JButton btnSubmit = new JButton("Xác nhận lưu phác đồ");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 12));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(TEAL);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSubmit.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSubmit.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "Đã lưu phác đồ điều trị và dự toán chi phí thành công!", "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        costPanel.add(btnSubmit);

        JPanel splitPane = new JPanel(new GridLayout(1, 2, 15, 0));
        splitPane.setOpaque(false);
        splitPane.add(appointmentPanel);
        splitPane.add(costPanel);

        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createFormRow(String labelText, javax.swing.JComponent comp) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(JLabel.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 12));
        lbl.setForeground(INK);
        lbl.setPreferredSize(new Dimension(130, 25));

        comp.setFont(new Font("Arial", Font.PLAIN, 12));
        if (comp instanceof JTextField) {
            ((JTextField) comp).setPreferredSize(new Dimension(200, 30));
        } else if (comp instanceof JComboBox) {
            ((JComboBox<?>) comp).setPreferredSize(new Dimension(200, 30));
            ((JComboBox<?>) comp).setBackground(Color.WHITE);
        }

        row.add(lbl, BorderLayout.WEST);
        row.add(comp, BorderLayout.CENTER);
        return row;
    }

    private void loadSampleData() {
        tableModel.addRow(new Object[] { "Đợt 1", "Chụp X-quang quanh chóp", "R46", "X-Quang", "100.000", "100.000",
                "Đã hoàn tất" });
        tableModel.addRow(new Object[] { "Đợt 2", "Chữa tủy răng vĩnh viễn", "R46", "Không", "1.200.000", "1.200.000",
                "Đang thực hiện" });
        tableModel.addRow(new Object[] { "Đợt 3", "Bọc răng sứ Cercon HT", "R46", "Lấy dấu mẫu", "4.500.000",
                "4.500.000", "Chưa thực hiện" });
    }
}
/* end Vy */