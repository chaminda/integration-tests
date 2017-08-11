package org.wso2.sp.tests.util;

import java.util.concurrent.TimeoutException;

/**
 * Created by chaminda on 8/10/17.
 */
public class ResultsCallBack {
    private String patternToMatch;

    public ResultsCallBack(String patternToMatch){
        this.patternToMatch = patternToMatch;
    }

    private boolean receiveResult(String eventMessage) {
        //return Pattern.compile(patternToMatch).matcher(eventMessage).find();
        return patternToMatch.equalsIgnoreCase(eventMessage);
    }

    public synchronized void waitForResult(long timeout, long interval, String eventMessage){
        boolean arrived=false;
        long tm = System.currentTimeMillis() + timeout;
        while (!arrived) {
            long delay = tm - System.currentTimeMillis();
            try {
                if (delay < 0) {
                    throw new TimeoutException();
                }
                wait(interval);
                arrived = this.receiveResult(eventMessage);
            } catch (TimeoutException e) {
                e.printStackTrace(); break;
            } catch (InterruptedException e) {
                e.printStackTrace(); break;
            }
        }
    }

}
