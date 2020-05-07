package oleogin.lerning;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.Function;

public class ImageRepositories {

    public static Function<String,byte[]> local(String allImagesFolder){

        return s -> {
            try {
                return Files.readAllBytes(new File(allImagesFolder+"/"+s).toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
