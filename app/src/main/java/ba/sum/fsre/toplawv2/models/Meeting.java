package ba.sum.fsre.toplawv2.models;

public class Meeting {
    private String nazivSlucaja;
    private String note;
    private String date;
    private String startTime;
    private String endTime;
    private String priority;
    private boolean remindMe;
    private String userId;

    public Meeting() {}

    public Meeting(String nazivSlucaja, String note, String date, String startTime, String endTime,
                   String priority, boolean remindMe, String userId) {
        this.nazivSlucaja = nazivSlucaja;
        this.note = note;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.priority = priority;
        this.remindMe = remindMe;
        this.userId = userId;
    }

    // Getteri i setteri
    public String getNazivSlucaja() { return nazivSlucaja; }
    public void setNazivSlucaja(String nazivSlucaja) { this.nazivSlucaja = nazivSlucaja; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }

    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public boolean isRemindMe() { return remindMe; }
    public void setRemindMe(boolean remindMe) { this.remindMe = remindMe; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
