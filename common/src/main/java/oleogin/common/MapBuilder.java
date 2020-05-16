package oleogin.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MapBuilder<A,B> implements Serializable {

    public static final long serialVersionUID = 1L;

    public Map<A,B> map;

    public MapBuilder(Map<A, B> map) {
        this.map = map;
    }

    public MapBuilder() {
        this.map = new HashMap<>();
    }


    public MapBuilder<A,B> put(A key, B value){
        map.put(key, value);

        return new MapBuilder<>(map);
    }

    public Map<A,B> build(){
        return map;
    }

    public <T extends B> T get(A key){
        return (T)map.get(key);
    }

    public MapBuilder<A,B> remove(A key){
        map.remove(key);

        return new MapBuilder<>(map);
    }
}
