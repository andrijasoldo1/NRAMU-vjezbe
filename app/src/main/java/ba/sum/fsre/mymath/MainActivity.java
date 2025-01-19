package ba.sum.fsre.mymath;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;

import ba.sum.fsre.mymath.R;
import ba.sum.fsre.mymath.fragments.LoginFragment;
import ba.sum.fsre.mymath.fragments.RegisterFragment;

public class MainActivity extends AppCompatActivity {
    private MaterialButton toggleLeft, toggleRight;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        // Initialize views
        toggleLeft = findViewById(R.id.toggleLeft);
        toggleRight = findViewById(R.id.toggleRight);

        // Initialize Firebase Cloud Messaging
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyAppChannel", "Main Channel", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Log.d("FCM", "FCM Token: " + task.getResult());
            }
        });

        // Set initial state
        setInitialState();

        // Toggle button listeners
        toggleLeft.setOnClickListener(v -> switchToRegisterFragment());
        toggleRight.setOnClickListener(v -> switchToLoginFragment());
    }

    private void setInitialState() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
        toggleLeft.setSelected(true);
        toggleRight.setSelected(false);
    }

    private void switchToLoginFragment() {
        toggleLeft.setSelected(true);
        toggleRight.setSelected(false);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right)
                .replace(R.id.fragmentContainer, new LoginFragment())
                .commit();
    }

    private void switchToRegisterFragment() {
        toggleLeft.setSelected(false);
        toggleRight.setSelected(true);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragmentContainer, new RegisterFragment())
                .commit();
    }
}
