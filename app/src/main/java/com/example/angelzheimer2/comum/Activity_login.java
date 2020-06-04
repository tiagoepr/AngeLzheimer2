package com.example.angelzheimer2.comum;

import android.os.Bundle;

import com.example.angelzheimer2.R;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class Activity_login extends AppCompatActivity {

    private FragmentManager fragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ViewPager viewPager = findViewById(R.id.ViewPager);

        fragmentManager = getSupportFragmentManager();
        AuthenticationPagerAdapter pagerAdapter = new AuthenticationPagerAdapter(fragmentManager);
        pagerAdapter.addFragmet(new fragment_login());
        pagerAdapter.addFragmet(new fragment_register());
        viewPager.setAdapter(pagerAdapter);

    }


    class AuthenticationPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        public AuthenticationPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragmentList.get(i);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        void addFragmet(Fragment fragment) {
            fragmentList.add(fragment);
        }

        void removeFragmet(Fragment fragment) {
            int l = fragmentList.size();
            fragmentList.remove(l - 1);
            // Remove o último fragmento da lista
        }

    }

}
