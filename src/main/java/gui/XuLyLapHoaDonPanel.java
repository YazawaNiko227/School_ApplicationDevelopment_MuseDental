package gui;

import dao.HoaDonDAO;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import entity.HoaDon;
import util.HoaDonPdfExporter;
import util.QrCodeUtil;

/**
 * Panel "Lập hóa đơn" (bước 9 trong luồng khám bệnh chính).
 *
 * Luồng xử lý:
 * 1. Thu ngân chọn lần khám đã chốt (ChanDoan = 'Da chot') chưa có hóa đơn.
 * 2. Hệ thống tự nạp tiền dịch vụ (từ phác đồ: KeHoachDieuTri_DichVu ->
 * ChiTietHoaDon) và tiền thuốc (từ đơn thuốc: ChiTietDonThuoc ->
 * ChiTietHoaDonThuoc, giá = VatTu.DonGiaBan).
 * 3. Chọn phương thức thanh toán:
 * - Tiền mặt / Chuyển khoản -> HoaDon = 'Da thanh toan'.
 * - Trả góp -> chọn số đợt, tạo CongNoTraGop + DotThanhToan,
 * HoaDon = 'Dang tra gop'. Đợt 1 thu ngay khi lập hóa đơn.
 * 4. Bấm "Lập hóa đơn".
 *
 * Dữ liệu lần khám, dịch vụ, thuốc và hóa đơn được đọc/ghi trực tiếp qua DAO.
 */
public class XuLyLapHoaDonPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Phương thức thanh toán (DB lưu không dấu: 'Tien mat', 'Chuyen khoan', 'Tra
    // gop').
    private static final String TIEN_MAT = "Tiền mặt";
    private static final String CHUYEN_KHOAN = "Chuyển khoản";
    private static final String TRA_GOP = "Trả góp";

    // Trạng thái đợt thanh toán.
    private static final String DOT_DA_THANH_TOAN = "Đã thanh toán";
    private static final String DOT_CHUA_DEN_HAN = "Chưa đến hạn";

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final String tenDangNhap;
    private String maNhanVien;

    // {MaChanDoan, MaBenhNhan, HoTen, KetLuanChanDoan}
    private final List<String[]> dsLanKham = new ArrayList<>();
    private List<HoaDonDAO.DongTinhTien> dsDichVuDangChon = new ArrayList<>();
    private List<HoaDonDAO.DongTinhTien> dsThuocDangChon = new ArrayList<>();
    private boolean loiTaiDuLieu;

    // Tổng tiền của lần khám đang chọn.
    private double tienDichVu = 0;
    private double tienThuoc = 0;
    private boolean dangNapCombo = false;

    // ----- Chọn lần khám -----
    private final JComboBox<String> cboLanKham = new JComboBox<>();
    private final JLabel lblBenhNhan = new JLabel(" ");
    private final JLabel lblKetLuan = new JLabel(" ");
    private final JLabel lblNgayLap = new JLabel(LocalDate.now().format(NGAY));

    // ----- Chi tiết tiền -----
    private final DefaultTableModel dichVuModel = new DefaultTableModel(
            new String[] { "Mã", "Tên dịch vụ", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dichVuTable = new JTable(dichVuModel);
    private final DefaultTableModel thuocModel = new DefaultTableModel(
            new String[] { "Mã", "Tên thuốc", "Đơn giá (đ)", "SL", "Thành tiền (đ)" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable thuocTable = new JTable(thuocModel);
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienThuoc = new JLabel("0 đ");
    private final JLabel lblTongTien = new JLabel("0 đ");

    // ----- Thanh toán -----
    private final JComboBox<String> cboPhuongThuc = new JComboBox<>(
            new String[] { TIEN_MAT, CHUYEN_KHOAN, TRA_GOP });
    private final JSpinner spnSoDot = new JSpinner(new SpinnerNumberModel(3, 2, 12, 1));
    private final DefaultTableModel dotModel = new DefaultTableModel(
            new String[] { "Đợt", "Ngày đến hạn", "Số tiền (đ)", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dotTable = new JTable(dotModel);
    private final JLabel lblGhiChuThanhToan = new JLabel(" ");
    private final JLabel lblQr = new JLabel();
    private final JLabel lblQrNoiDung = new JLabel(" ");
    private final JPanel qrPanel = new JPanel();
    private final JButton btnLap = createButton("Lập hóa đơn", true);

    public XuLyLapHoaDonPanel() {
        this("");
    }

    public XuLyLapHoaDonPanel(String tenDangNhap) {
        this.tenDangNhap = tenDangNhap == null ? "" : tenDangNhap.trim();
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(createLanKhamCard(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(createChiTietCard(), BorderLayout.CENTER);
        center.add(createThanhToanCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        napComboLanKham();
        // Gắn listener sau khi đã nạp item để không bị kích hoạt sớm.
        cboLanKham.addActionListener(event -> {
            if (!dangNapCombo) {
                chonLanKham();
            }
        });
        cboPhuongThuc.addActionListener(event -> capNhatPhuongThuc());
        spnSoDot.addChangeListener(event -> loadBangDot());
        chonLanKham();
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createLanKhamCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Lần khám cần lập hóa đơn"));
        card.add(Box.createVerticalStrut(10));

        cboLanKham.setFont(new Font("Arial", Font.PLAIN, 12));
        card.add(createRow("Lần khám (đã chốt)", cboLanKham));

        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblKetLuan.setFont(new Font("Arial", Font.PLAIN, 12));
        lblKetLuan.setForeground(INK);
        lblNgayLap.setFont(new Font("Arial", Font.PLAIN, 12));
        lblNgayLap.setForeground(MUTED);
        for (JLabel label : new JLabel[] { lblBenhNhan, lblKetLuan }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(label);
            card.add(Box.createVerticalStrut(4));
        }
        JLabel lblThongTin = new JLabel();
        lblThongTin.setFont(new Font("Arial", Font.PLAIN, 12));
        lblThongTin.setForeground(MUTED);
        lblThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblThongTin.setText("Ngày lập: " + LocalDate.now().format(NGAY)
                + (tenDangNhap.isEmpty() ? "  ·  Chưa xác định tài khoản thu ngân"
                        : "  ·  Thu ngân: " + tenDangNhap));
        card.add(lblThongTin);
        return card;
    }

    // Bên trái: chi tiết tiền dịch vụ và tiền thuốc.
    private JPanel createChiTietCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        content.add(createCardTitle("Tiền dịch vụ (theo phác đồ)"));
        content.add(Box.createVerticalStrut(8));
        styleTable(dichVuTable);
        dichVuTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        dichVuTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        dichVuTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dichVuTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        dichVuTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        content.add(createScroll(dichVuTable));
        content.add(Box.createVerticalStrut(6));
        content.add(createMoneyRow("Cộng tiền dịch vụ", lblTienDichVu, 13, INK));
        content.add(Box.createVerticalStrut(14));

        content.add(createCardTitle("Tiền thuốc (theo đơn thuốc)"));
        content.add(Box.createVerticalStrut(8));
        styleTable(thuocTable);
        thuocTable.getColumnModel().getColumn(0).setPreferredWidth(55);
        thuocTable.getColumnModel().getColumn(1).setPreferredWidth(180);
        thuocTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        thuocTable.getColumnModel().getColumn(3).setPreferredWidth(40);
        thuocTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        content.add(createScroll(thuocTable));
        content.add(Box.createVerticalStrut(6));
        content.add(createMoneyRow("Cộng tiền thuốc", lblTienThuoc, 13, INK));
        content.add(Box.createVerticalStrut(14));

        content.add(createMoneyRow("TỔNG THANH TOÁN", lblTongTien, 20, TEAL));

        card.add(content, BorderLayout.CENTER);
        return card;
    }

    // Bên phải: phương thức thanh toán + bảng chia đợt.
    private JPanel createThanhToanCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(450, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.add(createCardTitle("Thanh toán"));
        top.add(Box.createVerticalStrut(10));
        top.add(createRow("Phương thức", cboPhuongThuc));
        top.add(createRow("Số đợt trả góp", spnSoDot));

        JLabel qrTitle = new JLabel("Mã QR chuyển khoản (tượng trưng)");
        qrTitle.setFont(new Font("Arial", Font.BOLD, 11));
        qrTitle.setForeground(MUTED);
        qrTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblQr.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblQrNoiDung.setFont(new Font("Arial", Font.PLAIN, 10));
        lblQrNoiDung.setForeground(MUTED);
        lblQrNoiDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        qrPanel.setOpaque(false);
        qrPanel.setLayout(new BoxLayout(qrPanel, BoxLayout.Y_AXIS));
        qrPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        qrPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        qrPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 215));
        qrPanel.add(qrTitle);
        qrPanel.add(Box.createVerticalStrut(4));
        qrPanel.add(lblQr);
        qrPanel.add(Box.createVerticalStrut(4));
        qrPanel.add(lblQrNoiDung);
        qrPanel.setVisible(false);
        top.add(qrPanel);

        lblGhiChuThanhToan.setFont(new Font("Arial", Font.ITALIC, 11));
        lblGhiChuThanhToan.setForeground(MUTED);
        lblGhiChuThanhToan.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(lblGhiChuThanhToan);
        top.add(Box.createVerticalStrut(8));
        JLabel dotTitle = new JLabel("Lịch các đợt thanh toán");
        dotTitle.setFont(new Font("Arial", Font.BOLD, 11));
        dotTitle.setForeground(MUTED);
        dotTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        top.add(dotTitle);
        card.add(top, BorderLayout.NORTH);

        styleTable(dotTable);
        dotTable.getColumnModel().getColumn(0).setPreferredWidth(40);
        dotTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(2).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        JScrollPane scroll = new JScrollPane(dotTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        JButton btnLamMoi = createButton("Làm mới", false);
        btnLamMoi.addActionListener(event -> napComboLanKham());
        btnLap.addActionListener(event -> lapHoaDon());
        actions.add(btnLamMoi);
        actions.add(btnLap);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Chọn lần khám: nạp tiền dịch vụ, tiền thuốc và tính tổng. */
    private void chonLanKham() {
        int index = cboLanKham.getSelectedIndex();
        dichVuModel.setRowCount(0);
        thuocModel.setRowCount(0);
        dsDichVuDangChon = new ArrayList<>();
        dsThuocDangChon = new ArrayList<>();
        tienDichVu = 0;
        tienThuoc = 0;

        if (index < 0) {
            lblBenhNhan.setText(loiTaiDuLieu
                    ? "Không thể tải danh sách lần khám từ CSDL."
                    : "Chưa có lần khám đã chốt chưa lập hóa đơn.");
            lblKetLuan.setText(loiTaiDuLieu ? "Vui lòng kiểm tra kết nối CSDL và thử làm mới."
                    : "Chỉ lần khám đã chốt và chưa có hóa đơn mới được hiển thị.");
            btnLap.setEnabled(false);
            capNhatTongTien();
            return;
        }
        String[] lanKham = dsLanKham.get(index);
        lblBenhNhan.setText("Bệnh nhân: " + lanKham[2] + "  ·  " + lanKham[1]);
        lblKetLuan.setText("Kết luận chẩn đoán: " + lanKham[3]);
        try {
            dsDichVuDangChon = hoaDonDAO.layDichVuTheoChanDoan(lanKham[0]);
            dsThuocDangChon = hoaDonDAO.layThuocTheoChanDoan(lanKham[0]);
            for (HoaDonDAO.DongTinhTien dong : dsDichVuDangChon) {
                double thanhTien = dong.donGia() * dong.soLuong();
                tienDichVu += thanhTien;
                dichVuModel.addRow(new Object[] { dong.ma(), dong.ten(), TIEN.format(dong.donGia()),
                        dong.soLuong(), TIEN.format(thanhTien) });
            }
            for (HoaDonDAO.DongTinhTien dong : dsThuocDangChon) {
                double thanhTien = dong.donGia() * dong.soLuong();
                tienThuoc += thanhTien;
                thuocModel.addRow(new Object[] { dong.ma(), dong.ten(), TIEN.format(dong.donGia()),
                        dong.soLuong(), TIEN.format(thanhTien) });
            }
        } catch (java.sql.SQLException exception) {
            btnLap.setEnabled(false);
            JOptionPane.showMessageDialog(this,
                    "Không thể tải chi tiết dịch vụ và thuốc từ CSDL:\n" + exception.getMessage(),
                    "Lỗi tải hóa đơn", JOptionPane.ERROR_MESSAGE);
            capNhatTongTien();
            return;
        }
        capNhatTongTien();
        btnLap.setEnabled(maNhanVien != null && tienDichVu + tienThuoc > 0);
    }

    /** Bật/tắt phần trả góp theo phương thức thanh toán. */
    private void capNhatPhuongThuc() {
        boolean traGop = TRA_GOP.equals(cboPhuongThuc.getSelectedItem());
        boolean chuyenKhoan = CHUYEN_KHOAN.equals(cboPhuongThuc.getSelectedItem());
        spnSoDot.setEnabled(traGop);
        qrPanel.setVisible(chuyenKhoan);
        if (chuyenKhoan) {
            capNhatMaQr();
        }
        if (qrPanel.getParent() != null) {
            qrPanel.getParent().revalidate();
            qrPanel.getParent().repaint();
        }
        if (traGop) {
            lblGhiChuThanhToan.setText("Đợt 1 thu ngay khi lập hóa đơn, các đợt sau cách nhau 1 tháng.");
        } else {
            lblGhiChuThanhToan.setText("Thanh toán một lần, hóa đơn ghi nhận \"Đã thanh toán\".");
        }
        loadBangDot();
    }

    /** Cập nhật ảnh QR tượng trưng theo lần khám và tổng tiền đang chọn. */
    private void capNhatMaQr() {
        double tong = tienDichVu + tienThuoc;
        String noiDung = "THANH TOAN HOA DON - " + TIEN.format(tong) + " d";
        int index = cboLanKham.getSelectedIndex();
        if (index >= 0 && index < dsLanKham.size()) {
            noiDung = "THANH TOAN " + dsLanKham.get(index)[0] + " - " + TIEN.format(tong) + " d";
        }
        lblQr.setIcon(new ImageIcon(QrCodeUtil.taoMaQrTuongTrung(noiDung, 140)));
        lblQrNoiDung.setText("<html>" + noiDung + "<br><i>QR tượng trưng, không dùng để quét thật.</i></html>");
    }

    /** Kiểm tra rồi lập hóa đơn (và công nợ nếu trả góp). */
    private void lapHoaDon() {
        int index = cboLanKham.getSelectedIndex();
        if (index < 0) {
            showMessage("Vui lòng chọn lần khám cần lập hóa đơn.");
            return;
        }
        if (maNhanVien == null) {
            showWarning("Không xác định được nhân viên đang đăng nhập để ghi nhận người lập hóa đơn.");
            return;
        }
        double tong = tienDichVu + tienThuoc;
        if (tong <= 0) {
            showWarning("Lần khám này không có khoản nào để tính tiền.");
            return;
        }
        String[] lanKham = dsLanKham.get(index);
        boolean traGop = TRA_GOP.equals(cboPhuongThuc.getSelectedItem());
        int soDot = traGop ? (Integer) spnSoDot.getValue() : 0;
        String phuongThucDb = traGop ? "Tra gop"
                : CHUYEN_KHOAN.equals(cboPhuongThuc.getSelectedItem()) ? "Chuyen khoan" : "Tien mat";
        String xacNhanThu = traGop
                ? "Xác nhận đã thu đợt đầu " + TIEN.format(Math.floor(tong / soDot)) + " đ."
                : CHUYEN_KHOAN.equals(cboPhuongThuc.getSelectedItem())
                        ? "Chỉ tiếp tục sau khi đã đối soát giao dịch chuyển khoản."
                        : "Chỉ tiếp tục sau khi đã nhận đủ tiền mặt.";
        Object thongDiepXacNhan;
        if (CHUYEN_KHOAN.equals(cboPhuongThuc.getSelectedItem())) {
            String noiDungQr = "THANH TOAN " + lanKham[0] + " - " + TIEN.format(tong) + " d";
            JLabel text = new JLabel("<html>Lập hóa đơn <b>" + TIEN.format(tong) + " đ</b> cho "
                    + lanKham[2] + "<br>Nhân viên lập: " + tenDangNhap
                    + "<br>Phương thức: " + cboPhuongThuc.getSelectedItem()
                    + "<br>" + xacNhanThu
                    + "<br><br>Mã QR bên cạnh là mã <b>tượng trưng</b> để minh họa chuyển khoản.</html>");
            JPanel xacNhanPanel = new JPanel(new BorderLayout(16, 0));
            xacNhanPanel.setOpaque(false);
            xacNhanPanel.add(text, BorderLayout.CENTER);
            xacNhanPanel.add(new JLabel(new ImageIcon(
                    QrCodeUtil.taoMaQrTuongTrung(noiDungQr, 130))), BorderLayout.EAST);
            thongDiepXacNhan = xacNhanPanel;
        } else {
            thongDiepXacNhan = "Lập hóa đơn " + TIEN.format(tong) + " đ cho " + lanKham[2] + "\n"
                    + "Nhân viên lập: " + tenDangNhap + "\nPhương thức: "
                    + cboPhuongThuc.getSelectedItem() + "\n" + xacNhanThu;
        }
        int choice = JOptionPane.showConfirmDialog(this, thongDiepXacNhan,
                "Xác nhận lập hóa đơn", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        String trangThai = traGop ? HoaDon.DANG_TRA_GOP : HoaDon.DA_THANH_TOAN;
        String maHoaDon;
        try {
            maHoaDon = hoaDonDAO.lapHoaDon(LocalDate.now(), trangThai, phuongThucDb,
                    maNhanVien, lanKham[1], lanKham[0], soDot);
        } catch (java.sql.SQLException | IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể lập hóa đơn:\n" + exception.getMessage(),
                    "Lỗi lập hóa đơn", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String thongBao = "Đã lập hóa đơn " + maHoaDon + ". Tổng tiền: " + TIEN.format(tong) + " đ.";
        if (traGop) {
            thongBao += "\nĐã tạo công nợ gồm " + soDot
                    + " đợt. Đợt đầu đã thu, còn lại " + (soDot - 1) + " đợt cần thu.";
        }
        showMessage(thongBao);

        int exportChoice = JOptionPane.showConfirmDialog(this,
                "Bạn có muốn xuất hóa đơn " + maHoaDon + " ra file PDF không?",
                "Xuất hóa đơn PDF", JOptionPane.YES_NO_OPTION);
        if (exportChoice == JOptionPane.YES_OPTION) {
            try {
                HoaDonDAO.ChiTietHoaDon detail = hoaDonDAO.layChiTietHoaDon(maHoaDon);
                if (detail == null) {
                    showWarning("Không tìm thấy hóa đơn vừa lập để xuất PDF.");
                } else {
                    xuatHoaDonPdf(detail);
                }
            } catch (java.sql.SQLException exception) {
                showDatabaseError("Không thể tải hóa đơn để xuất PDF", exception);
            }
        }
        napComboLanKham();
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private void xuatHoaDonPdf(HoaDonDAO.ChiTietHoaDon detail) {
        String maHoaDon = detail.hoaDon.ma;
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Lưu hóa đơn PDF");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Tệp PDF (*.pdf)", "pdf"));
        fileChooser.setSelectedFile(new File("HoaDon_" + maHoaDon + ".pdf"));
        if (fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File selectedFile = fileChooser.getSelectedFile();
        String fileName = selectedFile.getName();
        if (!fileName.toLowerCase(Locale.ROOT).endsWith(".pdf")) {
            selectedFile = new File(selectedFile.getParentFile(), fileName + ".pdf");
        }
        if (selectedFile.exists()) {
            int overwriteChoice = JOptionPane.showConfirmDialog(this,
                    "Tệp đã tồn tại. Bạn có muốn ghi đè không?",
                    "Xác nhận ghi đè", JOptionPane.YES_NO_OPTION);
            if (overwriteChoice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            HoaDonPdfExporter.export(selectedFile.toPath(), maHoaDon, detail.hoaDon.ngayLap,
                    detail.hoaDon.maBenhNhan, detail.hoaDon.tenBenhNhan, detail.maChanDoan,
                    detail.ketLuan, hienThiPhuongThuc(detail.hoaDon.phuongThuc),
                    hienThiTrangThai(detail.hoaDon.trangThai),
                    layDongHoaDon(detail.dichVu), layDongHoaDon(detail.thuoc),
                    tong(detail.dichVu), tong(detail.thuoc), detail.hoaDon.tongTien);
            showMessage("Đã xuất hóa đơn PDF:\n" + selectedFile.getAbsolutePath());
        } catch (IOException | SecurityException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể xuất hóa đơn PDF:\n" + exception.getMessage(),
                    "Lỗi xuất hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
    }

    private List<String[]> layDongHoaDon(List<HoaDonDAO.DongHoaDon> dongHoaDon) {
        List<String[]> rows = new ArrayList<>();
        for (HoaDonDAO.DongHoaDon dong : dongHoaDon) {
            rows.add(new String[] { dong.ma, dong.ten, TIEN.format(dong.donGia),
                    String.valueOf(dong.soLuong), TIEN.format(dong.thanhTien()) });
        }
        return rows;
    }

    private static double tong(List<HoaDonDAO.DongHoaDon> dongHoaDon) {
        double tong = 0;
        for (HoaDonDAO.DongHoaDon dong : dongHoaDon) {
            tong += dong.thanhTien();
        }
        return tong;
    }

    private static String hienThiPhuongThuc(String phuongThuc) {
        if ("Tien mat".equals(phuongThuc)) {
            return TIEN_MAT;
        }
        if ("Chuyen khoan".equals(phuongThuc)) {
            return CHUYEN_KHOAN;
        }
        return "Tra gop".equals(phuongThuc) ? TRA_GOP : phuongThuc;
    }

    private static String hienThiTrangThai(String trangThai) {
        if (HoaDon.DA_THANH_TOAN.equals(trangThai)) {
            return "Đã thanh toán";
        }
        if (HoaDon.DANG_TRA_GOP.equals(trangThai)) {
            return "Đang trả góp";
        }
        return trangThai;
    }

    // Nạp lại combo các lần khám chờ lập hóa đơn.
    private void napComboLanKham() {
        dangNapCombo = true;
        String maDangChon = null;
        int selectedIndex = cboLanKham.getSelectedIndex();
        if (selectedIndex >= 0 && selectedIndex < dsLanKham.size()) {
            maDangChon = dsLanKham.get(selectedIndex)[0];
        }
        cboLanKham.removeAllItems();
        dsLanKham.clear();
        try {
            maNhanVien = hoaDonDAO.layMaNhanVienTheoTenDangNhap(tenDangNhap);
            loiTaiDuLieu = false;
            for (HoaDonDAO.LanKhamHoaDon lanKham : hoaDonDAO.layLanKhamChuaLapHoaDon()) {
                String[] row = { lanKham.maChanDoan(), lanKham.maBenhNhan(),
                        lanKham.hoTen(), lanKham.ketLuan() };
                dsLanKham.add(row);
                cboLanKham.addItem(row[0] + " - " + row[2] + " (" + row[1] + ")");
            }
        } catch (java.sql.SQLException exception) {
            maNhanVien = null;
            loiTaiDuLieu = true;
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dữ liệu lập hóa đơn từ CSDL:\n" + exception.getMessage(),
                    "Lỗi tải hóa đơn", JOptionPane.ERROR_MESSAGE);
        }
        if (maDangChon != null) {
            for (int i = 0; i < dsLanKham.size(); i++) {
                if (maDangChon.equals(dsLanKham.get(i)[0])) {
                    cboLanKham.setSelectedIndex(i);
                    break;
                }
            }
        }
        dangNapCombo = false;
        chonLanKham();
    }

    private void showDatabaseError(String action, Exception exception) {
        JOptionPane.showMessageDialog(this, action + ":\n" + exception.getMessage(),
                "Lỗi cơ sở dữ liệu", JOptionPane.ERROR_MESSAGE);
    }

    private void capNhatTongTien() {
        lblTienDichVu.setText(TIEN.format(tienDichVu) + " đ");
        lblTienThuoc.setText(TIEN.format(tienThuoc) + " đ");
        lblTongTien.setText(TIEN.format(tienDichVu + tienThuoc) + " đ");
        capNhatPhuongThuc();
    }

    // Hiển thị lịch chia đợt khi chọn trả góp (chia đều, đợt cuối nhận phần dư).
    private void loadBangDot() {
        dotModel.setRowCount(0);
        if (!TRA_GOP.equals(cboPhuongThuc.getSelectedItem())) {
            return;
        }
        double tong = tienDichVu + tienThuoc;
        if (tong <= 0) {
            return;
        }
        int soDot = (Integer) spnSoDot.getValue();
        double moiDot = Math.floor(tong / soDot);
        for (int i = 0; i < soDot; i++) {
            double soTien = (i == soDot - 1) ? tong - moiDot * (soDot - 1) : moiDot;
            dotModel.addRow(new Object[] {
                    i + 1,
                    LocalDate.now().plusMonths(i).format(NGAY),
                    TIEN.format(soTien),
                    i == 0 ? DOT_DA_THANH_TOAN : DOT_CHUA_DEN_HAN });
        }
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

    // Một hàng "nhãn ... số tiền" căn hai đầu.
    private JPanel createMoneyRow(String caption, JLabel value, int size, Color color) {
        JLabel captionLabel = new JLabel(caption);
        captionLabel.setFont(new Font("Arial", Font.BOLD, 11));
        captionLabel.setForeground(MUTED);
        value.setFont(new Font("Arial", Font.BOLD, size));
        value.setForeground(color);

        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.add(captionLabel, BorderLayout.WEST);
        row.add(value, BorderLayout.EAST);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, size + 14));
        return row;
    }

    private JScrollPane createScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        scroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        scroll.setPreferredSize(new Dimension(100, 130));
        return scroll;
    }

    /**
     * Một hàng "nhãn + ô nhập": nhãn rộng cố định bên trái, ô nhập giãn hết phần
     * còn lại.
     */
    private JPanel createRow(String labelText, Component field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
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

    private static JButton createButton(String text, boolean primary) {
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