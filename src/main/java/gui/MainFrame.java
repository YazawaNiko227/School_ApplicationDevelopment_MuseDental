/*
    Người làm phần này: Phan Đức Toàn
    MSSV: 24741631
*/

package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public final class MainFrame extends JFrame {
    // Màu giao diện dùng chung cho thanh điều hướng và vùng nội dung.
    private static final Color INK = new Color(32, 53, 68);
    private static final Color MUTED = new Color(119, 139, 151);
    private static final Color TEAL = new Color(26, 151, 151);
    private static final Color CANVAS = new Color(244, 248, 249);
    private static final Color LINE = new Color(226, 234, 237);

    // Tên hiển thị trong thanh đầu trang và lời chào dashboard.
    private final String username;
    // Các thành phần này được giữ lại để cập nhật trang khi người dùng chọn menu.
    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JLabel pageTitle = new JLabel("Tổng quan");
    private final JLabel pageDescription = new JLabel("Dữ liệu minh họa · Tình hình hoạt động phòng khám hôm nay");
    private JButton selectedNavigationButton;
    private JButton expandedSectionButton;
    private JPanel expandedSectionItems;

    public MainFrame(String username) {
        super("Muse Dental | Hệ thống quản lý phòng khám");
        this.username = username == null || username.trim().isEmpty() ? "Nhân viên" : username.trim();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1080, 700));
        setSize(1360, 850);
        setLocationRelativeTo(null);
        setContentPane(createLayout());
        showDashboard();
    }

    private JPanel createLayout() {
        // Khung ứng dụng gồm thanh menu cố định và vùng làm việc thay đổi theo chức năng.
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CANVAS);
        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createWorkspace(), BorderLayout.CENTER);
        return root;
    }

    private JPanel createSidebar() {
        // Tạo logo, các nhóm menu có thể thu gọn và vùng đăng xuất.
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(264, 0));
        sidebar.setBackground(Color.WHITE);
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, LINE));

        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(24, 20, 20, 18));
        top.add(createLogo(), BorderLayout.WEST);

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel brandName = new JLabel("MUSE DENTAL");
        brandName.setFont(new Font("Arial", Font.BOLD, 16));
        brandName.setForeground(INK);
        JLabel brandSubtitle = new JLabel("CLINIC MANAGEMENT");
        brandSubtitle.setFont(new Font("Arial", Font.BOLD, 8));
        brandSubtitle.setForeground(MUTED);
        brand.add(brandName);
        brand.add(Box.createVerticalStrut(4));
        brand.add(brandSubtitle);
        top.add(brand, BorderLayout.CENTER);
        sidebar.add(top, BorderLayout.NORTH);

        JPanel navigation = new JPanel();
        navigation.setBackground(Color.WHITE);
        navigation.setBorder(new EmptyBorder(6, 12, 18, 12));
        navigation.setLayout(new BoxLayout(navigation, BoxLayout.Y_AXIS));
        navigation.add(createSectionLabel("TỔNG QUAN"));
        addNavigationItem(navigation, "Tổng quan", event -> showDashboard(), true);

        addCollapsibleSection(navigation, "HỆ THỐNG", new String[] {
                "Đổi mật khẩu",
                "Sao lưu & phục hồi",
                "Phân quyền hệ thống RBAC"
        });
        addCollapsibleSection(navigation, "DANH MỤC", new String[] {
                "Hồ sơ nhân viên",
                "Dịch vụ & bảng giá"
        });
        addCollapsibleSection(navigation, "CẬP NHẬT", new String[] {
                "Đặt lịch hẹn khám và nhắc lịch",
                "Tiếp đón và check-in",
                "Hồ sơ bệnh nhân",
                "Hồ sơ bệnh án điện tử EMR",
                "Khám lâm sàng và sơ đồ răng",
                "Kê đơn thuốc điện tử",
                "Phác đồ điều trị và dự toán chi phí",
                "Ghi nhận vật tư tiêu hao",
                "Lập hóa đơn và in hóa đơn",
                "Công nợ và trả góp theo đợt",
                "Kho thuốc và vật tư y tế"
        });
        addCollapsibleSection(navigation, "TÌM KIẾM", new String[] {
                "Tìm hồ sơ bệnh nhân",
                "Tìm hồ sơ nhân viên",
                "Tra cứu hồ sơ bệnh án",
                "Tra cứu hóa đơn / giao dịch",
                "Tra cứu tồn kho thuốc / dụng cụ"
        });
        addCollapsibleSection(navigation, "XỬ LÝ", new String[] {
                "Dashboard điều phối phòng khám",
                "Lịch làm việc cá nhân và báo bận",
                "Dashboard phân công ca trực Y tá",
                "Chuẩn bị dụng cụ và cập nhật trạng thái phòng",
                "Phân công lịch làm việc và ca trực"
        });
        addCollapsibleSection(navigation, "BÁO BIỂU", new String[] {
                "Báo cáo doanh thu",
                "Báo cáo hiệu suất nhân sự",
                "Thống kê lượt khám & lịch hẹn",
                "Báo cáo xuất / nhập / tồn kho",
                "Báo cáo thống kê và KPI nha sĩ"
        });
        navigation.add(Box.createVerticalGlue());

        JScrollPane navigationScroll = new JScrollPane(navigation);
        navigationScroll.setBorder(null);
        navigationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navigationScroll.getVerticalScrollBar().setUnitIncrement(16);
        sidebar.add(navigationScroll, BorderLayout.CENTER);

        sidebar.add(createAccountFooter(), BorderLayout.SOUTH);
        return sidebar;
    }

    // Giữ thông tin tài khoản và đăng xuất ở cuối thanh bên như mẫu giao diện.
    private JPanel createAccountFooter() {
        JPanel footer = new JPanel(new BorderLayout(0, 4));
        footer.setBackground(Color.WHITE);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, LINE),
                new EmptyBorder(12, 12, 12, 12)));

        JPanel accountCard = new JPanel(new BorderLayout(10, 0));
        accountCard.setBackground(new Color(244, 247, 250));
        accountCard.setBorder(new EmptyBorder(10, 10, 10, 8));
        accountCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        accountCard.add(new AccountAvatar(username), BorderLayout.WEST);

        JPanel accountDetails = new JPanel();
        accountDetails.setOpaque(false);
        accountDetails.setLayout(new BoxLayout(accountDetails, BoxLayout.Y_AXIS));
        JLabel accountName = new JLabel(username);
        accountName.setFont(new Font("Arial", Font.BOLD, 11));
        accountName.setForeground(INK);
        accountName.setAlignmentX(LEFT_ALIGNMENT);
        JLabel accountRole = new JLabel("Nhân viên phòng khám");
        accountRole.setFont(new Font("Arial", Font.PLAIN, 9));
        accountRole.setForeground(MUTED);
        accountRole.setAlignmentX(LEFT_ALIGNMENT);
        accountDetails.add(accountName);
        accountDetails.add(Box.createVerticalStrut(3));
        accountDetails.add(accountRole);
        accountCard.add(accountDetails, BorderLayout.CENTER);
        footer.add(accountCard, BorderLayout.NORTH);

        JButton logoutButton = new JButton("Đăng xuất", new LogoutIcon());
        logoutButton.setHorizontalAlignment(SwingConstants.LEFT);
        logoutButton.setIconTextGap(9);
        logoutButton.setFont(new Font("Arial", Font.PLAIN, 11));
        logoutButton.setForeground(MUTED);
        logoutButton.setBackground(new Color(244, 247, 250));
        logoutButton.setBorder(new EmptyBorder(10, 9, 5, 5));
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setContentAreaFilled(true);
        logoutButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutButton.addActionListener(event -> logout());
        // BorderLayout.CENTER kéo nút phủ hết chiều ngang bên trong footer.
        footer.add(logoutButton, BorderLayout.CENTER);
        return footer;
    }

    /**
     * Thêm nhóm accordion vào menu; bấm tiêu đề để mở hoặc thu gọn các chức năng con.
     * Mỗi lần chỉ mở một nhóm để thanh điều hướng luôn gọn và dễ sử dụng.
     */
    private void addCollapsibleSection(JPanel navigation, String title, String[] items) {
        JButton sectionButton = new JButton(title, new SectionToggleIcon(false));
        sectionButton.setHorizontalAlignment(SwingConstants.LEFT);
        sectionButton.setIconTextGap(10);
        sectionButton.setFont(new Font("Arial", Font.BOLD, 11));
        sectionButton.setForeground(MUTED);
        sectionButton.setBackground(Color.WHITE);
        sectionButton.setBorder(new EmptyBorder(11, 13, 11, 10));
        sectionButton.setFocusPainted(false);
        sectionButton.setBorderPainted(false);
        sectionButton.setContentAreaFilled(false);
        sectionButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        sectionButton.setAlignmentX(LEFT_ALIGNMENT);
        sectionButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel itemPanel = new JPanel();
        itemPanel.setBackground(Color.WHITE);
        itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.Y_AXIS));
        itemPanel.setVisible(false);

        for (String item : items) {
            JButton itemButton = createNavigationButton(item);
            itemButton.setBorder(new EmptyBorder(9, 22, 9, 8));
            itemButton.setAlignmentX(LEFT_ALIGNMENT);
            itemButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            itemButton.addActionListener(event -> {
                setSelectedNavigationButton(itemButton);
                showModule(item, title);
            });
            itemPanel.add(itemButton);
        }

        sectionButton.addActionListener(event -> {
            boolean shouldExpand = !itemPanel.isVisible();
            if (expandedSectionItems != null && expandedSectionItems != itemPanel) {
                expandedSectionItems.setVisible(false);
                expandedSectionButton.setIcon(new SectionToggleIcon(false));
            }

            itemPanel.setVisible(shouldExpand);
            sectionButton.setIcon(new SectionToggleIcon(shouldExpand));
            expandedSectionItems = shouldExpand ? itemPanel : null;
            expandedSectionButton = shouldExpand ? sectionButton : null;
            navigation.revalidate();
            navigation.repaint();
        });

        navigation.add(sectionButton);
        navigation.add(itemPanel);
    }

    // Đóng dashboard và mở lại màn hình đăng nhập.
    private void logout() {
        dispose();
        new LoginFrame().setVisible(true);
    }

    // Đọc logo từ resources để hiển thị nhận diện Muse ở đầu thanh điều hướng.
    private JLabel createLogo() {
        URL logoResource = getClass().getResource("/images/Logo.png");
        if (logoResource == null) {
            throw new IllegalStateException("Không tìm thấy logo tại resources/images/Logo.png.");
        }
        ImageIcon icon = new ImageIcon(logoResource);
        JLabel logo = new JLabel(new ImageIcon(icon.getImage().getScaledInstance(42, 42, java.awt.Image.SCALE_SMOOTH)));
        logo.setPreferredSize(new Dimension(42, 42));
        return logo;
    }

    // Tạo phần làm việc chứa tiêu đề trang và nội dung chức năng.
    private JPanel createWorkspace() {
        JPanel workspace = new JPanel(new BorderLayout());
        workspace.setBackground(CANVAS);
        workspace.add(createTopBar(), BorderLayout.NORTH);

        contentPanel.setBackground(CANVAS);
        contentPanel.setBorder(new EmptyBorder(28, 34, 34, 34));
        workspace.add(contentPanel, BorderLayout.CENTER);
        return workspace;
    }

    // Hiển thị tiêu đề trang và ô tìm nhanh trên thanh đầu trang.
    private JPanel createTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(Color.WHITE);
        topBar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, LINE),
                new EmptyBorder(15, 30, 15, 30)));

        JPanel pageHeading = new JPanel();
        pageHeading.setOpaque(false);
        pageHeading.setLayout(new BoxLayout(pageHeading, BoxLayout.Y_AXIS));
        pageTitle.setFont(new Font("Arial", Font.BOLD, 19));
        pageTitle.setForeground(INK);
        pageDescription.setFont(new Font("Arial", Font.PLAIN, 11));
        pageDescription.setForeground(MUTED);
        pageHeading.add(pageTitle);
        pageHeading.add(Box.createVerticalStrut(4));
        pageHeading.add(pageDescription);
        topBar.add(pageHeading, BorderLayout.WEST);

        JPanel right = new JPanel(new BorderLayout());
        right.setOpaque(false);
        JTextField search = new JTextField();
        search.setFont(new Font("Arial", Font.PLAIN, 12));
        search.setToolTipText("Tìm nhanh bệnh nhân, lịch hẹn...");
        search.setPreferredSize(new Dimension(230, 36));
        search.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(0, 11, 0, 11)));
        right.add(search, BorderLayout.EAST);
        topBar.add(right, BorderLayout.EAST);
        return topBar;
    }

    // Thêm tiêu đề nhóm, ví dụ HỆ THỐNG hoặc CẬP NHẬT, vào thanh điều hướng.
    private JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 9));
        label.setForeground(new Color(151, 169, 177));
        label.setBorder(new EmptyBorder(19, 12, 8, 0));
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    // Tạo nút điều hướng độc lập, dùng cho trang tổng quan.
    private void addNavigationItem(
            JPanel navigation,
            String title,
            java.util.function.Consumer<ActionEvent> action,
            boolean selected) {
        JButton button = createNavigationButton(title);
        button.setAlignmentX(LEFT_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        button.addActionListener(event -> {
            setSelectedNavigationButton(button);
            action.accept(event);
        });
        navigation.add(button);
        if (selected) {
            setSelectedNavigationButton(button);
        }
    }

    // Dùng chung kiểu nút cho menu chính và các mục con để giữ giao diện đồng nhất.
    private JButton createNavigationButton(String title) {
        // Dùng icon tự vẽ với chữ viết tắt, tránh phụ thuộc ký tự biểu tượng của hệ điều hành.
        JButton button = new JButton(title, new MenuBadgeIcon(title));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setIconTextGap(10);
        button.setFont(new Font("Arial", Font.PLAIN, 12));
        button.setForeground(MUTED);
        button.setBackground(Color.WHITE);
        button.setBorder(new EmptyBorder(11, 13, 11, 10));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    // Đánh dấu mục vừa chọn để người dùng nhận biết vị trí hiện tại.
    private void setSelectedNavigationButton(JButton button) {
        if (selectedNavigationButton != null) {
            selectedNavigationButton.setForeground(MUTED);
            selectedNavigationButton.setBackground(Color.WHITE);
            selectedNavigationButton.setContentAreaFilled(false);
        }
        selectedNavigationButton = button;
        selectedNavigationButton.setForeground(TEAL);
        selectedNavigationButton.setBackground(new Color(231, 246, 245));
        selectedNavigationButton.setContentAreaFilled(true);
    }

    private void showDashboard() {
        // Hiển thị trang tổng quan; số liệu hiện tại là dữ liệu minh họa, chưa lấy từ CSDL.
        pageTitle.setText("Tổng quan");
        pageDescription.setText("Dữ liệu minh họa · Tình hình hoạt động phòng khám hôm nay");
        contentPanel.removeAll();
        contentPanel.add(createDashboardContent(), BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Kết hợp lời chào, các chỉ số chính và lịch hẹn mẫu thành trang tổng quan.
    private JPanel createDashboardContent() {
        JPanel dashboard = new JPanel(new BorderLayout(0, 22));
        dashboard.setOpaque(false);
        dashboard.add(createGreeting(), BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(0, 22));
        center.setOpaque(false);
        center.add(createSummaryCards(), BorderLayout.NORTH);
        center.add(createActivityPanel(), BorderLayout.CENTER);
        dashboard.add(center, BorderLayout.CENTER);
        return dashboard;
    }

    // Hiển thị lời chào theo tài khoản và ngày hiện tại.
    private JPanel createGreeting() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel greeting = new JLabel("Xin chào, " + username);
        greeting.setFont(new Font("Arial", Font.BOLD, 23));
        greeting.setForeground(INK);
        JLabel description = new JLabel("Chúc bạn một ngày làm việc hiệu quả tại Muse Dental.");
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setForeground(MUTED);
        text.add(greeting);
        text.add(Box.createVerticalStrut(7));
        text.add(description);
        panel.add(text, BorderLayout.WEST);

        String date = LocalDate.now().format(
                DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", Locale.forLanguageTag("vi-VN")));
        JLabel dateLabel = new JLabel(date);
        dateLabel.setFont(new Font("Arial", Font.BOLD, 11));
        dateLabel.setForeground(MUTED);
        panel.add(dateLabel, BorderLayout.EAST);
        return panel;
    }

    // Tạo các thẻ thống kê nhanh; số liệu hiện tại chỉ phục vụ minh họa giao diện.
    private JPanel createSummaryCards() {
        // Các thẻ tóm tắt những chỉ số vận hành chính theo menu dự án.
        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(createSummaryCard("LỊCH HẸN HÔM NAY", "24", "+ 4 so với hôm qua", "LH", new Color(231, 246, 245)));
        cards.add(createSummaryCard("BỆNH NHÂN MỚI", "08", "Trong ngày hôm nay", "BN", new Color(237, 242, 255)));
        cards.add(createSummaryCard("CHỜ TIẾP ĐÓN", "05", "Cần được hỗ trợ", "TD", new Color(255, 246, 232)));
        cards.add(createSummaryCard("DOANH THU HÔM NAY", "12,8 tr", "Tạm tính trong ngày", "DT", new Color(253, 237, 244)));
        return cards;
    }

    // Dựng một thẻ thống kê gồm nhãn, giá trị, ghi chú và biểu tượng.
    private JPanel createSummaryCard(String title, String value, String note, String icon, Color iconBackground) {
        JPanel card = new JPanel(new BorderLayout(0, 17));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(18, 18, 17, 18)));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 9));
        titleLabel.setForeground(MUTED);
        JLabel iconLabel = new JLabel(icon, SwingConstants.CENTER);
        iconLabel.setFont(new Font("Arial", Font.BOLD, 15));
        iconLabel.setForeground(TEAL);
        iconLabel.setOpaque(true);
        iconLabel.setBackground(iconBackground);
        iconLabel.setPreferredSize(new Dimension(34, 34));
        top.add(titleLabel, BorderLayout.WEST);
        top.add(iconLabel, BorderLayout.EAST);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 25));
        valueLabel.setForeground(INK);
        JLabel noteLabel = new JLabel(note);
        noteLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        noteLabel.setForeground(MUTED);
        bottom.add(valueLabel);
        bottom.add(Box.createVerticalStrut(5));
        bottom.add(noteLabel);
        card.add(top, BorderLayout.NORTH);
        card.add(bottom, BorderLayout.CENTER);
        return card;
    }

    // Hiển thị danh sách các lịch hẹn mẫu trong ngày.
    private JPanel createActivityPanel() {
        // Danh sách lịch hẹn mẫu để minh họa khu vực điều phối trong dashboard.
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(LINE),
                new EmptyBorder(20, 21, 18, 21)));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel title = new JLabel("Lịch hẹn sắp tới");
        title.setFont(new Font("Arial", Font.BOLD, 15));
        title.setForeground(INK);
        JButton viewAll = new JButton("Xem tất cả");
        viewAll.setFont(new Font("Arial", Font.BOLD, 11));
        viewAll.setForeground(TEAL);
        viewAll.setBorderPainted(false);
        viewAll.setContentAreaFilled(false);
        viewAll.setFocusPainted(false);
        viewAll.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewAll.addActionListener(event -> showModule("Lịch hẹn & tiếp đón", "Cập nhật"));
        heading.add(title, BorderLayout.WEST);
        heading.add(viewAll, BorderLayout.EAST);

        JPanel appointmentList = new JPanel(new GridLayout(4, 1, 0, 0));
        appointmentList.setOpaque(false);
        appointmentList.add(createAppointmentRow("09:00", "Nguyễn Minh Anh", "Khám tổng quát · BS. Tuấn", "Đang chờ", new Color(255, 246, 232)));
        appointmentList.add(createAppointmentRow("09:30", "Trần Hoàng Nam", "Lấy cao răng · BS. Linh", "Đã xác nhận", new Color(231, 246, 245)));
        appointmentList.add(createAppointmentRow("10:00", "Lê Ngọc Hà", "Tái khám · BS. Tuấn", "Đã xác nhận", new Color(231, 246, 245)));
        appointmentList.add(createAppointmentRow("10:30", "Phạm Gia Bảo", "Tư vấn điều trị · BS. Minh", "Đang chờ", new Color(255, 246, 232)));

        panel.add(heading, BorderLayout.NORTH);
        panel.add(appointmentList, BorderLayout.CENTER);
        return panel;
    }

    // Dựng một dòng lịch hẹn với giờ, thông tin bệnh nhân và trạng thái.
    private JPanel createAppointmentRow(String time, String patient, String detail, String status, Color statusBackground) {
        JPanel row = new JPanel(new BorderLayout(14, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, LINE));

        JLabel timeLabel = new JLabel(time, SwingConstants.CENTER);
        timeLabel.setFont(new Font("Arial", Font.BOLD, 12));
        timeLabel.setForeground(TEAL);
        timeLabel.setPreferredSize(new Dimension(62, 50));

        JPanel patientInfo = new JPanel();
        patientInfo.setOpaque(false);
        patientInfo.setLayout(new BoxLayout(patientInfo, BoxLayout.Y_AXIS));
        JLabel patientLabel = new JLabel(patient);
        patientLabel.setFont(new Font("Arial", Font.BOLD, 12));
        patientLabel.setForeground(INK);
        JLabel detailLabel = new JLabel(detail);
        detailLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        detailLabel.setForeground(MUTED);
        patientInfo.add(patientLabel);
        patientInfo.add(Box.createVerticalStrut(5));
        patientInfo.add(detailLabel);

        JLabel statusLabel = new JLabel(status, SwingConstants.CENTER);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 9));
        statusLabel.setForeground(statusBackground.equals(new Color(231, 246, 245)) ? TEAL : new Color(177, 125, 47));
        statusLabel.setOpaque(true);
        statusLabel.setBackground(statusBackground);
        statusLabel.setBorder(new EmptyBorder(6, 9, 6, 9));

        row.add(timeLabel, BorderLayout.WEST);
        row.add(patientInfo, BorderLayout.CENTER);
        row.add(statusLabel, BorderLayout.EAST);
        return row;
    }

    // Thông báo rõ các màn hình nghiệp vụ chưa được nối với xử lý hoặc cơ sở dữ liệu.
    private void showModule(String title, String category) {
        // Các chức năng nghiệp vụ chưa triển khai được báo rõ thay vì hiển thị dữ liệu giả.
        pageTitle.setText(title);
        pageDescription.setText(category + " · Hệ thống quản lý phòng khám Muse Dental");
        JPanel placeholder = new JPanel(new GridBagLayout());
        placeholder.setBackground(Color.WHITE);
        placeholder.setBorder(BorderFactory.createLineBorder(LINE));
        JPanel message = new JPanel();
        message.setOpaque(false);
        message.setLayout(new BoxLayout(message, BoxLayout.Y_AXIS));
        JLabel icon = new JLabel("i", SwingConstants.CENTER);
        icon.setAlignmentX(CENTER_ALIGNMENT);
        icon.setFont(new Font("Arial", Font.PLAIN, 32));
        icon.setForeground(TEAL);
        JLabel heading = new JLabel("Đang xây dựng chức năng");
        heading.setAlignmentX(CENTER_ALIGNMENT);
        heading.setFont(new Font("Arial", Font.BOLD, 18));
        heading.setForeground(INK);
        JLabel description = new JLabel("Mục \"" + title + "\" sẽ được triển khai theo menu dự án.");
        description.setAlignmentX(CENTER_ALIGNMENT);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setForeground(MUTED);
        message.add(icon);
        message.add(Box.createVerticalStrut(12));
        message.add(heading);
        message.add(Box.createVerticalStrut(8));
        message.add(description);
        placeholder.add(message);

        contentPanel.removeAll();
        contentPanel.add(placeholder, BorderLayout.CENTER);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Avatar dạng vòng tròn với chữ cái đầu tài khoản, không cần phụ thuộc icon font.
    private static final class AccountAvatar extends JPanel {
        private final String initials;

        private AccountAvatar(String accountName) {
            setOpaque(false);
            setPreferredSize(new Dimension(36, 36));
            setMinimumSize(new Dimension(36, 36));
            String trimmedName = accountName.trim();
            initials = trimmedName.isEmpty()
                    ? "NV"
                    : trimmedName.substring(0, Math.min(2, trimmedName.length())).toUpperCase(Locale.ROOT);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(51, 91, 177));
            g.fillOval(0, 0, getWidth(), getHeight());
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics metrics = g.getFontMetrics();
            int textX = (getWidth() - metrics.stringWidth(initials)) / 2;
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(initials, textX, textY);
            g.dispose();
        }
    }

    // Icon đăng xuất vẽ bằng nét vector để hiển thị nhất quán trên các font hệ thống.
    private static final class LogoutIcon implements Icon {
        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(MUTED);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + 2, y + 2, x + 2, y + 14);
            g.drawLine(x + 2, y + 2, x + 7, y + 2);
            g.drawLine(x + 2, y + 14, x + 7, y + 14);
            g.drawLine(x + 5, y + 8, x + 14, y + 8);
            g.drawLine(x + 10, y + 4, x + 14, y + 8);
            g.drawLine(x + 10, y + 12, x + 14, y + 8);
            g.dispose();
        }
    }

    /**
     * Icon menu vẽ chữ viết tắt ASCII trong huy hiệu màu, không phụ thuộc font icon.
     */
    private static final class MenuBadgeIcon implements Icon {
        private final String abbreviation;

        private MenuBadgeIcon(String title) {
            String normalized = Normalizer.normalize(title, Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "")
                    .replace('đ', 'd')
                    .replace('Đ', 'D');
            String[] words = normalized.split("[^A-Za-z0-9]+");
            StringBuilder initials = new StringBuilder(2);
            for (String word : words) {
                if (!word.isEmpty()) {
                    initials.append(Character.toUpperCase(word.charAt(0)));
                    if (initials.length() == 2) {
                        break;
                    }
                }
            }
            abbreviation = initials.length() == 0 ? "MD" : initials.toString();
        }

        @Override
        public int getIconWidth() {
            return 26;
        }

        @Override
        public int getIconHeight() {
            return 26;
        }

        @Override
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(231, 246, 245));
            g.fillRoundRect(x, y, getIconWidth(), getIconHeight(), 8, 8);
            g.setColor(TEAL);
            g.setFont(new Font(Font.DIALOG, Font.BOLD, 9));
            FontMetrics metrics = g.getFontMetrics();
            int textX = x + (getIconWidth() - metrics.stringWidth(abbreviation)) / 2;
            int textY = y + (getIconHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(abbreviation, textX, textY);
            g.dispose();
        }
    }

    /**
     * Mở rộng/thu gọn được vẽ bằng nét ngang và dọc thay ký tự mũi tên dễ lỗi font.
     */
    private static final class SectionToggleIcon implements Icon {
        private final boolean expanded;

        private SectionToggleIcon(boolean expanded) {
            this.expanded = expanded;
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
        public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(MUTED);
            g.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int centerX = x + getIconWidth() / 2;
            int centerY = y + getIconHeight() / 2;
            g.drawLine(centerX - 4, centerY, centerX + 4, centerY);
            if (!expanded) {
                g.drawLine(centerX, centerY - 4, centerX, centerY + 4);
            }
            g.dispose();
        }
    }

    // Điểm khởi chạy độc lập để có thể mở trực tiếp dashboard khi phát triển giao diện.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception exception) {
                System.err.println("Không thể áp dụng giao diện hệ điều hành: " + exception.getMessage());
            }
            new MainFrame("Nhân viên").setVisible(true);
        });
    }
}
