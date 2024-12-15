package ba.sum.fsre.mymath.models;

public class Conversation {

    private String receiverId;
    private String receiverName;
    private String lastMessage;
    private long timestamp;

    public Conversation() {
        // Default constructor
    }

    public Conversation(String receiverId, String receiverName, String lastMessage, long timestamp) {
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}