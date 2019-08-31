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

    public static class UpdateStateRequest<K, V> {
        private final Map<K, V> updatedState;

        public UpdateStateRequest(Map<K, V> updatedState) {
            this.updatedState = updatedState;
        }

        public Map<K, V> getUpdatedState() {
            return this.updatedState;
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
                .match(UpdateStateRequest.class, this::updateState)
                .matchAny(o -> System.out.println("I'm coming home againnnnnnnn"))
                .build();
    }

    private void get(MasterActor.GetRequest<K, V> getRequest) {
        getSender().tell(this.internalState.get(getRequest.getKey()), getContext().getParent());
    }

    private void getSize(MasterActor.GetSizeRequest req) {
        getSender().tell(this.internalState.size(), getContext().getParent());
    }

    private void updateState(UpdateStateRequest<K, V> updateStateRequest) {
        this.internalState = new ConcurrentHashMap<>(updateStateRequest.updatedState);
    }
}
