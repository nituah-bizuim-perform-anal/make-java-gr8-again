package org.jmh;

import org.imperialistic.CapitalisticTimedSizableHashMap;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.profile.GCProfiler;
import org.openjdk.jmh.results.format.ResultFormatType;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import org.single_actor.SingleActorTimedSizableHashMap;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/*
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Measurement(iterations = 20, time = 200, timeUnit = TimeUnit.SECONDS)
@Warmup(iterations = 10, time = 200, timeUnit = TimeUnit.SECONDS)*/
public class SingleActorTimedSizableHashMap_bench {

    final static int COUNT = 1000;

    @State(Scope.Thread)
    public static class MyState {

        @Setup(Level.Trial)
        public void doSetup() {
            map = new SingleActorTimedSizableHashMap<>();
            for(int i=0;i<COUNT;++i){
                map.put(i, String.valueOf(i), 10, TimeUnit.HOURS);
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
    public long getSize(MyState state) throws InterruptedException {
        return state.map.size();
    }

    @Benchmark
    public Optional<Optional<String>> getValues(MyState state) throws InterruptedException {
        Optional<Optional<String>> s = null;

        for(int i = 0; i<COUNT; i++)
            s = Optional.ofNullable(state.map.get(i));

        return s;
    }

    @Benchmark
    public Optional<Optional<String>> getRandomValue(MyState state) throws InterruptedException {
        return Optional.ofNullable(state.map.get((int)(Math.random() * COUNT + 1)));
    }



    @Benchmark
    public void putKeys(MyState state) throws InterruptedException {
        for(int i =COUNT; i<COUNT*2;i++)
            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
    }


    @Benchmark
    @Threads(4)
    public Optional<Optional<String>> getValuesMultiThreads(MyState state)throws InterruptedException {
        Optional<Optional<String>> s = null;

        for(int i =0; i<COUNT;i++)
            s = Optional.ofNullable(state.map.get(i));

        return s;
    }


    @Benchmark
    @Threads(4)
    public void putKeysMultiThreads(MyState state)throws InterruptedException {
        for(int i =COUNT; i<COUNT*2;i++)
            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
    }


    /*
    // test is problematic
    @Benchmark
    public void removeKey(MyState state) throws InterruptedException {
        for(int i =0; i<COUNT;i++)
            state.map.remove(42069);
    }

    @Benchmark
    public void removeKeys(MyState state) throws InterruptedException {
        for(int i =COUNT; i<COUNT * 2;i++)
            state.map.remove(i);
    }
     */


    public static void main(String[] args) throws RunnerException {
        /*
         * You can run this test:
         *
         * a) Via the command line:
         *    $ mvn clean install
         *    $ java -jar target/benchmarks.jar
         */

        /*
        better yet run this
        java -cp  target/benchmarks.jar org.jmh.SingleActorTimedSizableHashMap_bench
         */

        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMdd_HHmmss");

        Options opt = new OptionsBuilder()
                .addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
                .include(SingleActorTimedSizableHashMap_bench.class.getSimpleName())
                .forks(1)
                //.threads(4)
                .measurementIterations(3)
                .measurementTime(TimeValue.seconds(10))
                .warmupIterations(2)
                .warmupTime(TimeValue.seconds(10))
                .result("results/result_singleactor" + formatter.format(new Date()) + ".json")
                .resultFormat(ResultFormatType.JSON)
                //.jvmArgs("-server", "-XX:+UseG1GC", "-Xmx256m")
                .jvmArgs("-Xms1g", "-Xmx1g", "-Xmn800m", "-server")
                .build();

        new Runner(opt).run();
    }
}