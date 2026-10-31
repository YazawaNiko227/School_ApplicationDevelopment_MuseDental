/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import dao.PhanQuyenDAO;
import entity.VaiTro;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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

public final class HeThongPhanQuyenRbacPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color LINE = new Color(226, 234, 237);
    private static final String TAT_CA = "Tất cả vai trò";
    private static final int COT_NHOM = 0;
    private static final int COT_CHUC_NANG = 1;
    private static final int COT_VAI_TRO_DAU = 2;

    private final PhanQuyenDAO phanQuyenDAO = new PhanQuyenDAO();
    private List<VaiTro> vaiTros;
    private JComboBox<String> roleSelector;
    private JLabel summary;
    private DefaultTableModel permissionModel;
    private JTable permissionTable;
    private TableRowSorter<DefaultTableModel> sorter;

    public HeThongPhanQuyenRbacPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        try {
            phanQuyenDAO.khoiTaoQuyenMacDinhNeuChuaCo();
            vaiTros = phanQuyenDAO.layDanhSachVaiTro();
            permissionModel = createModel();
            permissionTable = new JTable(permissionModel);
            sorter = new TableRowSorter<>(permissionModel);
            loadMatrix();
            add(createRoleToolbar(), BorderLayout.NORTH);
            add(createPermissionCard(), BorderLayout.CENTER);
            applyFilter();
        } catch (SQLException exception) {
            add(createErrorPanel(exception), BorderLayout.CENTER);
        }
    }

    private DefaultTableModel createModel() {
        Object[] columns = new Object[COT_VAI_TRO_DAU + vaiTros.size()];
        columns[COT_NHOM] = "Nhóm";
        columns[COT_CHUC_NANG] = "Chức năng";
        for (int i = 0; i < vaiTros.size(); i++) {
            columns[COT_VAI_TRO_DAU + i] = vaiTros.get(i).getTenVaiTro();
        }
        return new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return columnIndex < COT_VAI_TRO_DAU ? String.class : Boolean.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column >= COT_VAI_TRO_DAU;
            }
        };
    }

    private void loadMatrix() throws SQLException {
        permissionModel.setRowCount(0);
        Map<String, Set<String>> storedPermissions = phanQuyenDAO.layTatCaQuyen();
        for (Map.Entry<String, List<String>> group : PhanQuyenMenu.nhomMenu().entrySet()) {
            for (String item : group.getValue()) {
                Object[] row = new Object[COT_VAI_TRO_DAU + vaiTros.size()];
                row[COT_NHOM] = group.getKey();
                row[COT_CHUC_NANG] = item;
                for (int i = 0; i < vaiTros.size(); i++) {
                    Set<String> allowed = storedPermissions.get(vaiTros.get(i).getMaVaiTro());
                    row[COT_VAI_TRO_DAU + i] = allowed != null && allowed.contains(item);
                }
                permissionModel.addRow(row);
            }
        }
    }

    private JPanel createRoleToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel rolePicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePicker.setBackground(Color.WHITE);
        rolePicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel roleLabel = new JLabel("Lọc theo vai trò:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleLabel.setForeground(INK);

        roleSelector = new JComboBox<>();
        roleSelector.addItem(TAT_CA);
        for (VaiTro vaiTro : vaiTros) {
            roleSelector.addItem(vaiTro.getTenVaiTro());
        }
        roleSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        roleSelector.setPreferredSize(new Dimension(190, 36));
        roleSelector.addActionListener(event -> applyFilter());
        rolePicker.add(roleLabel);
        rolePicker.add(roleSelector);

        JButton saveButton = new JButton("Lưu phân quyền");
        saveButton.setFont(new Font("Arial", Font.BOLD, 11));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(26, 151, 151));
        saveButton.setBorderPainted(false);
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(140, 38));
        saveButton.addActionListener(event -> savePermissions());

        toolbar.add(rolePicker, BorderLayout.WEST);
        toolbar.add(saveButton, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel createPermissionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(17, 18, 14, 18)));

        JLabel title = new JLabel("Quyền xem chức năng theo vai trò");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        summary = new JLabel();
        summary.setFont(new Font("Arial", Font.PLAIN, 11));
        summary.setForeground(MUTED);
        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.add(title);
        heading.add(summary);

        configurePermissionTable();
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel note = new JLabel(
                "<html>Quyền được lưu trong SQL Server và áp dụng khi người dùng đăng nhập lần tiếp theo. "
                        + "Mục \"Tổng quan\" luôn hiển thị. Phân quyền hiện kiểm soát hiển thị menu, "
                        + "chưa thay thế kiểm tra quyền ở từng thao tác dữ liệu.</html>");
        note.setFont(new Font("Arial", Font.PLAIN, 10));
        note.setForeground(MUTED);

        card.add(heading, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private void configurePermissionTable() {
        permissionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        permissionTable.setForeground(INK);
        permissionTable.setRowHeight(34);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        permissionTable.setGridColor(new Color(237, 242, 244));
        permissionTable.setShowVerticalLines(false);
        permissionTable.setFillsViewportHeight(true);
        permissionTable.setRowSelectionAllowed(false);
        permissionTable.setRowSorter(sorter);
        for (int column = 0; column < permissionModel.getColumnCount(); column++) {
            sorter.setSortable(column, false);
        }

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

    private void savePermissions() {
        if (permissionTable.isEditing() && !permissionTable.getCellEditor().stopCellEditing()) {
            return;
        }
        Map<String, Set<String>> permissions = new LinkedHashMap<>();
        for (VaiTro vaiTro : vaiTros) {
            permissions.put(vaiTro.getMaVaiTro(), new LinkedHashSet<>());
        }
        for (int row = 0; row < permissionModel.getRowCount(); row++) {
            String function = String.valueOf(permissionModel.getValueAt(row, COT_CHUC_NANG));
            for (int column = 0; column < vaiTros.size(); column++) {
                if (Boolean.TRUE.equals(permissionModel.getValueAt(row, COT_VAI_TRO_DAU + column))) {
                    permissions.get(vaiTros.get(column).getMaVaiTro()).add(function);
                }
            }
        }
        try {
            phanQuyenDAO.capNhatTatCaQuyen(permissions);
            JOptionPane.showMessageDialog(
                    this,
                    "Đã lưu phân quyền. Thay đổi sẽ có hiệu lực sau khi tài khoản đăng nhập lại.",
                    "Thành công",
                    JOptionPane.INFORMATION_MESSAGE);
            applyFilter();
        } catch (SQLException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    "Không thể lưu phân quyền:\n" + exception.getMessage(),
                    "Lỗi phân quyền",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void applyFilter() {
        if (sorter == null || roleSelector == null || summary == null) {
            return;
        }
        int selected = roleSelector.getSelectedIndex();
        int total = permissionModel.getRowCount();
        if (selected <= 0) {
            sorter.setRowFilter(null);
            summary.setText("Hiển thị " + vaiTros.size() + " vai trò × " + total
                    + " chức năng. Chọn ô để cấp hoặc thu hồi quyền xem.");
            return;
        }
        final int column = COT_VAI_TRO_DAU + selected - 1;
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                return Boolean.TRUE.equals(entry.getValue(column));
            }
        });
        summary.setText(roleSelector.getSelectedItem() + " được cấp quyền xem "
                + permissionTable.getRowCount() + "/" + total + " chức năng.");
    }

    private JPanel createErrorPanel(SQLException exception) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(20, 22, 20, 22)));
        JLabel message = new JLabel("<html><b>Không thể tải cấu hình phân quyền từ CSDL.</b><br>"
                + exception.getMessage() + "<br>Hãy chạy database/Migration_RBAC.sql trong SSMS.</html>");
        message.setForeground(new Color(184, 55, 55));
        panel.add(message, BorderLayout.NORTH);
        return panel;
    }
}
