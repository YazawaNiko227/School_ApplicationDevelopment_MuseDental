/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableRowSorter;

import util.PhanQuyenMenu;

/**
 * Ma trận phân quyền menu theo vai trò (chỉ xem).
 *
 * Dữ liệu lấy trực tiếp từ {@link PhanQuyenMenu}, cũng là nguồn mà sidebar dùng
 * để ẩn/hiện menu, nên bảng này luôn khớp với hành vi thật của hệ thống.
 * Quyền được cấu hình trong mã nguồn, không lưu CSDL.
 */
public final class HeThongPhanQuyenRbacPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color LINE = new Color(226, 234, 237);

    private static final String TAT_CA = "Tất cả vai trò";
    private static final int COT_NHOM = 0;
    private static final int COT_CHUC_NANG = 1;
    private static final int COT_VAI_TRO_DAU = 2;

    private final List<String> maVaiTro = PhanQuyenMenu.danhSachMaVaiTro();
    private final JComboBox<String> roleSelector = new JComboBox<>();
    private final JLabel summary = new JLabel();
    private final DefaultTableModel permissionModel;
    private final JTable permissionTable;
    private final TableRowSorter<DefaultTableModel> sorter;

    public HeThongPhanQuyenRbacPanel() {
        permissionModel = createModel();
        permissionTable = new JTable(permissionModel);
        sorter = new TableRowSorter<>(permissionModel);

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        loadMatrix();
        add(createRoleToolbar(), BorderLayout.NORTH);
        add(createPermissionCard(), BorderLayout.CENTER);
        applyFilter();
    }

    // Bảng chỉ đọc: cột Nhóm, Chức năng và mỗi vai trò một cột Boolean.
    private DefaultTableModel createModel() {
        Object[] columns = new Object[COT_VAI_TRO_DAU + maVaiTro.size()];
        columns[COT_NHOM] = "Nhóm";
        columns[COT_CHUC_NANG] = "Chức năng";
        for (int i = 0; i < maVaiTro.size(); i++) {
            columns[COT_VAI_TRO_DAU + i] = PhanQuyenMenu.tenVaiTro(maVaiTro.get(i));
        }
        return new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex < COT_VAI_TRO_DAU ? String.class : Boolean.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    // Nạp ma trận từ PhanQuyenMenu, mỗi mục menu là một hàng.
    private void loadMatrix() {
        permissionModel.setRowCount(0);
        for (Map.Entry<String, List<String>> group : PhanQuyenMenu.nhomMenu().entrySet()) {
            for (String item : group.getValue()) {
                Object[] row = new Object[COT_VAI_TRO_DAU + maVaiTro.size()];
                row[COT_NHOM] = group.getKey();
                row[COT_CHUC_NANG] = item;
                for (int i = 0; i < maVaiTro.size(); i++) {
                    row[COT_VAI_TRO_DAU + i] = PhanQuyenMenu.duocXem(maVaiTro.get(i), item);
                }
                permissionModel.addRow(row);
            }
        }
    }

    // Chọn vai trò để chỉ xem các mục vai trò đó được thấy.
    private JPanel createRoleToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel rolePicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePicker.setBackground(Color.WHITE);
        rolePicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel roleLabel = new JLabel("Xem theo vai trò:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleLabel.setForeground(INK);
        roleSelector.addItem(TAT_CA);
        for (String ma : maVaiTro) {
            roleSelector.addItem(PhanQuyenMenu.tenVaiTro(ma));
        }
        roleSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        roleSelector.setPreferredSize(new Dimension(190, 36));
        roleSelector.addActionListener(event -> applyFilter());
        rolePicker.add(roleLabel);
        rolePicker.add(roleSelector);

        JLabel modeLabel = new JLabel("CẤU HÌNH TRONG MÃ NGUỒN · CHỈ XEM");
        modeLabel.setFont(new Font("Arial", Font.BOLD, 9));
        modeLabel.setForeground(new Color(26, 120, 120));
        modeLabel.setOpaque(true);
        modeLabel.setBackground(new Color(231, 246, 245));
        modeLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        toolbar.add(rolePicker, BorderLayout.WEST);
        toolbar.add(modeLabel, BorderLayout.EAST);
        return toolbar;
    }

    // Thẻ chứa bảng ma trận và ghi chú.
    private JPanel createPermissionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(17, 18, 14, 18)));

        JLabel title = new JLabel("Quyền truy cập menu theo vai trò");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        summary.setFont(new Font("Arial", Font.PLAIN, 11));
        summary.setForeground(MUTED);
        JPanel headingText = new JPanel(new GridLayout(2, 1, 0, 4));
        headingText.setOpaque(false);
        headingText.add(title);
        headingText.add(summary);

        configurePermissionTable();
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel note = new JLabel(
                "<html>Vai trò của tài khoản được lấy từ cột MaVaiTro khi đăng nhập; menu không "
                        + "thuộc vai trò sẽ bị ẩn khỏi thanh điều hướng. Mục \"Tổng quan\" luôn hiển thị. "
                        + "Muốn đổi quyền, chỉnh trong lớp PhanQuyenMenu.</html>");
        note.setFont(new Font("Arial", Font.PLAIN, 10));
        note.setForeground(MUTED);

        card.add(headingText, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    // Cấu hình hiển thị bảng, giữ nguyên thứ tự nhóm của sidebar (không cho sắp
    // xếp).
    private void configurePermissionTable() {
        permissionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        permissionTable.setForeground(INK);
        permissionTable.setRowHeight(34);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        permissionTable.setGridColor(new Color(237, 242, 244));
        permissionTable.setShowVerticalLines(false);
        permissionTable.setFillsViewportHeight(true);
        permissionTable.setFocusable(false);
        permissionTable.setRowSelectionAllowed(false);

        for (int column = 0; column < permissionModel.getColumnCount(); column++) {
            sorter.setSortable(column, false);
        }
        permissionTable.setRowSorter(sorter);

        JTableHeader header = permissionTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 251));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);

        permissionTable.getColumnModel().getColumn(COT_NHOM).setPreferredWidth(90);
        permissionTable.getColumnModel().getColumn(COT_CHUC_NANG).setPreferredWidth(300);
        for (int column = COT_VAI_TRO_DAU; column < permissionTable.getColumnCount(); column++) {
            permissionTable.getColumnModel().getColumn(column).setPreferredWidth(95);
        }
    }

    // Lọc các hàng theo vai trò đang chọn và cập nhật dòng tóm tắt.
    private void applyFilter() {
        int selected = roleSelector.getSelectedIndex();
        int total = permissionModel.getRowCount();
        if (selected <= 0) {
            sorter.setRowFilter(null);
            summary.setText("Hiển thị đủ " + maVaiTro.size() + " vai trò × " + total
                    + " chức năng. Chọn một vai trò để chỉ xem các mục vai trò đó được thấy.");
            return;
        }
        final int column = COT_VAI_TRO_DAU + selected - 1;
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return Boolean.TRUE.equals(entry.getValue(column));
            }
        });
        summary.setText(roleSelector.getSelectedItem() + " được thấy " + permissionTable.getRowCount()
                + "/" + total + " chức năng (chưa kể mục Tổng quan).");
    }
}