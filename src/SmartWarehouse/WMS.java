package src.SmartWarehouse;

// TODO
public class WMS {

    /*
     * Sub-classes of the WMS as outlined in the MAPE-K architecture in Assignment 3
     * TODO: Assignment 4 - Implement the WMS using these
     */
    public static class Monitor {
    }

    public static class Analyzer {
    }

    public static class Planner {
    }

    public static class Executor {
    }

    public static class KnowledgeBase {
        /*
         * TODO: Create a private variable to hold all precoputed paths (to cell 40)
         * Create getter and setter functions for it
         */

        // private static allPathsTo40

        // private static setPathTo40(path) {}

        // private static getPathTo40(startingCell) {}
    }

    // Constants
    // Number of robots
    private final int ROBOTNUMBER = 10;
    // Number of conveyor belts
    private final int CBNUMBER = 2;
    // Number of charging stations
    private final int CSNUMBER = 4;

    // Dummy function for notifications send to WMS
    public static void notify(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {

        /*
         * For assignment 3: initiate 2 robots and start their movements
         * Can probably delete this after assignment 3
         * Can probably also do this in a different class I guess, but imo it's easier
         * to have all this top-level stuff in this class. Having a "main" class
         * when that is not the main class for the system is very confusing
         */

        // Initiate the warehouse - DO NOT DELETE, WILL BE USED FOR ASSIGNMENT 4
        // initWarehouse();

        // Old code from previous assignments
        // ERRORS: The robot constructor has been modified. functions in the Robot class
        // have been deleted
        // CBC conveyorBeltController = new CBC();
        // Robot robot = new Robot("R#1");
        // Robot robot2 = new Robot("R#2");
        // String packetRFID = "P#1";
        // robot.enqueueForFetching(conveyorBeltController, packetRFID);
    }

    private static void initWarehouse() {
        /*
         * TODO
         * Initiate robots
         * Initiate conveyor belts with some packages on them
         * Compute all paths to grid 40 and store them in KnowledgeBase.allPathsTo40
         */

    }
}
