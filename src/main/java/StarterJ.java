import javafx.application.Application;
import javafx.stage.Stage;

public class StarterJ extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Application.launch(StarterK.class);
    }

    public static void main(String[] args) {
        StarterK.main(args);
    }
}
