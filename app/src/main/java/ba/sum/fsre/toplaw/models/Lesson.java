package ba.sum.fsre.toplaw.models;

import com.google.firebase.firestore.PropertyName;

public class Lesson {

    private String title;
    private String imageUrl;

    public Lesson() {}

    public Lesson(String title, String imageUrl) {
        this.title = title;
        this.imageUrl = imageUrl;
    }

    @PropertyName("title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @PropertyName("imageUrl")
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}