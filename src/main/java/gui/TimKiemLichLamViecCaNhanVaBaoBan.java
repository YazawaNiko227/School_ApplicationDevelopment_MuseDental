/*Vy */
package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * TimKiemLichLamViecCaNhanVaBaoBan hiển thị lịch làm việc cá nhân của bác
 * sĩ/nhân viên,
 * danh sách bệnh nhân theo ca và form đăng ký báo bận / xin nghỉ đột xuất.
 * Giao diện hoàn toàn bằng tiếng Việt có dấu và sử dụng bộ chọn ngày/tuần trực
 * quan.
 */
public final class TimKiemLichLamViecCaNhanVaBaoBan extends JPanel {

    // Bảng màu đồng bộ hệ thống Muse Dental
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color TEAL = new Color(26, 151, 151);

    private final JComboBox<String> cbDateSelector; // Thay thế việc nhập text thô bằng bộ chọn ngày/tuần trực quan
    private final JTable scheduleTable;
    private final DefaultTableModel scheduleModel;

    private final JTable patientTable;
    private final DefaultTableModel patientModel;

    private final JComboBox<String> cbShiftSelector = new JComboBox<>(new String[] {
            "Ca sáng (08:00 - 12:00)", "Ca chiều (13:30 - 17:30)", "Ca tối (18:00 - 20:30)"
    });
    private final JComboBox<String> cbSubstituteDoctor = new JComboBox<>(new String[] {
            "BS. Phạm Văn D", "BS. Nguyễn Văn Hùng", "BS. Trần Thị Mai", "BS. Lê Minh Sang"
    });
    private final JTextArea txtReason = new JTextFieldTextArea();

    public TimKiemLichLamViecCaNhanVaBaoBan() {
        setLayout(new BorderLayout(0, 18));
        setBackground(CANVAS);
        setBorder(new EmptyBorder(0, 0, 0, 0));

        // Khởi tạo bộ chọn ngày trực quan (thay vì JTextField nhập text thô)
        String currentDateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy ('Tuần này')"));
        cbDateSelector = new JComboBox<>(new String[] {
                currentDateStr,
                "Tuần tới (20/10/2026 - 25/10/2026)",
                "Tuần tiếp theo (27/10/2026 - 01/11/2026)"
        });

        // Khởi tạo bảng lịch làm việc tuần
        String[] scheduleColumns = { "Khung giờ", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7" };
        scheduleModel = new DefaultTableModel(scheduleColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        scheduleTable = new JTable(scheduleModel);

        // Khởi tạo bảng danh sách bệnh nhân theo ca
        String[] patientColumns = { "Mã hẹn", "Họ tên", "Giờ", "Trạng thái" };
        patientModel = new DefaultTableModel(patientColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        patientTable = new JTable(patientModel);

        add(createTopToolbar(), BorderLayout.NORTH);
        add(createCenterContent(), BorderLayout.CENTER);

        loadSampleData();
    }

    private JPanel createTopToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel titleBlock = new JPanel();
        titleBlock.setOpaque(false);
        titleBlock.setLayout(new BoxLayout(titleBlock, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Lịch làm việc cá nhân và báo bận");
        title.setFont(new Font("Arial", Font.BOLD, 21));
        title.setForeground(INK);
        JLabel subtitle = new JLabel(
                "Theo dõi lịch trực hàng tuần, quản lý bệnh nhân hẹn khám và gửi yêu cầu xin nghỉ đột xuất.");
        subtitle.setFont(new Font("Arial", Font.PLAIN, 12));
        subtitle.setForeground(MUTED);
        titleBlock.add(title);
        titleBlock.add(Box.createVerticalStrut(4));
        titleBlock.add(subtitle);

        // Thanh chọn ngày/tuần trực quan ở góc phải
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        filterPanel.setOpaque(false);
        JLabel lblDate = new JLabel("Chọn thời gian:");
        lblDate.setFont(new Font("Arial", Font.BOLD, 12));
        lblDate.setForeground(INK);
        cbDateSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        cbDateSelector.setPreferredSize(new Dimension(250, 36));
        cbDateSelector.setBackground(Color.WHITE);

        filterPanel.add(lblDate);
        filterPanel.add(cbDateSelector);

        toolbar.add(titleBlock, BorderLayout.WEST);
        toolbar.add(filterPanel, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel createCenterContent() {
        JPanel center = new JPanel(new BorderLayout(0, 15));
        center.setOpaque(false);

        // 1. Khu vực hiển thị bảng lịch làm việc tuần
        JPanel scheduleCard = new JPanel(new BorderLayout());
        scheduleCard.setBackground(Color.WHITE);
        scheduleCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel lblScheduleTitle = new JLabel("Lịch trực tuần");
        lblScheduleTitle.setFont(new Font("Arial", Font.BOLD, 15));
        lblScheduleTitle.setForeground(INK);

        scheduleTable.setRowHeight(35);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        scheduleTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = scheduleTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setBackground(new Color(248, 250, 251));
        header.setForeground(MUTED);

        JScrollPane scheduleScroll = new JScrollPane(scheduleTable);
        scheduleScroll.setPreferredSize(new Dimension(0, 200));
        scheduleScroll.setBorder(BorderFactory.createLineBorder(LINE));

        JPanel tableWrap = new JPanel(new BorderLayout());
        tableWrap.setOpaque(false);
        tableWrap.add(lblScheduleTitle, BorderLayout.NORTH);
        tableWrap.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        tableWrap.add(scheduleScroll, BorderLayout.SOUTH);

        center.add(tableWrap, BorderLayout.NORTH);

        // 2. Khu vực phía dưới chia làm 2 phần: Danh sách bệnh nhân (Trái) và Form Báo
        // bận (Phải)
        JPanel bottomSplit = new JPanel(new GridLayout(1, 2, 15, 0));
        bottomSplit.setOpaque(false);

        bottomSplit.add(createPatientListPanel());
        bottomSplit.add(createAbsenceRequestPanel());

        center.add(bottomSplit, BorderLayout.CENTER);
        return center;
    }

    private JPanel createPatientListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Danh sách bệnh nhân đã hẹn trong ca đang chọn");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(INK);

        patientTable.setRowHeight(32);
        patientTable.setFont(new Font("Arial", Font.PLAIN, 12));
        JTableHeader pHeader = patientTable.getTableHeader();
        pHeader.setFont(new Font("Arial", Font.BOLD, 10));
        pHeader.setBackground(new Color(248, 250, 251));

        JScrollPane scroll = new JScrollPane(patientTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(title, BorderLayout.NORTH);
        wrap.add(Box.createVerticalStrut(8), BorderLayout.CENTER);
        wrap.add(scroll, BorderLayout.SOUTH);

        return wrap;
    }

    private JPanel createAbsenceRequestPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(15, 15, 15, 15)));

        JLabel title = new JLabel("Báo bận / Xin nghỉ đột xuất");
        title.setFont(new Font("Arial", Font.BOLD, 13));
        title.setForeground(INK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));

        panel.add(createFormRow("Ca xin đề xuất thay thế:", cbShiftSelector));
        panel.add(Box.createVerticalStrut(8));

        JLabel lblReason = new JLabel("Lý do xin nghỉ:");
        lblReason.setFont(new Font("Arial", Font.PLAIN, 11));
        lblReason.setForeground(INK);
        lblReason.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(lblReason);
        panel.add(Box.createVerticalStrut(4));

        txtReason.setFont(new Font("Arial", Font.PLAIN, 12));
        txtReason.setBorder(BorderFactory.createLineBorder(LINE));
        JScrollPane scrollReason = new JScrollPane(txtReason);
        scrollReason.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        scrollReason.setAlignmentX(LEFT_ALIGNMENT);
        panel.add(scrollReason);
        panel.add(Box.createVerticalStrut(8));

        panel.add(createFormRow("Nha sĩ thay thế:", cbSubstituteDoctor));
        panel.add(Box.createVerticalStrut(15));

        JButton btnSubmit = new JButton("Gửi yêu cầu báo bận");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 12));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setBackground(TEAL);
        btnSubmit.setFocusPainted(false);
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSubmit.setAlignmentX(LEFT_ALIGNMENT);
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnSubmit.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "Đã gửi yêu cầu báo bận thành công cho quản lý phòng khám!",
                    "Thông báo",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        panel.add(btnSubmit);
        return panel;
    }

    private JPanel createFormRow(String labelText, JComboBox<String> comboBox) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setForeground(INK);

        comboBox.setFont(new Font("Arial", Font.PLAIN, 11));
        comboBox.setBackground(Color.WHITE);

        row.add(lbl, BorderLayout.WEST);
        row.add(comboBox, BorderLayout.CENTER);
        return row;
    }

    private static class JTextFieldTextArea extends JTextArea {
        public JTextFieldTextArea() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(new EmptyBorder(4, 4, 4, 4));
        }
    }

    private void loadSampleData() {
        // Dữ liệu mẫu lịch trực
        scheduleModel.addRow(new Object[] { "08:00 - 09:00", "Nguyễn Văn A", "", "Trần Thị B", "", "Lê Văn C", "" });
        scheduleModel.addRow(new Object[] { "09:00 - 10:00", "", "Phạm Thị D", "", "Hoàng Văn E", "", "Vũ Văn H" });
        scheduleModel.addRow(new Object[] { "10:00 - 11:00", "Đỗ Văn F", "", "", "Ngô Thị G", "", "" });
        scheduleModel.addRow(new Object[] { "14:00 - 15:00", "", "", "Bùi Thị I", "", "", "" });
        scheduleModel.addRow(new Object[] { "15:00 - 16:00", "Nguyễn Văn A", "", "", "", "Lê Văn C", "" });

        // Dữ liệu mẫu bệnh nhân trong ca
        patientModel.addRow(new Object[] { "LH001", "Nguyễn Văn A", "08:00", "Đã xác nhận" });
        patientModel.addRow(new Object[] { "LH002", "Trần Hoàng Nam", "08:30", "Chờ xác nhận" });
        patientModel.addRow(new Object[] { "LH003", "Lê Ngọc Hà", "09:00", "Dời lịch" });
    }
}
/* end Vy */