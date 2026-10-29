package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Bộ phào chỉ (style) dùng chung cho các panel: màu, phông, cách tạo thẻ,
 * nút, hàng nhãn + ô nhập, bảng, định dạng ngày/tiền và hộp thoại thông báo.
 * Gom về một chỗ để các panel giữ chỉ phần bố cục riêng của mình.
 */
public final class GiaoDienUtil {

    // ---- Màu chủ đạo của ứng dụng ----
    public static final Color INK = new Color(32, 53, 68);          // chữ chính
    public static final Color MUTED = new Color(119, 139, 151);     // chữ phụ
    public static final Color TEAL = new Color(26, 151, 151);       // xanh nhấn
    public static final Color LINE = new Color(226, 234, 237);      // đường viền
    public static final Color CANVAS = new Color(244, 248, 249);    // nền vùng làm việc
    public static final Color NEN_O_CHON = new Color(231, 246, 245); // nền ô được chọn
    public static final Color DANGER = new Color(214, 69, 80);
    public static final Color RED = new Color(200, 70, 70);
    public static final Color ORANGE = new Color(177, 125, 47);
    public static final Color BLUE = new Color(51, 91, 177);

    // ---- Phông dùng chung ----
    public static final Font CHU = new Font("Arial", Font.PLAIN, 12);
    public static final Font CHU_DAM = new Font("Arial", Font.BOLD, 12);
    public static final Font NHO = new Font("Arial", Font.PLAIN, 11);
    public static final Font NHO_DAM = new Font("Arial", Font.BOLD, 11);
    public static final Font NHAN = new Font("Arial", Font.BOLD, 10);

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter GIO = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter NGAY_GIO = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final NumberFormat TIEN = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private GiaoDienUtil() {
    }

    // ------------------------------------------------------------------
    // Tạo thành phần
    // ------------------------------------------------------------------

    /** Thẻ nền trắng dựng dọc (BoxLayout Y) bọc khung viền cách đệm 16/18. */
    public static JPanel taoThe() {
        JPanel the = new JPanel();
        the.setLayout(new BoxLayout(the, BoxLayout.Y_AXIS));
        trangTriThe(the);
        return the;
    }

    /** Thẻ nền trắng cho card có bố cục tự chọn (ví dụ BorderLayout). */
    public static void trangTriThe(JPanel the) {
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(16, 18, 16, 18)));
    }

    /** Tiêu đề thẻ (Arial 15 bold). */
    public static JLabel taoTieuDe(String text) {
        JLabel tieuDe = new JLabel(text);
        tieuDe.setFont(new Font("Arial", Font.BOLD, 15));
        tieuDe.setForeground(INK);
        tieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tieuDe;
    }

    /** Nhãn phụ nhỏ (Arial 11 bold, màu nhạt). */
    public static JLabel taoNhanPhu(String text) {
        JLabel nhan = new JLabel(text);
        nhan.setFont(NHO_DAM);
        nhan.setForeground(MUTED);
        return nhan;
    }

    /** Nút lệnh: primary màu xanh nhấn, còn lại nền trắng viền xanh. */
    public static JButton taoNut(String text, boolean chinh) {
        JButton nut = new JButton(text);
        nut.setFont(CHU_DAM);
        nut.setFocusPainted(false);
        nut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nut.setOpaque(true);
        if (chinh) {
            nut.setBackground(TEAL);
            nut.setForeground(Color.WHITE);
            nut.setBorder(new EmptyBorder(8, 16, 8, 16));
            nut.setBorderPainted(false);
        } else {
            nut.setBackground(Color.WHITE);
            nut.setForeground(INK);
            nut.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE), new EmptyBorder(7, 15, 7, 15)));
        }
        return nut;
    }

    /** Hàng "nhãn + ô nhập": nhãn rộng 110 trái, ô nhập giãn hết phần còn lại. */
    public static JPanel taoHang(String nhan, Component o) {
        tuyChinhO(o);
        JLabel tieuDeNhan = new JLabel(nhan);
        tieuDeNhan.setFont(CHU);
        tieuDeNhan.setForeground(INK);
        tieuDeNhan.setPreferredSize(new Dimension(110, 32));

        JPanel hang = new JPanel(new java.awt.BorderLayout(8, 0));
        hang.setOpaque(false);
        hang.setAlignmentX(Component.LEFT_ALIGNMENT);
        hang.setBorder(new EmptyBorder(0, 0, 8, 0));
        hang.add(tieuDeNhan, java.awt.BorderLayout.WEST);
        hang.add(o, java.awt.BorderLayout.CENTER);
        hang.setMaximumSize(new Dimension(Integer.MAX_VALUE, o instanceof javax.swing.JScrollPane ? 82 : 42));
        return hang;
    }

    /** Phào ô nhập: ô chữ style theo chuẩn chung; component khác chỉ cần phông. */
    public static void tuyChinhO(Component o) {
        if (o instanceof JTextField truong) {
            truong.setFont(CHU);
            truong.setForeground(INK);
            truong.setPreferredSize(new Dimension(160, 32));
            truong.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(LINE), new EmptyBorder(0, 8, 0, 8)));
        } else {
            o.setFont(CHU);
        }
    }

    /** Phào bảng dữ liệu: cao dòng, phông, màu vùng chọn, tiêu đề cột. */
    public static void tuyChinhBang(javax.swing.JTable bang) {
        bang.setRowHeight(30);
        bang.setFont(CHU);
        bang.setForeground(INK);
        bang.setShowVerticalLines(false);
        bang.setGridColor(LINE);
        bang.setFillsViewportHeight(true);
        bang.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        bang.setSelectionBackground(NEN_O_CHON);
        bang.setSelectionForeground(INK);
        bang.getTableHeader().setFont(NHO_DAM);
        bang.getTableHeader().setReorderingAllowed(false);
    }

    // ------------------------------------------------------------------
    // Định dạng dữ liệu hiển thị
    // ------------------------------------------------------------------

    public static String dinhDangNgay(LocalDate ngay) {
        return ngay == null ? "" : ngay.format(NGAY);
    }

    public static String dinhDangGio(LocalTime gio) {
        return gio == null ? "" : gio.format(GIO);
    }

    public static String dinhDangNgayGio(LocalDateTime thoiDiem) {
        return thoiDiem == null ? "" : thoiDiem.format(NGAY_GIO);
    }

    /** Định dạng tiền kiểu Việt Nam: 1.500.000. */
    public static String dinhDangTien(double soTien) {
        return TIEN.format(soTien);
    }

    /**
     * Trạng thái DB lưu không dấu ("Dang kham"...) nhưng giao diện hiện có dấu
     * ("Đang khám"...). Trạng thái lạ hiển thị nguyên bản.
     */
    public static String hienThiTrangThai(String trangThaiDb) {
        if (trangThaiDb == null) {
            return "";
        }
        return switch (trangThaiDb) {
            case "Cho xac nhan" -> "Chờ xác nhận";
            case "Da xac nhan" -> "Đã xác nhận";
            case "Da check-in" -> "Đã check-in";
            case "Dang kham" -> "Đang khám";
            case "Hoan thanh" -> "Hoàn thành";
            case "Vang" -> "Vắng";
            case "Huy" -> "Hủy";
            default -> trangThaiDb;
        };
    }

    // ------------------------------------------------------------------
    // Hộp thoại thông báo
    // ------------------------------------------------------------------

    public static void thongBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void canhBao(Component cha, String noiDung) {
        JOptionPane.showMessageDialog(cha, noiDung, "Dữ liệu không hợp lệ", JOptionPane.WARNING_MESSAGE);
    }

    /** Báo lỗi truy vấn CSDL kèm stack trace để dễ phát hiện lúc phát triển. */
    public static void baoLoi(Component cha, SQLException e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(cha,
                "Lỗi CSDL: " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
