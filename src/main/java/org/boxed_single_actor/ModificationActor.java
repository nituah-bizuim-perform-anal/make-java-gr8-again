package org.boxed_single_actor;

import akka.actor.AbstractActorWithTimers;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class ModificationActor<K, V> extends AbstractActorWithTimers {

    private final HashMap<K, V> internalState;

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

    public static class KillRequest{
        public KillRequest(){
        }
    }

    public static class UpdateStateRequest<K, V> {
        private final HashMap<K, V> newMap;
        public UpdateStateRequest(HashMap<K, V> newMap) {
            this.newMap = newMap;
        }

        public HashMap<K, V> getNewMap() {
            return this.newMap;
        }
    }

    public static class RemoveResult<K ,V> {
        private final HashMap<K, V> newMap;
        private final V result;

        public RemoveResult(V result, HashMap<K, V> updatedMap) {
            this.newMap = updatedMap;
            this.result = result;
        }

        public V getResult() {
            return result;
        }

        public HashMap<K, V> getNewMap() {
            return newMap;
        }
    }

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    static Props props() {
        return Props.create(ModificationActor.class, () -> new ModificationActor());
    }

    public ModificationActor() {
        this.internalState = new HashMap<>();
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(RemoveRequest.class, this::remove)
                .match(PutRequest.class, this::put)
                .matchAny(o -> log.info("the fuck? get outta here lol"))
                .build();
    }

    private void put(PutRequest<K, V> putRequest) {
        this.internalState.put(putRequest.key, putRequest.value);

        sender().tell(new UpdateStateRequest(this.internalState), self());

        // NOTE - VERY IMPORTANT!!!!!%@#$)$@#*)!@#$jasfdkasdfjlkfdsjglksdfjgkl my dad beats me at nights
        // According to Akka's documentation, startSingleTimer ensures that if we schedule 2 single timers
        // with the same key, the previous one is cancelled. Hence we don't have to use the AtomicLong and the wrapper
        // class that uses it anymore - we can have Akka take care for it.
        getTimers().startSingleTimer(putRequest.key, new RemoveRequest<>(putRequest.key), new FiniteDuration(putRequest.duration, putRequest.unit));
        //sender().tell("", self());
    }

    private void remove(RemoveRequest<K> removeRequest) {
        sender().tell(new RemoveResult<>(this.internalState.remove(removeRequest.key), this.internalState), self());
    }

    public void kill(KillRequest msg){
        getContext().stop(getSelf());
    }
}
