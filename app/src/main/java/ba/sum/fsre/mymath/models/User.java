package ba.sum.fsre.mymath.models;

import com.google.firebase.firestore.PropertyName;

public class User {
    private String firstName;
    private String lastName;
    private String telephone;
    private String dateOfBirth;

    public User() {}

    public User(String firstName, String lastName, String telephone, String dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.telephone = telephone;
        this.dateOfBirth = dateOfBirth;
    }
    @PropertyName("firstName")
    public String getFirstName() {
        return firstName;
    }

    @PropertyName("firstName")
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @PropertyName("lastName")
    public String getLastName() {
        return lastName;
    }
    @PropertyName("lastName")
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @PropertyName("telephone")
    public String getTelephone() {
        return telephone;
    }

    @PropertyName("telephone")
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @PropertyName("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    @PropertyName("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
}