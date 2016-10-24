package org.firstinspires.ftc.hdcode.Velocity_Vortex;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;

import org.firstinspires.ftc.hdcode.ResQ.HDAuto_2015;
import org.firstinspires.ftc.hdlib.Alliance;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDAuto;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDOpMode;
import org.firstinspires.ftc.hdlib.Telemetry.HDDashboard;
import org.firstinspires.ftc.hdlib.Telemetry.HDMenu.HDMenu;
import org.firstinspires.ftc.hdlib.Telemetry.HDMenu.HDNumberMenu;
import org.firstinspires.ftc.hdlib.Telemetry.HDMenu.HDTextMenu;

/**
 * Created by Akash on 10/20/2016.
 */

@Autonomous(name = "Autonomous", group = "Testing")
public class HDAutonomous extends HDOpMode{

    private enum Strategy
    {
        DO_NOTHING,
        BEACON_CAP_BALL,
    }

    public enum StartPosition
    {
        CORNER_VORTEX,
    }

    private HDAuto mHDAuto = null;
    private double delay = 0.0;
    private Strategy strategy = Strategy.BEACON_CAP_BALL;
    private Alliance alliance = Alliance.RED_ALLIANCE;
    private StartPosition startPosition = StartPosition.CORNER_VORTEX;

    @Override
    public void initialize() {


        HDNumberMenu delayMenu = new HDNumberMenu("Delay", 0, 30, 1, 0, "Seconds", null, gamepad1);

        HDTextMenu strategyMenu = new HDTextMenu("Strategy", delayMenu, gamepad1);
        strategyMenu.addChoice("Do Nothing", Strategy.DO_NOTHING);
        strategyMenu.addChoice("Beacon and Cap Ball", Strategy.BEACON_CAP_BALL);

        HDTextMenu allianceMenu = new HDTextMenu("Alliance", strategyMenu, gamepad1);
        allianceMenu.addChoice("Red Alliance", Alliance.RED_ALLIANCE);
        allianceMenu.addChoice("Blue Alliance", Alliance.BLUE_ALLIANCE);

        HDMenu.runMenus(allianceMenu);

        delay = delayMenu.getValue();
        alliance = (Alliance) allianceMenu.getChoice();

        switch (strategy){
            case DO_NOTHING:
                mHDAuto = new AutoDoNothing();
                break;
            case BEACON_CAP_BALL:
                mHDAuto = new AutoBeaconCapBall(delay, alliance, startPosition);
                break;
        }

        mDisplay.displayPrintf(1, HDDashboard.textPosition.Centered, "Selected Options: ");
        mDisplay.displayPrintf(3, HDDashboard.textPosition.Centered, "Alliance: " + alliance.toString());
        mDisplay.displayPrintf(2, HDDashboard.textPosition.Centered, "Strategy: " + strategy.toString());
        mDisplay.displayPrintf(4, HDDashboard.textPosition.Centered, "Delay: " + delay + " Seconds");
        mDisplay.displayPrintf(5, HDDashboard.textPosition.Centered, "Start Position: " + startPosition.toString());
    }

    @Override
    public void initializeLoop() {
    }

    @Override
    public void Start() {
        mHDAuto.start();
    }

    @Override
    public void continuousRun(double elapsedTime) {
        mHDAuto.runLoop(elapsedTime);
    }

}
