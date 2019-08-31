package org.boxed_single_actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.util.Timeout;
import org.base.TimedSizableMap;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

public class BoxedSingleActorTimedSizableHashMap<K, V> implements TimedSizableMap<K, V> {
    private final Timeout globalTimeout = new Timeout(1, TimeUnit.SECONDS);
    private final FiniteDuration globalTimeoutDuration = globalTimeout.duration();
    private ConcurrentHashMap<K, V> latestMap;

    private ActorSystem system;

    private ActorRef mapActor;

    public BoxedSingleActorTimedSizableHashMap() {
        latestMap = new ConcurrentHashMap<>();

        system = ActorSystem.create("testSystem");

        this.mapActor = system.actorOf(ModificationActor.props());
    }

    @Override
    public long size() {
        return this.latestMap.size();
    }

    @Override
    public void put(K key, V value, int duration, TimeUnit unit) {
        this.mapActor.tell(new ModificationActor.PutRequest<>(key, value, duration, unit), ActorRef.noSender());

        Future<Object> bla = ask(this.mapActor, new ModificationActor.PutRequest<>(key, value, duration, unit), globalTimeout);
        try {
            ModificationActor.UpdateStateRequest updatedMAp = (ModificationActor.UpdateStateRequest) Await.result(bla, globalTimeoutDuration);
            this.latestMap = new ConcurrentHashMap<>(updatedMAp.getNewMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Optional<V> get(K key) {
        return Optional.ofNullable(this.latestMap.get(key));
    }

    @Override
    public Optional<V> remove(K key) {
        Future<Object> bla = ask(this.mapActor, new ModificationActor.RemoveRequest<>(key), globalTimeout);
        try {
            ModificationActor.RemoveResult value = (ModificationActor.RemoveResult) Await.result(bla, globalTimeoutDuration);

            this.latestMap = new ConcurrentHashMap<>(value.getNewMap());

            return (Optional<V>) Optional.ofNullable(value.getResult());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.ofNullable(null);
        }
    }

    public void kill(){
        //this.mapActor.tell(new MapActor.KillRequest(), ActorRef.noSender());
        system.stop(this.mapActor);
        system.terminate();
    }
}
