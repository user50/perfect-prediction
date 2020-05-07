package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;

public class DeleteReq implements HttpRequestProvider {
    private String host;
    private int port;
    private String resource;

    public DeleteReq(String host, int port, String resource) {
        this.host = host;
        this.port = port;
        this.resource = resource;
    }

    @Override
    public HttpRequestBase getRequest() {
        return  new HttpPut(resource);
    }

    @Override
    public HttpHost getHost() {
        return null;
    }
}
