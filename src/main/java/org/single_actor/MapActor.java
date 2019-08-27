package org.single_actor;

import akka.actor.AbstractActor;
import akka.actor.AbstractActorWithTimers;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class MapActor<K, V> extends AbstractActorWithTimers {
    private static Object TIMEOUT_REMOVE_KEY = "TimeoutRemoveKey";

    private final HashMap<K, V> internalState;

    public static class GetRequest<K, V> {
        private final K key;


        public GetRequest(K key) {
            this.key = key;
        }

        public K getKey() {
            return this.key;
        }
    }

    public static class GetSizeRequest {
        public GetSizeRequest() {
        }
    }

    public static class PutRequest<K, V> {
        private final K key;
        private final V value;
        private final int duration;
        private final TimeUnit unit;

        public PutRequest(K key, V value, int duration, TimeUnit unit) {
            this.key = key;
            this.value = value;
            this.duration = duration;
            this.unit = unit;
        }

        public K getKey() {
            return this.key;
        }

        public V getValue() {
            return this.value;
        }
    }

    public static class RemoveRequest<K> {
        private final K key;

        public RemoveRequest(K key) {
            this.key = key;
        }

        public K getKey() {
            return this.key;
        }
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    static Props props() {
        return Props.create(MapActor.class, () -> new MapActor());
    }

    public MapActor() {
        this.internalState = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(GetRequest.class, this::get)
                .match(RemoveRequest.class, this::remove)
                .match(GetSizeRequest.class, this::getSize)
                .match(PutRequest.class, this::put)
                .matchAny(o -> log.info("the fuck? get outta here lol"))
                .build();
    }

    private void put(PutRequest<K, V> putRequest) {
        this.internalState.put(putRequest.key, putRequest.value);
        getTimers().startSingleTimer(TIMEOUT_REMOVE_KEY, new RemoveRequest<>(putRequest.key), new FiniteDuration(putRequest.duration, putRequest.unit));
        //sender().tell("", self());
    }

    private void get(GetRequest<K, V> getRequest) {
        sender().tell(this.internalState.get(getRequest.key), self());
    }

    private void getSize(GetSizeRequest req) {
        // return this.internalState.size();
        sender().tell(this.internalState.size(), self());
    }

    private void remove(RemoveRequest<K> removeRequest) {
        sender().tell(this.internalState.remove(removeRequest.key), self());
    }
}
