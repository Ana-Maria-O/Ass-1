package src.SmartWarehouse;

import java.util.List;

public class ChargeTask {
    Robot missionRobot;
    Integer chargingStationPosition;

    public ChargeTask(Robot missionRobot, Integer chargingStationPosition, List<Integer> pathToCS) {
        this.missionRobot = missionRobot;
        this.chargingStationPosition = chargingStationPosition;
        missionRobot.setCurrentSelectedPath(pathToCS);
        System.out.println("Charge task path set to: " + pathToCS);
        missionRobot.setTargetPosition(pathToCS.get(pathToCS.size() - 1));
        missionRobot.chargeTask = this;
        missionRobot.deactivateBug2();
        WMS.activeRobots.add(missionRobot.index);
    }

    public void timeStep() {
        if (missionRobot.pathIsComplete()) {
            missionRobot.setBatteryLevel(100);
            System.out.println(missionRobot.getName() + " recharges to 100%");
            missionRobot.abortingToCharge = false;
            WMS.activeRobots.remove((Object) missionRobot.index);
            missionRobot.chargeTask = null;
        } else {
            missionRobot.stepTowardsTarget();
        }
    }
}
