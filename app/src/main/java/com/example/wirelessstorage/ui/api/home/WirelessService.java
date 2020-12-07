package com.example.wirelessstorage.ui.api.home;

import java.util.List;
import java.util.function.Consumer;

public interface WirelessService {

    /**
     * Start wireless service.
     */
    public void start();

    /**
     * Stop wireless service.
     */
    public void stop();

    /**
     * watch service event.
     * @param consumer callback to be called when event happens.
     */
    public void watch(Consumer<WirelessServiceEvent> consumer);

    /**
     * unregister a event watcher.
     * @param consumer the event consumer to be removed.
     */
    public void unwatch(Consumer<WirelessServiceEvent> consumer);

    /**
     * obtain current connected devices
     * @param consumer accept results.
     */
    public void devices(Consumer<List<WirelessDevice>> consumer);

    /**
     * obtain discovered devices.
     * @param consumer accept results.
     */
    public void peers(Consumer<List<WirelessDevice>> consumer);

    /**
     * connect to a device
     * @param device which device to connect with
     */
    public void connect(WirelessDevice device);

    /**
     * remove a connected device
     * @param device which device to remove
     */
    public void remove(WirelessDevice device);

    /**
     * get current state of wireless service
     * @return state
     */
    public WirelessServiceState state();

    public static interface WirelessServiceEvent {

    }

    public static interface WirelessDevice {

    }

    public static interface WirelessServiceState {

    }

    public static enum State {
        STATE_INACTIVE, // wifi service is not start

        STATE_ACTIVATING, // wifi service is starting
        STATE_DEACTIVATING, // wifi service is stopping

        STATE_ACTIVE_RUNNING, // wifi service is running normally
        STATE_ACTIVE_ERROR, // wifi service is paused due to error
        STATE_ACTIVE_BUSY, // wifi service is paused due to busy

    }
}
