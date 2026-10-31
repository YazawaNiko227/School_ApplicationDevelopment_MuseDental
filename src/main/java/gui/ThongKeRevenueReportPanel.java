package gui;

import com.toedter.calendar.JDateChooser;
import util.GiaoDienUtil;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * Panel "Báo cáo doanh thu" (menu THỐNG KÊ).
 *
 * Không cần bảng mới trong SQL: doanh thu được tổng hợp từ
 * HoaDon + ChiTietHoaDon (tiền dịch vụ) + ChiTietHoaDonThuoc (tiền thuốc).
 *
 * Luồng xử lý:
 * 1. Chọn khoảng ngày (theo HoaDon.NgayLap) và cách nhóm.
 * 2. Bấm "Thống kê" -> bảng kết quả, biểu đồ thanh ngang và các thẻ tổng hợp.
 *
 * Chỉ tính hóa đơn "Đã thanh toán" và "Đang trả góp"
 * (hóa đơn "Chưa thanh toán" chưa phải doanh thu).
 *
 * Hiện tại dùng dữ liệu mẫu trong bộ nhớ (khớp file SQL). Chỗ cần nối DAO ghi
 * "TODO DAO".
 */
public class ThongKeRevenueReportPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color BLUE = new Color(51, 91, 177);
    private static final Color ORANGE = new Color(177, 125, 47);

    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DecimalFormat PHAN_TRAM = new DecimalFormat("0.0");

    // Trạng thái thanh toán (DB lưu không dấu).
    private static final String DA_THANH_TOAN = "Đã thanh toán";
    private static final String DANG_TRA_GOP = "Đang trả góp";
    private static final String CHUA_THANH_TOAN = "Chưa thanh toán";

    // Loại dòng chi tiết.
    private static final String LOAI_DICH_VU = "Dịch vụ";
    private static final String LOAI_THUOC = "Thuốc";

    // Các cách nhóm báo cáo.
    private static final String THEO_NGAY = "Theo ngày";
    private static final String THEO_THANG = "Theo tháng";
    private static final String THEO_MUC = "Theo dịch vụ / thuốc";
    private static final String THEO_PHUONG_THUC = "Theo phương thức thanh toán";

    // Chỉ số cột của hóa đơn (Object[]).
    private static final int H_MA = 0;
    private static final int H_NGAY = 1; // LocalDate
    private static final int H_TRANG_THAI = 2;
    private static final int H_PHUONG_THUC = 3;

    // Chỉ số cột của dòng chi tiết (Object[]).
    private static final int C_MA_HD = 0;
    private static final int C_LOAI = 1;
    private static final int C_TEN = 2;
    private static final int C_THANH_TIEN = 3; // Double

    // Dữ liệu mẫu. TODO DAO: HoaDonDAO.layHoaDonTrongKhoang(tuNgay, denNgay)
    // {MaHoaDon, NgayLap, TrangThaiThanhToan, PhuongThucThanhToan}
    private final List<Object[]> dsHoaDon = new ArrayList<>();
    // {MaHoaDon, loại, tên dịch vụ/thuốc, thành tiền = SoLuong * DonGiaTaiThoiDiem}
    private final List<Object[]> dsChiTiet = new ArrayList<>();

    // ----- Bộ lọc -----
    private final JDateChooser chonTuNgay = new JDateChooser();
    private final JDateChooser chonDenNgay = new JDateChooser();
    private final JComboBox<String> cboNhom = new JComboBox<>(
            new String[] { THEO_THANG, THEO_NGAY, THEO_MUC, THEO_PHUONG_THUC });

    // ----- Thẻ tổng hợp -----
    private final JLabel lblTongDoanhThu = new JLabel("0 đ");
    private final JLabel lblSoHoaDon = new JLabel("0");
    private final JLabel lblTienDichVu = new JLabel("0 đ");
    private final JLabel lblTienThuoc = new JLabel("0 đ");

    // ----- Kết quả -----
    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[] { "Nhóm", "Số hóa đơn", "Doanh thu (đ)", "Tỷ lệ" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(tableModel);
    // Biểu đồ: mỗi nhóm một dòng, thanh ngang (JProgressBar) thể hiện tỷ lệ %.
    private final JPanel pnlBieuDo = new JPanel();

    public ThongKeRevenueReportPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        taoDuLieuMau();

        add(createSummaryPanel(), BorderLayout.NORTH);
        add(createResultCard(), BorderLayout.CENTER);

        // Mặc định thống kê từ đầu năm đến hôm nay.
        GiaoDienUtil.datNgayDuocChon(chonTuNgay, LocalDate.now().withDayOfYear(1));
        GiaoDienUtil.datNgayDuocChon(chonDenNgay, LocalDate.now());
        thongKe();
    }

    // =====================================================================
    // DỮ LIỆU MẪU (khớp file SQL)
    // =====================================================================

    private void taoDuLieuMau() {
        dsHoaDon.add(new Object[] { "HD001", LocalDate.of(2026, 9, 1), DA_THANH_TOAN, "Tiền mặt" });
        dsHoaDon.add(new Object[] { "HD002", LocalDate.of(2026, 9, 5), DA_THANH_TOAN, "Chuyển khoản" });
        dsHoaDon.add(new Object[] { "HD003", LocalDate.of(2026, 9, 10), DANG_TRA_GOP, "Trả góp" });
        dsHoaDon.add(new Object[] { "HD004", LocalDate.of(2026, 10, 1), CHUA_THANH_TOAN, "Chưa chọn" });

        // ChiTietHoaDon (dịch vụ)
        dsChiTiet.add(new Object[] { "HD001", LOAI_DICH_VU, "Lấy cao răng", 200000.0 });
        dsChiTiet.add(new Object[] { "HD001", LOAI_DICH_VU, "Trám răng composite", 300000.0 });
        dsChiTiet.add(new Object[] { "HD002", LOAI_DICH_VU, "Tẩy trắng răng", 2000000.0 });
        dsChiTiet.add(new Object[] { "HD003", LOAI_DICH_VU, "Bọc răng sứ", 7000000.0 });
        dsChiTiet.add(new Object[] { "HD004", LOAI_DICH_VU, "Trám răng composite", 300000.0 });
        // ChiTietHoaDonThuoc (thuốc)
        dsChiTiet.add(new Object[] { "HD001", LOAI_THUOC, "Amoxicillin 500mg", 75000.0 });
        dsChiTiet.add(new Object[] { "HD001", LOAI_THUOC, "Paracetamol 500mg", 15000.0 });
    }

    // =====================================================================
    // GIAO DIỆN
    // =====================================================================

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        panel.setOpaque(false);
        panel.add(createSummaryCard("TỔNG DOANH THU", lblTongDoanhThu, TEAL));
        panel.add(createSummaryCard("SỐ HÓA ĐƠN", lblSoHoaDon, BLUE));
        panel.add(createSummaryCard("TIỀN DỊCH VỤ", lblTienDichVu, INK));
        panel.add(createSummaryCard("TIỀN THUỐC", lblTienThuoc, ORANGE));
        return panel;
    }

    private JPanel createSummaryCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 16, 12, 16)));
        card.setPreferredSize(new Dimension(220, 78));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setForeground(color);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueLabel);
        return card;
    }

    private JPanel createResultCard() {
        JPanel card = new JPanel(new BorderLayout(0, 12));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));

        card.add(createFilterRow(), BorderLayout.NORTH);

        // Biểu đồ ở trên, bảng ở dưới.
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(createBieuDo(), BorderLayout.NORTH);
        body.add(createTable(), BorderLayout.CENTER);
        card.add(body, BorderLayout.CENTER);

        JLabel note = new JLabel("Chỉ tính hóa đơn \"Đã thanh toán\" và \"Đang trả góp\". "
                + "Doanh thu = tiền dịch vụ + tiền thuốc trên hóa đơn, tính theo ngày lập.");
        note.setFont(new Font("Arial", Font.ITALIC, 11));
        note.setForeground(MUTED);
        card.add(note, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createFilterRow() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row.setOpaque(false);

        GiaoDienUtil.cauHinhChonNgay(chonTuNgay, "dd/MM/yyyy");
        GiaoDienUtil.cauHinhChonNgay(chonDenNgay, "dd/MM/yyyy");
        chonTuNgay.setPreferredSize(new Dimension(140, 32));
        chonDenNgay.setPreferredSize(new Dimension(140, 32));
        chonTuNgay.setToolTipText("Chọn ngày bắt đầu từ lịch");
        chonDenNgay.setToolTipText("Chọn ngày kết thúc từ lịch");
        cboNhom.setFont(new Font("Arial", Font.PLAIN, 12));

        JButton btnThongKe = createButton("Thống kê", true);
        JButton btnLamMoi = createButton("Làm mới", false);
        btnThongKe.addActionListener(event -> thongKe());
        btnLamMoi.addActionListener(event -> lamMoi());

        row.add(createLabel("Từ ngày"));
        row.add(chonTuNgay);
        row.add(createLabel("Đến ngày"));
        row.add(chonDenNgay);
        row.add(createLabel("Nhóm"));
        row.add(cboNhom);
        row.add(btnThongKe);
        row.add(btnLamMoi);
        return row;
    }

    // Khung chứa biểu đồ: các dòng xếp dọc, nhiều quá thì có thanh cuộn.
    private JScrollPane createBieuDo() {
        pnlBieuDo.setLayout(new BoxLayout(pnlBieuDo, BoxLayout.Y_AXIS));
        pnlBieuDo.setBackground(Color.WHITE);
        pnlBieuDo.setBorder(new EmptyBorder(8, 10, 8, 10));

        JScrollPane scroll = new JScrollPane(pnlBieuDo);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.setPreferredSize(new Dimension(100, 160));
        return scroll;
    }

    private JScrollPane createTable() {
        table.setRowHeight(30);
        table.setFont(new Font("Arial", Font.PLAIN, 12));
        table.setForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(LINE);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setReorderingAllowed(false);

        // Cột số căn phải.
        DefaultTableCellRenderer right = new DefaultTableCellRenderer();
        right.setHorizontalAlignment(SwingConstants.RIGHT);
        for (int i = 1; i <= 3; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(right);
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(Color.WHITE);
        return scroll;
    }

    // =====================================================================
    // XỬ LÝ NGHIỆP VỤ
    // =====================================================================

    /** Đọc bộ lọc, tổng hợp doanh thu rồi cập nhật bảng, biểu đồ và thẻ tổng. */
    private void thongKe() {
        LocalDate tuNgay = GiaoDienUtil.layNgayDaChon(chonTuNgay);
        LocalDate denNgay = GiaoDienUtil.layNgayDaChon(chonDenNgay);
        if (tuNgay == null || denNgay == null) {
            showWarning("Vui lòng chọn đầy đủ Từ ngày và Đến ngày.");
            return;
        }
        if (tuNgay.isAfter(denNgay)) {
            showWarning("Từ ngày không được sau Đến ngày.");
            return;
        }

        String nhom = (String) cboNhom.getSelectedItem();
        // nhóm -> tiền, nhóm -> các mã hóa đơn thuộc nhóm (để đếm số hóa đơn)
        Map<String, Double> tienTheoNhom = new TreeMap<>();
        Map<String, Set<String>> hoaDonTheoNhom = new TreeMap<>();
        Set<String> tatCaHoaDon = new HashSet<>();
        double tongDichVu = 0;
        double tongThuoc = 0;

        // TODO DAO: thay vòng lặp bằng query GROUP BY trong HoaDonDAO.
        for (Object[] ct : dsChiTiet) {
            Object[] hd = timHoaDon((String) ct[C_MA_HD]);
            if (hd == null || !tinhDoanhThu(hd, tuNgay, denNgay)) {
                continue;
            }
            double tien = (Double) ct[C_THANH_TIEN];
            String khoa = taoKhoaNhom(nhom, hd, ct);

            tienTheoNhom.merge(khoa, tien, Double::sum);
            hoaDonTheoNhom.computeIfAbsent(khoa, k -> new HashSet<>()).add((String) hd[H_MA]);
            tatCaHoaDon.add((String) hd[H_MA]);

            if (LOAI_DICH_VU.equals(ct[C_LOAI])) {
                tongDichVu += tien;
            } else {
                tongThuoc += tien;
            }
        }
        double tong = tongDichVu + tongThuoc;

        // Cập nhật thẻ tổng hợp.
        lblTongDoanhThu.setText(TIEN.format(tong) + " đ");
        lblSoHoaDon.setText(String.valueOf(tatCaHoaDon.size()));
        lblTienDichVu.setText(TIEN.format(tongDichVu) + " đ");
        lblTienThuoc.setText(TIEN.format(tongThuoc) + " đ");

        // Cập nhật bảng kết quả.
        tableModel.setRowCount(0);
        for (Map.Entry<String, Double> entry : tienTheoNhom.entrySet()) {
            double tyLe = tong == 0 ? 0 : entry.getValue() * 100 / tong;
            tableModel.addRow(new Object[] {
                    entry.getKey(),
                    hoaDonTheoNhom.get(entry.getKey()).size(),
                    TIEN.format(entry.getValue()),
                    PHAN_TRAM.format(tyLe) + "%" });
        }
        veBieuDo(tienTheoNhom, tong);

        if (tienTheoNhom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có doanh thu trong khoảng ngày đã chọn.",
                    "Thông báo", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Vẽ biểu đồ: mỗi nhóm một dòng gồm tên nhóm và một thanh ngang theo tỷ lệ %.
     */
    private void veBieuDo(Map<String, Double> tienTheoNhom, double tong) {
        pnlBieuDo.removeAll();
        for (Map.Entry<String, Double> entry : tienTheoNhom.entrySet()) {
            int phanTram = tong == 0 ? 0 : (int) Math.round(entry.getValue() * 100 / tong);

            JLabel lblTen = new JLabel(entry.getKey());
            lblTen.setFont(new Font("Arial", Font.PLAIN, 12));
            lblTen.setForeground(INK);
            lblTen.setPreferredSize(new Dimension(190, 26));

            JProgressBar thanh = new JProgressBar(0, 100);
            thanh.setValue(phanTram);
            thanh.setStringPainted(true);
            thanh.setString(TIEN.format(entry.getValue()) + " đ (" + phanTram + "%)");
            thanh.setForeground(TEAL);
            thanh.setBackground(new Color(244, 247, 250));
            thanh.setBorderPainted(false);

            JPanel dong = new JPanel(new BorderLayout(10, 0));
            dong.setOpaque(false);
            dong.setBorder(new EmptyBorder(3, 0, 3, 0));
            dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
            dong.add(lblTen, BorderLayout.WEST);
            dong.add(thanh, BorderLayout.CENTER);
            pnlBieuDo.add(dong);
        }
        pnlBieuDo.revalidate();
        pnlBieuDo.repaint();
    }

    private void lamMoi() {
        GiaoDienUtil.datNgayDuocChon(chonTuNgay, LocalDate.now().withDayOfYear(1));
        GiaoDienUtil.datNgayDuocChon(chonDenNgay, LocalDate.now());
        cboNhom.setSelectedIndex(0);
        thongKe();
    }

    // =====================================================================
    // HÀM HỖ TRỢ
    // =====================================================================

    // Hóa đơn có được tính vào doanh thu không (đúng trạng thái và trong khoảng
    // ngày).
    private boolean tinhDoanhThu(Object[] hd, LocalDate tuNgay, LocalDate denNgay) {
        String trangThai = (String) hd[H_TRANG_THAI];
        if (!DA_THANH_TOAN.equals(trangThai) && !DANG_TRA_GOP.equals(trangThai)) {
            return false;
        }
        LocalDate ngay = (LocalDate) hd[H_NGAY];
        return !ngay.isBefore(tuNgay) && !ngay.isAfter(denNgay);
    }

    // Khóa nhóm: tháng "2026-09", ngày "2026-09-01", tên dịch vụ/thuốc, phương
    // thức.
    private String taoKhoaNhom(String nhom, Object[] hd, Object[] ct) {
        LocalDate ngay = (LocalDate) hd[H_NGAY];
        if (THEO_NGAY.equals(nhom)) {
            return ngay.toString();
        } else if (THEO_MUC.equals(nhom)) {
            return ct[C_LOAI] + ": " + ct[C_TEN];
        } else if (THEO_PHUONG_THUC.equals(nhom)) {
            return (String) hd[H_PHUONG_THUC];
        }
        return ngay.toString().substring(0, 7);
    }

    private Object[] timHoaDon(String maHoaDon) {
        for (Object[] hd : dsHoaDon) {
            if (hd[H_MA].equals(maHoaDon)) {
                return hd;
            }
        }
        return null;
    }

    private void showWarning(String message) {
        JOptionPane.showMessageDialog(this, message, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    // ----- Tạo thành phần giao diện -----

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setForeground(INK);
        return label;
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