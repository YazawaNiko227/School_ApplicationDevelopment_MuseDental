package gui;

import entity.VatTu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel quản lý vật tư y tế (CRUD) cho mục DANH MỤC → "Quản lý Vật tư y tế".
 * Dùng bảng VatTu, nhưng chỉ quản lý các dòng có LoaiVatTu khác "Thuoc"
 * (phần thuốc do DanhMucQuanLyThuocPanel đảm nhiệm).
 *
 * Bên trái: danh sách + tìm kiếm + lọc loại. Bên phải: form thêm / sửa / xóa.
 * Dữ liệu tạm giữ trong bộ nhớ, chỗ cần nối DAO được đánh dấu "TODO DAO".
 */
public class DanhMucQuanLyVatTuYTePanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(214, 69, 80);

    private static final String LOAI_THUOC = "Thuoc";
    private static final String TAT_CA = "Tất cả loại";
    private static final String[] LOAI_VAT_TU = { "Tieu hao", "Vat lieu", "Dung cu" };
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);
    // Định dạng tiền kiểu Việt Nam: 1.500.000
    private static final NumberFormat MONEY_FMT = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final List<VatTu> danhSach = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã vật tư", "Tên vật tư", "Loại", "Đơn vị", "Tồn kho", "Ngưỡng tối thiểu",
                    "Giá bán", "Hạn sử dụng" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        // Cột số để sắp xếp đúng theo giá trị số.
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 6) {
                return Double.class;
            }
            return (column == 4 || column == 5) ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField txtSearch = new JTextField();
    private final JComboBox<String> cbFilterLoai = new JComboBox<>();
    private final JLabel lblCount = new JLabel();

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JComboBox<String> cbLoai = new JComboBox<>(LOAI_VAT_TU);
    private final JTextField txtDonVi = new JTextField();
    private final JTextField txtTon = new JTextField();
    private final JTextField txtNguong = new JTextField();
    private final JTextField txtGiaBan = new JTextField();
    private final JTextField txtHan = new JTextField();

    private final JButton btnAdd = createButton("Thêm", TEAL, Color.WHITE);
    private final JButton btnUpdate = createButton("Sửa", new Color(51, 91, 177), Color.WHITE);
    private final JButton btnDelete = createButton("Xóa", DANGER, Color.WHITE);
    private final JButton btnClear = createButton("Làm mới", new Color(236, 241, 243), INK);

    public DanhMucQuanLyVatTuYTePanel() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);

        loadData();
        setupTable();
        add(createListCard(), BorderLayout.CENTER);
        add(createFormCard(), BorderLayout.EAST);

        refreshTable();
        clearForm();
    }

    // ---------------------------- Dữ liệu ----------------------------

    /**
     * TODO DAO: thay bằng vatTuDAO.getAllExceptLoai("Thuoc"). Hiện dùng dữ liệu mẫu
     * theo file SQL.
     */
    private void loadData() {
        danhSach.add(new VatTu("VT003", "Gang tay y te", "Tieu hao", "Hop", 40, 10, LocalDate.of(2028, 1, 1), 0));
        danhSach.add(
                new VatTu("VT004", "Composite tram rang", "Vat lieu", "Tuyp", 25, 5, LocalDate.of(2027, 3, 15), 0));
        danhSach.add(new VatTu("VT006", "Mu su (rang su)", "Vat lieu", "Cai", 15, 5, null, 0));
    }

    private VatTu findById(String ma) {
        for (VatTu vt : danhSach) {
            if (vt.getMaVatTu().equalsIgnoreCase(ma)) {
                return vt;
            }
        }
        return null;
    }

    /**
     * Sinh mã VT### tiếp theo. Lưu ý: bảng VatTu dùng chung với thuốc, nên khi nối
     * DAO
     * hãy lấy mã lớn nhất trên toàn bảng VatTu để không trùng với mã thuốc.
     */
    private String nextId() {
        int max = 0;
        for (VatTu vt : danhSach) {
            try {
                max = Math.max(max, Integer.parseInt(vt.getMaVatTu().substring(2)));
            } catch (RuntimeException ignored) {
                // Bỏ qua mã không theo định dạng VT###.
            }
        }
        return String.format("VT%03d", max + 1);
    }

    // ---------------------------- Giao diện ----------------------------

    private void setupTable() {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowSorter(sorter);

        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setForeground(MUTED);
        table.getTableHeader().setBackground(new Color(247, 250, 251));

        CellRenderer renderer = new CellRenderer();
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
            VatTu vt = findById(ma);
            if (vt != null) {
                fillForm(vt);
                setEditing(true);
            }
        });
    }

    private JPanel createListCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(18, 18, 18, 18)));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        styleTextField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(240, 36));
        txtSearch.setToolTipText("Tìm theo mã hoặc tên vật tư");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        cbFilterLoai.addItem(TAT_CA);
        for (String loai : LOAI_VAT_TU) {
            cbFilterLoai.addItem(loai);
        }
        styleCombo(cbFilterLoai);
        cbFilterLoai.setPreferredSize(new Dimension(150, 36));
        cbFilterLoai.addActionListener(event -> applyFilter());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        left.add(txtSearch);
        left.add(cbFilterLoai);

        lblCount.setFont(new Font("Arial", Font.BOLD, 11));
        lblCount.setForeground(MUTED);
        toolbar.add(left, BorderLayout.WEST);
        toolbar.add(lblCount, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);

        card.add(toolbar, BorderLayout.NORTH);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createFormCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(340, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(18, 18, 18, 18)));

        JLabel heading = new JLabel("Thông tin vật tư y tế");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(INK);
        card.add(heading, BorderLayout.NORTH);

        for (JTextField f : new JTextField[] { txtMa, txtTen, txtDonVi, txtTon, txtNguong, txtGiaBan, txtHan }) {
            styleTextField(f);
        }
        styleCombo(cbLoai);
        cbLoai.setEditable(true); // cho phép nhập loại mới ngoài danh sách gợi ý
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(244, 247, 250));
        txtHan.setToolTipText("Định dạng dd/MM/yyyy, để trống nếu vật tư không có hạn dùng");
        txtGiaBan.setToolTipText("Giá bán cho bệnh nhân. Nhập 0 nếu vật tư chỉ dùng nội bộ, không tính tiền riêng");

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createRow(field("Mã vật tư", txtMa), field("Đơn vị tính", txtDonVi)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Tên vật tư *", txtTen)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Loại vật tư", cbLoai)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Số lượng tồn", txtTon), field("Ngưỡng tối thiểu", txtNguong)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Giá bán (VNĐ)", txtGiaBan)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Hạn sử dụng (dd/MM/yyyy)", txtHan)));
        form.add(Box.createVerticalGlue());
        card.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        actions.setOpaque(false);
        actions.add(btnAdd);
        actions.add(btnUpdate);
        actions.add(btnDelete);
        actions.add(btnClear);
        card.add(actions, BorderLayout.SOUTH);

        btnAdd.addActionListener(event -> onAdd());
        btnUpdate.addActionListener(event -> onUpdate());
        btnDelete.addActionListener(event -> onDelete());
        btnClear.addActionListener(event -> clearForm());
        return card;
    }

    private JPanel field(String label, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        lbl.setForeground(MUTED);
        input.setPreferredSize(new Dimension(100, 34));
        panel.add(lbl, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createRow(JPanel... fields) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                row.add(Box.createHorizontalStrut(10));
            }
            row.add(fields[i]);
        }
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 10, 0, 10)));
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("Arial", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(INK);
    }

    private static JButton createButton(String text, Color background, Color foreground) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(foreground);
        button.setBackground(background);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(68, 36));
        return button;
    }

    // ---------------------------- Bảng & form ----------------------------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (VatTu vt : danhSach) {
            tableModel.addRow(new Object[] {
                    vt.getMaVatTu(), vt.getTenVatTu(),
                    vt.getLoaiVatTu() == null ? "" : vt.getLoaiVatTu(),
                    vt.getDonViTinh() == null ? "" : vt.getDonViTinh(),
                    vt.getSoLuongTon(), vt.getNguongToiThieu(), vt.getDonGiaBan(),
                    vt.getHanSuDung() == null ? "" : vt.getHanSuDung().format(DATE_FMT)
            });
        }
        applyFilter();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        String keyword = txtSearch.getText().trim();
        if (!keyword.isEmpty()) {
            filters.add(RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1));
        }
        Object loai = cbFilterLoai.getSelectedItem();
        if (loai != null && !TAT_CA.equals(loai)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(loai.toString()) + "$", 2));
        }
        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        lblCount.setText("Hiển thị " + table.getRowCount() + " / " + danhSach.size() + " vật tư");
    }

    private void fillForm(VatTu vt) {
        txtMa.setText(vt.getMaVatTu());
        txtTen.setText(vt.getTenVatTu());
        cbLoai.setSelectedItem(vt.getLoaiVatTu() == null ? "" : vt.getLoaiVatTu());
        txtDonVi.setText(vt.getDonViTinh() == null ? "" : vt.getDonViTinh());
        txtTon.setText(String.valueOf(vt.getSoLuongTon()));
        txtNguong.setText(String.valueOf(vt.getNguongToiThieu()));
        txtGiaBan.setText(MONEY_FMT.format(vt.getDonGiaBan()));
        txtHan.setText(vt.getHanSuDung() == null ? "" : vt.getHanSuDung().format(DATE_FMT));
    }

    private void clearForm() {
        table.clearSelection();
        txtMa.setText(nextId());
        txtTen.setText("");
        cbLoai.setSelectedIndex(0);
        txtDonVi.setText("");
        txtTon.setText("0");
        txtNguong.setText("0");
        txtGiaBan.setText("0");
        txtHan.setText("");
        setEditing(false);
        txtTen.requestFocusInWindow();
    }

    private void setEditing(boolean editing) {
        btnAdd.setEnabled(!editing);
        btnUpdate.setEnabled(editing);
        btnDelete.setEnabled(editing);
    }

    // ---------------------------- CRUD ----------------------------

    private void onAdd() {
        try {
            VatTu vt = readForm(txtMa.getText().trim());
            if (findById(vt.getMaVatTu()) != null) {
                throw new IllegalArgumentException("Mã vật tư đã tồn tại.");
            }
            // TODO DAO: vatTuDAO.insert(vt);
            danhSach.add(vt);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã thêm vật tư " + vt.getTenVatTu() + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onUpdate() {
        try {
            VatTu updated = readForm(txtMa.getText().trim());
            VatTu current = findById(updated.getMaVatTu());
            if (current == null) {
                throw new IllegalArgumentException("Không tìm thấy vật tư cần cập nhật.");
            }
            // TODO DAO: vatTuDAO.update(updated);
            danhSach.set(danhSach.indexOf(current), updated);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin vật tư.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDelete() {
        VatTu vt = findById(txtMa.getText().trim());
        if (vt == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa vật tư " + vt.getMaVatTu() + " - " + vt.getTenVatTu() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO DAO: vatTuDAO.delete(ma). Vật tư đã nằm trong phiếu nhập / phiếu xuất sẽ
            // vướng
            // khóa ngoại -> bắt SQLException và báo "Vật tư đã được sử dụng, không thể
            // xóa".
            danhSach.remove(vt);
            refreshTable();
            clearForm();
        }
    }

    // ---------------------------- Kiểm tra dữ liệu ----------------------------

    private VatTu readForm(String ma) {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            txtTen.requestFocusInWindow();
            throw new IllegalArgumentException("Tên vật tư không được để trống.");
        }

        Object loaiValue = cbLoai.getSelectedItem();
        String loai = loaiValue == null ? "" : loaiValue.toString().trim();
        if (loai.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn hoặc nhập loại vật tư.");
        }
        if (LOAI_THUOC.equalsIgnoreCase(loai)) {
            throw new IllegalArgumentException("Loại \"Thuoc\" được quản lý ở mục Quản Lý Thuốc.");
        }

        int ton = parseNonNegativeInt(txtTon, "Số lượng tồn");
        int nguong = parseNonNegativeInt(txtNguong, "Ngưỡng tối thiểu");
        double giaBan = parseMoney(txtGiaBan, "Giá bán");

        LocalDate han = null;
        String hanText = txtHan.getText().trim();
        if (!hanText.isEmpty()) {
            try {
                han = LocalDate.parse(hanText, DATE_FMT);
            } catch (DateTimeParseException ex) {
                txtHan.requestFocusInWindow();
                throw new IllegalArgumentException("Hạn sử dụng phải theo dạng dd/MM/yyyy.");
            }
        }
        String donVi = txtDonVi.getText().trim();
        return new VatTu(ma, ten, loai, donVi.isEmpty() ? null : donVi, ton, nguong, han, giaBan);
    }

    private int parseNonNegativeInt(JTextField field, String name) {
        try {
            int value = Integer.parseInt(field.getText().trim());
            if (value < 0) {
                throw new NumberFormatException();
            }
            return value;
        } catch (NumberFormatException ex) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(name + " phải là số nguyên không âm.");
        }
    }

    /**
     * Đọc số tiền: chấp nhận 5000 hoặc 5.000, để trống coi như 0 (vật tư dùng nội
     * bộ).
     */
    private double parseMoney(JTextField field, String name) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            return 0;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (text.contains("-") || digits.isEmpty()) {
            field.requestFocusInWindow();
            throw new IllegalArgumentException(name + " phải là số tiền không âm.");
        }
        return Double.parseDouble(digits);
    }

    // ---------------------------- Renderer ----------------------------

    /** Tô đỏ tồn kho khi thấp hơn ngưỡng và hạn sử dụng khi đã hết hạn. */
    private static final class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Arial", Font.PLAIN, 12));
            setForeground(INK);
            if (!selected) {
                setBackground(Color.WHITE);
            }

            setHorizontalAlignment(SwingConstants.LEFT);
            int modelRow = table.convertRowIndexToModel(row);
            if (column == 4) {
                int ton = (Integer) table.getModel().getValueAt(modelRow, 4);
                int nguong = (Integer) table.getModel().getValueAt(modelRow, 5);
                if (ton <= nguong) {
                    setForeground(DANGER);
                    setFont(new Font("Arial", Font.BOLD, 12));
                }
            } else if (column == 6 && value instanceof Number) {
                double gia = ((Number) value).doubleValue();
                if (gia == 0) {
                    setText("Nội bộ");
                    setForeground(MUTED);
                } else {
                    setText(MONEY_FMT.format(gia) + " đ");
                    setForeground(TEAL);
                    setFont(new Font("Arial", Font.BOLD, 12));
                }
                setHorizontalAlignment(SwingConstants.RIGHT);
            } else if (column == 7 && value != null && !value.toString().isEmpty()) {
                try {
                    if (LocalDate.parse(value.toString(), DATE_FMT).isBefore(LocalDate.now())) {
                        setForeground(DANGER);
                        setFont(new Font("Arial", Font.BOLD, 12));
                    }
                } catch (DateTimeParseException ignored) {
                    // Giữ màu mặc định.
                }
            }
            return this;
        }
    }
}