package oleogin.http;

import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class MultiThreadHttpServiceProvider {

    private static HttpService INSTANCE;
    public HttpService newInstance() {
        return newInstance(2);
    }

    public HttpService newInstance(int maxPerRout){
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager();

        manager.setDefaultMaxPerRoute(maxPerRout);

        return new HttpService(
                HttpClients.custom()
                .setConnectionManager(manager)
                .build());
    }

    public HttpService singleton(){
        if (INSTANCE == null)
            INSTANCE = newInstance();

        return INSTANCE;
    }

}
