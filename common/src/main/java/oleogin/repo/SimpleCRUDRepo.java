package oleogin.repo;

import oleogin.common.MapBuilder;
import oleogin.common.SerializationUtil;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SimpleCRUDRepo {

    private List<MapBuilder<String,Object>> data;
    private String file;
    private String idField;

    public SimpleCRUDRepo(List<MapBuilder<String, Object>> data, String file, String idField) {
        this.data = data;
        this.file = file;
        this.idField = idField;
    }

    public static <T> SimpleCRUDRepo create(String file, String idField){
        File f = new File(file);
        if (!f.exists()){
            return new SimpleCRUDRepo(new ArrayList<>(), file, idField);

        }else {
            try(InputStream is = new FileInputStream(file)){
                List<MapBuilder<String, Object>> data = SerializationUtil.readObj(is);

                return new SimpleCRUDRepo(data, file, idField);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<MapBuilder<String,Object>> list(Predicate<MapBuilder<String,Object>> predicate){
        return data.stream()
                .filter(predicate)
                .collect(Collectors.toList());
    }

    public Optional<MapBuilder<String,Object>> get(Object id){
        List<MapBuilder<String, Object>> collect = data.stream()
                .filter(mb -> mb.get(idField).equals(id))
                .collect(Collectors.toList());

        if (collect.isEmpty())
            return Optional.empty();

        if (collect.size() == 1)
            return Optional.of(collect.get(0));

        throw new RuntimeException("Find "+collect.size()+" elements but need only one.");
    }

    public synchronized void delete(Object id){
        List<MapBuilder<String, Object>> collect = data.stream()
                .filter(mb -> !mb.get(idField).equals(id))
                .collect(Collectors.toList());

        if (collect.size() == data.size())
            return;

        this.data = collect;
        SerializationUtil.writeObj((Serializable) collect, file);
    }

    public synchronized void put(Object id, MapBuilder<String,Object> mb){
        Optional<MapBuilder<String, Object>> found = get(id);

        if (found.isPresent())
            throw new RuntimeException("Element with id "+id+" already exists");

        mb.put(idField, id);
        data.add(mb);
        SerializationUtil.writeObj((Serializable) data, file);
    }

    public synchronized void clear(){
        data = new ArrayList<>();
        new File(file).delete();
    }

}
