package functional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.google.common.collect.Iterables.transform;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public class IterablesBenchmark {

    public static void main(String[] args) throws RunnerException {
        Options options = new OptionsBuilder()
            .include(CollectListToList.class.getSimpleName())
            .forks(1)
            .build();

        new Runner(options).run();
    }

    private static final com.google.common.base.Function<String, String> guavaIdentity = new com.google.common.base.Function<String, String>() {
        @Override
        public String apply(String s) {
            return s;
        }
    };

    private static final java.util.function.Function<String, String> streamsIdentity = new java.util.function.Function<String, String>() {
        @Override
        public String apply(String s) {
            return s;
        }
    };

    private static <T> Collector<T, ImmutableList.Builder<T>, ImmutableList<T>> immutableList() {
        return Collector.of(ImmutableList.Builder::new, ImmutableList.Builder::add,
            (l, r) -> l.addAll(r.build()), ImmutableList.Builder<T>::build);
    }


    private static final String[] data = new String[10];
    static {
        Arrays.fill(data, "hello");
    }

    private static List<String> list = Arrays.asList(data);

    @BenchmarkMode(value = Mode.Throughput)
    @Warmup(iterations = 5)
    @Measurement(iterations = 5)
    public static class CollectListToList {

        @Benchmark
        public void iterate() {
            List<String> result = new ArrayList<String>(list.size());
            for (String each : list) {
                result.add(each);
            }
        }

        @Benchmark
        public void iterate_immutable() {
            ImmutableList.Builder<String> builder = ImmutableList.builder();
            for (String each : list) {
                builder.add(each);
            }
            List<String> result = builder.build();
        }

        @Benchmark
        public void guava() {
            List<String> result = ImmutableList.copyOf(transform(list, guavaIdentity));
        }

        @Benchmark
        public void guava_immutable() {
            List<String> result = Lists.newArrayList(transform(list, guavaIdentity));
        }

        @Benchmark
        public void streams() {
            List<String> result = list.stream().map(streamsIdentity).collect(Collectors.toList());
        }

        @Benchmark
        public void streams_immutable() {
            List<String> result = list.stream().map(streamsIdentity).collect(immutableList());
        }
    }
}