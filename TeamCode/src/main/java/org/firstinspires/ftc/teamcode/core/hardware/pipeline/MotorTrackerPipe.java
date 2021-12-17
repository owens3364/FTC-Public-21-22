package org.firstinspires.ftc.teamcode.core.hardware.pipeline;

import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.core.annotations.hardware.RunMode;
import org.firstinspires.ftc.teamcode.core.hardware.state.MotorPositionData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MotorTrackerPipe extends HardwarePipeline {
  private static MotorTrackerPipe instance;
  private final Map<String, MotorPositionData> motorPositions = new HashMap<>();
  private final List<CallbackData<Integer>> motorPositionCallbacks = new LinkedList<>();
  private boolean inPipe = false;

  public MotorTrackerPipe(String name) {
    super(name);
    MotorTrackerPipe.instance = this;
  }

  public MotorTrackerPipe(String name, HardwarePipeline nextPipe) {
    super(name, nextPipe);
    MotorTrackerPipe.instance = this;
  }

  public static MotorTrackerPipe getInstance() {
    return instance;
  }

  public void setCallbackForMotorPosition(CallbackData<Integer> data) {
    if (!inPipe) {
      motorPositionCallbacks.add(data);
    } else {
      ExitPipe.getInstance().onNextTick(() -> motorPositionCallbacks.add(data));
    }
  }

  public int getPositionOf(String motorName) throws IllegalArgumentException {
    MotorPositionData motorPositionData = motorPositions.get(motorName);
    if (motorPositionData != null) {
      return (int) motorPositionData.getCurrentTicks();
    }
    throw new IllegalArgumentException("That Motor is not tracked!");
  }

  public double getVelocity(String motorName) throws IllegalArgumentException {
    MotorPositionData motorPositionData = motorPositions.get(motorName);
    if (motorPositionData != null) {
      return motorPositionData.velocity();
    }
    throw new IllegalArgumentException("That Motor is not tracked!");
  }

  @Override
  @SuppressWarnings("all")
  public StateFilterResult process(Map<String, Object> hardware, StateFilterResult r) {
    inPipe = true;
    r.getNextMotorStates()
        .forEach(
            (m) -> {
              if (m.getRunMode() == RunMode.RUN_TO_POSITION
                  || m.getRunMode() == RunMode.RUN_USING_ENCODER) {
                int pos = ((DcMotor) hardware.get(m.getName())).getCurrentPosition();
                MotorPositionData data = motorPositions.get(m.getName());
                if (data != null && data.getCurrentTicks() != pos) {
                  data.addDataPoint(System.nanoTime(), pos);
                } else {
                  motorPositions.put(m.getName(), new MotorPositionData(System.nanoTime(), pos));
                }
              }
            });
    DataTracker.evaluateCallbacks(
        motorPositionCallbacks,
        motorPositions,
        (MotorPositionData data) -> (int) data.getCurrentTicks());
    inPipe = false;
    return super.process(hardware, r);
  }
}
