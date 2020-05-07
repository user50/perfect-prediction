package oleogin.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class JsonBodyExtractor<T> implements HttpResponseHandler<T> {

    private TypeReference<T> typeReference;

    public JsonBodyExtractor(TypeReference<T> typeReference) {
        this.typeReference = typeReference;
    }

    @Override
    public T handle(CloseableHttpResponse httpResponse) {
        String body = getBodyAsString(httpResponse);

        try {
            return new ObjectMapper().readValue(body, typeReference);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse response body. Reason: "+e.getMessage()+". Body: "+body);
        }
    }

    private String getBodyAsString(CloseableHttpResponse httpResponse){
        try {
            Scanner s = new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
