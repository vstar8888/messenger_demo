package ru.demo.messenger.main;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import ru.demo.messenger.chats.list.ChatsFragment;
import ru.demo.messenger.profile.ProfileFragment;
import ru.demo.messenger.people.PeopleFragment;

class MainPagerAdapter extends FragmentPagerAdapter {
    private Fragment[] fragments;

    MainPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
        fragments = new Fragment[]{
                ChatsFragment.newInstance(),
                PeopleFragment.newInstance(),
                ProfileFragment.newInstance(),
        };
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return null;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

}