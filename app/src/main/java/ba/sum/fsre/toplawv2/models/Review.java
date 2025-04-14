package ba.sum.fsre.toplawv2.models;

import com.google.firebase.firestore.PropertyName;

public class Review {
    private String lawyerEmail;
    private String reviewerEmail;
    private float rating;
    private String reviewText;

    public Review() {
        // Default constructor required for Firebase
    }

    public Review(String lawyerEmail, String reviewerEmail, float rating, String reviewText) {
        this.lawyerEmail = lawyerEmail;
        this.reviewerEmail = reviewerEmail;
        this.rating = rating;
        this.reviewText = reviewText;
    }

    @PropertyName("lawyerEmail")
    public String getLawyerEmail() {
        return lawyerEmail;
    }

    @PropertyName("lawyerEmail")
    public void setLawyerEmail(String lawyerEmail) {
        this.lawyerEmail = lawyerEmail;
    }

    @PropertyName("reviewerEmail")
    public String getReviewerEmail() {
        return reviewerEmail;
    }

    @PropertyName("reviewerEmail")
    public void setReviewerEmail(String reviewerEmail) {
        this.reviewerEmail = reviewerEmail;
    }

    @PropertyName("rating")
    public float getRating() {
        return rating;
    }

    @PropertyName("rating")
    public void setRating(float rating) {
        this.rating = rating;
    }

    @PropertyName("reviewText")
    public String getReviewText() {
        return reviewText;
    }

    @PropertyName("reviewText")
    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }
}
