package oleogin.common;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MiniBatchSupplier<T> implements Supplier<Optional<List<T>>> {

    private Supplier<Optional<T>> elementSupplier;
    private int batchSize;

    public MiniBatchSupplier(Supplier<Optional<T>> elementSupplier, int batchSize) {
        this.elementSupplier = elementSupplier;
        this.batchSize = batchSize;
    }

    @Override
    public Optional<List<T>> get() {
        List<T> collect = IntStream.range(0, batchSize)
                .mapToObj(value -> elementSupplier.get())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        return collect.isEmpty() ? Optional.empty() : Optional.of(collect);
    }
}
