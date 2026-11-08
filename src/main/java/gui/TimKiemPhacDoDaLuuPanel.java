package gui;

import entity.ChiTietKeHoachDieuTriDichVu;
import entity.KeHoachDieuTri;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import service.PhacDoDaLuuService;

public class TimKiemPhacDoDaLuuPanel extends JPanel {
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color LINE = new Color(226, 234, 237);
    private static final DecimalFormat TIEN = new DecimalFormat("#,##0");
    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PhacDoDaLuuService service = new PhacDoDaLuuService();
    private final List<KeHoachDieuTri> plans = new ArrayList<>();
    private final JTextField searchField = new JTextField();
    private final JLabel planInfo = new JLabel("Chọn một phác đồ để xem chi tiết.");
    private final JLabel diagnosisInfo = new JLabel(" ");
    private final JLabel totalInfo = new JLabel("Tổng dự toán: 0 đ");

    private final DefaultTableModel planModel = new DefaultTableModel(
            new String[] { "Mã phác đồ", "Tên phác đồ", "Mã bệnh nhân", "Bệnh nhân", "Ngày khám", "Thời gian",
                    "Trạng thái" },
            0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable planTable = new JTable(planModel);
    private final TableRowSorter<DefaultTableModel> planSorter = new TableRowSorter<>(planModel);

    private final DefaultTableModel serviceModel = new DefaultTableModel(
            new String[] { "Mã dịch vụ", "Dịch vụ", "Số lượng", "Đơn giá", "Thành tiền" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable serviceTable = new JTable(serviceModel);

    public TimKiemPhacDoDaLuuPanel() {
        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        add(createToolbar(), BorderLayout.NORTH);
        add(createContent(), BorderLayout.CENTER);

        planTable.setModel(planModel);
        planTable.setRowSorter(planSorter);
        planTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        planTable.setRowHeight(30);
        planTable.getTableHeader().setReorderingAllowed(false);
        planTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                hienThiChiTietDaChon();
            }
        });
        serviceTable.setRowHeight(28);
        serviceTable.getTableHeader().setReorderingAllowed(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                locDanhSach();
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                locDanhSach();
            }
        });
        taiDanhSach();
    }

    private JPanel createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setOpaque(false);
        JLabel heading = new JLabel("Phác đồ đã lưu");
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(INK);
        toolbar.add(heading, BorderLayout.WEST);

        JPanel controls = new JPanel(new BorderLayout(8, 0));
        controls.setOpaque(false);
        searchField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchField.setToolTipText("Tìm theo mã, tên phác đồ hoặc bệnh nhân");
        searchField.setPreferredSize(new Dimension(300, 36));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(6, 9, 6, 9)));
        JButton refreshButton = new JButton("Tải lại");
        refreshButton.setFont(new Font("Arial", Font.BOLD, 12));
        refreshButton.setForeground(Color.WHITE);
        refreshButton.setBackground(TEAL);
        refreshButton.setFocusPainted(false);
        refreshButton.addActionListener(event -> taiDanhSach());
        controls.add(searchField, BorderLayout.CENTER);
        controls.add(refreshButton, BorderLayout.EAST);
        toolbar.add(controls, BorderLayout.EAST);
        return toolbar;
    }

    private JPanel createContent() {
        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setOpaque(false);
        JPanel listCard = createCard();
        listCard.setLayout(new BorderLayout());
        styleTable(planTable);
        JScrollPane plansScroll = new JScrollPane(planTable);
        plansScroll.setBorder(BorderFactory.createEmptyBorder());
        listCard.add(plansScroll, BorderLayout.CENTER);
        content.add(listCard, BorderLayout.CENTER);

        JPanel detailsCard = createCard();
        detailsCard.setLayout(new BorderLayout(0, 8));
        detailsCard.setPreferredSize(new Dimension(0, 230));
        JPanel detailsHeader = new JPanel(new BorderLayout(0, 4));
        detailsHeader.setOpaque(false);
        planInfo.setFont(new Font("Arial", Font.BOLD, 13));
        planInfo.setForeground(INK);
        diagnosisInfo.setFont(new Font("Arial", Font.PLAIN, 11));
        diagnosisInfo.setForeground(MUTED);
        detailsHeader.add(planInfo, BorderLayout.NORTH);
        detailsHeader.add(diagnosisInfo, BorderLayout.CENTER);
        detailsCard.add(detailsHeader, BorderLayout.NORTH);
        styleTable(serviceTable);
        JScrollPane servicesScroll = new JScrollPane(serviceTable);
        servicesScroll.setBorder(BorderFactory.createLineBorder(LINE));
        detailsCard.add(servicesScroll, BorderLayout.CENTER);
        totalInfo.setFont(new Font("Arial", Font.BOLD, 13));
        totalInfo.setForeground(TEAL);
        totalInfo.setHorizontalAlignment(SwingConstants.RIGHT);
        detailsCard.add(totalInfo, BorderLayout.SOUTH);
        content.add(detailsCard, BorderLayout.SOUTH);
        return content;
    }

    private JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE), new EmptyBorder(12, 14, 12, 14)));
        return card;
    }

    private void taiDanhSach() {
        try {
            List<KeHoachDieuTri> loadedPlans = service.layDanhSach();
            plans.clear();
            plans.addAll(loadedPlans);
            planModel.setRowCount(0);
            for (KeHoachDieuTri plan : plans) {
                planModel.addRow(new Object[] {
                        plan.getMaKeHoach(),
                        plan.getTenPhacDo(),
                        plan.getBenhNhan().getMaBenhNhan(),
                        plan.getBenhNhan().getHoTen(),
                        plan.getChanDoan().getNgayKham() == null ? "" : NGAY.format(plan.getChanDoan().getNgayKham()),
                        plan.getThoiGianDuKien() == null ? "" : plan.getThoiGianDuKien() + " ngày",
                        service.trangThaiHienThi(plan.getTrangThai())
                });
            }
            locDanhSach();
            xoaChiTiet();
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải danh sách phác đồ đã lưu:\n" + exception.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void locDanhSach() {
        String keyword = searchField.getText().trim();
        planSorter.setRowFilter(keyword.isEmpty()
                ? null
                : RowFilter.regexFilter("(?i)" + Pattern.quote(keyword), 0, 1, 2, 3));
    }

    private void hienThiChiTietDaChon() {
        int selectedRow = planTable.getSelectedRow();
        if (selectedRow < 0) {
            xoaChiTiet();
            return;
        }
        int modelRow = planTable.convertRowIndexToModel(selectedRow);
        String planId = (String) planModel.getValueAt(modelRow, 0);
        KeHoachDieuTri plan = timPhacDo(planId);
        if (plan == null) {
            xoaChiTiet();
            return;
        }

        String ketLuan = plan.getChanDoan().getKetLuanChanDoan();
        String diagnosis = ketLuan == null || ketLuan.trim().isEmpty()
                ? "Chưa có kết luận chẩn đoán"
                : ketLuan;
        planInfo.setText(plan.getTenPhacDo() + " · " + plan.getMaKeHoach()
                + " · Bệnh nhân: " + plan.getBenhNhan().getHoTen());
        diagnosisInfo.setText("Chẩn đoán " + plan.getMaChanDoanId() + ": " + diagnosis);
        try {
            List<ChiTietKeHoachDieuTriDichVu> services = service.layDichVu(planId);
            serviceModel.setRowCount(0);
            double total = 0;
            for (ChiTietKeHoachDieuTriDichVu item : services) {
                double donGia = item.getDichVu().getDonGia();
                total += donGia * item.getSoLuong();
                serviceModel.addRow(new Object[] {
                        item.getMaDichVuId(),
                        item.getDichVu().getTenDichVu(),
                        item.getSoLuong(),
                        TIEN.format(donGia) + " đ",
                        TIEN.format(donGia * item.getSoLuong()) + " đ"
                });
            }
            totalInfo.setText("Tổng dự toán: " + TIEN.format(total) + " đ");
        } catch (SQLException exception) {
            JOptionPane.showMessageDialog(this,
                    "Không thể tải dịch vụ của phác đồ " + planId + ":\n" + exception.getMessage(),
                    "Lỗi CSDL", JOptionPane.ERROR_MESSAGE);
            serviceModel.setRowCount(0);
            totalInfo.setText("Tổng dự toán: không thể tải");
        }
    }

    private KeHoachDieuTri timPhacDo(String maKeHoach) {
        for (KeHoachDieuTri plan : plans) {
            if (plan.getMaKeHoach().equals(maKeHoach)) {
                return plan;
            }
        }
        return null;
    }

    private void xoaChiTiet() {
        planInfo.setText("Chọn một phác đồ để xem chi tiết.");
        diagnosisInfo.setText(" ");
        serviceModel.setRowCount(0);
        totalInfo.setText("Tổng dự toán: 0 đ");
    }

    private static void styleTable(JTable table) {
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setForeground(INK);
        table.setSelectionBackground(new Color(231, 246, 245));
        table.setSelectionForeground(INK);
        table.setShowVerticalLines(false);
        table.setGridColor(new Color(238, 242, 244));
        table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 11));
        table.getTableHeader().setForeground(INK);
        table.getTableHeader().setBackground(new Color(244, 247, 250));
        table.getTableHeader().setPreferredSize(new Dimension(0, 32));
    }
}
