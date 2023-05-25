package id.npad93.p10;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) {
        db = new AbsenceManager("absen.sqlite3");
        SceneCollection scenes = new SceneCollection(primaryStage, db, 800, 600);

        // Initialisasi Stage
        primaryStage.setScene(scenes.getMainScene());
        primaryStage.setTitle("Absenshie");
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (db != null) {
            db.close();
        }
    }

    private AbsenceManager db;
}
