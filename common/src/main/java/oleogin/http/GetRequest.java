package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

import java.net.URI;
import java.net.URISyntaxException;

public class GetRequest implements HttpRequestProvider {

    private String host;
    private int port;
    private String resource;

    public GetRequest(String host, int port, String resource) {
        this.host = host;
        this.port = port;
        this.resource = resource;
    }

    public GetRequest(String url){
        try {
            URI uri = new URI(url);
            this.host = uri.getHost();
            this.port = uri.getPort();
            this.resource = uri.getPath() + (uri.getQuery() == null|| uri.getQuery().isEmpty() ? "" : "?"+uri.getQuery());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpRequestBase getRequest() {
        HttpGet httpGet = new HttpGet(resource);

        httpGet.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");

        return httpGet;
    }

    @Override
    public HttpHost getHost() {
        return new HttpHost(host, port);
    }
}
