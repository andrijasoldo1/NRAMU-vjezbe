package ba.sum.fsre.mymath.models;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.PropertyName;

public class Case {

    @DocumentId
    private String id; // Firestore document ID

    private String name;
    private String userId; // ID of the user who owns the case
    private String lawyerId; // ID of the assigned lawyer
    private double price;
    private String description;
    private String attachedDocumentation;
    private String status; // Open, In Progress, Closed
    private boolean isAnonymous;
    private String typeOfCase;

    public Case() {
        // Default constructor for Firestore
    }

    public Case(String name, String userId, String lawyerId, double price, String description,
                String attachedDocumentation, String status, boolean isAnonymous, String typeOfCase) {
        this.name = name;
        this.userId = userId;
        this.lawyerId = lawyerId;
        this.price = price;
        this.description = description;
        this.attachedDocumentation = attachedDocumentation;
        this.status = status;
        this.isAnonymous = isAnonymous;
        this.typeOfCase = typeOfCase;
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("name")
    public String getName() {
        return name;
    }

    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("lawyerId")
    public String getLawyerId() {
        return lawyerId;
    }

    @PropertyName("lawyerId")
    public void setLawyerId(String lawyerId) {
        this.lawyerId = lawyerId;
    }

    @PropertyName("price")
    public double getPrice() {
        return price;
    }

    @PropertyName("price")
    public void setPrice(double price) {
        this.price = price;
    }

    @PropertyName("description")
    public String getDescription() {
        return description;
    }

    @PropertyName("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @PropertyName("attachedDocumentation")
    public String getAttachedDocumentation() {
        return attachedDocumentation;
    }

    @PropertyName("attachedDocumentation")
    public void setAttachedDocumentation(String attachedDocumentation) {
        this.attachedDocumentation = attachedDocumentation;
    }

    @PropertyName("status")
    public String getStatus() {
        return status;
    }

    @PropertyName("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @PropertyName("isAnonymous")
    public boolean isAnonymous() {
        return isAnonymous;
    }

    @PropertyName("isAnonymous")
    public void setAnonymous(boolean anonymous) {
        isAnonymous = anonymous;
    }

    @PropertyName("typeOfCase")
    public String getTypeOfCase() {
        return typeOfCase;
    }

    @PropertyName("typeOfCase")
    public void setTypeOfCase(String typeOfCase) {
        this.typeOfCase = typeOfCase;
    }
}
