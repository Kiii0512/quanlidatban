package test;

import javax.swing.*;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.MaskFormatter;
import javax.swing.event.TableModelEvent;
import java.awt.*;
import java.awt.Image;
import java.awt.Graphics;
import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import dangnhap2.RestaurantBookingLoginUI;
import dao.EmployeeDAO;
import model.Employee;
import dao.TableDAO;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import model.Table;
import java.sql.SQLException;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import dao.ReservationDAO;
import model.Reservation;

public class CombinedRestaurantUI extends JFrame {

    private JTextField createFlatTextField(int cols) {
        JTextField tf = new JTextField(cols);
        tf.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xB0BEC5)));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(0x212121));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    private JPasswordField createFlatPasswordField(int cols) {
        JPasswordField pf = new JPasswordField(cols);
        pf.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xB0BEC5)));
        pf.setBackground(Color.WHITE);
        pf.setForeground(new Color(0x212121));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return pf;
    }
    private final CardLayout cardLayout;
    private JLabel lblWelcome;

    private String employeeName;
    private String employeeId;

    private final JPanel mainPanel;

    private final DefaultTableModel bookingTableModel;
    private DefaultTableModel tableListModel;
    private DefaultTableModel currentOrdersModel;
    private DefaultTableModel completedOrdersModel;
    private DefaultTableModel canceledOrdersModel;
    private JTable tblBooking;
    private JTable tblCurrentOrders;

    private JTextField nameField, emailField, phoneField, tableNumberField;
    private JFormattedTextField dateField, timeField;
    private JTextArea messageArea;
    private ImagePanel avatar;       // thay cho ImagePanel cục bộ
    private JLabel lblName;
    private JLabel lblAddr;
    private JLabel lblAge;
    private JLabel lblGender;
    private JLabel lblTitle;
    private ImagePanel homeAvatar;
    private model.Employee currentEmployee;
    private TableDAO tableDAO = new TableDAO();
    private JTable tableList;
    private ReservationDAO reservationDAO;
    private DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private javax.swing.JTextField txtEmail;

    public CombinedRestaurantUI() {
        
        UIManager.put("Panel.background", new Color(0xF2F4F5));        
        UIManager.put("Button.background", new Color(0xA3AC85));     
        UIManager.put("Button.foreground", Color.WHITE);             
        UIManager.put("Table.gridColor", new Color(0xCFD8DC));
        UIManager.put("Table.foreground", new Color(0x212121));
        UIManager.put("TableHeader.background", new Color(0x263238));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        
        
        setTitle("Giao diện Nhân viên");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // ------------ Models ------------
        String[] bookingCols = {"Họ tên", "Email", "SĐT", "Ngày", "Giờ", "Số bàn", "Tin nhắn"};
        bookingTableModel = new DefaultTableModel(bookingCols, 0);

        String[] tableCols = {"Số bàn", "Loại bàn", "Số chỗ", "Trạng thái"};

        tableListModel = new DefaultTableModel(tableCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return true;
            }
        };
        String[] orderCols = {"Họ tên", "Email", "SĐT", "Ngày", "Giờ", "Số bàn", "Tin nhắn"};
        currentOrdersModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        completedOrdersModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        canceledOrdersModel = new DefaultTableModel(orderCols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        // ------------ Panels ------------
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createHomePanel(), "Home");
        mainPanel.add(createStaffPanel(), "Staff");
        mainPanel.add(createTablePanel(), "Table");
        mainPanel.add(createBookingPanel(), "Booking");
        mainPanel.add(createOrderManagementPanel(), "Orders");
        add(mainPanel);
        loadTableData();
        cardLayout.show(mainPanel, "Home");
        try {
            reservationDAO = new ReservationDAO();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Không kết nối được DB:\n" + ex.getMessage(),
                    "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        refreshBookingTable();
        refreshOrderTables();
    }

    public CombinedRestaurantUI(String employeeId, String employeeName) {
        this();
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        lblWelcome.setText(employeeName + " - Mã: " + employeeId);
        setLocationRelativeTo(null);
        loadAndApplyEmployeeInfo();
    }

    private JPanel createHomePanel() {
        JPanel home = new JPanel(new BorderLayout(10, 10));
        home.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        home.setBackground(new Color(255, 204, 204));

        // === Phần trái: imgBg chứa JTable ===
        ImagePanel imgBg = new ImagePanel("restaurant.jpg");
        imgBg.setLayout(new BorderLayout(5, 5));
        TitledBorder tb = BorderFactory.createTitledBorder("Danh sách đặt bàn");
        tb.setTitleColor(Color.WHITE);
        imgBg.setBorder(tb);

        JTable tbl = new JTable(bookingTableModel) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tbl.setOpaque(false);
        tbl.setBackground(new Color(0, 0, 0, 0));
        tbl.setForeground(Color.WHITE);
        tbl.setRowHeight(24);
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc,
                    int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                ((JComponent) comp).setOpaque(false);
                return comp;
            }
        });
        JScrollPane sp = new JScrollPane(tbl);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        imgBg.add(sp, BorderLayout.CENTER);

        // === Phần phải: nav (avatar, info, nút) ===
        JPanel nav = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Avatar
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0.7;
        homeAvatar = new ImagePanel();
        homeAvatar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        nav.add(homeAvatar, gbc);

        // Thông tin nhân viên
        gbc.gridy = 1;
        gbc.weighty = 0.3;
        JPanel info = new JPanel(new BorderLayout());
        info.setBorder(BorderFactory.createTitledBorder("Thông tin nhân viên"));
        lblWelcome = new JLabel(employeeName != null ? employeeName + " - Mã: " + employeeId : "",
                SwingConstants.CENTER);
        info.add(lblWelcome, BorderLayout.CENTER);
        nav.add(info, gbc);

        // Các nút
        gbc.gridy = 2;
        gbc.weighty = 0;
        JPanel btns = new JPanel(new GridLayout(4, 1, 5, 5));
        JButton b1 = new JButton("Thông tin nhân viên");
        b1.addActionListener(e -> cardLayout.show(mainPanel, "Staff"));
        JButton b2 = new JButton("Danh sách bàn");
        b2.addActionListener(e -> cardLayout.show(mainPanel, "Table"));
        JButton b3 = new JButton("Đặt bàn");
        b3.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));
        JButton b4 = new JButton("Đăng xuất");
        b4.addActionListener(e -> logout());
        btns.add(b1);
        btns.add(b2);
        btns.add(b3);
        btns.add(b4);
        nav.add(btns, gbc);

        // === Gộp vào SplitPane 65/35 ===
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, imgBg, nav);
        split.setResizeWeight(0.65);
        split.setDividerLocation(0.65);
        split.setContinuousLayout(true);
        split.setOneTouchExpandable(true);
        split.setDividerSize(5);

        home.add(split, BorderLayout.CENTER);
        return home;
    }

    private JPanel createStaffPanel() {
        // 1. Panel chính
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(210, 213, 200)); //Đây là nền ngoài cùng

        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ----- Bên trái: avatar + nút Quay lại -----
        JPanel left = new JPanel(new BorderLayout(10, 10));
        left.setOpaque(true);
        left.setBackground(new Color(0xFF, 0xFF, 0xFF));   // đổi nền left

        // Avatar ở top, để tỷ lệ rộng/hẹp
        avatar = new ImagePanel();
        avatar.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        left.add(avatar, BorderLayout.CENTER);

        // Nút quay lại ở dưới
        JButton back = new JButton("Quay lại");
        back.setBackground(new Color(0xA3, 0xB1, 0x8A));    // đổi màu nút
        back.setForeground(Color.WHITE);
        back.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        JPanel btnWrap = new JPanel(); // để căn giữa ngang
        btnWrap.setOpaque(false);
        btnWrap.add(back);
        left.add(btnWrap, BorderLayout.SOUTH);

        // ----- Bên phải: thông tin nhân viên -----
        JPanel right = new JPanel();
        right.setOpaque(true);
        right.setBackground(new Color(0xE8, 0xEF, 0xE6));  // đổi nền right
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK), "Thông tin nhân viên"));

        // Khởi tạo các label nếu chưa
        lblName = new JLabel();
        lblAddr = new JLabel();
        lblAge = new JLabel();
        lblGender = new JLabel();
        lblTitle = new JLabel();

        // Tăng kích thước font cho dễ đọc
        Font infoFont = lblName.getFont().deriveFont(Font.PLAIN, 18f);
        lblName.setFont(infoFont);
        lblAddr.setFont(infoFont);
        lblAge.setFont(infoFont);
        lblGender.setFont(infoFont);
        lblTitle.setFont(infoFont);

        // Thêm khoảng cách giữa các label
        right.add(Box.createVerticalStrut(20));
        right.add(lblName);
        right.add(Box.createVerticalStrut(15));
        right.add(lblAddr);
        right.add(Box.createVerticalStrut(15));
        right.add(lblAge);
        right.add(Box.createVerticalStrut(15));
        right.add(lblGender);
        right.add(Box.createVerticalStrut(15));
        right.add(lblTitle);
        right.add(Box.createVerticalGlue());

        // ----- Gộp vào split pane -----
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        split.setResizeWeight(0.5);           // chia đều 50/50
        split.setDividerSize(3);
        split.setOneTouchExpandable(true);
        split.setContinuousLayout(true);

        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 1) Khởi tạo model và table
        String[] cols = {"Số bàn", "Loại bàn", "Số chỗ", "Trạng thái"};
        tableListModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 0;
            }
        };
        tableList = new JTable(tableListModel);

        Font vietnamese = new Font("Segoe UI", Font.PLAIN, 14);
        tableList.setFont(vietnamese);
        tableList.getTableHeader().setFont(vietnamese);

        JComboBox<String> comboLoai = new JComboBox<>(new String[]{"Thường", "VIP"});
        comboLoai.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tableList.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(comboLoai));

        String[] soCho = new String[9];
        for (int i = 0; i < 9; i++) {
            soCho[i] = String.valueOf(i + 2);
        }
        tableList.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(new JComboBox<>(soCho)));
        String[] states = {"Còn trống", "Đang sử dụng", "Đã được đặt", "Không khả dụng"};
        tableList.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(new JComboBox<>(states)));

        tableList.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                try {
                    String id = tableListModel.getValueAt(row, 0).toString();
                    String type = tableListModel.getValueAt(row, 1).toString();
                    int seats = Integer.parseInt(tableListModel.getValueAt(row, 2).toString());
                    String status = tableListModel.getValueAt(row, 3).toString();

                    Table t = new Table(id, type, seats, status);
                    tableDAO.update(t);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Lỗi khi cập nhật bàn:\n" + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        JScrollPane sp = new JScrollPane(tableList);
        panel.add(sp, BorderLayout.CENTER);
        JPanel btns = new JPanel();
        JButton addBtn = new JButton("Thêm bàn");
        addBtn.addActionListener(e -> addTableDialog());
        JButton delBtn = new JButton("Xóa bàn");
        delBtn.addActionListener(e -> deleteTableDialog());
        JButton back = new JButton("Quay lại");
        back.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        btns.add(addBtn);
        btns.add(delBtn);
        btns.add(back);
        panel.add(btns, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createBookingPanel() {
        JPanel form = new JPanel(new GridLayout(7, 2, 5, 5));
        form.setBackground(new Color(232, 239, 232));
        form.setBorder(BorderFactory.createTitledBorder("Thông tin đặt bàn"));
        nameField = createFlatTextField(20);
        emailField = createFlatTextField(20);
        phoneField = createFlatTextField(20);
        tableNumberField = createFlatTextField(20);
        try {
            MaskFormatter dm = new MaskFormatter("##/##/####");
            dm.setPlaceholderCharacter(' ');
            dateField = new JFormattedTextField(dm);
        } catch (ParseException ex) {
            dateField = new JFormattedTextField();
        }
        try {
            MaskFormatter tm = new MaskFormatter("##:##");
            tm.setPlaceholderCharacter(' ');
            timeField = new JFormattedTextField(tm);
        } catch (ParseException ex) {
            timeField = new JFormattedTextField();
        }
        messageArea = new JTextArea(3, 20);
        form.add(new JLabel("Họ và tên:"));
        form.add(nameField);
        form.add(new JLabel("Email:"));
        form.add(emailField);
        form.add(new JLabel("Số điện thoại:"));
        form.add(phoneField);
        form.add(new JLabel("Số bàn:"));
        form.add(tableNumberField);
        form.add(new JLabel("Ngày (dd/mm/yyyy):"));
        form.add(dateField);
        form.add(new JLabel("Giờ (hh:mm):"));
        form.add(timeField);
        form.add(new JLabel("Tin nhắn:"));
        form.add(new JScrollPane(messageArea));

        // Nút Xác nhận đặt bàn
        JButton btnConfirm = new JButton("Xác nhận");
        btnConfirm.addActionListener(e -> handleBooking());

// Nút Tìm kiếm đơn hiện tại
        JButton btnSearch = new JButton("Tìm kiếm");
        btnSearch.addActionListener(e -> openSearchDialog());

// Nút Chuyển sang trang Quản lý đơn
        JButton btnOrders = new JButton("Các đơn đặt bàn");
        btnOrders.addActionListener(e -> cardLayout.show(mainPanel, "Orders"));

// Nút Quay lại Home
        JButton btnBack = new JButton("Quay lại");
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Home"));

// Panel chứa nút
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        btnPanel.add(btnConfirm);
        btnPanel.add(btnSearch);
        btnPanel.add(btnOrders);
        btnPanel.add(btnBack);
        ImagePanel bg1 = new ImagePanel("restaurant.jpg");
        bg1.setLayout(new BorderLayout());
        TitledBorder b1 = BorderFactory.createTitledBorder("Danh sách đặt bàn");
        b1.setTitleColor(Color.WHITE);
        bg1.setBorder(b1);

        tblBooking = new JTable(bookingTableModel) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblBooking.setOpaque(false);
        tblBooking.setBackground(new Color(0, 0, 0, 0));
        tblBooking.setForeground(Color.WHITE);
        tblBooking.setRowHeight(24);
        tblBooking.addMouseListener(new MouseAdapter() {
            private void selectRowIfPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = tblBooking.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < tblBooking.getRowCount()) {
                        tblBooking.setRowSelectionInterval(row, row);
                    }
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                selectRowIfPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectRowIfPopup(e);
            }
        });
// Thêm popup menu
        JPopupMenu popup = new JPopupMenu();

        JMenuItem miComplete = new JMenuItem("Hoàn tất");
        miComplete.addActionListener(e -> performMoveSelected(bookingTableModel, completedOrdersModel));
        JMenuItem miCancel = new JMenuItem("Hủy đơn");
        miCancel.addActionListener(e -> performMoveSelected(bookingTableModel, canceledOrdersModel));
        popup.add(miComplete);
        popup.add(miCancel);
        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                Point p = tblBooking.getMousePosition();
                if (p != null) {
                    int row = tblBooking.rowAtPoint(p);
                    tblBooking.getSelectionModel().setSelectionInterval(row, row);
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
            }
        });
        tblBooking.setComponentPopupMenu(popup);

        JScrollPane sp1 = new JScrollPane(tblBooking);
        sp1.setOpaque(false);
        sp1.getViewport().setOpaque(false);
        bg1.add(sp1, BorderLayout.CENTER);

        ImagePanel bg2 = new ImagePanel("restaurant.jpg");
        bg2.setLayout(new BorderLayout());
        TitledBorder b2 = BorderFactory.createTitledBorder("Danh sách bàn (chỉ xem)");
        b2.setTitleColor(Color.WHITE);
        bg2.setBorder(b2);
        JTable tblList = new JTable(tableListModel) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tblList.setRowHeight(24);
        tblList.setEnabled(false);
        JScrollPane sp2 = new JScrollPane(tblList);
        bg2.add(sp2, BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, bg1, bg2);
        split.setResizeWeight(0.5);
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);
        center.add(form, BorderLayout.WEST);
        center.add(split, BorderLayout.CENTER);

        JPanel panel = new ImagePanel("restaurant.jpg");
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(center, BorderLayout.CENTER);
        panel.add(btnPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void handleBooking() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String tblNo = tableNumberField.getText().trim();
        String date = dateField.getText().trim();
        String time = timeField.getText().trim();
        String msg = messageArea.getText().trim();
        if (name.isEmpty() || email.isEmpty() || phone.isEmpty()
                || date.isEmpty() || time.isEmpty() || tblNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không để trống");
            return;
        }
        int idx = -1;
        for (int i = 0; i < tableListModel.getRowCount(); i++) {
            if (tableListModel.getValueAt(i, 0).toString().equals(tblNo)) {
                idx = i;
                break;
            }
        }
        if (idx < 0) {
            JOptionPane.showMessageDialog(this, "Bàn không tồn tại!");
            return;
        }
        String status = tableListModel.getValueAt(idx, 3).toString();
        if (!status.equals("Còn trống")) {
            JOptionPane.showMessageDialog(this, "Bàn không khả dụng: " + status);
            return;
        }
        boolean ev = email.contains("@");
        boolean pv = phone.matches("0\\d{9}");
        boolean dv = validateDate(date);
        boolean tv = validateTime(time);
        int inv = (!ev ? 1 : 0) + (!pv ? 1 : 0) + (!dv ? 1 : 0) + (!tv ? 1 : 0);
        if (inv >= 2) {
            JOptionPane.showMessageDialog(this, "Sai một số thông tin.Vui lòng thử lại!");
            return;
        }
        if (!pv) {
            JOptionPane.showMessageDialog(this, "SĐT sai định dạng");
            return;
        }
        if (!ev) {
            JOptionPane.showMessageDialog(this, "Email sai định dạng");
            return;
        }
        if (!dv) {
            JOptionPane.showMessageDialog(this, "Ngày sai định dạng");
            return;
        }
        if (!tv) {
            JOptionPane.showMessageDialog(this, "Giờ sai định dạng");
            return;
        }
        for (int i = 0; i < bookingTableModel.getRowCount(); i++) {
            if (bookingTableModel.getValueAt(i, 0).equals(name)
                    && bookingTableModel.getValueAt(i, 1).equals(email)
                    && bookingTableModel.getValueAt(i, 2).equals(phone)
                    && bookingTableModel.getValueAt(i, 3).equals(date)
                    && bookingTableModel.getValueAt(i, 4).equals(time)
                    && bookingTableModel.getValueAt(i, 5).equals(tblNo)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Thông tin bị trùng với đơn có sẵn!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE
                );
                return;  // dừng xử lý, không thêm đơn mới
            }
        }
        try {
            // 1) Chuyển đổi ngày, giờ và số bàn
            LocalDate d = LocalDate.parse(date, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            LocalTime t = LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            int tableNumber = Integer.parseInt(tblNo);

            // 2) Lưu vào DB
            Reservation r = new Reservation(name, email, phone, d, t, tableNumber, msg);
            reservationDAO.addReservation(r);

            // 3) Load lại bảng từ DB
            refreshBookingTable();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu đặt bàn: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        tableListModel.setValueAt("Đã được đặt", idx, 3);
        JOptionPane.showMessageDialog(this, "Đặt bàn thành công!");

        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        tableNumberField.setText("");
        dateField.setText("");
        timeField.setText("");
        messageArea.setText("");
    }

    private void openSearchDialog() {
        JTextField fName = new JTextField();
        JTextField fPhone = new JTextField();
        JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
        p.add(new JLabel("Họ tên:"));
        p.add(fName);
        p.add(new JLabel("SĐT:"));
        p.add(fPhone);

        int r = JOptionPane.showConfirmDialog(
                this, p, "Tìm đặt bàn", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        String name = fName.getText().trim();
        String phone = fPhone.getText().trim();
        // tìm trong currentOrdersModel
        for (int i = 0; i < currentOrdersModel.getRowCount(); i++) {
            if (currentOrdersModel.getValueAt(i, 0).equals(name)
                    && currentOrdersModel.getValueAt(i, 2).equals(phone)) {
                showOrderActionDialog(i);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Không tìm thấy đơn đặt bàn.");
    }

// 2. Dialog chi tiết đơn với lựa chọn Hoàn tất / Hủy / Quay lại
    private void showOrderActionDialog(int rowIndex) {
        // Lấy dữ liệu dòng
        Object[] data = new Object[currentOrdersModel.getColumnCount()];
        for (int j = 0; j < data.length; j++) {
            data[j] = currentOrdersModel.getValueAt(rowIndex, j);
        }
        // Format thông tin
        StringBuilder sb = new StringBuilder();
        String[] cols = {"Họ tên", "Email", "SĐT", "Số bàn", "Ngày", "Giờ", "Tin nhắn"};
        for (int j = 0; j < cols.length; j++) {
            sb.append(cols[j]).append(": ").append(data[j]).append("\n");
        }
        JTextArea info = new JTextArea(sb.toString());
        info.setEditable(false);

        int choice = JOptionPane.showOptionDialog(
                this,
                new JScrollPane(info),
                "Chi tiết đơn",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Quay lại", "Hoàn tất", "Hủy đơn"},
                "Quay lại"
        );
        if (choice == 1) {
            moveOrder(rowIndex, completedOrdersModel);
        } else if (choice == 2) {
            moveOrder(rowIndex, canceledOrdersModel);
        }
    }

// 3. Di chuyển đơn từ current → dest (với xác nhận)
    private void moveOrder(int rowIndex, DefaultTableModel destModel) {
        // Xác nhận hành động
        if (JOptionPane.showConfirmDialog(
                this,
                "Bạn có chắc chắn?",
                "Xác nhận",
                JOptionPane.YES_NO_OPTION
        ) != JOptionPane.YES_OPTION) {
            return;
        }

        // Lấy dữ liệu dòng từ currentOrdersModel
        Object[] row = new Object[currentOrdersModel.getColumnCount()];
        for (int j = 0; j < row.length; j++) {
            row[j] = currentOrdersModel.getValueAt(rowIndex, j);
        }

        currentOrdersModel.removeRow(rowIndex);
        removeFromBookingTable(row);
    }

// 4. Popup menu chuột phải để Hoàn tất / Hủy trên JTable đơn
    private void performMoveSelected(DefaultTableModel src, DefaultTableModel dest) {
        int row = tblBooking.getSelectedRow();
        if (row < 0) {
            return;
        }
        if (JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn?",
                "Xác nhận", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        // Lấy giá trị từng cột
        String name = (String) src.getValueAt(row, 0);
        String email = (String) src.getValueAt(row, 1);
        String phone = (String) src.getValueAt(row, 2);      
        LocalDate date = LocalDate.parse(src.getValueAt(row, 3).toString(), dateFmt);
        LocalTime time = LocalTime.parse(src.getValueAt(row, 4).toString(), timeFmt);
        int tableNo = Integer.parseInt(src.getValueAt(row, 5).toString());
        try {
            if (dest == canceledOrdersModel) {
                reservationDAO.cancelByInfo(name, email, phone, date, time, tableNo);
            } else {
                reservationDAO.completeByInfo(name, email, phone, date, time, tableNo);
            }
            // Làm mới cả hai UI
            refreshBookingTable();
            refreshOrderTables();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi chuyển đơn: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
// 5. Tạo panel Quản lý đơn (Orders)

    private JPanel createOrderManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Quản lý các đơn đặt bàn"));

        // --- Phần hiển thị 3 bảng ---
        tblCurrentOrders = new JTable(currentOrdersModel);
        JTable tDone = new JTable(completedOrdersModel);
        JTable tCancel = new JTable(canceledOrdersModel);
        JPanel north = new JPanel(new GridLayout(1, 3, 5, 5));
        north.add(wrapTableWithTitle(tblCurrentOrders, "Đơn hiện tại"));
        north.add(wrapTableWithTitle(tDone, "Đã hoàn tất"));
        north.add(wrapTableWithTitle(tCancel, "Đã hủy"));
        panel.add(north, BorderLayout.CENTER);

        // --- Phần nút ở dưới cùng ---
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton btnSearchOrders = new JButton("Tìm kiếm");
        JButton btnBack = new JButton("Quay lại");

        // Nút Quay lại: trở về màn Booking
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, "Booking"));

        // Nút Tìm kiếm: gọi dialog tìm đơn (như bạn đã code openOrderSearchDialog)
        btnSearchOrders.addActionListener(e -> openOrderSearchDialog());

        btnPanel.add(btnSearchOrders);
        btnPanel.add(btnBack);

        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JScrollPane wrapTableWithTitle(JTable table, String title) {
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder(title));
        return sp;
    }

    private void addTableDialog() {
        String[] soCho = new String[9];
        for (int i = 0; i < 9; i++) {
            soCho[i] = String.valueOf(i + 2);
        }
        String[] states = {"Còn trống", "Đang sử dụng", "Đã được đặt", "Không khả dụng"};
        JPanel p = new JPanel(new GridLayout(4, 2, 5, 5));
        JTextField tfNo = new JTextField();
        JComboBox<String> cbType = new JComboBox<>(new String[]{"Thường", "VIP"});
        JComboBox<String> cbSeats = new JComboBox<>(soCho);
        JComboBox<String> cbStat = new JComboBox<>(states);
        p.add(new JLabel("Số bàn:"));
        p.add(tfNo);
        p.add(new JLabel("Loại bàn:"));
        p.add(cbType);
        p.add(new JLabel("Số chỗ:"));
        p.add(cbSeats);
        p.add(new JLabel("Trạng thái:"));
        p.add(cbStat);
        int r = JOptionPane.showConfirmDialog(this, p, "Thêm bàn mới", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String no = tfNo.getText().trim();
            if (no.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Số bàn không được để trống!");
                return;
            }
            for (int i = 0; i < tableListModel.getRowCount(); i++) {
                if (tableListModel.getValueAt(i, 0).equals(no)) {
                    JOptionPane.showMessageDialog(this, "Số bàn đã tồn tại!");
                    return;
                }
            }
            Table t = new Table(
                    no,
                    cbType.getSelectedItem().toString(),
                    Integer.parseInt(cbSeats.getSelectedItem().toString()),
                    cbStat.getSelectedItem().toString()
            );
            try {
                tableDAO.add(t);
                loadTableData();
                JOptionPane.showMessageDialog(this, "Thêm bàn thành công!");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Thêm bàn thất bại:\n" + e.getMessage());
            }
        }
    }

    private void deleteTableDialog() {
        if (tableListModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Không còn bàn nào để xóa!");
            return;
        }
        // Tạo danh sách ID bàn hiện có
        DefaultListModel<String> lm = new DefaultListModel<>();
        for (int i = 0; i < tableListModel.getRowCount(); i++) {
            lm.addElement(tableListModel.getValueAt(i, 0).toString());
        }
        JList<String> list = new JList<>(lm);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        int r = JOptionPane.showConfirmDialog(
                this,
                new JScrollPane(list),
                "Chọn bàn cần xóa",
                JOptionPane.OK_CANCEL_OPTION
        );
        if (r == JOptionPane.OK_OPTION && list.getSelectedValue() != null) {
            String sel = list.getSelectedValue();
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Xóa bàn " + sel + "?",
                    "Xác nhận",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    tableDAO.delete(sel);      // gọi DAO để xóa trong DB
                    loadTableData();           // reload lại model từ DB
                    JOptionPane.showMessageDialog(this, "Xóa bàn thành công!");
                } catch (SQLException e) {
                    JOptionPane.showMessageDialog(this, "Xóa bàn thất bại:\n" + e.getMessage());
                }
            }
        }
    }

    private void openOrderSearchDialog() {
        // các trường nhập
        JFormattedTextField dateField;
        try {
            MaskFormatter dm = new MaskFormatter("##/##/####");
            dm.setPlaceholderCharacter(' ');
            dateField = new JFormattedTextField(dm);
        } catch (ParseException ex) {
            dateField = new JFormattedTextField();
        }
        JTextField fName = new JTextField();
        JTextField fPhone = new JTextField();
        JPanel p = new JPanel(new GridLayout(3, 2, 5, 5));
        p.add(new JLabel("Ngày (dd/MM/yyyy):"));
        p.add(dateField);
        p.add(new JLabel("Họ tên :"));
        p.add(fName);
        p.add(new JLabel("SĐT :"));
        p.add(fPhone);
        int r = JOptionPane.showConfirmDialog(this, p, "Tìm đơn", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String date = dateField.getText().trim();
        String name = fName.getText().trim();
        String phone = fPhone.getText().trim();
        if (!validateDate(date)) {
            JOptionPane.showMessageDialog(this, "Ngày sai định dạng!");
            return;
        }
        // gom tất cả model vào 1 list
        List<DefaultTableModel> models = List.of(currentOrdersModel,
                completedOrdersModel,
                canceledOrdersModel);
        List<Object[]> results = new ArrayList<>();
        // tìm tất cả đơn đúng “Ngày” (và nếu != rỗng, khớp name & phone)
        for (DefaultTableModel m : models) {
            for (int i = 0; i < m.getRowCount(); i++) {
                String d = m.getValueAt(i, 3).toString();
                String nm = m.getValueAt(i, 0).toString();
                String ph = m.getValueAt(i, 2).toString();
                if (d.equals(date)
                        && (name.isEmpty() || nm.equalsIgnoreCase(name))
                        && (phone.isEmpty() || ph.equals(phone))) {
                    // lưu thêm time để sort
                    Object[] row = new Object[m.getColumnCount()];
                    for (int c = 0; c < row.length; c++) {
                        row[c] = m.getValueAt(i, c);
                    }
                    results.add(row);
                }
            }
        }
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy đơn nào trong ngày này.");
            return;
        }
        // sort theo giờ (cột 4)
        results.sort((a, b) -> a[4].toString().compareTo(b[4].toString()));
        // tạo JList các khung giờ
        DefaultListModel<String> lm = new DefaultListModel<>();
        for (Object[] row : results) {
            lm.addElement(row[4] + " – " + row[0] + " – bàn " + row[5]);
        }
        JList<String> list = new JList<>(lm);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scroll = new JScrollPane(list);
        scroll.setPreferredSize(new Dimension(300, 200));
        int choice = JOptionPane.showOptionDialog(
                this,
                scroll,
                "Kết quả ngày " + date,
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Quay lại"},
                "Quay lại"
        );
        // khi chọn 1 mục (double-click) có thể show chi tiết, hoặc bạn chỉ cần xem list là đủ.
    }

    private int findRowInModel(DefaultTableModel model, String name, String phone) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(name)
                    && model.getValueAt(i, 2).equals(phone)) {
                return i;
            }
        }
        return -1;
    }

    private void removeFromBookingTable(Object[] rowData) {
        for (int i = 0; i < bookingTableModel.getRowCount(); i++) {
            boolean match = true;
            for (int c = 0; c < rowData.length; c++) {
                if (!bookingTableModel.getValueAt(i, c).equals(rowData[c])) {
                    match = false;
                    break;
                }
            }
            if (match) {
                bookingTableModel.removeRow(i);
                return;
            }
        }
    }

    private void sortTable(DefaultTableModel model) {
        List<Object[]> rows = new ArrayList<>();
        int cols = model.getColumnCount();
        for (int i = 0; i < model.getRowCount(); i++) {
            Object[] row = new Object[cols];
            for (int j = 0; j < cols; j++) {
                row[j] = model.getValueAt(i, j);
            }
            rows.add(row);
        }
        rows.sort((a, b) -> {
            try {
                return Integer.compare(Integer.parseInt(a[0].toString()),
                        Integer.parseInt(b[0].toString()));
            } catch (Exception e) {
                return a[0].toString().compareTo(b[0].toString());
            }
        });
        model.setRowCount(0);
        for (Object[] r : rows) {
            model.addRow(r);
        }
    }

    private boolean validateDate(String date) {
        if (!date.matches("\\d{2}/\\d{2}/\\d{4}")) {
            return false;
        }
        String[] p = date.split("/");
        int d = Integer.parseInt(p[0]), m = Integer.parseInt(p[1]), y = Integer.parseInt(p[2]);
        if (m < 1 || m > 12 || d < 1 || d > 31) {
            return false;
        }
        if (m == 2) {
            boolean leap = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
            return d <= (leap ? 29 : 28);
        }
        return !((m == 4 || m == 6 || m == 9 || m == 11) && d > 30);
    }

    private boolean validateTime(String time) {
        if (!time.matches("\\d{2}:\\d{2}")) {
            return false;
        }
        String[] p = time.split(":");
        int h = Integer.parseInt(p[0]), mi = Integer.parseInt(p[1]);
        return h >= 0 && h < 24 && mi >= 0 && mi < 60;
    }

    private void loadTableData() {
        try {
            tableListModel.setRowCount(0);        // xóa hết dòng cũ
            for (Table t : tableDAO.getAll()) {
                tableListModel.addRow(new Object[]{
                    t.getTableID(),
                    t.getTableType(),
                    t.getSeatCount(),
                    t.getStatus()
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi load danh sách bàn.");
        }
    }

    private void logout() {
        // đóng cửa sổ hiện tại
        this.dispose();
        // mở lại form login
        SwingUtilities.invokeLater(() -> {
            RestaurantBookingLoginUI loginUI = new RestaurantBookingLoginUI();
            loginUI.setVisible(true);
        });
    }

    private void refreshBookingTable() {
        try {
            // 1) Lấy danh sách Reservations từ DAO
            List<Reservation> list = reservationDAO.getAllReservations();

            // 2) Xóa dữ liệu cũ
            bookingTableModel.setRowCount(0);
            currentOrdersModel.setRowCount(0);

            // 3) Đổ lại
            for (Reservation r : list) {
                Object[] row = new Object[]{
                    r.getCustomerName(), // cột 0: Họ và tên
                    r.getEmail(), // cột 1: Email
                    r.getPhoneNumber(), // cột 2: SĐT
                    r.getReservationDate().format(dateFmt),// cột 4: Ngày
                    r.getReservationTime().format(timeFmt),// cột 5: Giờ

                    r.getGuestCount(), // cột 3: Số bàn
                    r.getMessage() // cột 6: Tin nhắn
                };
                bookingTableModel.addRow(row);
                currentOrdersModel.addRow(row);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi load đặt bàn: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void refreshOrderTables() {
        try {
            // Xóa sạch dữ liệu cũ
            currentOrdersModel.setRowCount(0);
            canceledOrdersModel.setRowCount(0);
            completedOrdersModel.setRowCount(0);

            // 1) Đơn hiện tại
            for (Reservation r : reservationDAO.getAllReservations()) {
                currentOrdersModel.addRow(new Object[]{
                    r.getCustomerName(),
                    r.getEmail(),
                    r.getPhoneNumber(),
                    r.getReservationDate().format(dateFmt),
                    r.getReservationTime().format(timeFmt),
                    r.getGuestCount(),
                    r.getMessage()
                });
            }

            // 2) Đơn đã hủy
            for (Reservation r : reservationDAO.getAllCancelled()) {
                canceledOrdersModel.addRow(new Object[]{
                    r.getCustomerName(),
                    r.getEmail(),
                    r.getPhoneNumber(),
                    r.getReservationDate().format(dateFmt),
                    r.getReservationTime().format(timeFmt),
                    r.getGuestCount(),
                    r.getMessage()
                });
            }

            // 3) Đơn đã hoàn tất
            for (Reservation r : reservationDAO.getAllCompleted()) {
                completedOrdersModel.addRow(new Object[]{
                    r.getCustomerName(),
                    r.getEmail(),
                    r.getPhoneNumber(),
                    r.getReservationDate().format(dateFmt),
                    r.getReservationTime().format(timeFmt),
                    r.getGuestCount(),
                    r.getMessage()
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi load Orders: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadAndApplyEmployeeInfo() {
        try {
            currentEmployee = EmployeeDAO.findById(employeeId);
            if (currentEmployee != null) {
                // Cập nhật avatar trên Staff panel
                avatar.setImage(currentEmployee.getPhoto());
                lblName.setText("Họ và tên:   " + currentEmployee.getName());
                lblAddr.setText("Địa chỉ:     " + currentEmployee.getAddress());
                lblAge.setText("Tuổi:        " + currentEmployee.getAge());
                lblGender.setText("Giới tính:   " + currentEmployee.getGender());
                lblTitle.setText("Chức vụ:     " + currentEmployee.getTitle());
                // Cập nhật avatar trên Home panel
                if (homeAvatar != null) {
                    homeAvatar.setImage(currentEmployee.getPhoto());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public class ImagePanel extends JPanel {

        private Image bg;

        public ImagePanel() {
            this.bg = null;
        }

        public ImagePanel(Image img) {
            this.bg = img;
        }

        public ImagePanel(String path) {
            this();
            Image img = null;
            File f = new File(path);
            if (f.exists()) {
                img = new ImageIcon(path).getImage();
            } else {
                URL url = getClass().getResource(path);
                if (url != null) {
                    img = new ImageIcon(url).getImage();
                }
            }
            this.bg = img;
        }

        public void setImage(Image img) {
            this.bg = img;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (bg != null) {
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CombinedRestaurantUI().setVisible(true));
    }
}
// Tổng tất cả đồ án hơn 3000 dòng