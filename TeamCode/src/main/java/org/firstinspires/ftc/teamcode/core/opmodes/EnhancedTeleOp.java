package org.firstinspires.ftc.teamcode.core.opmodes;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.core.controller.Controller;
import org.firstinspires.ftc.teamcode.core.hardware.pipeline.HardwarePipeline;
import org.firstinspires.ftc.teamcode.core.hardware.state.Component;
import org.firstinspires.ftc.teamcode.core.hardware.state.State;
import org.firstinspires.ftc.teamcode.core.magic.runtime.AotRuntime;
import org.firstinspires.ftc.teamcode.core.magic.runtime.HardwareMapDependentReflectionBasedMagicRuntime;
import org.firstinspires.ftc.teamcode.core.magic.runtime.ReflectionBasedMagicRuntime;
import org.firstinspires.ftc.teamcode.core.magic.runtime.ServiceRuntime;

import java.util.concurrent.ConcurrentHashMap;

public abstract class EnhancedTeleOp extends OpMode {

  protected Controller controller1;
  protected Controller controller2;
  protected final ConcurrentHashMap<String, Object> initializedHardware;
  protected final HardwarePipeline hardwarePipeline;
  private HardwareMapDependentReflectionBasedMagicRuntime aotRuntime;
  private ReflectionBasedMagicRuntime serviceRuntime;
  private Component robotObject;

  public EnhancedTeleOp(HardwarePipeline pipeline) {
    initializedHardware = new ConcurrentHashMap<>();
    hardwarePipeline = pipeline;
    controller1 = new Controller(Constants.GAMEPAD_1_NAME);
    controller2 = new Controller(Constants.GAMEPAD_2_NAME);
  }

  protected void initialize(Component robotObject) {
    this.robotObject = robotObject;
    aotRuntime =
        new AotRuntime(
            robotObject,
            initializedHardware);
    aotRuntime.initialize();
    serviceRuntime = new ServiceRuntime(telemetry, robotObject, controller1, controller2);
    serviceRuntime.initialize();
  }

  @Override
  public final void init() {
    telemetry.setMsTransmissionInterval(Constants.TELEMETRY_TRANSMISSION_INTERVAL);
    aotRuntime.setHardwareMap(hardwareMap);
    aotRuntime.waveWand();

    serviceRuntime.waveWand();
    onInitPressed();
  }

  public abstract void onInitPressed();

  @Override
  public final void init_loop() {
    super.init_loop();
    initLoop();
  }

  public abstract void initLoop();

  @Override
  public final void start() {
    super.start();
    controller1.initialize(gamepad1);
    controller2.initialize(gamepad2);
    onStartPressed();
  }

  public abstract void onStartPressed();

  @Override
  public final void loop() {
    controller1.update();
    controller2.update();
    onLoop();
    hardwarePipeline.process(initializedHardware, robotObject);
  }

  public abstract void onLoop();

  @Override
  public final void stop() {
    super.stop();
    State.clear();
    onStop();
  }

  public abstract void onStop();
}
