package org.jmh;

import org.base.TimedSizableMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.single_actor.SingleActorTimedSizableHashMap;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SingleActorTimedSizableHashMap_bench {

    final static int COUNT = 10000;

    @State(Scope.Thread)
    public static class MyState {

        @Setup(Level.Trial)
        public void doSetup() {
            map = new SingleActorTimedSizableHashMap<>();
            for(int i=0;i<COUNT;++i){
                map.put(i, String.valueOf(i), 10, TimeUnit.SECONDS);
            }
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            map.kill();
            System.out.println("Do TearDown");
        }

        public SingleActorTimedSizableHashMap<Integer, String> map = new SingleActorTimedSizableHashMap<>();
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 1, time = 2)
    @Measurement(iterations = 2, time = 5)
    public void getSize(MyState state) throws InterruptedException {
        for(int i =0; i<COUNT;i++)
            state.map.size();
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 1, time = 2)
    public void getValues(MyState state) throws InterruptedException {
        for(int i =0; i<COUNT;i++)
            Optional.ofNullable(state.map.get(i));
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 1, time = 2)
    public void putKeys(MyState state) throws InterruptedException {
        for(int i =0; i<COUNT;i++)
            state.map.put(i, String.valueOf(i), 10,TimeUnit.SECONDS);
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 1, time = 2)
    public void removeKey(MyState state) throws InterruptedException {
        for(int i =0; i<COUNT;i++)
            state.map.remove(42069);
    }

    @Benchmark
    @BenchmarkMode(Mode.All)
    @OutputTimeUnit(TimeUnit.SECONDS)
    @Warmup(iterations = 1, time = 2)
    public void removeKeys(MyState state) throws InterruptedException {
        for(int i =COUNT; i<COUNT * 2;i++)
            state.map.put(i, String.valueOf(i), 10,TimeUnit.SECONDS);
        for(int i =COUNT; i<COUNT * 2;i++)
            state.map.remove(i);
    }


    public static void main(String[] args) throws RunnerException {
        /*
         * You can run this test:
         *
         * a) Via the command line:
         *    $ mvn clean install
         *    $ java -jar target/benchmarks.jar
         */

        Options opt = new OptionsBuilder()
                .include(SingleActorTimedSizableHashMap_bench.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}