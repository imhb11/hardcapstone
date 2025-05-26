package com.example.youlivealone;

public class ChatMessage {
    private String senderId;
    private String message;
    private String timestamp;

    // 전체 파라미터를 받는 생성자
    public ChatMessage(String senderId, String message, String timestamp) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = timestamp;
    }

    // 간단 생성자 (현재 시간을 자동으로 설정)
    public ChatMessage(String senderId, String message) {
        this.senderId = senderId;
        this.message = message;
        this.timestamp = String.valueOf(System.currentTimeMillis());
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // 사용자 메시지 여부 판단
    public boolean isUser() {
        return "user".equals(senderId);
    }
}
