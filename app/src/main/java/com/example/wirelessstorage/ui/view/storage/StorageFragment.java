package com.example.wirelessstorage.ui.view.storage;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.wirelessstorage.R;
import com.example.wirelessstorage.ui.view.tab.TabView;

public class StorageFragment extends Fragment {

    private StorageViewModel model;
    private TabView tabView;

    public static StorageFragment newInstance() {
        return new StorageFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_storage, container, false);
        tabView = root.findViewById(R.id.tab_layout);
        tabView.setup(this);
        root.postDelayed(()->{
            model = new ViewModelProvider(this).get(StorageViewModel.class);
        }, 500);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.storage, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}