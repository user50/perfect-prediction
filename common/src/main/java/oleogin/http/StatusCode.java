package oleogin.http;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.util.Optional;

public class StatusCode implements HttpResponseHandler<Integer> {
    @Override
    public Integer handle(CloseableHttpResponse httpResponse) {
        return httpResponse.getStatusLine().getStatusCode();
    }
}
