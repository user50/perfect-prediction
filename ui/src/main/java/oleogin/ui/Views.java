package oleogin.ui;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oleogin.http.Http;
import oleogin.http.HttpReq;

import java.io.ByteArrayInputStream;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class Views {


    public static HBox testImgView(ImageDatum image, BiConsumer<Integer,String> addImgToTrainSet, Pane root ){
        HBox hBox = new HBox();
        ImageView imageView = new ImageView(image.getUrl());
        imageView.setOnMouseClicked(event -> {
            byte[] attendedImg = HttpReq.get()
                    .uri("/explain/" + image.getId())
                    .ok200bytes("localhost", 7000)
                    .get();

            new ImagePopup(root,  new ImageView(new Image(new ByteArrayInputStream(attendedImg)))).show();
        });

        hBox.getChildren().add(imageView);

        Button button0 = new Button("add to 0");
        Button button1 = new Button("add to 1");
        VBox vBox = new VBox(button0, button1);

        button0.setOnMouseClicked(event -> {
            addImgToTrainSet.accept(0, image.getId());
            hBox.getChildren().remove(vBox);
        });

        button1.setOnMouseClicked(event -> {
            addImgToTrainSet.accept(1, image.getId());
            hBox.getChildren().remove(vBox);

        });

        hBox.getChildren().addAll(vBox);

        return hBox;
    }

    public static Pane trainingImgView(ImageDatum image, Consumer<String> deleteImage){
        HBox hBox = new HBox();
        ImageView imageView = new ImageView(image.getUrl());
        hBox.getChildren().add(imageView);

        Button button = new Button("delete");
        button.setOnMouseClicked(event -> {
            deleteImage.accept(image.getId());
            hBox.getChildren().remove(button);
        });

        hBox.getChildren().addAll(button);

        return hBox;
    }

}
