package ba.sum.fsre.mymath;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

import ba.sum.fsre.mymath.fragments.AllCasesFragment;
import ba.sum.fsre.mymath.fragments.UserCasesFragment;
import ba.sum.fsre.mymath.fragments.DetailsFragment;
import ba.sum.fsre.mymath.fragments.ListViewFragment;
import ba.sum.fsre.mymath.fragments.MessagesFragment;

public class DetailsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        // Toolbar setup
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuButton = findViewById(R.id.menuButton);

        // Handle button clicks to toggle drawer
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(androidx.core.view.GravityCompat.END)) {
                drawerLayout.closeDrawer(androidx.core.view.GravityCompat.END);
            } else {
                drawerLayout.openDrawer(androidx.core.view.GravityCompat.END);
            }
        });

        // Handle NavigationView item selection
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
                startActivity(new Intent(this, MapsActivity.class));
            } else if (item.getItemId() == R.id.nav_lawyer_reviews) {
                startActivity(new Intent(this, ReviewActivity.class));
            } else if (item.getItemId() == R.id.nav_chatbot) {
                // Launch ChatbotActivity
                startActivity(new Intent(this, ChatbotActivity.class));
            } else if (item.getItemId() == R.id.nav_case_sharing) {
                // Launch CaseSharingActivity
                startActivity(new Intent(this, CaseSharingActivity.class));
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawer(androidx.core.view.GravityCompat.END);
            return true;
        });

        // Default fragment on activity start
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DetailsFragment())
                .commit();
    }
}
