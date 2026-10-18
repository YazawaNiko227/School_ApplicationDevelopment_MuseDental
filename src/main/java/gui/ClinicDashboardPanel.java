/*
    Người làm phần này: Tran thanh nhut
   1
*/

package gui;

import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * ClinicDashboardPanel — Màn hình điều phối phòng khám.
 *
 * Giao diện này cho phép nhân viên lễ tân / điều phối viên theo dõi
 * toàn bộ hoạt động phòng khám trong ngày theo thời gian thực:
 * · Trạng thái từng phòng (Trống / Đang dùng / Cần dọn dẹp / Không hoạt động)
 * · Hàng chờ bệnh nhân theo phòng / nha sĩ
 * · Lịch làm việc và tình trạng của từng nha sĩ
 * · Các thống kê nhanh trong ngày (tổng lịch, đã hoàn thành, đang chờ…)
 * · Nút hành động nhanh: check-in, phân công phòng, cập nhật trạng thái
 */
public final class ClinicDashboardPanel extends JPanel {

    // Bảng màu nhất quán với toàn ứng dụng
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(102, 119, 130);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color TEAL_SOFT = new Color(231, 246, 245);
    private static final Color LINE = new Color(226, 234, 237);
    private static final Color WHITE = Color.WHITE;

    // Màu trạng thái phòng
    private static final Color STATUS_FREE_FG = new Color(34, 197, 94);
    private static final Color STATUS_BUSY_FG = new Color(239, 68, 68);
    private static final Color STATUS_CLEAN_FG = new Color(234, 179, 8);
    private static final Color STATUS_OFF_FG = new Color(148, 163, 184);
    private static final Color STATUS_FREE_BG = new Color(220, 252, 231);
    private static final Color STATUS_BUSY_BG = new Color(254, 226, 226);
    private static final Color STATUS_CLEAN_BG = new Color(254, 249, 195);
    private static final Color STATUS_OFF_BG = new Color(241, 245, 249);

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    // Dữ liệu mock — thay bằng DAO thật khi kết nối CSDL
    private final List<Room> rooms = createSampleRooms();
    private final List<Dentist> dentists = createSampleDentists();
    private final List<QueueEntry> queue = createSampleQueue();

    // Label cập nhật động
    private final JLabel clockLabel;
    private final JLabel totalLabel;
    private final JLabel doneLabel;
    private final JLabel waitingLabel;
    private final JLabel inRoomLabel;

    // ─── Constructor ──────────────────────────────────────────────────────────
    public ClinicDashboardPanel() {
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        clockLabel = makeLiveLabel("--:--", 15, Font.BOLD, TEAL);
        totalLabel = makeLiveLabel("0", 26, Font.BOLD, INK);
        doneLabel = makeLiveLabel("0", 26, Font.BOLD, new Color(34, 197, 94));
        waitingLabel = makeLiveLabel("0", 26, Font.BOLD, new Color(234, 179, 8));
        inRoomLabel = makeLiveLabel("0", 26, Font.BOLD, TEAL);

        add(buildTopBar(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        refreshStats();
        startClock();
    }

    // ─── Top bar: đồng hồ + 4 thẻ thống kê ──────────────────────────────────
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout(16, 0));
        bar.setOpaque(false);

        // Ngày & đồng hồ
        JPanel timeBlock = new JPanel();
        timeBlock.setOpaque(false);
        timeBlock.setLayout(new BoxLayout(timeBlock, BoxLayout.Y_AXIS));
        String today = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy",
                        Locale.forLanguageTag("vi-VN")));
        JLabel dateLabel = new JLabel(today);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        dateLabel.setForeground(MUTED);
        timeBlock.add(dateLabel);
        timeBlock.add(Box.createVerticalStrut(4));
        timeBlock.add(clockLabel);

        // 4 thẻ mini
        JPanel cards = new JPanel(new GridLayout(1, 4, 12, 0));
        cards.setOpaque(false);
        cards.add(buildMiniCard("TỔNG LỊCH HÔM NAY", totalLabel, new Color(231, 246, 245), TEAL));
        cards.add(buildMiniCard("ĐANG TRONG PHÒNG", inRoomLabel, new Color(231, 246, 245), TEAL));
        cards.add(buildMiniCard("ĐANG CHỜ", waitingLabel, new Color(255, 246, 232), new Color(217, 119, 6)));
        cards.add(buildMiniCard("ĐÃ HOÀN THÀNH", doneLabel, new Color(220, 252, 231), new Color(22, 163, 74)));

        bar.add(timeBlock, BorderLayout.WEST);
        bar.add(cards, BorderLayout.CENTER);
        return bar;
    }

    private JPanel buildMiniCard(String title, JLabel valLabel, Color bg, Color fg) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(14, 16, 14, 16)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Arial", Font.BOLD, 9));
        titleLbl.setForeground(MUTED);
        valLabel.setForeground(fg);
        card.add(titleLbl, BorderLayout.NORTH);
        card.add(valLabel, BorderLayout.CENTER);
        return card;
    }

    // ─── Body: cột trái (phòng + nha sĩ) | cột phải (hàng chờ) ─────────────
    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setOpaque(false);

        JPanel left = new JPanel(new BorderLayout(0, 14));
        left.setOpaque(false);
        left.setPreferredSize(new Dimension(430, 0));
        left.add(buildSection("Trạng thái phòng khám", buildRoomGrid()), BorderLayout.NORTH);
        left.add(buildSection("Nha sĩ trực ca hôm nay", buildDentistList()), BorderLayout.CENTER);

        body.add(left, BorderLayout.WEST);
        body.add(buildSection("Hàng chờ bệnh nhân trong ngày", buildQueuePanel()), BorderLayout.CENTER);
        return body;
    }

    // ─── Lưới 6 phòng khám (2 cột) ───────────────────────────────────────────
    private JPanel buildRoomGrid() {
        JPanel grid = new JPanel(new GridLayout(0, 2, 12, 12));
        grid.setOpaque(false);
        for (Room room : rooms)
            grid.add(buildRoomCard(room));
        return grid;
    }

    private JPanel buildRoomCard(Room room) {
        Color fgColor = roomStatusFg(room.status);
        Color bgColor = roomStatusBg(room.status);
        String stText = roomStatusText(room.status);

        JPanel card = new JPanel(new BorderLayout(0, 10));
        card.setBackground(WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(13, 13, 13, 13)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Header: tên phòng + badge trạng thái
        JPanel hdr = new JPanel(new BorderLayout(6, 0));
        hdr.setOpaque(false);
        JLabel nameLabel = new JLabel(room.name);
        nameLabel.setFont(new Font("Arial", Font.BOLD, 13));
        nameLabel.setForeground(INK);
        JLabel badge = new JLabel(stText, SwingConstants.CENTER);
        badge.setFont(new Font("Arial", Font.BOLD, 9));
        badge.setForeground(fgColor);
        badge.setOpaque(true);
        badge.setBackground(bgColor);
        badge.setBorder(new EmptyBorder(4, 8, 4, 8));
        hdr.add(nameLabel, BorderLayout.WEST);
        hdr.add(badge, BorderLayout.EAST);

        // Info rows
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(htmlRow("Nha sĩ:", room.currentDentist.isEmpty() ? "—" : room.currentDentist));
        info.add(Box.createVerticalStrut(4));
        info.add(htmlRow("Bệnh nhân:", room.currentPatient.isEmpty() ? "—" : room.currentPatient));
        info.add(Box.createVerticalStrut(4));
        info.add(htmlRow("Vào phòng:", room.startTime == null ? "—" : room.startTime.format(TIME_FMT)));

        // Nút hành động nhanh theo trạng thái phòng
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        actions.setOpaque(false);
        if (room.status == RoomStatus.FREE) {
            JButton btn = makeBtn("Phân công", TEAL_SOFT, TEAL);
            btn.addActionListener(e -> showAssignDialog(room));
            actions.add(btn);
        } else if (room.status == RoomStatus.BUSY) {
            JButton btn = makeBtn("Hoàn thành", new Color(220, 252, 231), new Color(22, 163, 74));
            btn.addActionListener(e -> {
                for (QueueEntry q : queue) {
                    if (q.room.equals(room.id) && q.status == QueueStatus.IN_ROOM) {
                        q.status = QueueStatus.DONE;
                    }
                }
                room.status = RoomStatus.NEED_CLEAN;
                room.currentPatient = "";
                room.startTime = null;
                rebuildParent();
            });
            actions.add(btn);
        } else if (room.status == RoomStatus.NEED_CLEAN) {
            JButton btn = makeBtn("Đã dọn xong", new Color(254, 249, 195), new Color(161, 98, 7));
            btn.addActionListener(e -> {
                room.status = RoomStatus.FREE;
                room.currentDentist = "";
                rebuildParent();
            });
            actions.add(btn);
        }

        card.add(hdr, BorderLayout.NORTH);
        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.SOUTH);

        // Hover effect: đổi viền thành teal khi di chuột vào
        card.addMouseListener(new MouseAdapter() {
            final Border normal = card.getBorder();
            final Border hover = BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(TEAL, 1),
                    new EmptyBorder(13, 13, 13, 13));

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(normal);
            }
        });
        return card;
    }

    // ─── Danh sách nha sĩ trực ca ────────────────────────────────────────────
    private JPanel buildDentistList() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        for (Dentist d : dentists) {
            panel.add(buildDentistRow(d));
            panel.add(Box.createVerticalStrut(8));
        }
        return panel;
    }

    private JPanel buildDentistRow(Dentist d) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setBackground(WHITE);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(10, 12, 10, 12)));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 62));

        row.add(new DentistAvatar(d.name, d.avatarColor), BorderLayout.WEST);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel nameLbl = new JLabel(d.name);
        nameLbl.setFont(new Font("Arial", Font.BOLD, 12));
        nameLbl.setForeground(INK);
        JLabel specLbl = new JLabel(d.specialty + "  ·  " + d.room);
        specLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        specLbl.setForeground(MUTED);
        info.add(nameLbl);
        info.add(Box.createVerticalStrut(3));
        info.add(specLbl);
        row.add(info, BorderLayout.CENTER);

        Color stFg = d.available ? new Color(22, 163, 74) : TEAL;
        Color stBg = d.available ? new Color(220, 252, 231) : TEAL_SOFT;
        JLabel stBadge = new JLabel(d.available ? "Rảnh" : "Đang khám", SwingConstants.CENTER);
        stBadge.setFont(new Font("Arial", Font.BOLD, 10));
        stBadge.setForeground(stFg);
        stBadge.setOpaque(true);
        stBadge.setBackground(stBg);
        stBadge.setBorder(new EmptyBorder(5, 10, 5, 10));

        JLabel cntLbl = new JLabel(d.remainingAppts + " lịch còn lại", SwingConstants.RIGHT);
        cntLbl.setFont(new Font("Arial", Font.PLAIN, 10));
        cntLbl.setForeground(MUTED);

        JPanel right = new JPanel(new BorderLayout(0, 4));
        right.setOpaque(false);
        right.add(stBadge, BorderLayout.NORTH);
        right.add(cntLbl, BorderLayout.SOUTH);
        row.add(right, BorderLayout.EAST);
        return row;
    }

    // ─── Bảng hàng chờ bệnh nhân ─────────────────────────────────────────────
    private JPanel buildQueuePanel() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);

        // Toolbar: bộ lọc + nút check-in
        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        JLabel lbl = new JLabel("Lọc:");
        lbl.setFont(new Font("Arial", Font.BOLD, 11));
        lbl.setForeground(INK);
        String[] opts = { "Tất cả", "Đang chờ", "Đang trong phòng", "Hoàn thành", "Đã hủy" };
        JComboBox<String> filterBox = new JComboBox<>(opts);
        filterBox.setFont(new Font("Arial", Font.PLAIN, 11));
        filterBox.setPreferredSize(new Dimension(175, 32));
        filterRow.add(lbl);
        filterRow.add(filterBox);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnRow.setOpaque(false);
        JButton checkinBtn = makeBtn("✔  Check-in bệnh nhân", TEAL_SOFT, TEAL);
        checkinBtn.setFont(new Font("Arial", Font.BOLD, 11));
        checkinBtn.setPreferredSize(new Dimension(175, 32));
        checkinBtn.addActionListener(e -> showCheckinDialog());
        btnRow.add(checkinBtn);

        toolbar.add(filterRow, BorderLayout.WEST);
        toolbar.add(btnRow, BorderLayout.EAST);

        // Bảng dữ liệu hàng chờ
        String[] cols = { "#", "Giờ hẹn", "Bệnh nhân", "Nha sĩ", "Phòng", "Dịch vụ", "Trạng thái", "Chi tiết" };
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 7;
            }
        };

        JTable table = new JTable(model);
        table.setRowHeight(42);
        table.setFont(new Font("Arial", Font.PLAIN, 11));
        table.setForeground(INK);
        table.setGridColor(new Color(237, 242, 244));
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(TEAL_SOFT);
        table.setSelectionForeground(INK);
        table.setIntercellSpacing(new Dimension(0, 0));

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("Arial", Font.BOLD, 10));
        header.setForeground(MUTED);
        header.setBackground(new Color(248, 250, 251));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);

        int[] widths = { 36, 62, 140, 100, 52, 120, 118, 80 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // Renderer badge màu cột Trạng thái
        table.getColumnModel().getColumn(6).setCellRenderer(new QueueStatusRenderer());
        // Renderer + Editor nút Chi tiết
        table.getColumnModel().getColumn(7).setCellRenderer(new DetailBtnRenderer());
        table.getColumnModel().getColumn(7).setCellEditor(new DetailBtnEditor(table, model));

        // Nạp dữ liệu ban đầu
        populateQueue(model, null);

        // Xử lý bộ lọc trạng thái
        filterBox.addActionListener(e -> {
            String sel = (String) filterBox.getSelectedItem();
            model.setRowCount(0);
            populateQueue(model, "Tất cả".equals(sel) ? null : sel);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(LINE));
        scroll.getViewport().setBackground(WHITE);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        wrapper.add(toolbar, BorderLayout.NORTH);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    /**
     * Nạp dữ liệu hàng chờ vào bảng, tuỳ chọn lọc theo trạng thái.
     * filterStatus null = hiện tất cả.
     */
    private void populateQueue(DefaultTableModel model, String filterStatus) {
        int stt = 1;
        for (QueueEntry e : queue) {
            if (filterStatus != null && !e.statusText().equals(filterStatus))
                continue;
            model.addRow(new Object[] {
                    stt++,
                    e.appointTime.format(TIME_FMT),
                    e.patientName,
                    e.dentistName,
                    e.room,
                    e.service,
                    e.statusText(),
                    "Chi tiết"
            });
        }
    }

    // ─── Dialog phân công phòng ───────────────────────────────────────────────
    private void showAssignDialog(Room room) {
        String[] availDentists = dentists.stream()
                .filter(d -> d.available).map(d -> d.name).toArray(String[]::new);
        if (availDentists.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có nha sĩ nào rảnh.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String[] waitingPats = queue.stream()
                .filter(e -> e.status == QueueStatus.WAITING)
                .map(e -> e.patientName).toArray(String[]::new);
        if (waitingPats.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Không có bệnh nhân nào đang chờ.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JComboBox<String> dBox = new JComboBox<>(availDentists);
        JComboBox<String> pBox = new JComboBox<>(waitingPats);
        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setPreferredSize(new Dimension(360, 90));
        form.add(new JLabel("Nha sĩ phụ trách:"));
        form.add(dBox);
        form.add(new JLabel("Bệnh nhân:"));
        form.add(pBox);

        int r = JOptionPane.showConfirmDialog(this, form,
                "Phân công — " + room.name, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION)
            return;

        room.status = RoomStatus.BUSY;
        room.currentDentist = (String) dBox.getSelectedItem();
        room.currentPatient = (String) pBox.getSelectedItem();
        room.startTime = LocalTime.now();
        for (QueueEntry e : queue) {
            if (e.patientName.equals(room.currentPatient) && e.status == QueueStatus.WAITING) {
                e.status = QueueStatus.IN_ROOM;
                e.room = room.id;
                break;
            }
        }
        for (Dentist d : dentists) {
            if (d.name.equals(room.currentDentist)) {
                d.available = false;
                d.remainingAppts = Math.max(0, d.remainingAppts - 1);
                break;
            }
        }
        rebuildParent();
    }

    // ─── Dialog check-in bệnh nhân mới ───────────────────────────────────────
    private void showCheckinDialog() {
        JTextField patientField = new JTextField();
        JTextField serviceField = new JTextField();
        JComboBox<String> dentistBox = new JComboBox<>(
                dentists.stream().map(d -> d.name).toArray(String[]::new));
        String[] slots = { "08:00", "08:30", "09:00", "09:30", "10:00", "10:30",
                "11:00", "13:00", "13:30", "14:00", "14:30", "15:00", "15:30", "16:00" };
        JComboBox<String> timeBox = new JComboBox<>(slots);

        JPanel form = new JPanel(new GridLayout(0, 2, 10, 8));
        form.setPreferredSize(new Dimension(380, 145));
        form.add(new JLabel("Tên bệnh nhân *:"));
        form.add(patientField);
        form.add(new JLabel("Dịch vụ:"));
        form.add(serviceField);
        form.add(new JLabel("Nha sĩ:"));
        form.add(dentistBox);
        form.add(new JLabel("Giờ hẹn:"));
        form.add(timeBox);

        int r = JOptionPane.showConfirmDialog(this, form,
                "Check-in bệnh nhân", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION)
            return;

        String pName = patientField.getText().trim();
        if (pName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bệnh nhân.",
                    "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String svc = serviceField.getText().trim().isEmpty()
                ? "Khám tổng quát"
                : serviceField.getText().trim();
        String dentist = (String) dentistBox.getSelectedItem();
        LocalTime aTime = LocalTime.parse((String) timeBox.getSelectedItem(), TIME_FMT);
        String assignedRoom = dentists.stream()
                .filter(d -> d.name.equals(dentist))
                .map(d -> d.room).findFirst().orElse("—");

        queue.add(0, new QueueEntry(pName, dentist, assignedRoom, svc, aTime, QueueStatus.WAITING));
        rebuildParent();
        JOptionPane.showMessageDialog(this,
                "Đã check-in bệnh nhân " + pName + " thành công.\nĐang chờ phân công phòng.",
                "Check-in thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Rebuild toàn bộ panel bằng cách hoán đổi với instance mới mang state hiện
     * tại.
     * Cách này đơn giản và phù hợp khi dùng mock data.
     */
    private void rebuildParent() {
        SwingUtilities.invokeLater(() -> {
            Container parent = getParent();
            if (parent == null)
                return;
            ClinicDashboardPanel fresh = new ClinicDashboardPanel();
            fresh.rooms.clear();
            fresh.rooms.addAll(rooms);
            fresh.dentists.clear();
            fresh.dentists.addAll(dentists);
            fresh.queue.clear();
            fresh.queue.addAll(queue);
            fresh.refreshStats();
            parent.remove(this);
            parent.add(fresh, BorderLayout.CENTER);
            parent.revalidate();
            parent.repaint();
        });
    }

    // Cập nhật 4 số liệu thống kê dựa trên trạng thái hàng chờ
    private void refreshStats() {
        totalLabel.setText(String.valueOf(queue.size()));
        inRoomLabel.setText(String.valueOf(
                queue.stream().filter(e -> e.status == QueueStatus.IN_ROOM).count()));
        waitingLabel.setText(String.valueOf(
                queue.stream().filter(e -> e.status == QueueStatus.WAITING).count()));
        doneLabel.setText(String.valueOf(
                queue.stream().filter(e -> e.status == QueueStatus.DONE).count()));
    }

    // Đồng hồ thời gian thực cập nhật mỗi giây
    private void startClock() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> clockLabel.setText(LocalTime.now().format(TIME_FMT)));
            }
        }, 0, 1000);
        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && !isShowing())
                timer.cancel();
        });
    }

    // ─── UI helper methods ────────────────────────────────────────────────────
    /** Bọc nội dung trong card section với tiêu đề và đường kẻ phân cách */
    private JPanel buildSection(String title, JPanel content) {
        JPanel sec = new JPanel(new BorderLayout(0, 12));
        sec.setBackground(WHITE);
        sec.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(16, 16, 16, 16)));
        JLabel tlbl = new JLabel(title);
        tlbl.setFont(new Font("Arial", Font.BOLD, 14));
        tlbl.setForeground(INK);
        tlbl.setBorder(new EmptyBorder(0, 0, 10, 0));
        JPanel hdr = new JPanel(new BorderLayout());
        hdr.setOpaque(false);
        hdr.add(tlbl, BorderLayout.NORTH);
        JSeparator sep = new JSeparator();
        sep.setForeground(LINE);
        hdr.add(sep, BorderLayout.SOUTH);
        sec.add(hdr, BorderLayout.NORTH);
        sec.add(content, BorderLayout.CENTER);
        return sec;
    }

    /** Dòng thông tin dạng "Label: Giá trị" với màu HTML */
    private JLabel htmlRow(String label, String value) {
        JLabel lbl = new JLabel(
                "<html><span style='color:#667880'><b>" + label + "</b></span> " +
                        "<span style='color:#203544'>" + value + "</span></html>");
        lbl.setFont(new Font("Arial", Font.PLAIN, 11));
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    /** Nút hành động nhỏ gọn có màu nền và chữ tuỳ chỉnh */
    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 10));
        btn.setForeground(fg);
        btn.setBackground(bg);
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker()),
                new EmptyBorder(5, 10, 5, 10)));
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    /** JLabel với font/màu động, dùng cho số liệu cập nhật theo thời gian */
    private JLabel makeLiveLabel(String text, int size, int style, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Arial", style, size));
        lbl.setForeground(color);
        return lbl;
    }

    // ─── Helpers màu trạng thái phòng ────────────────────────────────────────
    private Color roomStatusFg(RoomStatus s) {
        return switch (s) {
            case FREE -> STATUS_FREE_FG;
            case BUSY -> STATUS_BUSY_FG;
            case NEED_CLEAN -> STATUS_CLEAN_FG;
            case OFF -> STATUS_OFF_FG;
        };
    }

    private Color roomStatusBg(RoomStatus s) {
        return switch (s) {
            case FREE -> STATUS_FREE_BG;
            case BUSY -> STATUS_BUSY_BG;
            case NEED_CLEAN -> STATUS_CLEAN_BG;
            case OFF -> STATUS_OFF_BG;
        };
    }

    private String roomStatusText(RoomStatus s) {
        return switch (s) {
            case FREE -> "● Trống";
            case BUSY -> "● Đang dùng";
            case NEED_CLEAN -> "● Cần dọn";
            case OFF -> "● Không hoạt động";
        };
    }

    // ─── Data model enums & classes ───────────────────────────────────────────
    enum RoomStatus {
        FREE, BUSY, NEED_CLEAN, OFF
    }

    enum QueueStatus {
        WAITING, IN_ROOM, DONE, CANCELLED
    }

    private static final class Room {
        String id, name, currentDentist, currentPatient;
        RoomStatus status;
        LocalTime startTime;

        Room(String id, String name, RoomStatus s, String d, String p, LocalTime t) {
            this.id = id;
            this.name = name;
            this.status = s;
            this.currentDentist = d;
            this.currentPatient = p;
            this.startTime = t;
        }
    }

    private static final class Dentist {
        String name, specialty, room;
        boolean available;
        int remainingAppts;
        Color avatarColor;

        Dentist(String n, String sp, String r, boolean a, int cnt, Color c) {
            name = n;
            specialty = sp;
            room = r;
            available = a;
            remainingAppts = cnt;
            avatarColor = c;
        }
    }

    private static final class QueueEntry {
        String patientName, dentistName, room, service;
        LocalTime appointTime;
        QueueStatus status;

        QueueEntry(String pn, String dn, String rm, String sv, LocalTime t, QueueStatus st) {
            patientName = pn;
            dentistName = dn;
            room = rm;
            service = sv;
            appointTime = t;
            status = st;
        }

        String statusText() {
            return switch (status) {
                case WAITING -> "Đang chờ";
                case IN_ROOM -> "Đang trong phòng";
                case DONE -> "Hoàn thành";
                case CANCELLED -> "Đã hủy";
            };
        }
    }

    // ─── Mock data mẫu ───────────────────────────────────────────────────────
    private List<Room> createSampleRooms() {
        return new ArrayList<>(Arrays.asList(
                new Room("P01", "Phòng 01", RoomStatus.BUSY,
                        "BS. Nguyễn Tuấn", "Nguyễn Minh Anh", LocalTime.of(8, 45)),
                new Room("P02", "Phòng 02", RoomStatus.BUSY,
                        "BS. Trần Linh", "Trần Hoàng Nam", LocalTime.of(9, 10)),
                new Room("P03", "Phòng 03", RoomStatus.FREE, "", "", null),
                new Room("P04", "Phòng 04", RoomStatus.NEED_CLEAN, "", "", null),
                new Room("P05", "Phòng 05", RoomStatus.FREE, "", "", null),
                new Room("P06", "Phòng 06", RoomStatus.OFF, "", "", null)));
    }

    private List<Dentist> createSampleDentists() {
        return new ArrayList<>(Arrays.asList(
                new Dentist("BS. Nguyễn Tuấn", "Nha khoa tổng quát", "P01",
                        false, 3, new Color(99, 102, 241)),
                new Dentist("BS. Trần Linh", "Chỉnh nha & niềng răng", "P02",
                        false, 4, new Color(236, 72, 153)),
                new Dentist("BS. Lê Minh", "Nhổ răng & phẫu thuật", "P03",
                        true, 5, new Color(34, 197, 94)),
                new Dentist("BS. Phạm Hương", "Tẩy trắng & thẩm mỹ", "P05",
                        true, 2, new Color(245, 158, 11))));
    }

    private List<QueueEntry> createSampleQueue() {
        return new ArrayList<>(Arrays.asList(
                new QueueEntry("Nguyễn Minh Anh", "BS. Nguyễn Tuấn", "P01",
                        "Khám tổng quát", LocalTime.of(8, 30), QueueStatus.IN_ROOM),
                new QueueEntry("Trần Hoàng Nam", "BS. Trần Linh", "P02",
                        "Lấy cao răng", LocalTime.of(9, 0), QueueStatus.IN_ROOM),
                new QueueEntry("Lê Ngọc Hà", "BS. Nguyễn Tuấn", "P01",
                        "Tái khám", LocalTime.of(9, 30), QueueStatus.WAITING),
                new QueueEntry("Phạm Gia Bảo", "BS. Lê Minh", "P03",
                        "Nhổ răng khôn", LocalTime.of(10, 0), QueueStatus.WAITING),
                new QueueEntry("Đỗ Thanh Bình", "BS. Trần Linh", "P02",
                        "Niềng răng tái khám", LocalTime.of(10, 30), QueueStatus.WAITING),
                new QueueEntry("Vũ Thị Thu", "BS. Phạm Hương", "P05",
                        "Tẩy trắng", LocalTime.of(11, 0), QueueStatus.WAITING),
                new QueueEntry("Hoàng Quốc Dũng", "BS. Nguyễn Tuấn", "P01",
                        "Trám răng", LocalTime.of(8, 0), QueueStatus.DONE),
                new QueueEntry("Ngô Thị Lan", "BS. Trần Linh", "P02",
                        "Khám tổng quát", LocalTime.of(8, 30), QueueStatus.DONE)));
    }

    // ─── Custom Renderers & Editors ───────────────────────────────────────────

    /** Badge màu trạng thái bệnh nhân trong cột hàng chờ */
    private static final class QueueStatusRenderer extends DefaultTableCellRenderer {
        private static final Color T = new Color(26, 151, 151);
        private static final Color MU = new Color(102, 119, 130);

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            String text = v == null ? "" : v.toString();
            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Arial", Font.BOLD, 10));
            lbl.setBorder(new EmptyBorder(6, 8, 6, 8));
            switch (text) {
                case "Đang chờ" -> {
                    lbl.setForeground(new Color(180, 100, 0));
                    lbl.setBackground(new Color(255, 246, 232));
                }
                case "Đang trong phòng" -> {
                    lbl.setForeground(T);
                    lbl.setBackground(new Color(231, 246, 245));
                }
                case "Hoàn thành" -> {
                    lbl.setForeground(new Color(22, 163, 74));
                    lbl.setBackground(new Color(220, 252, 231));
                }
                default -> {
                    lbl.setForeground(MU);
                    lbl.setBackground(new Color(241, 245, 249));
                }
            }
            if (sel)
                lbl.setBackground(new Color(231, 246, 245));
            return lbl;
        }
    }

    /** Renderer nút "Chi tiết" trong cột hành động */
    private static final class DetailBtnRenderer extends JButton implements TableCellRenderer {
        DetailBtnRenderer() {
            setText("Chi tiết");
            setFont(new Font("Arial", Font.BOLD, 10));
            setForeground(new Color(26, 151, 151));
            setBackground(new Color(231, 246, 245));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(186, 230, 227)),
                    new EmptyBorder(4, 10, 4, 10)));
            setFocusPainted(false);
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int r, int c) {
            return this;
        }
    }

    /** Editor nút "Chi tiết" — click hiện dialog thông tin chi tiết lịch hẹn */
    private final class DetailBtnEditor extends DefaultCellEditor {
        private final JButton btn;
        private final JTable table;
        private final DefaultTableModel model;

        DetailBtnEditor(JTable table, DefaultTableModel model) {
            super(new JCheckBox());
            this.table = table;
            this.model = model;
            btn = new JButton("Chi tiết");
            btn.setFont(new Font("Arial", Font.BOLD, 10));
            btn.setForeground(TEAL);
            btn.setBackground(TEAL_SOFT);
            btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(186, 230, 227)),
                    new EmptyBorder(4, 10, 4, 10)));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                int row = table.getSelectedRow();
                if (row < 0) {
                    fireEditingStopped();
                    return;
                }
                String msg = String.format(
                        "<html><table cellpadding='5'>" +
                                "<tr><td><b>Bệnh nhân</b></td><td>%s</td></tr>" +
                                "<tr><td><b>Nha sĩ</b></td><td>%s</td></tr>" +
                                "<tr><td><b>Phòng</b></td><td>%s</td></tr>" +
                                "<tr><td><b>Dịch vụ</b></td><td>%s</td></tr>" +
                                "<tr><td><b>Giờ hẹn</b></td><td>%s</td></tr>" +
                                "<tr><td><b>Trạng thái</b></td><td>%s</td></tr>" +
                                "</table></html>",
                        model.getValueAt(row, 2), model.getValueAt(row, 3),
                        model.getValueAt(row, 4), model.getValueAt(row, 5),
                        model.getValueAt(row, 1), model.getValueAt(row, 6));
                JOptionPane.showMessageDialog(ClinicDashboardPanel.this, msg,
                        "Chi tiết lịch hẹn", JOptionPane.INFORMATION_MESSAGE);
                fireEditingStopped();
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable t, Object v, boolean s, int r, int c) {
            return btn;
        }

        @Override
        public Object getCellEditorValue() {
            return "Chi tiết";
        }
    }

    /** Avatar hình tròn màu hiển thị chữ viết tắt tên nha sĩ */
    private static final class DentistAvatar extends JPanel {
        private final String initials;
        private final Color bg;

        DentistAvatar(String name, Color bg) {
            setOpaque(false);
            setPreferredSize(new Dimension(40, 40));
            setMinimumSize(new Dimension(40, 40));
            this.bg = bg;
            String clean = name.replace("BS. ", "").trim();
            String[] parts = clean.split("\\s+");
            initials = parts.length >= 2
                    ? "" + Character.toUpperCase(parts[0].charAt(0))
                            + Character.toUpperCase(parts[parts.length - 1].charAt(0))
                    : clean.substring(0, Math.min(2, clean.length())).toUpperCase(Locale.ROOT);
        }

        @Override
        protected void paintComponent(Graphics g2) {
            Graphics2D g = (Graphics2D) g2.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(bg);
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 13));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(initials,
                    (getWidth() - fm.stringWidth(initials)) / 2,
                    (getHeight() - fm.getHeight()) / 2 + fm.getAscent());
            g.dispose();
        }
    }
}
