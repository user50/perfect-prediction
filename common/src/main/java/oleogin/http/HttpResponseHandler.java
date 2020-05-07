package oleogin.http;

import org.apache.http.client.methods.CloseableHttpResponse;

/**
 * Created by user50 on 26.05.2015.
 */
public interface HttpResponseHandler<T> {

    T handle(CloseableHttpResponse httpResponse);

}
