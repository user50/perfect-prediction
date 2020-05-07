package oleogin.rocksdb;

import oleogin.common.Pair;
import org.rocksdb.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RockDbServices {
    private RocksDB db;

    public RockDbServices(String path) {
        RocksDB.loadLibrary();
        final Options options = new Options()
                .setCreateIfMissing(true)
                .setCreateMissingColumnFamilies(true);

        try {
            this.db = RocksDB.open(options, path );
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> db.close()));
    }

    public <T> Function<List<T>, Map<T,byte[]>> searchBatch(Function<T,byte[]> keySerializer){
        return ts -> {
            List<byte[]> keys = ts.stream().map(keySerializer).collect(Collectors.toList());

            try {
                List<byte[]> result = db.multiGetAsList(keys);

                return IntStream.range(0, ts.size())
                        .filter(i -> result.get(i) != null)
                        .boxed( )
                        .collect(Collectors.toMap(i -> ts.get(i), i -> result.get(i)));
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public <T> Function<T, Optional<byte[]>> search(Function<T,byte[]> keySerializer){
        return t -> {
            try {
                return Optional.ofNullable(db.get(keySerializer.apply(t)));
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public <T> Function<Map<T,byte[]>, Map<T,byte[]>> storageBatch(Function<T,byte[]> keySerializer){
        WriteOptions writeOptions = new WriteOptions();

        return tMap -> {
            try (WriteBatch batch = new WriteBatch()){
                for (Map.Entry<T, byte[]> entry : tMap.entrySet())
                    batch.put(keySerializer.apply(entry.getKey()), entry.getValue());

                db.write(writeOptions, batch);

                return tMap;
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public <T> Consumer<Pair<T,byte[]>> storage(Function<T,byte[]> keySerializer){

        return p -> {
            try (WriteBatch batch = new WriteBatch()){
                db.put(keySerializer.apply(p.getFirst()), p.getSecond());
            } catch (RocksDBException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public RocksDB getDb() {
        return db;
    }
}
