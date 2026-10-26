package com.mycompany.nhom11_doanmonhoc.model;

import java.io.Serializable;

public class Email implements Serializable {
    private static final long serialVersionUID = 1L;

    private String sender;
    private String recipient;
    private String subject;
    private String content;
    private String senderIP;
    private boolean isSpam;

    public Email(String sender, String recipient, String subject, String content) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.content = content;
        this.isSpam = false;
    }

    // Getters và Setters
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getSenderIP() { return senderIP; }
    public void setSenderIP(String senderIP) { this.senderIP = senderIP; }

    public boolean isSpam() { return isSpam; }
    public void setSpam(boolean spam) { isSpam = spam; }
}