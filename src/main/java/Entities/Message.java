package Entities;

import java.util.Date;

public class Message {
    private int id;
    private int senderId;
    private String message;
    private Date date;
    private String type;
    private String status;
    private String filePath;

    public Message(){}

    public Message(int id, int senderId, String message) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
    }

    public Message(int senderId, String message){
        this.senderId = senderId;
        this.message = message;
    }

    public Message(int id, int senderId, String message, Date date, String type, String status, String filePath) {
        this.id = id;
        this.senderId = senderId;
        this.message = message;
        this.date = date;
        this.type = type;
        this.status = status;
        this.filePath = filePath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
