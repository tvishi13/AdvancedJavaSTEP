import java.util.*;

enum Status {
    EMPTY,
    OCCUPIED,
    DELETED
}

class ParkingSpot {
    String licensePlate;
    long entryTime;
    Status status;

    public ParkingSpot() {
        this.status = Status.EMPTY;
    }
}

public class SmartParkingLot {

    private static final int CAPACITY = 500;
    private ParkingSpot[] table = new ParkingSpot[CAPACITY];

    private int occupiedCount = 0;
    private int totalProbes = 0;
    private int totalParks = 0;

    public SmartParkingLot() {
        for (int i = 0; i < CAPACITY; i++) {
            table[i] = new ParkingSpot();
        }
    }

    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % CAPACITY;
    }

    public void parkVehicle(String licensePlate) {

        int index = hash(licensePlate);
        int probes = 0;

        while (table[index].status == Status.OCCUPIED) {
            index = (index + 1) % CAPACITY;
            probes++;
        }

        table[index].licensePlate = licensePlate;
        table[index].entryTime = System.currentTimeMillis();
        table[index].status = Status.OCCUPIED;

        occupiedCount++;
        totalProbes += probes;
        totalParks++;

        System.out.println("Assigned spot #" + index +
                " (" + probes + " probes)");
    }

    public void exitVehicle(String licensePlate) {

        int index = hash(licensePlate);

        while (table[index].status != Status.EMPTY) {

            if (table[index].status == Status.OCCUPIED &&
                table[index].licensePlate.equals(licensePlate)) {

                long durationMillis =
                        System.currentTimeMillis() - table[index].entryTime;

                double hours = durationMillis / (1000.0 * 60 * 60);
                double fee = hours * 5; // $5 per hour

                table[index].status = Status.DELETED;
                occupiedCount--;

                System.out.println("Spot #" + index + " freed.");
                System.out.println("Duration: " +
                        String.format("%.2f", hours) + " hours");
                System.out.println("Fee: $" +
                        String.format("%.2f", fee));
                return;
            }

            index = (index + 1) % CAPACITY;
        }

        System.out.println("Vehicle not found.");
    }

    public void getStatistics() {

        double occupancy =
                (occupiedCount * 100.0) / CAPACITY;

        double avgProbes =
                totalParks == 0 ? 0 :
                        (double) totalProbes / totalParks;

        System.out.println("Occupancy: " +
                String.format("%.2f", occupancy) + "%");

        System.out.println("Avg Probes: " +
                String.format("%.2f", avgProbes));
    }

    public static void main(String[] args) {

        SmartParkingLot lot = new SmartParkingLot();

        lot.parkVehicle("ABC-1234");
        lot.parkVehicle("ABC-1235");
        lot.parkVehicle("XYZ-9999");

        lot.getStatistics();

        lot.exitVehicle("ABC-1234");

        lot.getStatistics();
    }
}
