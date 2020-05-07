package oleogin.common;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class StreamBuilder<T> {

    private Stream<T> stream;

    public StreamBuilder(Supplier<Optional<T>> supplier) {
        Iterable<T> iterable = () -> new IteratorOverSupplier<>(supplier);

        this.stream = StreamSupport.stream(iterable.spliterator(), false);
    }

    public StreamBuilder(Stream<T> stream) {
        this.stream = stream;
    }

    public StreamBuilder<T> limit(int limit){
        this.stream = stream.limit(limit);

        return this;
    }

    public <T1> StreamBuilder<T1> mapStream(Function<Stream<T>,Stream<T1>> streamMapper){
        return new StreamBuilder<>(streamMapper.apply(this.stream));
    }

    public StreamBuilder<List<T>> miniBatch(int size){
        MiniBatchSupplier<T> tMiniBatchSupplier = new MiniBatchSupplier<>(new SupplierOverIterator<>(stream.iterator()), size);

        return new StreamBuilder<>(tMiniBatchSupplier);
    }

    public <T1> StreamBuilder<T1> map(Function<T,T1> mapper){
        return new StreamBuilder<>(stream.map(mapper));
    }

    public StreamBuilder<T> shuffle(){
        List<T> collect = this.stream.collect(Collectors.toList());
        Collections.shuffle(collect);

        return new StreamBuilder<T>(collect.stream());
    }

    public StreamBuilder<T> peek(Consumer<T> consumer){
        return new StreamBuilder<>(stream.peek(consumer));
    }

    public StreamBuilder<T> peek(Supplier<Consumer<T>> supplierConsumer){
        return new StreamBuilder<>(stream.peek(supplierConsumer.get()));
    }

    public Stream<T> toStream(){
        return stream;
    }
}
