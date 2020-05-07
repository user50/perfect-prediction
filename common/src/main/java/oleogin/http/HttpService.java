package oleogin.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

public class HttpService {

    private CloseableHttpClient httpClient;

    public HttpService(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public <T> T execute(HttpRequestProvider httpRequestProvider, HttpResponseHandler<T> responseHandler) throws IOException {

        HttpRequestBase httpRequest = httpRequestProvider.getRequest();

        try (CloseableHttpResponse response = httpClient.execute(httpRequestProvider.getHost(), httpRequest)) {

            return responseHandler.handle(response);
        }
    }

    public <T> T executeSafe(HttpRequestProvider httpRequestProvider, HttpResponseHandler<T> responseHandler)  {
        HttpRequestBase httpRequest = httpRequestProvider.getRequest();

        try (CloseableHttpResponse response = httpClient.execute(httpRequestProvider.getHost(), httpRequest)) {

            return responseHandler.handle(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T executeSafe(HttpHost host, HttpRequestBase req, HttpResponseHandler<T> responseHandler)  {

        try (CloseableHttpResponse response = httpClient.execute(host, req)) {

            return responseHandler.handle(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String wGet(String url, boolean ignoreNotFound, boolean rewrite){
        try {
            URI uri = new URI(url);
            String path = uri.getPath()+(uri.getRawQuery() != null ? "?"+uri.getRawQuery() : "");
            String host = uri.getHost();
            int port = uri.getPort();

            String[] split = url.split("/");

            return execute(new GetRequest(host, port, path), new SaveAsFile(split[split.length-1], ignoreNotFound, rewrite));
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String wGet(String url, boolean ignoreNotFound){
        return wGet(url, ignoreNotFound, true);
    }

    public String wGet(String url){
        return wGet(url, false, true);
    }

    public String put(String host, int port, String resource, byte[] body){

        try {
            return execute(new PutByteReq(host, port, resource, body), new ExtractAsStringHttpResHandler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T post(String url, Object body, TypeReference<T> typeReference ){
        try {
            String execute = execute(new PostJsonRequest(url, body), new ExtractAsStringHttpResHandler());
            return new ObjectMapper().readValue(execute, typeReference);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void wPutFile(String url, String fileName){
        try {
            URI uri = new URI(url);
            String path = uri.getPath()+(uri.getRawQuery() != null ? "?"+uri.getRawQuery() : "");
            String host = uri.getHost();
            int port = uri.getPort();


            Integer statusCode = execute(new PutFileRequest(host, port, path, fileName), httpResponse -> httpResponse.getStatusLine().getStatusCode());

            if (statusCode!= 200)
                throw new RuntimeException("Received status code: "+statusCode);

        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    public Optional<byte[]> read200okBytes(String url){
        try {
            URI uri = new URI(url);
            String path = uri.getPath()+(uri.getRawQuery() != null ? "?"+uri.getRawQuery() : "");
            String host = uri.getHost();
            int port = uri.getPort();

            return execute(new GetRequest(host, port, path), new Extract200okBytes());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public <A> Optional<A> read200okResponse(String url, TypeReference<A> typeReference){
        Optional<byte[]> bytes = read200okBytes(url);

        if (!bytes.isPresent())
            return Optional.empty();

        try {
            return Optional.of(new ObjectMapper().readValue(new String(bytes.get()),  typeReference ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
