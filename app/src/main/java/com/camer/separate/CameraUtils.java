package com.camer.separate;

import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;

public class CameraUtils {

    private static boolean checkCameraFacing(final int facing) {
        if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
            return false;
        }
        final int cameraCount = Camera.getNumberOfCameras();
        CameraInfo info = new CameraInfo();
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, info);
            if (facing == info.facing) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasBackFacingCamera() {
        final int CAMERA_FACING_BACK = 0;
        return checkCameraFacing(CAMERA_FACING_BACK);
    }

    public static boolean hasFrontFacingCamera() {
        final int CAMERA_FACING_FRONT = 1;
        return checkCameraFacing(CAMERA_FACING_FRONT);
    }

    public static int getSdkVersion() {
        return android.os.Build.VERSION.SDK_INT;
    }
}
