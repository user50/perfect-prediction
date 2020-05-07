package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.FileEntity;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class PutFileRequest implements HttpRequestProvider{

    private String host;
    private int port;
    private String resource;
    private String file;

    public PutFileRequest(String host, int port, String resource, String file) {
        this.host = host;
        this.port = port;
        this.resource = resource;
        this.file = file;
    }

    public PutFileRequest(String url, String file){
        try {
            URI uri = new URI(url);
            this.host = uri.getHost();
            this.port = uri.getPort();
            this.resource = uri.getPath() + (uri.getQuery().isEmpty() ? "" : "?"+uri.getQuery());
            this.file = file;
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpRequestBase getRequest() {
        HttpPut httpPut = new HttpPut(resource);

        httpPut.setHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpPut.setEntity(new FileEntity(new File(file)));

        return httpPut;
    }

    @Override
    public HttpHost getHost() {
        return new HttpHost(host, port);
    }
}
