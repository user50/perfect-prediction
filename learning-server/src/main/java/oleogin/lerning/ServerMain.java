package oleogin.lerning;

import io.javalin.Javalin;
import oleogin.common.MapBuilder;
import oleogin.common.Pair;
import oleogin.common.SerializationUtil;
import oleogin.common.StreamBuilder;
import oleogin.repo.SimpleCRUDRepo;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ServerMain {

    public static void main(String[] args) {

        Nd4j.getMemoryManager().setAutoGcWindow(10000);

        Javalin app = Javalin.create()
                .start(Integer.parseInt(args[0]));

        String allImagesFolder = "d:/train_data_set";
        Function<String, byte[]> imageRepo = ImageRepositories.local(allImagesFolder);

        SimpleCRUDRepo trainingRepo = SimpleCRUDRepo.create("training_dataset.bin", "id");
        SimpleCRUDRepo testRepo = SimpleCRUDRepo.create("test_data.bin", "id");

        Function<String, INDArray> activation49 = Activation49.create(imageRepo);
        Function<String, INDArray> reshapedActivation49 = activation49.andThen(indArray -> indArray.reshape(1, 2048, 7 * 7));

        new HttpCRUDEndpointBuilder(trainingRepo)
                .idClass(String.class)
                .addFiltrationField("class", Integer.class)
                .create(app, "/training_samples");


        new HttpCRUDEndpointBuilder(testRepo)
                .idClass(String.class)
                .addFiltrationField("class", Integer.class)
                .create(app, "/test_samples");


        app.get("images/:imageId", ctx -> ctx.result(new ByteArrayInputStream(imageRepo.apply(ctx.pathParam("imageId")))));

        app.get("explain/:imageId", ctx -> {
            ComputationGraph model = Models.getAttentionModel();

            Function<String, byte[]> function = new AttentedRegionsFunProvider().get(activation49, model, imageRepo);

            ctx.result(new ByteArrayInputStream(function.apply(ctx.pathParam("imageId"))));
        });


        app.get("/raw_samples", ctx -> {
            List<String> toExclude = trainingRepo.list(stringObjectMapBuilder -> true).stream()
                    .map(mb -> mb.<String>get("id"))
                    .collect(Collectors.toList());

            List<MapBuilder<Object, Object>> result = Arrays.asList(new File(allImagesFolder).list())
                    .stream()
                    .filter(s -> !toExclude.contains(s))
                    .map(s -> new MapBuilder<>().put("id", s))
                    .limit(200)
                    .collect(Collectors.toList());

            ctx.result(new ByteArrayInputStream(SerializationUtil.writeObj((Serializable) result)));
        });



        app.get("train", ctx -> {
            AtomicInteger iterationDone = new AtomicInteger(0);
            HttpServletResponse res = ctx.res;
            ServletOutputStream os = res.getOutputStream();

            List<DataSet> dataSets = trainingRepo.list(mb -> true).stream()
                    .map(mb -> {
                        INDArray features = reshapedActivation49.apply(mb.get("id"));
                        INDArray labels = Nd4j.create(new double[]{mb.<Integer>get("class").doubleValue()}).reshape(1,1);
                        return new DataSet(features, labels);
                    })
                    .collect(Collectors.toList());

            ComputationGraph model = Models.getAttentionModel();

            int limit = 5000;
            new StreamBuilder<>(() -> Optional.of(dataSets))
                    .mapStream(listStream -> listStream
                            .peek(Collections::shuffle)
                            .flatMap(Collection::stream)
                    )
                    .miniBatch(8)
                    .limit(limit)
                    .peek(ds -> {
                        try {
                            os.write(SerializationUtil.int2bytes(iterationDone.incrementAndGet() * 100 / limit));
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

            Set<String> trainingFiles = trainingRepo.list(mb -> true ).stream().map(mb -> mb.<String>get("id")).collect(Collectors.toSet());

            List<Pair<File, Double>> collect = new StreamBuilder<>(Arrays.stream(new File(allImagesFolder).listFiles()))
                    .mapStream(fileStream -> fileStream.filter(file -> !trainingFiles.contains(file.getName())))
                    .shuffle()
                    .limit(limit)
                    .map(file -> new Pair<>(reshapedActivation49.apply(file.getName()), file))
                    .map(pair -> {
                        double average = IntStream.range(0, 20)
                                .mapToDouble(operand -> model.feedForward(pair.getFirst(), true).get("output").getDouble(0,0))
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



            testRepo.clear();


            collect.stream().filter(p -> p.getSecond() > 0.9)
                    .forEach(f -> testRepo.put(f.getFirst().getName(), new MapBuilder<String,Object>().put("id", f.getFirst().getName())
                                .put("class", "1")
                    ) );

            collect.stream().filter(p -> p.getSecond() < 0.1)
                    .forEach(f -> testRepo.put(f.getFirst().getName(), new MapBuilder<String,Object>().put("id", f.getFirst().getName())
                            .put("class", "0")
                    ) );

            collect.stream().filter(p -> Math.abs(p.getSecond() - 0.5) < 0.10)
                    .forEach(f -> testRepo.put(f.getFirst().getName(), new MapBuilder<String,Object>().put("id", f.getFirst().getName())
                            .put("class", "undefined")
                    ) );

            os.close();
        });
    }

}
