package com.example.lj.asrttstest.asr.text;

/**
 * Created by lj on 16/6/30.
 */

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LatencyMonitor {

	/*
	 * transaction start timestamp
	 * transaction running latency timestamp
	 * audio streaming begin timestamp
	 * audio streaming end timestamp
	 * audio streaming calculated time to complete
	 * transaction initial response timestamp
	 * transaction initial calculated user perceived latency
	 * transaction final response timestamp
	 * transaction response calculated time to complete
	 * transaction final calculated user perceived latency
	 * transaction total duration
	 *
	 */

    public class Marker {
        public static final String start = "start";
        public static final String connected = "connected";
        public static final String query_begin = "query_begin";
        public static final String audio_streaming_begin = "audio_streaming_begin";
        public static final String audio_streaming_end = "audio_streaming_end";
        public static final String query_complete = "query_complete";
        public static final String initial_response = "initial_response";
        public static final String final_response = "final_response";
        public static final String stop = "stop";
    }

    protected Map<String, Long> mMarkers = new HashMap<String, Long>();

    public void setMarker( String marker ) {
        mMarkers.put( marker, new Date().getTime() );
    }

    public void removeMarker( String marker ) {
        if( mMarkers.containsKey(marker) )
            mMarkers.remove(marker);
    }

    public long getMarker( String marker ) {
        if( !mMarkers.containsKey(marker) )
            return -1;

        return (long)mMarkers.get( marker );
    }

    public float calculateDistanceBetweenMarkers( String m1, String m2 ) {
        long start = getMarker(m1);
        long end = getMarker(m2);

        return (float)( (end - start) / 1000F );
    }

    public float calculateDistanceSinceStart( ) {
        long start = getMarker(Marker.start);
        long end = new Date().getTime();

        return (float)( (end - start) / 1000F );
    }

    public boolean start() {

        if( mMarkers.containsKey(Marker.start)) {
            System.out.println("LatencyMonitor.start() Error: monitor already running. Stop() and/or reset() monitor before calling start().");
            return false;
        }

        mMarkers.put( Marker.start, new Date().getTime() );
        return true;
    }

    public boolean stop() {

        if( !mMarkers.containsKey(Marker.start)) {
            System.out.println("LatencyMonitor.stop() Error: monitor not running. Reset() and/or start() monitoring before calling stop().");
            return false;
        }

        mMarkers.put( Marker.stop, new Date().getTime() );
        return true;
    }

    public void reset() {
        mMarkers.clear();
    }
}

