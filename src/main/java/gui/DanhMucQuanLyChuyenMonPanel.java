package gui;

import entity.ChuyenMon;
import service.ChuyenMonService;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

/** Danh mục: quản lý chuyên môn (thêm / sửa / xóa / tìm kiếm). */
public class DanhMucQuanLyChuyenMonPanel extends JPanel {
    // Bảng màu dùng chung với ManHinhChinhFrame.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color SELECT_BG = new Color(231, 246, 245);
    private static final Color DANGER = new Color(214, 69, 80);

    private final List<ChuyenMon> danhSach = new ArrayList<>();
    private final Map<String, Integer> soNhanVien = new HashMap<>();
    private final ChuyenMonService chuyenMonService = new ChuyenMonService();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã chuyên môn", "Tên chuyên môn", "Số nhân viên" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

    // Thanh công cụ.
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblCount = new JLabel();

    // Biểu mẫu.
    private final JTextField txtMa = new JTextField();
    private final JTextField txtTen = new JTextField();

    private final JButton btnAdd = createButton("Thêm", TEAL, Color.WHITE);
    private final JButton btnUpdate = createButton("Sửa", new Color(51, 91, 177), Color.WHITE);
    private final JButton btnDelete = createButton("Xóa", DANGER, Color.WHITE);
    private final JButton btnClear = createButton("Làm mới", new Color(236, 241, 243), INK);

    public DanhMucQuanLyChuyenMonPanel() {
        setLayout(new BorderLayout(18, 0));
        setOpaque(false);

        loadData();
        setupTable();
        add(createListCard(), BorderLayout.CENTER);
        add(createFormCard(), BorderLayout.EAST);

        refreshTable();
        clearForm();
    }

    // ------------------------------------------------------------------
    // Dữ liệu
    // ------------------------------------------------------------------

    private void loadData() {
        try {
            List<ChuyenMon> all = chuyenMonService.layTatCa();
            Map<String, Integer> counts = chuyenMonService.demNhanVienTheoChuyenMon();
            danhSach.clear();
            danhSach.addAll(all);
            soNhanVien.clear();
            soNhanVien.putAll(counts);
        } catch (SQLException exception) {
            hienThiLoiCSDL("Không thể tải danh sách chuyên môn", exception);
        }
    }

    private ChuyenMon findById(String ma) {
        for (ChuyenMon cm : danhSach) {
            if (cm.getMaChuyenMon().equalsIgnoreCase(ma)) {
                return cm;
            }
        }
        return null;
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------

    private void setupTable() {
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setSelectionBackground(SELECT_BG);
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
        table.getColumnModel().getColumn(0).setPreferredWidth(110);
        table.getColumnModel().getColumn(1).setPreferredWidth(260);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);

        table.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting()) {
                return;
            }
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) {
                return;
            }
            String ma = (String) tableModel.getValueAt(table.convertRowIndexToModel(viewRow), 0);
            ChuyenMon cm = findById(ma);
            if (cm != null) {
                fillForm(cm);
                setEditing(true);
            }
        });
    }

    private JPanel createListCard() {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(18, 18, 18, 18)));

        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        left.setOpaque(false);
        styleTextField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(280, 36));
        txtSearch.setToolTipText("Tìm theo mã hoặc tên chuyên môn");
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
        card.setPreferredSize(new Dimension(390, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(18, 18, 18, 18)));

        JLabel heading = new JLabel("Thông tin chuyên môn");
        heading.setFont(new Font("Arial", Font.BOLD, 15));
        heading.setForeground(INK);
        card.add(heading, BorderLayout.NORTH);

        styleTextField(txtMa);
        styleTextField(txtTen);
        txtMa.setEditable(false);
        txtMa.setBackground(new Color(244, 247, 250));

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createRow(field("Mã chuyên môn", txtMa)));
        form.add(Box.createVerticalStrut(12));
        form.add(createRow(field("Tên chuyên môn *", txtTen)));
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

    /** Một ô nhập gồm nhãn phía trên và control phía dưới. */
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
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 10, 0, 10)));
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
        button.setPreferredSize(new Dimension(78, 36));
        return button;
    }

    // ------------------------------------------------------------------
    // Bảng, lọc, form
    // ------------------------------------------------------------------

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (ChuyenMon cm : danhSach) {
            tableModel.addRow(new Object[] {
                    cm.getMaChuyenMon(), cm.getTenChuyenMon(),
                    soNhanVien.getOrDefault(cm.getMaChuyenMon(), 0) });
        }
        applyFilter();
    }

    private void applyFilter() {
        String keyword = txtSearch.getText().trim();
        sorter.setRowFilter(keyword.isEmpty() ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1));
        lblCount.setText("Hiển thị " + table.getRowCount() + " / " + danhSach.size() + " chuyên môn");
    }

    private void fillForm(ChuyenMon cm) {
        txtMa.setText(cm.getMaChuyenMon());
        txtTen.setText(cm.getTenChuyenMon());
    }

    /** Đưa form về trạng thái thêm mới. */
    private void clearForm() {
        table.clearSelection();
        txtMa.setText(chuyenMonService.sinhMa(danhSach));
        txtTen.setText("");
        setEditing(false);
        txtTen.requestFocusInWindow();
    }

    /** editing = true khi đang chọn một chuyên môn có sẵn (cho phép Sửa / Xóa). */
    private void setEditing(boolean editing) {
        btnAdd.setEnabled(!editing);
        btnUpdate.setEnabled(editing);
        btnDelete.setEnabled(editing);
    }

    // ------------------------------------------------------------------
    // CRUD
    // ------------------------------------------------------------------

    private void onAdd() {
        try {
            ChuyenMon cm = chuyenMonService.taoChuyenMon(txtMa.getText(), txtTen.getText());
            if (!chuyenMonService.them(cm)) {
                throw new SQLException("Không thêm được chuyên môn.");
            }
            loadData();
            refreshTable();
            clearForm();
            JOptionPane.showMessageDialog(this, "Đã thêm chuyên môn " + cm.getTenChuyenMon() + ".",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            hienThiLoiCSDL("Không thể thêm chuyên môn", ex);
        }
    }

    private void onUpdate() {
        try {
            ChuyenMon updated = chuyenMonService.taoChuyenMon(txtMa.getText(), txtTen.getText());
            if (!chuyenMonService.capNhat(updated)) {
                throw new SQLException("Không tìm thấy chuyên môn cần cập nhật trong CSDL.");
            }
            loadData();
            refreshTable();
            selectRowById(updated.getMaChuyenMon());
            JOptionPane.showMessageDialog(this, "Đã cập nhật chuyên môn.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Dữ liệu không hợp lệ",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            hienThiLoiCSDL("Không thể cập nhật chuyên môn", ex);
        }
    }

    private void onDelete() {
        ChuyenMon cm = findById(txtMa.getText().trim());
        if (cm == null) {
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa chuyên môn " + cm.getMaChuyenMon() + " - "
                        + cm.getTenChuyenMon() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (!chuyenMonService.xoa(cm.getMaChuyenMon())) {
                throw new SQLException("Không tìm thấy chuyên môn trong CSDL.");
            }
            loadData();
            refreshTable();
            clearForm();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Không thể xóa",
                    JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            hienThiLoiCSDL("Không thể xóa chuyên môn", ex);
        }
    }

    private void hienThiLoiCSDL(String thaoTac, SQLException exception) {
        JOptionPane.showMessageDialog(
                this,
                thaoTac + " do lỗi CSDL:\n" + exception.getMessage(),
                "Lỗi CSDL",
                JOptionPane.ERROR_MESSAGE);
    }

    private void selectRowById(String ma) {
        for (int i = 0; i < table.getRowCount(); i++) {
            if (ma.equals(tableModel.getValueAt(table.convertRowIndexToModel(i), 0))) {
                table.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    // ------------------------------------------------------------------
    // Renderer: padding cho ô, cột số nhân viên căn giữa
    // ------------------------------------------------------------------

    private static final class CellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected,
                boolean focus, int row, int column) {
            super.getTableCellRendererComponent(table, value, selected, false, row, column);
            setBorder(new EmptyBorder(0, 10, 0, 10));
            setHorizontalAlignment(column == 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
            setFont(new Font("Arial", Font.PLAIN, 12));
            setForeground(INK);
            if (!selected) {
                setBackground(Color.WHITE);
            }
            return this;
        }
    }
}