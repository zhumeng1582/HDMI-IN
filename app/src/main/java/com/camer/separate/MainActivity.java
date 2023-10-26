package com.camer.separate;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CaptureRequest;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;

import android.hardware.camera2.*;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1;
    private CameraManager cameraManager;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private TextureView textureView1;
    private TextureView textureView2;
    private TextView text;
    private TextView textTips;
    private CaptureRequest.Builder previewRequestBuilder;
    private CaptureRequest previewRequest;
    private final Timer timer = new Timer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        text = findViewById(R.id.text);
        textTips = findViewById(R.id.textTips);
        textureView1 = findViewById(R.id.textureView1);
        textureView2 = findViewById(R.id.textureView2);
        textureView1.setSurfaceTextureListener(surfaceTextureListener);
        textureView2.setSurfaceTextureListener(surfaceTextureListener);

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // 执行定时任务
                if (CameraUtils.hasBackFacingCamera() && cameraDevice == null) {
                    setupCamera();
                } else {
                    textTips.setText("无信号输入，已断开");
                }
            }
        }, 1000, 1000); // 延迟1秒后，每1秒执行一次

    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        text.setText("屏幕分辨率：" + screenWidth + "*" + screenHeight);
        text.postDelayed(new Runnable() {
            @Override
            public void run() {
                text.setVisibility(View.GONE);
            }
        }, 2000);


    }

    @Override
    protected void onStop() {
        super.onStop();
//        timer.cancel();
    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            setupCamera();

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    private void setupCamera() {

        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }
                openCamera(cameraId);
                break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            cameraError();
        }
    }

    private void openCamera(String cameraId) {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, REQUEST_CODE);
                return;
            }

            configureTransform(textureView1);
            configureTransform(textureView2);
            Log.d("openCamera", "------->cameraId = " + cameraId);
            cameraManager.openCamera(cameraId, stateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
            cameraError();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupCamera();
                } else {
                    // Permission denied
                }
                return;
            }
        }
    }

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            textTips.setText("");
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraError();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraError();
        }
    };

    private void cameraError() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        textTips.setText("无信号输入，已断开");

    }

    private void configureTransform(TextureView textureView) {

        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, textureView.getHeight(), textureView.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

    private void createCameraPreviewSession() {
        try {
            SurfaceTexture texture = textureView1.getSurfaceTexture();
//            texture.setDefaultBufferSize(textureView1.getWidth(), textureView1.getHeight());
            Surface surface = new Surface(texture);

            SurfaceTexture texture2 = textureView2.getSurfaceTexture();
//            texture2.setDefaultBufferSize(textureView1.getWidth(), textureView1.getHeight());
            Surface surface2 = new Surface(texture2);
            previewRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            previewRequestBuilder.addTarget(surface);
            previewRequestBuilder.addTarget(surface2);

            cameraDevice.createCaptureSession(Arrays.asList(surface, surface2), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {

                    try {
                        if (cameraDevice == null) return;
                        captureSession = session;

                        previewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
//                        previewRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, getDegrees());
                        previewRequest = previewRequestBuilder.build();
                        captureSession.setRepeatingRequest(previewRequest, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        cameraError();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
            cameraError();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
