package oleogin.http;


import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class Extract200okBytes implements HttpResponseHandler<Optional<byte[]>> {
    @Override
    public Optional<byte[]> handle(CloseableHttpResponse httpResponse) {
        if (httpResponse.getStatusLine().getStatusCode() != 200 )
            return Optional.empty();

        try {
            InputStream is = httpResponse.getEntity().getContent();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, nRead);

            buffer.flush();

            return Optional.of(buffer.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
