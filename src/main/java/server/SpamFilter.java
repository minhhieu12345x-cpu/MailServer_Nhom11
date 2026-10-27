/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server;
import com.mycompany.nhom11_doanmonhoc.model.Email;
import java.io.*;
import java.util.*;
/**
 *
 * @author dell
 */
public class SpamFilter {
    private List<String> blacklistIPs = new ArrayList<>();
    private List<String> keywords = new ArrayList<>();

    public SpamFilter() {
        loadBlacklist();
        loadKeywords();
    }

    // Đọc danh sách IP bị chặn từ blacklist.txt
    private void loadBlacklist() {
        File file = new File("blacklist.txt");
        if (!file.exists()) createDefaultFile(file, "127.0.0.2\n192.168.1.100");
        
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) blacklistIPs.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Đọc danh sách từ khóa rác từ keywords.txt
    private void loadKeywords() {
        File file = new File("keywords.txt");
        if (!file.exists()) createDefaultFile(file, "quảng cáo\ntrúng thưởng\nkhuyến mãi\nkiếm tiền\nfree");

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) keywords.add(line.trim().toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hàm kiểm tra Email có phải Spam không
    public boolean checkSpam(Email email) {
        // 1. Kiểm tra IP người gửi
        if (email.getSenderIP() != null && blacklistIPs.contains(email.getSenderIP())) {
            return true;
        }

        // 2. Kiểm tra từ khóa trong Tiêu đề và Nội dung
        String textToScan = (email.getSubject() + " " + email.getContent()).toLowerCase();
        for (String kw : keywords) {
            if (textToScan.contains(kw)) {
                return true;
            }
        }

        return false;
    }

    private void createDefaultFile(File file, String defaultContent) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(defaultContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
