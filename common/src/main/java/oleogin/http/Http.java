package oleogin.http;

public class Http {

    public static HttpService get(){
        return new MultiThreadHttpServiceProvider().singleton();
    }
}
