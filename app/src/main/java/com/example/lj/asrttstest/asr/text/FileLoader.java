package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class FileLoader
{
    public static byte[] load(File file) throws Exception
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));

        int byteRead = 0;
        byte[] buffer = new byte[1024];
        while( (byteRead = bis.read(buffer)) != -1 )
        {
            baos.write(buffer, 0, byteRead);
        }
        bis.close();
        baos.close();

        return baos.toByteArray();
    }
}

