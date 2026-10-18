/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * Ma trận quyền RBAC minh họa theo vai trò; chưa lưu xuống cơ sở dữ liệu.
 */
public final class PhanQuyenRbacPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ACCENT = new Color(218, 241, 239);
    private static final String[] ROLES = {
            "Quản trị viên", "Bác sĩ", "Y tá", "Lễ tân", "Nhân viên kho"
    };
    private static final Object[][] PERMISSIONS = {
            { "Hồ sơ bệnh nhân", false, false, false, false },
            { "Hồ sơ bệnh án EMR", false, false, false, false },
            { "Lịch hẹn và tiếp đón", false, false, false, false },
            { "Kê đơn và phác đồ", false, false, false, false },
            { "Hóa đơn và công nợ", false, false, false, false },
            { "Thuốc và vật tư", false, false, false, false },
            { "Nhân viên và phân quyền", false, false, false, false },
            { "Báo cáo và thống kê", false, false, false, false }
    };

    private final JComboBox<String> roleSelector = new JComboBox<>(ROLES);
    private final DefaultTableModel permissionModel = new DefaultTableModel(
            new Object[] { "Chức năng", "Xem", "Thêm", "Sửa", "Xóa" }, 0) {
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 0 ? String.class : Boolean.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column > 0;
        }
    };
    private final JTable permissionTable = new JTable(permissionModel);

    public PhanQuyenRbacPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        add(createRoleToolbar(), BorderLayout.NORTH);
        add(createPermissionCard(), BorderLayout.CENTER);
        loadRolePermissions();
    }

    // Cho chọn vai trò cần xem quyền; đổi vai trò nạp bộ quyền demo tương ứng.
    private JPanel createRoleToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JPanel rolePicker = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rolePicker.setBackground(Color.WHITE);
        rolePicker.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 14, 12, 14)));
        JLabel roleLabel = new JLabel("Vai trò:");
        roleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        roleLabel.setForeground(INK);
        roleSelector.setFont(new Font("Arial", Font.PLAIN, 12));
        roleSelector.setPreferredSize(new Dimension(190, 36));
        roleSelector.addActionListener(event -> loadRolePermissions());
        rolePicker.add(roleLabel);
        rolePicker.add(roleSelector);

        JLabel demoLabel = new JLabel("QUYỀN MẪU — CHƯA LƯU CSDL");
        demoLabel.setFont(new Font("Arial", Font.BOLD, 9));
        demoLabel.setForeground(new Color(139, 104, 38));
        demoLabel.setOpaque(true);
        demoLabel.setBackground(new Color(255, 246, 224));
        demoLabel.setBorder(new EmptyBorder(8, 10, 8, 10));
        toolbar.add(rolePicker, BorderLayout.WEST);
        toolbar.add(demoLabel, BorderLayout.EAST);
        return toolbar;
    }

    // Tạo bảng ma trận quyền cùng các nút đặt lại và lưu.
    private JPanel createPermissionCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(17, 18, 14, 18)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Quyền theo chức năng");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JLabel hint = new JLabel("Chọn hoặc bỏ chọn quyền Xem, Thêm, Sửa, Xóa cho vai trò.");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        JPanel headingText = new JPanel(new GridLayout(2, 1, 0, 4));
        headingText.setOpaque(false);
        headingText.add(title);
        headingText.add(hint);
        heading.add(headingText, BorderLayout.WEST);

        configurePermissionTable();
        JScrollPane scrollPane = new JScrollPane(permissionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(LINE));
        scrollPane.getViewport().setBackground(Color.WHITE);

        JLabel securityNote = new JLabel(
                "<html>Nguyên tắc khuyến nghị: chỉ cấp quyền tối thiểu cần thiết cho từng vai trò.</html>");
        securityNote.setFont(new Font("Arial", Font.PLAIN, 10));
        securityNote.setForeground(MUTED);

        JButton resetButton = createButton("Khôi phục quyền mẫu", false);
        resetButton.addActionListener(event -> loadRolePermissions());
        JButton saveButton = createButton("Lưu phân quyền", true);
        saveButton.addActionListener(event -> showSaveNotice());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(resetButton);
        actions.add(saveButton);

        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setOpaque(false);
        footer.add(securityNote, BorderLayout.CENTER);
        footer.add(actions, BorderLayout.EAST);
        card.add(heading, BorderLayout.NORTH);
        card.add(scrollPane, BorderLayout.CENTER);
        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    // Cấu hình bảng quyền có cột checkbox và chiều rộng dễ đọc.
    private void configurePermissionTable() {
        permissionTable.setFont(new Font("Arial", Font.PLAIN, 12));
        permissionTable.setForeground(INK);
        permissionTable.setRowHeight(40);
        permissionTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        permissionTable.setGridColor(new Color(237, 242, 244));
        permissionTable.setShowVerticalLines(false);
        permissionTable.setFillsViewportHeight(true);
        JTableHeader header = permissionTable.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 11));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 251));
        header.setPreferredSize(new Dimension(0, 38));
        permissionTable.getColumnModel().getColumn(0).setPreferredWidth(300);
        for (int column = 1; column < permissionTable.getColumnCount(); column++) {
            permissionTable.getColumnModel().getColumn(column).setPreferredWidth(75);
        }
    }

    // Gán màu sắc và kích thước thống nhất cho nút thao tác.
    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setForeground(INK);
        button.setBackground(primary ? ACCENT : Color.WHITE);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(primary ? ACCENT : LINE),
                new EmptyBorder(9, 12, 9, 12)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Tải ma trận quyền minh họa; quyền chỉnh sửa chỉ tồn tại trong phiên giao diện.
    private void loadRolePermissions() {
        if (permissionTable.isEditing()) {
            permissionTable.getCellEditor().stopCellEditing();
        }
        permissionModel.setRowCount(0);
        String role = (String) roleSelector.getSelectedItem();
        for (Object[] permission : PERMISSIONS) {
            boolean[] roleRights = rightsFor(role, (String) permission[0]);
            permissionModel.addRow(new Object[] {
                    permission[0], roleRights[0], roleRights[1], roleRights[2], roleRights[3]
            });
        }
    }

    // Cung cấp mẫu quyền theo vai trò để người dùng xem cấu trúc RBAC.
    private boolean[] rightsFor(String role, String function) {
        if ("Quản trị viên".equals(role)) {
            return new boolean[] { true, true, true, true };
        }
        if ("Bác sĩ".equals(role)) {
            if ("Hồ sơ bệnh án EMR".equals(function) || "Kê đơn và phác đồ".equals(function)) {
                return new boolean[] { true, true, true, false };
            }
            if ("Hồ sơ bệnh nhân".equals(function) || "Lịch hẹn và tiếp đón".equals(function)
                    || "Báo cáo và thống kê".equals(function)) {
                return new boolean[] { true, false, false, false };
            }
        } else if ("Y tá".equals(role)) {
            if ("Hồ sơ bệnh nhân".equals(function) || "Lịch hẹn và tiếp đón".equals(function)) {
                return new boolean[] { true, true, true, false };
            }
            if ("Hồ sơ bệnh án EMR".equals(function) || "Thuốc và vật tư".equals(function)) {
                return new boolean[] { true, false, false, false };
            }
        } else if ("Lễ tân".equals(role)) {
            if ("Hồ sơ bệnh nhân".equals(function) || "Lịch hẹn và tiếp đón".equals(function)
                    || "Hóa đơn và công nợ".equals(function)) {
                return new boolean[] { true, true, true, false };
            }
            if ("Báo cáo và thống kê".equals(function)) {
                return new boolean[] { true, false, false, false };
            }
        } else if ("Nhân viên kho".equals(role)) {
            if ("Thuốc và vật tư".equals(function)) {
                return new boolean[] { true, true, true, false };
            }
            if ("Hóa đơn và công nợ".equals(function)) {
                return new boolean[] { true, false, false, false };
            }
        }
        return new boolean[] { false, false, false, false };
    }

    // Không báo lưu thành công khi ứng dụng chưa có dịch vụ lưu role/permission.
    private void showSaveNotice() {
        if (permissionTable.isEditing()) {
            permissionTable.getCellEditor().stopCellEditing();
        }
        JOptionPane.showMessageDialog(
                this,
                "Ma trận quyền đang là dữ liệu mẫu. Thay đổi chưa được lưu và sẽ mất khi rời màn hình.",
                "Chưa kết nối lưu phân quyền",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
