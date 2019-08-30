package org.test;

import org.base.TimedSizableMap;
import org.junit.Test;
import org.single_actor.SingleActorTimedSizableHashMap;

import java.util.concurrent.*;
import java.util.stream.IntStream;

import static junit.framework.TestCase.assertEquals;

public class MapActorTest {

    @Test
    public void SingleMapActor_GetSize(){
        TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        assertEquals(0,map.size());
    }

    @Test
    public void SingleMapActor_Insert() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        map.put("gil2", 421, 629, TimeUnit.SECONDS);
        map.put("gil3", 4200, 619, TimeUnit.SECONDS);
        Thread.sleep(400);
        Thread.sleep(400);
        assertEquals(3,map.size());
    }

    @Test
    public void SingleMapActor_Remove_Key_Wait_Forever_Die_Respawn_And_Check_That_Item_Has_Been_Automatically_Removed_Test() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("hh", 420, 5, TimeUnit.SECONDS);
        Thread.sleep(100);
        assertEquals(1, map.size());
        Thread.sleep(6000);
        assertEquals(0, map.size());
    }

    @Test
    public void SingleMapActor_Remove_Key() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

    @Test
    public void SingleMapActor_Remove_Empty() throws InterruptedException {
        TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

    @Test
    public void Kill() throws InterruptedException {
        SingleActorTimedSizableHashMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        Thread.sleep(100);
        map.kill();
        //map.put("gil", 420, 69, TimeUnit.SECONDS);
        //assertEquals(1,map.size());
    }

    private static final int MAP_SIZE = 150000;
    private static final int NUM_PARTIES = 4;
    private final CyclicBarrier _bar = new CyclicBarrier(NUM_PARTIES);
    private final TimedSizableMap<Integer, Object> map = new SingleActorTimedSizableHashMap<>();

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
