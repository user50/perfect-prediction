package oleogin.ui;

import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import oleogin.common.FunctionBuilder;
import oleogin.common.Pair;
import oleogin.common.SerializationUtil;
import oleogin.http.Http;
import oleogin.http.HttpService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Data {

    public static Function<Integer, List<HBox>> localFolderImages(String folder){
        return new FunctionBuilder<>(Function.<Pair<Integer, Integer>>identity())
                .andThen(offsetLimit -> Arrays.stream(new File(folder).listFiles())
                        .skip(offsetLimit.getFirst())
                        .limit(offsetLimit.getSecond())
                        .map(file -> {
                            try {
                                Image image = new Image(new FileInputStream(file));
                                ImageView imageView = new ImageView(image);

                                Label label = new Label(file.getName(), imageView);

                                HBox hBox = new HBox(label);
                                hBox.setId("row");

                                return hBox;
                            } catch (FileNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .collect(Collectors.toList())
                )
                .<Integer, List<HBox>>wrap(fun -> {
                    AtomicInteger offset = new AtomicInteger(0);

                    return limit -> {
                        List<HBox> boxes = fun.apply(new Pair<>(offset.get(), limit));
                        offset.addAndGet(boxes.size());

                        return boxes;
                    };
                })
                .build();
    }

    public static Function<String,List<ImageDatum>> remoteImages(String host, int port){
        String trainingServerHost = "http://"+host+":"+port;

         return new FunctionBuilder<>(Function.<String>identity())
                .andThen(() -> {
                    HttpService http = Http.get();

                    return resource -> http.read200okBytes(trainingServerHost + resource).get();
                })
                .andThen(SerializationUtil::<List<String>>readObj)
                .andThen(strings -> strings.stream()
                        .map(imageResource -> {
                            String imageId = imageResource.split("/")[imageResource.split("/").length - 1];
                            return new ImageDatum(imageId, trainingServerHost + imageResource);
                        })
                        .collect(Collectors.toList())
                )
                .build();
    }

}
