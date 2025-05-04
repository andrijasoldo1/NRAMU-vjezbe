package ba.sum.fsre.toplawv2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.material.navigation.NavigationView;

import ba.sum.fsre.toplawv2.fragments.AllCasesFragment;
import ba.sum.fsre.toplawv2.fragments.LawyersFragment;
import ba.sum.fsre.toplawv2.fragments.ProfileFragment;
import ba.sum.fsre.toplawv2.fragments.UserCasesFragment;
import ba.sum.fsre.toplawv2.fragments.DetailsFragment;
import ba.sum.fsre.toplawv2.fragments.MessagesFragment;

public class DetailsActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_activity);

        // Setup drawer layout and menu button
        drawerLayout = findViewById(R.id.drawer_layout);
        ImageButton menuButton = findViewById(R.id.menuButton);

        // Toggle drawer on menu button click
        menuButton.setOnClickListener(v -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END);
            } else {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        // Setup NavigationView item selection
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            // Get the current fragment in the container
            Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

            if (itemId == R.id.nav_profil) {
                // If already displaying the profile (DetailsFragment), do nothing.
                if (currentFragment instanceof DetailsFragment) {
                    drawerLayout.closeDrawer(GravityCompat.END);
                    return true;
                }
                // Otherwise, create a new DetailsFragment.
                selectedFragment = new ProfileFragment();
                // If your intention was to launch a different activity for profile (e.g. Game1Activity),
                // you can remove the fragment transaction and use the following line instead:
                // startActivity(new Intent(this, Game1Activity.class));
            } else if (itemId == R.id.nav_cases) {
                if (!(currentFragment instanceof AllCasesFragment)) {
                    selectedFragment = new AllCasesFragment();
                }
            } else if (itemId == R.id.nav_user_cases) {
                if (!(currentFragment instanceof UserCasesFragment)) {
                    selectedFragment = new UserCasesFragment();
                }
            } else if (itemId == R.id.nav_messages) {
                if (!(currentFragment instanceof MessagesFragment)) {
                    selectedFragment = new MessagesFragment();
                }
            } else if (itemId == R.id.nav_map) {
                startActivity(new Intent(this, MapsActivity.class));
            } else if (itemId == R.id.nav_lawyer_reviews) {
                startActivity(new Intent(this, ReviewActivity.class));
            } else if (itemId == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChatbotActivity.class));
            } else if (itemId == R.id.nav_case_sharing) {
                startActivity(new Intent(this, CaseSharingActivity.class));
            }
            else if (itemId == R.id.nav_calendar) {
                startActivity(new Intent(this, CalendarActivity.class));
            } else if (itemId == R.id.nav_lawyers) {
                if (!(currentFragment instanceof LawyersFragment)) {
                    selectedFragment = new LawyersFragment();
                }
            }


            // If a new fragment was chosen, replace the fragment container.
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            // Close the navigation drawer.
            drawerLayout.closeDrawer(GravityCompat.END);
            return true;
        });

        // Set the default fragment on activity start.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DetailsFragment())
                .commit();
    }
}
