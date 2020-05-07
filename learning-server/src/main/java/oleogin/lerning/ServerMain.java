package oleogin.lerning;

import io.javalin.Javalin;
import oleogin.common.Pair;
import oleogin.common.SerializationUtil;
import oleogin.common.StreamBuilder;
import org.apache.commons.io.FileUtils;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ServerMain {

    public static void main(String[] args) {

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        String trainingDataFolder = "d:/anonimous";
        String allImagesFolder = "d:/train_data_set";
        String testDataFolder = "d:/anonimous_results";

        Javalin app = Javalin.create()
                .start(Integer.parseInt(args[0]));

        Function<String, byte[]> imageRepo = ImageRepositories.local(allImagesFolder);
        app.get("images/:imageId", ctx -> ctx.result(new ByteArrayInputStream(imageRepo.apply(ctx.pathParam("imageId")))));

        app.get("training/:class", ctx -> {
            String aClass = ctx.pathParam(":class");

            List<String> data = Arrays.stream(new File(trainingDataFolder + "/" + aClass).list())
                    .map(s -> "/images/" + s)
                    .collect(Collectors.toList());

            ctx.result(new ByteArrayInputStream(SerializationUtil.writeObj((Serializable) data)));
        });

        app.put("training/:class/:id", ctx -> {
            String aClass = ctx.pathParam(":class");
            String imageId = ctx.pathParam(":id");

            try(FileOutputStream os = new FileOutputStream(trainingDataFolder + "/" + aClass + "/" + imageId)){
                os.write(imageRepo.apply(imageId));
            }
        });

        app.delete("training/:class/:id", ctx -> {
            String aClass = ctx.pathParam(":class");
            String imageId = ctx.pathParam(":id");

            new File(trainingDataFolder + "/" + aClass + "/" + imageId).delete();
        });

        app.get("test/:class", ctx -> {
            String aClass = ctx.pathParam(":class");

            List<String> data = Arrays.stream(new File(testDataFolder + "/" + aClass).list())
                    .map(s -> "/images/" + s)
                    .collect(Collectors.toList());

            ctx.result(new ByteArrayInputStream(SerializationUtil.writeObj((Serializable) data)));
        });

        Function<String, INDArray> reshapedActivation49 = Activation49.create(imageRepo)
                .andThen(indArray -> indArray.reshape(1, 2048, 7 * 7));

        app.get("train", ctx -> {
            AtomicInteger iterationDone = new AtomicInteger(0);
            HttpServletResponse res = ctx.res;
            ServletOutputStream os = res.getOutputStream();

            List<DataSet> dataSets = Arrays.stream(new File(trainingDataFolder).listFiles())
                    .flatMap(folder -> Arrays.stream(folder.listFiles())
                            .map(File::getName)
                            .map(reshapedActivation49)
                            .map(indArray -> new DataSet(indArray, Nd4j.create(new double[]{Double.parseDouble(folder.getName())}).reshape(1, 1)))
                    )
                    .collect(Collectors.toList());

            ComputationGraph model = Models.getAttentionModel();

            new StreamBuilder<>(() -> Optional.of(dataSets))
                    .mapStream(listStream -> listStream
                            .peek(Collections::shuffle)
                            .flatMap(Collection::stream)
                    )
                    .miniBatch(8)
                    .limit(1000)
                    .peek(ds -> {
                        try {
                            os.write(SerializationUtil.int2bytes(iterationDone.incrementAndGet() * 100 / 1000));
                            os.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .map(DataSet::merge)
                    .toStream()
                    .forEach(dataSet -> model.fit(dataSet));

            ModelSerializer.writeModel(model, new File("mode.zip"), false);

            os.close();
        });

        app.get("test", ctx -> {
            int limit = 200;

            AtomicInteger handled = new AtomicInteger(0);
            HttpServletResponse res = ctx.res;
            ServletOutputStream os = res.getOutputStream();

            ComputationGraph model = Models.getAttentionModel();

            Set<String> trainingFiles = Arrays.stream(new File(trainingDataFolder).listFiles())
                    .flatMap(file -> Arrays.stream(file.listFiles()))
                    .map(file -> file.getName())
                    .collect(Collectors.toSet());

            List<Pair<File, Double>> collect = new StreamBuilder<>(Arrays.stream(new File(allImagesFolder).listFiles()))
                    .mapStream(fileStream -> fileStream.filter(file -> !trainingFiles.contains(file.getName())))
                    .shuffle()
                    .limit(limit)
                    .map(file -> new Pair<>(reshapedActivation49.apply(file.getName()), file))
                    .map(pair -> {
                        double average = IntStream.range(0, 20)
                                .mapToDouble(operand -> model.output(true, pair.getFirst())[0].getDouble(0, 0))
                                .summaryStatistics()
                                .getAverage();

                        return new Pair<>(pair.getSecond(), average);
                    })
                    .peek(p -> {
                        try {
                            os.write(SerializationUtil.int2bytes( handled.incrementAndGet() * 100 / limit ));
                            os.flush();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    } )
                    .toStream()
                    .collect(Collectors.toList());

            saveResults(testDataFolder+"/1", collect.stream().filter(p -> p.getSecond() > 0.9));
            saveResults(testDataFolder+"/0", collect.stream().filter(p -> p.getSecond() < 0.1));
            saveResults(testDataFolder+"/undefined", collect.stream().filter(p -> Math.abs(p.getSecond() - 0.5) < 0.10));

            os.close();
        });
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
