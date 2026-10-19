package gui; /*Võ Thành Tài */

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.util.Date;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;
import entity.EMRRecord;

public class EMRSearchPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color SELECT_BG = new Color(231, 246, 245);

    private static final String ALL_DENTISTS = "Tất cả nha sĩ";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String[] COLUMNS = {
            "Mã chẩn đoán", "Mã hồ sơ", "Bệnh nhân", "Ngày khám", "Nha sĩ", "Lý do khám"
    };

    private final List<EMRRecord> allRecords = createSampleData();
    private final List<EMRRecord> shownRecords = new ArrayList<>();

    private final JTextField keywordField = new JTextField();
    private final JComboBox<String> dentistCombo = new JComboBox<>();
    private final JDateChooser fromDateField = new JDateChooser();
    private final JDateChooser toDateField = new JDateChooser();

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel resultCount = new JLabel();

    private final JLabel detailTitle = new JLabel("Chọn một lần khám để xem chi tiết");
    private final JTextArea reasonArea = createReadOnlyArea();
    private final JTextArea symptomArea = createReadOnlyArea();
    private final JTextArea conclusionArea = createReadOnlyArea();

    public EMRSearchPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        loadDentists();
        add(createFilterPanel(), BorderLayout.NORTH);
        add(createResultPanel(), BorderLayout.CENTER);
        add(createDetailPanel(), BorderLayout.SOUTH);

        applyFilter();
    }

    // ===================== BỘ LỌC =====================

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 14, 18)));

        JLabel title = new JLabel("Bộ lọc tra cứu bệnh án");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        styleField(keywordField, 260);
        keywordField.setToolTipText("Mã hồ sơ, mã chẩn đoán, mã/tên bệnh nhân, lý do khám, kết luận...");
        styleCombo(dentistCombo, 190);

        fromDateField.setPreferredSize(new Dimension(190, 36));
        toDateField.setPreferredSize(new Dimension(190, 36));

        fromDateField.setToolTipText("Định dạng dd/MM/yyyy");
        toDateField.setToolTipText("Định dạng dd/MM/yyyy");

        row.add(createLabeledField("Từ khóa", keywordField));
        row.add(createLabeledField("Nha sĩ khám", dentistCombo));
        row.add(createLabeledField("Từ ngày (dd/MM/yyyy)", fromDateField));
        row.add(createLabeledField("Đến ngày (dd/MM/yyyy)", toDateField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        JButton searchButton = createButton("Tìm kiếm", true);
        JButton resetButton = createButton("Đặt lại", false);
        searchButton.addActionListener(event -> applyFilter());
        resetButton.addActionListener(event -> resetFilter());
        keywordField.addActionListener(event -> applyFilter());
        fromDateField.getDateEditor().addPropertyChangeListener("date", event -> applyFilter());
        toDateField.getDateEditor().addPropertyChangeListener("date", event -> applyFilter());
        buttons.add(searchButton);
        buttons.add(resetButton);

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(row);
        panel.add(Box.createVerticalStrut(12));
        panel.add(buttons);
        return panel;
    }

    private JPanel createLabeledField(String labelText, java.awt.Component field) {
        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);
        label.setAlignmentX(LEFT_ALIGNMENT);
        ((javax.swing.JComponent) field).setAlignmentX(LEFT_ALIGNMENT);
        wrapper.add(label);
        wrapper.add(Box.createVerticalStrut(5));
        wrapper.add(field);
        return wrapper;
    }

    private void styleField(JTextField field, int width) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setPreferredSize(new Dimension(width, 36));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 10, 0, 10)));
    }

    private void styleCombo(JComboBox<String> combo, int width) {
        combo.setFont(new Font("Arial", Font.PLAIN, 12));
        combo.setForeground(INK);
        combo.setBackground(Color.WHITE);
        combo.setPreferredSize(new Dimension(width, 36));
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 36));
        button.setOpaque(true);
        button.setBackground(primary ? SELECT_BG : Color.WHITE);
        button.setForeground(primary ? TEAL : INK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? SELECT_BG : LINE),
                new EmptyBorder(0, 10, 0, 10)));
        return button;
    }

    // ===================== BẢNG KẾT QUẢ =====================

    private JPanel createResultPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 14, 16)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Kết quả tra cứu");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        resultCount.setFont(new Font("Arial", Font.PLAIN, 11));
        resultCount.setForeground(MUTED);
        heading.add(title, BorderLayout.WEST);
        heading.add(resultCount, BorderLayout.EAST);

        configureTable();
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void configureTable() {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setRowHeight(34);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionBackground(SELECT_BG);
        table.setSelectionForeground(TEAL);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setFillsViewportHeight(true);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(CANVAS);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(0, 36));

        DefaultTableCellRenderer padded = new DefaultTableCellRenderer();
        padded.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.setDefaultRenderer(Object.class, padded);

        int[] widths = { 110, 100, 170, 100, 160, 320 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                showDetail(table.getSelectedRow());
            }
        });
    }

    // ===================== CHI TIẾT LẦN KHÁM =====================

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 14, 16)));
        panel.setPreferredSize(new Dimension(0, 230));

        detailTitle.setFont(new Font("Arial", Font.BOLD, 14));
        detailTitle.setForeground(INK);
        panel.add(detailTitle, BorderLayout.NORTH);

        JPanel columns = new JPanel();
        columns.setOpaque(false);
        columns.setLayout(new BoxLayout(columns, BoxLayout.X_AXIS));
        columns.add(createDetailBlock("LÝ DO KHÁM", reasonArea));
        columns.add(Box.createHorizontalStrut(12));
        columns.add(createDetailBlock("TRIỆU CHỨNG", symptomArea));
        columns.add(Box.createHorizontalStrut(12));
        columns.add(createDetailBlock("KẾT LUẬN CHẨN ĐOÁN", conclusionArea));
        panel.add(columns, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createDetailBlock(String title, JTextArea area) {
        JPanel block = new JPanel(new BorderLayout(0, 6));
        block.setOpaque(false);
        block.setPreferredSize(new Dimension(300, 140));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        block.add(label, BorderLayout.NORTH);
        block.add(scroll, BorderLayout.CENTER);
        return block;
    }

    private static JTextArea createReadOnlyArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(new Font("Arial", Font.PLAIN, 12));
        area.setForeground(INK);
        area.setBackground(CANVAS);
        area.setBorder(new EmptyBorder(8, 10, 8, 10));
        return area;
    }

    private void showDetail(int viewRow) {
        if (viewRow < 0 || viewRow >= shownRecords.size()) {
            detailTitle.setText("Chọn một lần khám để xem chi tiết");
            reasonArea.setText("");
            symptomArea.setText("");
            conclusionArea.setText("");
            return;
        }
        EMRRecord record = shownRecords.get(viewRow);
        detailTitle.setText("Chi tiết lần khám " + record.getMaChanDoan() + " · " + record.getTenBenhNhan());
        reasonArea.setText(nullToEmpty(record.getLyDoKham()));
        symptomArea.setText(nullToEmpty(record.getTrieuChung()));
        conclusionArea.setText(nullToEmpty(record.getKetLuanChanDoan()));
        reasonArea.setCaretPosition(0);
        symptomArea.setCaretPosition(0);
        conclusionArea.setCaretPosition(0);
    }

    // ===================== LỌC DỮ LIỆU =====================

    private void loadDentists() {
        dentistCombo.addItem(ALL_DENTISTS);
        List<String> seen = new ArrayList<>();
        for (EMRRecord record : allRecords) {
            if (!seen.contains(record.getTenNhaSi())) {
                seen.add(record.getTenNhaSi());
                dentistCombo.addItem(record.getTenNhaSi());
            }
        }
    }

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void applyFilter() {
        LocalDate from = toLocalDate(fromDateField.getDate());
        LocalDate to = toLocalDate(toDateField.getDate());
        if (from != null && to != null && from.isAfter(to)) {
            JOptionPane.showMessageDialog(this,
                    "Ngày bắt đầu không được sau ngày kết thúc.",
                    "Khoảng ngày không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String keyword = keywordField.getText().trim().toLowerCase();
        String dentist = (String) dentistCombo.getSelectedItem();

        shownRecords.clear();
        tableModel.setRowCount(0);
        for (EMRRecord record : allRecords) {
            if (!matchesKeyword(record, keyword)) {
                continue;
            }
            if (dentist != null && !ALL_DENTISTS.equals(dentist) && !dentist.equals(record.getTenNhaSi())) {
                continue;
            }
            if (from != null && record.getNgayKham().isBefore(from)) {
                continue;
            }
            if (to != null && record.getNgayKham().isAfter(to)) {
                continue;
            }
            shownRecords.add(record);
            tableModel.addRow(new Object[] {
                    record.getMaChanDoan(),
                    record.getMaHoSo(),
                    record.getTenBenhNhan(),
                    record.getNgayKham().format(DATE_FORMAT),
                    record.getTenNhaSi(),
                    record.getLyDoKham()
            });
        }
        resultCount.setText("Tìm thấy " + shownRecords.size() + " lần khám");
        showDetail(-1);
    }

    private boolean matchesKeyword(EMRRecord record, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String haystack = (nullToEmpty(record.getMaChanDoan()) + " "
                + nullToEmpty(record.getMaHoSo()) + " "
                + nullToEmpty(record.getMaBenhNhan()) + " "
                + nullToEmpty(record.getTenBenhNhan()) + " "
                + nullToEmpty(record.getLyDoKham()) + " "
                + nullToEmpty(record.getKetLuanChanDoan())).toLowerCase();
        return haystack.contains(keyword);
    }

    private void resetFilter() {
        keywordField.setText("");
        dentistCombo.setSelectedIndex(0);
        fromDateField.setDate(null);
        toDateField.setDate(null);
        applyFilter();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    // ===================== DỮ LIỆU MINH HỌA =====================

    private static List<EMRRecord> createSampleData() {
        List<EMRRecord> list = new ArrayList<>();
        list.add(new EMRRecord("HS001", "CD001", "BN001", "Nguyễn Minh Anh", LocalDate.of(2026, 9, 20),
                "NV002", "BS. Tuấn", "Đau răng hàm dưới bên phải",
                "Ê buốt khi ăn nóng lạnh, đau âm ỉ về đêm",
                "Sâu răng 46 độ 3, chỉ định điều trị tủy"));
        list.add(new EMRRecord("HS002", "CD002", "BN002", "Trần Hoàng Nam", LocalDate.of(2026, 9, 25),
                "NV003", "BS. Linh", "Lấy cao răng định kỳ",
                "Chảy máu chân răng khi chải",
                "Viêm nướu nhẹ do cao răng, hướng dẫn vệ sinh răng miệng"));
        list.add(new EMRRecord("HS003", "CD003", "BN003", "Lê Ngọc Hà", LocalDate.of(2026, 10, 1),
                "NV002", "BS. Tuấn", "Tái khám sau điều trị tủy",
                "Không đau, răng ăn nhai bình thường",
                "Răng 46 ổn định, hẹn làm mão sứ"));
        list.add(new EMRRecord("HS004", "CD004", "BN004", "Phạm Gia Bảo", LocalDate.of(2026, 10, 5),
                "NV004", "BS. Minh", "Tư vấn niềng răng",
                "Răng mọc chen chúc hàm trên, khớp cắn hở nhẹ",
                "Lệch lạc khớp cắn loại I, đề xuất niềng mắc cài kim loại"));
        list.add(new EMRRecord("HS005", "CD005", "BN001", "Nguyễn Minh Anh", LocalDate.of(2026, 10, 9),
                "NV002", "BS. Tuấn", "Tái khám",
                "Răng 46 còn hơi ê khi nhai mạnh",
                "Theo dõi thêm, hẹn kiểm tra lại sau 2 tuần"));
        list.add(new EMRRecord("HS006", "CD006", "BN005", "Võ Thanh Tâm", LocalDate.of(2026, 10, 12),
                "NV003", "BS. Linh", "Nhổ răng khôn",
                "Sưng đau vùng răng 38, há miệng hạn chế",
                "Răng khôn 38 mọc lệch, viêm quanh thân răng, chỉ định nhổ"));
        return list;
    }
}