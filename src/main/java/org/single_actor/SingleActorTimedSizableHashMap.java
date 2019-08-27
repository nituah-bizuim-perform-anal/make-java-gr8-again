package org.single_actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import org.base.TimedSizableMap;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SingleActorTimedSizableHashMap<K, V> implements TimedSizableMap<K, V> {
    private ActorRef mapActor;

    public SingleActorTimedSizableHashMap() {
        ActorSystem system = ActorSystem.create("testSystem");

        this.mapActor = system.actorOf(MapActor.props());
    }

    @Override
    public long size() {
        this.mapActor.tell(new MapActor.GetSizeRequest(), ActorRef.noSender());

        return 0;
    }

    @Override
    public void put(K key, V value, int duration, TimeUnit unit) {
        this.mapActor.tell(new MapActor.PutRequest<>(key, value, duration, unit), ActorRef.noSender());
    }

    @Override
    public Optional<V> get(K key) {
        this.mapActor.tell(new MapActor.GetRequest<>(key), ActorRef.noSender());

        return null;
    }

    @Override
    public Optional<V> remove(K key) {
        this.mapActor.tell(new MapActor.RemoveRequest<>(key), ActorRef.noSender());

        return null;
    }
}
