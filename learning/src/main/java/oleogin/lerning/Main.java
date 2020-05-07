package oleogin.lerning;

import oleogin.common.Pair;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.util.ModelSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {



    }

    private static void saveResults(String destinationDir, Stream<Pair<File, Double>> stream) throws IOException {
        FileUtils.deleteDirectory(new File(destinationDir));
        if (!new File(destinationDir).mkdir())
            throw new RuntimeException("Unable to create folder " + destinationDir);

        stream.forEach(p -> {
            File destination = new File(destinationDir + "\\" + p.getFirst().getName());

            try {
                Files.copy(p.getFirst().toPath(), destination.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }




}
