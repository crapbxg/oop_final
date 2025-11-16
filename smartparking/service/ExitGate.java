package service;

public class ExitGate {
    private ParkingSystem system;

    public ExitGate(ParkingSystem system) {
        this.system = system;
    }

    // Change 'int hoursStayed' to 'Double amount'
    public Double processExit(String ticketId, Double amount) throws Exception {
        return system.markExitWithAmount(ticketId, amount);
    }
}