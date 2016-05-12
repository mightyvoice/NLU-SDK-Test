package com.example.lj.asrttstest;

/**
 * Created by lj on 16/5/9.
 */
public class AppInfo {
    ////////// Settings for ASR
//    public static final String Host = "nmsp.labs.nuance.com";
//    public static final int Port = 443;
//    public static final String AppId = "NMDPTRIAL_yujacky_tcl_com20150630145537";
//    private static final String AppKeyStr = "b24c2e5ade2e23c321b6f5c84cec8b3d80150e402b496577785c526c80031a324f498d2d77b0f19866e660b2d2ecf05201fe932c48f8a3c054f52dd4f9470748";
//    public static final byte[] AppKey = toByteArray(AppKeyStr);

    //////////Settings for TTS
    //// it seems that it also works for ASR
    public static final String Host = "mtldev02.nuance.com";
    public static final int Port = 443;
    public static final String AppId = "NMT_EVAL_TCL_20150814";
    public static final byte[] AppKey = { (byte) 0x89, (byte) 0xe9,
            (byte) 0xb1, (byte) 0xb6, (byte) 0x19, (byte) 0xdf, (byte) 0xc7,
            (byte) 0xd6, (byte) 0x82, (byte) 0x23, (byte) 0x7e, (byte) 0x70,
            (byte) 0x1d, (byte) 0xa7, (byte) 0xad, (byte) 0xa4, (byte) 0x83,
            (byte) 0x16, (byte) 0xf6, (byte) 0x75, (byte) 0xf7, (byte) 0x3c,
            (byte) 0x5e, (byte) 0xcd, (byte) 0x23, (byte) 0xa4, (byte) 0x1f,
            (byte) 0xc4, (byte) 0x07, (byte) 0x82, (byte) 0xbc, (byte) 0x21,
            (byte) 0x2e, (byte) 0xd3, (byte) 0x56, (byte) 0x20, (byte) 0x22,
            (byte) 0xc2, (byte) 0x3e, (byte) 0x75, (byte) 0x21, (byte) 0x4d,
            (byte) 0xcb, (byte) 0x90, (byte) 0x10, (byte) 0x28, (byte) 0x6c,
            (byte) 0x23, (byte) 0xaf, (byte) 0xe1, (byte) 0x00, (byte) 0xe0,
            (byte) 0x0d, (byte) 0x44, (byte) 0x64, (byte) 0x87, (byte) 0x3e,
            (byte) 0x00, (byte) 0x4d, (byte) 0x1f, (byte) 0x4c, (byte) 0x8a,
            (byte) 0x58, (byte) 0x83 };

    private static byte[] toByteArray(String appKey)
    {
        String trimmedAppKey = appKey.trim();
        byte[] keyInBytes = new byte[trimmedAppKey.length()/2];

        for (int i = 0; i < trimmedAppKey.length()/2; i++)
        {
            String key = trimmedAppKey.substring(i*2, i*2 + 2);
            keyInBytes[i] = (byte)Integer.parseInt(key, 16);
        }

        return keyInBytes;
    }
}
