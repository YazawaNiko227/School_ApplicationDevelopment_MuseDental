package util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

/**
 * Xuat hoa don Nha khoa Muse ra file PDF bang Apache PDFBox 3.x.
 * Moi dong duoc viet tai toa do (x, y); xuong dong thi giam y.
 */
public class HoaDonPdfExporter {
    private static final float LE_TRAI = 50;
    private static final float LE_PHAI = 545;
    private static final float CHIEU_CAO_DONG = 18;
    private static final DecimalFormat DINH_DANG_TIEN = new DecimalFormat("#,##0");

    private final PDDocument taiLieu;
    private final PDType0Font fontThuong;
    private final PDType0Font fontDam;
    private PDPageContentStream noiDung;
    private float y;

    private HoaDonPdfExporter(PDDocument taiLieu, PDType0Font fontThuong,
            PDType0Font fontDam) throws IOException {
        this.taiLieu = taiLieu;
        this.fontThuong = fontThuong;
        this.fontDam = fontDam;
        taoTrangMoi();
    }

    /**
     * Ham duoc goi tu GUI de xuat hoa don.
     * Moi phan tu cua dichVu / thuoc la mang 5 chuoi:
     * [ma, ten, don gia, so luong, thanh tien].
     */
    public static void export(Path file, String maHoaDon, LocalDate ngayLap,
            String maBenhNhan, String hoTen, String maChanDoan,
            String ketLuan, String phuongThuc, String trangThai,
            List<String[]> dichVu, List<String[]> thuoc,
            double tienDichVu, double tienThuoc, double tong) throws IOException {

        // Su dung font Arial de hien thi tieng Viet co dau.
        File arial = new File("C:/Windows/Fonts/arial.ttf");
        File arialDam = new File("C:/Windows/Fonts/arialbd.ttf");
        if (!arial.isFile()) {
            throw new IOException("Khong tim thay font Arial tai C:/Windows/Fonts/arial.ttf");
        }

        try (PDDocument taiLieu = new PDDocument()) {
            PDType0Font fontThuong = PDType0Font.load(taiLieu, arial);
            PDType0Font fontDam = arialDam.isFile()
                    ? PDType0Font.load(taiLieu, arialDam)
                    : fontThuong;
            HoaDonPdfExporter pdf = new HoaDonPdfExporter(taiLieu, fontThuong, fontDam);

            pdf.vietTieuDe();
            pdf.vietThongTin("Mã hóa đơn:", maHoaDon);
            pdf.vietThongTin("Ngày lập:", ngayLap.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            pdf.vietThongTin("Bệnh nhân:", hoTen + " (" + maBenhNhan + ")");
            pdf.vietThongTin("Lần khám:", maChanDoan);
            pdf.vietThongTin("Chẩn đoán:", ketLuan);
            pdf.vietThongTin("Phương thức:", phuongThuc);
            pdf.vietThongTin("Trạng thái:", doiTrangThai(trangThai));
            pdf.xuongDong();

            pdf.vietBang("TIỀN DỊCH VỤ", dichVu);
            pdf.vietBang("TIỀN THUỐC", thuoc);
            pdf.vietDuoiHoaDon(tienDichVu, tienThuoc, tong);

            pdf.noiDung.close();
            taiLieu.save(file.toFile());
        }
    }

    private void vietTieuDe() throws IOException {
        vietChu("NHA KHOA MUSE", LE_TRAI, 16, true);
        xuongDong();
        vietChu("Phần mềm quản lý nha khoa", LE_TRAI, 10, false);
        xuongDong();
        xuongDong();
        vietGiua("HÓA ĐƠN THANH TOÁN", 20);
        xuongDong();
        xuongDong();
    }

    private void vietThongTin(String nhan, String giaTri) throws IOException {
        vietChu(nhan, LE_TRAI, 11, true);
        vietChu(catChu(giaTri, 11, 390), 150, 11, false);
        xuongDong();
    }

    private void vietBang(String tieuDe, List<String[]> danhSach) throws IOException {
        vietChu(tieuDe, LE_TRAI, 12, true);
        xuongDong();

        vietChu("Mã", 50, 10, true);
        vietChu("Tên", 105, 10, true);
        vietPhai("Đơn giá (đ)", 360, 10, true);
        vietPhai("SL", 415, 10, true);
        vietPhai("Thành tiền (đ)", 545, 10, true);
        keNgang();
        xuongDong();

        if (danhSach == null || danhSach.isEmpty()) {
            vietChu("Không có", LE_TRAI, 10, false);
            xuongDong();
        } else {
            for (String[] dong : danhSach) {
                if (dong == null || dong.length < 5) {
                    throw new IOException("Mỗi dòng hóa đơn phải có đủ 5 cột.");
                }
                vietChu(catChu(dong[0], 10, 50), 50, 10, false);
                vietChu(catChu(dong[1], 10, 175), 105, 10, false);
                vietPhai(dong[2], 360, 10, false);
                vietPhai(dong[3], 415, 10, false);
                vietPhai(dong[4], 545, 10, false);
                xuongDong();
            }
        }
        xuongDong();
    }

    private void vietDuoiHoaDon(double tienDichVu, double tienThuoc,
            double tong) throws IOException {
        keNgang();
        xuongDong();
        vietDongTien("Cộng tiền dịch vụ:", tienDichVu, 11, false);
        vietDongTien("Cộng tiền thuốc:", tienThuoc, 11, false);
        vietDongTien("TỔNG THANH TOÁN:", tong, 14, true);
        xuongDong();
        xuongDong();
        vietGiua("Cảm ơn quý khách đã sử dụng dịch vụ tại Nha khoa Muse!", 11);
    }

    private void vietDongTien(String nhan, double soTien, int coChu,
            boolean inDam) throws IOException {
        vietChu(nhan, 300, coChu, inDam);
        vietPhai(DINH_DANG_TIEN.format(soTien) + " đ", LE_PHAI, coChu, inDam);
        xuongDong();
    }

    // Viet chu tai toa do x va y hien tai.
    private void vietChu(String chu, float x, float coChu, boolean inDam) throws IOException {
        String noiDungChu = lamSach(chu);
        noiDung.beginText();
        noiDung.setFont(inDam ? fontDam : fontThuong, coChu);
        noiDung.newLineAtOffset(x, y);
        noiDung.showText(noiDungChu);
        noiDung.endText();
    }

    // Can phai bang cach lay vi tri ben phai tru di do rong cua chu.
    private void vietPhai(String chu, float xPhai, float coChu,
            boolean inDam) throws IOException {
        String sach = lamSach(chu);
        vietChu(sach, xPhai - doRong(sach, coChu, inDam), coChu, inDam);
    }

    private void vietGiua(String chu, float coChu) throws IOException {
        String sach = lamSach(chu);
        float x = (PDRectangle.A4.getWidth() - doRong(sach, coChu, true)) / 2;
        vietChu(sach, x, coChu, true);
    }

    private float doRong(String chu, float coChu, boolean inDam) throws IOException {
        PDType0Font font = inDam ? fontDam : fontThuong;
        return font.getStringWidth(chu) * coChu / 1000;
    }

    private static String lamSach(String chu) {
        return chu == null ? "" : chu.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
    }

    // Neu chu dai hon o cho phep, cat ngan va them dau ba cham.
    private String catChu(String chu, float coChu, float rongToiDa) throws IOException {
        String ketQua = lamSach(chu);
        if (doRong(ketQua, coChu, false) <= rongToiDa) {
            return ketQua;
        }
        while (!ketQua.isEmpty() && doRong(ketQua + "...", coChu, false) > rongToiDa) {
            ketQua = ketQua.substring(0, ketQua.length() - 1);
        }
        return ketQua + "...";
    }

    private void keNgang() throws IOException {
        noiDung.moveTo(LE_TRAI, y + 12);
        noiDung.lineTo(LE_PHAI, y + 12);
        noiDung.stroke();
    }

    private void xuongDong() throws IOException {
        y -= CHIEU_CAO_DONG;
        if (y < 60) {
            taoTrangMoi();
        }
    }

    private void taoTrangMoi() throws IOException {
        if (noiDung != null) {
            noiDung.close();
        }
        PDPage trang = new PDPage(PDRectangle.A4);
        taiLieu.addPage(trang);
        noiDung = new PDPageContentStream(taiLieu, trang);
        y = PDRectangle.A4.getHeight() - 60;
    }

    private static String doiTrangThai(String trangThai) {
        if ("Da thanh toan".equals(trangThai))
            return "Đã thanh toán";
        if ("Dang tra gop".equals(trangThai))
            return "Đang trả góp";
        return trangThai;
    }
}
