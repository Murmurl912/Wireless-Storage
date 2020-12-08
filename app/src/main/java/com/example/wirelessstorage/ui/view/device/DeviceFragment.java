package com.example.wirelessstorage.ui.view.device;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import androidx.recyclerview.widget.RecyclerView;
import com.example.wirelessstorage.R;

public class DeviceFragment extends Fragment {

    private DeviceViewModel model;
    private RecyclerView deviceContainer;
    private DeviceAdapter adapter;

    public static DeviceFragment newInstance() {
        return new DeviceFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.device, menu);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        deviceContainer = root.findViewById(R.id.device_container);
        deviceContainer.setAdapter((adapter = new DeviceAdapter()));

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = new ViewModelProvider(this).get(DeviceViewModel.class);

        model.devices().observe(getViewLifecycleOwner(), devices -> {
            adapter.setDevices(devices);
            adapter.notifyDataSetChanged();
        });
    }
}