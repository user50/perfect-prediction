package oleogin.ui;

import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import oleogin.http.Http;
import oleogin.http.HttpReq;


public class Views {

    public static Pane simpleImgView(ImageDatum image){
        ImageView imageView = new ImageView(image.getUrl());
        HBox hBox = new HBox(imageView);
        hBox.setId("row");

        return hBox;
    }

    public static Pane testImgView(ImageDatum image, String host, int port){
        HBox hBox = new HBox();
        hBox.setId("row");
        ImageView imageView = new ImageView(image.getUrl());

        VBox buttonBox = new VBox();
        buttonBox.setId("buttonBox");

        Button button0 = new Button("add to 0");
        Button button1 = new Button("add to 1");

        button0.setOnMouseClicked(event -> {
            Http.get().put(host, port, "/training/0/"+image.getId(), image.getId().getBytes());
            buttonBox.getChildren().remove(button0);
            buttonBox.getChildren().remove(button1);
        });

        button1.setOnMouseClicked(event -> {
            Http.get().put(host, port, "/training/1/"+image.getId(), image.getId().getBytes());
            buttonBox.getChildren().remove(button0);
            buttonBox.getChildren().remove(button1);
        });


        buttonBox.getChildren().addAll(button0,button1);

        hBox.getChildren().addAll(imageView, buttonBox);

        return hBox;
    }

    public static Pane trainingImgView(ImageDatum image, String host, int port){
        HBox hBox = new HBox();
        hBox.setId("row");
        ImageView imageView = new ImageView(image.getUrl());

        VBox buttonBox = new VBox();
        buttonBox.setId("buttonBox");

        Button button = new Button("delete");

        button.setOnMouseClicked(event -> {
            HttpReq.delete()
                    .uri("/training/0/"+image.getId())
                    .ok200bytes(host, port);

            HttpReq.delete()
                    .uri("/training/1/"+image.getId())
                    .ok200bytes(host, port);

            buttonBox.getChildren().remove(button);
        });


        buttonBox.getChildren().addAll(button);

        hBox.getChildren().addAll(imageView, buttonBox);

        return hBox;
    }

}
