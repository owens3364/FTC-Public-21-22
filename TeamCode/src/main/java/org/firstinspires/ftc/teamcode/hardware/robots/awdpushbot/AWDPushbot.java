package org.firstinspires.ftc.teamcode.hardware.robots.awdpushbot;

import org.firstinspires.ftc.teamcode.core.hardware.state.Component;
import org.firstinspires.ftc.teamcode.core.hardware.state.State;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.drivetrains.AWDClassicDrivetrain;

import java.util.List;

@SuppressWarnings("unused")
public class AWDPushbot implements Component {
  public final AWDClassicDrivetrain drivetrain = new AWDClassicDrivetrain();

  @Override
  public String getName() {
    return this.getClass().getName();
  }

  @Override
  public List<? super State> getNextState() {
    return drivetrain.getNextState();
  }
}
