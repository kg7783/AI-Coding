package de.einmaleins.trainer;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {
    
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return SessionListFragment.newInstance();
            case 1:
                return StatisticsFragment.newInstance();
            case 2:
                return TimesTableStatsFragment.newInstance();
            default:
                return SessionListFragment.newInstance();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
