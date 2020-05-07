package oleogin.http;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ByteArrayEntity;

import java.net.URI;
import java.util.Optional;

public class HttpReq {

    public HttpRequestBase req;

    public HttpReq(HttpRequestBase req) {
        this.req = req;
    }

    public static HttpReq get(){
        return new HttpReq(new HttpGet());
    }

    public static HttpReq port(){
        return new HttpReq(new HttpPost());
    }

    public static HttpReq delete(){
        return new HttpReq(new HttpDelete());
    }

    public static HttpReq put(){
        return new HttpReq(new HttpPut());
    }

    public HttpReq uri(String uri){
        req.setURI(URI.create(uri));

        return this;
    }


    public HttpReq body(byte[] body){
        if (!(req instanceof HttpEntityEnclosingRequestBase))
            throw new RuntimeException("Unsupported for get/delete requests");

        ((HttpEntityEnclosingRequestBase)req).setEntity(new ByteArrayEntity(body));

        return this;
    }

    public Optional<byte[]> ok200bytes(String host, int port){
        return Http.get().executeSafe(new HttpHost(host, port), req, new Extract200okBytes());
    }

}
