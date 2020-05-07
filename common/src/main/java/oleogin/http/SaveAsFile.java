package oleogin.http;

import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class SaveAsFile implements HttpResponseHandler<String> {

    private String fileName;
    private boolean ignoreNotFound;
    private boolean rewrite;

    public SaveAsFile(String fileName, boolean ignoreNotFound, boolean rewrite) {
        this.fileName = fileName;
        this.ignoreNotFound = ignoreNotFound;
        this.rewrite = rewrite;
    }

    @Override
    public String handle(CloseableHttpResponse httpResponse) {
        if (ignoreNotFound && httpResponse.getStatusLine().getStatusCode() == 404 )
            return fileName;

        if (httpResponse.getStatusLine().getStatusCode() != 200 )
            throw new RuntimeException("Received code: "+httpResponse.getStatusLine().getStatusCode());

        try {
            if (rewrite)
                new File(fileName).delete();

            InputStream is = httpResponse.getEntity().getContent();

            Files.copy(is, new File(fileName).toPath());

            is.close();

            return fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
