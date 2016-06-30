package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class LogData {
    public String sessionId = "";
    public float audioDuration = 0F;
    public float timeToConnect = 0F;
    public float timeToFirstResponse = 0F;
    public float timeToFinalResponse = 0F;
    public float totalTrxnDuration = 0F;

    public void writeToFile() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String now = sdf.format(new Date());

            String lineSeparator = System.getProperty("line.separator");
            String header = "timestamp, sessionid, audio duration, time to connect, time to first response, time to final response, total trxn duration" + lineSeparator;
            String s = String.format("%s,%s,%.3f,%.3f,%.3f,%.3f,%.3f%s", now, sessionId, audioDuration, timeToConnect, timeToFirstResponse, timeToFinalResponse, totalTrxnDuration, lineSeparator);

            File dir = new File(".");
            File fout = new File(dir.getCanonicalPath() + File.separator + "log.txt");

            FileOutputStream fos = new FileOutputStream(fout, true);
            Writer writer = new BufferedWriter(new OutputStreamWriter(fos, "utf-8"));
            if( fout.length() == 0 )
                writer.append(header);
            writer.append(s);
            writer.close();

        } catch (Exception e) {
            e.printStackTrace(System.out);
        }

    }
}

