/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.yawlconsulting.util.leakfinder;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author david
 */
public class LeakTrackingOutputStream extends FilterOutputStream {
    private final LeakFinder leakFinder;
    public LeakTrackingOutputStream(OutputStream out) {
        this(out,"");
    }    
    public LeakTrackingOutputStream(OutputStream out,String request) {
        super(out);
        this.leakFinder=new LeakFinder("OutputStream obtained but not closed",request);
    } 
    
    @Override
    public void close() throws IOException {
        leakFinder.close();
        super.close();
    }
}
