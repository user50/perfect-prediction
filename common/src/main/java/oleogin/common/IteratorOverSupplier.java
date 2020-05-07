package oleogin.common;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.Supplier;

public class IteratorOverSupplier<T> implements Iterator<T> {

    private final Supplier<Optional<T>> supplier;

    private T nextElement;

    public IteratorOverSupplier(Supplier<Optional<T>> supplier) {
        this.supplier = supplier;
    }

    @Override
    public boolean hasNext() {
        if (nextElement == null)
        {
            Optional<T> element = supplier.get();
            if (element.isPresent())
                nextElement = element.get();
        }

        return nextElement != null;
    }

    @Override
    public T next() {
        if (!hasNext())
            throw new IllegalStateException("No more elements");

        T element = nextElement;
        nextElement = null;

        return element;
    }

}
