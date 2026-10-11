
package com.lodgebilling;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

import java.time.LocalDate;

public class DashboardView {

    private final Main main;
    private final String username;
    private final BorderPane root = new BorderPane();
    private final VBox content = new VBox(18);

    public DashboardView(Main main, String username) {
        this.main = main;
        this.username = username;
        buildLayout();
        showBilling();
    }

    public Parent getView() {
        return root;
    }

    private void buildLayout() {
        root.getStyleClass().add("dashboard-root");

        Label brand = new Label("LODGE MANAGER");
        brand.getStyleClass().add("brand");

        Label userLabel = new Label("Signed in: " + username);
        userLabel.getStyleClass().add("user-label");

        Button logout = new Button("Logout");
        logout.getStyleClass().add("secondary-button");
        logout.setOnAction(e -> main.showLogin());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox top = new HBox(15, brand, spacer, userLabel, logout);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(18));
        top.getStyleClass().add("top-bar");
        root.setTop(top);

        Button billing = new Button("Billing");
        Button reservations = new Button("Reservations");
        Button saved = new Button("Saved Bills");

        for (Button button :
                new Button[]{billing, reservations, saved}) {
            button.getStyleClass().add("nav-button");
            button.setMaxWidth(Double.MAX_VALUE);
        }

        billing.setOnAction(e -> showBilling());
        reservations.setOnAction(e -> showReservations());
        saved.setOnAction(e -> showSavedBills());

        VBox navigation = new VBox(12, billing, reservations, saved);
        navigation.setPadding(new Insets(20));
        navigation.setPrefWidth(175);
        navigation.getStyleClass().add("sidebar");
        root.setLeft(navigation);

        content.setPadding(new Insets(24));
        content.getStyleClass().add("content-area");
        root.setCenter(content);
    }

    private Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    private TextField input(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("input-field");
        return field;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    private VBox card() {
        VBox box = new VBox(14);
        box.setPadding(new Insets(20));
        box.getStyleClass().add("panel-card");
        return box;
    }

    // ---------------- BILLING ----------------

    private void showBilling() {
        content.getChildren().clear();

        Label pageTitle = title("Guest Billing");

        Label description = new Label(
                "Enter guest details, calculate charges, and save the bill.");
        description.getStyleClass().add("subtitle");

        TextField guest = input("Guest full name");
        TextField phone = input("Phone number (optional)");

        ComboBox<String> room = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Standard", "Deluxe", "Family"));
        room.setPromptText("Select room type");
        room.setMaxWidth(Double.MAX_VALUE);
        room.getStyleClass().add("input-field");

        Spinner<Integer> nights = new Spinner<>(1, 365, 1);
        nights.setEditable(true);
        nights.setMaxWidth(Double.MAX_VALUE);

        TextField extras = input("Extra charges (K), e.g. 50");
        TextField paid = input("Amount paid (K), e.g. 200");

        TextArea notes = new TextArea();
        notes.setPromptText("Additional notes (optional)");
        notes.setPrefRowCount(2);
        notes.getStyleClass().add("input-field");

        Label calculation = new Label(
                "Select a room to calculate the bill.");
        calculation.getStyleClass().add("summary-label");

        Button calculate = primaryButton("Calculate Total");
        Button save = primaryButton("Save Bill");

        Label message = new Label();
        message.getStyleClass().add("message");

        Runnable calculateBill = () -> {
            if (room.getValue() == null) {
                calculation.setText("Please select a room type.");
                return;
            }

            try {
                double extra = readAmount(extras.getText());
                double amountPaid = readAmount(paid.getText());

                int numberOfNights = nights.getValue();

                double roomCharge =
                        LodgeData.getRoomPrice(room.getValue())
                                * numberOfNights;

                double discount = numberOfNights >= 5
                        ? roomCharge * 0.05 : 0;

                double total = roomCharge - discount + extra;

                calculation.setText(String.format(
                        "Room: K%.2f   Discount: K%.2f   Extras: K%.2f"
                                + "\nTotal: K%.2f   Paid: K%.2f   Balance: K%.2f",
                        roomCharge, discount, extra, total,
                        amountPaid, total - amountPaid));

            } catch (IllegalArgumentException ex) {
                calculation.setText(
                        "Enter valid non-negative amounts.");
            }
        };

        calculate.setOnAction(e -> calculateBill.run());

        save.setOnAction(e -> {
            if (guest.getText().isBlank() || room.getValue() == null) {
                message.setText(
                        "Enter the guest name and select a room.");
                return;
            }

            try {
                double extra = readAmount(extras.getText());
                double amountPaid = readAmount(paid.getText());

                int numberOfNights = nights.getValue();

                double roomCharge =
                        LodgeData.getRoomPrice(room.getValue())
                                * numberOfNights;

                double discount = numberOfNights >= 5
                        ? roomCharge * 0.05 : 0;

                double total = roomCharge - discount + extra;

                Bill bill = new Bill(
                        LodgeData.nextBillNumber(),
                        guest.getText().trim(),
                        room.getValue(),
                        numberOfNights,
                        total,
                        amountPaid,
                        notes.getText()
                );

                LodgeData.bills.add(bill);

                message.setText("Bill saved successfully.");

                // Display the newly saved bill.
                showSavedBills();

            } catch (IllegalArgumentException ex) {
                message.setText(
                        "Enter valid non-negative charges and payment.");
            }
        });

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);

        form.add(new Label("Guest name"), 0, 0);
        form.add(guest, 1, 0);

        form.add(new Label("Phone"), 0, 1);
        form.add(phone, 1, 1);

        form.add(new Label("Room type"), 0, 2);
        form.add(room, 1, 2);

        form.add(new Label("Nights"), 0, 3);
        form.add(nights, 1, 3);

        form.add(new Label("Extra charges (K)"), 0, 4);
        form.add(extras, 1, 4);

        form.add(new Label("Paid (K)"), 0, 5);
        form.add(paid, 1, 5);

        form.add(new Label("Notes"), 0, 6);
        form.add(notes, 1, 6);

        configureColumns(form);

        VBox panel = card();
        panel.getChildren().addAll(
                form,
                calculation,
                new HBox(10, calculate, save),
                message
        );

        content.getChildren().addAll(
                pageTitle, description, panel);
    }

    // ---------------- SAVED BILLS ----------------

    private void showSavedBills() {
        content.getChildren().clear();

        Label pageTitle = title("Saved Bills");

        Label description = new Label(
                "View bills saved during this application session.");
        description.getStyleClass().add("subtitle");

        TableView<Bill> table = new TableView<>();
        table.setItems(LodgeData.bills);
        table.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        TableColumn<Bill, Integer> number =
                new TableColumn<>("Bill No.");
        number.setCellValueFactory(
                new PropertyValueFactory<>("billNumber"));

        TableColumn<Bill, String> guest =
                new TableColumn<>("Guest");
        guest.setCellValueFactory(
                new PropertyValueFactory<>("guest"));

        TableColumn<Bill, String> room =
                new TableColumn<>("Room");
        room.setCellValueFactory(
                new PropertyValueFactory<>("room"));

        TableColumn<Bill, Integer> nights =
                new TableColumn<>("Nights");
        nights.setCellValueFactory(
                new PropertyValueFactory<>("nights"));

        TableColumn<Bill, Double> total =
                new TableColumn<>("Total (K)");
        total.setCellValueFactory(
                new PropertyValueFactory<>("total"));

        TableColumn<Bill, Double> paid =
                new TableColumn<>("Paid (K)");
        paid.setCellValueFactory(
                new PropertyValueFactory<>("paid"));

        TableColumn<Bill, Double> balance =
                new TableColumn<>("Balance (K)");
        balance.setCellValueFactory(
                new PropertyValueFactory<>("balance"));

        table.getColumns().addAll(
                number, guest, room, nights, total, paid, balance);

        table.setPlaceholder(
                new Label("No bills have been saved yet."));

        Button back = primaryButton("Back to Billing");
        back.setOnAction(e -> showBilling());

        VBox panel = card();
        panel.getChildren().addAll(table, back);
        VBox.setVgrow(table, Priority.ALWAYS);

        content.getChildren().addAll(
                pageTitle, description, panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
    }

    // ---------------- RESERVATIONS ----------------

    private void showReservations() {
        content.getChildren().clear();

        Label pageTitle = title("Room Reservations");

        Label description = new Label(
                "Enter guest details and save a reservation.");
        description.getStyleClass().add("subtitle");

        TextField guest = input("Guest full name");
        TextField phone = input("Phone number");

        ComboBox<String> room = new ComboBox<>(
                FXCollections.observableArrayList(
                        "Standard", "Deluxe", "Family"));
        room.setPromptText("Select room type");
        room.setMaxWidth(Double.MAX_VALUE);
        room.getStyleClass().add("input-field");

        DatePicker checkIn =
                new DatePicker(LocalDate.now());

        DatePicker checkOut =
                new DatePicker(LocalDate.now().plusDays(1));

        checkIn.setMaxWidth(Double.MAX_VALUE);
        checkOut.setMaxWidth(Double.MAX_VALUE);

        Spinner<Integer> guests = new Spinner<>(1, 30, 1);
        guests.setEditable(true);

        TextArea notes = new TextArea();
        notes.setPromptText("Reservation notes (optional)");
        notes.setPrefRowCount(2);
        notes.getStyleClass().add("input-field");

        Label message = new Label();
        message.getStyleClass().add("message");

        Button save = primaryButton("Save Reservation");

        save.setOnAction(e -> {
            if (guest.getText().isBlank()
                    || phone.getText().isBlank()
                    || room.getValue() == null
                    || checkIn.getValue() == null
                    || checkOut.getValue() == null) {
                message.setText(
                        "Complete all required reservation details.");
                return;
            }

            if (!checkOut.getValue().isAfter(checkIn.getValue())) {
                message.setText(
                        "Check-out must be after check-in.");
                return;
            }

            Reservation reservation = new Reservation(
                    LodgeData.nextReservationNumber(),
                    guest.getText().trim(),
                    phone.getText().trim(),
                    room.getValue(),
                    checkIn.getValue().toString(),
                    checkOut.getValue().toString(),
                    guests.getValue(),
                    notes.getText()
            );

            LodgeData.reservations.add(reservation);

            message.setText("Reservation saved successfully.");
        });

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);

        form.add(new Label("Guest name"), 0, 0);
        form.add(guest, 1, 0);

        form.add(new Label("Phone"), 0, 1);
        form.add(phone, 1, 1);

        form.add(new Label("Room type"), 0, 2);
        form.add(room, 1, 2);

        form.add(new Label("Check-in"), 0, 3);
        form.add(checkIn, 1, 3);

        form.add(new Label("Check-out"), 0, 4);
        form.add(checkOut, 1, 4);

        form.add(new Label("Number of guests"), 0, 5);
        form.add(guests, 1, 5);

        form.add(new Label("Notes"), 0, 6);
        form.add(notes, 1, 6);

        configureColumns(form);

        VBox panel = card();
        panel.getChildren().addAll(form, save, message);

        content.getChildren().addAll(
                pageTitle, description, panel);
    }

    // ---------------- HELPERS ----------------

    private double readAmount(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        double amount = Double.parseDouble(value.trim());

        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    "Amount must be a finite, non-negative number.");
        }

        return amount;
    }

    private void configureColumns(GridPane form) {
        ColumnConstraints left = new ColumnConstraints();
        left.setMinWidth(140);

        ColumnConstraints right = new ColumnConstraints();
        right.setHgrow(Priority.ALWAYS);
        right.setFillWidth(true);

        form.getColumnConstraints().addAll(left, right);
    }
}
