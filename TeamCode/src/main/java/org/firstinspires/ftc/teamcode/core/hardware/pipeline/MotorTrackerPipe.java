package org.firstinspires.ftc.teamcode.core.hardware.pipeline;

import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.core.annotations.hardware.RunMode;
import org.firstinspires.ftc.teamcode.core.hardware.state.MotorPositionData;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MotorTrackerPipe extends HardwarePipeline {
  private static MotorTrackerPipe instance;
  private final Map<String, MotorPositionData> motorPositions = new HashMap<>();
  private List<CallbackData<Integer>> motorPositionCallbacks = new LinkedList<>();
  private Map<String, Object> lastHardware;

  public MotorTrackerPipe(String name, HardwarePipeline nextPipe) {
    super(name, nextPipe);
    MotorTrackerPipe.instance = this;
  }

  public static MotorTrackerPipe getInstance() {
    return instance;
  }

  public void setCallbackForMotorPosition(CallbackData<Integer> data) {
    motorPositionCallbacks.add(data);
  }

  public void clearScheduledCallbacks() {
    motorPositionCallbacks.clear();
  }

  public int getPositionOf(String motorName) throws IllegalArgumentException {
    MotorPositionData motorPositionData = motorPositions.get(motorName);
    if (motorPositionData != null) {
      return (int) motorPositionData.getTicks();
    }
    if (lastHardware != null) {
      DcMotorEx motor = (DcMotorEx) lastHardware.get(motorName);
      if (motor != null) {
        return motor.getCurrentPosition();
      }
    }
    throw new IllegalArgumentException("Data Unavailable!");
  }

  public double getVelocity(String motorName) throws IllegalArgumentException {
    MotorPositionData motorPositionData = motorPositions.get(motorName);
    if (motorPositionData != null) {
      return motorPositionData.getVelocity();
    }
    if (lastHardware != null) {
      DcMotorEx motor = (DcMotorEx) lastHardware.get(motorName);
      if (motor != null) {
        return motor.getVelocity();
      }
    }
    throw new IllegalArgumentException("Data Unavailable!");
  }

  @Override
  @SuppressWarnings("all")
  public StateFilterResult process(Map<String, Object> hardware, StateFilterResult r) {
    synchronized (this) {
      lastHardware = hardware;
    }
    r.getNextMotorStates()
        .forEach(
            (m) -> {
              boolean proxyEncoder =
                  (m.getEncoderDataSource() != null && !m.getEncoderDataSource().isEmpty());
              if (m.getRunMode() == RunMode.RUN_TO_POSITION || proxyEncoder) {
                String encoder = proxyEncoder ? m.getEncoderDataSource() : m.getName();
                DcMotorEx motor = ((DcMotorEx) hardware.get(encoder));
                MotorPositionData data = motorPositions.get(m.getName());
                int currentPosition = motor.getCurrentPosition();
                double velocity = motor.getVelocity();
                if (data != null) {
                  data.addDataPoint(currentPosition, velocity);
                } else {
                  motorPositions.put(m.getName(), new MotorPositionData(currentPosition, velocity));
                }
              }
            });
    List<CallbackData<Integer>> functions;
    synchronized (this) {
      functions = motorPositionCallbacks;
      motorPositionCallbacks = new LinkedList<>(functions);
    }
    DataTracker.evaluateCallbacks(
        functions,
        motorPositionCallbacks,
        motorPositions,
        (MotorPositionData data) -> (int) data.getTicks());
    return super.process(hardware, r);
  }
}
