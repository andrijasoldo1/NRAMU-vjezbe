package ba.sum.fsre.mymath.models;

import com.google.firebase.firestore.PropertyName;

public class User {

    private String firstName;
    private String lastName;
    private String eMail;
    private String telephone;
    private String gender;
    private String address;
    private String dateOfBirth;
    private String placeOfBirth;
    private String university;
    private int yearOfStartingUniversity;
    private int yearOfFinishingUniversity;
    private String areaOfExpertise;
    private boolean isApproved; // default to false
    private String role;
    private String CV;
    private String picture;

    // Default constructor (required for Firebase)
    public User() {
        this.isApproved = false; // default value
    }

    // Parameterized constructor (you can customize this)
    public User(String firstName, String lastName, String eMail, String telephone, String gender,
                String address, String dateOfBirth, String placeOfBirth, String university,
                int yearOfStartingUniversity, int yearOfFinishingUniversity, String areaOfExpertise,
                boolean isApproved, String role, String CV, String picture) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.eMail = eMail;
        this.telephone = telephone;
        this.gender = gender;
        this.address = address;
        this.dateOfBirth = dateOfBirth;
        this.placeOfBirth = placeOfBirth;
        this.university = university;
        this.yearOfStartingUniversity = yearOfStartingUniversity;
        this.yearOfFinishingUniversity = yearOfFinishingUniversity;
        this.areaOfExpertise = areaOfExpertise;
        this.isApproved = isApproved;
        this.role = role;
        this.CV = CV;
        this.picture = picture;
    }

    // Getters and setters for all fields

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

    @PropertyName("eMail")
    public String geteMail() {
        return eMail;
    }

    @PropertyName("eMail")
    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    @PropertyName("telephone")
    public String getTelephone() {
        return telephone;
    }

    @PropertyName("telephone")
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    @PropertyName("gender")
    public String getGender() {
        return gender;
    }

    @PropertyName("gender")
    public void setGender(String gender) {
        this.gender = gender;
    }

    @PropertyName("address")
    public String getAddress() {
        return address;
    }

    @PropertyName("address")
    public void setAddress(String address) {
        this.address = address;
    }

    @PropertyName("dateOfBirth")
    public String getDateOfBirth() {
        return dateOfBirth;
    }

    @PropertyName("dateOfBirth")
    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    @PropertyName("placeOfBirth")
    public String getPlaceOfBirth() {
        return placeOfBirth;
    }

    @PropertyName("placeOfBirth")
    public void setPlaceOfBirth(String placeOfBirth) {
        this.placeOfBirth = placeOfBirth;
    }

    @PropertyName("university")
    public String getUniversity() {
        return university;
    }

    @PropertyName("university")
    public void setUniversity(String university) {
        this.university = university;
    }

    @PropertyName("yearOfStartingUniversity")
    public int getYearOfStartingUniversity() {
        return yearOfStartingUniversity;
    }

    @PropertyName("yearOfStartingUniversity")
    public void setYearOfStartingUniversity(int yearOfStartingUniversity) {
        this.yearOfStartingUniversity = yearOfStartingUniversity;
    }

    @PropertyName("yearOfFinishingUniversity")
    public int getYearOfFinishingUniversity() {
        return yearOfFinishingUniversity;
    }

    @PropertyName("yearOfFinishingUniversity")
    public void setYearOfFinishingUniversity(int yearOfFinishingUniversity) {
        this.yearOfFinishingUniversity = yearOfFinishingUniversity;
    }

    @PropertyName("areaOfExpertise")
    public String getAreaOfExpertise() {
        return areaOfExpertise;
    }

    @PropertyName("areaOfExpertise")
    public void setAreaOfExpertise(String areaOfExpertise) {
        this.areaOfExpertise = areaOfExpertise;
    }

    @PropertyName("isApproved")
    public boolean isApproved() {
        return isApproved;
    }

    @PropertyName("isApproved")
    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    @PropertyName("role")
    public String getRole() {
        return role;
    }

    @PropertyName("role")
    public void setRole(String role) {
        this.role = role;
    }

    @PropertyName("CV")
    public String getCV() {
        return CV;
    }

    @PropertyName("CV")
    public void setCV(String CV) {
        this.CV = CV;
    }

    @PropertyName("picture")
    public String getPicture() {
        return picture;
    }

    @PropertyName("picture")
    public void setPicture(String picture) {
        this.picture = picture;
    }
}
