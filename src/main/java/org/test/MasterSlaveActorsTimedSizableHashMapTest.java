package org.test;

import org.base.TimedSizableMap;
import org.concurrent_actors.MasterSlaveActorsTimedSizableHashMap;
import org.junit.Test;
import org.single_actor.SingleActorTimedSizableHashMap;

import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

public class MasterSlaveActorsTimedSizableHashMapTest {
    @Test
    public void MasterSlaveActorsMap_GetXD() throws Exception {
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        map.put("gil", 420, 500, TimeUnit.SECONDS);
        map.put("gil2", 421, 500, TimeUnit.SECONDS);
        map.put("gil3", 4200, 500, TimeUnit.SECONDS);
        Thread.sleep(200);

        int a = map.get("gil").get();
        int b = map.get("gil2").get();
        int c = map.get("gil3").get();
        assertEquals(a, 420);
        assertEquals(b, 421);
        assertEquals(c, 4200);
    }

    @Test
    public void MasterSlaveActorsMap_GetSize(){
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        assertEquals(0,map.size());
    }

    @Test
    public void MasterSlaveActorsMap_Insert() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        map.put("gil", 420, 500, TimeUnit.SECONDS);
        map.put("gil2", 421, 500, TimeUnit.SECONDS);
        map.put("gil3", 4200, 500, TimeUnit.SECONDS);
        Thread.sleep(400);
        Thread.sleep(400);
        assertEquals(3,map.size());
    }

    @Test
    public void MasterSlaveActorsMap_Remove_Key_Wait_Forever_Die_Respawn_And_Check_That_Item_Has_Been_Automatically_Removed_Test() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        map.put("hh", 420, 5, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals(1, map.size());
        Thread.sleep(6000);
        assertEquals(0, map.size());
    }

    @Test
    public void MasterSlaveActorsMap_Remove_Key() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

    @Test
    public void MasterSlaveActorsMap_Remove_Empty() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

    @Test
    public void Kill() throws InterruptedException {
        MasterSlaveActorsTimedSizableHashMap<String, Integer> map = new MasterSlaveActorsTimedSizableHashMap<>();
        Thread.sleep(100);

        //map.put("gil", 420, 69, TimeUnit.SECONDS);
        //assertEquals(1,map.size());
    }

    private static final int MAP_SIZE = 150000;
    private static final int NUM_PARTIES = 4;
    private final CyclicBarrier _bar = new CyclicBarrier(NUM_PARTIES);
    private final TimedSizableMap<Integer, Object> map = new MasterSlaveActorsTimedSizableHashMap<>();

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
