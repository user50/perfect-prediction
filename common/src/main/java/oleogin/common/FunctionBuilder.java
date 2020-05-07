
package oleogin.common;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FunctionBuilder<A, V> {

    private Function<A, V> function;

    public FunctionBuilder(Function<A, V> function) {
        this.function = function;
    }

    public <T extends FunctionBuilder<A,V>> T toFuncBuilder(Function<Function<A,V>, T> constructor){
        return constructor.apply(function);
    }

    public FunctionBuilder<A, V> retry(int count) {

        return new FunctionBuilder<A, V>(a -> {
            for (int i = 0; i < count - 1; i++) {
                try {
                    return function.apply(a);
                } catch (Exception e) {
                    //ignore
                }
            }

            return function.apply(a);
        });
    }

    public FunctionBuilder<A, V> retryExpWait(int count, long wait) {

        return new FunctionBuilder<A, V>(a -> {
            long startWeight = wait;
            for (int i = 0; i < count - 1; i++) {
                try {
                    return function.apply(a);
                } catch (Exception e) {
                    try {
                        Thread.sleep(startWeight);
                        startWeight = startWeight * 2;
                    } catch (InterruptedException ex) {
                        throw new RuntimeException(e);
                    }
                }
            }

            return function.apply(a);
        });
    }

    public FunctionBuilder<A, V> ignoreException(Supplier<V> defaultValue) {
        return new FunctionBuilder<>(a -> {
            try {
                return function.apply(a);
            } catch (Exception e) {
                return defaultValue.get();
            }
        });
    }

    public FunctionBuilder<A, Optional<V>> ignoreException() {
        return new FunctionBuilder<>(a -> {
            try {
                return Optional.of(function.apply(a));
            } catch (Exception e) {
                return Optional.empty();
            }
        });
    }

    public FunctionBuilder<List<A>, List<V>> toBatchFun() {
        Function<List<A>, List<V>> batchFun = as -> as.stream()
                .map(function)
                .collect(Collectors.toList());


        return new FunctionBuilder<>(batchFun);
    }

    public FunctionBuilder<A, V> synchronize() {
        Function<A, V> build = this.build();

        return new FunctionBuilder<A, V>(a -> {
            synchronized (build) {
                return build.apply(a);
            }
        });
    }

    public <V2> FunctionBuilder<A, V2> andThen(Function<V, V2> fun) {
        return new FunctionBuilder<>(build().andThen(fun));
    }

    public <V2> FunctionBuilder<A, V2> andThen(Supplier<Function<V, V2>> functionSupplier) {
        return new FunctionBuilder<>(build().andThen(functionSupplier.get()));
    }

    public <A2> FunctionBuilder<A2, V> compose(Function<A2, A> fun) {

        return new FunctionBuilder<>(build().compose(fun));
    }

    public FunctionBuilder<A, V> timeSpent(String hint) {
        return new FunctionBuilder<>(a -> {

            long start = System.currentTimeMillis();
            V apply = function.apply(a);
            System.out.println(hint + ": " + (System.currentTimeMillis() - start));

            return apply;
        });
    }

    public FunctionBuilder<A, V> meanTimeSpent(String hint) {
        AtomicLong totalSpent = new AtomicLong();
        AtomicInteger calls = new AtomicInteger();

        return new FunctionBuilder<>(a -> {
            long start = System.currentTimeMillis();
            V apply = function.apply(a);
            totalSpent.addAndGet(System.currentTimeMillis() - start);
            calls.incrementAndGet();
            System.out.println(hint + ": " + (totalSpent.get() / calls.get()));

            return apply;
        });
    }

    public FunctionBuilder<A, V> logException(String fileName) {
        return new FunctionBuilder<>(a -> {
            try {
                return function.apply(a);
            } catch (Exception e) {
                try (PrintWriter pw = new PrintWriter(new FileWriter(fileName, true))) {
                    pw.println(new Date());
                    e.printStackTrace(pw);
                } catch (IOException e1) {
                    //ignore
                }

                throw e;
            }
        });
    }

    public <A1, V1> FunctionBuilder<A1, V1> wrap(Function<Function<A, V>, Function<A1, V1>> secondOrderFunction) {
        return new FunctionBuilder<>(secondOrderFunction.apply(function));
    }

    public  FunctionBuilder<A, V> decorate(Function<Function<A, V>, Function<A, V>> secondOrderFunction) {
        return new FunctionBuilder<>(secondOrderFunction.apply(function));
    }


    public void consume(Consumer<Function<A, V>> functionConsumer) {
        functionConsumer.accept(function);
    }

    public FunctionBuilder<A, V> addCache(Function<A, Optional<V>> cache, BiConsumer<A, V> storage) {
        Function<A, V> function = this.function;

        return new FunctionBuilder<>(key -> {
            Optional<V> cached = cache.apply(key);

            if (cached.isPresent())
                return cached.get();

            V v = function.apply(key);
            storage.accept(key, v);

            return v;
        });
    }

    public FunctionBuilder<A, V> onExceptionFallBack(Function<A, V> fallback) {
        Function<A, V> function = this.function;

        return new FunctionBuilder<>(key -> {
            try {
                return function.apply(key);
            } catch (Exception e) {
                return fallback.apply(key);
            }
        });
    }

    public FunctionBuilder<A, V> limitedAccess(long msBetweenRequests) {
        Function<A, V> function = this.function;

        long lastAccess = 0L;

        return new FunctionBuilder<>(key -> {
            if (System.currentTimeMillis() - lastAccess < msBetweenRequests) {
                try {
                    Thread.sleep(msBetweenRequests - System.currentTimeMillis() + lastAccess);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            return function.apply(key);
        });
    }

    public Function<A, V> build() {
        return function;
    }

    public Consumer<A> buildAsConsumer() {
        return a -> function.apply(a);
    }

}
