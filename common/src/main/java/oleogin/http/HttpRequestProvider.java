package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpRequestBase;

/**
 * Created by user50 on 26.05.2015.
 */
public interface HttpRequestProvider {

    HttpRequestBase getRequest();

    HttpHost getHost();

}
