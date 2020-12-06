package com.example.wirelessstorage.ui.device;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceViewModel extends ViewModel {


    private MutableLiveData<List<Map<String, String>>> devices;

    public MutableLiveData<List<Map<String, String>>> devices() {
        if(devices == null) {
            devices = new MutableLiveData<>();
        }


        List<Map<String, String>> devices = new ArrayList<>();

        Map<String, String> raspberry = new HashMap<>();
        raspberry.put("name", "Raspberry Pi");
        raspberry.put("description", "Disconnected");
        devices.add(raspberry);

        this.devices.postValue(devices);

        return this.devices;
    }

}