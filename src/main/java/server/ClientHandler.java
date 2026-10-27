package server;

import com.mycompany.nhom11_doanmonhoc.model.Email;
import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientHandler implements Runnable {
    private Socket socket;
    private SpamFilter spamFilter;

    public ClientHandler(Socket socket, SpamFilter spamFilter) {
        this.socket = socket;
        this.spamFilter = spamFilter;
    }

    @Override
    public void run() {
        try {
            String clientIP = socket.getInetAddress().getHostAddress();
            
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                
                Email email = (Email) ois.readObject();
                email.setSenderIP(clientIP);

                boolean isSpam = spamFilter.checkSpam(email);
                email.setSpam(isSpam);

                saveEmailToFile(email);

                String response = isSpam ? "THƯ ĐÃ ĐƯỢC ĐÁNH DẤU LÀ SPAM VÀ LƯU VÀO MỤC SPAM!" : "GỬI THƯ THÀNH CÔNG VÀO INBOX!";
                oos.writeObject(response);
                oos.flush();
                
                System.out.println("[SERVER LOG] Nhan email tu IP: " + clientIP + " | Nguoi gui: " + email.getSender() + " | Ket Qua: " + (isSpam ? "SPAM" : "INBOX"));

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveEmailToFile(Email email) {
        String folderPath = email.isSpam() ? "storage/spam" : "storage/inbox";
        File dir = new File(folderPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS").format(new Date());
        String fileName = folderPath + "/email_" + timeStamp + ".txt";

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {
            writer.println("Sender: " + email.getSender());
            writer.println("Recipient: " + email.getRecipient());
            writer.println("SenderIP: " + email.getSenderIP());
            writer.println("Subject: " + email.getSubject());
            writer.println("Time: " + new Date());
            writer.println("----------------------------------------");
            writer.println("Content:\n" + email.getContent());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}