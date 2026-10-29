package gui;

import dao.PhongKhamDAO;
import entity.PhongKham;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Màn hình "Chuẩn bị dụng cụ và cập nhật trạng thái phòng" (bước 3 và bước 8
 * trong luồng khám bệnh chính).
 *
 * Y tá chọn một phòng, tick danh sách dụng cụ đã chuẩn bị rồi đặt phòng về
 * "Sẵn sàng". Phòng sau khi khám xong ("Cần vệ sinh") cũng được đặt lại
 * "Sẵn sàng" ở đây.
 *
 * Dữ liệu phòng và trạng thái được đọc, cập nhật qua PhongKhamDAO.
 */
public class XuLyChuanBiPhongKhamPanel extends JPanel {
    // Màu dùng chung với ManHinhChinhFrame.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    // Trạng thái hiển thị; PhongKhamDAO lưu các giá trị không dấu theo schema.
    private static final String SAN_SANG = "Sẵn sàng";
    private static final String DANG_KHAM = "Đang khám";
    private static final String CAN_VE_SINH = "Cần vệ sinh";
    private static final String BAO_TRI = "Bảo trì";
    private static final String TAT_CA = "Tất cả";

    // Chỉ liệt kê dụng cụ tái sử dụng và việc chuẩn bị phòng.
    // Vật tư dùng một lần (găng tay, thuốc tê, vật liệu...) ghi nhận ở màn hình
    // "Ghi nhận vật tư tiêu hao" để trừ kho.
    private static final String[] DANH_SACH_DUNG_CU = {
            "Khay dụng cụ khám (gương, thám trâm, kẹp gắp)",
            "Dụng cụ đã được tiệt trùng",
            "Ống hút nước bọt, cốc súc miệng",
            "Ghế nha khoa và đèn đã kiểm tra",
            "Lau khử khuẩn bề mặt"
    };

    // Mỗi phần tử: {mã phòng, tên phòng, trạng thái, lịch hẹn kế tiếp, cập nhật
    // lúc}
    private final List<Object[]> danhSachPhong = new ArrayList<>();
    private final PhongKhamDAO phongKhamDAO = new PhongKhamDAO();

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Mã phòng", "Tên phòng", "Trạng thái", "Lịch hẹn kế tiếp", "Cập nhật lúc" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> filterBox = new JComboBox<>(
            new String[] { TAT_CA, SAN_SANG, DANG_KHAM, CAN_VE_SINH, BAO_TRI });

    // Bốn thẻ thống kê: Sẵn sàng, Đang khám, Cần vệ sinh, Bảo trì.
    private final JLabel[] summaryValues = new JLabel[4];

    // Phần chi tiết bên phải.
    private final JLabel lblMaPhong = new JLabel("-");
    private final JLabel lblTenPhong = new JLabel("-");
    private final JLabel lblTrangThai = new JLabel("-");
    private final JLabel lblLichKeTiep = new JLabel("-");
    private final JLabel lblGoiY = new JLabel(" ");
    private final JCheckBox[] checklist = new JCheckBox[DANH_SACH_DUNG_CU.length];
    private final JTextArea txtGhiChu = new JTextArea(3, 20);
    private final JButton btnXacNhan = new JButton("Xác nhận");
    private final JButton btnCanVeSinh = new JButton("Báo cần vệ sinh");
    private final JButton btnBaoTri = new JButton("Báo bảo trì");

    public XuLyChuanBiPhongKhamPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createRoomListPanel(), BorderLayout.CENTER);
        center.add(createDetailPanel(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        napDuLieu();
        loadTable();
        refreshSummary();
        capNhatChiTiet();
    }

    private void napDuLieu() {
        try {
            List<PhongKham> phongKhamList = phongKhamDAO.layTatCa();
            Map<String, String> lichHenKeTiep = phongKhamDAO.layLichHenKeTiep();
            danhSachPhong.clear();
            for (PhongKham phong : phongKhamList) {
                danhSachPhong.add(new Object[] {
                        phong.getMaPhong(),
                        phong.getTenPhong(),
                        trangThaiHienThi(phong.getTrangThai()),
                        lichHenKeTiep.getOrDefault(phong.getMaPhong(), "Không có"),
                        "-"
                });
            }
        } catch (SQLException ex) {
            showDatabaseError("Không thể tải danh sách phòng khám.", ex);
        }
    }

    private String trangThaiHienThi(String trangThai) {
        if ("San sang".equals(trangThai)) {
            return SAN_SANG;
        }
        if ("Dang kham".equals(trangThai)) {
            return DANG_KHAM;
        }
        if ("Can ve sinh".equals(trangThai)) {
            return CAN_VE_SINH;
        }
        if ("Bao tri".equals(trangThai)) {
            return BAO_TRI;
        }
        throw new IllegalArgumentException("Trạng thái phòng khám không hợp lệ: " + trangThai);
    }

    private String trangThaiTrongDb(String trangThai) {
        if (SAN_SANG.equals(trangThai)) {
            return "San sang";
        }
        if (DANG_KHAM.equals(trangThai)) {
            return "Dang kham";
        }
        if (CAN_VE_SINH.equals(trangThai)) {
            return "Can ve sinh";
        }
        if (BAO_TRI.equals(trangThai)) {
            return "Bao tri";
        }
        throw new IllegalArgumentException("Trạng thái phòng khám không hợp lệ: " + trangThai);
    }

    // ===== Thẻ thống kê =====
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard(0, "SẴN SÀNG", TEAL));
        panel.add(createSummaryCard(1, "ĐANG KHÁM", BLUE));
        panel.add(createSummaryCard(2, "CẦN VỆ SINH", ORANGE));
        panel.add(createSummaryCard(3, "BẢO TRÌ", RED));
        return panel;
    }

    private JPanel createSummaryCard(int index, String title, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(180, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        summaryValues[index] = new JLabel("0");
        summaryValues[index].setFont(new Font("Arial", Font.BOLD, 24));
        summaryValues[index].setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(summaryValues[index]);
        return card;
    }

    // ===== Danh sách phòng =====
    private JPanel createRoomListPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 18, 16, 18)));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Danh sách phòng khám");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Lọc trạng thái:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        filterLabel.setForeground(MUTED);
        filterBox.setFont(new Font("Arial", Font.PLAIN, 12));
        filterBox.addActionListener(event -> loadTable());
        filterPanel.add(filterLabel);
        filterPanel.add(filterBox);

        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);

        table.setRowHeight(34);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
        table.getColumnModel().getColumn(2).setCellRenderer(new StatusRenderer());
        table.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                capNhatChiTiet();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    // ===== Chi tiết phòng + danh sách dụng cụ =====
    private JPanel createDetailPanel() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Chuẩn bị phòng");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(12));

        content.add(createInfoRow("Mã phòng", lblMaPhong));
        content.add(createInfoRow("Tên phòng", lblTenPhong));
        content.add(createInfoRow("Trạng thái", lblTrangThai));
        content.add(createInfoRow("Lịch hẹn kế tiếp", lblLichKeTiep));
        content.add(Box.createVerticalStrut(8));

        lblGoiY.setFont(new Font("Arial", Font.ITALIC, 11));
        lblGoiY.setForeground(MUTED);
        lblGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblGoiY);
        content.add(Box.createVerticalStrut(12));

        JLabel checklistTitle = new JLabel("Dụng cụ đã chuẩn bị (tick mục cần dùng, không bắt buộc)");
        checklistTitle.setFont(new Font("Arial", Font.BOLD, 12));
        checklistTitle.setForeground(INK);
        checklistTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(checklistTitle);
        content.add(Box.createVerticalStrut(6));

        for (int i = 0; i < DANH_SACH_DUNG_CU.length; i++) {
            checklist[i] = new JCheckBox(DANH_SACH_DUNG_CU[i]);
            checklist[i].setFont(new Font("Arial", Font.PLAIN, 12));
            checklist[i].setForeground(INK);
            checklist[i].setOpaque(false);
            checklist[i].setFocusPainted(false);
            checklist[i].setAlignmentX(Component.LEFT_ALIGNMENT);
            content.add(checklist[i]);
        }
        content.add(Box.createVerticalStrut(12));

        JLabel noteTitle = new JLabel("Ghi chú");
        noteTitle.setFont(new Font("Arial", Font.BOLD, 12));
        noteTitle.setForeground(INK);
        noteTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(noteTitle);
        content.add(Box.createVerticalStrut(6));

        txtGhiChu.setFont(new Font("Arial", Font.PLAIN, 12));
        txtGhiChu.setLineWrap(true);
        txtGhiChu.setWrapStyleWord(true);
        JScrollPane noteScroll = new JScrollPane(txtGhiChu);
        noteScroll.setBorder(BorderFactory.createLineBorder(LINE));
        noteScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        noteScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        content.add(noteScroll);
        content.add(Box.createVerticalStrut(14));

        styleButton(btnXacNhan, TEAL, Color.WHITE);
        styleButton(btnCanVeSinh, Color.WHITE, ORANGE);
        btnCanVeSinh.setBorderPainted(true);
        btnCanVeSinh.setBorder(BorderFactory.createLineBorder(ORANGE));
        styleButton(btnBaoTri, Color.WHITE, RED);
        btnBaoTri.setBorderPainted(true);
        btnBaoTri.setBorder(BorderFactory.createLineBorder(RED));
        btnXacNhan.addActionListener(event -> xacNhanSanSang());
        btnCanVeSinh.addActionListener(event -> baoCanVeSinh());
        btnBaoTri.addActionListener(event -> baoBaoTri());
        content.add(btnXacNhan);
        content.add(Box.createVerticalStrut(8));
        content.add(btnCanVeSinh);
        content.add(Box.createVerticalStrut(8));
        content.add(btnBaoTri);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.setPreferredSize(new Dimension(380, 0));
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel createInfoRow(String caption, JLabel value) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        captionLabel.setForeground(MUTED);
        captionLabel.setPreferredSize(new Dimension(110, 22));

        value.setFont(new Font("Arial", Font.BOLD, 12));
        value.setForeground(INK);

        row.add(captionLabel, BorderLayout.WEST);
        row.add(value, BorderLayout.CENTER);
        return row;
    }

    private void styleButton(JButton button, Color background, Color foreground) {
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setBackground(background);
        button.setForeground(foreground);
        button.setFocusPainted(false);
        button.setOpaque(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.setPreferredSize(new Dimension(200, 38));
    }

    // ===== Xử lý giao diện =====

    // Nạp lại bảng theo bộ lọc trạng thái.
    private void loadTable() {
        String filter = (String) filterBox.getSelectedItem();
        tableModel.setRowCount(0);
        for (Object[] phong : danhSachPhong) {
            if (TAT_CA.equals(filter) || phong[2].equals(filter)) {
                tableModel.addRow(phong);
            }
        }
    }

    private void refreshSummary() {
        int[] dem = new int[4];
        for (Object[] phong : danhSachPhong) {
            String trangThai = (String) phong[2];
            if (SAN_SANG.equals(trangThai)) {
                dem[0]++;
            } else if (DANG_KHAM.equals(trangThai)) {
                dem[1]++;
            } else if (CAN_VE_SINH.equals(trangThai)) {
                dem[2]++;
            } else if (BAO_TRI.equals(trangThai)) {
                dem[3]++;
            }
        }
        for (int i = 0; i < dem.length; i++) {
            summaryValues[i].setText(String.valueOf(dem[i]));
        }
    }

    // Tìm phòng đang được chọn trong bảng, trả về null nếu chưa chọn.
    private Object[] layPhongDangChon() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String maPhong = (String) tableModel.getValueAt(row, 0);
        for (Object[] phong : danhSachPhong) {
            if (phong[0].equals(maPhong)) {
                return phong;
            }
        }
        return null;
    }

    // Cập nhật phần chi tiết theo phòng đang chọn.
    private void capNhatChiTiet() {
        Object[] phong = layPhongDangChon();

        for (JCheckBox box : checklist) {
            box.setSelected(false);
        }
        txtGhiChu.setText("");

        if (phong == null) {
            lblMaPhong.setText("-");
            lblTenPhong.setText("-");
            lblTrangThai.setText("-");
            lblLichKeTiep.setText("-");
            lblGoiY.setText("Chọn một phòng ở danh sách bên trái.");
            btnXacNhan.setText("Xác nhận");
            setChiTietEnabled(false, false, false);
            return;
        }

        String trangThai = (String) phong[2];
        lblMaPhong.setText((String) phong[0]);
        lblTenPhong.setText((String) phong[1]);
        lblTrangThai.setText(trangThai);
        lblLichKeTiep.setText((String) phong[3]);

        if (DANG_KHAM.equals(trangThai)) {
            lblGoiY.setText("Phòng đang có bệnh nhân, chưa thể chuẩn bị.");
            btnXacNhan.setText("Xác nhận chuẩn bị xong");
            setChiTietEnabled(false, false, false);
        } else if (CAN_VE_SINH.equals(trangThai)) {
            lblGoiY.setText("Dọn dẹp xong thì bấm nút bên dưới để đặt lại Sẵn sàng.");
            btnXacNhan.setText("Đã dọn xong - đặt Sẵn sàng");
            setChiTietEnabled(true, true, false);
        } else if (BAO_TRI.equals(trangThai)) {
            lblGoiY.setText("Phòng đang bảo trì. Hoàn tất bảo trì để đưa vào sử dụng.");
            btnXacNhan.setText("Hoàn tất bảo trì - đặt Sẵn sàng");
            setChiTietEnabled(true, false, false);
        } else {
            lblGoiY.setText("Chuẩn bị dụng cụ theo ca khám rồi xác nhận. Không cần tick đủ mọi mục.");
            btnXacNhan.setText("Xác nhận đã chuẩn bị dụng cụ");
            setChiTietEnabled(true, true, true);
        }
    }

    // Bật/tắt các thành phần ở phần chi tiết.
    private void setChiTietEnabled(boolean choPhepChuanBi, boolean choPhepBaoTri, boolean choPhepCanVeSinh) {
        for (JCheckBox box : checklist) {
            box.setEnabled(choPhepChuanBi);
        }
        txtGhiChu.setEnabled(choPhepChuanBi);
        btnXacNhan.setEnabled(choPhepChuanBi);
        btnBaoTri.setEnabled(choPhepBaoTri);
        btnCanVeSinh.setEnabled(choPhepCanVeSinh);
    }

    // Y tá xác nhận đã chuẩn bị xong: phòng về Sẵn sàng.
    private void xacNhanSanSang() {
        Object[] phong = layPhongDangChon();
        if (phong == null) {
            return;
        }
        if (datTrangThai(phong, SAN_SANG)) {
            JOptionPane.showMessageDialog(this,
                    "Phòng " + phong[0] + " đã chuyển sang trạng thái Sẵn sàng.",
                    "Thành công", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Báo phòng cần vệ sinh (ví dụ phòng bị bẩn trước khi dùng).
    private void baoCanVeSinh() {
        Object[] phong = layPhongDangChon();
        if (phong == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Chuyển phòng " + phong[0] + " sang trạng thái Cần vệ sinh?",
                "Xác nhận báo cần vệ sinh", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            datTrangThai(phong, CAN_VE_SINH);
        }
    }

    // Báo phòng cần bảo trì.
    private void baoBaoTri() {
        Object[] phong = layPhongDangChon();
        if (phong == null) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Chuyển phòng " + phong[0] + " sang trạng thái Bảo trì?",
                "Xác nhận báo bảo trì", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            datTrangThai(phong, BAO_TRI);
        }
    }

    // Đổi trạng thái phòng trong danh sách rồi làm mới giao diện.
    private boolean datTrangThai(Object[] phong, String trangThaiMoi) {
        String maPhong = (String) phong[0];
        try {
            if (!phongKhamDAO.capNhatTrangThai(maPhong, trangThaiTrongDb(trangThaiMoi))) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy phòng " + maPhong + " trong cơ sở dữ liệu.",
                        "Không thể cập nhật", JOptionPane.ERROR_MESSAGE);
                napDuLieu();
                loadTable();
                refreshSummary();
                capNhatChiTiet();
                return false;
            }
        } catch (SQLException ex) {
            showDatabaseError("Không thể cập nhật trạng thái phòng khám.", ex);
            return false;
        }
        phong[2] = trangThaiMoi;
        phong[4] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm dd/MM"));
        loadTable();
        refreshSummary();

        // Chọn lại đúng phòng vừa cập nhật nếu nó vẫn còn trong bảng (theo bộ lọc).
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (maPhong.equals(tableModel.getValueAt(i, 0))) {
                table.setRowSelectionInterval(i, i);
                break;
            }
        }
        capNhatChiTiet();
        return true;
    }

    private void showDatabaseError(String message, SQLException ex) {
        String detail = ex.getMessage();
        JOptionPane.showMessageDialog(this,
                detail == null || detail.trim().isEmpty() ? message : message + "\n" + detail,
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    // Tô màu chữ cho cột trạng thái.
    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (SAN_SANG.equals(text)) {
                label.setForeground(TEAL);
            } else if (DANG_KHAM.equals(text)) {
                label.setForeground(BLUE);
            } else if (CAN_VE_SINH.equals(text)) {
                label.setForeground(ORANGE);
            } else {
                label.setForeground(RED);
            }
            return label;
        }
    }
}