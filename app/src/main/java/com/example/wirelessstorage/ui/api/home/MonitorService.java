package com.example.wirelessstorage.ui.api.home;

import java.time.Instant;
import java.util.function.Consumer;

public interface MonitorService {

    public void watch(ResourceType type, Consumer<ResourceStatus> consumer);

    public void unwatch(Consumer<ResourceStatus> consumer);

    public static enum ResourceType {
        NETWORK,
        DISK,
        MEMORY,
        POWER,
        ALL,
    }

    public interface ResourceStatus {

        public Instant instant();

        public ResourceType type();

    }
}
