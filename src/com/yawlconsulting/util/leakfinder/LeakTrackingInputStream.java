package com.yawlconsulting.util.leakfinder;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author david
 */
public class LeakTrackingInputStream extends java.io.BufferedInputStream {
    private final LeakFinder leakFinder;
    public LeakTrackingInputStream(InputStream in) {
        this(in, "");
    }

    public LeakTrackingInputStream(InputStream in,int size) {
        this(in,size,"");
    }
    public LeakTrackingInputStream(InputStream in,int size,String request) {
        super(in,size);
        this.leakFinder=new LeakFinder("InputStream obtained but not closed",request);
    }    
    public LeakTrackingInputStream(InputStream in,String request) {
        super(in);
        this.leakFinder=new LeakFinder("InputStream obtained but not closed",request);
    }    

    @Override
    public void close() throws IOException {
        leakFinder.close();
        super.close();
    }
}
