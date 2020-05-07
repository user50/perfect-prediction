package oleogin.ui;

import javafx.application.Application;
import javafx.stage.Stage;

public class Example extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        DesignV2 design = new DesignV2(primaryStage)
                .addProgressBarButton("run training", "/train")
                .addProgressBarButton("run test", "/test")
                .addShowImageButton("/training/0")
                .addShowImageButton("/training/1")
                .addShowImageButton("/test/1")
                .addShowImageButton("/test/0")
                .addShowImageButton("/test/undefined");

        primaryStage.show();
    }


}
