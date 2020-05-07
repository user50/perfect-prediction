package oleogin.http;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public class TypeReferences {

    public static <A> TypeReference<A> get(){
        return new TypeReference<A>(){};
    }
}
