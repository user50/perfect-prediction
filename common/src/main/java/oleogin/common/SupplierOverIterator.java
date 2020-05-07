package oleogin.common;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public class SupplierOverIterator<T> implements Supplier<Optional<T>> {

    private Iterator<T> iterator;

    public SupplierOverIterator(Iterator<T> iterator) {
        this.iterator = iterator;
    }

    @Override
    public Optional<T> get() {
        if (iterator.hasNext())
            return Optional.of(iterator.next());

        return Optional.empty();
    }
}
