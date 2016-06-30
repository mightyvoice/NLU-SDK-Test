package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

import java.util.HashMap;
import java.util.Map;

import java.util.Map;
import java.util.HashMap;

public class AudioChopperFactory
{
    private static final Map<String, Integer> codecMap;

    static {
        codecMap = new HashMap<String, Integer>();
        codecMap.put(Codec.PCM_16_8K, 320);
        codecMap.put(Codec.PCM_16_16K, 640);
        codecMap.put(Codec.PCM_16_32K, 1280);
        codecMap.put(Codec.SPEEX_8K, 320);
        codecMap.put(Codec.SPEEX_16K, 72);
        codecMap.put(Codec.AMR_07, 31);
        // Not sure what the bytes per frame value is for all available codecs. Need engineering's input here...
    }

    public static AudioChopper getAudioChopper(String codec, byte[] audio)
    {
        if( codecMap.containsKey(codec) )
            return new FixedSizeAudioChopper(codecMap.get(codec), 20, audio);
        else
            return null;
    }
}
