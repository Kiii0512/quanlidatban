package admin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import admin.DatabaseConnector;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.file.Files;
import java.io.IOException;
import javax.swing.TransferHandler;
import javax.swing.JFileChooser;
import javax.swing.SwingConstants;
import java.sql.Statement;
import java.util.function.BiConsumer;
import dangnhap2.RestaurantBookingLoginUI;
import java.util.Map;
import java.util.HashMap;

public class AdminUI extends JFrame {

    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private DefaultTableModel employeeTableModel;
    private JTable employeeTable;
    private final List<Employee> employees = new ArrayList<>();
    private int employeeIdCounter = 25;
    // Account management
    private DefaultTableModel activeAccountModel;
    private DefaultTableModel inactiveAccountModel;
    private final Map<String,String> plainPwdMap = new HashMap<>();

    public AdminUI() {
        UIManager.put("Panel.background", new Color(0xF2F4F5));   // nền chính
        UIManager.put("SplitPane.background", new Color(0xF2F4F5));
        UIManager.put("ScrollPane.background", new Color(0xFFFFFF));
        UIManager.put("Table.background", new Color(0xFFFFFF));
        UIManager.put("Table.gridColor", new Color(0xCFD8DC));
        UIManager.put("Table.foreground", new Color(0x212121));
        UIManager.put("TableHeader.background", new Color(0x263238));
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(0x00796B));   // nút chính
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.select", new Color(0x004D40));   // hover/push
        UIManager.put("Label.foreground", new Color(0x212121));
        setTitle("ADMIN");
        setSize(900, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.add(createHomePanel(), "Home");
        mainPanel.add(createEmployeeInfoPanel(), "ViewEmployees");
        mainPanel.add(createCreateAccountPanel(), "CreateAccount");
        add(mainPanel);
        setVisible(true);
    }

    private void loadAccounts(String status) {
        DefaultTableModel model = status.equals("Active") ? activeAccountModel : inactiveAccountModel;
        model.setRowCount(0);
        String call = "{call dbo.sp_GetAccountsByStatus(?)}";
        try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, status);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("FullName");
                    String id = rs.getString("EmployeeID");
                    String saltHex = rs.getString("Salt");
                    String plainPwd = rs.getString("PlainPassword");
                    model.addRow(new Object[]{name, id, saltHex, plainPwd});
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi load tài khoản (" + status + "):\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadEmployeesFromDb() {
        employees.clear();
        employeeTableModel.setRowCount(0);

        // 1) Lấy maxId từ DB
        int maxId = 0;
        String sqlMax = "SELECT MAX(CAST(EmployeeID AS INT)) AS MaxID FROM Employees";
        try (Connection conn = DatabaseConnector.getConnection(); Statement stmt = conn.createStatement(); ResultSet rsMax = stmt.executeQuery(sqlMax)) {
            if (rsMax.next()) {
                maxId = rsMax.getInt("MaxID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            // Nếu lỗi thì để maxId = 0
        }
        // Khởi tạo counter dựa trên maxId
        employeeIdCounter = maxId + 1;

        // 2) Load danh sách nhân viên
        String sql = "SELECT EmployeeID, FullName, Gender, Position, Address, Age, Salary,"+ "WorkStatus AS Status, Photo FROM Employees";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("EmployeeID");
                String name = rs.getString("FullName");
                String gender = rs.getString("Gender");
                String position = rs.getString("Position");
                String address = rs.getString("Address");
                int age = rs.getInt("Age");
                double salary = rs.getDouble("Salary");
                String status = rs.getString("Status");
                byte[] photo = rs.getBytes("Photo");

                Employee emp = new Employee(name, id, address, age, gender, position, salary, status, photo);
                employees.add(emp);
                employeeTableModel.addRow(new Object[]{name, id, gender, position, status});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Lỗi khi load danh sách nhân viên:\n" + ex.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void setAccountStatus(String empId, String newStatus) {
        String call = "{call dbo.sp_SetAccountStatus(?, ?)}";
        try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, empId);
            cs.setString(2, newStatus);
            cs.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái tài khoản:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
// ----- VIEW EMPLOYEES -----

    private JPanel createEmployeeInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Search bar
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Tìm kiếm: "));
        JTextField searchField = new JTextField(20);
        north.add(searchField);
        panel.add(north, BorderLayout.NORTH);

        // Table
        String[] cols = {"Tên nhân viên", "Mã số", "Giới tính", "Chức vụ","Tình trạng"};
        employeeTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        employeeTable = new JTable(employeeTableModel);
        loadEmployeesFromDb();
        panel.add(new JScrollPane(employeeTable), BorderLayout.CENTER);

        // Popup menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem view = new JMenuItem("Xem chi tiết");
        JMenuItem edit = new JMenuItem("Chỉnh sửa");
        JMenuItem delete = new JMenuItem("Xoá");

        popup.add(view);
        popup.add(edit);
        popup.add(delete);
        JMenuItem addPhoto = new JMenuItem("Thêm ảnh");
        popup.add(addPhoto);
        employeeTable.setComponentPopupMenu(popup);
        employeeTable.addMouseListener(new TableMouseListener(employeeTable));

        addPhoto.addActionListener(e -> openPhotoDialogForSelected());
        view.addActionListener(e -> showEmployeeDetail(false));
        edit.addActionListener(e -> showEmployeeDetail(true));
        delete.addActionListener(e -> deleteSelectedEmployee());

        // Bottom buttons
        JPanel south = new JPanel();
        JButton addBtn = new JButton("Thêm nhân viên");
        JButton backBtn = new JButton("Quay lại");
        south.add(addBtn);
        south.add(backBtn);
        panel.add(south, BorderLayout.SOUTH);

        addBtn.addActionListener(e -> addNewEmployee());
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Home"));

        // Filter as typing
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterEmployeeTable(searchField.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterEmployeeTable(searchField.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterEmployeeTable(searchField.getText());
            }
        });

        return panel;
    }

    private JPanel createHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel welcomeLabel = new JLabel("Chào mừng quản lý", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 28));
        panel.add(welcomeLabel, BorderLayout.CENTER);
        // 2) Nút Đăng xuất ở góc trên phải
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLogout = new JButton("Đăng xuất");
        btnLogout.setMargin(new Insets(2, 8, 2, 8)); // cho nhỏ gọn
        topRight.add(btnLogout);
        panel.add(topRight, BorderLayout.NORTH);

        // Xử lý khi bấm Đăng xuất
        btnLogout.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(
                    this,
                    "Bạn có chắc chắn muốn đăng xuất không?",
                    "Xác nhận đăng xuất",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );
            if (c == JOptionPane.YES_OPTION) {
                // 1) Đóng cửa sổ AdminUI
                SwingUtilities.getWindowAncestor(panel).dispose();
                // 2) Mở lại màn hình đăng nhập
                new RestaurantBookingLoginUI().setVisible(true);
            }
        });
        JPanel buttonPanel = new JPanel();
        JButton viewButton = new JButton("Xem thông tin nhân viên");
        JButton createButton = new JButton("Quản lý tài khoản nhân viên");
        buttonPanel.add(viewButton);
        buttonPanel.add(createButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        viewButton.addActionListener(e -> cardLayout.show(mainPanel, "ViewEmployees"));
        createButton.addActionListener(e -> {
            loadAccounts("Active");
            loadAccounts("Inactive");
            cardLayout.show(mainPanel, "CreateAccount");
        });
        return panel;
    }

    private JPanel createCreateAccountPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Nhập mã số nhân viên: "));
        JTextField idField = new JTextField(10);
        top.add(idField);
        JButton createBtn = new JButton("Tạo tài khoản");
        JButton backBtn = new JButton("Quay lại");
        top.add(createBtn);
        top.add(backBtn);
        panel.add(top, BorderLayout.NORTH);
        activeAccountModel = new DefaultTableModel(
                new String[]{"Tên nhân viên", "Mã số", "Salt", "Mật khẩu"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        inactiveAccountModel = new DefaultTableModel(
                new String[]{"Tên nhân viên", "Mã số", "Salt", "Mật khẩu"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable activeTable = new JTable(activeAccountModel);
        JTable inactiveTable = new JTable(inactiveAccountModel);
        JPopupMenu actPopup = new JPopupMenu();
        JMenuItem deactivate = new JMenuItem("Ngừng hoạt động");
        actPopup.add(deactivate);
        activeTable.setComponentPopupMenu(actPopup);
        activeTable.addMouseListener(new TableMouseListener(activeTable));
        deactivate.addActionListener(e -> deactivateAccount(activeTable));
        JPopupMenu inactPopup = new JPopupMenu();
        JMenuItem reactivate = new JMenuItem("Kích hoạt lại");
        JMenuItem delAccount = new JMenuItem("Xoá tài khoản");
        inactPopup.add(reactivate);
        inactPopup.add(delAccount);
        inactiveTable.setComponentPopupMenu(inactPopup);
        inactiveTable.addMouseListener(new TableMouseListener(inactiveTable));
        reactivate.addActionListener(e -> reactivateAccount(inactiveTable));
        delAccount.addActionListener(e -> deleteAccount(inactiveTable));
        JScrollPane activeScroll = new JScrollPane(activeTable);
        activeScroll.setBorder(BorderFactory.createTitledBorder("Tài khoản đang hoạt động"));

        JScrollPane inactiveScroll = new JScrollPane(inactiveTable);
        inactiveScroll.setBorder(BorderFactory.createTitledBorder("Tài khoản ngừng hoạt động"));
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,activeScroll,inactiveScroll);
        split.setDividerLocation(450);
        panel.add(split, BorderLayout.CENTER);
        createBtn.addActionListener(e -> {
            String empId = idField.getText().trim();
            if (empId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập mã số nhân viên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String call = "{call dbo.sp_CreateEmployeeAccount(?, ?, ?, ?)}";
            try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(call)) {

                cs.setString(1, empId);
                cs.registerOutParameter(2, java.sql.Types.INTEGER);
                cs.registerOutParameter(3, java.sql.Types.NVARCHAR);
                cs.registerOutParameter(4, java.sql.Types.NVARCHAR);  // plaintext pwd

                cs.execute();

                int res = cs.getInt(2);
                String msg = cs.getString(3);
                String plain = cs.getString(4);  // ← mật khẩu plaintext

                JOptionPane.showMessageDialog(
                        this,
                        msg,
                        res == 1 ? "Thành công" : "Lỗi",
                        res == 1 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);

                if (res == 1) {
                 
                    plainPwdMap.put(empId, plain);
                    loadAccounts("Active");
                    loadAccounts("Inactive");
                    overridePlainInModel(activeAccountModel, empId);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi khi tạo tài khoản:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        backBtn.addActionListener(e -> cardLayout.show(mainPanel, "Home"));
        return panel;
    }

    private void filterEmployeeTable(String keyword) {
        keyword = keyword.toLowerCase();
        employeeTableModel.setRowCount(0);
        for (Employee emp : employees) {
            if (emp.getName().toLowerCase().contains(keyword)
                    || emp.getId().contains(keyword)) {
                employeeTableModel.addRow(new Object[]{
                    emp.getName(), emp.getId(),
                    emp.getGender(), emp.getPosition()
                });
            }
        }
    }
    private void overridePlainInModel(DefaultTableModel model, String empId) {
        String plain = plainPwdMap.get(empId);
        if (plain == null) {
            return;      
        }
        for (int i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 1).equals(empId)) {
                // cột 0=Tên, 1=Mã số, 2=Salt, 3=Mật khẩu
                model.setValueAt(plain, i, 3);
                break;
            }
        }
    }
    private void openPhotoDialogForSelected() {
        // 1) Lấy Employee đang được chọn
        int sel = employeeTable.getSelectedRow();
        if (sel < 0) {
            return;
        }
        Employee emp = employees.get(employeeTable.convertRowIndexToModel(sel));

        // 2) Tạo JLabel preview và kích thước
        JLabel photoLabel = new JLabel("Kéo thả ảnh hoặc nhấn nút bên dưới", SwingConstants.CENTER);
        photoLabel.setPreferredSize(new Dimension(200, 200));
        photoLabel.setBorder(BorderFactory.createDashedBorder(Color.GRAY));
        byte[][] photoDataHolder = new byte[1][]; // để lưu dữ liệu tạm
        // 3) Thiết lập drag-and-drop
        photoLabel.setTransferHandler(new TransferHandler() {
            @Override
            public boolean importData(TransferSupport sup) {
                try {
                    @SuppressWarnings("unchecked")
                    List<File> files = (List<File>) sup.getTransferable()
                            .getTransferData(DataFlavor.javaFileListFlavor);
                    File imgFile = files.get(0);
                    byte[] data = Files.readAllBytes(imgFile.toPath());
                    savePhotoToDb(emp.getId(), data);
                    emp.setPhoto(data);

                    // ==== Chèn ở đây ====
                    ImageIcon ico = new ImageIcon(data);
                    // scale ảnh cho vừa với label
                    Image img = ico.getImage()
                            .getScaledInstance(photoLabel.getWidth(),
                                    photoLabel.getHeight(),
                                    Image.SCALE_SMOOTH);
                    photoLabel.setIcon(new ImageIcon(img));
                    photoLabel.setText(null);           // xoá dòng "Kéo thả ảnh..."
                    photoLabel.setBorder(null);         // (tuỳ chọn) bỏ viền dashed
                    // ===================

                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        });

        // 4) Nút chọn file thủ công
        JButton chooseBtn = new JButton("Chọn ảnh");
        chooseBtn.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File img = fc.getSelectedFile();
                try {
                    byte[] data = Files.readAllBytes(img.toPath());
                    savePhotoToDb(emp.getId(), data);
                    emp.setPhoto(data);
                    ImageIcon ico = new ImageIcon(data);
                    photoLabel.setIcon(new ImageIcon(
                            ico.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        });

        // 5) Hiển thị dialog
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.add(photoLabel, BorderLayout.CENTER);
        panel.add(chooseBtn, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this, panel,
                "Cập nhật ảnh nhân viên", JOptionPane.PLAIN_MESSAGE);
    }

    private void addNewEmployee() {
        // 1) Hiển thị form nhập liệu
        JTextField nameField = new JTextField(15);
        JTextField addressField = new JTextField(15);
        JTextField ageField = new JTextField(5);
        JComboBox<String> genderBox = new JComboBox<>(new String[]{"Nam", "Nữ"});
        JTextField salaryField = new JTextField(10);
        JLabel vndLabel = new JLabel("VND");

        // JLabel preview cho ảnh
        JLabel photoPreview = new JLabel("Chọn ảnh", SwingConstants.CENTER);
        photoPreview.setPreferredSize(new Dimension(100, 100));
        photoPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        // mảng tạm để lưu byte[]
        byte[][] photoDataHolder = new byte[1][];

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridy = 0;

        panel.add(new JLabel("Họ tên:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Địa chỉ:"), gbc);
        gbc.gridx = 1;
        panel.add(addressField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Tuổi:"), gbc);
        gbc.gridx = 1;
        panel.add(ageField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Giới tính:"), gbc);
        gbc.gridx = 1;
        panel.add(genderBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Lương:"), gbc);
        gbc.gridx = 1;
        panel.add(salaryField, gbc);
        gbc.gridx = 2;
        panel.add(vndLabel, gbc);

        // thêm preview và nút chọn ảnh
        gbc.gridx = 0;
        gbc.gridy++;
        panel.add(new JLabel("Ảnh:"), gbc);
        gbc.gridx = 1;
        panel.add(photoPreview, gbc);
        JButton pickBtn = new JButton("Chọn ảnh");
        gbc.gridx = 2;
        panel.add(pickBtn, gbc);

        // listener cho nút chọn ảnh
        pickBtn.addActionListener(ev -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File img = fc.getSelectedFile();
                try {
                    byte[] data = Files.readAllBytes(img.toPath());
                    photoDataHolder[0] = data;
                    ImageIcon ico = new ImageIcon(data);
                    photoPreview.setIcon(new ImageIcon(
                            ico.getImage().getScaledInstance(100, 100, Image.SCALE_SMOOTH)
                    ));
                    photoPreview.setText("");
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                    JOptionPane.showMessageDialog(this,
                            "Không thể đọc file ảnh:\n" + ioe.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Thêm nhân viên mới",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );
        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        // 2) Lấy dữ liệu và validate
        String name = nameField.getText().trim();
        String address = addressField.getText().trim();
        int age;
        try {
            age = Integer.parseInt(ageField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tuổi phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String gender = (String) genderBox.getSelectedItem();
        double salary;
        try {
            salary = Double.parseDouble(salaryField.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 3) Sinh ID
        String empId = String.format("%06d", employeeIdCounter++);

        // 4) Chèn xuống database (INSERT có Photo)
        String sql = "INSERT INTO Employees("
                + "EmployeeID, FullName, Address, Age, Gender, Position, Salary, WorkStatus, Photo"
                + ") VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            ps.setString(2, name);
            ps.setString(3, address);
            ps.setInt(4, age);
            ps.setString(5, gender);
            ps.setString(6, "Nhân viên");
            ps.setDouble(7, salary);
            ps.setString(8, "Đang làm việc");
            if (photoDataHolder[0] != null) {
                ps.setBytes(9, photoDataHolder[0]);
            } else {
                ps.setNull(9, java.sql.Types.VARBINARY);
            }
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi chèn vào database:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 5) Reload UI và thông báo
        loadEmployeesFromDb();
        JOptionPane.showMessageDialog(this,
                "Thêm nhân viên thành công! Mã số: " + empId,
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showEmployeeDetail(boolean editable) {
        int sel = employeeTable.getSelectedRow();
        if (sel < 0) {
            return;
        }
        int row = employeeTable.convertRowIndexToModel(sel);
        Employee emp = employees.get(row);

        // 1) Tải ảnh từ DB
        try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall("{call dbo.sp_GetEmployeeDetail(?)}")) {
            cs.setString(1, emp.getId());
            try (ResultSet rs = cs.executeQuery()) {
                if (rs.next()) {
                    emp.setPhoto(rs.getBytes("Photo"));
                    emp.setStatus(rs.getString("Status"));    // <-- LẤY STATUS MỚI
                    emp.setAddress(rs.getString("Address"));  // tuỳ bạn có muốn reload thêm
                    emp.setAge(rs.getInt("Age"));
                    emp.setPosition(rs.getString("Position"));
                    emp.setSalary(rs.getDouble("Salary"));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        // 2) Tạo panel chứa form
        JPanel detailPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
       gbc.fill     = GridBagConstraints.NONE;
    gbc.weightx  = 0;
    gbc.gridx    = 0;
    gbc.gridy    = 0;

    BiConsumer<String, JComponent> addRow = (label, comp) -> {
        // Nhãn
        gbc.gridx   = 0;
        detailPanel.add(new JLabel(label), gbc);

        // Field (giãn ngang)
        gbc.gridx   = 1;
        gbc.fill    = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        detailPanel.add(comp, gbc);

        // Chuyển dòng, reset fill/weight
        gbc.gridy++;
        gbc.fill    = GridBagConstraints.NONE;
        gbc.weightx = 0;
    };

    // Tạo các field
    JTextField nameField     = new JTextField(emp.getName(),    15);
    JTextField idField       = new JTextField(emp.getId(),      15);
    JTextField addressField  = new JTextField(emp.getAddress(), 15);
    JTextField ageField      = new JTextField(String.valueOf(emp.getAge()), 5);
    JTextField genderField   = new JTextField(emp.getGender(),   5);
    JTextField positionField = new JTextField(emp.getPosition(), 15);
    JTextField salaryField   = new JTextField(String.valueOf(emp.getSalary()), 10);
    JComboBox<String> statusBox = new JComboBox<>(new String[]{
        "Đang làm việc", "Đã nghỉ việc", "Đang nghỉ phép"
    });
        statusBox.setSelectedItem(emp.getStatus());
        statusBox.setEnabled(editable);
    // set editable
    nameField.setEditable(editable);
    addressField.setEditable(editable);
    ageField.setEditable(editable);
    positionField.setEditable(editable);
    salaryField.setEditable(editable);
    genderField.setEditable(false);
    idField.setEditable(false);
    statusBox.setEnabled(editable);

    // Thêm từng dòng
    addRow.accept("Họ tên:",    nameField);
    addRow.accept("Mã số:",     idField);
    addRow.accept("Địa chỉ:",   addressField);
    addRow.accept("Tuổi:",      ageField);
    addRow.accept("Giới tính:", genderField);
    addRow.accept("Chức vụ:",   positionField);
    addRow.accept("Lương:",     salaryField);
    addRow.accept("Tình trạng:",statusBox);

    JButton saveBtn = null;
    if (editable) {
        saveBtn = new JButton("Lưu");
        gbc.gridx    = 0;
        gbc.gridwidth = 2;
        detailPanel.add(saveBtn, gbc);
    }

    // === 2) Tạo panel ảnh ===
    JLabel imgLabel = new JLabel();
    imgLabel.setPreferredSize(new Dimension(200, 200));
    imgLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    if (emp.getPhoto() != null) {
        ImageIcon ico = new ImageIcon(emp.getPhoto());
        imgLabel.setIcon(new ImageIcon(
            ico.getImage().getScaledInstance(200, 200, Image.SCALE_SMOOTH)
        ));
    } else {
        imgLabel.setText("<html><i>Ảnh nhân viên<br/>chưa có</i></html>");
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
    }

    // === 3) JSplitPane với divider cố định và tay cầm rõ ===
    JSplitPane split = new JSplitPane(
        JSplitPane.HORIZONTAL_SPLIT, imgLabel, detailPanel
    );
    split.setDividerLocation(220);  // giữ phần ảnh rộng ~220px
    split.setResizeWeight(0);       // phần detail giãn, ảnh cố định
    split.setDividerSize(4);        // tay cầm rõ

    // === 4) Hiển thị dialog ===
    JDialog dialog = new JDialog(this,
        editable ? "Chỉnh sửa nhân viên" : "Chi tiết nhân viên", true);
    dialog.getContentPane().add(split);
    dialog.pack();
    dialog.setLocationRelativeTo(this);
        // 6) Gắn listener cho nút Lưu
        if (editable && saveBtn != null) {
            saveBtn.addActionListener(ev -> {
                // Validate và cập nhật emp từ các field
                String newName = nameField.getText().trim();
                String newAddress = addressField.getText().trim();
                int newAge;
                try {
                    newAge = Integer.parseInt(ageField.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(dialog, "Tuổi phải là số nguyên.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newPosition = positionField.getText().trim();
                double newSalary;
                try {
                    newSalary = Double.parseDouble(salaryField.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(dialog, "Lương phải là số.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                String newStatus = (String) statusBox.getSelectedItem();

                // Cập nhật object
                emp.setName(newName);
                emp.setAddress(newAddress);
                emp.setAge(newAge);
                emp.setPosition(newPosition);
                emp.setSalary(newSalary);
                emp.setStatus(newStatus);

                // Ghi lên DB
                String sqlUpdate
                        = "UPDATE Employees SET FullName=?, Address=?, Age=?, Position=?, Salary=?, WorkStatus=? WHERE EmployeeID=?";
                try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                    ps.setString(1, newName);
                    ps.setString(2, newAddress);
                    ps.setInt(3, newAge);
                    ps.setString(4, newPosition);
                    ps.setDouble(5, newSalary);
                    ps.setString(6, newStatus);
                    ps.setString(7, emp.getId());
                    int updated = ps.executeUpdate();
                    if (updated == 0) {
                        JOptionPane.showMessageDialog(dialog,
                                "Không tìm thấy nhân viên để cập nhật.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if ("Đã nghỉ việc".equals(newStatus)) {
                        setAccountStatus(emp.getId(), "Inactive");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(dialog,
                            "Lỗi khi cập nhật database:\n" + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Cập nhật table model
                employeeTableModel.setValueAt(emp.getName(), row, 0);
                employeeTableModel.setValueAt(emp.getId(), row, 1);
                employeeTableModel.setValueAt(emp.getGender(), row, 2);
                employeeTableModel.setValueAt(emp.getPosition(), row, 3);
                employeeTableModel.setValueAt(emp.getStatus(), row, 4);
                loadEmployeesFromDb();
                dialog.dispose();
            });
        }

        dialog.setVisible(true);
    }

  private void deleteSelectedEmployee() {
    int viewRow = employeeTable.getSelectedRow();
    if (viewRow < 0) {
        return;
    }
    int modelRow = employeeTable.convertRowIndexToModel(viewRow);
    Employee emp = employees.get(modelRow);

    // Hiện hộp thoại xác nhận
    int choice = JOptionPane.showConfirmDialog(
        this,
        "Bạn có chắc chắn muốn xoá hoàn toàn nhân viên:\n"
      + emp.getName() + " (ID=" + emp.getId() + ")?",
        "Xác nhận xoá",
        JOptionPane.YES_NO_OPTION,
        JOptionPane.WARNING_MESSAGE
    );
    if (choice != JOptionPane.YES_OPTION) {
        return;  // nếu chọn No thì huỷ lệnh xoá
    }

    // 1) Xoá account trong EmployeeAccounts (nếu có)
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement psAcc = conn.prepareStatement(
             "DELETE FROM dbo.EmployeeAccounts WHERE EmployeeID = ?")) {
        psAcc.setString(1, emp.getId());
        psAcc.executeUpdate();
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi xoá tài khoản nhân viên:\n" + ex.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 2) Xoá record trong Employees
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement psEmp = conn.prepareStatement(
             "DELETE FROM dbo.Employees WHERE EmployeeID = ?")) {
        psEmp.setString(1, emp.getId());
        int deleted = psEmp.executeUpdate();
        if (deleted == 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy nhân viên trong database để xoá.",
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi xoá nhân viên khỏi database:\n" + ex.getMessage(),
            "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 3) Cập nhật lại UI
    employees.remove(modelRow);
    employeeTableModel.removeRow(modelRow);

    JOptionPane.showMessageDialog(this,
        "Xoá nhân viên và tài khoản thành công.",
        "Đã xoá", JOptionPane.INFORMATION_MESSAGE);
}
    private void reactivateAccount(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }

        String empId = (String) table.getValueAt(r, 1);
        // Gọi stored procedure để set AccountStatus = 'Active'
        setAccountStatus(empId, "Active");
        // Reload cả hai bảng
        loadAccounts("Active");
        loadAccounts("Inactive");
        overridePlainInModel(activeAccountModel, empId);
    }
    private void deactivateAccount(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }
        String empId = (String) table.getValueAt(r, 1);
        setAccountStatus(empId, "Inactive");
        loadAccounts("Active");
        loadAccounts("Inactive");
           overridePlainInModel(inactiveAccountModel, empId);
    }
    
    private void deleteAccount(JTable table) {
        int r = table.getSelectedRow();
        if (r < 0) {
            return;
        }

        String empId = (String) table.getValueAt(r, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Chắc chắn xoá tài khoản " + empId + "?", "Xác nhận", JOptionPane.OK_CANCEL_OPTION);
        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        // 1) Xoá trên database
        String sql = "DELETE FROM dbo.EmployeeAccounts WHERE EmployeeID = ?";
        try (Connection conn = DatabaseConnector.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, empId);
            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                JOptionPane.showMessageDialog(this,
                        "Không tìm thấy tài khoản trong DB để xoá.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi xoá tài khoản trên DB:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 2) Xoá trên giao diện
        inactiveAccountModel.removeRow(r);
        JOptionPane.showMessageDialog(this,
                "Xoá tài khoản thành công.",
                "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AdminUI::new);
    }

    private void savePhotoToDb(String empId, byte[] photoData) {
        String call = "{call dbo.sp_UpdateEmployeePhoto(?, ?)}";
        try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(call)) {
            cs.setString(1, empId);
            cs.setBytes(2, photoData);
            cs.execute();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Lỗi khi lưu ảnh:\n" + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class Employee {

    private String name;
    private final String id;
    private String address, gender, position, status;
    private int age;
    private double salary;
    // ✦ MỚI: lưu bytes của ảnh
    private byte[] photo;

    // Sửa constructor để nhận thêm photo (thông thường truyền null nếu chưa có):
    public Employee(String name, String id,
            String address, int age,
            String gender, String position,
            double salary, String status,
            byte[] photo) {
        this.name = name;
        this.id = id;
        this.address = address;
        this.age = age;
        this.gender = gender;
        this.position = position;
        this.salary = salary;
        this.status = status;
        this.photo = photo;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getGender() {
        return gender;
    }

    public String getPosition() {
        return position;
    }

    public String getStatus() {
        return status;
    }

    public int getAge() {
        return age;
    }

    public double getSalary() {
        return salary;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] p) {
        photo = p;
    }

    public void setName(String v) {
        name = v;
    }

    public void setAddress(String v) {
        address = v;
    }

    public void setPosition(String v) {
        position = v;
    }

    public void setStatus(String v) {
        status = v;
    }

    public void setAge(int v) {
        age = v;
    }

    public void setSalary(double v) {
        salary = v;
    }
}

class Account {

    String name, id, password;

    public Account(String name, String id, String password) {
        this.name = name;
        this.id = id;
        this.password = password;
    }
}

class TableMouseListener extends MouseAdapter {

    private final JTable table;

    public TableMouseListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int r = table.rowAtPoint(e.getPoint());
        if (r >= 0) {
            table.setRowSelectionInterval(r, r);
        }
    }
}
