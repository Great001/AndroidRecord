package com.example.dw.videorecorder;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements Camera.PreviewCallback {

    private Button mBtnStart;
    private Button mBtnStop;
    private SurfaceView mSvVideo;

    private Camera camera;

    private int mWidth = 352;
    private int mHeight = 288;

    private Encoder process;
    private OnPreviewCallbackListener listener;

    private MediaCodec mDecoder;
    public static final String MIME_TYPE = "video/avc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBtnStart = (Button) findViewById(R.id.btn_start);
        mBtnStop = (Button) findViewById(R.id.btn_stop);
        mSvVideo = (SurfaceView) findViewById(R.id.sv_video_record);

        mBtnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecord();
                Toast.makeText(MainActivity.this, "开始录像...", Toast.LENGTH_SHORT).show();
            }
        });

        mBtnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopRecord();
                Toast.makeText(MainActivity.this, "停止录制中...", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void startRecord() {
        openCamera();
        startPreview();

    }


    public void stopRecord() {
        camera.stopPreview();
        process.release();
    }

    public void openCamera() {
        camera = Camera.open();  //0为后置摄像头，1为前置摄像头，无参默认为后置摄像头
        try {
            camera.setPreviewDisplay(mSvVideo.getHolder());   //掉坑2：设置预览界面
        } catch (IOException E){
            E.printStackTrace();
        }
        Camera.Parameters params = camera.getParameters();
        params.setFlashMode("off");
        params.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        params.setPreviewFormat(ImageFormat.YV12);
        params.setPictureSize(mWidth, mHeight);  //掉坑1：尺寸不支持的话，会crash掉
        params.setPreviewSize(mWidth, mHeight);
        camera.setDisplayOrientation(90);
        camera.setParameters(params);
    }

    public void startPreview() {
        byte[] buff = new byte[mWidth * mHeight * 3 / 2];
        camera.addCallbackBuffer(buff);
        camera.setPreviewCallbackWithBuffer(this);
        camera.startPreview();
        process = new Encoder(mSvVideo.getHolder().getSurface(), mWidth, mHeight);
        setPreviewListener(process);
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (listener != null) {
            listener.onFrame(data, 0, data.length, 0);
        }
        camera.addCallbackBuffer(data);
    }

    public interface OnPreviewCallbackListener {
        void onFrame(byte[] data, int offset, int length, int flags);
    }

    public void setPreviewListener(OnPreviewCallbackListener listener) {
        this.listener = listener;
    }
}
