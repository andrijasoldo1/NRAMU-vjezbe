package ba.sum.fsre.mymath;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import ba.sum.fsre.mymath.fragments.AllCasesFragment;
import ba.sum.fsre.mymath.fragments.UserCasesFragment;

public class CasesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cases);

        TabLayout tabLayout = findViewById(R.id.tabLayout);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, new UserCasesFragment())
                .commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new UserCasesFragment())
                                .commit();
                        break;
                    case 1:
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragmentContainer, new AllCasesFragment())
                                .commit();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
}
