package com.example.wirelessstorage.ui.home;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.*;

import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.wirelessstorage.R;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class HomeFragment extends Fragment {

    private HomeViewModel model;

    private TextView time;
    private ImageView statusIcon;
    private TextView statusLabel;
    private TextView networkName;
    private TextView networkPin;
    private TextView deviceCount;

    private volatile int state;
    private ImageButton play;

    private LineChart networkHistory;
    private LineChart memoryHistory;
    private PieChart diskUsage;
    private PieChart memoryUsage;

    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @SuppressLint("DefaultLocale")
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) {
                return;
            }

            switch (action) {
                case WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION:
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                    break;
                case WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION:
                    WifiP2pDeviceList list = intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
                    int size = 0;
                    if(list != null) {
                        size = list.getDeviceList().size();
                    }
                    deviceCount.setText(String.format("%d device(s) connected", size));
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        networkHistory = root.findViewById(R.id.home_network_history_chart);
        memoryHistory = root.findViewById(R.id.home_memory_history_chart);
        diskUsage = root.findViewById(R.id.home_disk_usage);
        memoryUsage = root.findViewById(R.id.home_memory_usage);

        networkName = root.findViewById(R.id.network_name);
        networkPin = root.findViewById(R.id.pin_label);

        statusIcon = root.findViewById(R.id.status_icon);
        statusLabel = root.findViewById(R.id.status_label);

        deviceCount = root.findViewById(R.id.device_label);

        play = root.findViewById(R.id.main_action);

        play.setOnClickListener(v -> {
            play.setEnabled(false);
            switch (state) {
                case 0:
                    start();
                    break;
                case -1:
                case 1:
                    if(channel != null) {
                        channel.close();
                    }
                    break;
            }
        });

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        model = new ViewModelProvider(this).get(HomeViewModel.class);

        model.network(getViewLifecycleOwner(), networkHistory);
        model.memory(requireContext(), getViewLifecycleOwner(), memoryUsage, memoryHistory);
        model.disk(getViewLifecycleOwner(), diskUsage);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onPause();
        if(channel != null) {
            channel.close();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    public void start() {
        state = 0;
        play.setImageResource(R.drawable.ic_play_dark);

        statusLabel.setText(" Stopped (Inactive)");
        statusLabel.setTextColor(Color.GRAY);
        statusIcon.setImageResource(R.drawable.ic_status_gray);

        WifiP2pConfig wifiP2pConfig = new WifiP2pConfig();
        wifiP2pConfig.wps.setup = WpsInfo.DISPLAY;
        wifiP2pConfig.wps.pin = "123456";
        wifiP2pConfig.wps.BSSID = "MUR_DEBUG";

        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);

        requireContext().registerReceiver(receiver, filter);
        manager = requireContext().getSystemService(WifiP2pManager.class);
        channel = manager.initialize(requireContext(), Looper.getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                requireContext().unregisterReceiver(receiver);
                state = 0;
                statusLabel.setText(" Stopped (Inactive)");
                statusLabel.setTextColor(Color.GRAY);
                statusIcon.setImageResource(R.drawable.ic_status_gray);
                channel = null;
                play.setEnabled(true);
                play.setImageResource(R.drawable.ic_play_dark);

                networkName.setText("SSID");
                networkPin.setText("PIN");
                deviceCount.setText("0 device(s) connected");
            }
        });


        WifiP2pConfig config = new WifiP2pConfig.Builder()
                .enablePersistentMode(true)
                .setNetworkName("DIRECT-MUR-DEBUG")
                .setPassphrase("12345678")
                .build();
        config.wps.setup = WpsInfo.DISPLAY;
        config.wps.pin = "123456";

        manager.requestGroupInfo(channel, group -> {
            if(group == null) {
                manager.createGroup(channel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        state = 1;
                        statusLabel.setText(" Running (Active)");
                        statusLabel.setTextColor(Color.GREEN);
                        statusIcon.setImageResource(R.drawable.ic_status_green);

                        new Handler(Looper.getMainLooper())
                                .postDelayed(()->{
                                    manager.requestGroupInfo(channel, new WifiP2pManager.GroupInfoListener() {
                                        @Override
                                        public void onGroupInfoAvailable(WifiP2pGroup group) {
                                            if(group == null) {
                                                return;
                                            }

                                            int size = group.getClientList().size();
                                            deviceCount.setText(size + " devices connected");

                                            networkName.setText(group.getNetworkName());
                                            networkPin.setText(group.getPassphrase());
                                        }
                                    });
                                }, 1000);

                        play.setImageResource(R.drawable.ic_stop_dark);
                        play.setEnabled(true);

                    }

                    @Override
                    public void onFailure(int reason) {
                        state = -1;
                        statusLabel.setText(" Error (Active): " + reason);
                        statusLabel.setTextColor(Color.YELLOW);
                        statusIcon.setImageResource(R.drawable.ic_status_yellow);

                        deviceCount.setText("0 devices connected");
                        networkName.setText("SSID");
                        networkPin.setText("PIN");

                        networkName.setText("");
                        networkPin.setText("");

                        play.setImageResource(R.drawable.ic_stop_dark);
                        play.setEnabled(true);
                    }
                });
            } else {
                state = 1;
                statusLabel.setText(" Running (Active)");
                statusLabel.setTextColor(Color.GREEN);
                statusIcon.setImageResource(R.drawable.ic_status_green);

                networkName.setText(group.getNetworkName());
                networkPin.setText(group.getPassphrase());

                int size = group.getClientList().size();
                deviceCount.setText(size + " devices connected");

                play.setImageResource(R.drawable.ic_stop_dark);
                play.setEnabled(true);
            }
        });
    }

}