package org.firstinspires.ftc.teamcode.hardware.mechanisms.auxiliary;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.core.annotations.hardware.Direction;
import org.firstinspires.ftc.teamcode.core.annotations.hardware.Hardware;
import org.firstinspires.ftc.teamcode.core.annotations.hardware.LateInit;
import org.firstinspires.ftc.teamcode.core.hardware.state.IServoState;
import org.firstinspires.ftc.teamcode.core.hardware.state.ServoState;
import org.firstinspires.ftc.teamcode.core.hardware.state.State;
import org.firstinspires.ftc.teamcode.core.magic.runtime.HardwareMapDependentReflectionBasedMagicRuntime;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import kotlin.Triple;

public class TapeMeasure implements ITapeMeasure {
  public static final String YAW_SERVO_NAME = "TAPE_MEASURE_YAW_SERVO";
  public static final String PITCH_SERVO_NAME = "TAPE_MEASURE_PITCH_SERVO";
  public static final String LENGTH_SERVO_NAME = "TAPE_MEASURE_LENGTH_SERVO";
  private static final double SERVO_INIT_SPEED = 0;
  private static final double ADJUSTMENT_RATE = 0.001;
  private final AtomicBoolean initialized = new AtomicBoolean(false);

  @Hardware(name = YAW_SERVO_NAME)
  @LateInit
  @SuppressWarnings("unused")
  public Servo yawServo;

  @Hardware(name = PITCH_SERVO_NAME)
  @LateInit
  @SuppressWarnings("unused")
  public Servo pitchServo;

  @Hardware(name = LENGTH_SERVO_NAME)
  @LateInit
  @SuppressWarnings("unused")
  public CRServo lengthServo;

  private IServoState yawServoState;
  private IServoState pitchServoState;
  private IServoState lengthServoState;
  private HardwareMapDependentReflectionBasedMagicRuntime runtime;

  public TapeMeasure() {
    initialize();
  }

  private void initialize() {
    yawServoState =
        new ServoState(YAW_SERVO_NAME, Direction.FORWARD, SERVO_INIT_SPEED, false)
            .withPosition(0.56);
    pitchServoState =
        new ServoState(PITCH_SERVO_NAME, Direction.FORWARD, SERVO_INIT_SPEED, false)
            .withPosition(0.5);
    lengthServoState = new ServoState(LENGTH_SERVO_NAME, Direction.FORWARD, SERVO_INIT_SPEED, true);
  }

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public List<? super State> getNextState() {
    return Arrays.asList(yawServoState, pitchServoState, lengthServoState);
  }

  @Override
  public Triple<Double, Double, Double> getState() {
    return new Triple<>(
        yawServoState.getPosition(), pitchServoState.getPosition(), lengthServoState.getPosition());
  }

  @Override
  public synchronized void adjustYaw(double amt) {
    ensureInitialized();
    double position = yawServoState.getPosition() - (amt * ADJUSTMENT_RATE);
    position = position < 0 ? 0 : position > 1 ? 1 : position;
    yawServoState = yawServoState.withPosition(position);
  }

  @Override
  public synchronized void adjustPitch(double amt) {
    ensureInitialized();
    double position = pitchServoState.getPosition() - (amt * ADJUSTMENT_RATE);
    position = position < 0 ? 0 : position > 1 ? 1 : position;
    pitchServoState = pitchServoState.withPosition(position);
  }

  @Override
  public synchronized void setYaw(double yaw) {
    ensureInitialized();
    yawServoState = yawServoState.withPosition(yaw < 0 ? 0 : yaw > 1 ? 1 : yaw);
  }

  @Override
  public synchronized void setPitch(double pitch) {
    ensureInitialized();
    pitchServoState = pitchServoState.withPosition(pitch < 0 ? 0 : pitch > 1 ? 1 : pitch);
  }

  @Override
  public synchronized void setLengthRate(double amt) {
    ensureInitialized();
    lengthServoState = lengthServoState.withPosition(amt < -1 ? -1 : amt > 1 ? 1 : amt);
  }

  @Override
  public synchronized void setInitRuntime(HardwareMapDependentReflectionBasedMagicRuntime runtime) {
    this.runtime = runtime;
  }

  private void ensureInitialized() {
    if (runtime != null && !initialized.getAndSet(true)) {
      runtime.lateInit(TapeMeasure.YAW_SERVO_NAME);
      runtime.lateInit(TapeMeasure.PITCH_SERVO_NAME);
      runtime.lateInit(TapeMeasure.LENGTH_SERVO_NAME);
    }
  }
}
