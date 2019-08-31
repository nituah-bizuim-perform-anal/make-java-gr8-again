package org.concurrent_actors;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.base.TimedSizableMap;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class MasterSlaveActorsTimedSizableHashMap<K, V> implements TimedSizableMap<K, V> {
    private final Timeout globalTimeout = new Timeout(50, TimeUnit.SECONDS);
    private final FiniteDuration globalTimeoutDuration = globalTimeout.duration();

    private final ActorSystem actorSys;
    private final ActorRef masterActor;

    public MasterSlaveActorsTimedSizableHashMap() {
        this.actorSys = ActorSystem.create();
        this.masterActor = actorSys.actorOf(MasterActor.props());
    }

    @Override
    public long size() {
        Future<Object> bla = ask(this.masterActor, new MasterActor.GetSizeRequest(), globalTimeout);
        try {
            int size = (int) Await.result(bla, globalTimeoutDuration);
            return size;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    @Override
    public void put(K key, V value, int duration, TimeUnit unit) {
        masterActor.tell(new MasterActor.PutRequest<>(key, value, duration, unit), ActorRef.noSender());
    }

    @Override
    public Optional<V> get(K key) {
        try {
            V value =(V) Await.result(akka.pattern.Patterns.ask(this.masterActor, new MasterActor.GetRequest<>(key), globalTimeout), globalTimeoutDuration);
            return Optional.ofNullable(value);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(null);
        }
    }

    @Override
    public Optional<V> remove(K key) {
        Future<Object> bla = akka.pattern.Patterns.ask(this.masterActor, new MasterActor.RemoveRequest<>(key), globalTimeout);
        try {
            V value = (V) Await.result(bla, globalTimeoutDuration);

            return Optional.ofNullable(value);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(null);
        }
    }
}
