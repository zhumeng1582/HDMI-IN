package com.camer.separate;


import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Bundle;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.camer.separate.base.BaseActivity;
import com.camer.separate.utils.CameraUtils;
import com.camer.separate.views.AutoFitTextureView;

import java.util.Arrays;
import java.util.List;

public class MainActivity2 extends BaseActivity {

    CameraManager cameraManager;//相机管理类
    CameraDevice cameraDevice;//相机设备类
    CameraCaptureSession cameraCaptureSession;//相机会话类
    String cameraId;//相机id
    List<Size> outputSizes;//相机输出尺寸
    int sizeIndex = 0;
    AutoFitTextureView previewView;//自适应相机预览view
    Size previewSize;//预览尺寸
    TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {

        //TextureView 可用时调用改回调方法
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //TextureView 可用，打开相机
            openCamera();
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
        initView();
    }

    private void initView() {
        previewView = findViewById(R.id.afttv_camera);
        previewView.setAspectRation(previewSize.getHeight(), previewSize.getWidth());

        //设置 TextureView 的状态监听
        previewView.setSurfaceTextureListener(surfaceTextureListener);
    }

    private void initCamera() {
        cameraManager = CameraUtils.getInstance().getCameraManager();
        cameraId = CameraUtils.getInstance().getCameraId(false);//默认使用后置相机
        //获取指定相机的输出尺寸列表，降序排序
        outputSizes = CameraUtils.getInstance().getCameraOutputSizes(cameraId, SurfaceTexture.class);
        //初始化预览尺寸
        previewSize = outputSizes.get(0);
    }

    @SuppressLint("MissingPermission")
    private void openCamera() {
        try {
            //打开相机
            cameraManager.openCamera(cameraId,
                    new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice camera) {
                            if (camera == null) {
                                return;
                            }
                            cameraDevice = camera;
                            //创建相机预览 session
                            createPreviewSession();
                        }

                        @Override
                        public void onDisconnected(CameraDevice camera) {
                            //释放相机资源
                            releaseCamera();
                        }

                        @Override
                        public void onError(CameraDevice camera, int error) {
                            //释放相机资源
                            releaseCamera();
                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void createPreviewSession() {
        //关闭之前的会话
        CameraUtils.getInstance().releaseCameraSession(cameraCaptureSession);
        //根据TextureView 和 选定的 previewSize 创建用于显示预览数据的Surface
        SurfaceTexture surfaceTexture = previewView.getSurfaceTexture();
        surfaceTexture.setDefaultBufferSize(previewSize.getWidth(), previewSize.getHeight());//设置SurfaceTexture缓冲区大小
        final Surface previewSurface = new Surface(surfaceTexture);

        try {
            //创建预览session
            cameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {

                            cameraCaptureSession = session;

                            try {
                                //构建预览捕获请求
                                CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                builder.addTarget(previewSurface);//设置 previewSurface 作为预览数据的显示界面
                                CaptureRequest captureRequest = builder.build();
                                //设置重复请求，以获取连续预览数据
                                session.setRepeatingRequest(captureRequest, new CameraCaptureSession.CaptureCallback() {
                                            @Override
                                            public void onCaptureProgressed(CameraCaptureSession session, CaptureRequest request, CaptureResult partialResult) {
                                                super.onCaptureProgressed(session, request, partialResult);
                                            }

                                            @Override
                                            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                                                super.onCaptureCompleted(session, request, result);
                                            }
                                        },
                                        null);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    },
                    null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void releaseCamera() {
        CameraUtils.getInstance().releaseCameraSession(cameraCaptureSession);
        CameraUtils.getInstance().releaseCameraDevice(cameraDevice);
    }
}