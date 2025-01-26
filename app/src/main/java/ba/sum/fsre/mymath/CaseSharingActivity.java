package ba.sum.fsre.mymath;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ba.sum.fsre.mymath.adapters.CaseSharingPagerAdapter;

public class CaseSharingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_sharing);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.viewPager);

        // Initialize CaseSharingPagerAdapter
        CaseSharingPagerAdapter adapter = new CaseSharingPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Attach TabLayout and ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(position == 0 ? "Sent Cases" : "Received Cases");
        }).attach();
    }
}
