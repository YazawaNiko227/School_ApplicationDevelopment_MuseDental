package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Phác đồ điều trị và dự toán chi phí" (bước 6 trong luồng khám bệnh
 * chính).
 *
 * Luồng xử lý:
 * 1. Chọn lần khám (ChanDoan nháp) đang khám ở ô trên cùng.
 * 2. Nhập tên phác đồ, thời gian dự kiến, trạng thái (bảng KeHoachDieuTri).
 * 3. Chọn dịch vụ ở danh mục bên trái, nhập số lượng, bấm "Thêm vào phác đồ"
 * (bảng KeHoachDieuTri_DichVu). Tổng dự toán = tổng (đơn giá x số lượng).
 * 4. Bấm "Lưu phác đồ". Mỗi lần khám chỉ có tối đa 1 phác đồ
 * (UQ_KeHoachDieuTri_ChanDoan), lưu lại lần nữa là cập nhật.
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class CapNhatPhacDoDieuTriVaDuToanChiPhiPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color RED = new Color(200, 70, 70);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");

    // Trạng thái phác đồ (DB lưu không dấu: 'Dang thuc hien', 'Hoan thanh').
    private static final String DANG_THUC_HIEN = "Đang thực hiện";
    private static final String HOAN_THANH = "Hoàn thành";

    // ----- Dữ liệu mẫu -----
    // TODO DAO: ChanDoanDAO.layDanhSachDangKham() thay cho dữ liệu này.
    // {MaChanDoan, MaBenhNhan, HoTen, KetLuanChanDoan}
    private static final String[][] MOCK_LAN_KHAM = {
            { "CD005", "BN003", "Đặng Quốc Khánh", "Viêm tủy răng số 46" },
            { "CD006", "BN002", "Nguyễn Thị Lan", "Mẻ răng cửa số 11, cần bọc sứ" },
            { "CD007", "BN004", "Trần Bảo Ngọc", "Sâu răng sữa số 55 mức độ nhẹ" }
    };

    // TODO DAO: DichVuDAO.layTatCa() thay cho dữ liệu này.
    // {MaDichVu, TenDichVu, DonGia, ThoiGianBaoHanh (tháng)}
    private static final Object[][] MOCK_DICH_VU = {
            { "DV001", "Lấy cao răng", 200000.0, 0 },
            { "DV002", "Trám răng composite", 300000.0, 6 },
            { "DV003", "Nhổ răng khôn", 1500000.0, 0 },
            { "DV004", "Bọc răng sứ", 3500000.0, 60 },
            { "DV005", "Điều trị tủy răng", 1200000.0, 12 },
            { "DV006", "Tẩy trắng răng", 2000000.0, 12 }
    };

    /**
     * Một phác đồ (KeHoachDieuTri) kèm danh sách dịch vụ (maDichVu -> số lượng).
     */
    private static final class KeHoach {
        String ma;
        String ten;
        int thoiGianDuKien;
        String trangThai;
        final Map<String, Integer> dichVu = new LinkedHashMap<>();
    }

    // maChanDoan -> phác đồ đã lưu (mỗi lần khám tối đa 1 phác đồ).
    private final Map<String, KeHoach> keHoachTheoChanDoan = new HashMap<>();
    // Danh sách dịch vụ đang chọn trên màn hình (chưa lưu): maDichVu -> số lượng.
    private final Map<String, Integer> dichVuDangChon = new LinkedHashMap<>();
    private int nextKeHoach = 5;

    // ----- Chọn lần khám -----
    private final JComboBox<String> cboLanKham = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblKetLuan = new JLabel(" ");
    private final JLabel lblCoPhacDo = new JLabel(" ");

    // ----- Danh mục dịch vụ -----
    private final JTextField txtTimDichVu = new JTextField();
    private final DefaultTableModel dichVuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "Bảo hành (tháng)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dichVuTable = new JTable(dichVuModel);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));

    // ----- Phác đồ -----
    private final JTextField txtTenPhacDo = new JTextField();
    private final JSpinner spnThoiGian = new JSpinner(new SpinnerNumberModel(7, 1, 365, 1));
    private final JComboBox<String> cboTrangThai = new JComboBox<>(new String[] { DANG_THUC_HIEN, HOAN_THANH });
    private final DefaultTableModel chonModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable chonTable = new JTable(chonModel);
    private final JLabel lblTongTien = new JLabel("0 đ");

    public CapNhatPhacDoDieuTriVaDuToanChiPhiPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        taoDuLieuMau();

        add(createLanKhamCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(createDichVuCard(), BorderLayout.WEST);
        center.add(createPhacDoCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadDichVu();
        for (String[] lk : MOCK_LAN_KHAM) {
            cboLanKham.addItem(lk[0] + " - " + lk[2] + " (" + lk[1] + ")");
        }
        // Gắn listener sau khi đã thêm item để không bị kích hoạt sớm.
        cboLanKham.addActionListener(event -> chonLanKham());
        chonLanKham();
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        // Lần khám CD007 đã có sẵn phác đồ để thử chức năng sửa.
        KeHoach kh = new KeHoach();
        kh.ma = "KH004";
        kh.ten = "Trám răng sữa 55";
        kh.thoiGianDuKien = 7;
        kh.trangThai = DANG_THUC_HIEN;
        kh.dichVu.put("DV002", 1);
        keHoachTheoChanDoan.put("CD007", kh);
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createLanKhamCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Lần khám cần lập phác đồ"));
        card.add(Box.createVerticalStrut(10));

        cboLanKham.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(createRow("Lần khám (nháp)", cboLanKham));

        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblKetLuan.setFont(new Font("Arial", Font.PLAIN, 12));
        lblKetLuan.setForeground(INK);
        lblCoPhacDo.setFont(new Font("Arial", Font.ITALIC, 11));
        lblCoPhacDo.setForeground(MUTED);
        for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan, lblCoPhacDo }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }
        return card;
    }

    // Danh mục dịch vụ bên trái.
    private JPanel createDichVuCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(470, 0));

        // Tiêu đề + ô tìm kiếm.
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createCardTitle("Danh mục dịch vụ"));
        top.add(Box.createVerticalStrut(8));

        styleField(txtTimDichVu);
        txtTimDichVu.setToolTipText("Nhập mã hoặc tên dịch vụ");
        txtTimDichVu.addActionListener(event -> loadDichVu());
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> loadDichVu());
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTimDichVu, BorderLayout.CENTER);
        searchRow.add(btnTim, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        top.add(searchRow);
        card.add(top, BorderLayout.NORTH);

        styleTable(dichVuTable);
        dichVuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        dichVuTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        dichVuTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dichVuTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        JScrollPane scroll = new JScrollPane(dichVuTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Số lượng + nút thêm.
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        spnSoLuong.setFont(new Font("Arial", Font.PLAIN, 12));
        spnSoLuong.setPreferredSize(new Dimension(70, 32));
        JButton btnThem = createButton("Thêm vào phác đồ", true);
        btnThem.addActionListener(event -> themDichVu());
        actions.add(createSmallLabel("Số lượng:"));
        actions.add(spnSoLuong);
        actions.add(btnThem);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // Phác đồ + dịch vụ đã chọn + dự toán bên phải.
    private JPanel createPhacDoCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        // Phần form phác đồ.
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.add(createCardTitle("Phác đồ điều trị"));
        form.add(Box.createVerticalStrut(10));
        form.add(createRow("Tên phác đồ *", txtTenPhacDo));
        form.add(createRow("Thời gian (ngày)", spnThoiGian));
        form.add(createRow("Trạng thái", cboTrangThai));
        JLabel dsTitle = createSmallLabel("Dịch vụ trong phác đồ");
        dsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(dsTitle);
        card.add(form, BorderLayout.NORTH);

        // Bảng dịch vụ đã chọn.
        styleTable(chonTable);
        chonTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        chonTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        chonTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        chonTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        chonTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        JScrollPane scroll = new JScrollPane(chonTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        // Tổng dự toán + nút thao tác.
        JPanel bottom = new JPanel(new BorderLayout(0, 10));
        bottom.setOpaque(false);

        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        JLabel totalCaption = new JLabel("TỔNG DỰ TOÁN CHI PHÍ");
        totalCaption.setFont(new Font("Arial", Font.BOLD, 11));
        totalCaption.setForeground(MUTED);
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 20));
        lblTongTien.setForeground(TEAL);
        totalRow.add(totalCaption, BorderLayout.WEST);
        totalRow.add(lblTongTien, BorderLayout.EAST);
        bottom.add(totalRow, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnXoa = createButton("Xóa dịch vụ", false);
        JButton btnLamMoi = createButton("Làm mới", false);
        JButton btnLuu = createButton("Lưu phác đồ", true);
        btnXoa.addActionListener(event -> xoaDichVu());
        btnLamMoi.addActionListener(event -> chonLanKham());
        btnLuu.addActionListener(event -> luuPhacDo());
        actions.add(btnXoa);
        actions.add(btnLamMoi);
        actions.add(btnLuu);
        bottom.add(actions, BorderLayout.SOUTH);
        card.add(bottom, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Chọn lần khám: nạp phác đồ đã lưu (nếu có) hoặc xóa trắng form. */
    private void chonLanKham() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String[] lanKham = MOCK_LAN_KHAM[index];
        lblBenhNhan.setText("Bệnh nhân: " + lanKham[2] + "  ·  " + lanKham[1]);
        lblKetLuan.setText("Kết luận chẩn đoán: " + lanKham[3]);

        dichVuDangChon.clear();
        // TODO DAO: KeHoachDieuTriDAO.timTheoChanDoan(maChanDoan) + lấy
        // KeHoachDieuTri_DichVu
        KeHoach kh = keHoachTheoChanDoan.get(lanKham[0]);
        if (kh == null) {
            lblCoPhacDo.setText("Lần khám này chưa có phác đồ. Lưu sẽ tạo phác đồ mới.");
            txtTenPhacDo.setText("");
            spnThoiGian.setValue(7);
            cboTrangThai.setSelectedItem(DANG_THUC_HIEN);
        } else {
            lblCoPhacDo.setText("Đã có phác đồ " + kh.ma + ". Lưu sẽ cập nhật phác đồ này.");
            txtTenPhacDo.setText(kh.ten);
            spnThoiGian.setValue(kh.thoiGianDuKien);
            cboTrangThai.setSelectedItem(kh.trangThai);
            dichVuDangChon.putAll(kh.dichVu);
        }
        loadDichVuDaChon();
    }

    /** Thêm dịch vụ đang chọn ở danh mục vào phác đồ (cộng dồn nếu đã có). */
    private void themDichVu() {
        int row = dichVuTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một dịch vụ trong danh mục.");
            return;
        }
        String maDichVu = (String) dichVuModel.getValueAt(row, 0);
        int soLuong = (Integer) spnSoLuong.getValue();
        dichVuDangChon.merge(maDichVu, soLuong, Integer::sum);
        spnSoLuong.setValue(1);
        loadDichVuDaChon();
    }

    /** Bỏ dịch vụ đang chọn ra khỏi phác đồ. */
    private void xoaDichVu() {
        int row = chonTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một dịch vụ trong phác đồ để xóa.");
            return;
        }
        dichVuDangChon.remove((String) chonModel.getValueAt(row, 0));
        loadDichVuDaChon();
    }

    /** Kiểm tra dữ liệu rồi lưu phác đồ cho lần khám đang chọn. */
    private void luuPhacDo() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0) {
            showMessage("Vui lòng chọn lần khám.");
            return;
        }
        String ten = txtTenPhacDo.getText().trim();
        if (ten.isEmpty()) {
            showWarning("Tên phác đồ không được để trống.");
            txtTenPhacDo.requestFocusInWindow();
            return;
        }
        if (ten.length() > 150) {
            showWarning("Tên phác đồ tối đa 150 ký tự.");
            txtTenPhacDo.requestFocusInWindow();
            return;
        }
        if (dichVuDangChon.isEmpty()) {
            showWarning("Phác đồ cần có ít nhất một dịch vụ.");
            return;
        }

        String maChanDoan = MOCK_LAN_KHAM[index][0];
        KeHoach kh = keHoachTheoChanDoan.get(maChanDoan);
        boolean taoMoi = kh == null;
        if (taoMoi) {
            kh = new KeHoach();
            // TODO DAO: sinh mã từ DB.
            kh.ma = String.format("KH%03d", nextKeHoach++);
        }
        kh.ten = ten;
        kh.thoiGianDuKien = (Integer) spnThoiGian.getValue();
        kh.trangThai = (String) cboTrangThai.getSelectedItem();
        kh.dichVu.clear();
        kh.dichVu.putAll(dichVuDangChon);

        // TODO DAO (một transaction):
        // 1. Tạo mới: KeHoachDieuTriDAO.them(maKeHoach, ten, thoiGian, trangThai,
        // maChanDoan, maBenhNhan)
        // Cập nhật: KeHoachDieuTriDAO.capNhat(...)
        // 2. KeHoachDieuTri_DichVuDAO.xoaTheoKeHoach(maKeHoach) rồi them lại từng dòng
        // (maKeHoach, maDichVu, soLuong)
        keHoachTheoChanDoan.put(maChanDoan, kh);

        lblCoPhacDo.setText("Đã có phác đồ " + kh.ma + ". Lưu sẽ cập nhật phác đồ này.");
        showMessage((taoMoi ? "Đã tạo phác đồ " : "Đã cập nhật phác đồ ") + kh.ma
                + ". Tổng dự toán: " + lblTongTien.getText() + ".");
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Nạp danh mục dịch vụ, lọc theo từ khóa (mã hoặc tên).
    private void loadDichVu() {
        String keyword = txtTimDichVu.getText().trim().toLowerCase();
        dichVuModel.setRowCount(0);
        // TODO DAO: DichVuDAO.timKiem(keyword)
        for (Object[] dv : MOCK_DICH_VU) {
            String ma = ((String) dv[0]).toLowerCase();
            String ten = ((String) dv[1]).toLowerCase();
            if (keyword.isEmpty() || ma.contains(keyword) || ten.contains(keyword)) {
                dichVuModel.addRow(new Object[] { dv[0], dv[1], TIEN.format((Double) dv[2]), dv[3] });
            }
        }
    }

    // Nạp lại bảng dịch vụ đã chọn và tính tổng dự toán.
    private void loadDichVuDaChon() {
        chonModel.setRowCount(0);
        double tong = 0;
        for (Map.Entry<String, Integer> entry : dichVuDangChon.entrySet()) {
            Object[] dv = timDichVu(entry.getKey());
            if (dv == null) {
                continue;
            }
            double donGia = (Double) dv[2];
            int soLuong = entry.getValue();
            double thanhTien = donGia * soLuong;
            tong += thanhTien;
            chonModel.addRow(new Object[] {
                    dv[0], dv[1], TIEN.format(donGia), soLuong, TIEN.format(thanhTien) });
        }
        lblTongTien.setText(TIEN.format(tong) + " đ");
    }

    private Object[] timDichVu(String maDichVu) {
        for (Object[] dv : MOCK_DICH_VU) {
            if (dv[0].equals(maDichVu)) {
                return dv;
            }
        }
        return null;
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    // ----- Tạo thành phần giao diện -----

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        return card;
    }

    private JLabel createCardTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 15));
        label.setForeground(INK);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createSmallLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(MUTED);
        return label;
    }

    /**
     * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
     * còn lại.
     */
    private JPanel createRow(String labelText, Component field) {
        if (field instanceof JTextField) {
            styleField((JTextField) field);
        } else {
            field.setFont(new Font("Arial", Font.PLAIN, 12));
        }
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        label.setPreferredSize(new Dimension(120, 32));

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setBorder(new EmptyBorder(0, 0, 8, 0));
        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        return row;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setPreferredSize(new Dimension(160, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);
    }

    private JButton createButton(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);
        if (primary) {
            button.setBackground(TEAL);
            button.setForeground(Color.WHITE);
            button.setBorder(new EmptyBorder(8, 16, 8, 16));
            button.setBorderPainted(false);
        } else {
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE), new EmptyBorder(7, 15, 7, 15)));
        }
        return button;
    }
}