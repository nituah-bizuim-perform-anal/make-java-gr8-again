package org.single_actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import static akka.pattern.Patterns.ask;

import akka.util.Timeout;
import org.base.TimedSizableMap;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class SingleActorTimedSizableHashMap<K, V> implements TimedSizableMap<K, V> {
    private final Timeout globalTimeout = new Timeout(1, TimeUnit.SECONDS);
    private final FiniteDuration globalTimeoutDuration = globalTimeout.duration();

    private ActorRef mapActor;

    public SingleActorTimedSizableHashMap() {
        ActorSystem system = ActorSystem.create("testSystem");

        this.mapActor = system.actorOf(MapActor.props());
    }

    @Override
    public long size() {

        Future<Object> bla = ask(this.mapActor, new MapActor.GetSizeRequest(), globalTimeout);
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
        this.mapActor.tell(new MapActor.PutRequest<>(key, value, duration, unit), ActorRef.noSender());
    }

    @Override
    public Optional<V> get(K key) {
        Future<Object> bla = ask(this.mapActor, new MapActor.GetRequest<>(key), globalTimeout);
        try {
            V value =(V) Await.result(bla, globalTimeoutDuration);

            return Optional.ofNullable(value);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(null);
        }
    }

    @Override
    public Optional<V> remove(K key) {
        Future<Object> bla = ask(this.mapActor, new MapActor.RemoveRequest<>(key), globalTimeout);
        try {
            V value = (V) Await.result(bla, globalTimeoutDuration);

            return Optional.ofNullable(value);
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(null);
        }
    }

    public void kill(){
        this.mapActor.tell(new MapActor.KillRequest(), ActorRef.noSender());
    }
}
