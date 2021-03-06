package org.firstinspires.ftc.teamcode.opmodes.tele;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.core.controller.BooleanSurface;
import org.firstinspires.ftc.teamcode.core.controller.ScalarSurface;
import org.firstinspires.ftc.teamcode.core.opmodes.EnhancedTeleOp;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.lifts.FourHeightLiftState;
import org.firstinspires.ftc.teamcode.hardware.robots.sliderliftbot.SliderLiftBot;

import java.util.concurrent.atomic.AtomicBoolean;

@TeleOp(name = "SliderLiftTele")
@Disabled
@SuppressWarnings("unused")
public class SliderLiftTele extends EnhancedTeleOp {
  private final SliderLiftBot robot;
  private final AtomicBoolean halfSpeed = new AtomicBoolean(false);
  private final AtomicBoolean alreadyIntaking = new AtomicBoolean(false);
  private final AtomicBoolean alreadyOuttaking = new AtomicBoolean(false);

  public SliderLiftTele() {
    super(new SliderLiftBot());
    this.robot = (SliderLiftBot) robotObject;
  }

  private static double THIRD_MANIPULATION(double in) {
    return Math.pow(in, 3);
  }

  @Override
  public void onInitPressed() {}

  @Override
  public void initLoop() {}

  @Override
  public void onStartPressed() {
    controller1.setManipulation(SliderLiftTele::THIRD_MANIPULATION, ScalarSurface.LEFT_STICK_Y);

    controller1.registerOnPressedCallback(
        () -> {
          if (robot.intake.getState().isLowered()) {
            robot.intake.raise();
          } else {
            robot.intake.lower();
          }
        },
        true,
        BooleanSurface.DPAD_DOWN);

    controller1.registerOnPressedCallback(
        () -> halfSpeed.set(!halfSpeed.get()), true, BooleanSurface.X);

    controller2.registerOnPressedCallback(
        robot.carouselSpinner::spinForward, true, BooleanSurface.LEFT_STICK);
    controller2.registerOnPressedCallback(
        robot.carouselSpinner::spinBackward, true, BooleanSurface.RIGHT_STICK);

    controller2.registerOnPressedCallback(
        () -> {
          if (alreadyIntaking.get()) {
            robot.intake.stop();
            alreadyIntaking.set(false);
          } else if (robot.fourHeightLift.getState() == FourHeightLiftState.HEIGHT_0) {
            robot.intake.beginIntaking();
            alreadyIntaking.set(true);
          }
        },
        true,
        BooleanSurface.RIGHT_BUMPER);
    controller2.registerOnPressedCallback(
        () -> {
          if (alreadyOuttaking.get()) {
            robot.intake.stop();
            alreadyOuttaking.set(false);
          } else if (robot.fourHeightLift.getState() == FourHeightLiftState.HEIGHT_0) {
            robot.intake.beginOuttaking();
            alreadyOuttaking.set(true);
          }
        },
        true,
        BooleanSurface.LEFT_BUMPER);

    controller2.registerOnPressedCallback(
        () -> {
          robot.outtakeBucket.carry();
          robot.fourHeightLift.goToHeight0();
        },
        true,
        BooleanSurface.A);
    controller2.registerOnPressedCallback(
        () -> {
          robot.outtakeBucket.carry();
          robot.intake.stop();
          robot.fourHeightLift.goToHeight1();
        },
        true,
        BooleanSurface.X);
    controller2.registerOnPressedCallback(
        () -> {
          robot.outtakeBucket.carry();
          robot.intake.stop();
          robot.fourHeightLift.goToHeight2();
        },
        true,
        BooleanSurface.B);
    controller2.registerOnPressedCallback(
        () -> {
          robot.outtakeBucket.carry();
          robot.intake.stop();
          robot.fourHeightLift.goToHeight3();
        },
        true,
        BooleanSurface.Y);

    controller2.registerOnPressedCallback(
        () -> {
          if (robot.fourHeightLift.getState() != FourHeightLiftState.HEIGHT_0) {
            robot.outtakeBucket.dump();
          }
        },
        true,
        BooleanSurface.DPAD_RIGHT);
  }

  @Override
  public void onLoop() {
    double turnValue = controller1.rightStickX();
    double speed = halfSpeed.get() ? 0.5 : 1;
    robot.drivetrain.driveBySticks(
        -controller1.leftStickX() * speed,
        -controller1.leftStickY() * speed,
        -turnValue * .9 * speed);
  }

  @Override
  public void onStop() {}
}
