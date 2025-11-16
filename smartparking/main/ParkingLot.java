package main;

public class ParkingLot {
    private Floor[] floors;

    public ParkingLot(int maxFloors, int spotsPerFloor) {
        this.floors = new Floor[maxFloors];
        for (int i = 0; i < maxFloors; i++) {
            floors[i] = new Floor(i + 1, spotsPerFloor);
        }
    }

    public boolean addSpotToFloor(int floorNumber, ParkingSpot spot) {
        if (floorNumber < 1 || floorNumber > floors.length) return false;
        return floors[floorNumber - 1].addSpot(spot);
    }

    // Find a free spot SPECIFICALLY for this vehicle type
    public ParkingSpot findFreeSpotForVehicle(String vehicleType) {
        for (Floor floor : floors) {
            // We delegate to Floor to find the specific type
            for (ParkingSpot spot : floor.getSpots()) {
                if (spot != null && spot.isFree() && spot.getSpotType().equalsIgnoreCase(vehicleType)) {
                    return spot;
                }
            }
        }
        return null;
    }

    public ParkingSpot findSpot(String spotId) {
        for (Floor f : floors) {
            ParkingSpot p = f.findSpotById(spotId);
            if (p != null) return p;
        }
        return null;
    }

    public Floor[] getFloors() { return floors; }
}