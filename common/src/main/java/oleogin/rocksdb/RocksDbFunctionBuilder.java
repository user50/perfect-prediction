package oleogin.rocksdb;

import oleogin.common.FunctionBuilder;
import oleogin.common.SerializationUtil;
import oleogin.common.Pair;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class RocksDbFunctionBuilder<A,B> extends FunctionBuilder<A,B> {

    public RocksDbFunctionBuilder(Function<A, B> function) {
        super(function);
    }

    public RocksDbFunctionBuilder<A,B> addRocksdbCache(String folder, Function<A,byte[]> keySerial){
        RockDbServices rockDbServices = new RockDbServices(folder);

        Function<A, Optional<B>> search = new FunctionBuilder<>(rockDbServices.search(keySerial))
                .andThen(bytes -> bytes.map(SerializationUtil::<B>readObj))
                .build();

        Consumer<Pair<A, byte[]>> storage = rockDbServices.storage(keySerial);

        Function<A, B> f = this.addCache(search, (a, b) -> storage.accept(new Pair<>(a, SerializationUtil.writeObj((Serializable) b)))).build();

        return new RocksDbFunctionBuilder<>(f);
    }
}
