package org.firstinspires.ftc.teamcode.core.hardware.state;

import org.firstinspires.ftc.teamcode.core.hardware.pipeline.CallbackData;
import org.firstinspires.ftc.teamcode.core.hardware.pipeline.MotorTrackerPipe;

public class MotorPositionReachedCallback {
  private final String motorName;
  private final int motorTarget;
  private final int motorTolerance;
  private final boolean targetAlreadyReached;

  public MotorPositionReachedCallback(
      String motorName, int motorTarget, int motorTolerance, boolean targetAlreadyReached) {
    this.motorName = motorName;
    this.motorTarget = motorTarget;
    this.motorTolerance = motorTolerance;
    this.targetAlreadyReached = targetAlreadyReached;
  }

  public void andThen(Runnable r) {
    if (shouldSetCallback(r)) {
      MotorTrackerPipe.getInstance()
          .setCallbackForMotorPosition(
              new CallbackData<>(
                  motorName, (ticks) -> Math.abs(ticks - motorTarget) <= motorTolerance, r));
    }
  }

  public void andAfterMotorIsBelow(int target, int tolerance, Runnable r) {
    if (shouldSetCallback(r)) {
      MotorTrackerPipe.getInstance()
          .setCallbackForMotorPosition(
              new CallbackData<>(motorName, (ticks) -> ticks < target + tolerance, r));
    }
  }

  public void andAfterMotorIsBeyond(int target, int tolerance, Runnable r) {
    if (shouldSetCallback(r)) {
      MotorTrackerPipe.getInstance()
          .setCallbackForMotorPosition(
              new CallbackData<>(motorName, (ticks) -> ticks > target - tolerance, r));
    }
  }

  private boolean shouldSetCallback(Runnable r) {
    if (targetAlreadyReached) {
      r.run();
      return false;
    }
    return true;
  }
}
