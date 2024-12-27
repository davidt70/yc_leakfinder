package com.yawlconsulting.util.leakfinder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Set;
import junit.framework.*;

public class TestLeakFinder extends TestCase {

    public TestLeakFinder() {

    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        LeakFinder.enable();// default
        LeakFinder.clear();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testDetectLeak() throws MalformedURLException, IOException, URISyntaxException {

        URL url = new URI("https://www.google.com/").toURL();
        toString(DummyLib.doGet(url));

        Set<IOException> leaks = LeakFinder.checkForLeaks();
        int tryCt = 5;
        while (tryCt-- > 0 && leaks.isEmpty()) {
            leaks = LeakFinder.checkForLeaks();
            // in a production system running 
            // the background GC will eventually find the leak, 
            // here we are trying to force it for the test
            // and one would call LeakFinder.getDetectedLeaks() instead.
        }
        assertEquals("[java.io.IOException: Resouce Leak BUG found in code:InputStream obtained but not closed]", leaks.toString());
    }

    public void testDisableDetect() throws MalformedURLException, IOException, URISyntaxException {
        LeakFinder.disable();
        LeakFinder.clear();
        try {
            URL url = new URI("https://www.google.com/").toURL();
            toString(DummyLib.doGet(url));

            Set<IOException> leaks = LeakFinder.checkForLeaks();
            assertTrue("disabled no leaks expected to be detected", leaks.isEmpty());
        } finally {
            LeakFinder.enable();
        }
    }

    private String toString(InputStream in) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            in.transferTo(out);
            return out.toString();
        }
    }

    public void testDetectNoLeak() throws MalformedURLException, IOException, URISyntaxException {

        URL url = new URI("https://www.google.com/").toURL();

        try (InputStream in = DummyLib.doGet(url);) {
            toString(in);
        }
        Set<IOException> leaks = LeakFinder.checkForLeaks();
        assertTrue("expect leaks to be empty", leaks.isEmpty());
    }
}
