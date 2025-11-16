package main;

public class ParkingLot {
    private Floor[] floors;
    private int floorCount;

    public ParkingLot(int maxFloors, int spotsPerFloor) {
        this.floors = new Floor[maxFloors];
        this.floorCount = 0;
        for (int i = 0; i < maxFloors; i++) {
            floors[floorCount++] = new Floor(i + 1, spotsPerFloor);
        }
    }

    public boolean addSpotToFloor(int floorNumber, ParkingSpot spot) {
        if (floorNumber < 1 || floorNumber > floorCount) {
            return false;
        }
        Floor f = floors[floorNumber - 1];
        return f.addSpot(spot);
    }

    public ParkingSpot findSpot(String spotId) {
        for (int i = 0; i < floorCount; i++) {
            ParkingSpot p = floors[i].findSpotById(spotId);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    public ParkingSpot findFreeSpotForVehicle(String vehicleType) {
        for (int i = 0; i < floorCount; i++) {
            ParkingSpot p = floors[i].findFirstFreeSpotForType(vehicleType);
            if (p != null) {
                return p;
            }
        }
        return null;
    }

    public Floor[] getFloors() {
        return floors;
    }
}