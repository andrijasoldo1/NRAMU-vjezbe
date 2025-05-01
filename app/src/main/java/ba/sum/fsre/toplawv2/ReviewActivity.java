package ba.sum.fsre.toplawv2;

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

import ba.sum.fsre.toplawv2.adapters.ReviewAdapter;
import ba.sum.fsre.toplawv2.models.Review;
import ba.sum.fsre.toplawv2.models.User;

public class ReviewActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<User> lawyerList;
    private Map<String, List<Review>> reviewsMap;
    private Map<String, Integer> acceptedCasesMap;
    private Map<String, Integer> resolvedCasesMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        recyclerView = findViewById(R.id.recyclerViewLawyerReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lawyerList = new ArrayList<>();
        reviewsMap = new HashMap<>();
        acceptedCasesMap = new HashMap<>();
        resolvedCasesMap = new HashMap<>();

        db = FirebaseFirestore.getInstance();

        loadLawyers();
    }

    private void loadLawyers() {
        db.collection("users")
                .whereEqualTo("isApproved", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    lawyerList.clear();
                    int total = querySnapshot.size();
                    if (total == 0) {
                        loadReviews(); // fallback
                        return;
                    }

                    final int[] loadedCount = {0};

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User lawyer = doc.toObject(User.class);
                        String userId = doc.getId();
                        String email = lawyer.geteMail();

                        acceptedCasesMap.put(email, 0);
                        resolvedCasesMap.put(email, 0);

                        db.collection("users").document(userId).collection("accepted_cases").get()
                                .addOnSuccessListener(acceptedSnap -> {
                                    acceptedCasesMap.put(email, acceptedSnap.size());

                                    db.collection("users").document(userId).collection("resolved_cases").get()
                                            .addOnSuccessListener(resolvedSnap -> {
                                                resolvedCasesMap.put(email, resolvedSnap.size());

                                                lawyerList.add(lawyer);

                                                loadedCount[0]++;
                                                if (loadedCount[0] == total) {
                                                    adapter = new ReviewAdapter(this, lawyerList, reviewsMap, acceptedCasesMap, resolvedCasesMap);
                                                    recyclerView.setAdapter(adapter);
                                                    loadReviews();
                                                }
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(this, "Failed to load resolved cases: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Failed to load accepted cases: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
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

                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
