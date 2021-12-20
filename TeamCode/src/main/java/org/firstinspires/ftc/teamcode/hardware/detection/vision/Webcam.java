package org.firstinspires.ftc.teamcode.hardware.detection.vision;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.os.Handler;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.android.util.Size;
import org.firstinspires.ftc.robotcore.external.function.Continuation;
import org.firstinspires.ftc.robotcore.external.hardware.camera.Camera;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCaptureRequest;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCaptureSession;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraCharacteristics;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraException;
import org.firstinspires.ftc.robotcore.external.hardware.camera.CameraManager;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.internal.network.CallbackLooper;
import org.firstinspires.ftc.robotcore.internal.system.ContinuationSynchronizer;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import org.firstinspires.ftc.teamcode.core.annotations.PostInit;
import org.firstinspires.ftc.teamcode.core.annotations.hardware.Hardware;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Webcam implements FtcCamera {
  private static final int secondsPermissionTimeout = Integer.MAX_VALUE;
  private static final String CAMERA_NAME = "CB_AUTO_Webcam 1";
  private final Handler callbackHandler = CallbackLooper.getDefault().getHandler();
  private final BlockingQueue<Bitmap> frameQueue = new LinkedBlockingQueue<>(1);

  @Hardware(name = CAMERA_NAME)
  public WebcamName cameraName;

  private CameraManager cameraManager;
  private Camera camera;
  private CameraCaptureSession session;
  private CameraCharacteristics characteristics;

  @Override
  @PostInit(argType = WebcamName.class)
  public synchronized void init() {
    if (cameraName == null) return;
    if (cameraManager == null) {
      cameraManager = ClassFactory.getInstance().getCameraManager();
    }
    if (camera == null) {
      camera =
          cameraManager.requestPermissionAndOpenCamera(
              new Deadline(secondsPermissionTimeout, TimeUnit.SECONDS), cameraName, null);
    }
    characteristics = cameraName.getCameraCharacteristics();
  }

  @Override
  public void start() {
    try {
      ContinuationSynchronizer<CameraCaptureSession> synchronizer =
          new ContinuationSynchronizer<>();
      Size size;
      int fps;
      synchronized (this) {
        size = characteristics.getDefaultSize(ImageFormat.YUY2);
        fps = characteristics.getMaxFramesPerSecond(ImageFormat.YUY2, size);
      }
      synchronized (this) {
        try {
          camera.createCaptureSession(
              Continuation.create(
                  callbackHandler,
                  new CameraCaptureSession.StateCallbackDefault() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                      try {
                        final CameraCaptureRequest captureRequest =
                            camera.createCaptureRequest(ImageFormat.YUY2, size, fps);
                        session.startCapture(
                            captureRequest,
                            (_u, _uu, cameraFrame) -> {
                              Bitmap bmp = captureRequest.createEmptyBitmap();
                              cameraFrame.copyToBitmap(bmp);
                              try {
                                frameQueue.put(bmp);
                                deinit();
                              } catch (InterruptedException e) {
                                StringWriter sw = new StringWriter();
                                PrintWriter pw = new PrintWriter(sw);
                                e.printStackTrace(pw);
                              }
                            },
                            Continuation.create(callbackHandler, (_u, _uu, _uuu) -> {}));
                        synchronizer.finish(session);
                      } catch (CameraException | RuntimeException e) {
                        StringWriter sw = new StringWriter();
                        PrintWriter pw = new PrintWriter(sw);
                        e.printStackTrace(pw);
                        session.close();
                        synchronizer.finish(null);
                      }
                    }
                  }));
        } catch (CameraException e) {
          StringWriter sw = new StringWriter();
          PrintWriter pw = new PrintWriter(sw);
          e.printStackTrace(pw);
          synchronizer.finish(null);
        }
      }
      try {
        synchronizer.await();
      } catch (InterruptedException e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        Thread.currentThread().interrupt();
      }
      synchronized (this) {
        session = synchronizer.getValue();
      }
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
    }
  }

  @Override
  public synchronized void deinit() {
    try {
      if (session != null) {
        session.stopCapture();
        session.close();
        session = null;
      }
      if (camera != null) {
        camera.close();
        camera = null;
      }
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      e.printStackTrace(pw);
    }
  }

  @Override
  public synchronized Mat grabFrame() {
    if (cameraName == null) return null;
    Mat mat = new Mat();
    try {
      Utils.bitmapToMat(frameQueue.take(), mat);
    } catch (InterruptedException e) {
      return null;
    }
    return mat;
  }
}
