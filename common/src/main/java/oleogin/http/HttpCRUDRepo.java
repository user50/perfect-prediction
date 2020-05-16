package oleogin.http;

import oleogin.common.MapBuilder;
import oleogin.common.SerializationUtil;

import java.util.List;
import java.util.Optional;

public class HttpCRUDRepo {

    private String host;
    private int port;
    private String resource;

    public HttpCRUDRepo(String host, int port, String resource) {
        this.host = host;
        this.port = port;
        this.resource = resource;
    }

    public Optional<MapBuilder<String,Object>> get(String id){
        return HttpReq.get()
                .uri(resource+"/"+id)
                .ok200bytes(host, port)
                .map(SerializationUtil::readObj);
    }

    public Optional<MapBuilder<String,Object>> list(String id){
        return HttpReq.get()
                .uri(resource+"/"+id)
                .ok200bytes(host, port)
                .map(SerializationUtil::readObj);
    }

    public List<MapBuilder<String,Object>> list(){
        return HttpReq.get()
                .uri(resource)
                .ok200bytes(host, port)
                .map(SerializationUtil::<List<MapBuilder<String,Object>>>readObj)
                .orElseThrow(() -> new RuntimeException("GET "+resource+" got not 200ok code"));
    }

    public void put(String id, MapBuilder<String,Object> body){
        HttpReq.put()
                .uri(resource+"/"+id)
                .body(SerializationUtil.writeObj(body) )
                .ok200bytes(host, port);
    }

    public void delete(String id){
        HttpReq.delete()
                .uri(resource+"/"+id)
                .ok200bytes(host, port);
    }




}
