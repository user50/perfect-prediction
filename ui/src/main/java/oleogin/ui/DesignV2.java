package oleogin.ui;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import oleogin.common.SerializationUtil;
import oleogin.common.StreamBuilder;
import oleogin.http.GetRequest;
import oleogin.http.Http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.function.Function;

public class DesignV2 {
    private VBox root;
    private HBox head;

    private ScrollPane scrollPane;
    private HBox topVertical;
    private VBox contentBox;
    private Scene scene;

    public DesignV2(Stage primaryStage) {
        root = new VBox();
        root.setId("root");

        head = new HBox();
        head.setId("head");
        root.getChildren().add(head);

        scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        root.getChildren().add(scrollPane);

        topVertical = new HBox();
        topVertical.setId("topBlock");

        contentBox = new VBox();
        contentBox.setId("contentBox");
        topVertical.getChildren().add(contentBox);

        scrollPane.setContent(topVertical);

        scene = new Scene(root);
        scene.getStylesheets().add("/stylesV2.css");
        primaryStage.setScene(scene);
    }

    public DesignV2 addShowImageButton(String imageLocation){

        Button button = new Button(imageLocation);

        button.setOnAction(action -> {

            Function<String, List<Pane>> imageRepo = Data.remoteImages("localhost", 7000);

            contentBox.getChildren().clear();
            contentBox.getChildren().addAll(imageRepo.apply(imageLocation));
        });

        HBox hBox = new HBox();
        hBox.getChildren().addAll( button);

        head.getChildren().addAll(hBox);

        return this;
    }

    public DesignV2 addProgressBarButton(String hint, String location){

        ProgressBar progressBar = new ProgressBar(0);
        Button button = new Button(hint);

        button.setOnMouseClicked(event -> {
            new Thread(() -> Http.get().executeSafe(new GetRequest("http://localhost:7000"+location), httpResponse -> {
                try {
                    InputStream is = httpResponse.getEntity().getContent();

                    new StreamBuilder<>(() -> SerializationUtil.readInt(is))
                            .toStream()
                            .forEach(integer -> progressBar.setProgress( (double)integer/100 ));

                    progressBar.setProgress(0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                return null;
            })).start();

        });

        VBox vBox = new VBox();
        vBox.setId("trainingBox");
        vBox.getChildren().addAll(button, progressBar);

        head.getChildren().addAll(vBox);

        return this;
    }

    public VBox getRoot() {
        return root;
    }

    public HBox getHead() {
        return head;
    }

    public ScrollPane getScrollPane() {
        return scrollPane;
    }

    public HBox getTopVertical() {
        return topVertical;
    }

    public VBox getContentBox() {
        return contentBox;
    }

    public Scene getScene() {
        return scene;
    }
}
