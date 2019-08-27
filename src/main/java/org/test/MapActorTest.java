package org.test;

import org.base.TimedMap;
import org.junit.Test;
import org.single_actor.SingleActorTimedSizableHashMap;

import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertEquals;

public class MapActorTest {

    @Test
    public void SingleMapActor_GetSize(){
        TimedMap.TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        assertEquals(0,map.size());
    }

    @Test
    public void SingleMapActor_Insert() throws InterruptedException {
        TimedMap.TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        map.put("gil2", 421, 629, TimeUnit.SECONDS);
        map.put("gil3", 4200, 619, TimeUnit.SECONDS);
        Thread.sleep(400);
        Thread.sleep(400);
        assertEquals(3,map.size());
    }

    @Test
    public void SingleMapActor_Remove_Key() throws InterruptedException {
        TimedMap.TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

    @Test
    public void SingleMapActor_Remove_Empty() throws InterruptedException {
        TimedMap.TimedSizableMap<String, Integer> map = new SingleActorTimedSizableHashMap<>();
        map.put("gil", 420, 69, TimeUnit.SECONDS);
        Thread.sleep(2000);
        map.remove("gil");
        Thread.sleep(2000);
        assertEquals(0,map.size());
    }

}
