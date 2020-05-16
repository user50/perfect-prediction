package oleogin.lerning;

import io.javalin.Javalin;
import oleogin.common.MapBuilder;
import oleogin.common.Pair;
import oleogin.common.SerializationUtil;
import oleogin.repo.SimpleCRUDRepo;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static oleogin.common.SerializationUtil.writeObj;

public class HttpCRUDEndpointBuilder {

    private SimpleCRUDRepo crudRepo;
    private Class<?> idClass = String.class;
    private Map<String, Class<?>> filtrationFields = new HashMap<>();

    public HttpCRUDEndpointBuilder(SimpleCRUDRepo crudRepo) {
        this.crudRepo = crudRepo;
    }

    public HttpCRUDEndpointBuilder idClass(Class<?> idClass){
        this.idClass = idClass;

        return this;
    }

    public HttpCRUDEndpointBuilder addFiltrationField(String fieldName, Class<?> aClass){
        filtrationFields.put(fieldName, aClass);

        return this;
    }

    public <T> void create(Javalin app, String resource ){
        app.get(resource+"/:id", ctx -> {
            Object id = ctx.pathParam("id", idClass).get();

            Optional<?> result = crudRepo.get(id);

            if (result.isPresent())
                ctx.result(new ByteArrayInputStream(writeObj((Serializable) result.get())));
            else
                ctx.status(404);
        });

        app.get(resource, ctx -> {
            Predicate<MapBuilder<String, Object>> filter = filtrationFields.entrySet()
                    .stream()
                    .map(e -> new Pair<>(e.getKey(), ctx.queryParam(e.getKey(), e.getValue()).getOrNull()))
                    .filter(p -> p.getSecond() != null )
                    .<Predicate<MapBuilder<String, Object>>>map(stringPair -> mb -> mb.get(stringPair.getFirst()).equals(stringPair.getSecond()))
                    .reduce(t -> true, Predicate::and);

            ctx.result(new ByteArrayInputStream(writeObj((Serializable) crudRepo.list(filter))));
        });

        app.put(resource+"/:id", ctx -> {
            Object id = ctx.pathParam("id", idClass).get();
            MapBuilder<String,Object> obj = SerializationUtil.readObj(ctx.bodyAsBytes());

            crudRepo.put(id, obj);
        });

        app.delete(resource+"/:id", ctx -> {
            Object id = ctx.pathParam("id", idClass).get();

            crudRepo.delete(id);
        });


    }





}
