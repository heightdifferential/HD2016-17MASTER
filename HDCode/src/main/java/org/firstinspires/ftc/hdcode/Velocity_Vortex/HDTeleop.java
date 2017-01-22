package org.firstinspires.ftc.hdcode.Velocity_Vortex;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.hdlib.Controls.HDGamepad;
import org.firstinspires.ftc.hdlib.General.Alliance;
import org.firstinspires.ftc.hdlib.HDRobot;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDOpMode;
import org.firstinspires.ftc.hdlib.Telemetry.HDDiagnosticDisplay;


/**
 * Created by Akash on 5/7/2016.
 */

@TeleOp(name = "HDTeleop", group = "Teleop")
public class HDTeleop extends HDOpMode implements HDGamepad.HDButtonMonitor{

    private enum DriveMode{
        TANK_DRIVE,
        MECANUM_FIELD_CENTRIC,
    }

    double speed = 0.6;
    HDDiagnosticDisplay diagnosticDisplay;
    HDRobot robot;
    DriveMode driveMode;
    HDGamepad driverGamepad;
    HDGamepad servoBoyGamepad;
    Alliance alliance;
    ElapsedTime intervalRun;

    //Flywheel Management Variables
    static double FlywheelSpeed = 0.37;
    ElapsedTime shooterTime;

    //Flywheel RPM Calc Variables
    double deltaMS = 0;
    double delta_enc = 0;
    double curEncoder = 0;
    double calcTime = 0;
    double lastEncoder = 0;
    double flywheelRPM = 0;
    static double flywheelTicksperRev = 1120;

    @Override
    public void initialize() {
        try {
            alliance = Alliance.retrieveAlliance(hardwareMap.appContext); //Retrieve Last Alliance from Autonomous.
        }catch (Exception e){
            alliance = Alliance.BLUE_ALLIANCE;
        }
        robot = new HDRobot(alliance);
        diagnosticDisplay = new HDDiagnosticDisplay(mDisplay, robot.driveHandler);
        driverGamepad = new HDGamepad(gamepad1, this);
        servoBoyGamepad = new HDGamepad(gamepad2, this);
        robot.shooter.raiseCollector();
        shooterTime = new ElapsedTime();
        intervalRun = new ElapsedTime();
    }

    @Override
    public void initializeLoop() {

    }


    @Override
    public void Start(){
        if(robot.navX.getSensorData().isCalibrating()) {
            driveMode = DriveMode.TANK_DRIVE;
        } else {
            driveMode = DriveMode.MECANUM_FIELD_CENTRIC;
        }
        driverGamepad.setGamepad(gamepad1);
        servoBoyGamepad.setGamepad(gamepad2);
        robot.shooter.lowerCollector();
        robot.shooter.resetEncoders();
    }

    @Override
    public void continuousRun(double elapsedTime) {
        diagnosticDisplay.addProgramSpecificTelemetry(1, "Alliance: %s", alliance.toString());
        diagnosticDisplay.addProgramSpecificTelemetry(2, "Drive Mode: %s", driveMode.toString());
        diagnosticDisplay.addProgramSpecificTelemetry(3, "Drive Speed: "+ String.valueOf(speed*100) + " Percent");
        robotDrive();
        shooterSubsystem();
        if(intervalRun.milliseconds() > 35){
            intervalRun.reset();
            calculateFlywheelRPM();
        }
    }

    private void shooterSubsystem() {
        robot.shooter.setFlywheelPower(FlywheelSpeed);
        if(gamepad1.left_trigger > .5){
            if(shooterTime.milliseconds() < 200) {
                robot.shooter.setAcceleratorPower(-1);
                robot.shooter.setCollectorPower(-.75);
            }else if(shooterTime.milliseconds() < 520){
                robot.shooter.setAcceleratorPower(1);
                robot.shooter.setCollectorPower(.75);
            }else{
                shooterTime.reset();
            }
        }else{
            robot.shooter.setAcceleratorPower(-1);
            robot.shooter.setFlywheelPower(FlywheelSpeed);
            robot.shooter.setCollectorPower(.75);
            shooterTime.reset();
        }
    }

    private void calculateFlywheelRPM(){
        curEncoder = robot.shooter.getFlywheelEncoderCount();

        deltaMS = System.currentTimeMillis() - calcTime;
        calcTime = System.currentTimeMillis();

        delta_enc = curEncoder - lastEncoder;

        lastEncoder = curEncoder;

        flywheelRPM = (1000.0/deltaMS) * delta_enc * 60.0 / flywheelTicksperRev;

        diagnosticDisplay.addProgramSpecificTelemetry(3, "Flywheel RPM: " + String.valueOf(flywheelRPM));
    }


    private void robotDrive(){
            switch (driveMode) {
                case TANK_DRIVE:
                    robot.driveHandler.tankDrive(-gamepad1.left_stick_y*speed, -gamepad1.right_stick_y*speed);
                    break;
                case MECANUM_FIELD_CENTRIC:
                    if(gamepad1.y){
                        robot.driveHandler.mecanumDrive_Cartesian_keepFrontPos(gamepad1.left_stick_x*.5, gamepad1.left_stick_y*.5, 180.0, robot.navX.getYaw());
                    }else if(gamepad1.b){
                        robot.driveHandler.mecanumDrive_Cartesian_keepFrontPos(gamepad1.left_stick_x*.5, gamepad1.left_stick_y*.5, -90.0, robot.navX.getYaw());
                    }else{
                        robot.driveHandler.mecanumDrive_Cartesian(gamepad1.left_stick_x * speed, gamepad1.left_stick_y * speed, gamepad1.right_stick_x * speed, robot.navX.getYaw());
                    }
                    break;
            }
    }

    @Override
    public void buttonChange(HDGamepad instance, HDGamepad.gamepadButtonChange button, boolean pressed) {
        if(instance == driverGamepad){
            switch (button) {
                case A:
                    break;
                case B:
                    break;
                case X:
                    break;
                case Y:
                    break;
                case DPAD_LEFT:
                    break;
                case DPAD_RIGHT:
                    break;
                case DPAD_UP:
                    if(pressed && !robot.navX.getSensorData().isCalibrating())
                        driveMode = DriveMode.MECANUM_FIELD_CENTRIC;
                    break;
                case DPAD_DOWN:
                    if(pressed)
                        driveMode = DriveMode.TANK_DRIVE;
                    break;
                case LEFT_BUMPER:
                    if(pressed){
                        speed = speed - 0.2;
                        speed = Range.clip(speed, 0.2,1);
                    }
                    break;
                case RIGHT_BUMPER:
                    if(pressed){
                        speed = speed + 0.2;
                        speed = Range.clip(speed, 0.2,1);
                    }
                    break;
                case LEFT_TRIGGER:
                    break;
                case RIGHT_TRIGGER:
                    break;
                case START:
                    if(pressed)
                        robot.navX.zeroYaw();
                    break;
            }
        }else if(instance == servoBoyGamepad){
            switch (button) {
                case A:
                    break;
                case B:
                    break;
                case X:
                    break;
                case Y:
                    break;
                case DPAD_LEFT:
                    break;
                case DPAD_RIGHT:
                    break;
                case DPAD_UP:
                    break;
                case DPAD_DOWN:
                    break;
                case LEFT_BUMPER:
                    break;
                case RIGHT_BUMPER:
                    break;
                case LEFT_TRIGGER:
                    break;
                case RIGHT_TRIGGER:
                    break;
                case START:
                    break;
            }
        }
    }
}
