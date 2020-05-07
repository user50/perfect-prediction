package oleogin.http;

import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class WebIteratorProvider {

    private String url;

    public WebIteratorProvider(String url) {
        this.url = url;
    }

    public <T> Supplier<Optional<List<T>>> get(TypeReference<List<T>> typeReference){
        HttpService httpService = new MultiThreadHttpServiceProvider().newInstance();

        return () -> {
            try {
                List<T> execute = httpService.execute(new GetRequest(url), new JsonBodyExtractor<>(typeReference));

                return execute.isEmpty()? Optional.empty() : Optional.of(execute);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public <T> Supplier<Optional<List<T>>> get(){
        TypeReference<List<T>> typeReference = new TypeReference<List<T>>(){};

        return get(typeReference);
    }
}
