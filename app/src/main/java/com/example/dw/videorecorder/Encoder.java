package com.example.dw.videorecorder;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lhc on 2016/12/16.
 */

public class Encoder implements MainActivity.OnPreviewCallbackListener {

    private MediaCodec mEncoder;
    public static final String MIME_TYPE = "video/avc";

    public static final int mBitRate = 5 * 1024 * 1024;  //清晰度
    public static final int mFrameRate = 30;  //流畅度
    public static final int mTimeInterval = 5;

    private OnOutputListener listener;
    private Decoder decoder;


    public Encoder(Surface surface, int width, int height) {
        try {
            mEncoder = MediaCodec.createEncoderByType(MIME_TYPE);
            MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, mBitRate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, mFrameRate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);  //坑最大，参数不对的话导致crash
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, mTimeInterval);
            mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mEncoder.start();

            decoder = new Decoder(surface, width, height);
            setOnOutputListener(decoder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onFrame(byte[] data, int offset, int length, int flags) {

        ByteBuffer[] inputBuffers = mEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();

        int inputBufferIndex = mEncoder.dequeueInputBuffer(100);
        if (inputBufferIndex > -1) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data, offset, length);
            mEncoder.queueInputBuffer(inputBufferIndex, 0, length, 0, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 100);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            if (listener != null)
                listener.onFrame(outputBuffer.array(), 0, length, flags);
            mEncoder.releaseOutputBuffer(outputBufferIndex, false);
            outputBufferIndex = mEncoder.dequeueOutputBuffer(bufferInfo, 0);
        }


    }

    public void release() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (decoder != null) {
            decoder.release();
        }
    }

    public interface OnOutputListener {
        void onFrame(byte[] data, int offset, int length, int flag);
    }

    public void setOnOutputListener(OnOutputListener listener) {
        this.listener = listener;
    }

}
