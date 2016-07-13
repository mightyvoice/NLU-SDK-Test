package com.example.lj.asrttstest.text.http;

/**
 * Created by lj on 16/6/30.
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class Chunk {

    private ByteArrayOutputStream _byteStream;

    public Chunk() {
        _byteStream = new ByteArrayOutputStream();
    }

    public void append(String str) {
        if (str != null) {
            append(str.getBytes());
        }
    }

    public void append(byte[] buffer) {
        try {
            if (buffer != null) {
                _byteStream.write(buffer);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeTo(OutputStream outstream) throws IOException {
        int size = _byteStream.size();
        outstream.write(String.format("%x\r\n", size).getBytes());
        if (size > 0) {
            outstream.write(_byteStream.toByteArray());
        }
        outstream.write("\r\n".getBytes());
        outstream.flush();
    }
}

