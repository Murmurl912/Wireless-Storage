package com.example.wirelessstorage.ui.view.tab;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.example.wirelessstorage.ui.api.storage.Storage;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Objects;
import java.util.Stack;

public class TabView extends LinearLayout {

    private final AppBarLayout appBarLayout = new AppBarLayout(getContext());
    private final TabLayout tabLayout = new TabLayout(getContext());
    private final ViewPager2 viewPager2 = new ViewPager2(getContext());
    private TabAdapter tabAdapter;
    private Stack<Fragment> fragments;
    private Stack<Bundle> bundles;

    public TabView(Context context) {
        super(context);
        init();
    }

    public TabView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TabView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setOrientation(LinearLayout.VERTICAL);
        appBarLayout.addView(tabLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(appBarLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(viewPager2, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    public void setup(Fragment fragment) {
        tabAdapter = new TabAdapter(fragment);
        viewPager2.setAdapter(tabAdapter);
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            Bundle bundle = bundles.get(position);
            tab.setText(bundle.getString("text"));
        }).attach();
    }

    public void setup(FragmentActivity activity) {
        tabAdapter = new TabAdapter(activity);
    }

    public void setup(FragmentManager manager, Lifecycle lifecycle) {
        tabAdapter = new TabAdapter(manager, lifecycle);
    }

    public void push(Fragment fragment, Bundle bundle) {
        fragments.push(fragment);
        bundles.push(bundle);
        tabAdapter.notifyItemInserted(fragments.size() - 1);
    }

    public void pop() {
        fragments.pop();
        bundles.pop();
        tabAdapter.notifyItemRemoved(fragments.size());
    }

    public void pop(Fragment fragment) {
        int size = 0;
        while (!Objects.equals(fragment, fragments.peek())) {
            fragments.pop();
            bundles.pop();
            size++;
        }
        tabAdapter.notifyItemRangeRemoved(fragments.size(), size);
    }

    private class TabAdapter extends FragmentStateAdapter {

        public TabAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        public TabAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        public TabAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return fragments.get(position);
        }

        @Override
        public int getItemCount() {
            return fragments.size();
        }
    }
}
