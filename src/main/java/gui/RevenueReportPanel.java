package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.toedter.calendar.JDateChooser;
import entity.RevenueRecord;

/**
 * Màn hình Báo cáo doanh thu.
 *
 * Bố cục (không dùng GridBagLayout / GridLayout):
 * - NORTH : bộ lọc + 4 thẻ thống kê (BoxLayout, FlowLayout)
 * - CENTER: biểu đồ cột doanh thu theo ngày/tháng + bảng chi tiết hóa đơn
 *
 * Doanh thu = tiền thực thu (đã thu), công nợ = tổng tiền - đã thu.
 */
public class RevenueReportPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color SELECT_BG = new Color(231, 246, 245);
    private static final Color DEBT = new Color(177, 125, 47);

    private static final String ALL_METHODS = "Tất cả phương thức";
    private static final String ALL_STATUS = "Tất cả trạng thái";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale VN = Locale.forLanguageTag("vi-VN");
    private static final String[] COLUMNS = {
            "Mã HĐ", "Ngày lập", "Bệnh nhân", "Thu ngân", "Phương thức",
            "Trạng thái", "Tổng tiền", "Đã thu", "Còn nợ"
    };

    // Dữ liệu: thay createSampleData() bằng DAO khi nối CSDL.
    private final List<RevenueRecord> allRecords = createSampleData();
    private final List<RevenueRecord> shownRecords = new ArrayList<>();

    private final JTextField keywordField = new JTextField();
    private final JComboBox<String> methodCombo = new JComboBox<>(
            new String[] { ALL_METHODS, "Tiền mặt", "Chuyển khoản", "Trả góp" });
    private final JComboBox<String> statusCombo = new JComboBox<>(
            new String[] { ALL_STATUS, "Đã thanh toán", "Đang trả góp", "Chưa thanh toán" });
    private final JDateChooser fromDateField = new JDateChooser();
    private final JDateChooser toDateField = new JDateChooser();

    private final JLabel revenueValue = new JLabel();
    private final JLabel revenueNote = new JLabel();
    private final JLabel invoiceValue = new JLabel();
    private final JLabel invoiceNote = new JLabel();
    private final JLabel debtValue = new JLabel();
    private final JLabel debtNote = new JLabel();
    private final JLabel averageValue = new JLabel();
    private final JLabel averageNote = new JLabel();

    private final RevenueChart chart = new RevenueChart();
    private final JLabel chartSubtitle = new JLabel();

    private final DefaultTableModel tableModel = new DefaultTableModel(COLUMNS, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel resultCount = new JLabel();
    private final JLabel footerTotal = new JLabel();

    public RevenueReportPanel() {
        super(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        JPanel filter = createFilterPanel();
        filter.setAlignmentX(LEFT_ALIGNMENT);
        JPanel cards = createSummaryCards();
        cards.setAlignmentX(LEFT_ALIGNMENT);
        north.add(filter);
        north.add(Box.createVerticalStrut(16));
        north.add(cards);
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 16));
        center.setOpaque(false);
        center.add(createChartPanel(), BorderLayout.NORTH);
        center.add(createTablePanel(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

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

        JLabel title = new JLabel("Bộ lọc báo cáo doanh thu");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        title.setAlignmentX(LEFT_ALIGNMENT);

        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);

        styleField(keywordField, 200);
        keywordField.setToolTipText("Mã hóa đơn, mã/tên bệnh nhân, thu ngân...");
        styleCombo(methodCombo, 170);
        styleCombo(statusCombo, 170);
        fromDateField.setPreferredSize(new Dimension(160, 36));
        toDateField.setPreferredSize(new Dimension(160, 36));
        fromDateField.setToolTipText("Định dạng dd/MM/yyyy");
        toDateField.setToolTipText("Định dạng dd/MM/yyyy");

        row.add(createLabeledField("Từ khóa", keywordField));
        row.add(createLabeledField("Phương thức thanh toán", methodCombo));
        row.add(createLabeledField("Trạng thái", statusCombo));
        row.add(createLabeledField("Từ ngày", fromDateField));
        row.add(createLabeledField("Đến ngày", toDateField));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttons.setOpaque(false);
        buttons.setAlignmentX(LEFT_ALIGNMENT);
        JButton searchButton = createButton("Xem báo cáo", true);
        JButton resetButton = createButton("Đặt lại", false);
        searchButton.addActionListener(event -> applyFilter());
        resetButton.addActionListener(event -> resetFilter());
        keywordField.addActionListener(event -> applyFilter());
        methodCombo.addActionListener(event -> applyFilter());
        statusCombo.addActionListener(event -> applyFilter());
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
        ((JComponent) field).setAlignmentX(LEFT_ALIGNMENT);
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
        button.setPreferredSize(new Dimension(120, 36));
        button.setOpaque(true);
        button.setBackground(primary ? SELECT_BG : Color.WHITE);
        button.setForeground(primary ? TEAL : INK);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? SELECT_BG : LINE),
                new EmptyBorder(0, 10, 0, 10)));
        return button;
    }

    // ===================== THẺ THỐNG KÊ =====================

    private JPanel createSummaryCards() {
        JPanel cards = new JPanel();
        cards.setOpaque(false);
        cards.setLayout(new BoxLayout(cards, BoxLayout.X_AXIS));
        cards.add(createSummaryCard("DOANH THU THỰC THU", revenueValue, revenueNote, TEAL));
        cards.add(Box.createHorizontalStrut(14));
        cards.add(createSummaryCard("SỐ HÓA ĐƠN", invoiceValue, invoiceNote, INK));
        cards.add(Box.createHorizontalStrut(14));
        cards.add(createSummaryCard("CÔNG NỢ CÒN LẠI", debtValue, debtNote, DEBT));
        cards.add(Box.createHorizontalStrut(14));
        cards.add(createSummaryCard("TRUNG BÌNH / HÓA ĐƠN", averageValue, averageNote, INK));
        return cards;
    }

    private JPanel createSummaryCard(String title, JLabel value, JLabel note, Color valueColor) {
        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 14, 18)));
        card.setPreferredSize(new Dimension(250, 96));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 96));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 22));
        value.setForeground(valueColor);
        note.setFont(new Font("Arial", Font.PLAIN, 10));
        note.setForeground(MUTED);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(value);
        bottom.add(Box.createVerticalStrut(4));
        bottom.add(note);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    // ===================== BIỂU ĐỒ =====================

    private JPanel createChartPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 12, 16)));
        panel.setPreferredSize(new Dimension(0, 250));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Biểu đồ doanh thu thực thu");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        title.setForeground(INK);
        chartSubtitle.setFont(new Font("Arial", Font.PLAIN, 11));
        chartSubtitle.setForeground(MUTED);
        heading.add(title, BorderLayout.WEST);
        heading.add(chartSubtitle, BorderLayout.EAST);

        panel.add(heading, BorderLayout.NORTH);
        JScrollPane chartScroll = new JScrollPane(chart);
        chartScroll.setBorder(null);
        chartScroll.setOpaque(false);
        chartScroll.getViewport().setOpaque(false);
        panel.add(chartScroll, BorderLayout.CENTER);
        return panel;
    }

    // ===================== BẢNG CHI TIẾT =====================

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 12, 16)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Chi tiết hóa đơn");
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

        footerTotal.setFont(new Font("Arial", Font.BOLD, 12));
        footerTotal.setForeground(INK);
        footerTotal.setHorizontalAlignment(SwingConstants.RIGHT);

        panel.add(heading, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(footerTotal, BorderLayout.SOUTH);
        return panel;
    }

    private void configureTable() {
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setRowHeight(32);
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

        DefaultTableCellRenderer left = new DefaultTableCellRenderer();
        left.setBorder(new EmptyBorder(0, 10, 0, 10));
        table.setDefaultRenderer(Object.class, left);

        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        right.setBorder(new EmptyBorder(0, 10, 0, 10));
        for (int col = 6; col <= 8; col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(right);
        }

        int[] widths = { 70, 90, 150, 130, 110, 120, 110, 110, 110 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    // ===================== LỌC & TÍNH TOÁN =====================

    private LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
        String method = (String) methodCombo.getSelectedItem();
        String status = (String) statusCombo.getSelectedItem();

        shownRecords.clear();
        tableModel.setRowCount(0);
        double revenue = 0;
        double debt = 0;
        double total = 0;

        for (RevenueRecord record : allRecords) {
            if (!matchesKeyword(record, keyword)) {
                continue;
            }
            if (method != null && !ALL_METHODS.equals(method)
                    && !method.equals(record.getPhuongThucThanhToan())) {
                continue;
            }
            if (status != null && !ALL_STATUS.equals(status)
                    && !status.equals(record.getTrangThaiThanhToan())) {
                continue;
            }
            if (from != null && record.getNgayLap().isBefore(from)) {
                continue;
            }
            if (to != null && record.getNgayLap().isAfter(to)) {
                continue;
            }
            shownRecords.add(record);
            revenue += record.getDaThu();
            debt += record.getConLai();
            total += record.getTongTien();
            tableModel.addRow(new Object[] {
                    record.getMaHoaDon(),
                    record.getNgayLap().format(DATE_FORMAT),
                    record.getTenBenhNhan(),
                    record.getTenThuNgan(),
                    record.getPhuongThucThanhToan() == null ? "—" : record.getPhuongThucThanhToan(),
                    record.getTrangThaiThanhToan(),
                    formatMoney(record.getTongTien()),
                    formatMoney(record.getDaThu()),
                    formatMoney(record.getConLai())
            });
        }

        int count = shownRecords.size();
        revenueValue.setText(formatMoney(revenue));
        revenueNote.setText("Tổng tiền đã thu trong kỳ");
        invoiceValue.setText(String.valueOf(count));
        invoiceNote.setText("Tổng giá trị " + formatMoney(total));
        debtValue.setText(formatMoney(debt));
        debtNote.setText("Trả góp và chưa thanh toán");
        averageValue.setText(formatMoney(count == 0 ? 0 : revenue / count));
        averageNote.setText("Doanh thu thực thu / số hóa đơn");

        resultCount.setText("Tìm thấy " + count + " hóa đơn");
        footerTotal.setText("Tổng cộng:  " + formatMoney(total) + "  |  Đã thu: "
                + formatMoney(revenue) + "  |  Còn nợ: " + formatMoney(debt));

        updateChart(from, to);
    }

    private boolean matchesKeyword(RevenueRecord record, String keyword) {
        if (keyword.isEmpty()) {
            return true;
        }
        String haystack = (nullToEmpty(record.getMaHoaDon()) + " "
                + nullToEmpty(record.getMaBenhNhan()) + " "
                + nullToEmpty(record.getTenBenhNhan()) + " "
                + nullToEmpty(record.getMaNhanVien()) + " "
                + nullToEmpty(record.getTenThuNgan())).toLowerCase();
        return haystack.contains(keyword);
    }

    /** Gom doanh thu theo ngày; nếu quá 31 ngày khác nhau thì gom theo tháng. */
    private void updateChart(LocalDate from, LocalDate to) {
        TreeMap<LocalDate, Double> byDay = new TreeMap<>();
        for (RevenueRecord record : shownRecords) {
            byDay.merge(record.getNgayLap(), record.getDaThu(), Double::sum);
        }

        boolean byMonth = byDay.size() > 31;
        TreeMap<LocalDate, Double> grouped = new TreeMap<>();
        if (byMonth) {
            for (Map.Entry<LocalDate, Double> entry : byDay.entrySet()) {
                grouped.merge(entry.getKey().withDayOfMonth(1), entry.getValue(), Double::sum);
            }
        } else {
            grouped.putAll(byDay);
        }

        List<String> labels = new ArrayList<>();
        List<Double> values = new ArrayList<>();
        DateTimeFormatter labelFormat = DateTimeFormatter.ofPattern(byMonth ? "MM/yyyy" : "dd/MM");
        for (Map.Entry<LocalDate, Double> entry : grouped.entrySet()) {
            labels.add(entry.getKey().format(labelFormat));
            values.add(entry.getValue());
        }
        chart.setData(labels, values);
        chartSubtitle.setText(byMonth ? "Gom theo tháng" : "Gom theo ngày");
    }

    private void resetFilter() {
        keywordField.setText("");
        methodCombo.setSelectedIndex(0);
        statusCombo.setSelectedIndex(0);
        fromDateField.setDate(null);
        toDateField.setDate(null);
        applyFilter();
    }

    private static String formatMoney(double value) {
        return String.format(VN, "%,.0f đ", value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    // ===================== BIỂU ĐỒ ĐƠN GIẢN =====================
    // Mỗi ngày (hoặc tháng) là 1 dòng: [nhãn] [thanh ngang] [số tiền].
    // Thanh dài/ngắn theo tỉ lệ so với ngày có doanh thu cao nhất.

    private static final class RevenueChart extends JPanel {
        private RevenueChart() {
            setOpaque(false);
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        }

        private void setData(List<String> labels, List<Double> values) {
            removeAll();
            if (values.isEmpty()) {
                JLabel empty = new JLabel("Không có dữ liệu doanh thu trong khoảng đã chọn");
                empty.setFont(new Font("Arial", Font.PLAIN, 12));
                empty.setForeground(MUTED);
                add(empty);
            } else {
                double max = 0;
                for (double value : values) {
                    max = Math.max(max, value);
                }
                for (int i = 0; i < values.size(); i++) {
                    add(createRow(labels.get(i), values.get(i), max));
                }
            }
            revalidate();
            repaint();
        }

        private JPanel createRow(String label, double value, double max) {
            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);
            row.setBorder(new EmptyBorder(3, 0, 3, 0));
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel dateLabel = new JLabel(label);
            dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
            dateLabel.setForeground(MUTED);
            dateLabel.setPreferredSize(new Dimension(60, 20));

            // Thanh ngang: giá trị 0-100 là phần trăm so với ngày cao nhất
            JProgressBar bar = new JProgressBar(0, 100);
            bar.setValue(max <= 0 ? 0 : (int) Math.round(value * 100 / max));
            bar.setForeground(TEAL);
            bar.setBackground(CANVAS);
            bar.setBorderPainted(false);

            JLabel moneyLabel = new JLabel(formatMoney(value), SwingConstants.RIGHT);
            moneyLabel.setFont(new Font("Arial", Font.BOLD, 11));
            moneyLabel.setForeground(INK);
            moneyLabel.setPreferredSize(new Dimension(110, 20));

            row.add(dateLabel, BorderLayout.WEST);
            row.add(bar, BorderLayout.CENTER);
            row.add(moneyLabel, BorderLayout.EAST);
            return row;
        }
    }

    // ===================== DỮ LIỆU MINH HỌA =====================

    private static List<RevenueRecord> createSampleData() {
        List<RevenueRecord> list = new ArrayList<>();
        list.add(new RevenueRecord("HD001", LocalDate.of(2026, 10, 1), "BN001", "Võ Thanh Hải",
                "NV004", "Phạm Quốc Dũng", "Tiền mặt", "Đã thanh toán", 500000, 500000));
        list.add(new RevenueRecord("HD002", LocalDate.of(2026, 10, 3), "BN002", "Nguyễn Thị Lan",
                "NV004", "Phạm Quốc Dũng", "Chuyển khoản", "Đã thanh toán", 2000000, 2000000));
        list.add(new RevenueRecord("HD003", LocalDate.of(2026, 10, 5), "BN002", "Nguyễn Thị Lan",
                "NV004", "Phạm Quốc Dũng", "Trả góp", "Đang trả góp", 7000000, 3000000));
        list.add(new RevenueRecord("HD004", LocalDate.of(2026, 10, 7), "BN004", "Trần Bảo Ngọc",
                "NV004", "Phạm Quốc Dũng", null, "Chưa thanh toán", 300000, 0));
        list.add(new RevenueRecord("HD005", LocalDate.of(2026, 10, 9), "BN003", "Đặng Quốc Khánh",
                "NV004", "Phạm Quốc Dũng", "Tiền mặt", "Đã thanh toán", 1500000, 1500000));
        list.add(new RevenueRecord("HD006", LocalDate.of(2026, 10, 12), "BN001", "Võ Thanh Hải",
                "NV004", "Phạm Quốc Dũng", "Chuyển khoản", "Đã thanh toán", 1200000, 1200000));
        list.add(new RevenueRecord("HD007", LocalDate.of(2026, 10, 14), "BN003", "Đặng Quốc Khánh",
                "NV004", "Phạm Quốc Dũng", "Trả góp", "Đang trả góp", 3500000, 1500000));
        list.add(new RevenueRecord("HD008", LocalDate.of(2026, 10, 16), "BN002", "Nguyễn Thị Lan",
                "NV004", "Phạm Quốc Dũng", "Tiền mặt", "Đã thanh toán", 200000, 200000));
        list.add(new RevenueRecord("HD009", LocalDate.of(2026, 10, 19), "BN004", "Trần Bảo Ngọc",
                "NV004", "Phạm Quốc Dũng", "Chuyển khoản", "Đã thanh toán", 2000000, 2000000));
        return list;
    }
}