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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class CapitalisticTimedSizableHashMap_bench {

    final static int COUNT = 1000;

    @State(Scope.Thread)
    public static class MyState {

        @Setup(Level.Trial)
        public void doSetup() {
            map = new CapitalisticTimedSizableHashMap<>();
            for(int i=0;i<COUNT;++i){
                map.put(i, String.valueOf(i), 10, TimeUnit.HOURS);
            }
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown");
        }

        public CapitalisticTimedSizableHashMap<Integer, String> map = new CapitalisticTimedSizableHashMap<>();
    }


    @Benchmark
    public long getSize(MyState state) throws InterruptedException {
        return state.map.size();
    }

    @Benchmark
    public Optional<String> getValues(MyState state) throws InterruptedException {
        Optional<String> s = null;

        for(int i = 0; i<COUNT; i++)
            s = state.map.get(i);

        return s;
    }

    @Benchmark
    public Optional<String> getRandomValue(MyState state) throws InterruptedException {
        Optional<String> s = null;

        for(int i = 0; i<COUNT; i++)
            s = state.map.get((int)(Math.random() * COUNT + 1));

        return s;
    }

    @Benchmark
    public void putKeys(MyState state) throws InterruptedException {
        for(int i =COUNT; i<COUNT*2;i++)
            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
    }

    @Benchmark
    @Threads(4)
    public Optional<String> getValuesMultiThreads(MyState state)throws InterruptedException {
        Optional<String> s = null;

        for(int i =0; i<COUNT;i++)
            s = state.map.get(i);

        return s;
    }

    @Benchmark
    @Threads(4)
    public void putKeysMultiThreads(MyState state)throws InterruptedException {
        for(int i =COUNT; i<COUNT*2;i++)
            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
    }


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
        java -cp  target/benchmarks.jar org.jmh.CapitalisticTimedSizableHashMap_bench
         */

        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMdd_HHmmss");

        Options opt = new OptionsBuilder()
                .addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
                .include(CapitalisticTimedSizableHashMap_bench.class.getSimpleName())
                .forks(1)
                //.threads(4)
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .result("results\\result_imperialistic" + formatter.format(new Date()) + ".json")
                .resultFormat(ResultFormatType.JSON)
                //.jvmArgs("-server", "-XX:+UseG1GC", "-Xmx256m")
                .jvmArgs("-Xms1g", "-Xmx1g", "-Xmn800m", "-server")
                .build();

        new Runner(opt).run();
    }
}