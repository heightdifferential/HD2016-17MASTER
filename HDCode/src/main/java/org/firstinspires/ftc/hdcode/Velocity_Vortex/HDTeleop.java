package org.firstinspires.ftc.hdcode.Velocity_Vortex;

import android.util.Log;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.hdlib.Controls.HDGamepad;
import org.firstinspires.ftc.hdlib.General.Alliance;
import org.firstinspires.ftc.hdlib.HDRobot;
import org.firstinspires.ftc.hdlib.OpModeManagement.HDOpMode;
import org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems.HDCap;
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

    private enum ShootingModes{
        Bring_Ball_Down1,
        Shoot_Ball1,
        Bring_Ball_Down2,
        Shoot_Ball2,
        Bring_Ball_Down3,
        Shoot_Ball3,
    }

    HDDiagnosticDisplay diagnosticDisplay;
    HDRobot robot;
    DriveMode driveMode;
    HDGamepad driverGamepad;
    HDGamepad servoBoyGamepad;
    Alliance alliance;
    ShootingModes shootingMode = ShootingModes.Bring_Ball_Down1;
    int shootTimes = 0;

    private final double flywheelSpeed = 0.8;
    double shootingTimer = 0.0;
    double driveSpeed = 1.0;
    boolean flywheelRunning = false;
    boolean collectorForward = true;
    boolean shooting = false;
    boolean liftManualAdjust = false;
    boolean collectorUp = false;
    boolean flipGyro = false;

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
        robot.lift.lowerArms();
    }

    @Override
    public void initializeLoop() {

    }


    @Override
    public void Start(){
        driveMode = DriveMode.MECANUM_FIELD_CENTRIC;
        driverGamepad.setGamepad(gamepad1);
        servoBoyGamepad.setGamepad(gamepad2);
        robot.shooter.lowerCollector();
        robot.shooter.resetEncoders();
    }

    @Override
    public void continuousRun(double elapsedTime) {
        diagnosticDisplay.addProgramSpecificTelemetry(1, "Alliance: %s", alliance.toString());
        diagnosticDisplay.addProgramSpecificTelemetry(2, "Drive Mode: %s", driveMode.toString());
        if(robot.capLift.getCurrentPosition() > 650) {
            diagnosticDisplay.addProgramSpecificTelemetry(3, "Drive Speed: " + String.valueOf(15) + " Percent");
        }
        else {
            diagnosticDisplay.addProgramSpecificTelemetry(3, "Drive Speed: " + String.valueOf(driveSpeed * 100) + " Percent");
        }
        diagnosticDisplay.addProgramSpecificTelemetry(4, "Flywheel Enabled: %s", String.valueOf(flywheelRunning));
        diagnosticDisplay.addProgramSpecificTelemetry(5, "Lift Motor Power, Target Pos: %.2f, %d", robot.capLift.getPower(), robot.capLift.getTargetPosition());
        diagnosticDisplay.addProgramSpecificTelemetry(6, "Lift Motor Position: " + String.valueOf(robot.capLift.getCurrentPosition()));
        diagnosticDisplay.addProgramSpecificTelemetry(7, "Lift Motor Mode: " + String.valueOf(robot.capLift.getMode()));
        diagnosticDisplay.addProgramSpecificTelemetry(8, "Collector Encoder Count: %.2f", robot.shooter.getCollectorEncoderCount());
        diagnosticDisplay.addProgramSpecificTelemetry(9, "Flip Gyro: " + String.valueOf(flipGyro));
        diagnosticDisplay.addProgramSpecificTelemetry(10, String.valueOf(robot.flywheel1.getCurrentPosition()) + ":" + String.valueOf(robot.flywheel2.getCurrentPosition()));
        robotDrive();
        shooterSubsystem();
        liftSubsystem();
    }


    private void shooterSubsystem(){
        if(flywheelRunning){
                robot.shooter.setFlywheelPower(flywheelSpeed);
        }else{
            robot.shooter.setFlywheelPower(0);
        }
        if(shooting){
            switch (shootingMode) {
                case Bring_Ball_Down1:
                    robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                    if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 50) {
                        robot.shooter.setCollectorPower(0);
                        robot.shooter.setAcceleratorPower(0);
                        robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        shootingTimer = System.currentTimeMillis();
                        shootingMode = ShootingModes.Shoot_Ball1;
                    }else{
                        robot.shooter.setCollectorPower(-1);
                        robot.shooter.setAcceleratorPower(-1);
                    }
                    break;
                case Shoot_Ball1:
                            robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 200) {
                                robot.shooter.setCollectorPower(0);
                                robot.shooter.setAcceleratorPower(0);
                                robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                                robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                                shootingTimer = System.currentTimeMillis();
                                shootingMode = ShootingModes.Bring_Ball_Down2;
                            }else{
                                robot.shooter.setCollectorPower(1);
                                robot.shooter.setAcceleratorPower(1);
                            }
                    break;
                case Bring_Ball_Down2:
                    if((shootingTimer + 250) < System.currentTimeMillis()) {
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 50) {
                            robot.shooter.setCollectorPower(0);
                            robot.shooter.setAcceleratorPower(0);
                            robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            shootingTimer = System.currentTimeMillis();
                            shootingMode = ShootingModes.Shoot_Ball2;
                        }else{
                            robot.shooter.setCollectorPower(-1);
                            robot.shooter.setAcceleratorPower(-1);
                        }
                    }
                    break;
                case Shoot_Ball2:
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 200) {
                            robot.shooter.setCollectorPower(0);
                            robot.shooter.setAcceleratorPower(0);
                            robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            shootingTimer = System.currentTimeMillis();
                            shootingMode = ShootingModes.Bring_Ball_Down3;
                        }else{
                            robot.shooter.setCollectorPower(1);
                            robot.shooter.setAcceleratorPower(1);
                        }
                    break;
                case Bring_Ball_Down3:
                    if((shootingTimer + 250) < System.currentTimeMillis()) {
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 50) {
                            robot.shooter.setCollectorPower(0);
                            robot.shooter.setAcceleratorPower(0);
                            robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            shootingTimer = System.currentTimeMillis();
                            shootingMode = ShootingModes.Shoot_Ball3;
                        }else{
                            robot.shooter.setCollectorPower(-1);
                            robot.shooter.setAcceleratorPower(-1);
                        }
                    }
                    break;
                case Shoot_Ball3:
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        if (Math.abs(robot.collectorMotor.getCurrentPosition()) > 320) {
                            robot.shooter.setCollectorPower(0);
                            robot.shooter.setAcceleratorPower(0);
                            robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                            robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                            shootingTimer = System.currentTimeMillis();
                            shooting = false;
                        }else{
                            robot.shooter.setCollectorPower(1);
                            robot.shooter.setAcceleratorPower(1);
                        }
                    break;
            }
        }else{
            if(collectorUp){
                robot.shooter.raiseCollector();
            }else{
                robot.shooter.lowerCollector();
            }
            if(gamepad1.a && !collectorUp){
                robot.shooter.setCollectorPower(-.6);
                robot.shooter.setAcceleratorPower(-1);
            }
            else if(collectorForward){
                robot.shooter.setCollectorPower(.6);
                robot.shooter.setAcceleratorPower(-1);
            }else{
                robot.shooter.setCollectorPower(0);
                robot.shooter.setAcceleratorPower(0);
            }
        }
    }

    private void liftSubsystem(){
        if(liftManualAdjust){
            robot.lift.setPower(-(gamepad2.left_stick_y*0.5));
        }
        else if(robot.lift.curLiftMode == HDCap.liftMode.TOP){
            double pos = robot.lift.capMotor.getCurrentPosition();
            if(pos < 10500){
                robot.lift.capMotor.setPower(1);
            }else{
                robot.lift.capMotor.setPower(0.1);
            }
        }else if(robot.lift.curLiftMode == HDCap.liftMode.BOTTOM){
            double pos = robot.lift.capMotor.getCurrentPosition();
            if(pos > 1000){
                robot.lift.setMotorMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                robot.lift.capMotor.setPower(-0.5);
            }else if(pos > 0){
                robot.lift.lowerArms();
                robot.lift.setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
                robot.lift.capMotor.setTargetPosition(0);
                robot.lift.capMotor.setPower(-0.2);
            }
        }
    }

    private void robotDrive(){
        switch (driveMode) {
            case TANK_DRIVE:
                if(robot.capLift.getCurrentPosition() > 650){
                    robot.driveHandler.tankDrive(-gamepad1.left_stick_y* 0.15, -gamepad1.right_stick_y* 0.15);
                }else{
                    robot.driveHandler.tankDrive(-gamepad1.left_stick_y* driveSpeed, -gamepad1.right_stick_y* driveSpeed);
                }
                break;
            case MECANUM_FIELD_CENTRIC:
                if(gamepad1.y){
                    robot.driveHandler.mecanumDrive_Cartesian_keepFrontPos(gamepad1.left_stick_x*.2, gamepad1.left_stick_y*.2, 180.0, robot.navX.getYaw());
                }else if(gamepad1.b){
                    robot.driveHandler.mecanumDrive_Cartesian_keepFrontPos(gamepad1.left_stick_x*.2, gamepad1.left_stick_y*.2, -90.0, robot.navX.getYaw());
                }else if(robot.capLift.getCurrentPosition() > 650){
                    robot.driveHandler.mecanumDrive_Cartesian(gamepad1.left_stick_x * .15, gamepad1.left_stick_y * .15, gamepad1.right_stick_x * .15, robot.navX.getYaw());
                }
                else{
                    robot.driveHandler.mecanumDrive_Cartesian(gamepad1.left_stick_x * driveSpeed, gamepad1.left_stick_y * driveSpeed, gamepad1.right_stick_x * driveSpeed, robot.navX.getYaw());
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
                    if(!pressed)
                        robot.driveHandler.firstRun = true;
                    break;
                case X:
                    if(pressed){
                        if(collectorForward){
                            collectorForward = !collectorForward;
                        }else{
                            collectorUp = false;
                            collectorForward = true;
                        }
                    }

                    break;
                case Y:
                    if(!pressed)
                        robot.driveHandler.firstRun = true;
                    break;
                case DPAD_LEFT:
                    if(pressed){
                        if(collectorUp){
                            collectorUp = false;
                        }else{
                            collectorForward = false;
                            collectorUp = true;
                        }
                    }
                    break;
                case DPAD_RIGHT:
                    if(pressed){
                        flipGyro = !flipGyro;
                        robot.navX.flipGyro(flipGyro);
                    }
                    break;
                case DPAD_UP:
                    if(pressed)
                        driveMode = DriveMode.MECANUM_FIELD_CENTRIC;
                    break;
                case DPAD_DOWN:
                    if(pressed)
                        driveMode = DriveMode.TANK_DRIVE;
                    break;
                case LEFT_BUMPER:
                    if(pressed){
                        driveSpeed = driveSpeed - 0.2;
                        driveSpeed = Range.clip(driveSpeed, 0.2, 1);
                    }
                    break;
                case RIGHT_BUMPER:
                    if(pressed){
                        driveSpeed = driveSpeed + 0.2;
                        driveSpeed = Range.clip(driveSpeed, 0.2, 1);
                    }
                    break;
                case LEFT_TRIGGER:
                    if(pressed) {
                        flywheelRunning = !flywheelRunning;
                    }
                    break;
                case RIGHT_TRIGGER:
                    if(pressed && shooting == false){
                        shooting = true;
                        shootingMode = ShootingModes.Bring_Ball_Down1;
                        robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        shootTimes = 0;
                    }
                    break;
                case START:
                    if(pressed)
                        robot.navX.zeroYaw();
                    break;
            }
        }else if(instance == servoBoyGamepad){
            switch (button) {
                case A:
                    if(pressed && !liftManualAdjust) {
                        robot.lift.retractLift();
                        robot.lift.raiseTopArm();
                    }
                    break;
                case B:
                    if(pressed &&!liftManualAdjust) {
                        robot.lift.dropPosition();
                        robot.lift.raiseTopArm();
                    }
                    break;
                case X:
                    if(pressed && !liftManualAdjust) {
                        robot.lift.movePosition();
                        robot.lift.holdCap();
                    }
                    break;
                case Y:
                    if(pressed && !liftManualAdjust) {
                        if (robot.lift.curLiftMode == HDCap.liftMode.CARRY || robot.lift.curLiftMode == HDCap.liftMode.DROP) {
                            robot.lift.extendLift();
                            robot.lift.holdCap();
                        }
                    }
                    break;
                case DPAD_LEFT:
                    break;
                case DPAD_RIGHT:
                    break;
                case DPAD_UP:
                    if(pressed){
                        robot.lift.raiseArms();
                        robot.lift.raiseTopArm();
                    }
                    break;
                case DPAD_DOWN:
                    if(pressed){
                        if(robot.capLift.getCurrentPosition() < 100){
                            robot.lift.lowerTopArm();
                        }
                        robot.lift.lowerArms();
                    }
                    break;
                case LEFT_BUMPER:
                    break;
                case RIGHT_BUMPER:
                    break;
                case LEFT_TRIGGER:
                    if(pressed) {
                        flywheelRunning = !flywheelRunning;
                    }
                    break;
                case RIGHT_TRIGGER:
                    if(pressed && shooting == false){
                        shooting = true;
                        shootingMode = ShootingModes.Bring_Ball_Down1;
                        robot.collectorMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
                        robot.collectorMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        shootTimes = 0;
                    }
                    break;
                case START:
                    if(pressed){
                        robot.lift.setPower(0.0);
                        robot.lift.setMotorMode(DcMotor.RunMode.RUN_USING_ENCODER);
                        liftManualAdjust = true;
                    }else{
                        liftManualAdjust = false;
                        robot.lift.capMotor.setTargetPosition(robot.lift.capMotor.getCurrentPosition());
                        robot.lift.setMotorMode(DcMotor.RunMode.RUN_TO_POSITION);
                    }
                    break;
            }
        }
    }
}