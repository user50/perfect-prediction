package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;

import java.util.Map;

public class PostByteReq implements HttpRequestProvider {

    private String host;
    private int port;
    private String resource;
    private byte[] body;

    public PostByteReq(String host, int port, String resource, byte[] body) {
        this.host = host;
        this.port = port;
        this.resource = resource;
        this.body = body;
    }

    @Override
    public HttpRequestBase getRequest() {
        HttpPost httpPost = new HttpPost(resource);
        httpPost.setEntity(new ByteArrayEntity(body));

        return httpPost;
    }

    @Override
    public HttpHost getHost() {
        return new HttpHost(host, port);
    }
}
