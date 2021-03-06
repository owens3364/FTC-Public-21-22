package org.firstinspires.ftc.teamcode.core.hardware.pipeline;

import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.core.hardware.state.IMotorState;
import org.firstinspires.ftc.teamcode.core.hardware.state.IServoState;
import org.firstinspires.ftc.teamcode.core.hardware.state.State;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExitPipe extends HardwarePipeline {
  private static ExitPipe instance;
  private List<Runnable> postLoopFunctions = new LinkedList<>();

  public ExitPipe(String name) {
    super(name);
    ExitPipe.instance = this;
  }

  public static ExitPipe getInstance() {
    return instance;
  }

  public synchronized void onNextTick(Runnable r) {
    postLoopFunctions.add(r);
  }

  public synchronized void clearScheduledCallbacks() {
    postLoopFunctions.clear();
  }

  @Override
  public StateFilterResult process(Map<String, Object> hardware, StateFilterResult r) {
    r.getNextMotorStates().forEach((m) -> processMotor(m, hardware));
    r.getNextServoStates().forEach((s) -> processServo(s, hardware));
    List<Runnable> functions;
    synchronized (this) {
      functions = postLoopFunctions;
      postLoopFunctions = new LinkedList<>();
    }
    functions.forEach(Runnable::run);
    return super.process(hardware, r);
  }

  @SuppressWarnings("all")
  private void processMotor(IMotorState nextState, Map<String, Object> hardware) {
    IMotorState currentState = State.currentStateOf(nextState.getName());
    DcMotorEx motorObj = (DcMotorEx) hardware.get(nextState.getName());
    boolean noState = currentState == null;
    if (noState || currentState.getDirection() != nextState.getDirection()) {
      motorObj.setDirection(nextState.getDirection().primitiveConversion());
    }
    if (noState || currentState.getRunMode() != nextState.getRunMode()) {
      motorObj.setMode(nextState.getRunMode().primitiveConversion());
    }
    if (noState || currentState.getZeroPowerBehavior() != nextState.getZeroPowerBehavior()) {
      motorObj.setZeroPowerBehavior(nextState.getZeroPowerBehavior().primitiveConversion());
    }
    if (noState || currentState.getPower() != nextState.getPower()) {
      motorObj.setPower(nextState.getPower());
    }
    nextState.makeCurrent();
  }

  @SuppressWarnings("all")
  private void processServo(IServoState nextState, Map<String, Object> hardware) {
    IServoState currentState = State.currentStateOf(nextState.getName());
    if (!nextState.isContinuousRotation()) {
      Servo servoObj = (Servo) hardware.get(nextState.getName());
      boolean noState = currentState == null;
      if (noState || currentState.getDirection() != nextState.getDirection()) {
        servoObj.setDirection(nextState.getDirection().primitiveServoConversion());
      }
      if (noState || currentState.getPosition() != nextState.getPosition()) {
        servoObj.setPosition(nextState.getPosition());
      }
    } else {
      CRServo servoObj = (CRServo) hardware.get(nextState.getName());
      boolean noState = currentState == null;
      if (noState || currentState.getDirection() != nextState.getDirection()) {
        servoObj.setDirection(nextState.getDirection().primitiveConversion());
      }
      if (noState || currentState.getPosition() != nextState.getPosition()) {
        servoObj.setPower(nextState.getPosition());
      }
    }
    nextState.makeCurrent();
  }
}
