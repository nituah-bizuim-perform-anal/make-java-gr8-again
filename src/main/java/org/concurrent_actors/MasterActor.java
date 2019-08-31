package org.concurrent_actors;

// Or "Instrumentation" Actor. fucking sue me.

import akka.actor.*;
import akka.routing.*;
import akka.routing.Broadcast;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MasterActor<K, V> extends AbstractActorWithTimers {
    private final HashMap<K, V> internalState;
    // private Router router;
    private ActorRef router2;

    static Props props() {
        return Props.create(MasterActor.class, () -> new MasterActor());
    }

    public MasterActor() {
        this.internalState = new HashMap<>();

        List<Routee> routees = new ArrayList<Routee>();

        router2 = getContext().actorOf(new SmallestMailboxPool(4).props(Props.create(MapStateActor.class)));
    }

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

    public static class RemoveRequest<K> {
        private final K key;

        public RemoveRequest(K key) {
            this.key = key;
        }

        public K getKey() {
            return this.key;
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

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(PutRequest.class, this::put)
                .match(RemoveRequest.class, this::remove)
                .match(
                        GetRequest.class,
                        message -> {
                            // router.route(message, getSender());
                            router2.tell(message, getSender());
                        })
                .match(
                        GetSizeRequest.class,
                        message -> {
                            // router.route(message, getSender());
                            router2.tell(message, getSender());
                        }
                )
                .matchAny(msg -> System.out.println("WOOOO UNEXPECTED MANNN " + msg))
                .build();
    }

    private void put(PutRequest<K, V> putRequest) {
        this.internalState.put(putRequest.key, putRequest.value);

        // TODO - MAKE THIS WORK LOL
        // router.tell(new Broadcast("Watch out for Davy Jones' locker"), getTestActor());
        router2.tell(new Broadcast(new MapStateActor.ApplyPutDeltaRequest<>(putRequest.key, putRequest.value)), getSelf());

        // NOTE - VERY IMPORTANT!!!!!%@#$)$@#*)!@#$jasfdkasdfjlkfdsjglksdfjgkl my dad beats me at nights
        // According to Akka's documentation, startSingleTimer ensures that if we schedule 2 single timers
        // with the same key, the previous one is cancelled. Hence we don't have to use the AtomicLong and the wrapper
        // class that uses it anymore - we can have Akka take care for it.
        getTimers().startSingleTimer(putRequest.key, new RemoveRequest<>(putRequest.key), new FiniteDuration(putRequest.duration, putRequest.unit));
        //sender().tell("", self());
    }

    private void remove(RemoveRequest<K> removeRequest) {
        V res = this.internalState.remove(removeRequest.key);
        sender().tell(res, self());
        router2.tell(new Broadcast(new MapStateActor.ApplyRemoveDeltaRequest<>(removeRequest.key)), getSelf());
    }
}
