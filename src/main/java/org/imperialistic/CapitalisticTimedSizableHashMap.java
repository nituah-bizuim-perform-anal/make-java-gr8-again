package org.imperialistic;

import org.base.TimedSizableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CapitalisticTimedSizableHashMap<K, V> implements TimedSizableMap<K, V> {
    private final ScheduledExecutorService removalService = Executors.newSingleThreadScheduledExecutor();
    private final ConcurrentHashMap<K, V> data;

    public CapitalisticTimedSizableHashMap() {
        this(new ConcurrentHashMap<>());
    }

    protected CapitalisticTimedSizableHashMap(Map<K, V> map) {
        this.data = new ConcurrentHashMap<>(map);
    }

    @Override
    public long size() {
        return this.data.size();
    }

    @Override
    public void put(K key, V value, int duration, TimeUnit unit) {
        this.data.put(key, value);

        this.removalService.schedule(() -> this.data.compute(key, (k, v) -> v.equals(value) ? null: v), duration, unit);
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(this.data.get(key));
    }

    @Override
    public Optional<V> remove(K key) {
        return Optional.ofNullable(this.data.remove(key));
    }


    private static final class StampedObject<T> {
        final T obj;
        final long stamp;

        private StampedObject(T obj, long stamp) {this.obj = obj; this.stamp = stamp;}

        public static <R> StampedObject<R>
        of(R obj, long stamp) {return new StampedObject<>(obj, stamp);}
    }
}
