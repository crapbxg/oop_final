package util;

/**
 * Interface for generating simple text-based reports.
 * Implemented by Admin in this project.
 */
public interface Reportable {

    /**
     * Generates a report and returns it as a String.
     *
     * @return report text
     */
    String generateReport();
}
