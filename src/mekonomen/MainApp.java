package mekonomen;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import mekonomen.repo.Inventory;
import mekonomen.repo.MemberRegistry;
import mekonomen.service.DataStore;
import mekonomen.service.MembershipService;
import mekonomen.service.RentalService;
import mekonomen.ui.UiFactory;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        DataStore store = new DataStore("data");

        MemberRegistry memberRegistry = new MemberRegistry();
        Inventory inventory = new Inventory();

        MembershipService membershipService = new MembershipService(memberRegistry);
        RentalService rentalService = new RentalService(inventory, memberRegistry);

        store.loadAll(memberRegistry, inventory, rentalService);

        UiFactory ui = new UiFactory(memberRegistry, inventory, membershipService, rentalService, store);

        Scene scene = new Scene(ui.createRoot(), 1100, 700);
        stage.setTitle("Mekonomen – Uthyrningssystem");
        stage.setScene(scene);
        stage.show();

        stage.setOnCloseRequest(e -> store.saveAll(memberRegistry, inventory, rentalService));
    }

    public static void main(String[] args) {
        launch(args);
    }
}