package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

public class FixedSizeAudioChopper implements AudioChopper
{
    private int frameLengthInBytes;
    private int frameLengthInMs;

    private byte[] audio;
    private int curPos = 0;

    public FixedSizeAudioChopper(int frameLengthInBytes, int frameLengthInMs, byte[] audio)
    {
        this.frameLengthInBytes = frameLengthInBytes;
        this.frameLengthInMs = frameLengthInMs;
        this.audio = audio;
    }

    public int getFrameLengthInMs()
    {
        return this.frameLengthInMs;
    }

    public synchronized byte[] getNextChunk(int maxSize)
    {
        int len = audio.length-curPos <= maxSize ? audio.length-curPos : (maxSize / this.frameLengthInBytes) * this.frameLengthInBytes;
        byte[] chunk = new byte[len];
        System.arraycopy(audio, curPos, chunk, 0, len);
        curPos += len;
        return chunk;
    }

    public synchronized byte[] getNextFrame()
    {
        int len = audio.length-curPos <= this.frameLengthInBytes ? audio.length-curPos : frameLengthInBytes;
        byte[] frame = new byte[len];
        System.arraycopy(audio, curPos, frame, 0, len);
        curPos += len;
        return frame;
    }

    public byte[] getXFrame(int numFrames)
    {
        int len = audio.length-curPos <= this.frameLengthInBytes*numFrames ? audio.length-curPos : this.frameLengthInBytes*numFrames;
        byte[] chunk = new byte[len];
        System.arraycopy(audio, curPos, chunk, 0, len);
        curPos += len;
        return chunk;
    }

    public synchronized boolean hasMoreFrame()
    {
        return curPos != audio.length;
    }
}

