package com.example.wirelessstorage.ui.home;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import androidx.annotation.UiThread;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("DefaultLocale")
public class HomeViewModel extends ViewModel {

    public static final String TAG = HomeViewModel.class.getSimpleName();

    private MutableLiveData<Instant> time;
    private Timer timer;
    private volatile boolean isMonitoring = false;

    public HomeViewModel() {
        time = new MutableLiveData<>();
    }

    @UiThread
    public void network(LifecycleOwner owner, LineChart chart) {
        final TimeDataSet sent;
        sent = new TimeDataSet(60, "Packet Sent: 0KB/S");
        sent.setDrawValues(false);
        sent.setColor(Color.RED);
        sent.setDrawCircles(false);
        sent.setDrawFilled(true);
        sent.setFillColor(Color.RED);
        sent.setValueTextColor(Color.BLACK);
        sent.setMode(LineDataSet.Mode.STEPPED);
        sent.setCubicIntensity(0.2f);

        final TimeDataSet received =
                new TimeDataSet(60, "Packet Received: 0KB/S");
        received.setDrawValues(false);
        received.setColor(Color.BLUE);
        received.setDrawCircles(false);
        received.setDrawFilled(true);
        received.setFillColor(Color.BLUE);
        received.setValueTextColor(Color.BLACK);
        received.setMode(LineDataSet.Mode.STEPPED);

        chart.setData(new LineData(sent, received));
        chart.getDescription().setEnabled(false);

        chart.getAxisLeft().setAxisMinimum(0);
        chart.getAxisRight().setAxisMinimum(0);
        chart.getDescription().setEnabled(false);
        chart.setAutoScaleMinMaxEnabled(true);
        chart.getAxisLeft().setDrawLabels(false);
        chart.getAxisRight().setDrawLabels(false);
        chart.setDrawGridBackground(false);
        chart.getXAxis().setDrawLabels(false);
        chart.invalidate();

        time.observe(owner, new Observer<Instant>() {
            private long preTx = TrafficStats.getTotalTxBytes();
            private long preRx = TrafficStats.getTotalRxBytes();

            @Override
            public void onChanged(Instant instant) {

                long rx = TrafficStats.getTotalRxBytes();
                long tx = TrafficStats.getTotalTxBytes();

                long inSpeed = rx - preRx;
                long outSpeed = tx - preTx;

                preRx = rx;
                preTx = tx;

                sent.add(outSpeed);
                sent.setLabel(String.format("Packet Sent: %fKB/S", outSpeed / 1024.0));

                received.add(inSpeed);
                received.setLabel(String.format("Packet Received: %fKB/S", inSpeed / 1024.0));

                chart.getData().notifyDataChanged();
                chart.notifyDataSetChanged();
                chart.invalidate();
            }
        });

        monitor();
    };

    @UiThread
    public void memory(Context context, LifecycleOwner owner, PieChart pie, LineChart line) {
        TimeDataSet memory = new TimeDataSet(60, "RAM Used: 0GB");
        memory.setDrawValues(false);
        memory.setColor(Color.GREEN);
        memory.setDrawCircles(false);
        memory.setDrawFilled(true);
        memory.setFillColor(Color.GREEN);
        memory.setValueTextColor(Color.BLACK);
        memory.setMode(LineDataSet.Mode.STEPPED);
        memory.setCubicIntensity(0.2f);

        line.setData(new LineData(memory));
        line.getDescription().setEnabled(false);
        line.getAxisLeft().setAxisMinimum(0);
        line.getAxisRight().setAxisMinimum(0);
        line.getAxisLeft().setDrawLabels(false);
        line.getAxisRight().setDrawLabels(false);
        line.getXAxis().setDrawLabels(false);
        line.setDrawGridBackground(false);

        ArrayList<PieEntry> arrayList = new ArrayList<>();
        arrayList.add(new PieEntry(1, "Used"));
        arrayList.add(new PieEntry(0, "Free"));
        PieDataSet dataSet = new PieDataSet(arrayList, "");
        dataSet.setColors(Color.GRAY, Color.CYAN);
        dataSet.setDrawValues(false);
        PieData data = new PieData(dataSet);
        data.setDrawValues(false);

        pie.setCenterText("RAM");
        pie.setDrawEntryLabels(false);
        pie.setData(data);
        pie.getDescription().setEnabled(false);

        line.invalidate();
        pie.invalidate();

        time.observe(owner, new Observer<Instant>() {
            private ActivityManager manager = context.getSystemService(ActivityManager.class);
            private ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();

            @Override
            public void onChanged(Instant instant) {
                manager.getMemoryInfo(memoryInfo);
                memory.add(memoryInfo.totalMem - memoryInfo.availMem);
                memory.setLabel(String.format("RAM Used: %.2fGB / %.2fGB",
                        (memoryInfo.totalMem - memoryInfo.availMem) / 1024.0 / 1024.0 / 1024.0,
                        memoryInfo.totalMem / 1024.0 / 1024.0 / 1024.0
                ));
                line.getAxisLeft().setAxisMaximum(memoryInfo.totalMem);
                memory.notifyDataSetChanged();
                line.getData().notifyDataChanged();
                line.notifyDataSetChanged();
                line.invalidate();

                dataSet.clear();
                dataSet.addEntry(new PieEntry(memoryInfo.totalMem - memoryInfo.availMem, "Used"));
                dataSet.addEntry(new PieEntry(memoryInfo.availMem, "Free"));
                dataSet.notifyDataSetChanged();
                data.notifyDataChanged();
                pie.notifyDataSetChanged();
                pie.invalidate();
            }
        });

        monitor();
    };

    public void disk(LifecycleOwner owner, PieChart pie) {

        ArrayList<PieEntry> arrayList = new ArrayList<>();
        arrayList.add(new PieEntry(1, "Used"));
        arrayList.add(new PieEntry(0, "Free"));
        final PieDataSet dataSet = new PieDataSet(arrayList, "");

        final PieData data = new PieData(dataSet);
        dataSet.setColors(Color.GRAY, Color.YELLOW);
        data.setDrawValues(false);

        pie.setCenterText("Disk");
        pie.setData(data);
        pie.setDrawEntryLabels(false);
        pie.getDescription().setEnabled(false);
        pie.invalidate();


        time.observe(owner, instant -> {
            StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
            long total = statFs.getTotalBytes();
            long free = statFs.getFreeBytes();

            dataSet.clear();
            dataSet.addEntry(new PieEntry(total - free, "Used"));
            dataSet.addEntry(new PieEntry(free, "Free"));
            dataSet.notifyDataSetChanged();
            data.notifyDataChanged();
            pie.notifyDataSetChanged();
            pie.invalidate();
        });

        monitor();
    }

    private void monitor() {
        if(isMonitoring) {
            return;
        }
        if(timer == null) {
            timer = new Timer();
        }

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                time.postValue(Instant.now());
            }
        }, 0, 1000);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(timer != null) {
            timer.cancel();
            timer = null;
        }

    }
}