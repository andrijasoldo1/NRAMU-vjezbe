package ba.sum.fsre.mymath;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ba.sum.fsre.mymath.adapters.ReviewAdapter;
import ba.sum.fsre.mymath.fragments.AllCasesFragment;
import ba.sum.fsre.mymath.fragments.ListViewFragment;
import ba.sum.fsre.mymath.fragments.MessagesFragment;
import ba.sum.fsre.mymath.fragments.UserCasesFragment;
import ba.sum.fsre.mymath.models.Review;
import ba.sum.fsre.mymath.models.User;
import ba.sum.fsre.mymath.fragments.DetailsFragment;


public class ReviewActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private ReviewAdapter adapter;
    private List<User> lawyerList;
    private Map<String, List<Review>> reviewsMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable the default ActionBar
        supportRequestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_review);

        // Set up Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Configure the Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle NavigationView item clicks
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.nav_profil) {
                selectedFragment = new DetailsFragment();
            } else if (item.getItemId() == R.id.nav_lekcije) {
                selectedFragment = new ListViewFragment();
            } else if (item.getItemId() == R.id.nav_game1) {
                startActivity(new Intent(this, Game1Activity.class));
            } else if (item.getItemId() == R.id.nav_cases) {
                selectedFragment = new AllCasesFragment();
            } else if (item.getItemId() == R.id.nav_user_cases) {
                selectedFragment = new UserCasesFragment();
            } else if (item.getItemId() == R.id.nav_messages) {
                selectedFragment = new MessagesFragment();
            } else if (item.getItemId() == R.id.nav_map) {
                startActivity(new Intent(this, MapsActivity.class)); // Navigate to MapsActivity
            } else if (item.getItemId() == R.id.nav_lawyer_reviews) {
                // Reload the current activity
                startActivity(new Intent(this, ReviewActivity.class));
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerViewLawyerReviews);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        lawyerList = new ArrayList<>();
        reviewsMap = new HashMap<>();
        adapter = new ReviewAdapter(this, lawyerList, reviewsMap);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        // Load lawyers and reviews
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
