package org.wso2.sp.tests;

import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.wso2.sp.tests.util.HTTPResponse;
import org.wso2.sp.tests.util.TestResults;
import org.wso2.sp.tests.util.TestUtil;

import java.net.URI;

import static org.wso2.sp.tests.util.Constants.*;
import static org.wso2.sp.tests.util.TestUtil.waitThread;

/**
 * Created by chaminda on 8/7/17.
 */
public class TestXMLInput{
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestXMLInput.class);
    private URI msf4j_baseURI = URI.create(MSF4J_TEST_API);
    private URI appApi_baseURI = URI.create(SIDDHI_APP_API);

    @Test
    public void testXmlInputMapping(){
    //Deploy siddhi app having input receiver with xml event mapper and include http sink to send event data to msf4j service
        String pub_Url = "http://localhost:8080/testresults";
    String app_body = "@App:name('TestSiddhiAppxml')\n" +
            "@source(type='inMemory', topic='symbol', @map(type='xml'))\n" +
            "define stream FooStream (symbol string, price float, class string);\n" +
            "@Sink(type='http', publisher.url='"+pub_Url+"', method='{{method}}',headers='{{headers}}',\n" +
            "@map(type='json'))\n" +
            "define stream BarStream (message string,method String,headers String);\n" +
            "from FooStream#log()\n" +
            "select symbol as message, 'POST' as method, class as headers\n" +
            "insert into BarStream;";
        //Deploy siddhi app

        String appApi_path = "/siddhi-apps";

        log.info("Deploying Siddhi App...");
        HTTPResponse httpResponse = TestUtil.sendHRequest(app_body, appApi_baseURI, appApi_path, HEADER_CONTTP_TEXT, HTTP_POST,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        //Assert whether siddhi app deployed - RestAPI call
        Assert.assertEquals(httpResponse.getResponseCode(), 201);
        //wait untill siddhi app get deployed
        waitThread(5000);

        //Test case name(fully qualified method name)
        String testName= "com.wso2.sp.test.VerifyXML";
        String query_body = "{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"siddhiAppName\": \"TestSiddhiAppxml\",\n" +
                "  \"timestamp\": null,\n" +
                "  \"data\": [\n" +
                "   \"TestData\",\n" +
                "   5.0,\n" +
                "   \"cclassName:"+testName+"\"\n" +
                "  ]\n" +
                "}";
        String simulator_path="/simulation/single";

        //Send single event
        log.info("Publishing a single event");
        HTTPResponse query_response = TestUtil.sendHRequest(query_body, appApi_baseURI, simulator_path, HEADER_CONTTP_TEXT, HTTP_POST,
                false, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        //wait till events published
        waitThread(3000);
        //Event should published to msf4j service using above sink
        Assert.assertEquals(query_response.getResponseCode(), 200);
        Assert.assertEquals(query_response.getContentType(),HEADER_CONTTP_JSON);
        Assert.assertEquals(query_response.getMessage(),"{\"status\":\"OK\",\"message\":\"Single Event simulation started successfully\"}");

        //polling the get method of msf4j service

        final HTTPResponse msf4j_get_respns = TestUtil.sendHRequest("", msf4j_baseURI, "/testresults"+"/com.wso2.sp.test.VerifyXM", HEADER_CONTTP_TEXT, HTTP_GET,
                false, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        //expected result
        final String msgPattern = "{\"message\":\"TestData\",\"method\":\"POST\",\"headers\":\"cclassName:com.wso2.sp.test.VerifyXML\"}";

         TestResults testResults = new TestResults() {
            @Override
            public void waitForResults(int retryCount, long interval) {
                super.verifyResult(interval,retryCount, msf4j_get_respns.getMessage());
            }

            @Override
            public boolean resultsFound(String eventMessage) {
                return msgPattern.equalsIgnoreCase(eventMessage);
            }
        };
        //verify event results
        testResults.waitForResults(10,1000);
        Assert.assertEquals(msf4j_get_respns.getMessage(), msgPattern);
        //Delete siddhi app - optional
      /*HTTPResponse apiDelete_respns = TestUtil.sendHRequest("", appApi_baseURI, "/siddhi-apps/TestSiddhiAppxml", HEADER_CONTTP_TEXT, "DELETE",
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);*/
    }

    @AfterTest
    public void tearDown(){
        //clearing msf4j service
        HTTPResponse msf4j_clear_respns = TestUtil.sendHRequest("", msf4j_baseURI, "/testresults/clear", HEADER_CONTTP_TEXT, HTTP_POST,
                false, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        Assert.assertEquals(msf4j_clear_respns.getResponseCode(), 204);

    }


}
