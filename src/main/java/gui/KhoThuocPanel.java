package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class KhoThuocPanel extends JPanel {

    // Component tầng trên (Tồn kho)
    private JTabbedPane tabbedPane;
    private JTextField txtTimKiemVatTu;
    private JComboBox<String> cbLoaiVatTu;
    private JButton btnTimKiem, btnLapPhieuNhapMoi;
    private JTable tableTonKho;
    private DefaultTableModel modelTonKho;

    // Component tầng dưới (Lập phiếu nhập kho)
    private JComboBox<String> cbNhaCungCap;
    private JTextField txtSoHD, txtNgayNhap;
    private JTable tableNhapKho;
    private DefaultTableModel modelNhapKho;
    private JButton btnThemDong, btnLuuPhieu, btnHuyBo;
    private JLabel lblTongTien;

    public KhoThuocPanel() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----------------------------------------------------
        // 1. TIÊU ĐỀ CHÍNH
        // ----------------------------------------------------
        JLabel lblHeader = new JLabel("Quan ly Kho thuoc & Vat tu Y te");
        lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblHeader, BorderLayout.NORTH);

        // Tạo Panel chính chứa cả 2 phần (dùng JSplitPane để co giãn 2 phần trên/dưới)
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setResizeWeight(0.5);

        // ----------------------------------------------------
        // 2. TẦNG TRÊN: TAB & BẢNG TỒN KHO THỜI GIAN THỰC
        // ----------------------------------------------------
        JPanel panelTopContainer = new JPanel(new BorderLayout(5, 5));
        tabbedPane = new JTabbedPane();

        JPanel panelTonKho = new JPanel(new BorderLayout(5, 5));

        // 2.1 Thanh lọc & Tìm kiếm (Filter Bar)
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        pnlFilter.add(new JLabel("Tim kiem vat tu"));
        txtTimKiemVatTu = new JTextField("Ten vat tu, hoat chat...", 15);
        pnlFilter.add(txtTimKiemVatTu);

        pnlFilter.add(new JLabel("Loai vat tu:"));
        cbLoaiVatTu = new JComboBox<>(new String[] { "Tat ca", "Thuoc", "Vat tu y te" });
        pnlFilter.add(cbLoaiVatTu);

        btnTimKiem = new JButton("Tim kiem");
        btnLapPhieuNhapMoi = new JButton("+ Lap phieu nhap kho moi");
        pnlFilter.add(btnTimKiem);
        pnlFilter.add(btnLapPhieuNhapMoi);

        panelTonKho.add(pnlFilter, BorderLayout.NORTH);

        // 2.2 Bảng dữ liệu Tồn kho
        String[] colsTonKho = { "Ten vat tu", "Don vi", "SL ton", "Nguong toi thieu", "Han su dung", "Nha CC",
                "Trang thai", "Thao tac" };
        Object[][] dataTonKho = {
                { "Kim tiem 25G", "Cai", "850", "200", "12/2027", "Cty A", "OK", "Xem chi tiet" },
                { "Bong gac vo trung", "Goi", "320", "100", "06/2027", "Cty B", "OK", "Xem chi tiet" },
                { "Gang tay khong bot (M)", "Doi", "95", "200", "09/2026", "Cty A", "! CANH BAO THAP", "Xem chi tiet" },
                { "Composite A2", "Tuyp", "12", "20", "03/2027", "Cty C", "! CANH BAO THAP", "Xem chi tiet" },
                { "Thuoc te NaCl 0.9%", "Chai", "450", "50", "08/2027", "Cty D", "OK", "Xem chi tiet" },
                { "Cao xu nieng rang (lot)", "Tui", "8", "30", "01/2027", "Cty C", "! CANH BAO THAP", "Xem chi tiet" },
                { "Mask y te 3 lop", "Cai", "1200", "500", "05/2028", "Cty B", "OK", "Xem chi tiet" }
        };
        modelTonKho = new DefaultTableModel(dataTonKho, colsTonKho);
        tableTonKho = new JTable(modelTonKho);
        panelTonKho.add(new JScrollPane(tableTonKho), BorderLayout.CENTER);

        // Add vào TabbedPane
        tabbedPane.addTab("Ton kho thoi gian thuc", panelTonKho);
        tabbedPane.addTab("Nhap kho", new JPanel());
        tabbedPane.addTab("Kiem ke & Dieu chinh", new JPanel());
        tabbedPane.addTab("Nhat ky lich su", new JPanel());

        panelTopContainer.add(tabbedPane, BorderLayout.CENTER);

        // ----------------------------------------------------
        // 3. TẦNG DƯỚI: LẬP PHIẾU NHẬP KHO TỪ NHÀ CUNG CẤP
        // ----------------------------------------------------
        JPanel panelBottomContainer = new JPanel(new BorderLayout(5, 5));
        panelBottomContainer.setBorder(BorderFactory.createTitledBorder("LAP PHIEU NHAP KHO TU NHA CUNG CAP"));

        // 3.1 Form thông tin nhà cung cấp & hóa đơn
        JPanel pnlPhieuHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        pnlPhieuHeader.add(new JLabel("Nha cung cap:"));
        cbNhaCungCap = new JComboBox<>(new String[] { "Cty TNHH Y te A", "Cty B", "Cty C" });
        pnlPhieuHeader.add(cbNhaCungCap);

        pnlPhieuHeader.add(new JLabel("So HD/PO:"));
        txtSoHD = new JTextField("PO2026-0089", 10);
        pnlPhieuHeader.add(txtSoHD);

        pnlPhieuHeader.add(new JLabel("Ngay nhap:"));
        txtNgayNhap = new JTextField("27/09/2026", 8);
        pnlPhieuHeader.add(txtNgayNhap);

        panelBottomContainer.add(pnlPhieuHeader, BorderLayout.NORTH);

        // 3.2 Bảng chi tiết mặt hàng nhập kho
        String[] colsNhapKho = { "Ten vat tu", "Don vi", "So luong nhap", "Don gia nhap", "Thanh tien", "Han SD",
                "Xoa" };
        Object[][] dataNhapKho = {
                { "Gang tay khong bot (M)", "Doi", "500", "3.500 VND", "1.750.000 VND", "09/2027", "true" },
                { "Composite A2", "Tuyp", "30", "45.000 VND", "1.350.000 VND", "12/2027", "true" }
        };
        modelNhapKho = new DefaultTableModel(dataNhapKho, colsNhapKho);
        tableNhapKho = new JTable(modelNhapKho);
        panelBottomContainer.add(new JScrollPane(tableNhapKho), BorderLayout.CENTER);

        // 3.3 Bottom Action Bar (Footer)
        JPanel pnlFooter = new JPanel(new BorderLayout());
        btnThemDong = new JButton("+ Them dong vat tu");
        pnlFooter.add(btnThemDong, BorderLayout.WEST);

        JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        lblTongTien = new JLabel("Tong tien: 3.100.000 VND  ");
        lblTongTien.setFont(new Font("Arial", Font.BOLD, 13));
        btnLuuPhieu = new JButton("Luu phieu");
        btnHuyBo = new JButton("Huy bo");

        pnlRightFooter.add(lblTongTien);
        pnlRightFooter.add(btnLuuPhieu);
        pnlRightFooter.add(btnHuyBo);

        pnlFooter.add(pnlRightFooter, BorderLayout.EAST);
        panelBottomContainer.add(pnlFooter, BorderLayout.SOUTH);

        // Add 2 phần vào SplitPane
        mainSplitPane.setTopComponent(panelTopContainer);
        mainSplitPane.setBottomComponent(panelBottomContainer);

        add(mainSplitPane, BorderLayout.CENTER);
    }

    // // Hàm main test độc lập nhanh giao diện
    // public static void main(String[] args) {
    // JFrame frame = new JFrame("Test Kho Thuoc & Vat Tu Y Te");
    // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    // frame.setSize(1100, 700);
    // frame.setLocationRelativeTo(null);
    // frame.add(new KhoThuocPanel());
    // frame.setVisible(true);
    // }

    // lệnh terminal để chạy test giao diện nhanh:
    // javac -encoding UTF-8 -d out src/main/java/gui/KhoThuocPanel.java
    // java -cp out gui.KhoThuocPanel

}