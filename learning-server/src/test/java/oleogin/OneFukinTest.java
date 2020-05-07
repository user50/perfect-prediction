package oleogin;

import oleogin.common.SerializationUtil;
import oleogin.common.StreamBuilder;
import oleogin.http.GetRequest;
import oleogin.http.Http;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class OneFukinTest {

    @Test
    public void name() throws IOException {
        Http.get().execute(new GetRequest("http://localhost:7000/train"), httpResponse -> {
            try {
                InputStream is = httpResponse.getEntity().getContent();

                new StreamBuilder<>(() -> SerializationUtil.readInt(is))
                        .toStream()
                        .forEach(integer -> System.out.println(integer));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        });


    }
}
