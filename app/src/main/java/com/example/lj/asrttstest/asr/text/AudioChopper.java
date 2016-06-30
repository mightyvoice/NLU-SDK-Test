package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

public interface AudioChopper
{
    public byte[] getNextFrame();
    public byte[] getXFrame(int numFrames);
    public byte[] getNextChunk(int maxSize);
    public boolean hasMoreFrame();
    public int getFrameLengthInMs();
}

