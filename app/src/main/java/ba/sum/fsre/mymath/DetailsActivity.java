package ba.sum.fsre.mymath;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;

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

        // Disable the default ActionBar provided by the theme
        supportRequestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        setContentView(R.layout.details_activity);

        // Set up the custom Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);

        // Configure the Drawer Toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.open_drawer, R.string.close_drawer);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle navigation item clicks
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
                startActivity(new Intent(this, ReviewActivity.class)); // Navigate to ReviewActivity
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            drawerLayout.closeDrawers();
            return true;
        });

        // Set default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DetailsFragment())
                .commit();
    }
}
