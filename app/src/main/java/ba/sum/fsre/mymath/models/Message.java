package ba.sum.fsre.mymath.models;

import com.google.firebase.firestore.PropertyName;

import java.util.List;

public class Message {

    private String senderId;
    private String receiverId;
    private String text;
    private String base64Media; // Optional for images/files
    private long timestamp;
    private List<String> participants; // List of participants' IDs

    // Default constructor (required for Firestore)
    public Message() {}

    // Constructor
    public Message(String senderId, String receiverId, String text, String base64Media, long timestamp) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.base64Media = base64Media;
        this.timestamp = timestamp;

        // Automatically set participants
        this.participants = List.of(senderId, receiverId);
    }

    @PropertyName("senderId")
    public String getSenderId() {
        return senderId;
    }

    @PropertyName("senderId")
    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    @PropertyName("receiverId")
    public String getReceiverId() {
        return receiverId;
    }

    @PropertyName("receiverId")
    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    @PropertyName("text")
    public String getText() {
        return text;
    }

    @PropertyName("text")
    public void setText(String text) {
        this.text = text;
    }

    @PropertyName("base64Media")
    public String getBase64Media() {
        return base64Media;
    }

    @PropertyName("base64Media")
    public void setBase64Media(String base64Media) {
        this.base64Media = base64Media;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @PropertyName("participants")
    public List<String> getParticipants() {
        return participants;
    }

    @PropertyName("participants")
    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}
