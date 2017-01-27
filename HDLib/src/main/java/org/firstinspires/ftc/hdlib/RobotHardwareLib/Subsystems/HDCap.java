package org.firstinspires.ftc.hdlib.RobotHardwareLib.Subsystems;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

/**
 * Created by Akash on 1/25/2017.
 */
public class HDCap {

    DcMotor capMotor;
    final int liftExtended = 3000;
    final int liftRetracted = 0;

    public HDCap(DcMotor capMotor){
        this.capMotor = capMotor;
        capMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        capMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        capMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        capMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        capMotor.setPower(0);
    }

    public void extendLift(){
        capMotor.setPower(0.3);
    }

    public void resetEncoders(){
        capMotor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        capMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void retractLift(){
        capMotor.setPower(-0.1);

    }

}
