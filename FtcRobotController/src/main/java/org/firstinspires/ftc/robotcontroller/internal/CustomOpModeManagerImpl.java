package org.firstinspires.ftc.robotcontroller.internal;

import android.app.Activity;

import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeManagerImpl;

/** {@link CustomOpModeManagerImpl} is the owner of the concept of a 'current' opmode. */
@SuppressWarnings("unused,WeakerAccess")
public class CustomOpModeManagerImpl extends OpModeManagerImpl {
  private static final String AUTO_ONLY_PREFIX = "CB_AUTO_";

  public CustomOpModeManagerImpl(Activity activity, HardwareMap hardwareMap) {
    super(activity, hardwareMap);
  }

  @Override
  protected void callActiveOpModeInit() {
    synchronized (this.listeners) {
      for (OpModeManagerNotifier.Notifications listener : this.listeners) {
        listener.onOpModePreInit(activeOpMode);
      }
    }
    for (HardwareDevice device : this.hardwareMap) {
      if (device instanceof OpModeManagerNotifier.Notifications) {
        if (hardwareMap.getNamesOf(device).stream()
            .findFirst()
            .orElse("")
            .startsWith(AUTO_ONLY_PREFIX)) {
          if (activeOpModeName.startsWith(AUTO_ONLY_PREFIX)) {
            ((OpModeManagerNotifier.Notifications) device).onOpModePreInit(activeOpMode);
          }
        } else {
          ((OpModeManagerNotifier.Notifications) device).onOpModePreInit(activeOpMode);
        }
      }
    }

    activeOpMode.internalPreInit();
    try {
      detectStuck(activeOpMode.msStuckDetectInit, "init()", activeOpMode::init, true);
    } catch (ForceStopException e) {
      /*
       * OpMode ran away in init() but we were able force stop him.
       * Get out of dodge with a switch to the StopRobot OpMode.
       */
      initActiveOpMode(DEFAULT_OP_MODE_NAME);
      skipCallToStop = true;
    }
  }
}
