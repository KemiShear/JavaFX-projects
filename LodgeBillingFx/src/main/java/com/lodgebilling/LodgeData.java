
package com.lodgebilling;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.LinkedHashMap;
import java.util.Map;

public final class LodgeData {

    private LodgeData() {}

    public static final ObservableList<Bill> bills =
            FXCollections.observableArrayList();

    public static final ObservableList<Reservation> reservations =
            FXCollections.observableArrayList();

    // Demo account storage: accounts remain available until the app closes.
    private static final Map<String, String> accounts =
            new LinkedHashMap<>();

    private static int billCounter = 1;
    private static int reservationCounter = 1;

    static {
        accounts.put("admin", "admin123");
    }

    public static boolean register(String username, String password) {
        String key = username.trim().toLowerCase();

        if (key.isEmpty() || password.isEmpty() || accounts.containsKey(key)) {
            return false;
        }

        accounts.put(key, password);
        return true;
    }

    public static boolean login(String username, String password) {
        if (username == null || password == null) return false;

        String savedPassword =
                accounts.get(username.trim().toLowerCase());

        return savedPassword != null && savedPassword.equals(password);
    }

    public static int nextBillNumber() {
        return billCounter++;
    }

    public static int nextReservationNumber() {
        return reservationCounter++;
    }

    public static double getRoomPrice(String room) {
        return switch (room) {
            case "Standard" -> 350;
            case "Deluxe" -> 550;
            case "Family" -> 750;
            default -> 0;
        };
    }
}
