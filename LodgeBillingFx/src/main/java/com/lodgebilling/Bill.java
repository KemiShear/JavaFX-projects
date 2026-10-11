
package com.lodgebilling;

public class Bill {

    private final int billNumber;
    private final String guest;
    private final String room;
    private final int nights;
    private final double total;
    private final double paid;
    private final String notes;

    public Bill(int billNumber, String guest, String room,
                int nights, double total, double paid) {
        this(billNumber, guest, room, nights, total, paid, "");
    }

    public Bill(int billNumber, String guest, String room,
                int nights, double total, double paid, String notes) {
        this.billNumber = billNumber;
        this.guest = guest;
        this.room = room;
        this.nights = nights;
        this.total = total;
        this.paid = paid;
        this.notes = notes == null ? "" : notes;
    }

    public int getBillNumber() { return billNumber; }
    public String getGuest() { return guest; }
    public String getRoom() { return room; }
    public int getNights() { return nights; }
    public double getTotal() { return total; }
    public double getPaid() { return paid; }
    public double getBalance() { return total - paid; }
    public String getNotes() { return notes; }
}
