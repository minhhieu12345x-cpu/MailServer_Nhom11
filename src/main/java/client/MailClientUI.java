/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to edit this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package client;

import com.mycompany.nhom11_doanmonhoc.model.Email;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;

/**
 *
 * @author dell
 */
public class MailClientUI extends JFrame {
    private JTextField txtSender, txtRecipient, txtSubject;
    private JTextArea txtContent, txtLog;
    private JButton btnSend, btnViewInbox, btnViewSpam;

    // =========================================================================
    // CẤU HÌNH IP SERVER TẠI ĐÂY:
    // - Khi test 1 máy (Local): để là "127.0.0.1"
    // - Khi test 2 máy (Wi-Fi điện thoại): đổi thành IPv4 của máy Server (VD: "192.168.43.15")
    // =========================================================================
    private static final String SERVER_HOST = "127.0.0.1";
    private static final int SERVER_PORT = 8025;

    public MailClientUI() {
        setTitle("Hệ thống Mail Client - Nhóm 11");
        setSize(800, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));

        // --- Panel Soạn thư ---
        JPanel panelTop = new JPanel(new GridLayout(6, 2, 5, 5));
        panelTop.setBorder(BorderFactory.createTitledBorder("Soạn Thư Điện Tử (SMTP Client)"));

        panelTop.add(new JLabel(" Email Người Gửi:"));
        txtSender = new JTextField();
        panelTop.add(txtSender);

        panelTop.add(new JLabel(" Email Người Nhận:"));
        txtRecipient = new JTextField();
        panelTop.add(txtRecipient);

        panelTop.add(new JLabel(" Tiêu đề (Subject):"));
        txtSubject = new JTextField();
        panelTop.add(txtSubject);

        add(panelTop, BorderLayout.NORTH);

        // --- Panel Nội dung thư ---
        JPanel panelCenter = new JPanel(new BorderLayout(5, 5));
        panelCenter.setBorder(BorderFactory.createTitledBorder("Nội dung thư (Hệ thống sẽ quét từ khóa rác khi gửi)"));
        txtContent = new JTextArea();
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        panelCenter.add(new JScrollPane(txtContent), BorderLayout.CENTER);

        // Các nút bấm chức năng
        JPanel panelButtons = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        btnSend = new JButton("Gửi Email");
        btnViewInbox = new JButton("Xem Inbox Server");
        btnViewSpam = new JButton("Xem Thư Rác (Spam)");
        
        panelButtons.add(btnSend);
        panelButtons.add(btnViewInbox);
        panelButtons.add(btnViewSpam);
        panelCenter.add(panelButtons, BorderLayout.SOUTH);

        add(panelCenter, BorderLayout.CENTER);

        // --- Panel Log Phản hồi ---
        JPanel panelSouth = new JPanel(new BorderLayout());
        panelSouth.setBorder(BorderFactory.createTitledBorder("Trạng thái / Nhật ký phản hồi từ Server"));
        txtLog = new JTextArea(4, 20);
        txtLog.setEditable(false);
        panelSouth.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        add(panelSouth, BorderLayout.SOUTH);

        // --- Xử lý sự kiện (Actions) ---
        btnSend.addActionListener(e -> sendEmail());
        btnViewInbox.addActionListener(e -> openMailBoxDialog("storage/inbox", "HỘP THƯ ĐẾN (INBOX)"));
        btnViewSpam.addActionListener(e -> openMailBoxDialog("storage/spam", "HỘP THƯ RÁC (SPAM)"));
    }

    private void sendEmail() {
        String sender = txtSender.getText().trim();
        String recipient = txtRecipient.getText().trim();
        String subject = txtSubject.getText().trim();
        String content = txtContent.getText().trim();

        if (sender.isEmpty() || recipient.isEmpty() || subject.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ thông tin người gửi, nhận và tiêu đề!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Email email = new Email(sender, recipient, subject, content);
            
            oos.writeObject(email);
            oos.flush();

            String serverResponse = (String) ois.readObject();
            txtLog.append("[GỬI THÀNH CÔNG] " + serverResponse + "\n");
            JOptionPane.showMessageDialog(this, serverResponse, "Kết quả lọc Spam từ Server", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            txtLog.append("[LỖI KẾT NỐI] Không thể kết nối tới Server tại " + SERVER_HOST + ":" + SERVER_PORT + "\n");
            JOptionPane.showMessageDialog(this, "Không thể kết nối tới Server tại " + SERVER_HOST, "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void openMailBoxDialog(String folderPath, String boxTitle) {
        File dir = new File(folderPath);
        if (!dir.exists() || dir.listFiles() == null || dir.listFiles().length == 0) {
            JOptionPane.showMessageDialog(this, "Thư mục " + boxTitle + " hiện đang trống!", boxTitle, JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JDialog dialog = new JDialog(this, boxTitle, true);
        dialog.setSize(800, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        File[] files = dir.listFiles();
        DefaultListModel<EmailFileItem> listModel = new DefaultListModel<>();

        for (File file : files) {
            if (file.isFile()) {
                String sender = "Không rõ";
                String subject = "Không có tiêu đề";
                String time = "Không rõ thời gian";

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        if (line.startsWith("Sender: ")) sender = line.substring(8);
                        else if (line.startsWith("Subject: ")) subject = line.substring(9);
                        else if (line.startsWith("Time: ")) time = line.substring(6);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                listModel.addElement(new EmailFileItem(file, sender, subject, time));
            }
        }

        JList<EmailFileItem> emailList = new JList<>(listModel);
        emailList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emailList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof EmailFileItem) {
                    EmailFileItem item = (EmailFileItem) value;
                    setText("<html><b>Từ:</b> " + item.sender + " | <b>Tiêu đề:</b> " + item.subject + "<br/><font color='gray'>Thời gian: " + item.time + "</font></html>");
                }
                return c;
            }
        });

        JTextArea txtDetailContent = new JTextArea();
        txtDetailContent.setEditable(false);
        txtDetailContent.setLineWrap(true);
        txtDetailContent.setWrapStyleWord(true);

        emailList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                EmailFileItem selectedItem = emailList.getSelectedValue();
                if (selectedItem != null) {
                    StringBuilder fullContent = new StringBuilder();
                    try (BufferedReader br = new BufferedReader(new FileReader(selectedItem.file))) {
                        String line;
                        while ((line = br.readLine()) != null) {
                            fullContent.append(line).append("\n");
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    txtDetailContent.setText(fullContent.toString());
                }
            }
        });

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(emailList), new JScrollPane(txtDetailContent));
        splitPane.setDividerLocation(320);
        dialog.add(splitPane, BorderLayout.CENTER);

        JPanel panelBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnDelete = new JButton("Xóa Thư Đã Chọn");
        btnDelete.setForeground(Color.RED);
        
        btnDelete.addActionListener(e -> {
            EmailFileItem selectedItem = emailList.getSelectedValue();
            if (selectedItem != null) {
                int confirm = JOptionPane.showConfirmDialog(dialog, "Bạn có chắc chắn muốn xóa vĩnh viễn email này không?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (selectedItem.file.delete()) {
                        JOptionPane.showMessageDialog(dialog, "Đã xóa email thành công!");
                        dialog.dispose();
                        openMailBoxDialog(folderPath, boxTitle);
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Không thể xóa tệp tin email!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(dialog, "Vui lòng chọn một email từ danh sách bên trái để xóa!", "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        panelBottom.add(btnDelete);
        dialog.add(panelBottom, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    private static class EmailFileItem {
        File file;
        String sender;
        String subject;
        String time;

        public EmailFileItem(File file, String sender, String subject, String time) {
            this.file = file;
            this.sender = sender;
            this.subject = subject;
            this.time = time;
        }

        @Override
        public String toString() {
            return subject;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MailClientUI().setVisible(true);
        });
    }
}