package org.concurrent_actors;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class MapStateActor<K, V> extends AbstractActor {
    private Map<K, V> internalState;

    public static class ApplyRemoveDeltaRequest<K> {
        private final K removedItemKey;

        public ApplyRemoveDeltaRequest(K removedKey) {
            this.removedItemKey = removedKey;
        }

        public K getRemovedItemKey() {
            return this.removedItemKey;
        }
    }

    public static class ApplyPutDeltaRequest<K, V> {
        private final K puttedKey;
        private final V puttedValue;

        public ApplyPutDeltaRequest(K puttedKey, V puttedValue) {
            this.puttedKey = puttedKey;
            this.puttedValue = puttedValue;
        }

        public K getPuttedKey() {
            return puttedKey;
        }

        public V getPuttedValue() {
            return puttedValue;
        }
    }

    static Props props() {
        return Props.create(MapStateActor.class, () -> new MapStateActor());
    }

    public MapStateActor() {
        this.internalState = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(MasterActor.GetRequest.class, this::get)
                .match(MasterActor.GetSizeRequest.class, this::getSize)
                .match(ApplyRemoveDeltaRequest.class, this::applyRemoval)
                .match(ApplyPutDeltaRequest.class, this::applyPut)
                .matchAny(o -> System.out.println("I'm coming home againnnnnnnn"))
                .build();
    }

    private void get(MasterActor.GetRequest<K, V> getRequest) {
        getSender().tell(this.internalState.get(getRequest.getKey()), getContext().getParent());
    }

    private void getSize(MasterActor.GetSizeRequest req) {
        getSender().tell(this.internalState.size(), getContext().getParent());
    }

    private void applyRemoval(ApplyRemoveDeltaRequest<K> updateStateRequest) {
        this.internalState.remove(updateStateRequest.getRemovedItemKey());
    }

    private void applyPut(ApplyPutDeltaRequest<K, V> updateStateRequest) {
        this.internalState.put(updateStateRequest.puttedKey, updateStateRequest.puttedValue);
    }
}
