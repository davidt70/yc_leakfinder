package com.yawlconsulting.util.leakfinder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author david
 */
public class DummyLib {

    /**
     * example method of a library that if misused by a developer failing 
     * to close the input stream will report the line of code where 
     * the resource leak occurred.
     * @param url
     * @return
     * @throws IOException 
     */
    public static InputStream doGet(URL url)
            throws IOException {

        InputStream ans = new ByteArrayInputStream(url.toString().getBytes(StandardCharsets.UTF_8));

        if (LeakFinder.isEnabled()) {
            ans = new LeakTrackingInputStream(ans,url.toString());
        }
        return ans;
    }
}
