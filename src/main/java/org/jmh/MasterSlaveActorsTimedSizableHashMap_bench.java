package org.jmh;

import org.concurrent_actors.MasterSlaveActorsTimedSizableHashMap;
import org.openjdk.jmh.annotations.*;
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

public class MasterSlaveActorsTimedSizableHashMap_bench {
    final static int COUNT = 1000;

    @State(Scope.Thread)
    public static class MyState {

        @Setup(Level.Trial)
        public void doSetup() {
            map = new MasterSlaveActorsTimedSizableHashMap<>();
            for(int i=0;i<COUNT;++i){
                map.put(i, String.valueOf(i), 10, TimeUnit.HOURS);
            }
        }

        @TearDown(Level.Trial)
        public void doTearDown() {
            System.out.println("Do TearDown");
        }

        public MasterSlaveActorsTimedSizableHashMap<Integer, String> map = new MasterSlaveActorsTimedSizableHashMap<>();
    }


    @Benchmark
    public long getSize(MasterSlaveActorsTimedSizableHashMap_bench.MyState state) throws InterruptedException {
        return state.map.size();
    }

    @Benchmark
    public Optional<String> getValues(MasterSlaveActorsTimedSizableHashMap_bench.MyState state) throws InterruptedException {
        Optional<String> s = null;

        for(int i = 0; i<COUNT; i++)
            s = state.map.get(i);

        return s;
    }

    @Benchmark
    public Optional<String> getRandomValue(MasterSlaveActorsTimedSizableHashMap_bench.MyState state) throws InterruptedException {
        return state.map.get((int)(Math.random() * COUNT + 1));
    }



    @Benchmark
    public void putKeys(MasterSlaveActorsTimedSizableHashMap_bench.MyState state) throws InterruptedException {
        for(int i =COUNT; i<COUNT*2;i++)
            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
    }


    @Benchmark
    @Threads(4)
    public Optional<String> getValuesMultiThreads(MasterSlaveActorsTimedSizableHashMap_bench.MyState state)throws InterruptedException {
        Optional<String> s = null;

        for(int i =0; i<COUNT;i++)
            s = state.map.get(i);

        return s;
    }


    @Benchmark
    @Threads(4)
    public void putKeysMultiThreads(MasterSlaveActorsTimedSizableHashMap_bench.MyState state)throws InterruptedException {
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
        java -cp  target/benchmarks.jar org.jmh.MasterSlaveActorsTimedSizableHashMap_bench
         */

        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMdd_HHmmss");

        Options opt = new OptionsBuilder()
                .addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
                .include(MasterSlaveActorsTimedSizableHashMap_bench.class.getSimpleName())
                .forks(1)
                //.threads(4)
                .measurementIterations(1)
                .measurementTime(TimeValue.seconds(1))
                .warmupIterations(1)
                .warmupTime(TimeValue.seconds(1))
                .result("results\\result_masterslave" + formatter.format(new Date()) + ".json")
                .resultFormat(ResultFormatType.JSON)
                //.jvmArgs("-server", "-XX:+UseG1GC", "-Xmx256m")
                .jvmArgs("-Xms1g", "-Xmx1g", "-Xmn800m", "-server")
                .build();

        new Runner(opt).run();
    }
}
