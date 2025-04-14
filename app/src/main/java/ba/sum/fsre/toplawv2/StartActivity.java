package ba.sum.fsre.toplawv2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        // Fetch buttons by their IDs
        Button nextLawyer = findViewById(R.id.nextLawyer);
        Button nextUser = findViewById(R.id.nextUser);

        // Set click listeners to navigate to DetailsActivity
        View.OnClickListener navigateToDetails = v -> {
            Intent intent = new Intent(StartActivity.this, DetailsActivity.class);
            startActivity(intent);
        };

        nextLawyer.setOnClickListener(navigateToDetails);
        nextUser.setOnClickListener(navigateToDetails);
    }
}
