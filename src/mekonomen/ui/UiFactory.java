package mekonomen.ui;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import mekonomen.model.*;
import mekonomen.repo.Inventory;
import mekonomen.repo.MemberRegistry;
import mekonomen.service.DataStore;
import mekonomen.service.MembershipService;
import mekonomen.service.RentalService;

import java.time.LocalDate;
import java.util.List;

public class UiFactory {

    private final MemberRegistry memberRegistry;
    private final Inventory inventory;
    private final MembershipService membershipService;
    private final RentalService rentalService;
    private final DataStore store;

    private final ObservableList<Member> membersObs = FXCollections.observableArrayList();
    private final ObservableList<Item> itemsObs = FXCollections.observableArrayList();
    private final ObservableList<Rental> rentalsObs = FXCollections.observableArrayList();

    private final Label status = new Label("");

    public UiFactory(MemberRegistry memberRegistry, Inventory inventory,
                     MembershipService membershipService, RentalService rentalService,
                     DataStore store) {
        this.memberRegistry = memberRegistry;
        this.inventory = inventory;
        this.membershipService = membershipService;
        this.rentalService = rentalService;
        this.store = store;

        refreshAll();
    }

    public Parent createRoot() {
        BorderPane root = new BorderPane();

        TabPane tabs = new TabPane();
        tabs.getTabs().add(new Tab("Medlemmar", createMembersTab()));
        tabs.getTabs().add(new Tab("Objekt", createItemsTab()));
        tabs.getTabs().add(new Tab("Uthyrningar", createRentalsTab()));
        tabs.getTabs().add(new Tab("Summering", createSummaryTab()));

        for (Tab t : tabs.getTabs()) t.setClosable(false);

        root.setCenter(tabs);

        HBox bottom = new HBox(10);
        bottom.setPadding(new Insets(8));

        Button saveBtn = new Button("Spara nu");
        Button exitBtn = new Button("Avsluta");

        saveBtn.setOnAction(e -> {
            try {
                store.saveAll(memberRegistry, inventory, rentalService);
                setStatus("Sparat till fil.");
            } catch (Exception ex) {
                showError("Kunde inte spara", ex.getMessage());
            }
        });

        exitBtn.setOnAction(e -> {
            try {
                store.saveAll(memberRegistry, inventory, rentalService);
            } finally {
                Platform.exit();
            }
        });

        bottom.getChildren().addAll(saveBtn, exitBtn, new Separator(), status);
        root.setBottom(bottom);

        return root;
    }


    private Parent createMembersTab() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        TableView<Member> table = new TableView<Member>();
        table.setItems(membersObs);

        TableColumn<Member, String> cId = new TableColumn<Member, String>("ID");
        cId.setCellValueFactory(m -> new SimpleStringProperty(m.getValue().getId().substring(0, 8)));
        cId.setPrefWidth(90);

        TableColumn<Member, String> cName = new TableColumn<Member, String>("Namn");
        cName.setCellValueFactory(m -> new SimpleStringProperty(m.getValue().getFullName()));
        cName.setPrefWidth(200);

        TableColumn<Member, String> cPhone = new TableColumn<Member, String>("Telefon");
        cPhone.setCellValueFactory(new PropertyValueFactory<Member, String>("phone"));
        cPhone.setPrefWidth(140);

        TableColumn<Member, String> cLevel = new TableColumn<Member, String>("Level");
        cLevel.setCellValueFactory(m -> new SimpleStringProperty(m.getValue().getLevel().name()));
        cLevel.setPrefWidth(120);

        TableColumn<Member, String> cAddress = new TableColumn<Member, String>("Adress");
        cAddress.setCellValueFactory(new PropertyValueFactory<Member, String>("address"));
        cAddress.setPrefWidth(350);

        table.getColumns().addAll(cId, cName, cPhone, cLevel, cAddress);

        TextField search = new TextField();
        search.setPromptText("Sök medlem (namn/telefon/id) ...");

        FilteredList<Member> filtered = new FilteredList<Member>(membersObs, m -> true);
        search.textProperty().addListener((obs, oldV, newV) -> {
            String q = newV == null ? "" : newV.trim().toLowerCase();
            filtered.setPredicate(m -> {
                if (q.isEmpty()) return true;
                return safe(m.getFullName()).toLowerCase().contains(q)
                        || safe(m.getPhone()).toLowerCase().contains(q)
                        || safe(m.getId()).toLowerCase().contains(q);
            });
        });
        table.setItems(filtered);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        TextField first = new TextField();
        TextField last = new TextField();
        TextField phone = new TextField();
        TextField address = new TextField();
        ComboBox<MemberLevel> level = new ComboBox<MemberLevel>(FXCollections.observableArrayList(MemberLevel.values()));
        level.setValue(MemberLevel.STANDARD);

        form.addRow(0, new Label("Förnamn"), first, new Label("Efternamn"), last);
        form.addRow(1, new Label("Telefon"), phone, new Label("Adress"), address);
        form.addRow(2, new Label("Level"), level);

        Button add = new Button("Lägg till");
        Button update = new Button("Uppdatera vald");
        Button remove = new Button("Ta bort vald");
        Button history = new Button("Visa historik");
        Button clear = new Button("Rensa");

        clear.setOnAction(e -> {
            clearFields(first, last, phone, address);
            level.setValue(MemberLevel.STANDARD);
            table.getSelectionModel().clearSelection();
            first.requestFocus();
            setStatus("Formuläret rensat.");
        });

        HBox buttons = new HBox(8, add, update, remove, history, clear);

        add.setOnAction(e -> {
            try {
                Member m = new Member(
                        first.getText(),
                        last.getText(),
                        phone.getText(),
                        address.getText(),
                        level.getValue()
                );

                membershipService.registerMember(m);
                refreshAll();
                setStatus("Medlem tillagd: " + m.getFullName());


                clearFields(first, last, phone, address);
                level.setValue(MemberLevel.STANDARD);


                table.getSelectionModel().clearSelection();


                first.requestFocus();

            } catch (Exception ex) {
                showError("Kunde inte lägga till medlem", ex.getMessage());
            }
        });

        update.setOnAction(e -> {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Välj en medlem i listan."); return; }
            try {
                membershipService.updateMember(sel, first.getText(), last.getText(), phone.getText(), address.getText(), level.getValue());
                refreshAll();
                setStatus("Medlem uppdaterad: " + sel.getFullName());
            } catch (Exception ex) {
                showError("Kunde inte uppdatera medlem", ex.getMessage());
            }
        });

        remove.setOnAction(e -> {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Välj en medlem i listan."); return; }
            boolean ok = memberRegistry.removeById(sel.getId());
            if (ok) {
                refreshAll();
                setStatus("Medlem borttagen.");
            }
        });

        history.setOnAction(e -> {
            Member sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Välj en medlem i listan."); return; }
            List<String> h = sel.getHistory();
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setTitle("Historik");
            a.setHeaderText(sel.getFullName());
            a.setContentText(h.isEmpty() ? "(ingen historik)" : String.join("\n", h));
            a.showAndWait();
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel == null) return;
            first.setText(sel.getFirstName());
            last.setText(sel.getLastName());
            phone.setText(sel.getPhone());
            address.setText(sel.getAddress());
            level.setValue(sel.getLevel());
        });

        VBox top = new VBox(8, search);
        VBox right = new VBox(10, new Label("Skapa / ändra medlem"), form, buttons);
        right.setPadding(new Insets(0, 0, 0, 10));

        pane.setTop(top);
        pane.setCenter(table);
        pane.setRight(right);

        return pane;
    }


    private Parent createItemsTab() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        TableView<Item> table = new TableView<Item>();
        table.setItems(itemsObs);

        TableColumn<Item, String> cId = new TableColumn<Item, String>("ID");
        cId.setCellValueFactory(it -> new SimpleStringProperty(it.getValue().getId().substring(0, 8)));
        cId.setPrefWidth(90);

        TableColumn<Item, String> cType = new TableColumn<Item, String>("Typ");
        cType.setCellValueFactory(it -> new SimpleStringProperty(it.getValue().getType()));
        cType.setPrefWidth(150);

        TableColumn<Item, String> cName = new TableColumn<Item, String>("Namn");
        cName.setCellValueFactory(new PropertyValueFactory<Item, String>("name"));
        cName.setPrefWidth(240);

        TableColumn<Item, String> cPrice = new TableColumn<Item, String>("Pris/dag");
        cPrice.setCellValueFactory(it -> new SimpleStringProperty(String.valueOf(it.getValue().getPricePerDay())));
        cPrice.setPrefWidth(100);

        TableColumn<Item, String> cAvail = new TableColumn<Item, String>("Tillgänglig");
        cAvail.setCellValueFactory(it -> new SimpleStringProperty(it.getValue().isAvailable() ? "Ja" : "Nej"));
        cAvail.setPrefWidth(110);

        TableColumn<Item, String> cDetails = new TableColumn<Item, String>("Detaljer");
        cDetails.setCellValueFactory(it -> new SimpleStringProperty(it.getValue().getDetails()));
        cDetails.setPrefWidth(360);

        table.getColumns().addAll(cId, cType, cName, cPrice, cAvail, cDetails);

        TextField search = new TextField();
        search.setPromptText("Sök objekt (namn/detaljer) ...");

        ComboBox<String> typeFilter = new ComboBox<String>(FXCollections.observableArrayList(
                "Alla", "Biltillbehör", "Bilvård"
        ));
        typeFilter.setValue("Alla");

        CheckBox onlyAvailable = new CheckBox("Endast tillgängliga");

        FilteredList<Item> filtered = new FilteredList<Item>(itemsObs, it -> true);
        Runnable applyFilter = () -> {
            String q = search.getText() == null ? "" : search.getText().trim().toLowerCase();
            String t = typeFilter.getValue();
            boolean availOnly = onlyAvailable.isSelected();

            filtered.setPredicate(it -> {
                if (availOnly && !it.isAvailable()) return false;
                if (t != null && !"Alla".equals(t) && !it.getType().equals(t)) return false;
                if (q.isEmpty()) return true;
                return safe(it.getName()).toLowerCase().contains(q)
                        || safe(it.getDetails()).toLowerCase().contains(q);
            });
        };

        search.textProperty().addListener((o, a, b) -> applyFilter.run());
        typeFilter.valueProperty().addListener((o, a, b) -> applyFilter.run());
        onlyAvailable.selectedProperty().addListener((o, a, b) -> applyFilter.run());

        table.setItems(filtered);

        GridPane form = new GridPane();
        form.setHgap(8);
        form.setVgap(8);
        form.setPadding(new Insets(10));

        ComboBox<String> type = new ComboBox<String>(FXCollections.observableArrayList(
                "Biltillbehör", "Bilvård"
        ));
        type.setValue("Biltillbehör");

        TextField name = new TextField();
        TextField price = new TextField();
        TextField f1 = new TextField();
        TextField f2 = new TextField();

        Label f1Label = new Label("Tillbehörstyp");
        Label f2Label = new Label("Passar");

        type.valueProperty().addListener((obs, old, val) -> {
            if ("Biltillbehör".equals(val)) {
                f1Label.setText("Tillbehörstyp");
                f2Label.setText("Passar");
            }
            if ("Bilvård".equals(val)) {
                f1Label.setText("Rengöringstyp");
                f2Label.setText("Notering");
            }
        });

        form.addRow(0, new Label("Typ"), type);
        form.addRow(1, new Label("Namn"), name);
        form.addRow(2, new Label("Pris/dag"), price);
        form.addRow(3, f1Label, f1);
        form.addRow(4, f2Label, f2);

        Button add = new Button("Lägg till");
        Button update = new Button("Uppdatera vald");
        Button remove = new Button("Ta bort vald");
        Button clear = new Button("Rensa");

        HBox rightButtons = new HBox(8, add, update, remove, clear);

        clear.setOnAction(e -> {
            clearFields(name, price, f1, f2);


            type.setValue("Biltillbehör");


            table.getSelectionModel().clearSelection();


            name.requestFocus();

            setStatus("Objekt-formuläret rensat.");
        });


        add.setOnAction(e -> {
            try {
                double p = Double.parseDouble(price.getText().trim());
                Item it;

                if ("Biltillbehör".equals(type.getValue())) {
                    it = new CarAppliance(name.getText(), p, f1.getText(), f2.getText());
                } else {
                    it = new CleaningAppliance(name.getText(), p, f1.getText(), f2.getText());
                }

                inventory.add(it);
                refreshAll();
                setStatus("Objekt tillagd: " + it.getName());
                clearFields(name, price, f1, f2);
            } catch (Exception ex) {
                showError("Kunde inte lägga till objekt", ex.getMessage());
            }
        });

        update.setOnAction(e -> {
            Item sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Välj ett objekt i listan."); return; }

            try {
                double p = Double.parseDouble(price.getText().trim());
                sel.setName(name.getText());
                sel.setPricePerDay(p);

                if (sel instanceof CarAppliance) {
                    CarAppliance c = (CarAppliance) sel;
                    c.setApplianceType(f1.getText());
                    c.setCompatibility(f2.getText());
                } else if (sel instanceof CleaningAppliance) {
                    CleaningAppliance c = (CleaningAppliance) sel;
                    c.setCleaningType(f1.getText());
                    c.setNotes(f2.getText());
                }

                refreshAll();
                setStatus("Objekt uppdaterad.");
            } catch (Exception ex) {
                showError("Kunde inte uppdatera objekt", ex.getMessage());
            }
        });

        remove.setOnAction(e -> {
            Item sel = table.getSelectionModel().getSelectedItem();
            if (sel == null) { showInfo("Välj ett objekt i listan."); return; }
            boolean ok = inventory.removeById(sel.getId());
            if (ok) {
                refreshAll();
                setStatus("Objekt borttagen.");
            }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, o, sel) -> {
            if (sel == null) return;
            name.setText(sel.getName());
            price.setText(String.valueOf(sel.getPricePerDay()));
            type.setValue(sel.getType());

            if (sel instanceof CarAppliance) {
                CarAppliance c = (CarAppliance) sel;
                f1.setText(c.getApplianceType());
                f2.setText(c.getCompatibility());
            } else if (sel instanceof CleaningAppliance) {
                CleaningAppliance c = (CleaningAppliance) sel;
                f1.setText(c.getCleaningType());
                f2.setText(c.getNotes());
            }
        });

        HBox filters = new HBox(10, search, typeFilter, onlyAvailable);
        VBox right = new VBox(10, new Label("Skapa / ändra objekt"), form, new HBox(8, add, update, remove));
        right.setPadding(new Insets(0, 0, 0, 10));

        pane.setTop(filters);
        pane.setCenter(table);
        pane.setRight(right);

        return pane;
    }


    private Parent createRentalsTab() {
        BorderPane pane = new BorderPane();
        pane.setPadding(new Insets(10));

        TableView<Rental> table = new TableView<Rental>();
        table.setItems(rentalsObs);

        TableColumn<Rental, String> cId = new TableColumn<Rental, String>("ID");
        cId.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getId().substring(0, 8)));
        cId.setPrefWidth(90);

        TableColumn<Rental, String> cMember = new TableColumn<Rental, String>("Medlem");
        cMember.setCellValueFactory(r -> new SimpleStringProperty(memberName(r.getValue().getMemberId())));
        cMember.setPrefWidth(220);

        TableColumn<Rental, String> cItem = new TableColumn<Rental, String>("Objekt");
        cItem.setCellValueFactory(r -> new SimpleStringProperty(itemName(r.getValue().getItemId())));
        cItem.setPrefWidth(300);

        TableColumn<Rental, String> cStart = new TableColumn<Rental, String>("Start");
        cStart.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getStartDate().toString()));
        cStart.setPrefWidth(100);

        TableColumn<Rental, String> cEnd = new TableColumn<Rental, String>("Slut");
        cEnd.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().getEndDate() == null ? "" : r.getValue().getEndDate().toString()));
        cEnd.setPrefWidth(100);

        TableColumn<Rental, String> cTotal = new TableColumn<Rental, String>("Total");
        cTotal.setCellValueFactory(r -> new SimpleStringProperty(String.valueOf(r.getValue().getTotalPrice())));
        cTotal.setPrefWidth(100);

        TableColumn<Rental, String> cStatus = new TableColumn<Rental, String>("Status");
        cStatus.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().isActive() ? "Aktiv" : "Avslutad"));
        cStatus.setPrefWidth(100);

        table.getColumns().addAll(cId, cMember, cItem, cStart, cEnd, cTotal, cStatus);

        VBox form = new VBox(8);
        form.setPadding(new Insets(10));

        ComboBox<Member> memberBox = new ComboBox<Member>(membersObs);
        memberBox.setPromptText("Välj medlem");

        ComboBox<Item> itemBox = new ComboBox<Item>(itemsObs);
        itemBox.setPromptText("Välj objekt (måste vara tillgänglig)");

        DatePicker start = new DatePicker(LocalDate.now());

        Button book = new Button("Boka uthyrning");
        Button close = new Button("Avsluta vald uthyrning (idag)");

        book.setOnAction(e -> {
            Member m = memberBox.getValue();
            Item it = itemBox.getValue();
            if (m == null || it == null) { showInfo("Välj medlem och objekt."); return; }
            try {
                if (!it.isAvailable()) { showInfo("Valt objekt är inte tillgänglig."); return; }
                rentalService.bookRental(m.getId(), it.getId(), start.getValue());
                refreshAll();
                setStatus("Bokning skapad.");
            } catch (Exception ex) {
                showError("Kunde inte boka", ex.getMessage());
            }
        });

        close.setOnAction(e -> {
            Rental r = table.getSelectionModel().getSelectedItem();
            if (r == null) { showInfo("Välj en uthyrning i listan."); return; }
            try {
                double total = rentalService.closeRental(r.getId(), LocalDate.now());
                refreshAll();
                setStatus("Uthyrning avslutad. Total: " + total + " kr");
            } catch (Exception ex) {
                showError("Kunde inte avsluta", ex.getMessage());
            }
        });

        form.getChildren().addAll(
                new Label("Boka uthyrning"),
                new Label("Medlem"), memberBox,
                new Label("Objekt"), itemBox,
                new Label("Startdatum"), start,
                book,
                new Separator(),
                close
        );

        pane.setCenter(table);
        pane.setRight(form);

        return pane;
    }


    private Parent createSummaryTab() {
        VBox box = new VBox(10);
        box.setPadding(new Insets(12));

        Label active = new Label();
        Label revenue = new Label();
        Label counts = new Label();

        Button refresh = new Button("Uppdatera summering");
        refresh.setOnAction(e -> {
            active.setText("Aktiva uthyrningar: " + rentalService.countActiveRentals());
            revenue.setText("Total intäkt (avslutade): " + rentalService.totalRevenue() + " kr");
            counts.setText("Antal medlemmar: " + memberRegistry.getAll().size()
                    + " | Antal objekt: " + inventory.getAll().size()
                    + " | Antal uthyrningar: " + rentalService.getAllRentals().size());
        });

        refresh.fire();
        box.getChildren().addAll(refresh, active, revenue, counts);
        return box;
    }


    private void refreshAll() {
        membersObs.setAll(memberRegistry.getAll());
        itemsObs.setAll(inventory.getAll());
        rentalsObs.setAll(rentalService.getAllRentals());
    }

    private void setStatus(String msg) { status.setText(msg == null ? "" : msg); }

    private void showError(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Fel");
        a.setHeaderText(title);
        a.setContentText(msg == null ? "" : msg);
        a.showAndWait();
    }

    private void showInfo(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Info");
        a.setHeaderText(null);
        a.setContentText(msg == null ? "" : msg);
        a.showAndWait();
    }

    private String memberName(String memberId) {
        Member m = memberRegistry.findById(memberId);
        return m == null ? "(okänd)" : m.getFullName();
    }

    private String itemName(String itemId) {
        Item it = inventory.findById(itemId);
        return it == null ? "(okänd)" : it.getName() + " [" + it.getType() + "]";
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void clearFields(TextField... fields) {
        for (TextField f : fields) f.clear();
    }
}
