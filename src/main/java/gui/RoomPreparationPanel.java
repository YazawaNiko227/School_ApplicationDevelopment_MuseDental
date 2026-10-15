package gui;

import entity.ChiTietXuatKho;
import entity.PhieuXuatKho;
import entity.PhongKham;
import entity.VatTu;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;


public final class RoomPreparationPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color DANGER = new Color(192, 57, 43);
    private static final Color WARNING = new Color(177, 125, 47);
    private static final Color SELECTED_BG = new Color(231, 246, 245);

    // Mã nhân viên lập phiếu xuất; khi có đăng nhập thật thì truyền vào từ MainFrame.
    private static final String DEFAULT_NHAN_VIEN = "NV001";

    private static final String[] STATUS_LABELS = {
            "Sẵn sàng", "Đang chuẩn bị", "Đang sử dụng", "Đang vệ sinh", "Bảo trì"
    };

    // Dữ liệu
    private final List<PhongKham> rooms = new ArrayList<>();
    private final List<VatTu> supplies = new ArrayList<>();
    private final List<PhieuXuatKho> issueSlips = new ArrayList<>();
    private final List<ChiTietXuatKho> issueDetails = new ArrayList<>();
    private int slipCounter = 1;

    // Thành phần giao diện cần cập nhật
    private final DefaultListModel<PhongKham> roomModel = new DefaultListModel<>();
    private final JList<PhongKham> roomList = new JList<>(roomModel);
    private final JLabel roomNameLabel = new JLabel("Chưa chọn phòng");
    private final JLabel roomStatusBadge = new JLabel("", SwingConstants.CENTER);
    private final JLabel lastPrepLabel = new JLabel(" ");
    private final JComboBox<String> statusCombo = new JComboBox<>(STATUS_LABELS);
    private final JLabel readyValue = new JLabel("0");
    private final JLabel inUseValue = new JLabel("0");
    private final JLabel attentionValue = new JLabel("0");

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[] { "Chuẩn bị", "Mã vật tư", "Tên dụng cụ", "ĐVT", "Tồn kho", "Số lượng", "Cảnh báo" }, 0) {
        @Override
        public Class<?> getColumnClass(int column) {
            if (column == 0) {
                return Boolean.class;
            }
            if (column == 4 || column == 5) {
                return Integer.class;
            }
            return String.class;
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return column == 0 || column == 5;
        }
    };

    private final JTable table = new JTable(tableModel) {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component component = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row) && row < supplies.size()) {
                VatTu supply = supplies.get(row);
                boolean bad = supply.isHetHan() || supply.isSapHetHang();
                component.setForeground(bad ? DANGER : INK);
                component.setBackground(Color.WHITE);
            }
            return component;
        }
    };

    public RoomPreparationPanel() {
        setLayout(new BorderLayout(20, 0));
        setOpaque(false);
        loadSampleData();
        add(createRoomListCard(), BorderLayout.WEST);
        add(createRightSide(), BorderLayout.CENTER);

        for (PhongKham room : rooms) {
            roomModel.addElement(room);
        }
        refreshStats();
        if (!roomModel.isEmpty()) {
            roomList.setSelectedIndex(0);
        }
    }

    // ------------------------------------------------------------------
    // Dữ liệu minh họa
    // ------------------------------------------------------------------

    private void loadSampleData() {
        rooms.add(new PhongKham("P01", "Phòng khám 1", PhongKham.SAN_SANG));
        rooms.add(new PhongKham("P02", "Phòng khám 2", PhongKham.DANG_SU_DUNG));
        rooms.add(new PhongKham("P03", "Phòng khám 3", PhongKham.DANG_VE_SINH));
        rooms.add(new PhongKham("P04", "Phòng phẫu thuật", PhongKham.DANG_CHUAN_BI));
        rooms.add(new PhongKham("P05", "Phòng X-quang", PhongKham.BAO_TRI));

        LocalDate today = LocalDate.now();
        supplies.add(new VatTu("VT001", "Găng tay y tế", "Vật tư", "Hộp", 40, 10, today.plusMonths(14)));
        supplies.add(new VatTu("VT002", "Khẩu trang y tế", "Vật tư", "Hộp", 25, 10, today.plusMonths(10)));
        supplies.add(new VatTu("VT003", "Gương nha khoa", "Dụng cụ", "Cái", 30, 8, null));
        supplies.add(new VatTu("VT004", "Thám trâm", "Dụng cụ", "Cái", 18, 6, null));
        supplies.add(new VatTu("VT005", "Kẹp gắp", "Dụng cụ", "Cái", 5, 6, null));
        supplies.add(new VatTu("VT006", "Ống hút nước bọt", "Vật tư", "Gói", 60, 20, today.plusMonths(8)));
        supplies.add(new VatTu("VT007", "Thuốc tê Lidocaine", "Thuốc", "Ống", 22, 10, today.plusDays(20)));
        supplies.add(new VatTu("VT008", "Chất trám composite", "Vật tư", "Tuýp", 9, 4, today.minusDays(5)));
    }

    // ------------------------------------------------------------------
    // Dựng giao diện
    // ------------------------------------------------------------------

    private JPanel createRoomListCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LINE));
        card.setPreferredSize(new Dimension(300, 0));

        JLabel title = new JLabel("Danh sách phòng");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setBorder(new EmptyBorder(18, 18, 14, 18));
        card.add(title, BorderLayout.NORTH);

        roomList.setCellRenderer(new RoomRenderer());
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setFixedCellHeight(62);
        roomList.setBorder(null);
        roomList.addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                onRoomSelected();
            }
        });

        JScrollPane scroll = new JScrollPane(roomList);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINE));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private JPanel createRightSide() {
        JPanel right = new JPanel(new BorderLayout(0, 16));
        right.setOpaque(false);
        right.add(createStatsRow(), BorderLayout.NORTH);
        right.add(createDetailCard(), BorderLayout.CENTER);
        return right;
    }

    /** Hàng thống kê nhanh: dùng BoxLayout ngang, các thẻ có cùng kích thước ưu tiên. */
    private JPanel createStatsRow() {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.add(createStatCard("PHÒNG SẴN SÀNG", readyValue, TEAL));
        row.add(Box.createHorizontalStrut(14));
        row.add(createStatCard("ĐANG SỬ DỤNG", inUseValue, WARNING));
        row.add(Box.createHorizontalStrut(14));
        row.add(createStatCard("CẦN XỬ LÝ", attentionValue, DANGER));
        return row;
    }

    private JPanel createStatCard(String title, JLabel value, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 6));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 14, 16)));
        card.setPreferredSize(new Dimension(200, 78));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);
        value.setFont(new Font("Arial", Font.BOLD, 24));
        value.setForeground(color);
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel createDetailCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createLineBorder(LINE));
        card.add(createRoomHeader(), BorderLayout.NORTH);
        card.add(createSupplyTable(), BorderLayout.CENTER);
        card.add(createActionBar(), BorderLayout.SOUTH);
        return card;
    }

    /** Tên phòng, huy hiệu trạng thái bên trái; chọn và cập nhật trạng thái bên phải. */
    private JPanel createRoomHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(16, 20, 16, 20)));

        roomNameLabel.setFont(new Font("Arial", Font.BOLD, 17));
        roomNameLabel.setForeground(INK);
        roomNameLabel.setAlignmentX(LEFT_ALIGNMENT);

        roomStatusBadge.setFont(new Font("Arial", Font.BOLD, 10));
        roomStatusBadge.setOpaque(true);
        roomStatusBadge.setBorder(new EmptyBorder(5, 10, 5, 10));

        JPanel nameLine = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        nameLine.setOpaque(false);
        nameLine.setAlignmentX(LEFT_ALIGNMENT);
        nameLine.add(roomNameLabel);
        nameLine.add(roomStatusBadge);

        lastPrepLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        lastPrepLabel.setForeground(MUTED);
        lastPrepLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        lastPrepLabel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.add(nameLine);
        left.add(lastPrepLabel);
        header.add(left, BorderLayout.CENTER);

        statusCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        statusCombo.setPreferredSize(new Dimension(160, 34));
        JButton updateStatus = createButton("Cập nhật trạng thái", false);
        updateStatus.addActionListener(event -> updateRoomStatus());

        JPanel statusControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusControls.setOpaque(false);
        statusControls.add(new JLabel("Đổi sang:"));
        statusControls.add(statusCombo);
        statusControls.add(updateStatus);
        header.add(statusControls, BorderLayout.EAST);
        return header;
    }

    private JScrollPane createSupplyTable() {
        table.setRowHeight(34);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setGridColor(LINE);
        table.setShowVerticalLines(false);
        table.setSelectionBackground(SELECTED_BG);
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
        table.setFillsViewportHeight(true);

        int[] widths = { 70, 90, 220, 60, 80, 80, 130 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    private JPanel createActionBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LINE),
                new EmptyBorder(12, 16, 12, 16)));

        JPanel leftButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftButtons.setOpaque(false);
        JButton selectAll = createButton("Chọn tất cả", false);
        JButton clearAll = createButton("Bỏ chọn", false);
        selectAll.addActionListener(event -> setAllChecked(true));
        clearAll.addActionListener(event -> setAllChecked(false));
        leftButtons.add(selectAll);
        leftButtons.add(clearAll);

        JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightButtons.setOpaque(false);
        JButton confirm = createButton("Xác nhận chuẩn bị dụng cụ", true);
        confirm.addActionListener(event -> confirmPreparation());
        rightButtons.add(confirm);

        bar.add(leftButtons, BorderLayout.WEST);
        bar.add(rightButtons, BorderLayout.EAST);
        return bar;
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(button.getPreferredSize().width + 16, 34));
        if (primary) {
            button.setBackground(TEAL);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setOpaque(true);
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE),
                    new EmptyBorder(6, 14, 6, 14)));
        }
        return button;
    }

    // ------------------------------------------------------------------
    // Xử lý nghiệp vụ
    // ------------------------------------------------------------------

    private PhongKham getSelectedRoom() {
        return roomList.getSelectedValue();
    }

    private void onRoomSelected() {
        PhongKham room = getSelectedRoom();
        if (room == null) {
            roomNameLabel.setText("Chưa chọn phòng");
            roomStatusBadge.setText("");
            roomStatusBadge.setOpaque(false);
            return;
        }
        roomNameLabel.setText(room.getMaPhong() + " · " + room.getTenPhong());
        applyBadge(roomStatusBadge, room.getTrangThai());
        statusCombo.setSelectedIndex(indexOfStatus(room.getTrangThai()));
        lastPrepLabel.setText(" ");
        loadSupplyTable();
    }

    private void loadSupplyTable() {
        if (table.isEditing()) {
            table.getCellEditor().cancelCellEditing();
        }
        tableModel.setRowCount(0);
        for (VatTu supply : supplies) {
            tableModel.addRow(new Object[] {
                    Boolean.FALSE,
                    supply.getMaVatTu(),
                    supply.getTenVatTu(),
                    supply.getDonViTinh(),
                    supply.getSoLuongTon(),
                    1,
                    warningText(supply)
            });
        }
    }

    private String warningText(VatTu supply) {
        if (supply.isHetHan()) {
            return "Đã hết hạn";
        }
        if (supply.isSapHetHang()) {
            return "Sắp hết hàng";
        }
        if (supply.isSapHetHan()) {
            return "Sắp hết hạn";
        }
        return "Bình thường";
    }

    private void setAllChecked(boolean checked) {
        stopEditing();
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            // Vật tư đã hết hạn thì không cho chọn hàng loạt.
            boolean allowed = !supplies.get(row).isHetHan();
            tableModel.setValueAt(checked && allowed, row, 0);
        }
    }

    private void stopEditing() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }
    }

    private void updateRoomStatus() {
        PhongKham room = getSelectedRoom();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng.", "Chưa chọn phòng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String newStatus = PhongKham.CAC_TRANG_THAI[statusCombo.getSelectedIndex()];
        if (newStatus.equals(room.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Phòng đang ở trạng thái này rồi.", "Không thay đổi",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Đổi trạng thái " + room.getTenPhong() + " sang \"" + STATUS_LABELS[statusCombo.getSelectedIndex()]
                        + "\"?",
                "Xác nhận cập nhật", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }
        room.setTrangThai(newStatus);
        applyBadge(roomStatusBadge, newStatus);
        roomList.repaint();
        refreshStats();
    }

    /**
     * Kiểm tra dữ liệu, tạo PhieuXuatKho + ChiTietXuatKho, trừ tồn kho,
     * rồi chuyển phòng sang "Sẵn sàng".
     */
    private void confirmPreparation() {
        stopEditing();
        PhongKham room = getSelectedRoom();
        if (room == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng.", "Chưa chọn phòng",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (PhongKham.BAO_TRI.equals(room.getTrangThai()) || PhongKham.DANG_SU_DUNG.equals(room.getTrangThai())) {
            JOptionPane.showMessageDialog(this,
                    "Không thể chuẩn bị dụng cụ khi phòng đang bảo trì hoặc đang sử dụng.",
                    "Phòng chưa thể chuẩn bị", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<ChiTietXuatKho> pending = new ArrayList<>();
        String slipId = String.format("PX%05d", slipCounter);
        for (int row = 0; row < tableModel.getRowCount(); row++) {
            if (!Boolean.TRUE.equals(tableModel.getValueAt(row, 0))) {
                continue;
            }
            VatTu supply = supplies.get(row);
            int quantity = toInt(tableModel.getValueAt(row, 5));
            if (supply.isHetHan()) {
                showError("\"" + supply.getTenVatTu() + "\" đã hết hạn sử dụng, không thể xuất.");
                return;
            }
            if (quantity <= 0) {
                showError("Số lượng của \"" + supply.getTenVatTu() + "\" phải lớn hơn 0.");
                return;
            }
            if (quantity > supply.getSoLuongTon()) {
                showError("\"" + supply.getTenVatTu() + "\" chỉ còn " + supply.getSoLuongTon() + " "
                        + supply.getDonViTinh() + " trong kho.");
                return;
            }
            pending.add(new ChiTietXuatKho(slipId, supply.getMaVatTu(), quantity));
        }
        if (pending.isEmpty()) {
            showError("Vui lòng chọn ít nhất một dụng cụ cần chuẩn bị.");
            return;
        }

        slipCounter++;
        issueSlips.add(new PhieuXuatKho(slipId, LocalDate.now(), DEFAULT_NHAN_VIEN, null));
        for (ChiTietXuatKho detail : pending) {
            issueDetails.add(detail);
            for (VatTu supply : supplies) {
                if (supply.getMaVatTu().equals(detail.getMaVatTu())) {
                    supply.setSoLuongTon(supply.getSoLuongTon() - detail.getSoLuongSuDung());
                }
            }
        }

        room.setTrangThai(PhongKham.SAN_SANG);
        applyBadge(roomStatusBadge, room.getTrangThai());
        statusCombo.setSelectedIndex(indexOfStatus(room.getTrangThai()));
        lastPrepLabel.setText("Đã lập phiếu " + slipId + " lúc "
                + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")) + " · " + pending.size()
                + " loại dụng cụ");
        roomList.repaint();
        refreshStats();
        loadSupplyTable();
        JOptionPane.showMessageDialog(this,
                "Đã chuẩn bị " + pending.size() + " loại dụng cụ cho " + room.getTenPhong()
                        + ".\nPhòng được chuyển sang trạng thái Sẵn sàng.",
                "Chuẩn bị thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu chưa hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    private int toInt(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private void refreshStats() {
        int ready = 0;
        int inUse = 0;
        int attention = 0;
        for (PhongKham room : rooms) {
            String status = room.getTrangThai();
            if (PhongKham.SAN_SANG.equals(status)) {
                ready++;
            } else if (PhongKham.DANG_SU_DUNG.equals(status)) {
                inUse++;
            } else {
                attention++;
            }
        }
        readyValue.setText(String.valueOf(ready));
        inUseValue.setText(String.valueOf(inUse));
        attentionValue.setText(String.valueOf(attention));
    }

    // ------------------------------------------------------------------
    // Hỗ trợ hiển thị trạng thái
    // ------------------------------------------------------------------

    private static int indexOfStatus(String status) {
        for (int i = 0; i < PhongKham.CAC_TRANG_THAI.length; i++) {
            if (PhongKham.CAC_TRANG_THAI[i].equals(status)) {
                return i;
            }
        }
        return 0;
    }

    private static String statusLabel(String status) {
        return STATUS_LABELS[indexOfStatus(status)];
    }

    private static Color statusForeground(String status) {
        switch (status) {
            case PhongKham.SAN_SANG:
                return TEAL;
            case PhongKham.DANG_CHUAN_BI:
                return new Color(51, 91, 177);
            case PhongKham.DANG_SU_DUNG:
                return WARNING;
            case PhongKham.DANG_VE_SINH:
                return new Color(125, 86, 170);
            default:
                return DANGER;
        }
    }

    private static Color statusBackground(String status) {
        switch (status) {
            case PhongKham.SAN_SANG:
                return new Color(231, 246, 245);
            case PhongKham.DANG_CHUAN_BI:
                return new Color(237, 242, 255);
            case PhongKham.DANG_SU_DUNG:
                return new Color(255, 246, 232);
            case PhongKham.DANG_VE_SINH:
                return new Color(243, 236, 250);
            default:
                return new Color(253, 237, 244);
        }
    }

    private static void applyBadge(JLabel badge, String status) {
        badge.setText(statusLabel(status));
        badge.setForeground(statusForeground(status));
        badge.setBackground(statusBackground(status));
        badge.setOpaque(true);
    }

    /** Dòng phòng trong danh sách: tên + mã bên trái, huy hiệu trạng thái bên phải. */
    private static final class RoomRenderer extends JPanel implements ListCellRenderer<PhongKham> {
        private final JLabel nameLabel = new JLabel();
        private final JLabel codeLabel = new JLabel();
        private final JLabel statusLabel = new JLabel("", SwingConstants.CENTER);

        private RoomRenderer() {
            super(new BorderLayout(8, 0));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                    new EmptyBorder(0, 16, 0, 12)));

            nameLabel.setFont(new Font("Arial", Font.BOLD, 12));
            nameLabel.setForeground(INK);
            codeLabel.setFont(new Font("Arial", Font.PLAIN, 10));
            codeLabel.setForeground(MUTED);

            JPanel text = new JPanel();
            text.setOpaque(false);
            text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
            text.add(Box.createVerticalGlue());
            text.add(nameLabel);
            text.add(Box.createVerticalStrut(4));
            text.add(codeLabel);
            text.add(Box.createVerticalGlue());

            statusLabel.setFont(new Font("Arial", Font.BOLD, 9));
            statusLabel.setOpaque(true);
            statusLabel.setBorder(new EmptyBorder(4, 8, 4, 8));

            JPanel statusWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            statusWrap.setOpaque(false);
            statusWrap.add(statusLabel);

            add(text, BorderLayout.CENTER);
            add(statusWrap, BorderLayout.EAST);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends PhongKham> list, PhongKham room, int index,
                boolean isSelected, boolean cellHasFocus) {
            nameLabel.setText(room.getTenPhong());
            codeLabel.setText("Mã phòng: " + room.getMaPhong());
            applyBadge(statusLabel, room.getTrangThai());
            setBackground(isSelected ? SELECTED_BG : Color.WHITE);
            return this;
        }
    }
}
