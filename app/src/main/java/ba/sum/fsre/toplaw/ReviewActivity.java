package ba.sum.fsre.toplaw;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.toplaw.adapters.ReviewAdapter;
import ba.sum.fsre.toplaw.models.Review;
import ba.sum.fsre.toplaw.models.User;

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<User> lawyerList;
    private Map<String, List<Review>> reviewsMap; // Map of lawyerEmail to their reviews
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        recyclerView = findViewById(R.id.recyclerViewLawyerReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lawyerList = new ArrayList<>();
        reviewsMap = new HashMap<>();
        adapter = new ReviewAdapter(this, lawyerList, reviewsMap);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        loadLawyers();
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("isApproved", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lawyerList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User lawyer = doc.toObject(User.class);
                        lawyerList.add(lawyer);
                    }
                    loadReviews(); // Load reviews after fetching lawyers
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load lawyers: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void loadReviews() {
        db.collection("reviews")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reviewsMap.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Review review = doc.toObject(Review.class);
                        String lawyerEmail = review.getLawyerEmail();

                        if (!reviewsMap.containsKey(lawyerEmail)) {
                            reviewsMap.put(lawyerEmail, new ArrayList<>());
                        }
                        reviewsMap.get(lawyerEmail).add(review);
                    }
                    adapter.notifyDataSetChanged(); // Refresh the adapter after loading reviews
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}