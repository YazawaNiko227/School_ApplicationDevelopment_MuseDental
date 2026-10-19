package gui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class QuanLyKhoThuocPanel extends JPanel {

        // Component tầng trên (Tồn kho)
        private JTabbedPane tabbedPane;
        private JTextField txtTimKiemVatTu;
        private JComboBox<String> cbLoaiVatTu;
        private JButton btnTimKiem, btnLapPhieuNhapMoi;
        private JTable tableTonKho;
        private DefaultTableModel modelTonKho;

        // Component tầng dưới (Lập phiếu nhập kho)
        private JComboBox<String> cbNhaCungCap;
        private JTextField txtSoHD;
        private JSpinner spinnerNgayNhap; // Đã đổi thành JSpinner
        private JTable tableNhapKho;
        private DefaultTableModel modelNhapKho;
        private JButton btnThemDong, btnLuuPhieu, btnHuyBo;
        private JLabel lblTongTien;

        public QuanLyKhoThuocPanel() {
                setLayout(new BorderLayout(5, 5));
                setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                // Đặt font mặc định cho toàn bộ Panel để hiển thị tốt tiếng Việt
                setFont(new Font("Segoe UI", Font.PLAIN, 14));

                // ----------------------------------------------------
                // 1. TIÊU ĐỀ CHÍNH
                // ----------------------------------------------------
                JLabel lblHeader = new JLabel("Quản lý Kho thuốc & Vật tư Y tế");
                lblHeader.setFont(new Font("Arial", Font.BOLD, 18));
                lblHeader.setForeground(new Color(0, 102, 204)); // Thêm màu cho đẹp
                add(lblHeader, BorderLayout.NORTH);

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
                pnlFilter.add(new JLabel("Tìm kiếm vật tư:"));
                txtTimKiemVatTu = new JTextField("Tên vật tư, hoạt chất...", 15);
                pnlFilter.add(txtTimKiemVatTu);

                pnlFilter.add(new JLabel("Loại vật tư:"));
                cbLoaiVatTu = new JComboBox<>(new String[] { "Tất cả", "Thuốc", "Vật tư y tế" });
                pnlFilter.add(cbLoaiVatTu);

                btnTimKiem = new JButton("Tìm kiếm");
                btnLapPhieuNhapMoi = new JButton("+ Lập phiếu nhập kho mới");
                pnlFilter.add(btnTimKiem);
                pnlFilter.add(btnLapPhieuNhapMoi);

                panelTonKho.add(pnlFilter, BorderLayout.NORTH);

                // 2.2 Bảng dữ liệu Tồn kho
                String[] colsTonKho = { "Tên vật tư", "Đơn vị", "SL tồn", "Ngưỡng tối thiểu", "Hạn sử dụng", "Nhà CC",
                                "Trạng thái", "Thao tác" };
                Object[][] dataTonKho = {
                                { "Kim tiêm 25G", "Cái", "850", "200", "12/2027", "Cty A", "OK", "Xem chi tiết" },
                                { "Bông gạc vô trùng", "Gói", "320", "100", "06/2027", "Cty B", "OK", "Xem chi tiết" },
                                { "Găng tay không bột (M)", "Đôi", "95", "200", "09/2026", "Cty A", "! CẢNH BÁO THẤP",
                                                "Xem chi tiết" },
                                { "Composite A2", "Tuýp", "12", "20", "03/2027", "Cty C", "! CẢNH BÁO THẤP",
                                                "Xem chi tiết" },
                                { "Thuốc tê NaCl 0.9%", "Chai", "450", "50", "08/2027", "Cty D", "OK", "Xem chi tiết" },
                                { "Cao xu niềng răng (lọt)", "Túi", "8", "30", "01/2027", "Cty C", "! CẢNH BÁO THẤP",
                                                "Xem chi tiết" },
                                { "Mask y tế 3 lớp", "Cái", "1200", "500", "05/2028", "Cty B", "OK", "Xem chi tiết" }
                };
                modelTonKho = new DefaultTableModel(dataTonKho, colsTonKho);
                tableTonKho = new JTable(modelTonKho);
                panelTonKho.add(new JScrollPane(tableTonKho), BorderLayout.CENTER);

                // Add vào TabbedPane
                tabbedPane.addTab("Tồn kho thời gian thực", panelTonKho);
                tabbedPane.addTab("Nhập kho", new JPanel());
                tabbedPane.addTab("Kiểm kê & Điều chỉnh", new JPanel());
                tabbedPane.addTab("Nhật ký lịch sử", new JPanel());

                panelTopContainer.add(tabbedPane, BorderLayout.CENTER);

                // ----------------------------------------------------
                // 3. TẦNG DƯỚI: LẬP PHIẾU NHẬP KHO TỪ NHÀ CUNG CẤP
                // ----------------------------------------------------
                JPanel panelBottomContainer = new JPanel(new BorderLayout(5, 5));
                panelBottomContainer.setBorder(BorderFactory.createTitledBorder("LẬP PHIẾU NHẬP KHO TỪ NHÀ CUNG CẤP"));

                // 3.1 Form thông tin nhà cung cấp & hóa đơn
                JPanel pnlPhieuHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
                pnlPhieuHeader.add(new JLabel("Nhà cung cấp:"));
                cbNhaCungCap = new JComboBox<>(new String[] { "Cty TNHH Y tế A", "Cty B", "Cty C" });
                pnlPhieuHeader.add(cbNhaCungCap);

                pnlPhieuHeader.add(new JLabel("Số HĐ/PO:"));
                txtSoHD = new JTextField("PO2026-0089", 10);
                pnlPhieuHeader.add(txtSoHD);

                pnlPhieuHeader.add(new JLabel("Ngày nhập:"));

                // --- SỬ DỤNG JSPINNER ĐỂ CHỌN NGÀY ---
                spinnerNgayNhap = new JSpinner(new SpinnerDateModel());
                JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(spinnerNgayNhap, "dd/MM/yyyy");
                spinnerNgayNhap.setEditor(dateEditor);
                spinnerNgayNhap.setValue(new Date()); // Set mặc định là ngày hôm nay
                pnlPhieuHeader.add(spinnerNgayNhap);

                panelBottomContainer.add(pnlPhieuHeader, BorderLayout.NORTH);

                // 3.2 Bảng chi tiết mặt hàng nhập kho
                String[] colsNhapKho = { "Tên vật tư", "Đơn vị", "Số lượng nhập", "Đơn giá nhập", "Thành tiền",
                                "Hạn SD",
                                "Xóa" };
                Object[][] dataNhapKho = {
                                { "Găng tay không bột (M)", "Đôi", "500", "3.500 VND", "1.750.000 VND", "09/2027",
                                                "true" },
                                { "Composite A2", "Tuýp", "30", "45.000 VND", "1.350.000 VND", "12/2027", "true" }
                };
                modelNhapKho = new DefaultTableModel(dataNhapKho, colsNhapKho);
                tableNhapKho = new JTable(modelNhapKho);
                panelBottomContainer.add(new JScrollPane(tableNhapKho), BorderLayout.CENTER);

                // 3.3 Bottom Action Bar (Footer)
                JPanel pnlFooter = new JPanel(new BorderLayout());
                btnThemDong = new JButton("+ Thêm dòng vật tư");
                pnlFooter.add(btnThemDong, BorderLayout.WEST);

                JPanel pnlRightFooter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
                lblTongTien = new JLabel("Tổng tiền: 3.100.000 VND  ");
                lblTongTien.setFont(new Font("Arial", Font.BOLD, 14));
                lblTongTien.setForeground(Color.RED);
                btnLuuPhieu = new JButton("Lưu phiếu");
                btnHuyBo = new JButton("Hủy bỏ");

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

        // Hàm main test độc lập nhanh giao diện (Đã mở comment)
        public static void main(String[] args) {
                // Cài đặt Look and Feel cho giống hệ điều hành (giúp UI đẹp hơn)
                try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                        e.printStackTrace();
                }

                JFrame frame = new JFrame("Test Kho Thuốc & Vật Tư Y Tế");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(1100, 700);
                frame.setLocationRelativeTo(null);
                frame.add(new QuanLyKhoThuocPanel());
                frame.setVisible(true);
        }
}