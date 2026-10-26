package gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Khám lâm sàng và sơ đồ răng" (bước 5 và bước 8 trong luồng khám bệnh
 * chính).
 *
 * Nha sĩ thực hiện:
 * 1. Chọn bệnh nhân "Đã check-in" trong hàng chờ, bấm "Vào khám"
 * -> tạo ChanDoan nháp (gắn MaLichHen), LichHen = "Đang khám",
 * PhongKham = "Đang khám".
 * 2. Nhập lý do khám, triệu chứng, kết luận chẩn đoán; nhấp vào từng răng trên
 * sơ đồ để ghi tình trạng bệnh lý (bảng SoDoRang, khóa (MaHoSo, MaRang)).
 * 3. Bấm "Lưu nháp" để lưu giữa chừng, "Hoàn tất khám" để chốt:
 * ChanDoan = "Đã chốt", LichHen = "Hoàn thành", PhongKham = "Cần vệ sinh".
 *
 * Phác đồ điều trị, kê đơn thuốc, ghi nhận vật tư làm ở các menu riêng
 * (chỉ cần MaChanDoan), nên nha sĩ nên làm xong các việc đó trước khi hoàn tất.
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ. Chỗ cần nối DAO ghi "TODO DAO".
 */
public class XuLyKhamLamSangVaSoDoRangPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color ORANGE = new Color(177, 125, 47);
    private static final Color RED = new Color(200, 70, 70);
    private static final Color BLUE = new Color(51, 91, 177);

    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Trạng thái lịch hẹn (hiển thị có dấu, DB lưu không dấu).
    private static final String DA_XAC_NHAN = "Đã xác nhận";
    private static final String DA_CHECK_IN = "Đã check-in";
    private static final String DANG_KHAM = "Đang khám";
    private static final String HOAN_THANH = "Hoàn thành";

    // Trạng thái phòng.
    private static final String SAN_SANG = "Sẵn sàng";
    private static final String CAN_VE_SINH = "Cần vệ sinh";
    private static final String BAO_TRI = "Bảo trì";

    // Tình trạng bệnh lý của răng (cột SoDoRang.TinhTrangBenhLy).
    private static final String[] TINH_TRANG = {
            "Bình thường", "Sâu răng", "Viêm tủy", "Mẻ răng", "Mọc lệch", "Đã trám", "Viêm nướu", "Mất răng"
    };

    // Bệnh nhân dưới ngưỡng tuổi này tự động hiển thị sơ đồ răng sữa.
    private static final int TUOI_RANG_SUA = 13;

    /** Một ca khám = một lịch hẹn + lần khám (ChanDoan) sinh ra từ lịch đó. */
    private static final class CaKham {
        final String maLich;
        final LocalDateTime gioHen;
        final String maBenhNhan;
        final String hoTen;
        final String gioiTinh;
        final LocalDate ngaySinh;
        final String tienSu;
        final String diUng;
        final String nguoiBaoHo;
        final String maHoSo;
        final String nhaSi;
        String maPhong;
        String trangThai;
        LocalDateTime checkIn;
        // Phần chẩn đoán (có sau khi bấm "Vào khám").
        String maChanDoan;
        String lyDo = "";
        String trieuChung = "";
        String ketLuan = "";
        boolean daChot;

        CaKham(String maLich, LocalDateTime gioHen, String maBenhNhan, String hoTen, String gioiTinh,
                LocalDate ngaySinh, String tienSu, String diUng, String nguoiBaoHo, String maHoSo,
                String nhaSi, String maPhong, String trangThai, LocalDateTime checkIn) {
            this.maLich = maLich;
            this.gioHen = gioHen;
            this.maBenhNhan = maBenhNhan;
            this.hoTen = hoTen;
            this.gioiTinh = gioiTinh;
            this.ngaySinh = ngaySinh;
            this.tienSu = tienSu;
            this.diUng = diUng;
            this.nguoiBaoHo = nguoiBaoHo;
            this.maHoSo = maHoSo;
            this.nhaSi = nhaSi;
            this.maPhong = maPhong;
            this.trangThai = trangThai;
            this.checkIn = checkIn;
        }
    }

    // Dữ liệu mẫu. TODO DAO: LichHenDAO.layLichKhamHomNay(),
    // PhongKhamDAO.layTatCa(),
    // SoDoRangDAO.layTheoHoSo(maHoSo)
    private final List<CaKham> dsCa = new ArrayList<>();
    private final Map<String, String> trangThaiPhong = new LinkedHashMap<>();
    // maHoSo -> (maRang -> {tình trạng, ghi chú})
    private final Map<String, Map<Integer, String[]>> soDoRang = new HashMap<>();
    private int nextChanDoan = 5;

    private CaKham caHienTai;
    private boolean dangNapBang;

    // ----- Hàng chờ -----
    private final DefaultTableModel lichModel = new DefaultTableModel(
            new String[] { "Mã lịch", "Giờ hẹn", "Bệnh nhân", "Phòng", "Trạng thái" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable lichTable = new JTable(lichModel);
    private final JLabel[] summaryValues = new JLabel[3];
    private final JButton btnVaoKham = createButton("Vào khám", true);

    // ----- Thông tin bệnh nhân -----
    private final JLabel lblTenBn = new JLabel("Chưa chọn bệnh nhân");
    private final JLabel lblThongTinBn = new JLabel(" ");
    private final JLabel lblDiUng = new JLabel(" ");
    private final JLabel lblTienSu = new JLabel(" ");
    private final JLabel lblBaoHo = new JLabel(" ");
    private final JLabel lblTrangThaiKham = new JLabel(" ");

    // ----- Khám lâm sàng -----
    private final JTextArea txtLyDo = new JTextArea(2, 20);
    private final JTextArea txtTrieuChung = new JTextArea(3, 20);
    private final JTextArea txtKetLuan = new JTextArea(3, 20);

    // ----- Sơ đồ răng -----
    private final ToothChart chart = new ToothChart();
    private final JCheckBox chkRangSua = new JCheckBox("Hiển thị sơ đồ răng sữa");
    private final JLabel lblRangChon = new JLabel(" ");
    private final JComboBox<String> cboTinhTrang = new JComboBox<>(TINH_TRANG);
    private final JTextField txtGhiChuRang = new JTextField();
    private final JButton btnLuuRang = createButton("Lưu răng", true);
    private final JButton btnXoaRang = createButton("Xóa ghi nhận", false);
    private final JLabel lblTomTatRang = new JLabel(" ");

    // ----- Thao tác -----
    private final JButton btnLuuNhap = createButton("Lưu nháp", false);
    private final JButton btnHoanTat = createButton("Hoàn tất khám", true);

    public XuLyKhamLamSangVaSoDoRangPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        taoDuLieuMau();

        add(createSummaryPanel(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(18, 0));
        center.setOpaque(false);
        center.add(createQueueCard(), BorderLayout.WEST);
        center.add(createDetailPanel(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        chart.setOnSelect(this::chonRang);
        lamMoi();
    }

    // =====================================================================
    // DỮ LIỆU MẪU
    // =====================================================================

    private void taoDuLieuMau() {
        LocalDate homNay = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        dsCa.add(new CaKham("LH003", homNay.atTime(8, 30), "BN003", "Đặng Quốc Khánh", "Nam",
                LocalDate.of(1978, 1, 15), "Tiểu đường type 2", null, null, "HS003",
                "NV001 - Nguyễn Văn An", "P01", DA_CHECK_IN, now.minusMinutes(12)));
        dsCa.add(new CaKham("LH004", homNay.atTime(9, 0), "BN002", "Nguyễn Thị Lan", "Nữ",
                LocalDate.of(1985, 9, 21), "Cao huyết áp", null, null, "HS002",
                "NV002 - Trần Thị Bình", "P02", DA_CHECK_IN, now.minusMinutes(8)));
        dsCa.add(new CaKham("LH005", homNay.atTime(9, 30), "BN004", "Trần Bảo Ngọc", "Nữ",
                LocalDate.of(2015, 6, 30), "Không", null, "Trần Văn Minh (cha)", "HS004",
                "NV002 - Trần Thị Bình", "P02", DA_CHECK_IN, now.minusMinutes(3)));
        dsCa.add(new CaKham("LH006", homNay.atTime(10, 0), "BN001", "Võ Thanh Hải", "Nam",
                LocalDate.of(1990, 4, 10), "Không", "Dị ứng Penicillin", null, "HS001",
                "NV001 - Nguyễn Văn An", "P01", DA_CHECK_IN, now.minusMinutes(1)));

        // Khớp bảng PhongKham trong SQL (P01 - P03).
        trangThaiPhong.put("P01", SAN_SANG);
        trangThaiPhong.put("P02", SAN_SANG);
        trangThaiPhong.put("P03", BAO_TRI);

        // Khớp dữ liệu SoDoRang trong SQL.
        datRangMau("HS001", 16, "Sâu răng", "Cần trám composite");
        datRangMau("HS001", 26, "Bình thường", "");
        datRangMau("HS001", 38, "Mọc lệch", "Chỉ định nhổ");
        datRangMau("HS002", 11, "Mẻ răng", "Dự kiến bọc sứ");
        datRangMau("HS003", 46, "Viêm tủy", "Cần điều trị tủy");
        datRangMau("HS004", 55, "Sâu răng", "");
    }

    private void datRangMau(String maHoSo, int maRang, String tinhTrang, String ghiChu) {
        soDoRang.computeIfAbsent(maHoSo, k -> new TreeMap<>()).put(maRang, new String[] { tinhTrang, ghiChu });
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard(0, "CHỜ KHÁM", ORANGE));
        panel.add(createSummaryCard(1, "ĐANG KHÁM", BLUE));
        panel.add(createSummaryCard(2, "HOÀN THÀNH", TEAL));
        return panel;
    }

    private JPanel createSummaryCard(int index, String title, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 16, 12, 16)));
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

    // Hàng chờ khám bên trái.
    private JPanel createQueueCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
        card.setPreferredSize(new Dimension(450, 0));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Hàng chờ khám");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel hint = new JLabel("Chọn bệnh nhân đã check-in rồi bấm \"Vào khám\".");
        hint.setFont(new Font("Arial", Font.PLAIN, 11));
        hint.setForeground(MUTED);
        hint.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(title);
        header.add(Box.createVerticalStrut(4));
        header.add(hint);
        card.add(header, BorderLayout.NORTH);

        styleTable(lichTable);
        lichTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        lichTable.getColumnModel().getColumn(1).setPreferredWidth(55);
        lichTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        lichTable.getColumnModel().getColumn(3).setPreferredWidth(50);
        lichTable.getColumnModel().getColumn(4).setPreferredWidth(95);
        lichTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        lichTable.getSelectionModel().addListSelectionListener(event -> {
            if (event.getValueIsAdjusting() || dangNapBang) {
                return;
            }
            chuyenCa(layCaDangChon());
        });
        JScrollPane scroll = new JScrollPane(lichTable);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        card.add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        btnVaoKham.addActionListener(event -> vaoKham());
        actions.add(btnVaoKham);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    // Phần chi tiết bên phải: thông tin bệnh nhân, khám lâm sàng, sơ đồ răng.
    private JPanel createDetailPanel() {
        JPanel inner = new JPanel(new BorderLayout(0, 14));
        inner.setOpaque(false);
        inner.add(createClinicalCard(), BorderLayout.NORTH);
        inner.add(createToothCard(), BorderLayout.CENTER);

        JPanel content = new JPanel(new BorderLayout(0, 14));
        content.setOpaque(false);
        content.add(createPatientCard(), BorderLayout.NORTH);
        content.add(inner, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        btnLuuNhap.addActionListener(event -> luuNhap());
        btnHoanTat.addActionListener(event -> hoanTat());
        actions.add(btnLuuNhap);
        actions.add(btnHoanTat);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createPatientCard() {
        JPanel card = createCard();

        lblTenBn.setFont(new Font("Arial", Font.BOLD, 16));
        lblTenBn.setForeground(INK);
        lblThongTinBn.setFont(new Font("Arial", Font.PLAIN, 12));
        lblThongTinBn.setForeground(MUTED);
        lblDiUng.setFont(new Font("Arial", Font.BOLD, 12));
        lblDiUng.setForeground(RED);
        lblTienSu.setFont(new Font("Arial", Font.PLAIN, 12));
        lblTienSu.setForeground(INK);
        lblBaoHo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblBaoHo.setForeground(INK);
        lblTrangThaiKham.setFont(new Font("Arial", Font.BOLD, 11));
        lblTrangThaiKham.setForeground(BLUE);

        for (JLabel label : new JLabel[] { lblTenBn, lblThongTinBn, lblDiUng, lblTienSu, lblBaoHo,
                lblTrangThaiKham }) {
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
        }
        card.add(lblTenBn);
        card.add(Box.createVerticalStrut(4));
        card.add(lblThongTinBn);
        card.add(Box.createVerticalStrut(8));
        card.add(lblDiUng);
        card.add(Box.createVerticalStrut(4));
        card.add(lblTienSu);
        card.add(Box.createVerticalStrut(4));
        card.add(lblBaoHo);
        card.add(Box.createVerticalStrut(8));
        card.add(lblTrangThaiKham);
        return card;
    }

    private JPanel createClinicalCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Khám lâm sàng"));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Lý do khám", createTextAreaScroll(txtLyDo, 52)));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Triệu chứng", createTextAreaScroll(txtTrieuChung, 66)));
        card.add(Box.createVerticalStrut(10));
        card.add(labeled("Kết luận chẩn đoán *", createTextAreaScroll(txtKetLuan, 66)));
        return card;
    }

    private JPanel createToothCard() {
        JPanel card = createCard();
        card.add(createCardTitle("Sơ đồ răng"));
        card.add(Box.createVerticalStrut(6));

        // Hàng điều khiển: chuyển răng sữa / răng vĩnh viễn.
        chkRangSua.setFont(new Font("Arial", Font.PLAIN, 12));
        chkRangSua.setForeground(INK);
        chkRangSua.setOpaque(false);
        chkRangSua.setFocusPainted(false);
        chkRangSua.addActionListener(event -> {
            chart.setChild(chkRangSua.isSelected());
            capNhatEditorRang();
        });
        JPanel toggleRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        toggleRow.setOpaque(false);
        toggleRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        toggleRow.add(chkRangSua);
        toggleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        card.add(toggleRow);

        chart.setAlignmentX(Component.LEFT_ALIGNMENT);
        chart.setMaximumSize(new Dimension(Integer.MAX_VALUE, 240));
        card.add(chart);
        card.add(Box.createVerticalStrut(8));

        // Chú giải màu.
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        legend.setOpaque(false);
        legend.setAlignmentX(Component.LEFT_ALIGNMENT);
        legend.add(createLegendItem("Chưa ghi nhận", Color.WHITE));
        for (String tinhTrang : TINH_TRANG) {
            legend.add(createLegendItem(tinhTrang, ToothChart.mauTheo(tinhTrang)));
        }
        legend.setMaximumSize(new Dimension(Integer.MAX_VALUE, legend.getPreferredSize().height + 4));
        card.add(legend);
        card.add(Box.createVerticalStrut(12));

        // Khu ghi nhận tình trạng của răng đang chọn.
        lblRangChon.setFont(new Font("Arial", Font.BOLD, 12));
        lblRangChon.setForeground(INK);
        lblRangChon.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblRangChon);
        card.add(Box.createVerticalStrut(6));

        cboTinhTrang.setFont(new Font("Arial", Font.PLAIN, 12));
        cboTinhTrang.setPreferredSize(new Dimension(140, 32));
        styleField(txtGhiChuRang);
        txtGhiChuRang.setPreferredSize(new Dimension(260, 32));
        txtGhiChuRang.setToolTipText("Ghi chú cho răng đang chọn (tối đa 500 ký tự)");
        btnLuuRang.addActionListener(event -> luuRang());
        btnXoaRang.addActionListener(event -> xoaRang());

        JPanel editRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        editRow.setOpaque(false);
        editRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        editRow.add(cboTinhTrang);
        editRow.add(txtGhiChuRang);
        editRow.add(btnLuuRang);
        editRow.add(btnXoaRang);
        editRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(editRow);
        card.add(Box.createVerticalStrut(10));

        lblTomTatRang.setFont(new Font("Arial", Font.PLAIN, 11));
        lblTomTatRang.setForeground(MUTED);
        lblTomTatRang.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblTomTatRang);
        card.add(Box.createVerticalGlue());
        return card;
    }

    private JPanel createLegendItem(String text, Color color) {
        JLabel label = new JLabel(text, new SquareIcon(color), SwingConstants.LEFT);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(MUTED);
        label.setIconTextGap(5);
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    // =====================================================================
    // XỬ LÝ NGHIỆP VỤ
    // =====================================================================

    /**
     * Nha sĩ bấm "Vào khám": tạo ChanDoan nháp, lịch và phòng chuyển "Đang khám".
     */
    private void vaoKham() {
        CaKham ca = layCaDangChon();
        if (ca == null) {
            showMessage("Vui lòng chọn một bệnh nhân trong hàng chờ.");
            return;
        }
        if (!DA_CHECK_IN.equals(ca.trangThai)) {
            showMessage("Chỉ vào khám được bệnh nhân đang ở trạng thái \"" + DA_CHECK_IN + "\".");
            return;
        }
        String trangThaiPhongHienTai = trangThaiPhong.get(ca.maPhong);
        if (!SAN_SANG.equals(trangThaiPhongHienTai)) {
            showMessage("Phòng " + ca.maPhong + " đang \"" + trangThaiPhongHienTai + "\". "
                    + "Hãy nhờ lễ tân điều phối sang phòng \"" + SAN_SANG + "\" "
                    + "hoặc chờ y tá chuẩn bị xong phòng.");
            return;
        }

        // TODO DAO (một transaction):
        // 1. ChanDoanDAO.taoNhap(maChanDoan, ngayKham, maLichHen, maHoSo, maNhaSi) ->
        // TrangThai='Nhap'
        // 2. LichHenDAO.capNhatTrangThai(maLichHen, 'Dang kham')
        // 3. PhongKhamDAO.capNhatTrangThai(maPhong, 'Dang kham')
        // Mã chẩn đoán sinh từ DB; một lịch hẹn chỉ sinh tối đa 1 lần khám
        // (UQ_ChanDoan_LichHen).
        ca.maChanDoan = String.format("CD%03d", nextChanDoan++);
        ca.trangThai = DANG_KHAM;
        trangThaiPhong.put(ca.maPhong, DANG_KHAM);

        lamMoi(ca.maLich);
        showMessage("Đã tạo chẩn đoán nháp " + ca.maChanDoan + ". Phòng " + ca.maPhong
                + " chuyển sang \"" + DANG_KHAM + "\".");
    }

    /** Lưu giữa chừng, vẫn ở trạng thái nháp. */
    private void luuNhap() {
        if (!dangKham(caHienTai)) {
            return;
        }
        try {
            kiemTraForm();
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
            return;
        }
        ghiFormVaoCa(caHienTai);
        // TODO DAO: ChanDoanDAO.capNhatNhap(maChanDoan, lyDo, trieuChung, ketLuan)
        showMessage("Đã lưu nháp chẩn đoán " + caHienTai.maChanDoan + ".");
    }

    /**
     * Chốt chẩn đoán: ChanDoan "Đã chốt", lịch "Hoàn thành", phòng "Cần vệ sinh".
     */
    private void hoanTat() {
        if (!dangKham(caHienTai)) {
            return;
        }
        try {
            kiemTraForm();
            if (txtKetLuan.getText().trim().isEmpty()) {
                txtKetLuan.requestFocusInWindow();
                throw new IllegalArgumentException("Kết luận chẩn đoán không được để trống khi hoàn tất khám.");
            }
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Hoàn tất khám cho " + caHienTai.hoTen + "?\n"
                        + "Sau khi chốt sẽ không sửa được chẩn đoán.\n"
                        + "Hãy chắc chắn phác đồ điều trị, đơn thuốc và vật tư tiêu hao đã được ghi nhận.",
                "Xác nhận hoàn tất khám", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        ghiFormVaoCa(caHienTai);
        // TODO DAO (một transaction):
        // 1. ChanDoanDAO.chot(maChanDoan, lyDo, trieuChung, ketLuan) -> TrangThai='Da
        // chot'
        // 2. LichHenDAO.capNhatTrangThai(maLichHen, 'Hoan thanh')
        // 3. PhongKhamDAO.capNhatTrangThai(maPhong, 'Can ve sinh')
        // Các răng đã ghi nhận được lưu bằng SoDoRangDAO ở mỗi lần bấm "Lưu răng".
        CaKham ca = caHienTai;
        ca.daChot = true;
        ca.trangThai = HOAN_THANH;
        trangThaiPhong.put(ca.maPhong, CAN_VE_SINH);

        lamMoi(ca.maLich);
        showMessage("Đã chốt chẩn đoán " + ca.maChanDoan + ". Phòng " + ca.maPhong
                + " chuyển sang \"" + CAN_VE_SINH + "\" để y tá dọn dẹp. "
                + "Thu ngân có thể lập hóa đơn cho lần khám này.");
    }

    // ----- Sơ đồ răng -----

    private void chonRang(int maRang) {
        capNhatEditorRang();
    }

    /** Ghi nhận (thêm mới hoặc cập nhật) tình trạng của răng đang chọn. */
    private void luuRang() {
        Integer maRang = chart.getSelected();
        if (!dangKham(caHienTai) || maRang == null) {
            return;
        }
        String ghiChu = txtGhiChuRang.getText().trim();
        if (ghiChu.length() > 500) {
            showWarning("Ghi chú răng tối đa 500 ký tự.");
            return;
        }
        String tinhTrang = (String) cboTinhTrang.getSelectedItem();
        // TODO DAO: SoDoRangDAO.luuHoacCapNhat(maHoSo, maRang, loaiRang(maRang),
        // tinhTrang, ghiChu)
        layDuLieuRang().put(maRang, new String[] { tinhTrang, ghiChu });
        chart.repaint();
        capNhatEditorRang();
        capNhatTomTatRang();
    }

    /** Xóa ghi nhận của răng đang chọn khỏi sơ đồ. */
    private void xoaRang() {
        Integer maRang = chart.getSelected();
        if (!dangKham(caHienTai) || maRang == null || !layDuLieuRang().containsKey(maRang)) {
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this,
                "Xóa ghi nhận của răng " + maRang + " khỏi sơ đồ?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // TODO DAO: SoDoRangDAO.xoa(maHoSo, maRang)
            layDuLieuRang().remove(maRang);
            chart.repaint();
            capNhatEditorRang();
            capNhatTomTatRang();
        }
    }

    /**
     * Răng 1-2: cửa, 3: nanh, 4-5: hàm nhỏ, 6-7: hàm, 8: khôn; số 5x-8x là răng
     * sữa.
     */
    private static String loaiRang(int maRang) {
        if (maRang >= 50) {
            return "Răng sữa";
        }
        int viTri = maRang % 10;
        if (viTri <= 2) {
            return "Răng cửa";
        } else if (viTri == 3) {
            return "Răng nanh";
        } else if (viTri <= 5) {
            return "Răng hàm nhỏ";
        } else if (viTri <= 7) {
            return "Răng hàm";
        }
        return "Răng khôn";
    }

    // =====================================================================
    // ĐỒNG BỘ GIAO DIỆN
    // =====================================================================

    // Chuyển sang ca khác: lưu tạm nội dung đang gõ của ca cũ (nếu còn đang khám).
    private void chuyenCa(CaKham caMoi) {
        if (dangKham(caHienTai)) {
            ghiFormVaoCa(caHienTai);
        }
        caHienTai = caMoi;
        hienThiCa();
    }

    // Nạp lại bảng hàng chờ, giữ nguyên dòng đang chọn (nếu có).
    private void lamMoi() {
        lamMoi(caHienTai == null ? null : caHienTai.maLich);
    }

    private void lamMoi(String maLichCanChon) {
        dangNapBang = true;
        lichModel.setRowCount(0);
        int choKham = 0;
        int dangKhamDem = 0;
        int hoanThanh = 0;
        for (CaKham ca : dsCa) {
            if (DA_CHECK_IN.equals(ca.trangThai)) {
                choKham++;
            } else if (DANG_KHAM.equals(ca.trangThai)) {
                dangKhamDem++;
            } else if (HOAN_THANH.equals(ca.trangThai)) {
                hoanThanh++;
            } else {
                continue; // Lịch chưa check-in chưa vào hàng chờ khám.
            }
            lichModel.addRow(new Object[] {
                    ca.maLich, ca.gioHen.format(GIO), ca.hoTen, ca.maPhong, ca.trangThai });
        }
        summaryValues[0].setText(String.valueOf(choKham));
        summaryValues[1].setText(String.valueOf(dangKhamDem));
        summaryValues[2].setText(String.valueOf(hoanThanh));

        if (maLichCanChon != null) {
            for (int i = 0; i < lichModel.getRowCount(); i++) {
                if (maLichCanChon.equals(lichModel.getValueAt(i, 0))) {
                    lichTable.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
        dangNapBang = false;
        caHienTai = layCaDangChon();
        hienThiCa();
    }

    // Đổ dữ liệu của ca đang chọn lên form và bật/tắt các nút theo trạng thái.
    private void hienThiCa() {
        CaKham ca = caHienTai;
        boolean choPhepSua = dangKham(ca);

        if (ca == null) {
            lblTenBn.setText("Chưa chọn bệnh nhân");
            lblThongTinBn.setText("Chọn một bệnh nhân trong hàng chờ để xem hồ sơ và sơ đồ răng.");
            lblDiUng.setText(" ");
            lblTienSu.setText(" ");
            lblBaoHo.setText(" ");
            lblTrangThaiKham.setText(" ");
            txtLyDo.setText("");
            txtTrieuChung.setText("");
            txtKetLuan.setText("");
            chart.setData(new TreeMap<>());
            chart.setChild(false);
            chkRangSua.setSelected(false);
        } else {
            int tuoi = ca.ngaySinh == null ? -1 : Period.between(ca.ngaySinh, LocalDate.now()).getYears();
            lblTenBn.setText(ca.hoTen + "  ·  " + ca.maBenhNhan);
            lblThongTinBn.setText(ca.gioiTinh
                    + (ca.ngaySinh == null ? "" : "  ·  " + ca.ngaySinh.format(NGAY) + " (" + tuoi + " tuổi)")
                    + "  ·  Hồ sơ " + ca.maHoSo + "  ·  " + ca.nhaSi);
            if (ca.diUng == null || ca.diUng.isEmpty()) {
                lblDiUng.setText("Dị ứng: không ghi nhận");
                lblDiUng.setForeground(MUTED);
                lblDiUng.setFont(new Font("Arial", Font.PLAIN, 12));
            } else {
                lblDiUng.setText("CẢNH BÁO DỊ ỨNG: " + ca.diUng);
                lblDiUng.setForeground(RED);
                lblDiUng.setFont(new Font("Arial", Font.BOLD, 12));
            }
            lblTienSu.setText("Tiền sử bệnh lý: " + (ca.tienSu == null ? "Không" : ca.tienSu));
            lblBaoHo.setText(ca.nguoiBaoHo == null ? " " : "Người bảo hộ: " + ca.nguoiBaoHo);
            lblTrangThaiKham.setText(moTaTrangThai(ca));

            txtLyDo.setText(ca.lyDo);
            txtTrieuChung.setText(ca.trieuChung);
            txtKetLuan.setText(ca.ketLuan);
            txtLyDo.setCaretPosition(0);
            txtTrieuChung.setCaretPosition(0);
            txtKetLuan.setCaretPosition(0);

            chart.setData(soDoRang.computeIfAbsent(ca.maHoSo, k -> new TreeMap<>()));
            boolean treEm = tuoi >= 0 && tuoi < TUOI_RANG_SUA;
            chkRangSua.setSelected(treEm);
            chart.setChild(treEm);
        }

        txtLyDo.setEditable(choPhepSua);
        txtTrieuChung.setEditable(choPhepSua);
        txtKetLuan.setEditable(choPhepSua);
        btnLuuNhap.setEnabled(choPhepSua);
        btnHoanTat.setEnabled(choPhepSua);
        btnVaoKham.setEnabled(ca != null && DA_CHECK_IN.equals(ca.trangThai));
        capNhatEditorRang();
        capNhatTomTatRang();
    }

    private String moTaTrangThai(CaKham ca) {
        if (ca.daChot) {
            return "Lần khám " + ca.maChanDoan + " đã được chốt (chỉ xem).";
        }
        if (DANG_KHAM.equals(ca.trangThai)) {
            return "Đang khám tại phòng " + ca.maPhong + "  ·  Chẩn đoán nháp " + ca.maChanDoan;
        }
        return "Đã check-in, chờ nha sĩ bấm \"Vào khám\" (phòng " + ca.maPhong + "). "
                + "Có thể xem sơ đồ răng từ các lần khám trước.";
    }

    // Cập nhật khu ghi nhận răng theo răng đang chọn và trạng thái ca khám.
    private void capNhatEditorRang() {
        Integer maRang = chart.getSelected();
        boolean choPhepSua = dangKham(caHienTai);
        if (maRang == null) {
            lblRangChon.setText("Nhấp vào một răng trên sơ đồ để xem hoặc ghi nhận tình trạng.");
            cboTinhTrang.setSelectedIndex(0);
            txtGhiChuRang.setText("");
            cboTinhTrang.setEnabled(false);
            txtGhiChuRang.setEnabled(false);
            btnLuuRang.setEnabled(false);
            btnXoaRang.setEnabled(false);
            return;
        }
        String[] banGhi = layDuLieuRang().get(maRang);
        lblRangChon.setText("Răng " + maRang + "  ·  " + loaiRang(maRang)
                + (banGhi == null ? "  ·  chưa ghi nhận" : ""));
        cboTinhTrang.setSelectedItem(banGhi == null ? TINH_TRANG[0] : banGhi[0]);
        txtGhiChuRang.setText(banGhi == null ? "" : banGhi[1]);
        cboTinhTrang.setEnabled(choPhepSua);
        txtGhiChuRang.setEnabled(choPhepSua);
        btnLuuRang.setEnabled(choPhepSua);
        btnXoaRang.setEnabled(choPhepSua && banGhi != null);
    }

    private void capNhatTomTatRang() {
        Map<Integer, String[]> data = layDuLieuRang();
        if (data.isEmpty()) {
            lblTomTatRang.setText("Chưa có răng nào được ghi nhận trong hồ sơ này.");
            return;
        }
        StringBuilder sb = new StringBuilder("<html>Đã ghi nhận " + data.size() + " răng: ");
        boolean dauTien = true;
        for (Map.Entry<Integer, String[]> entry : data.entrySet()) {
            if (!dauTien) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append(" (").append(entry.getValue()[0]).append(")");
            dauTien = false;
        }
        sb.append("</html>");
        lblTomTatRang.setText(sb.toString());
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    private boolean dangKham(CaKham ca) {
        return ca != null && DANG_KHAM.equals(ca.trangThai) && !ca.daChot;
    }

    private Map<Integer, String[]> layDuLieuRang() {
        if (caHienTai == null) {
            return new TreeMap<>();
        }
        return soDoRang.computeIfAbsent(caHienTai.maHoSo, k -> new TreeMap<>());
    }

    private CaKham layCaDangChon() {
        int row = lichTable.getSelectedRow();
        if (row < 0) {
            return null;
        }
        String ma = (String) lichModel.getValueAt(row, 0);
        for (CaKham ca : dsCa) {
            if (ca.maLich.equals(ma)) {
                return ca;
            }
        }
        return null;
    }

    private void ghiFormVaoCa(CaKham ca) {
        ca.lyDo = txtLyDo.getText().trim();
        ca.trieuChung = txtTrieuChung.getText().trim();
        ca.ketLuan = txtKetLuan.getText().trim();
    }

    // Kiểm tra độ dài theo các cột của bảng ChanDoan.
    private void kiemTraForm() {
        if (txtLyDo.getText().trim().length() > 500) {
            txtLyDo.requestFocusInWindow();
            throw new IllegalArgumentException("Lý do khám tối đa 500 ký tự.");
        }
        if (txtTrieuChung.getText().trim().length() > 1000) {
            txtTrieuChung.requestFocusInWindow();
            throw new IllegalArgumentException("Triệu chứng tối đa 1000 ký tự.");
        }
        if (txtKetLuan.getText().trim().length() > 1000) {
            txtKetLuan.requestFocusInWindow();
            throw new IllegalArgumentException("Kết luận chẩn đoán tối đa 1000 ký tự.");
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

    private JScrollPane createTextAreaScroll(JTextArea area, int height) {
        area.setFont(new Font("Arial", Font.PLAIN, 12));
        area.setForeground(INK);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(6, 8, 6, 8));
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.setPreferredSize(new Dimension(100, height));
        return scroll;
    }

    // Nhãn nhỏ phía trên + ô nhập, cao đúng bằng nội dung.
    private JPanel labeled(String caption, JComponent input) {
        JPanel panel = new JPanel(new BorderLayout(0, 4));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel label = new JLabel(caption);
        label.setFont(new Font("Arial", Font.BOLD, 10));
        label.setForeground(MUTED);
        panel.add(label, BorderLayout.NORTH);
        panel.add(input, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        return panel;
    }

    private void styleField(JTextField field) {
        field.setFont(new Font("Arial", Font.PLAIN, 12));
        field.setForeground(INK);
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

    // =====================================================================
    // THÀNH PHẦN VẼ TAY
    // =====================================================================

    // Tô màu chữ cho cột trạng thái lịch hẹn.
    private static final class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column);
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 11));
            String text = String.valueOf(value);
            if (HOAN_THANH.equals(text) || DA_XAC_NHAN.equals(text)) {
                label.setForeground(TEAL);
            } else if (DA_CHECK_IN.equals(text)) {
                label.setForeground(ORANGE);
            } else if (DANG_KHAM.equals(text)) {
                label.setForeground(BLUE);
            } else {
                label.setForeground(INK);
            }
            return label;
        }
    }

    // Ô vuông màu nhỏ dùng cho chú giải.
    private static final class SquareIcon implements Icon {
        private final Color color;

        private SquareIcon(Color color) {
            this.color = color;
        }

        @Override
        public int getIconWidth() {
            return 14;
        }

        @Override
        public int getIconHeight() {
            return 14;
        }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(color);
            g.fillRoundRect(x, y, 14, 14, 5, 5);
            g.setColor(new Color(190, 202, 209));
            g.drawRoundRect(x, y, 13, 13, 5, 5);
            g.dispose();
        }
    }

    /**
     * Sơ đồ răng theo ký hiệu FDI, vẽ bằng Graphics2D (không dùng layout lưới).
     * Hàm trên: 18-11 | 21-28; hàm dưới: 48-41 | 31-38.
     * Răng sữa: 55-51 | 61-65 và 85-81 | 71-75.
     * Nhấp vào răng để chọn; màu ô thể hiện tình trạng bệnh lý đã ghi nhận.
     */
    private static final class ToothChart extends JPanel {
        private static final int[] NGUOI_LON_TREN_PHAI = { 18, 17, 16, 15, 14, 13, 12, 11 };
        private static final int[] NGUOI_LON_TREN_TRAI = { 21, 22, 23, 24, 25, 26, 27, 28 };
        private static final int[] NGUOI_LON_DUOI_PHAI = { 48, 47, 46, 45, 44, 43, 42, 41 };
        private static final int[] NGUOI_LON_DUOI_TRAI = { 31, 32, 33, 34, 35, 36, 37, 38 };
        private static final int[] TRE_EM_TREN_PHAI = { 55, 54, 53, 52, 51 };
        private static final int[] TRE_EM_TREN_TRAI = { 61, 62, 63, 64, 65 };
        private static final int[] TRE_EM_DUOI_PHAI = { 85, 84, 83, 82, 81 };
        private static final int[] TRE_EM_DUOI_TRAI = { 71, 72, 73, 74, 75 };

        private Map<Integer, String[]> data = new TreeMap<>();
        private boolean child;
        private Integer selected;
        private IntConsumer onSelect;
        private final Map<Integer, Rectangle> hitBoxes = new HashMap<>();

        private ToothChart() {
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(640, 240));
            setBorder(BorderFactory.createLineBorder(LINE));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent event) {
                    for (Map.Entry<Integer, Rectangle> entry : hitBoxes.entrySet()) {
                        if (entry.getValue().contains(event.getPoint())) {
                            selected = entry.getKey();
                            repaint();
                            if (onSelect != null) {
                                onSelect.accept(entry.getKey());
                            }
                            return;
                        }
                    }
                }
            });
        }

        static Color mauTheo(String tinhTrang) {
            switch (tinhTrang) {
                case "Bình thường":
                    return new Color(206, 238, 226);
                case "Sâu răng":
                    return new Color(205, 120, 45);
                case "Viêm tủy":
                    return new Color(214, 69, 80);
                case "Mẻ răng":
                    return new Color(245, 205, 80);
                case "Mọc lệch":
                    return new Color(155, 100, 190);
                case "Đã trám":
                    return new Color(51, 91, 177);
                case "Viêm nướu":
                    return new Color(235, 125, 160);
                case "Mất răng":
                    return new Color(150, 162, 170);
                default:
                    return Color.WHITE;
            }
        }

        private static boolean canChuTrang(String tinhTrang) {
            return "Sâu răng".equals(tinhTrang) || "Viêm tủy".equals(tinhTrang)
                    || "Mọc lệch".equals(tinhTrang) || "Đã trám".equals(tinhTrang)
                    || "Viêm nướu".equals(tinhTrang) || "Mất răng".equals(tinhTrang);
        }

        void setData(Map<Integer, String[]> data) {
            this.data = data;
            this.selected = null;
            repaint();
        }

        void setChild(boolean child) {
            this.child = child;
            this.selected = null;
            repaint();
        }

        void setOnSelect(IntConsumer onSelect) {
            this.onSelect = onSelect;
        }

        Integer getSelected() {
            return selected;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hitBoxes.clear();

            int[] trenPhai = child ? TRE_EM_TREN_PHAI : NGUOI_LON_TREN_PHAI;
            int[] trenTrai = child ? TRE_EM_TREN_TRAI : NGUOI_LON_TREN_TRAI;
            int[] duoiPhai = child ? TRE_EM_DUOI_PHAI : NGUOI_LON_DUOI_PHAI;
            int[] duoiTrai = child ? TRE_EM_DUOI_TRAI : NGUOI_LON_DUOI_TRAI;
            int n = trenPhai.length;

            int gap = 24;
            int margin = 24;
            int cellW = Math.max(24, Math.min(46, (getWidth() - 2 * margin - gap) / (2 * n)));
            int cellH = 52;
            int total = 2 * n * cellW + gap;
            int x0 = (getWidth() - total) / 2;
            int yTren = 38;
            int yDuoi = yTren + cellH + 54;
            int xTrai = x0 + n * cellW + gap;

            // Đường chia giữa hai hàm và giữa hai bên.
            g.setColor(LINE);
            g.setStroke(new BasicStroke(1.2f));
            int yGiua = yTren + cellH + 27;
            g.drawLine(x0, yGiua, x0 + total, yGiua);
            int xGiua = x0 + n * cellW + gap / 2;
            g.drawLine(xGiua, yTren - 8, xGiua, yDuoi + cellH + 8);

            // Nhãn.
            g.setColor(MUTED);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g.getFontMetrics();
            g.drawString("PHẢI BỆNH NHÂN", x0, 20);
            String trai = "TRÁI BỆNH NHÂN";
            g.drawString(trai, x0 + total - fm.stringWidth(trai), 20);
            String hamTren = "HÀM TRÊN";
            g.drawString(hamTren, xGiua - fm.stringWidth(hamTren) / 2, 20);
            String hamDuoi = "HÀM DƯỚI";
            g.drawString(hamDuoi, xGiua - fm.stringWidth(hamDuoi) / 2, yDuoi + cellH + 26);

            for (int i = 0; i < n; i++) {
                veRang(g, trenPhai[i], x0 + i * cellW, yTren, cellW, cellH);
                veRang(g, trenTrai[i], xTrai + i * cellW, yTren, cellW, cellH);
                veRang(g, duoiPhai[i], x0 + i * cellW, yDuoi, cellW, cellH);
                veRang(g, duoiTrai[i], xTrai + i * cellW, yDuoi, cellW, cellH);
            }
            g.dispose();
        }

        private void veRang(Graphics2D g, int maRang, int x, int y, int cellW, int cellH) {
            Rectangle box = new Rectangle(x + 3, y, cellW - 6, cellH);
            hitBoxes.put(maRang, box);

            String[] banGhi = data.get(maRang);
            String tinhTrang = banGhi == null ? null : banGhi[0];
            g.setColor(tinhTrang == null ? Color.WHITE : mauTheo(tinhTrang));
            g.fillRoundRect(box.x, box.y, box.width, box.height, 14, 14);

            boolean duocChon = selected != null && selected == maRang;
            g.setColor(duocChon ? TEAL : new Color(190, 202, 209));
            g.setStroke(new BasicStroke(duocChon ? 2.6f : 1.2f));
            g.drawRoundRect(box.x, box.y, box.width, box.height, 14, 14);

            g.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g.getFontMetrics();
            String so = String.valueOf(maRang);
            g.setColor(tinhTrang != null && canChuTrang(tinhTrang) ? Color.WHITE : INK);
            g.drawString(so, box.x + (box.width - fm.stringWidth(so)) / 2,
                    box.y + (box.height - fm.getHeight()) / 2 + fm.getAscent());
        }
    }
}