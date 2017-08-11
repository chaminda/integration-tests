package org.wso2.sp.tests.util;

import java.util.concurrent.TimeoutException;

/**
 * Created by chaminda on 8/10/17.
 */
public class ResultsReceiver {
    private static ResultsCallBack resultsCallBack;



    public synchronized void responseArrived(boolean arrived) {
        arrived = true;
        notify();
    }
}
