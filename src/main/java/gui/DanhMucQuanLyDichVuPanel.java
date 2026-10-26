package gui;

import entity.DichVu;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

/**
 * Panel quản lý dịch vụ và bảng giá (CRUD) cho mục DANH MỤC → "Quản lý dịch vụ
 * và bảng giá".
 * Dùng bảng DichVu: mã, tên, đơn giá, thời gian bảo hành (tháng) và mô tả.
 *
 * Bên trái: bảng giá + tìm kiếm (bấm tiêu đề cột để sắp xếp, ví dụ theo đơn
 * giá).
 * Bên phải: form thêm / sửa / xóa.
 * Dữ liệu tạm giữ trong bộ nhớ, chỗ cần nối DAO được đánh dấu "TODO DAO".
 */
public class DanhMucQuanLyDichVuPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(214, 69, 80);

    // Định dạng tiền kiểu Việt Nam: 1.500.000
    private static final NumberFormat MONEY_FMT = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private final List<DichVu> danhSach = new ArrayList<>();
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã dịch vụ", "Tên dịch vụ", "Đơn giá", "Bảo hành", "Mô tả" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        // Cột đơn giá và bảo hành là số để sắp xếp đúng theo giá trị.
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 2) {
                return Double.class;
            }
            return column == 3 ? Integer.class : String.class;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel();

    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();
    private final JTextField txtDonGia = new JTextField();
    private final JTextField txtBaoHanh = new JTextField();
    private final JTextArea txtMoTa = new JTextArea(4, 20);

    private final JButton btnAdd = createButton("Thêm", TEAL, Color.WHITE);
    private final JButton btnUpdate = createButton("Sửa", new Color(51, 91, 177), Color.WHITE);
    private final JButton btnDelete = createButton("Xóa", DANGER, Color.WHITE);
    private final JButton btnClear = createButton("Làm mới", new Color(236, 241, 243), INK);

    public DanhMucQuanLyDichVuPanel() {
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
     * TODO DAO: thay bằng dichVuDAO.getAll(). Hiện dùng dữ liệu mẫu theo file SQL.
     */
    private void loadData() {
        danhSach.add(new DichVu("DV001", "Lay cao rang", 200000, 0, "Lay cao va danh bong"));
        danhSach.add(new DichVu("DV002", "Tram rang composite", 300000, 6, "Tram rang sau bang composite"));
        danhSach.add(new DichVu("DV003", "Nho rang khon", 1500000, 0, "Tieu phau nho rang khon moc lech"));
        danhSach.add(new DichVu("DV004", "Boc rang su", 3500000, 60, "Boc rang su tham my / 1 rang"));
        danhSach.add(new DichVu("DV005", "Dieu tri tuy rang", 1200000, 12, "Lay tuy va tram bit ong tuy"));
        danhSach.add(new DichVu("DV006", "Tay trang rang", 2000000, 12, "Tay trang tai phong kham"));
    }

    private DichVu findById(String ma) {
        for (DichVu dv : danhSach) {
            if (dv.getMaDichVu().equalsIgnoreCase(ma)) {
                return dv;
            }
        }
        return null;
    }

    /** Sinh mã DV### tiếp theo dựa trên mã lớn nhất hiện có. */
    private String nextId() {
        int max = 0;
        for (DichVu dv : danhSach) {
            try {
                max = Math.max(max, Integer.parseInt(dv.getMaDichVu().substring(2)));
            } catch (RuntimeException ignored) {
                // Bỏ qua mã không theo định dạng DV###.
            }
        }
        return String.format("DV%03d", max + 1);
    }

    private int warrantyOf(DichVu dv) {
        Integer months = dv.getThoiGianBaoHanh();
        return months == null ? 0 : months;
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
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(2).setPreferredWidth(110);
        table.getColumnModel().getColumn(3).setPreferredWidth(90);
        table.getColumnModel().getColumn(4).setPreferredWidth(220);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || table.getSelectedRow() < 0) {
                return;
            }
            String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
            DichVu dv = findById(ma);
            if (dv != null) {
                fillForm(dv);
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
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.setToolTipText("Tìm theo mã hoặc tên dịch vụ");
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
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(txtSearch);

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

        JLabel heading = new JLabel("Thông tin dịch vụ");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(INK);
        card.add(heading, BorderLayout.NORTH);

        for (JTextField f : new JTextField[] { txtMa, txtTen, txtDonGia, txtBaoHanh }) {
            styleTextField(f);
        }
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(244, 247, 250));
        txtDonGia.setToolTipText("Nhập số tiền, ví dụ 1500000 hoặc 1.500.000");
        txtBaoHanh.setToolTipText("Số tháng bảo hành, nhập 0 nếu không bảo hành");

        txtMoTa.setFont(new Font("Arial", Font.PLAIN, 12));
        txtMoTa.setForeground(INK);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        txtMoTa.setBorder(new EmptyBorder(8, 10, 8, 10));
        JScrollPane moTaScroll = new JScrollPane(txtMoTa);
        moTaScroll.setBorder(BorderFactory.createLineBorder(LINE));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createRow(field("Mã dịch vụ", txtMa), field("Bảo hành (tháng)", txtBaoHanh)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Tên dịch vụ *", txtTen)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Đơn giá (VNĐ) *", txtDonGia)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Mô tả", moTaScroll, 100)));
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
        return field(label, input, 34);
    }

    private JPanel field(String label, JComponent input, int inputHeight) {
        JPanel panel = new JPanel(new BorderLayout(0, 5));
        panel.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Arial", Font.BOLD, 10));
        lbl.setForeground(MUTED);
        input.setPreferredSize(new Dimension(100, inputHeight));
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
        for (DichVu dv : danhSach) {
            tableModel.addRow(new Object[] {
                    dv.getMaDichVu(), dv.getTenDichVu(), dv.getDonGia(), warrantyOf(dv),
                    dv.getMoTa() == null ? "" : dv.getMoTa()
            });
        }
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1));
        lblCount.setText("Hiển thị " + table.getRowCount() + " / " + danhSach.size() + " dịch vụ");
    }

    private void fillForm(DichVu dv) {
        txtMa.setText(dv.getMaDichVu());
        txtTen.setText(dv.getTenDichVu());
        txtDonGia.setText(MONEY_FMT.format(dv.getDonGia()));
        txtBaoHanh.setText(String.valueOf(warrantyOf(dv)));
        txtMoTa.setText(dv.getMoTa() == null ? "" : dv.getMoTa());
        txtMoTa.setCaretPosition(0);
    }

    private void clearForm() {
        table.clearSelection();
        txtMa.setText(nextId());
        txtTen.setText("");
        txtDonGia.setText("");
        txtBaoHanh.setText("0");
        txtMoTa.setText("");
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
            DichVu dv = readForm(txtMa.getText().trim());
            if (findById(dv.getMaDichVu()) != null) {
                throw new IllegalArgumentException("Mã dịch vụ đã tồn tại.");
            }
            // TODO DAO: dichVuDAO.insert(dv);
            danhSach.add(dv);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã thêm dịch vụ " + dv.getTenDichVu() + ".");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onUpdate() {
        try {
            DichVu updated = readForm(txtMa.getText().trim());
            DichVu current = findById(updated.getMaDichVu());
            if (current == null) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ cần cập nhật.");
            }
            // TODO DAO: dichVuDAO.update(updated);
            // Hóa đơn cũ không bị ảnh hưởng khi đổi giá vì ChiTietHoaDon đã lưu
            // DonGiaTaiThoiDiem.
            danhSach.set(danhSach.indexOf(current), updated);
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã cập nhật thông tin dịch vụ.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void onDelete() {
        DichVu dv = findById(txtMa.getText().trim());
        if (dv == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa dịch vụ " + dv.getMaDichVu() + " - " + dv.getTenDichVu() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO DAO: dichVuDAO.delete(ma). Dịch vụ đã nằm trong kế hoạch điều trị / hóa
            // đơn sẽ
            // vướng khóa ngoại -> bắt SQLException và báo "Dịch vụ đã được sử dụng, không
            // thể xóa".
            danhSach.remove(dv);
            refreshTable();
            clearForm();
        }
    }

    // ---------------------------- Kiểm tra dữ liệu ----------------------------

    private DichVu readForm(String ma) {
        String ten = txtTen.getText().trim();
        if (ten.isEmpty()) {
            txtTen.requestFocusInWindow();
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (ten.length() > 150) {
            throw new IllegalArgumentException("Tên dịch vụ tối đa 150 ký tự.");
        }

        // Bỏ dấu chấm, phẩy, khoảng trắng, chữ "đ"... chỉ giữ lại chữ số.
        String digits = txtDonGia.getText().replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            txtDonGia.requestFocusInWindow();
            throw new IllegalArgumentException("Vui lòng nhập đơn giá hợp lệ (số tiền không âm).");
        }
        double donGia = Double.parseDouble(digits);

        int baoHanh;
        String baoHanhText = txtBaoHanh.getText().trim();
        try {
            baoHanh = baoHanhText.isEmpty() ? 0 : Integer.parseInt(baoHanhText);
            if (baoHanh < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            txtBaoHanh.requestFocusInWindow();
            throw new IllegalArgumentException("Thời gian bảo hành phải là số tháng không âm.");
        }

        String moTa = txtMoTa.getText().trim();
        if (moTa.length() > 500) {
            throw new IllegalArgumentException("Mô tả tối đa 500 ký tự.");
        }
        return new DichVu(ma, ten, donGia, baoHanh, moTa.isEmpty() ? null : moTa);
    }

    // ---------------------------- Renderer ----------------------------

    /**
     * Hiển thị đơn giá dạng 1.500.000 đ (căn phải) và bảo hành dạng "12 tháng" /
     * "Không".
     */
    private static final class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setFont(new Font("Arial", Font.PLAIN, 12));
            setForeground(INK);
            setHorizontalAlignment(SwingConstants.LEFT);
            if (!selected) {
                setBackground(Color.WHITE);
            }

            if (column == 2 && value instanceof Number) {
                setText(MONEY_FMT.format(((Number) value).doubleValue()) + " đ");
                setHorizontalAlignment(SwingConstants.RIGHT);
                setForeground(TEAL);
                setFont(new Font("Arial", Font.BOLD, 12));
            } else if (column == 3 && value instanceof Number) {
                int months = ((Number) value).intValue();
                setText(months == 0 ? "Không" : months + " tháng");
            }
            return this;
        }
    }
}