package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MailServer {
    private static final int PORT = 8025;

    public static void main(String[] args) {
        SpamFilter spamFilter = new SpamFilter();
        ExecutorService pool = Executors.newFixedThreadPool(10);
        
        // Kich hoat tinh nang tu dong don dep thu rac ngam
        startAutoCleanSpamTask();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("==========================================");
            System.out.println("MAIL SERVER DA KHOI DONG TAI CONG " + PORT);
            System.out.println("Dang cho ket noi tu Client...");
            System.out.println("==========================================");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket, spamFilter));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pool.shutdown();
        }
    }

    private static void startAutoCleanSpamTask() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        
        // Cai dat 30 giay de test nhanh (khi nop bai co the doi lai thanh 7L * 24 * 60 * 60 * 1000)
        long expireDurationInMillis = 30 * 1000; 

        scheduler.scheduleAtFixedRate(() -> {
            File spamDir = new File("storage/spam");
            if (spamDir.exists() && spamDir.isDirectory()) {
                File[] files = spamDir.listFiles();
                if (files != null) {
                    long currentTime = System.currentTimeMillis();

                    for (File file : files) {
                        if (file.isFile()) {
                            long fileAge = currentTime - file.lastModified();
                            if (fileAge > expireDurationInMillis) {
                                // Doc thong tin chi tiet tu file truoc khi xoa de hien thi len console
                                String sender = "Khong ro";
                                String subject = "Khong co tieu de";
                                String time = "Khong ro thoi gian";

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

                                // Thuc hien xoa file
                                if (file.delete()) {
                                    System.out.println("\n--------------------------------------------------");
                                    System.out.println("[AUTO-CLEAN SERVER] Da tu dong xoa thu rac qua han!");
                            
                                    System.out.println("• Nguoi gui: " + sender);
                                    System.out.println("• Tieu de  : " + subject);
                                    System.out.println("• Thoi gian: " + time);
                                    System.out.println("--------------------------------------------------");
                                }
                            }
                        }
                    }
                }
            }
        }, 0, 10, TimeUnit.SECONDS); // Quet kiem tra moi 10 giay
    }
}