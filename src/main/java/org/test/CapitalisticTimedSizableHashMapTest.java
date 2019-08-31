package org.test;

import org.base.TimedSizableMap;
import org.imperialistic.CapitalisticTimedSizableHashMap;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

public class CapitalisticTimedSizableHashMapTest {

    @Test
    public void SingleMapActor_GetSize(){
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();
        assertEquals(0,map.size());
    }

    @Test
    public void Capitalistic_Insert() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        map.put("gil2", 421, 629, TimeUnit.SECONDS);
        map.put("gil3", 4200, 619, TimeUnit.SECONDS);
        assertEquals(3,map.size());
    }

    @Test
    public void Capitalistic_MultiThreadedPutAndGet() throws Exception {
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();

        Executor exec1 = Executors.newSingleThreadExecutor();
        Executor exec2 = Executors.newSingleThreadExecutor();

        exec2.execute(() -> {
            map.put("420", 123, 200, TimeUnit.MILLISECONDS);
        });

        Thread.sleep(2);
        map.get("420").ifPresent(integer -> assertEquals(integer, (Integer)123));

        exec1.execute(() -> map.put("420", 696969, 10, TimeUnit.SECONDS));

        Thread.sleep(50);

        assertEquals(map.size(), 1);
        map.get("420").ifPresent(integer -> assertEquals(integer, (Integer)696969));

        Thread.sleep(200);

        assertEquals(map.size(), 1);
        map.get("420").ifPresent(integer -> assertEquals(integer, (Integer)696969));

        Thread.sleep(15000);

        assertEquals(map.size(), 0);
    }

    @Test
    public void Capitalistic_Remove_Key_Wait_Forever_Die_Respawn_And_Check_That_Item_Has_Been_Automatically_Removed_Test() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();
        map.put("hh", 420, 5, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals(1, map.size());
        Thread.sleep(6000);
        assertEquals(0, map.size());
    }

    @Test
    public void Capitalistic_Remove_Key() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        map.remove("gil");
        assertEquals(0,map.size());
    }

    @Test
    public void Capitalistic_Remove_Empty() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new CapitalisticTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        map.remove("gil");
        assertEquals(0,map.size());
    }

    private static final int MAP_SIZE = 150000;
    private static final int NUM_PARTIES = 4;
    private final CyclicBarrier _bar = new CyclicBarrier(NUM_PARTIES);
    private final TimedSizableMap<Integer, Object> map = new CapitalisticTimedSizableHashMap<>();

    private final Runnable PUT_ACTION = () -> {
        try {
            _bar.await();
            IntStream.range(0, MAP_SIZE).
                    forEach(x ->
                    {
                        map.put(x, new Object(), 5, TimeUnit.MILLISECONDS);
                    });
        } catch (BrokenBarrierException | InterruptedException ex)
        {
            ex.printStackTrace();
        }
    };

    @Test
    public void testConcurrentPut() throws InterruptedException
    {
        final ExecutorService _s = Executors.newCachedThreadPool();
        _bar.reset();

        for(int i = 0; i < NUM_PARTIES; i++)
        {
            _s.submit(PUT_ACTION);
        }

        Thread.sleep(10000);
        assertEquals(0, map.size());
    }
}
