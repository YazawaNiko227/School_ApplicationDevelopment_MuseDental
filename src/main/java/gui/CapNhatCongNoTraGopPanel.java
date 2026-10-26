package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Công nợ và trả góp theo đợt" (bước 9 trong luồng khám bệnh chính,
 * nối tiếp "Lập hóa đơn" khi bệnh nhân chọn trả góp).
 *
 * Luồng xử lý:
 * 1. Danh sách hồ sơ trả góp (bảng CongNoTraGop) bên trái, có lọc theo trạng
 * thái và tìm theo mã / tên bệnh nhân / mã hóa đơn.
 * 2. Chọn một hồ sơ để xem các đợt thanh toán (bảng DotThanhToan) bên phải.
 * 3. "Thu tiền đợt": thu đợt chưa thanh toán sớm nhất -> đợt = 'Da thanh toan',
 * ghi NgayThanhToanThucTe, giảm SoDotConLai. Thu hết các đợt thì
 * HoaDon chuyển sang 'Da thanh toan'.
 * 4. "Nhắc nợ": soạn nội dung nhắc bệnh nhân về đợt chưa thanh toán gần nhất.
 *
 * Đợt chưa thanh toán mà quá ngày đến hạn được hiển thị là "Quá hạn"
 * (khi nối DB nên cập nhật cột TrangThai = 'Qua han' khi mở màn hình).
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class CapNhatCongNoTraGopPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Trạng thái đợt (DB lưu không dấu: 'Da thanh toan', 'Chua den han', 'Qua
    // han').
    private static final String DOT_DA_THANH_TOAN = "Đã thanh toán";
    private static final String DOT_CHUA_DEN_HAN = "Chưa đến hạn";
    private static final String DOT_QUA_HAN = "Quá hạn";

    // Trạng thái hồ sơ công nợ (tính từ các đợt, không lưu trong DB).
    private static final String DANG_TRA_GOP = "Đang trả góp";
    private static final String QUA_HAN = "Quá hạn";
    private static final String HOAN_TAT = "Hoàn tất";
    private static final String TAT_CA = "Tất cả";

    // Số ngày tính là "sắp đến hạn".
    private static final int NGAY_SAP_DEN_HAN = 7;

    /** Một đợt thanh toán (DotThanhToan). */
    private static final class Dot {
        final String ma;
        final LocalDate denHan;
        final double soTien;
        boolean daThanhToan;
        LocalDate ngayThanhToan;

        Dot(String ma, LocalDate denHan, double soTien, boolean daThanhToan, LocalDate ngayThanhToan) {
            this.ma = ma;
            this.denHan = denHan;
            this.soTien = soTien;
            this.daThanhToan = daThanhToan;
            this.ngayThanhToan = ngayThanhToan;
        }

        // Trạng thái hiển thị: đợt chưa thu mà quá hạn thì là "Quá hạn".
        String trangThai() {
            if (daThanhToan) {
                return DOT_DA_THANH_TOAN;
            }
            return denHan.isBefore(LocalDate.now()) ? DOT_QUA_HAN : DOT_CHUA_DEN_HAN;
        }
    }

    /** Một hồ sơ trả góp (CongNoTraGop) kèm hóa đơn, bệnh nhân và các đợt. */
    private static final class CongNo {
        final String ma;
        final String maHoaDon;
        final String maBenhNhan;
        final String hoTen;
        final String soDienThoai;
        final double tongTien;
        final List<Dot> dots = new ArrayList<>();

        CongNo(String ma, String maHoaDon, String maBenhNhan, String hoTen, String soDienThoai, double tongTien) {
            this.ma = ma;
            this.maHoaDon = maHoaDon;
            this.maBenhNhan = maBenhNhan;
            this.hoTen = hoTen;
            this.soDienThoai = soDienThoai;
            this.tongTien = tongTien;
        }

        // Tương ứng cột CongNoTraGop.SoDotConLai.
        int soDotConLai() {
            int dem = 0;
            for (Dot d : dots) {
                if (!d.daThanhToan) {
                    dem++;
                }
            }
            return dem;
        }

        double daThu() {
            double tong = 0;
            for (Dot d : dots) {
                if (d.daThanhToan) {
                    tong += d.soTien;
                }
            }
            return tong;
        }

        double conLai() {
            return tongTien - daThu();
        }

        // Đợt chưa thanh toán sớm nhất, null nếu đã thu hết.
        Dot dotCanThu() {
            for (Dot d : dots) {
                if (!d.daThanhToan) {
                    return d;
                }
            }
            return null;
        }

        String trangThai() {
            if (soDotConLai() == 0) {
                return HOAN_TAT;
            }
            for (Dot d : dots) {
                if (DOT_QUA_HAN.equals(d.trangThai())) {
                    return QUA_HAN;
                }
            }
            return DANG_TRA_GOP;
        }
    }

    // TODO DAO: CongNoTraGopDAO.layTatCa() +
    // DotThanhToanDAO.timTheoCongNo(maCongNo)
    private final List<CongNo> dsCongNo = new ArrayList<>();
    private boolean dangNapBang = false;

    // ----- Thẻ thống kê -----
    private final JLabel lblDangTraGop = new JLabel("0");
    private final JLabel lblQuaHan = new JLabel("0");
    private final JLabel lblSapDenHan = new JLabel("0");
    private final JLabel lblConPhaiThu = new JLabel("0 đ");

    // ----- Danh sách công nợ -----
    private final JTextField txtTim = new JTextField();
    private final JComboBox<String> cboLoc = new JComboBox<>(
            new String[] { TAT_CA, DANG_TRA_GOP, QUA_HAN, HOAN_TAT });
    private final DefaultTableModel congNoModel = new DefaultTableModel(
            new String[] { "Mã CN", "Hóa đơn", "Bệnh nhân", "Tổng tiền (đ)", "Đã thu (đ)", "Còn lại (đ)",
                    "Đợt còn", "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable congNoTable = new JTable(congNoModel);

    // ----- Chi tiết các đợt -----
    private final JLabel lblBenhNhan = new JLabel("Chưa chọn hồ sơ trả góp");
    private final JLabel lblHoaDon = new JLabel(" ");
    private final JLabel lblTienDo = new JLabel(" ");
    private final DefaultTableModel dotModel = new DefaultTableModel(
            new String[] { "Mã đợt", "Đợt", "Ngày đến hạn", "Số tiền (đ)", "Trạng thái", "Ngày thu" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable dotTable = new JTable(dotModel);
    private final JButton btnNhacNo = createButton("Nhắc nợ", false);
    private final JButton btnThuTien = createButton("Thu tiền đợt", true);

    public CapNhatCongNoTraGopPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        taoDuLieuMau();

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createDanhSachCard(), BorderLayout.CENTER);
        center.add(createChiTietCard(), BorderLayout.EAST);
        add(center, BorderLayout.CENTER);

        lamMoi(null);
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        LocalDate homNay = LocalDate.now();

        // CN001: khớp dữ liệu mẫu trong SQL (HD003, BN002, 3 đợt).
        CongNo cn1 = new CongNo("CN001", "HD003", "BN002", "Nguyễn Thị Lan", "0912000002", 7000000);
        cn1.dots.add(new Dot("DOT001", LocalDate.of(2026, 9, 10), 3000000, true, LocalDate.of(2026, 9, 10)));
        cn1.dots.add(new Dot("DOT002", LocalDate.of(2026, 10, 10), 2000000, false, null));
        cn1.dots.add(new Dot("DOT003", LocalDate.of(2026, 11, 10), 2000000, false, null));
        dsCongNo.add(cn1);

        // CN002: đợt 2 sắp đến hạn (tính theo ngày hiện tại để dễ thử).
        CongNo cn2 = new CongNo("CN002", "HD005", "BN003", "Đặng Quốc Khánh", "0912000003", 6000000);
        cn2.dots.add(new Dot("DOT004", homNay.minusMonths(1), 2000000, true, homNay.minusMonths(1)));
        cn2.dots.add(new Dot("DOT005", homNay.plusDays(5), 2000000, false, null));
        cn2.dots.add(new Dot("DOT006", homNay.plusDays(35), 2000000, false, null));
        dsCongNo.add(cn2);

        // CN003: đã trả xong, dùng để thử bộ lọc "Hoàn tất".
        CongNo cn3 = new CongNo("CN003", "HD006", "BN001", "Võ Thanh Hải", "0912000001", 2000000);
        cn3.dots.add(new Dot("DOT007", homNay.minusMonths(2), 1000000, true, homNay.minusMonths(2)));
        cn3.dots.add(new Dot("DOT008", homNay.minusMonths(1), 1000000, true, homNay.minusMonths(1)));
        dsCongNo.add(cn3);
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard("HỒ SƠ ĐANG TRẢ GÓP", lblDangTraGop, BLUE, 180));
        panel.add(createSummaryCard("ĐỢT QUÁ HẠN", lblQuaHan, RED, 180));
        panel.add(createSummaryCard("SẮP ĐẾN HẠN (" + NGAY_SAP_DEN_HAN + " NGÀY)", lblSapDenHan, ORANGE, 180));
        panel.add(createSummaryCard("CÒN PHẢI THU", lblConPhaiThu, TEAL, 230));
        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel value, Color color, int width) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(width, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        value.setFont(new Font("Arial", Font.BOLD, 24));
        value.setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(value);
        return card;
    }

    // Bên trái: danh sách hồ sơ trả góp.
    private JPanel createDanhSachCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        // Tiêu đề + bộ lọc, ô tìm kiếm.
        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel title = new JLabel("Hồ sơ trả góp");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        filterPanel.setOpaque(false);
        JLabel filterLabel = new JLabel("Trạng thái:");
        filterLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        filterLabel.setForeground(MUTED);
        cboLoc.setFont(new Font("Arial", Font.PLAIN, 12));
        cboLoc.addActionListener(event -> lamMoi(layMaCongNoDangChon()));
        filterPanel.add(filterLabel);
        filterPanel.add(cboLoc);
        header.add(title, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        top.add(header);
        top.add(Box.createVerticalStrut(8));

        styleField(txtTim);
        txtTim.setToolTipText("Nhập mã công nợ, mã hóa đơn hoặc tên bệnh nhân");
        txtTim.addActionListener(event -> lamMoi(null));
        JButton btnTim = createButton("Tìm", false);
        btnTim.addActionListener(event -> lamMoi(null));
        JPanel searchRow = new JPanel(new BorderLayout(8, 0));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(txtTim, BorderLayout.CENTER);
        searchRow.add(btnTim, BorderLayout.EAST);
        searchRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        top.add(searchRow);
        card.add(top, BorderLayout.NORTH);

        styleTable(congNoTable);
        congNoTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        congNoTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        congNoTable.getColumnModel().getColumn(2).setPreferredWidth(130);
        congNoTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(4).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(5).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(6).setPreferredWidth(55);
        congNoTable.getColumnModel().getColumn(7).setPreferredWidth(90);
        congNoTable.getColumnModel().getColumn(7).setCellRenderer(new StatusRenderer());
        congNoTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && !dangNapBang) {
                hienThiChiTiet();
            }
        });
        JScrollPane scroll = new JScrollPane(congNoTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    // Bên phải: các đợt của hồ sơ đang chọn.
    private JPanel createChiTietCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(500, 0));

        JPanel top = new JPanel();
        top.setOpaque(false);
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Các đợt thanh toán");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        lblBenhNhan.setFont(new Font("Arial", Font.BOLD, 12));
        lblBenhNhan.setForeground(INK);
        lblHoaDon.setFont(new Font("Arial", Font.PLAIN, 12));
        lblHoaDon.setForeground(MUTED);
        lblTienDo.setFont(new Font("Arial", Font.BOLD, 12));
        lblTienDo.setForeground(TEAL);
        for (JLabel label : new JLabel[] { title, lblBenhNhan, lblHoaDon, lblTienDo }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        top.add(title);
        top.add(Box.createVerticalStrut(10));
        top.add(lblBenhNhan);
        top.add(Box.createVerticalStrut(4));
        top.add(lblHoaDon);
        top.add(Box.createVerticalStrut(4));
        top.add(lblTienDo);
        card.add(top, BorderLayout.NORTH);

        styleTable(dotTable);
        dotTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        dotTable.getColumnModel().getColumn(1).setPreferredWidth(40);
        dotTable.getColumnModel().getColumn(2).setPreferredWidth(90);
        dotTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        dotTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        dotTable.getColumnModel().getColumn(5).setPreferredWidth(85);
        dotTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        JScrollPane scroll = new JScrollPane(dotTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        btnNhacNo.addActionListener(event -> nhacNo());
        btnThuTien.addActionListener(event -> thuTienDot());
        actions.add(btnNhacNo);
        actions.add(btnThuTien);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // =====================================================================
    // XỬ LÝ SỰ KIỆN
    // =====================================================================

    /** Thu tiền đợt chưa thanh toán sớm nhất của hồ sơ đang chọn. */
    private void thuTienDot() {
        CongNo cn = layCongNoDangChon();
        if (cn == null) {
            showMessage("Vui lòng chọn một hồ sơ trả góp.");
            return;
        }
        int row = dotTable.getSelectedRow();
        if (row < 0) {
            showMessage("Vui lòng chọn đợt cần thu trong bảng bên phải.");
            return;
        }
        Dot dot = cn.dots.get(row);
        if (dot.daThanhToan) {
            showMessage("Đợt " + (row + 1) + " đã được thanh toán.");
            return;
        }
        if (dot != cn.dotCanThu()) {
            showWarning("Vui lòng thu theo thứ tự: hãy thu đợt "
                    + (cn.dots.indexOf(cn.dotCanThu()) + 1) + " trước.");
            return;
        }

        String thongTinQuaHan = DOT_QUA_HAN.equals(dot.trangThai()) ? "\n(Đợt này đã quá hạn.)" : "";
        int choice = JOptionPane.showConfirmDialog(this,
                "Thu đợt " + (row + 1) + " của " + cn.hoTen + ": " + TIEN.format(dot.soTien) + " đ?"
                        + thongTinQuaHan,
                "Xác nhận thu tiền", JOptionPane.YES_NO_OPTION);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        // TODO DAO (một transaction):
        // 1. DotThanhToanDAO.thanhToan(maDot, ngayThanhToanThucTe = hôm nay)
        // -> TrangThai='Da thanh toan'
        // 2. CongNoTraGopDAO.capNhatSoDotConLai(maCongNo, soDotConLai - 1)
        // 3. Nếu SoDotConLai = 0: HoaDonDAO.capNhatTrangThai(maHoaDon, 'Da thanh toan')
        dot.daThanhToan = true;
        dot.ngayThanhToan = LocalDate.now();

        String thongBao = "Đã thu đợt " + (row + 1) + ": " + TIEN.format(dot.soTien) + " đ.";
        if (cn.soDotConLai() == 0) {
            thongBao += "\nHồ sơ " + cn.ma + " đã trả hết, hóa đơn " + cn.maHoaDon
                    + " chuyển sang \"Đã thanh toán\".";
        } else {
            thongBao += "\nCòn " + cn.soDotConLai() + " đợt, " + TIEN.format(cn.conLai()) + " đ.";
        }
        lamMoi(cn.ma);
        showMessage(thongBao);
    }

    /** Soạn nội dung nhắc nợ cho đợt chưa thanh toán gần nhất. */
    private void nhacNo() {
        CongNo cn = layCongNoDangChon();
        if (cn == null) {
            showMessage("Vui lòng chọn một hồ sơ trả góp.");
            return;
        }
        Dot dot = cn.dotCanThu();
        if (dot == null) {
            showMessage("Hồ sơ này đã trả hết, không cần nhắc nợ.");
            return;
        }

        String noiDung;
        if (DOT_QUA_HAN.equals(dot.trangThai())) {
            noiDung = "Phòng khám Muse xin thông báo: đợt thanh toán " + TIEN.format(dot.soTien)
                    + " đ của hóa đơn " + cn.maHoaDon + " đã quá hạn từ ngày " + dot.denHan.format(NGAY)
                    + ". Vui lòng thanh toán sớm. Xin cảm ơn.";
        } else {
            noiDung = "Phòng khám Muse xin nhắc: đợt thanh toán " + TIEN.format(dot.soTien)
                    + " đ của hóa đơn " + cn.maHoaDon + " sẽ đến hạn vào ngày " + dot.denHan.format(NGAY)
                    + ". Xin cảm ơn.";
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Gửi tin nhắn tới " + cn.hoTen + " (" + cn.soDienThoai + "):\n\n" + noiDung,
                "Xác nhận nhắc nợ", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO: gửi SMS / Zalo thật khi có tích hợp (hiện chỉ giả lập).
            showMessage("Đã gửi nhắc nợ tới " + cn.soDienThoai + ".");
        }
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Nạp lại bảng công nợ theo bộ lọc, cập nhật thẻ thống kê, chọn lại dòng cũ.
    private void lamMoi(String maCanChon) {
        dangNapBang = true;
        String keyword = txtTim.getText().trim().toLowerCase();
        String loc = (String) cboLoc.getSelectedItem();

        congNoModel.setRowCount(0);
        for (CongNo cn : dsCongNo) {
            boolean khopTuKhoa = keyword.isEmpty()
                    || cn.ma.toLowerCase().contains(keyword)
                    || cn.maHoaDon.toLowerCase().contains(keyword)
                    || cn.hoTen.toLowerCase().contains(keyword);
            boolean khopLoc = loc == null || TAT_CA.equals(loc) || loc.equals(cn.trangThai());
            if (khopTuKhoa && khopLoc) {
                congNoModel.addRow(new Object[] {
                        cn.ma, cn.maHoaDon, cn.hoTen, TIEN.format(cn.tongTien), TIEN.format(cn.daThu()),
                        TIEN.format(cn.conLai()), cn.soDotConLai(), cn.trangThai() });
            }
        }

        capNhatThongKe();

        if (maCanChon != null) {
            for (int i = 0; i < congNoModel.getRowCount(); i++) {
                if (maCanChon.equals(congNoModel.getValueAt(i, 0))) {
                    congNoTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        dangNapBang = false;
        hienThiChiTiet();
    }

    // Thống kê tính trên toàn bộ hồ sơ (không phụ thuộc bộ lọc).
    private void capNhatThongKe() {
        LocalDate homNay = LocalDate.now();
        int dangTraGop = 0;
        int dotQuaHan = 0;
        int sapDenHan = 0;
        double conPhaiThu = 0;
        for (CongNo cn : dsCongNo) {
            if (cn.soDotConLai() > 0) {
                dangTraGop++;
                conPhaiThu += cn.conLai();
            }
            for (Dot d : cn.dots) {
                if (d.daThanhToan) {
                    continue;
                }
                if (d.denHan.isBefore(homNay)) {
                    dotQuaHan++;
                } else if (!d.denHan.isAfter(homNay.plusDays(NGAY_SAP_DEN_HAN))) {
                    sapDenHan++;
                }
            }
        }
        lblDangTraGop.setText(String.valueOf(dangTraGop));
        lblQuaHan.setText(String.valueOf(dotQuaHan));
        lblSapDenHan.setText(String.valueOf(sapDenHan));
        lblConPhaiThu.setText(TIEN.format(conPhaiThu) + " đ");
    }

    // Đổ các đợt của hồ sơ đang chọn lên bảng bên phải.
    private void hienThiChiTiet() {
        CongNo cn = layCongNoDangChon();
        dotModel.setRowCount(0);
        if (cn == null) {
            lblBenhNhan.setText("Chưa chọn hồ sơ trả góp");
            lblHoaDon.setText("Chọn một hồ sơ ở danh sách bên trái để xem các đợt.");
            lblTienDo.setText(" ");
            btnNhacNo.setEnabled(false);
            btnThuTien.setEnabled(false);
            return;
        }
        lblBenhNhan.setText(cn.hoTen + "  ·  " + cn.maBenhNhan + "  ·  " + cn.soDienThoai);
        lblHoaDon.setText("Hồ sơ " + cn.ma + "  ·  Hóa đơn " + cn.maHoaDon
                + "  ·  Tổng " + TIEN.format(cn.tongTien) + " đ");
        lblTienDo.setText("Đã thu " + TIEN.format(cn.daThu()) + " đ  ·  Còn lại "
                + TIEN.format(cn.conLai()) + " đ (" + cn.soDotConLai() + " đợt)");

        int stt = 1;
        for (Dot d : cn.dots) {
            dotModel.addRow(new Object[] {
                    d.ma, stt++, d.denHan.format(NGAY), TIEN.format(d.soTien), d.trangThai(),
                    d.ngayThanhToan == null ? "-" : d.ngayThanhToan.format(NGAY) });
        }
        boolean conNo = cn.soDotConLai() > 0;
        btnNhacNo.setEnabled(conNo);
        btnThuTien.setEnabled(conNo);
    }

    private String layMaCongNoDangChon() {
        int row = congNoTable.getSelectedRow();
        return row < 0 ? null : (String) congNoModel.getValueAt(row, 0);
    }

    private CongNo layCongNoDangChon() {
        String ma = layMaCongNoDangChon();
        if (ma == null) {
            return null;
        }
        for (CongNo cn : dsCongNo) {
            if (cn.ma.equals(ma)) {
                return cn;
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

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
        field.setPreferredSize(new Dimension(160, 32));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(32);
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

    // Tô màu chữ cho cột trạng thái (dùng cho cả hồ sơ và đợt).
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (HOAN_TAT.equals(text) || DOT_DA_THANH_TOAN.equals(text)) {
                label.setForeground(TEAL);
            } else if (DANG_TRA_GOP.equals(text)) {
                label.setForeground(BLUE);
            } else if (QUA_HAN.equals(text) || DOT_QUA_HAN.equals(text)) {
                label.setForeground(RED);
            } else {
                label.setForeground(MUTED);
            }
            return label;
        }
    }
}