package ba.sum.fsre.toplaw.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ba.sum.fsre.toplaw.fragments.ReceivedCasesFragment;
import ba.sum.fsre.toplaw.fragments.SentCasesFragment;

public class CaseSharingPagerAdapter extends FragmentStateAdapter {

    public CaseSharingPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the appropriate fragment for the tab
        return position == 0 ? new SentCasesFragment() : new ReceivedCasesFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // Total number of tabs
    }
}
