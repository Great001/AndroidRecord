package com.example.dw.videorecorder;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.view.Surface;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by lhc on 2016/12/16.
 */

public class Decoder implements Encoder.OnOutputListener {

    public static final String MIME_TYPE = "video/avc";

    private MediaCodec mDecoder;
    private int mCount = 1;

    public Decoder(Surface surface, int width, int height) {
        try {
            mDecoder = MediaCodec.createDecoderByType(MIME_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MediaFormat format = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mDecoder.configure(format, surface, null, 0);
        mDecoder.start();
    }

    @Override
    public void onFrame(byte[] data, int offset, int length, int flag) {
        ByteBuffer[] inputBuffers = mDecoder.getInputBuffers();
        int inputBufferIndex = mDecoder.dequeueInputBuffer(0);
        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(data, offset, length);
            mDecoder.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / 30, 0);
            mCount++;
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
        while (outputBufferIndex >= 0) {
            mDecoder.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, 0);
        }
    }

    public void release() {
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }
    }

}
