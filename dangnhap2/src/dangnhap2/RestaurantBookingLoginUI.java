package dangnhap2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import java.util.regex.Pattern;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.SwingUtilities;
import test.CombinedRestaurantUI;
import java.sql.CallableStatement;
import admin.AdminUI;
import javax.swing.JOptionPane;
import java.sql.Types;

public class RestaurantBookingLoginUI extends JFrame {

    private JTextField createFlatTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xB0BEC5)));
        tf.setBackground(Color.WHITE);
        tf.setForeground(new Color(0x212121));
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return tf;
    }

    /**
     * Tạo JPasswordField phẳng (bottom border).
     */
    private JPasswordField createFlatPasswordField(int columns) {
        JPasswordField pf = new JPasswordField(columns);
        pf.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xB0BEC5)));
        pf.setBackground(Color.WHITE);
        pf.setForeground(new Color(0x212121));
        pf.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return pf;
    }
    private final CardLayout cardLayout;
    private final JPanel mainPanel;
    private JPanel welcomePanel;
    private JButton btnEmpLogin, btnMgrLogin;
    private JPanel empLoginPanel;
    private JTextField empIdField;
    private JPasswordField empPassField;
    private JToggleButton eyeEmpBtn;
    private JButton btnEmpSignIn, btnEmpBack;
    private JLabel empForgotLabel;
    private JPanel mgrLoginPanel;
    private JTextField mgrIdField, mgrUserField;
    private JPasswordField mgrPassField;
    private JToggleButton eyeMgrBtn;
    private JButton btnMgrSignIn, btnMgrBack;
    private JLabel mgrForgotLabel;
    private JPanel forgotPanel;
    private JTextField nameField, forgotIdField, phoneField, emailField, verificationField;
    private JLabel codeLabel;
    private JButton btnConfirm, btnBack;
    private String currentCode;
    private String lastLoginCard;
    private String loginRole;
    private char empPassDefaultEcho;
    private char mgrPassDefaultEcho;

    public RestaurantBookingLoginUI() {
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
        UIManager.put("Button.select", new Color(0x004D40));       // hover/push
        UIManager.put("Label.foreground", new Color(0x212121));
        UIManager.put("TextField.background", Color.WHITE);
        UIManager.put("TextField.foreground", new Color(0x212121));
        UIManager.put("TextField.border", BorderFactory.createLineBorder(new Color(0xB0BEC5)));
        UIManager.put("PasswordField.background", Color.WHITE);
        UIManager.put("PasswordField.foreground", new Color(0x212121));
        UIManager.put("PasswordField.border", BorderFactory.createLineBorder(new Color(0xB0BEC5)));
        setTitle("Hệ thống quản lí đặt bàn nhà hàng");
        setSize(500, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        initWelcomePanel();
        initEmpLoginPanel();
        initMgrLoginPanel();
        initForgotPanel();

        getContentPane().add(mainPanel);
        cardLayout.show(mainPanel, "welcome");
    }

    private void initWelcomePanel() {
        welcomePanel = new JPanel(new BorderLayout(0, 20));
        JLabel title = new JLabel("Hệ thống quản lí đặt bàn nhà hàng", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        welcomePanel.add(title, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        btnEmpLogin = new JButton("Đăng nhập nhân viên");
        btnMgrLogin = new JButton("Đăng nhập quản lí");
        btnPanel.add(btnEmpLogin);
        btnPanel.add(btnMgrLogin);
        welcomePanel.add(btnPanel, BorderLayout.SOUTH);

        btnEmpLogin.addActionListener(e -> {
            lastLoginCard = "empLogin";
            clearEmpFields();
            cardLayout.show(mainPanel, "empLogin");
        });
        btnMgrLogin.addActionListener(e -> {
            lastLoginCard = "mgrLogin";
            clearMgrFields();
            cardLayout.show(mainPanel, "mgrLogin");
        });

        mainPanel.add(welcomePanel, "welcome");
    }

    private void initEmpLoginPanel() {
        empLoginPanel = new JPanel(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        empLoginPanel.add(new JLabel("Mã số nhân viên:"), gbc);
        gbc.gridx = 1;
        empIdField = createFlatTextField(15);
        empLoginPanel.add(empIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        empLoginPanel.add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1;

        // Mật khẩu (flat + eye toggle)
        empPassField = createFlatPasswordField(15);
// lưu echo mặc định (thường là '●' hoặc '*')
        empPassDefaultEcho = empPassField.getEchoChar();

        JPanel passPanel = new JPanel(new BorderLayout(5, 0));
        passPanel.setOpaque(false);
        passPanel.add(empPassField, BorderLayout.CENTER);

// khởi tạo eyeEmpBtn (biến thành viên)
        eyeEmpBtn = new JToggleButton("\uD83D\uDC41");  // biểu tượng 👁
        eyeEmpBtn.setPreferredSize(new Dimension(30, empPassField.getPreferredSize().height));
        eyeEmpBtn.setBorder(null);
        eyeEmpBtn.setContentAreaFilled(false);
        eyeEmpBtn.addActionListener(ev -> {
            // toggle echo
            empPassField.setEchoChar(eyeEmpBtn.isSelected() ? (char) 0 : empPassDefaultEcho);
        });
        passPanel.add(eyeEmpBtn, BorderLayout.EAST);

// thêm cả panel chứa field+eye
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        empLoginPanel.add(passPanel, gbc);
        gbc.gridwidth = 1;  // reset

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        empForgotLabel = new JLabel("Quên mật khẩu?", SwingConstants.RIGHT);
        empForgotLabel.setForeground(Color.BLUE.darker());
        empForgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        empLoginPanel.add(empForgotLabel, gbc);

        gbc.gridy = 3;
        btnEmpSignIn = new JButton("Đăng nhập");
        empLoginPanel.add(btnEmpSignIn, gbc);
        btnEmpSignIn.addActionListener((ActionEvent e) -> {
            String userId = empIdField.getText().trim();
            String password = new String(empPassField.getPassword());

            if (userId.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ Mã số và Mật khẩu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String callSp = "{call dbo.sp_EmployeeLogin(?,?)}";
            try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(callSp)) {

                cs.setString(1, userId);
                cs.setString(2, password);
                cs.execute();
                try (ResultSet rs1 = cs.getResultSet()) {
                    rs1.next();
                    int loginResult = rs1.getInt("LoginResult");
                    String message = rs1.getString("Message");
                    loginRole = rs1.getString("Role");  // <-- lưu Role vào biến toàn cục
                    if (loginResult != 1) {
                        JOptionPane.showMessageDialog(this, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT AccountStatus FROM dbo.EmployeeAccounts WHERE EmployeeID = ?")) {
                        ps.setString(1, userId);
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                String accStatus = rs.getString("AccountStatus");
                                if (!"Active".equalsIgnoreCase(accStatus)) {
                                    JOptionPane.showMessageDialog(this,
                                            "Tài khoản của bạn đang bị khoá (Status=" + accStatus + ").",
                                            "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                        }
                    }
                }
                // 2) Chuyển sang result‑set thứ hai: UserID + FullName
                if (cs.getMoreResults()) {
                    try (ResultSet rs2 = cs.getResultSet()) {
                        if (rs2.next()) {
                            String actualUserId = rs2.getString("UserID");
                            String fullName = rs2.getString("FullName");

                            // Bất kể user là Admin hay Employee đều vào giao diện nhân viên
                            new CombinedRestaurantUI(actualUserId, fullName).setVisible(true);
                            RestaurantBookingLoginUI.this.dispose();
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi kết nối hoặc thực thi:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }

        });
        gbc.gridy = 4;
        btnEmpBack = new JButton("Quay lại");
        empLoginPanel.add(btnEmpBack, gbc);

        empForgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                generateCode();
                cardLayout.show(mainPanel, "forgot");
            }
        });
        btnEmpBack.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        mainPanel.add(empLoginPanel, "empLogin");
    }

    private void initMgrLoginPanel() {
        mgrLoginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        mgrLoginPanel.add(new JLabel("Mã số quản lí:"), gbc);
        gbc.gridx = 1;
        mgrIdField = createFlatTextField(15);
        mgrLoginPanel.add(mgrIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        mgrLoginPanel.add(new JLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1;
        mgrUserField = createFlatTextField(15);
        mgrLoginPanel.add(mgrUserField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        mgrLoginPanel.add(new JLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1;
        
// Mật khẩu quản lí (flat + eye toggle)
mgrPassField = createFlatPasswordField(15);
// lưu echo mặc định
mgrPassDefaultEcho = mgrPassField.getEchoChar();

// gói chung vào panel
JPanel mgrPassPanel = new JPanel(new BorderLayout(5,0));
mgrPassPanel.setOpaque(false);
mgrPassPanel.add(mgrPassField, BorderLayout.CENTER);

// khởi tạo eyeMgrBtn (biến thành viên)
eyeMgrBtn = new JToggleButton("\uD83D\uDC41");
eyeMgrBtn.setPreferredSize(new Dimension(30, mgrPassField.getPreferredSize().height));
eyeMgrBtn.setBorder(null);
eyeMgrBtn.setContentAreaFilled(false);
eyeMgrBtn.addActionListener(ev -> {
    mgrPassField.setEchoChar(
        eyeMgrBtn.isSelected() ? (char)0 : mgrPassDefaultEcho
    );
});
mgrPassPanel.add(eyeMgrBtn, BorderLayout.EAST);

// thêm vào layout
gbc.gridx = 1;
gbc.gridwidth = 2;
mgrLoginPanel.add(mgrPassPanel, gbc);
gbc.gridwidth = 1;

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        mgrForgotLabel = new JLabel("Quên mật khẩu?", SwingConstants.RIGHT);
        // mgrForgotLabel.setForeground(Color.BLUE.darker());
        mgrForgotLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        // mgrLoginPanel.add(mgrForgotLabel, gbc);

        gbc.gridy = 4;
        btnMgrSignIn = new JButton("Đăng nhập");

// Gọi sp_AdminLogin và mở AdminUI khi thành công
        btnMgrSignIn.addActionListener(e -> {
            String adminId = mgrIdField.getText().trim();
            String user = mgrUserField.getText().trim();
            String pwd = new String(mgrPassField.getPassword());

            if (adminId.isEmpty() || user.isEmpty() || pwd.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Vui lòng nhập đầy đủ Mã quản lí, Tên đăng nhập và Mật khẩu.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Gọi Stored Procedure (thay 3 tham số nếu bạn đã ALTER SP)
            String callSp = "{call dbo.sp_AdminLogin(?,?,?)}";
            try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall(callSp)) {

                cs.setString(1, adminId);
                cs.setString(2, user);
                cs.setString(3, pwd);

                cs.execute();
                try (ResultSet rs = cs.getResultSet()) {
                    rs.next();
                    int ok = rs.getInt("LoginResult");
                    String msg = rs.getString("Message");
                    if (ok != 1) {
                        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // Mở giao diện Admin
                new admin.AdminUI().setVisible(true);
                SwingUtilities.getWindowAncestor(mgrLoginPanel).dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi kết nối hoặc thực thi:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

// Thêm lại vào panel như cũ
        mgrLoginPanel.add(btnMgrSignIn, gbc);

        gbc.gridy = 5;
        btnMgrBack = new JButton("Quay lại");
        mgrLoginPanel.add(btnMgrBack, gbc);

        mgrForgotLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                generateCode();
                cardLayout.show(mainPanel, "forgot");
            }
        });
        btnMgrBack.addActionListener(e -> cardLayout.show(mainPanel, "welcome"));

        mainPanel.add(mgrLoginPanel, "mgrLogin");
    }

    private void initForgotPanel() {
        forgotPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        int row = 0;

        // Họ tên
        gbc.gridx = 0;
        gbc.gridy = row;
        forgotPanel.add(new JLabel("Họ và tên:"), gbc);
        gbc.gridx = 1;
        nameField = createFlatTextField(14);
        forgotPanel.add(nameField, gbc);

        // Mã số nhân viên
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        forgotPanel.add(new JLabel("Mã số nhân viên:"), gbc);
        gbc.gridx = 1;
        forgotIdField = createFlatTextField(14);
        forgotPanel.add(forgotIdField, gbc);

// Mật khẩu mới
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        forgotPanel.add(new JLabel("Mật khẩu mới:"), gbc);

// Tạo field + nút “mắt”
        JPasswordField newPassField = createFlatPasswordField(15);
        char echoNew = newPassField.getEchoChar();
        JToggleButton eyeNew = new JToggleButton("\uD83D\uDC41");
        eyeNew.setPreferredSize(new Dimension(30, newPassField.getPreferredSize().height));
        eyeNew.setBorder(null);
        eyeNew.setContentAreaFilled(false);
        eyeNew.addActionListener(ev -> {
            newPassField.setEchoChar(eyeNew.isSelected() ? (char) 0 : echoNew);
        });

// Gói chung vào panel phẳng
        JPanel newPwPanel = new JPanel(new BorderLayout(5, 0));
        newPwPanel.setOpaque(false);
        newPwPanel.add(newPassField, BorderLayout.CENTER);
        newPwPanel.add(eyeNew, BorderLayout.EAST);

// Thêm mớiPwPanel thay cho newPassField
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        forgotPanel.add(newPwPanel, gbc);
        gbc.gridwidth = 1;  // reset nếu cần

// Xác nhận mật khẩu mới
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        forgotPanel.add(new JLabel("Nhập lại mật khẩu:"), gbc);

// Tạo confirm field + nút “mắt”
        JPasswordField confirmField = createFlatPasswordField(15);
        char echoConfirm = confirmField.getEchoChar();
        JToggleButton eyeConfirm = new JToggleButton("\uD83D\uDC41");
        eyeConfirm.setPreferredSize(new Dimension(30, confirmField.getPreferredSize().height));
        eyeConfirm.setBorder(null);
        eyeConfirm.setContentAreaFilled(false);
        eyeConfirm.addActionListener(ev -> {
            confirmField.setEchoChar(eyeConfirm.isSelected() ? (char) 0 : echoConfirm);
        });

// Gói chung vào panel phẳng
        JPanel confirmPwPanel = new JPanel(new BorderLayout(5, 0));
        confirmPwPanel.setOpaque(false);
        confirmPwPanel.add(confirmField, BorderLayout.CENTER);
        confirmPwPanel.add(eyeConfirm, BorderLayout.EAST);

// Thêm confirmPwPanel
        gbc.gridx = 1;
        gbc.gridwidth = 2;
        forgotPanel.add(confirmPwPanel, gbc);
        gbc.gridwidth = 1;
        // Mã xác minh
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        forgotPanel.add(new JLabel("Mã xác minh:"), gbc);
        gbc.gridx = 1;
        verificationField = createFlatTextField(14);
        forgotPanel.add(verificationField, gbc);
        row++;
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        codeLabel = new JLabel(currentCode, SwingConstants.CENTER);
        codeLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        forgotPanel.add(codeLabel, gbc);

        // Nút xác nhận
        row++;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        btnConfirm = new JButton("Xác nhận cấp lại mật khẩu");
        forgotPanel.add(btnConfirm, gbc);

        // Nút quay lại
        row++;
        gbc.gridy = row;
        btnBack = new JButton("Quay lại");
        forgotPanel.add(btnBack, gbc);

        // Listener cho btnConfirm
        btnConfirm.addActionListener(e -> {
            String fullName = nameField.getText().trim();
            String empId = forgotIdField.getText().trim();
            String pw1 = new String(newPassField.getPassword());
            String pw2 = new String(confirmField.getPassword());
            String code = verificationField.getText().trim();

            // 1) Kiểm tra không trống
            if (fullName.isEmpty() || empId.isEmpty() || pw1.isEmpty() || pw2.isEmpty() || code.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Không thể để trống.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 2) kiểm mã xác minh
            if (!code.equals(currentCode)) {
                JOptionPane.showMessageDialog(this, "Mã xác minh không trùng khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 3) kiểm mật khẩu trùng
            if (!pw1.equals(pw2)) {
                JOptionPane.showMessageDialog(this, "Mật khẩu nhập lại không khớp.", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // 4) Gọi SP reset pw
            try (Connection conn = DatabaseConnector.getConnection(); CallableStatement cs = conn.prepareCall("{call dbo.sp_ResetEmployeePassword(?,?,?,?,?)}")) {

                // set IN params
                cs.setString(1, empId);
                cs.setString(2, fullName);
                cs.setString(3, pw1);
                // register OUT params
                cs.registerOutParameter(4, Types.INTEGER);
                cs.registerOutParameter(5, Types.NVARCHAR);

                cs.execute();

                int res = cs.getInt(4);
                String msg = cs.getString(5);
                if (res == 1) {
                    JOptionPane.showMessageDialog(this,
                            "Cấp lại mật khẩu thành công!\nMã: " + empId + "\nMật khẩu mới: " + pw1,
                            "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    cardLayout.show(mainPanel, lastLoginCard);
                } else {
                    JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Lỗi cơ sở dữ liệu:\n" + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        btnBack.addActionListener(e -> cardLayout.show(mainPanel, lastLoginCard));
        mainPanel.add(forgotPanel, "forgot");
    }

    private void generateCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        currentCode = sb.toString();
        codeLabel.setText(currentCode);
    }

    private void handleConfirm() {
        String name = nameField.getText().trim();
        String id = forgotIdField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String code = verificationField.getText().trim();

        if (name.isEmpty() || id.isEmpty() || phone.isEmpty() || email.isEmpty() || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không thể để trống thông tin", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        boolean validPhone = Pattern.matches("0\\d{9}", phone);
        boolean validEmail = email.contains("@") && !email.startsWith("@") && !email.endsWith("@");
        if (!validPhone || !validEmail) {
            JOptionPane.showMessageDialog(this, "Số điện thoại hoặc email không đúng định dạng", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!code.equals(currentCode)) {
            JOptionPane.showMessageDialog(this, "Mã xác nhận đã nhập sai", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Xác nhận thành công! Vui lòng kiểm tra email để nhận mật khẩu mới.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
        cardLayout.show(mainPanel, lastLoginCard);
    }

    private void clearEmpFields() {
        empIdField.setText("");
        empPassField.setText("");
        eyeEmpBtn.setSelected(false);
        empPassField.setEchoChar(empPassDefaultEcho);
    }

    private void clearMgrFields() {
        mgrIdField.setText("");
        mgrUserField.setText("");
        mgrPassField.setText("");
        eyeMgrBtn.setSelected(false);
        mgrPassField.setEchoChar(mgrPassDefaultEcho);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RestaurantBookingLoginUI().setVisible(true));
    }
}
