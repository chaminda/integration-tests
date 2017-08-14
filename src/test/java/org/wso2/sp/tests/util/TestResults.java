package org.wso2.sp.tests.util;

/**
 * Created by chaminda on 8/13/17.
 */
public abstract class TestResults {

    //method for msf4j service listener implementation
    public abstract void waitForResults(int retryCount, long interval);

    //This will verify the actual result with expected.
    public abstract boolean resultsFound(String eventMessage);

    //msf4j service listener - wait for and verify test results
    public synchronized void verifyResult(long interval, int maxRetry, String eventMessage){
        boolean arrived=false;
        while(!arrived){
            for(int i=0; i<=maxRetry && (!arrived);i++){
                arrived = resultsFound(eventMessage);
                try {
                    if(arrived){
                        break;
                    }
                    wait(interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
