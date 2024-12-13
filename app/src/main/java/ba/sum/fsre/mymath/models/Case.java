package ba.sum.fsre.mymath.models;

import com.google.firebase.firestore.PropertyName;

import java.util.ArrayList;
import java.util.List;

public class Case {

    private String id; // Firestore document ID
    private String name; // Case name
    private String userId; // User who owns the case
    private String lawyerId; // Assigned lawyer ID
    private double price; // Price of the case
    private String description; // Case description
    private Object attachedDocumentation; // Flexible to handle both String and List<String>
    private String status; // Case status (e.g., "Open")
    private boolean isAnonymous; // Whether the case is anonymous
    private String typeOfCase; // Type of case (e.g., "Civil")

    public Case() {
        // Default constructor for Firestore
    }

    public Case(String name, String userId, String lawyerId, double price, String description,
                List<String> attachedDocumentation, String status, boolean isAnonymous, String typeOfCase) {
        this.name = name;
        this.userId = userId;
        this.lawyerId = lawyerId;
        this.price = price;
        this.description = description;
        this.attachedDocumentation = attachedDocumentation != null ? attachedDocumentation : new ArrayList<>();
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
    public List<String> getAttachedDocumentation() {
        if (attachedDocumentation instanceof String) {
            // Convert single String to a List<String>
            List<String> docs = new ArrayList<>();
            docs.add((String) attachedDocumentation);
            return docs;
        } else if (attachedDocumentation instanceof List) {
            return (List<String>) attachedDocumentation;
        }
        return new ArrayList<>();
    }

    @PropertyName("attachedDocumentation")
    public void setAttachedDocumentation(Object attachedDocumentation) {
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
