package ba.sum.fsre.mymath;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

import ba.sum.fsre.mymath.fragments.LoginFragment;
import ba.sum.fsre.mymath.fragments.RegisterFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new LoginFragment()).commit();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("MyMath", "Channel human readable title", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                String token = task.getResult();
                Log.d("FCM", "FCM registration token: " + token);
            } else {
                Log.e("FCM", "Failed to get FCM token", task.getException());
            }
        });

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        tabLayout.addTab(tabLayout.newTab().setText("Prijava"));
        tabLayout.addTab(tabLayout.newTab().setText("Registracija"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Fragment selectedFragment = null;

                switch (tab.getPosition()){
                    case 0:
                        selectedFragment = new LoginFragment();
                        break;
                    case 1:
                        selectedFragment = new RegisterFragment();
                        break;
                }
                assert selectedFragment != null;
                getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, selectedFragment).commit();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}