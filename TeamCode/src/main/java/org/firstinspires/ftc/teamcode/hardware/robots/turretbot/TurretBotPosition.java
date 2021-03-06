package org.firstinspires.ftc.teamcode.hardware.robots.turretbot;

import org.firstinspires.ftc.teamcode.core.game.related.Alliance;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.auxiliary.Turret;
import org.firstinspires.ftc.teamcode.hardware.mechanisms.lifts.DualJointAngularLift;

public enum TurretBotPosition {
  INTAKE_POSITION,
  INTAKE_HOVER_POSITION,
  SHARED_CLOSE_POSITION,
  SHARED_MIDDLE_POSITION,
  SHARED_FAR_POSITION,
  TIPPED_CLOSE_POSITION,
  TIPPED_MIDDLE_POSITION,
  TIPPED_FAR_POSITION,
  ALLIANCE_BOTTOM_POSITION,
  ALLIANCE_MIDDLE_POSITION,
  ALLIANCE_TOP_POSITION,
  TEAM_MARKER_GRAB_POSITION,
  TEAM_MARKER_DEPOSIT_POSITION,
  AUTO_REACH_BOTTOM,
  AUTO_REACH_MIDDLE,
  AUTO_REACH_TOP;

  public int turretTarget(Alliance alliance) {
    switch (this) {
      case SHARED_CLOSE_POSITION:
      case SHARED_MIDDLE_POSITION:
      case SHARED_FAR_POSITION:
      case TIPPED_CLOSE_POSITION:
      case TIPPED_MIDDLE_POSITION:
      case TIPPED_FAR_POSITION:
        return alliance == Alliance.RED
            ? Turret.TICKS_RIGHT
            : alliance == Alliance.BLUE ? Turret.TICKS_LEFT : Turret.TICKS_FRONT;
      case ALLIANCE_BOTTOM_POSITION:
      case ALLIANCE_MIDDLE_POSITION:
      case ALLIANCE_TOP_POSITION:
      case TEAM_MARKER_GRAB_POSITION:
      case TEAM_MARKER_DEPOSIT_POSITION:
        return alliance == Alliance.RED
            ? Turret.TICKS_CCW_BACK
            : alliance == Alliance.BLUE ? Turret.TICKS_CW_BACK : Turret.TICKS_FRONT;
      case AUTO_REACH_BOTTOM:
      case AUTO_REACH_MIDDLE:
      case AUTO_REACH_TOP:
        return alliance == Alliance.RED
            ? Turret.TICKS_DIAGONAL_RIGHT
            : alliance == Alliance.BLUE ? Turret.TICKS_DIAGONAL_LEFT : Turret.TICKS_FRONT;
      default:
        return Turret.TICKS_FRONT;
    }
  }

  public int firstJointTarget() {
    switch (this) {
      case INTAKE_POSITION:
        return -210;
      case SHARED_CLOSE_POSITION:
      case SHARED_MIDDLE_POSITION:
        return 10;
      case SHARED_FAR_POSITION:
        return -250;
      case TIPPED_CLOSE_POSITION:
        return 160;
      case TIPPED_MIDDLE_POSITION:
        return 110;
      case TIPPED_FAR_POSITION:
        return -120;
      case ALLIANCE_BOTTOM_POSITION:
        return -460;
      case AUTO_REACH_BOTTOM:
        return -465;
      case ALLIANCE_MIDDLE_POSITION:
        return -100;
      case AUTO_REACH_MIDDLE:
        return -135;
      case ALLIANCE_TOP_POSITION:
        return 540;
      case AUTO_REACH_TOP:
        return 135;
      case TEAM_MARKER_GRAB_POSITION:
        return -570;
      case TEAM_MARKER_DEPOSIT_POSITION:
        return 500;
      default:
        return 0;
    }
  }

  public double secondJointTarget() {
    switch (this) {
      case SHARED_CLOSE_POSITION:
        return 0.715;
      case SHARED_MIDDLE_POSITION:
        return 0.515;
      case SHARED_FAR_POSITION:
      case TIPPED_FAR_POSITION:
      case AUTO_REACH_MIDDLE:
        return 0.235;
      case TIPPED_CLOSE_POSITION:
        return 0.695;
      case TIPPED_MIDDLE_POSITION:
        return 0.475;
      case ALLIANCE_BOTTOM_POSITION:
      case TEAM_MARKER_GRAB_POSITION:
        return 0.115;
      case AUTO_REACH_BOTTOM:
        return 0.145;
      case ALLIANCE_MIDDLE_POSITION:
        return 0.265;
      case ALLIANCE_TOP_POSITION:
        return 0.555;
      case AUTO_REACH_TOP:
        return 0.285;
      case TEAM_MARKER_DEPOSIT_POSITION:
        return 0.415;
      default:
        return DualJointAngularLift.LIFT_JOINT_TWO_INTAKE_POSITION;
    }
  }
}
