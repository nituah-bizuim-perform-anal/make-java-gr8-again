//package org.jmh;
//
//import org.boxed_single_actor.BoxedSingleActorTimedSizableHashMap;
//import org.openjdk.jmh.annotations.*;
//import org.openjdk.jmh.results.format.ResultFormatType;
//import org.openjdk.jmh.runner.Runner;
//import org.openjdk.jmh.runner.RunnerException;
//import org.openjdk.jmh.runner.options.Options;
//import org.openjdk.jmh.runner.options.OptionsBuilder;
//import org.openjdk.jmh.runner.options.TimeValue;
//
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Optional;
//import java.util.concurrent.TimeUnit;
//
//public class BoxedSingleActorTimedSizableHashMap_bench {
//    final static int COUNT = 1000;
//
//    @State(Scope.Thread)
//    public static class MyState {
//
//        @Setup(Level.Trial)
//        public void doSetup() throws InterruptedException {
//            System.out.println("SETTING UP");
//            map = new BoxedSingleActorTimedSizableHashMap<>();
//            for(int i=0;i<COUNT;++i){
//                map.put(i, String.valueOf(i), 10, TimeUnit.HOURS);
//            }
//
//            System.out.println("GOOD NIGHT");
//            Thread.sleep(5000);
//            System.out.println("Wakey!");
//        }
//
//        @TearDown(Level.Trial)
//        public void doTearDown() {
//            map.kill();
//            System.out.println("Do TearDown");
//        }
//
//        public BoxedSingleActorTimedSizableHashMap<Integer, String> map = new BoxedSingleActorTimedSizableHashMap<>();
//    }
//
//
//    @Benchmark
//    public long getSize(BoxedSingleActorTimedSizableHashMap_bench.MyState state) throws InterruptedException {
//        return state.map.size();
//    }
//
//    @Benchmark
//    public Optional<String> getValues(BoxedSingleActorTimedSizableHashMap_bench.MyState state) throws InterruptedException {
//        Optional<String> s = null;
//
//        for(int i = 0; i<COUNT; i++)
//            s = state.map.get(i);
//
//        return s;
//    }
//
//    @Benchmark
//    public Optional<String> getRandomValue(BoxedSingleActorTimedSizableHashMap_bench.MyState state) throws InterruptedException {
//        return state.map.get((int)(Math.random() * COUNT + 1));
//    }
//
//
//
//    @Benchmark
//    public void putKeys(BoxedSingleActorTimedSizableHashMap_bench.MyState state) throws InterruptedException {
//        for(int i =COUNT; i<COUNT*2;i++)
//            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
//    }
//
//
//    @Benchmark
//    @Threads(4)
//    public Optional<String> getValuesMultiThreads(BoxedSingleActorTimedSizableHashMap_bench.MyState state)throws InterruptedException {
//        Optional<String> s = null;
//
//        for(int i =0; i<COUNT;i++)
//            s = state.map.get(i);
//
//        return s;
//    }
//
//
//    @Benchmark
//    @Threads(4)
//    public void putKeysMultiThreads(BoxedSingleActorTimedSizableHashMap_bench.MyState state)throws InterruptedException {
//        for(int i =COUNT; i<COUNT*2;i++)
//            state.map.put(i, String.valueOf(i), 1,TimeUnit.SECONDS);
//    }
//
//
//    /*
//    // test is problematic
//    @Benchmark
//    public void removeKey(MyState state) throws InterruptedException {
//        for(int i =0; i<COUNT;i++)
//            state.map.remove(42069);
//    }
//
//    @Benchmark
//    public void removeKeys(MyState state) throws InterruptedException {
//        for(int i =COUNT; i<COUNT * 2;i++)
//            state.map.remove(i);
//    }
//     */
//
//
//    public static void main(String[] args) throws RunnerException {
//        /*
//         * You can run this test:
//         *
//         * a) Via the command line:
//         *    $ mvn clean install
//         *    $ java -jar target/benchmarks.jar
//         */
//
//        /*
//        better yet run this
//        java -cp  target/benchmarks.jar org.jmh.BoxedSingleActorTimedSizableHashMap_bench
//         */
//
//        SimpleDateFormat formatter= new SimpleDateFormat("yyyyMMdd_HHmmss");
//
//        Options opt = new OptionsBuilder()
//                .addProfiler(org.openjdk.jmh.profile.GCProfiler.class)
//                .include(BoxedSingleActorTimedSizableHashMap_bench.class.getSimpleName())
//                .forks(1)
//                //.threads(4)
//                .measurementIterations(1)
//                .measurementTime(TimeValue.seconds(1))
//                .warmupIterations(1)
//                .warmupTime(TimeValue.seconds(1))
//                .result("results/result_boxedactor" + formatter.format(new Date()) + ".json")
//                .resultFormat(ResultFormatType.JSON)
//                //.jvmArgs("-server", "-XX:+UseG1GC", "-Xmx256m")
//                .jvmArgs("-Xms1g", "-Xmx1g", "-Xmn800m", "-server")
//                .build();
//
//        new Runner(opt).run();
//    }
//}
