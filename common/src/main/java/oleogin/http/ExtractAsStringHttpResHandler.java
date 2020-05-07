package oleogin.http;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.Scanner;

public class ExtractAsStringHttpResHandler implements HttpResponseHandler<String> {
    @Override
    public String handle(CloseableHttpResponse httpResponse) {
        try {
            java.util.Scanner s = new Scanner(httpResponse.getEntity().getContent()).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
