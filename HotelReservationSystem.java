import java.util.*;
import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * ================================================
 *   CodeAlpha Internship — Task 4
 *   Hotel Reservation System
 *   Author: Dharshan
 * ================================================
 */

// Enum for room categories
enum RoomType {
    STANDARD, DELUXE, SUITE
}

// Represents a hotel room
class Room {
    private int roomNumber;
    private RoomType type;
    private double pricePerNight;
    private boolean isAvailable;

    public Room(int roomNumber, RoomType type, double pricePerNight) {
        this.roomNumber = roomNumber;
        this.type = type;
        this.pricePerNight = pricePerNight;
        this.isAvailable = true;
    }

    public int getRoomNumber()       { return roomNumber; }
    public RoomType getType()        { return type; }
    public double getPricePerNight() { return pricePerNight; }
    public boolean isAvailable()     { return isAvailable; }
    public void setAvailable(boolean available) { this.isAvailable = available; }

    public void display() {
        System.out.printf("  Room %-4d | %-10s | ₹%8.2f/night | %s%n",
                roomNumber, type, pricePerNight,
                isAvailable ? "✅ Available" : "❌ Booked");
    }
}

// Represents a booking
class Booking {
    private static int bookingCounter = 1000;

    private String bookingId;
    private String guestName;
    private String guestPhone;
    private Room room;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private double totalAmount;
    private double gstAmount;
    private boolean isPaid;
    private String bookingDate;

    public Booking(String guestName, String guestPhone, Room room,
                   LocalDate checkIn, LocalDate checkOut) {
        this.bookingId   = "BK" + (++bookingCounter);
        this.guestName   = guestName;
        this.guestPhone  = guestPhone;
        this.room        = room;
        this.checkIn     = checkIn;
        this.checkOut    = checkOut;
        this.bookingDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        double baseAmount = nights * room.getPricePerNight();
        this.gstAmount   = Math.round(baseAmount * 0.18 * 100.0) / 100.0; // 18% GST
        this.totalAmount = Math.round((baseAmount + gstAmount) * 100.0) / 100.0;
        this.isPaid      = false;
    }

    public String getBookingId()  { return bookingId; }
    public String getGuestName()  { return guestName; }
    public Room getRoom()         { return room; }
    public boolean isPaid()       { return isPaid; }
    public void markPaid()        { this.isPaid = true; }

    public long getNights() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public void displaySummary() {
        System.out.println("  ----------------------------------------------------");
        System.out.println("  Booking ID    : " + bookingId);
        System.out.println("  Guest Name    : " + guestName);
        System.out.println("  Phone         : " + guestPhone);
        System.out.printf ("  Room          : %d (%s)%n", room.getRoomNumber(), room.getType());
        System.out.println("  Check-In      : " + checkIn);
        System.out.println("  Check-Out     : " + checkOut);
        System.out.println("  Nights        : " + getNights());
        System.out.printf ("  Base Amount   : ₹%.2f%n", totalAmount - gstAmount);
        System.out.printf ("  GST (18%%)     : ₹%.2f%n", gstAmount);
        System.out.printf ("  Total Amount  : ₹%.2f%n", totalAmount);
        System.out.println("  Payment       : " + (isPaid ? "✅ Paid" : "❌ Pending"));
        System.out.println("  Booked On     : " + bookingDate);
        System.out.println("  ----------------------------------------------------");
    }

    // Save booking to file
    public void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter("booking_" + bookingId + ".txt"))) {
            pw.println("========================================");
            pw.println("     HOTEL SUNRISE — BOOKING RECEIPT    ");
            pw.println("========================================");
            pw.println("Booking ID   : " + bookingId);
            pw.println("Guest Name   : " + guestName);
            pw.println("Phone        : " + guestPhone);
            pw.println("Room No.     : " + room.getRoomNumber() + " (" + room.getType() + ")");
            pw.println("Check-In     : " + checkIn);
            pw.println("Check-Out    : " + checkOut);
            pw.println("Nights       : " + getNights());
            pw.printf ("Base Amount  : ₹%.2f%n", totalAmount - gstAmount);
            pw.printf ("GST (18%%)    : ₹%.2f%n", gstAmount);
            pw.printf ("Total Amount : ₹%.2f%n", totalAmount);
            pw.println("Payment      : " + (isPaid ? "Paid" : "Pending"));
            pw.println("Booked On    : " + bookingDate);
            pw.println("========================================");
            pw.println("  Thank you for choosing Hotel Sunrise!");
            pw.println("========================================");
        } catch (IOException e) {
            System.out.println("  [!] Could not save receipt: " + e.getMessage());
        }
    }
}

// Main Hotel Reservation System
public class HotelReservationSystem {

    static ArrayList<Room> rooms = new ArrayList<>();
    static ArrayList<Booking> bookings = new ArrayList<>();
    static Scanner sc = new Scanner(System.in);
    static DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static void main(String[] args) {
        initializeRooms();

        System.out.println("============================================");
        System.out.println("    HOTEL SUNRISE — RESERVATION SYSTEM      ");
        System.out.println("         Powered by CodeAlpha               ");
        System.out.println("============================================");

        boolean running = true;
        while (running) {
            printMenu();
            int choice = getIntInput("Enter your choice: ");

            switch (choice) {
                case 1 -> viewAllRooms();
                case 2 -> searchAvailableRooms();
                case 3 -> makeReservation();
                case 4 -> viewBooking();
                case 5 -> cancelReservation();
                case 6 -> makePayment();
                case 7 -> viewAllBookings();
                case 8 -> {
                    System.out.println("\n  Thank you for choosing Hotel Sunrise. Goodbye!");
                    running = false;
                }
                default -> System.out.println("  [!] Invalid choice. Try again.");
            }
        }

        sc.close();
    }

    // Pre-load hotel rooms
    static void initializeRooms() {
        // Standard Rooms (101–105) — ₹2,000/night
        for (int i = 101; i <= 105; i++)
            rooms.add(new Room(i, RoomType.STANDARD, 2000));

        // Deluxe Rooms (201–204) — ₹4,500/night
        for (int i = 201; i <= 204; i++)
            rooms.add(new Room(i, RoomType.DELUXE, 4500));

        // Suites (301–302) — ₹9,000/night
        for (int i = 301; i <= 302; i++)
            rooms.add(new Room(i, RoomType.SUITE, 9000));
    }

    static void printMenu() {
        System.out.println("\n============================================");
        System.out.println("  MENU");
        System.out.println("  1. View All Rooms");
        System.out.println("  2. Search Available Rooms");
        System.out.println("  3. Make a Reservation");
        System.out.println("  4. View Booking Details");
        System.out.println("  5. Cancel Reservation");
        System.out.println("  6. Make Payment");
        System.out.println("  7. View All Bookings");
        System.out.println("  8. Exit");
        System.out.println("============================================");
    }

    // View all rooms with status
    static void viewAllRooms() {
        System.out.println("\n  === ALL ROOMS ===");
        System.out.println("  ------------------------------------------------");
        System.out.printf("  %-10s | %-10s | %-15s | %s%n",
                "Room No.", "Type", "Price/Night", "Status");
        System.out.println("  ------------------------------------------------");

        for (Room r : rooms) r.display();
        System.out.println("  ------------------------------------------------");

        // Summary count
        long available = rooms.stream().filter(Room::isAvailable).count();
        System.out.printf("  Total: %d rooms | Available: %d | Booked: %d%n",
                rooms.size(), available, rooms.size() - available);
    }

    // Search available rooms by type
    static void searchAvailableRooms() {
        System.out.println("\n  Search by Room Type:");
        System.out.println("  1. Standard  (₹2,000/night)");
        System.out.println("  2. Deluxe    (₹4,500/night)");
        System.out.println("  3. Suite     (₹9,000/night)");
        System.out.println("  4. All Available Rooms");

        int choice = getIntInput("  Enter choice: ");
        RoomType filter = null;

        switch (choice) {
            case 1 -> filter = RoomType.STANDARD;
            case 2 -> filter = RoomType.DELUXE;
            case 3 -> filter = RoomType.SUITE;
            case 4 -> filter = null;
            default -> { System.out.println("  [!] Invalid choice."); return; }
        }

        System.out.println("\n  === AVAILABLE ROOMS ===");
        System.out.println("  ------------------------------------------------");
        boolean found = false;

        for (Room r : rooms) {
            if (r.isAvailable() && (filter == null || r.getType() == filter)) {
                r.display();
                found = true;
            }
        }

        if (!found) System.out.println("  No rooms available for this category.");
        System.out.println("  ------------------------------------------------");
    }

    // Make a new reservation
    static void makeReservation() {
        System.out.println("\n  === MAKE A RESERVATION ===");

        System.out.print("  Enter Guest Name  : ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) { System.out.println("  [!] Name cannot be empty."); return; }

        System.out.print("  Enter Phone Number: ");
        String phone = sc.nextLine().trim();
        if (phone.isEmpty()) { System.out.println("  [!] Phone cannot be empty."); return; }

        // Show available rooms
        System.out.println("\n  Available Rooms:");
        System.out.println("  ------------------------------------------------");
        boolean anyAvailable = false;
        for (Room r : rooms) {
            if (r.isAvailable()) { r.display(); anyAvailable = true; }
        }

        if (!anyAvailable) {
            System.out.println("  [!] No rooms available at the moment.");
            return;
        }
        System.out.println("  ------------------------------------------------");

        int roomNo = getIntInput("  Enter Room Number to book: ");
        Room selectedRoom = findRoom(roomNo);

        if (selectedRoom == null) {
            System.out.println("  [!] Room not found.");
            return;
        }
        if (!selectedRoom.isAvailable()) {
            System.out.println("  [!] Room " + roomNo + " is already booked.");
            return;
        }

        LocalDate checkIn  = getDateInput("  Enter Check-In Date  (dd-MM-yyyy): ");
        LocalDate checkOut = getDateInput("  Enter Check-Out Date (dd-MM-yyyy): ");

        if (!checkOut.isAfter(checkIn)) {
            System.out.println("  [!] Check-out date must be after check-in date.");
            return;
        }

        // Create booking
        Booking booking = new Booking(name, phone, selectedRoom, checkIn, checkOut);
        bookings.add(booking);
        selectedRoom.setAvailable(false);

        System.out.println("\n  [✓] Reservation Confirmed!");
        booking.displaySummary();
        booking.saveToFile();
        System.out.println("  [✓] Receipt saved to booking_" + booking.getBookingId() + ".txt");
    }

    // View a specific booking
    static void viewBooking() {
        System.out.print("\n  Enter Booking ID (e.g. BK1001): ");
        String id = sc.nextLine().trim().toUpperCase();

        Booking b = findBooking(id);
        if (b == null) {
            System.out.println("  [!] Booking not found.");
            return;
        }

        System.out.println("\n  === BOOKING DETAILS ===");
        b.displaySummary();
    }

    // Cancel a reservation
    static void cancelReservation() {
        System.out.print("\n  Enter Booking ID to cancel: ");
        String id = sc.nextLine().trim().toUpperCase();

        Booking b = findBooking(id);
        if (b == null) {
            System.out.println("  [!] Booking not found.");
            return;
        }

        System.out.println("\n  Booking to cancel:");
        b.displaySummary();
        System.out.print("  Are you sure you want to cancel? (yes/no): ");
        String confirm = sc.nextLine().trim().toLowerCase();

        if (confirm.equals("yes")) {
            b.getRoom().setAvailable(true);
            bookings.remove(b);
            System.out.println("  [✓] Booking " + id + " has been cancelled. Room is now available.");
        } else {
            System.out.println("  [✗] Cancellation aborted.");
        }
    }

    // Simulate payment
    static void makePayment() {
        System.out.print("\n  Enter Booking ID to pay: ");
        String id = sc.nextLine().trim().toUpperCase();

        Booking b = findBooking(id);
        if (b == null) {
            System.out.println("  [!] Booking not found.");
            return;
        }

        if (b.isPaid()) {
            System.out.println("  [!] This booking is already paid.");
            return;
        }

        b.displaySummary();

        System.out.println("\n  Select Payment Method:");
        System.out.println("  1. Credit/Debit Card");
        System.out.println("  2. UPI");
        System.out.println("  3. Net Banking");
        System.out.println("  4. Cash");

        int method = getIntInput("  Enter choice: ");
        String[] methods = {"Credit/Debit Card", "UPI", "Net Banking", "Cash"};

        if (method < 1 || method > 4) {
            System.out.println("  [!] Invalid payment method.");
            return;
        }

        System.out.println("\n  Processing payment via " + methods[method - 1] + "...");
        try { Thread.sleep(1000); } catch (InterruptedException e) { /* ignore */ }

        b.markPaid();
        b.saveToFile();
        System.out.println("  [✓] Payment successful via " + methods[method - 1] + "!");
        System.out.println("  [✓] Receipt updated for Booking ID: " + b.getBookingId());
    }

    // View all bookings
    static void viewAllBookings() {
        System.out.println("\n  === ALL BOOKINGS ===");
        System.out.println("  ------------------------------------------------");

        if (bookings.isEmpty()) {
            System.out.println("  No bookings found.");
        } else {
            for (Booking b : bookings) b.displaySummary();
        }
        System.out.println("  ------------------------------------------------");
        System.out.println("  Total Bookings: " + bookings.size());
    }

    // Find room by number
    static Room findRoom(int number) {
        for (Room r : rooms)
            if (r.getRoomNumber() == number) return r;
        return null;
    }

    // Find booking by ID
    static Booking findBooking(String id) {
        for (Booking b : bookings)
            if (b.getBookingId().equalsIgnoreCase(id)) return b;
        return null;
    }

    // Safe integer input
    static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    // Safe date input
    static LocalDate getDateInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return LocalDate.parse(sc.nextLine().trim(), dateFormat);
            } catch (Exception e) {
                System.out.println("  [!] Invalid date. Use format dd-MM-yyyy (e.g. 15-07-2025)");
            }
        }
    }
}
