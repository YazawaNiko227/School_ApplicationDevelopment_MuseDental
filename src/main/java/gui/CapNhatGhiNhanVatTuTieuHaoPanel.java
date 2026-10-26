package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
 * Panel "Ghi nhận vật tư tiêu hao" (bước 7 trong luồng khám bệnh chính).
 *
 * Luồng xử lý:
 * 1. Chọn lịch hẹn đang khám / vừa khám xong (hoặc "Xuất dùng chung" nếu không
 * gắn với lịch hẹn nào, ứng với PhieuXuatKho.MaLichHen = NULL).
 * 2. Chọn vật tư trong danh mục, nhập số lượng, bấm "Thêm vào phiếu"
 * (bảng ChiTietXuatKho, khóa chính (MaPhieuXuat, MaVatTu) nên mỗi vật tư
 * chỉ có 1 dòng trong một phiếu).
 * 3. Bấm "Lưu phiếu xuất": tạo PhieuXuatKho + ChiTietXuatKho, trừ
 * VatTu.SoLuongTon. Một lịch hẹn có thể có nhiều phiếu xuất.
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class CapNhatGhiNhanVatTuTieuHaoPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final String TAT_CA = "Tất cả";
    private static final String KHONG_GAN_LICH = "Xuất dùng chung (không gắn lịch hẹn)";
    // Khóa dùng trong map lịch sử cho phiếu không gắn lịch hẹn.
    private static final String KHOA_DUNG_CHUNG = "-";

    // Chỉ số cột trong mỗi dòng vật tư (Object[]).
    private static final int V_MA = 0;
    private static final int V_TEN = 1;
    private static final int V_LOAI = 2;
    private static final int V_DVT = 3;
    private static final int V_TON = 4; // Integer
    private static final int V_NGUONG = 5; // Integer
    private static final int V_HSD = 6; // LocalDate hoặc null

    // ----- Dữ liệu mẫu -----
    // TODO DAO: LichHenDAO.layLichDangKhamVaVuaKham() thay cho dữ liệu này.
    // {MaLichHen, HoTen, MaBenhNhan, MaPhong, TrangThai}
    private static final String[][] MOCK_LICH_HEN = {
            { "LH003", "Đặng Quốc Khánh", "BN003", "P01", "Đang khám" },
            { "LH004", "Nguyễn Thị Lan", "BN002", "P02", "Đang khám" },
            { "LH005", "Trần Bảo Ngọc", "BN004", "P02", "Hoàn thành" }
    };

    // Kho vật tư (bản sao trong bộ nhớ để trừ tồn). TODO DAO: VatTuDAO.
    private final List<Object[]> kho = new ArrayList<>();
    // Lịch sử đã xuất: maLichHen -> các dòng {maPhieu, maVatTu, soLuong}.
    private final Map<String, List<Object[]>> daXuat = new HashMap<>();
    // Các dòng đang soạn trên màn hình (chưa lưu): maVatTu -> số lượng sử dụng.
    private final Map<String, Integer> dangSoan = new LinkedHashMap<>();
    private int nextPhieuXuat = 5;

    // ----- Chọn lịch hẹn -----
    private final JComboBox<String> cboLichHen = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");

    // ----- Danh mục vật tư -----
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboLoai = new JComboBox<>(
            new String[] { TAT_CA, "Thuốc", "Tiêu hao", "Vật liệu" });
    private final DefaultTableModel vatTuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên vật tư", "Loại", "ĐVT", "Tồn" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable vatTuTable = new JTable(vatTuModel);
    private final JSpinner spnSoLuong = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

    // ----- Phiếu xuất đang soạn + lịch sử -----
    private final DefaultTableModel phieuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên vật tư", "ĐVT", "Số lượng dùng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable phieuTable = new JTable(phieuModel);
    private final DefaultTableModel lichSuModel = new DefaultTableModel(
            new String[] { "Mã phiếu", "Mã VT", "Tên vật tư", "Số lượng" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lichSuTable = new JTable(lichSuModel);

    public CapNhatGhiNhanVatTuTieuHaoPanel() {
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        taoDuLieuMau();

        add(createLichHenCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(createVatTuCard(), BorderLayout.WEST);
        center.add(createPhieuCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        loadVatTu();
        cboLichHen.addItem(KHONG_GAN_LICH);
        for (String[] lh : MOCK_LICH_HEN) {
            cboLichHen.addItem(lh[0] + " - " + lh[1] + " (" + lh[4] + ", phòng " + lh[3] + ")");
        }
        // Gắn listener sau khi đã thêm item để không bị kích hoạt sớm.
        cboLichHen.addActionListener(event -> chonLichHen());
        chonLichHen();
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        // Khớp bảng VatTu trong SQL: {mã, tên, loại, ĐVT, tồn, ngưỡng tối thiểu, HSD}
        kho.add(new Object[] { "VT001", "Amoxicillin 500mg", "Thuốc", "Viên", 200, 50, LocalDate.of(2027, 6, 30) });
        kho.add(new Object[] { "VT002", "Paracetamol 500mg", "Thuốc", "Viên", 500, 100, LocalDate.of(2027, 12, 31) });
        kho.add(new Object[] { "VT003", "Găng tay y tế", "Tiêu hao", "Hộp", 40, 10, LocalDate.of(2028, 1, 1) });
        kho.add(new Object[] { "VT004", "Composite trám răng", "Vật liệu", "Tuýp", 25, 5, LocalDate.of(2027, 3, 15) });
        kho.add(new Object[] { "VT005", "Thuốc tê Lidocaine 2%", "Thuốc", "Ống", 60, 20, LocalDate.of(2027, 2, 28) });
        kho.add(new Object[] { "VT006", "Mão sứ (răng sứ)", "Vật liệu", "Cái", 15, 5, null });

        // Lịch hẹn LH005 đã có sẵn một phiếu xuất để xem lịch sử.
        List<Object[]> ds = new ArrayList<>();
        ds.add(new Object[] { "PX004", "VT003", 2 });
        daXuat.put("LH005", ds);
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createLichHenCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Lần khám cần ghi nhận vật tư"));
        card.add(Box.createVerticalStrut(10));

        cboLichHen.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(createRow("Lịch hẹn", cboLichHen));

        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblBenhNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblBenhNhan);
        card.add(Box.createVerticalStrut(4));

        JLabel lblNgay = new JLabel();
        lblNgay.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNgay.setForeground(MUTED);
        lblNgay.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblNgay.setText("Ngày xuất kho: " + LocalDate.now().format(NGAY)
                + "  ·  Người xuất: nhân viên đang đăng nhập");
        card.add(lblNgay);
        return card;
    }

    // Danh mục vật tư bên trái.
    private JPanel createVatTuCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(470, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createCardTitle("Danh mục vật tư"));
        top.add(Box.createVerticalStrut(8));

        styleField(txtTim);
        txtTim.setToolTipText("Nhập mã hoặc tên vật tư");
        txtTim.addActionListener(event -> loadVatTu());
        cboLoai.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLoai.addActionListener(event -> loadVatTu());
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> loadVatTu());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(cboLoai);
        buttons.add(btnTim);
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTim, BorderLayout.CENTER);
        searchRow.add(buttons, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        top.add(searchRow);
        card.add(top, BorderLayout.NORTH);

        styleTable(vatTuTable);
        vatTuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        vatTuTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        vatTuTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        vatTuTable.getColumnModel().getColumn(3).setPreferredWidth(45);
        vatTuTable.getColumnModel().getColumn(4).setPreferredWidth(45);
        JScrollPane scroll = new JScrollPane(vatTuTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        spnSoLuong.setFont(new Font("Arial", Font.PLAIN, 12));
        spnSoLuong.setPreferredSize(new Dimension(70, 32));
        JButton btnThem = createButton("Thêm vào phiếu", true);
        btnThem.addActionListener(event -> themVatTu());
        actions.add(createSmallLabel("Số lượng dùng:"));
        actions.add(spnSoLuong);
        actions.add(btnThem);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // Phiếu xuất đang soạn + lịch sử đã xuất bên phải.
    private JPanel createPhieuCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createCardTitle("Phiếu xuất đang soạn"));
        content.add(Box.createVerticalStrut(8));

        styleTable(phieuTable);
        phieuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        phieuTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        phieuTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        phieuTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        JScrollPane phieuScroll = new JScrollPane(phieuTable);
        phieuScroll.setBorder(BorderFactory.createLineBorder(LINE));
        phieuScroll.getViewport().setBackground(Color.WHITE);
        phieuScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(phieuScroll);
        content.add(Box.createVerticalStrut(10));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnXoa = createButton("Xóa dòng", false);
        JButton btnLamMoi = createButton("Làm mới", false);
        JButton btnLuu = createButton("Lưu phiếu xuất", true);
        btnXoa.addActionListener(event -> xoaVatTu());
        btnLamMoi.addActionListener(event -> chonLichHen());
        btnLuu.addActionListener(event -> luuPhieuXuat());
        actions.add(btnXoa);
        actions.add(btnLamMoi);
        actions.add(btnLuu);
        actions.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        content.add(actions);
        content.add(Box.createVerticalStrut(14));

        content.add(createCardTitle("Đã xuất cho lần khám này"));
        content.add(Box.createVerticalStrut(8));

        styleTable(lichSuTable);
        JScrollPane lichSuScroll = new JScrollPane(lichSuTable);
        lichSuScroll.setBorder(BorderFactory.createLineBorder(LINE));
        lichSuScroll.getViewport().setBackground(Color.WHITE);
        lichSuScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lichSuScroll);

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /**
     * Chọn lịch hẹn: hiện thông tin bệnh nhân, xóa phiếu đang soạn, nạp lịch sử.
     */
    private void chonLichHen() {
        int index = cboLichHen.getSelectedIndex();
        if (index < 0) {
            return;
        }
        if (index == 0) {
            lblBenhNhan.setText("Xuất dùng chung cho phòng khám, không gắn với bệnh nhân nào.");
        } else {
            String[] lh = MOCK_LICH_HEN[index - 1];
            lblBenhNhan.setText("Bệnh nhân: " + lh[1] + "  ·  " + lh[2] + "  ·  Phòng " + lh[3]);
        }
        dangSoan.clear();
        spnSoLuong.setValue(1);
        loadPhieu();
        loadLichSu();
    }

    /** Thêm vật tư đang chọn vào phiếu (cộng dồn nếu đã có trong phiếu). */
    private void themVatTu() {
        int row = vatTuTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một vật tư trong danh mục.");
            return;
        }
        Object[] vt = timVatTu((String) vatTuModel.getValueAt(row, 0));
        String maVatTu = (String) vt[V_MA];
        int soLuong = (Integer) spnSoLuong.getValue();
        int daCo = dangSoan.getOrDefault(maVatTu, 0);
        int ton = (Integer) vt[V_TON];

        if (daCo + soLuong > ton) {
            showWarning("Số lượng dùng (" + (daCo + soLuong) + ") vượt quá tồn kho (" + ton + ").");
            return;
        }
        LocalDate hsd = (LocalDate) vt[V_HSD];
        if (hsd != null && hsd.isBefore(LocalDate.now())) {
            showWarning("Vật tư " + vt[V_TEN] + " đã hết hạn sử dụng, không thể xuất.");
            return;
        }

        dangSoan.put(maVatTu, daCo + soLuong);
        spnSoLuong.setValue(1);
        loadPhieu();
    }

    /** Bỏ dòng vật tư đang chọn ra khỏi phiếu. */
    private void xoaVatTu() {
        int row = phieuTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn một dòng trong phiếu để xóa.");
            return;
        }
        dangSoan.remove((String) phieuModel.getValueAt(row, 0));
        loadPhieu();
    }

    /** Lưu phiếu xuất kho: ghi chi tiết và trừ tồn kho. */
    private void luuPhieuXuat() {
        int index = cboLichHen.getSelectedIndex();
        if (index < 0) {
            showMessage("Vui lòng chọn lịch hẹn.");
            return;
        }
        if (dangSoan.isEmpty()) {
            showWarning("Phiếu xuất cần có ít nhất một vật tư.");
            return;
        }

        // Kiểm tra lại tồn kho lần cuối trước khi trừ.
        for (Map.Entry<String, Integer> entry : dangSoan.entrySet()) {
            Object[] vt = timVatTu(entry.getKey());
            if (entry.getValue() > (Integer) vt[V_TON]) {
                showWarning("Vật tư " + vt[V_TEN] + " chỉ còn " + vt[V_TON] + " " + vt[V_DVT] + ".");
                return;
            }
        }

        String khoa = index == 0 ? KHOA_DUNG_CHUNG : MOCK_LICH_HEN[index - 1][0];
        // TODO DAO (một transaction):
        // 1. PhieuXuatKhoDAO.them(maPhieuXuat, ngayXuat, maNhanVien, maLichHen hoặc
        // NULL)
        // 2. ChiTietXuatKhoDAO.them(maPhieuXuat, maVatTu, soLuongSuDung) cho từng dòng
        // 3. VatTuDAO.truTon(maVatTu, soLuongSuDung) cho từng dòng
        // (SoLuongTon có CHECK >= 0 nên DB cũng chặn xuất quá tồn.)
        String maPhieu = String.format("PX%03d", nextPhieuXuat++);
        List<Object[]> lichSu = daXuat.computeIfAbsent(khoa, k -> new ArrayList<>());
        List<String> sapHet = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dangSoan.entrySet()) {
            Object[] vt = timVatTu(entry.getKey());
            int tonMoi = (Integer) vt[V_TON] - entry.getValue();
            vt[V_TON] = tonMoi;
            lichSu.add(new Object[] { maPhieu, entry.getKey(), entry.getValue() });
            if (tonMoi <= (Integer) vt[V_NGUONG]) {
                sapHet.add(vt[V_TEN] + " (còn " + tonMoi + ")");
            }
        }
        dangSoan.clear();
        loadVatTu();
        loadPhieu();
        loadLichSu();

        String thongBao = "Đã lưu phiếu xuất " + maPhieu + " và trừ tồn kho.";
        if (!sapHet.isEmpty()) {
            thongBao += "\nLưu ý, vật tư đã chạm ngưỡng tồn tối thiểu: " + String.join(", ", sapHet) + ".";
        }
        showMessage(thongBao);
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Nạp danh mục vật tư, lọc theo từ khóa (mã/tên) và loại.
    private void loadVatTu() {
        String keyword = txtTim.getText().trim().toLowerCase();
        String loai = (String) cboLoai.getSelectedItem();
        // Giữ lại dòng đang chọn sau khi nạp lại (ví dụ sau khi trừ tồn).
        String maDangChon = null;
        if (vatTuTable.getSelectedRow() >= 0) {
            maDangChon = (String) vatTuModel.getValueAt(vatTuTable.getSelectedRow(), 0);
        }
        vatTuModel.setRowCount(0);
        // TODO DAO: VatTuDAO.timKiem(keyword, loai)
        for (Object[] vt : kho) {
            boolean khopTuKhoa = keyword.isEmpty()
                    || ((String) vt[V_MA]).toLowerCase().contains(keyword)
                    || ((String) vt[V_TEN]).toLowerCase().contains(keyword);
            boolean khopLoai = loai == null || TAT_CA.equals(loai) || loai.equals(vt[V_LOAI]);
            if (khopTuKhoa && khopLoai) {
                vatTuModel.addRow(new Object[] { vt[V_MA], vt[V_TEN], vt[V_LOAI], vt[V_DVT], vt[V_TON] });
            }
        }
        if (maDangChon != null) {
            for (int i = 0; i < vatTuModel.getRowCount(); i++) {
                if (maDangChon.equals(vatTuModel.getValueAt(i, 0))) {
                    vatTuTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    // Nạp lại bảng phiếu đang soạn.
    private void loadPhieu() {
        phieuModel.setRowCount(0);
        for (Map.Entry<String, Integer> entry : dangSoan.entrySet()) {
            Object[] vt = timVatTu(entry.getKey());
            phieuModel.addRow(new Object[] { vt[V_MA], vt[V_TEN], vt[V_DVT], entry.getValue() });
        }
    }

    // Nạp lại bảng các phiếu đã xuất cho lịch hẹn đang chọn.
    private void loadLichSu() {
        lichSuModel.setRowCount(0);
        int index = cboLichHen.getSelectedIndex();
        if (index < 0) {
            return;
        }
        String khoa = index == 0 ? KHOA_DUNG_CHUNG : MOCK_LICH_HEN[index - 1][0];
        // TODO DAO: ChiTietXuatKhoDAO.timTheoLichHen(maLichHen)
        List<Object[]> ds = daXuat.get(khoa);
        if (ds == null) {
            return;
        }
        for (Object[] dong : ds) {
            Object[] vt = timVatTu((String) dong[1]);
            lichSuModel.addRow(new Object[] { dong[0], dong[1], vt[V_TEN], dong[2] });
        }
    }

    private Object[] timVatTu(String maVatTu) {
        for (Object[] vt : kho) {
            if (vt[V_MA].equals(maVatTu)) {
                return vt;
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
        label.setPreferredSize(new Dimension(110, 32));

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