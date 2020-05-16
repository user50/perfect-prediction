package oleogin.ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import oleogin.common.MapBuilder;
import oleogin.common.SerializationUtil;
import oleogin.common.StreamBuilder;
import oleogin.http.GetRequest;
import oleogin.http.Http;
import oleogin.http.HttpCRUDRepo;
import oleogin.http.HttpReq;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


public class Controller {
    private String host = "localhost";
    private int port = 7000;
    private Function<String, List<ImageDatum>> imageRepo = Data.remoteImages(host, port);
    private HttpCRUDRepo trainingImagesRepo = new HttpCRUDRepo(host, port, "/training_samples");
    private HttpCRUDRepo testImagesRepo = new HttpCRUDRepo(host, port, "/test_samples");
    private HttpCRUDRepo rawImagesRepo = new HttpCRUDRepo(host, port, "/raw_samples");

    @FXML
    private VBox root;

    @FXML
    private VBox contentBox;

    @FXML
    private ProgressBar trainProgressBar;

    @FXML
    private ProgressBar testProgressBar;


    @FXML
    public void train(ActionEvent actionEvent){
        System.out.println(actionEvent.getEventType());

        new Thread(() -> Http.get().executeSafe(new GetRequest("http://localhost:7000/train"), httpResponse -> {
            try {
                InputStream is = httpResponse.getEntity().getContent();

                new StreamBuilder<>(() -> SerializationUtil.readInt(is))
                        .toStream()
                        .forEach(integer -> trainProgressBar.setProgress( (double)integer/100 ));

                trainProgressBar.setProgress(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        })).start();
    }

    @FXML
    public void test(ActionEvent actionEvent){

        System.out.println(actionEvent.getEventType().getName());

        new Thread(() -> Http.get().executeSafe(new GetRequest("http://localhost:7000/test"), httpResponse -> {
            try {
                InputStream is = httpResponse.getEntity().getContent();

                new StreamBuilder<>(() -> SerializationUtil.readInt(is))
                        .toStream()
                        .forEach(integer -> testProgressBar.setProgress( (double)integer/100 ));

                testProgressBar.setProgress(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return null;
        })).start();
    }

    @FXML
    public void showTrainImg0(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = trainingImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .filter(mb -> mb.get("class").equals(0))
                .collect(Collectors.toList());

        showTrainImages(images);
    }

    @FXML
    public void showTrainImg1(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = trainingImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .filter(mb -> mb.get("class").equals(1))
                .collect(Collectors.toList());

        showTrainImages(images);
    }

    @FXML
    public void randomImages(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = rawImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .collect(Collectors.toList());


        showTestImages(images);
    }

    @FXML
    public void showTestImg0(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = testImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .filter(mb -> mb.get("class").equals("0"))
                .collect(Collectors.toList());

        showTestImages(images);
    }

    @FXML
    public void showTestImg1(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = testImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .filter(mb -> mb.get("class").equals("1"))
                .collect(Collectors.toList());

        showTestImages(images);
    }

    @FXML
    public void showTestImgUndefined(ActionEvent actionEvent){
        List<MapBuilder<String,Object>> images = testImagesRepo.list()
                .stream()
                .peek(mb -> System.out.println(mb.build()))
                .filter(mb -> mb.get("class").equals("undefined"))
                .collect(Collectors.toList());

        showTestImages(images);
    }

    private void showTestImages(List<MapBuilder<String,Object>> images){
        contentBox.getChildren().clear();

        images.stream()
                .map(mb -> new ImageDatum(mb.<String>get("id"), "http://"+host + ":" + port + "/images/" + mb.<String>get("id")))
                .map(imageDatum -> Views.testImgView(imageDatum, this::addImageToTrain, root))
                .forEach(hBox -> contentBox.getChildren().add(hBox));
    }

    private void showTrainImages(List<MapBuilder<String,Object>> images){
        contentBox.getChildren().clear();

        images.stream()
                .map(mb -> new ImageDatum(mb.<String>get("id"), "http://"+host + ":" + port + "/images/" + mb.<String>get("id")))
                .map(imageDatum -> Views.trainingImgView(imageDatum, imageId -> trainingImagesRepo.delete(imageId) ))
                .forEach(hBox -> contentBox.getChildren().add(hBox));
    }

    private void addImageToTrain(int aClass, String imgId){
        trainingImagesRepo.put(imgId, new MapBuilder<String,Object>().put("id", imgId)
                .put("class", aClass)
        );
    }
}
