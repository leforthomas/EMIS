package com.geocento.webapps.earthimages.emis.common.server.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class StreamGobbler extends Thread {

    InputStream is;
    StringBuffer stringBuffer;

    public StreamGobbler(InputStream is, StringBuffer stringBuffer)
    {
        this.is = is;
        this.stringBuffer = stringBuffer;
    }
    
    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ( (line = br.readLine()) != null)
                if(stringBuffer != null && !line.startsWith("Warning")) {
                    stringBuffer.append(line + "\n");
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
    }
}