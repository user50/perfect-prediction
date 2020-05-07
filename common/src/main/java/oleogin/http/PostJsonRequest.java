package oleogin.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHeader;

import java.net.URI;
import java.net.URISyntaxException;

public class PostJsonRequest implements HttpRequestProvider{

    private String host;
    private int port;
    private String resource;
    private Object body;

    public PostJsonRequest(String host, int port, String resource, Object body) {
        this.host = host;
        this.port = port;
        this.resource = resource;
        this.body = body;
    }

    public PostJsonRequest(String url, Object body) {
        try {
            URI uri = new URI(url);

            this.host = uri.getHost();
            this.port = uri.getPort();
            this.resource = uri.getPath() + (uri.getQuery() == null || uri.getQuery().isEmpty() ? "" : "?"+uri.getQuery());
            this.body = body;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpRequestBase getRequest() {
        HttpPost httpPost = new HttpPost(resource);
        httpPost.setHeader(new BasicHeader("Content-Type","application/json"));
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            httpPost.setEntity(new ByteArrayEntity(objectMapper.writeValueAsBytes(body)));

            return httpPost;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpHost getHost() {
        return new HttpHost(host, port);
    }
}
