package org.wso2.sp.tests;

import org.testng.Assert;
import org.testng.ITest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;
import org.wso2.sp.tests.util.HTTPResponse;
import org.wso2.sp.tests.util.ResultsCallBack;
import org.wso2.sp.tests.util.TestUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.TimeoutException;

import static org.wso2.sp.tests.util.Constants.*;
import static org.wso2.sp.tests.util.TestUtil.waitThread;

/**
 * Created by chaminda on 8/7/17.
 */
public class TestXMLInput {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TestXMLInput.class);

    @Test
    public void testXmlInputMapping(){
    //Deploy siddhi app having input receiver with xml event mapper and include http sink to send event data to msf4j service
    String app_body = "@App:name('TestSiddhiAppxml')\n" +
            "@source(type='inMemory', topic='symbol', @map(type='xml'))\n" +
            "define stream FooStream (symbol string, price float, class string);\n" +
            "@Sink(type='http', publisher.url='http://localhost:8080/testresults', method='{{method}}',headers='{{headers}}',\n" +
            "@map(type='json'))\n" +
            "define stream BarStream (message string,method String,headers String);\n" +
            "from FooStream\n" +
            "select symbol as message, 'POST' as method, class as headers\n" +
            "insert into BarStream;";
        //Deploy siddhi app
        URI appApi_baseURI = URI.create(SIDDHI_APP_API);
        String appApi_path = "/siddhi-apps";

        log.info("Deploying Siddhi App...");
        HTTPResponse httpResponse = TestUtil.sendHRequest(app_body, appApi_baseURI, appApi_path, HEADER_CONTTP_TEXT, HTTP_POST,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        //Assert whether siddhi app deployed - RestAPI call
        Assert.assertEquals(httpResponse.getResponseCode(), 201);
        waitThread(5000);

        //Send single event

        String query_body = "{\n" +
                "  \"streamName\": \"FooStream\",\n" +
                "  \"siddhiAppName\": \"TestSiddhiAppxml\",\n" +
                "  \"timestamp\": null,\n" +
                "  \"data\": [\n" +
                "   \"TestData\",\n" +
                "   5.0,\n" +
                "   \"cclassName:com.wso2.sp.test.VerifyXML\"\n" +
                "  ]\n" +
                "}";
        String simulator_path="/simulation/single";

        log.info("Publishing a single event");
        HTTPResponse query_response = TestUtil.sendHRequest(query_body, appApi_baseURI, simulator_path, HEADER_CONTTP_TEXT, HTTP_POST,
                false, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        waitThread(3000);
        //Event should published to msf4j service using above sink
        Assert.assertEquals(query_response.getResponseCode(), 200);
        Assert.assertEquals(query_response.getContentType(),HEADER_CONTTP_JSON);
        Assert.assertEquals(query_response.getMessage(),"{\"status\":\"OK\",\"message\":\"Single Event simulation started successfully\"}");

        //polling the get method of msf4j service
        URI msf4j_baseURI = URI.create(MSF4J_TEST_API);
        HTTPResponse msf4j_get_respns = TestUtil.sendHRequest("", msf4j_baseURI, "/testresults"+"/com.wso2.sp.test.VerifyXM", HEADER_CONTTP_TEXT, HTTP_GET,
                false, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        //verify the event
        String msgPattern = "{\"message\":\"TestData\",\"method\":\"POST\",\"headers\":\"cclassName:com.wso2.sp.test.VerifyXML\"}";

        ResultsCallBack resultsCallBack = new ResultsCallBack(msgPattern) ;
        resultsCallBack.waitForResult(6000,1000,msf4j_get_respns.getMessage());
        Assert.assertEquals(msf4j_get_respns.getMessage(), msgPattern);

      /* HTTPResponse apiDelete_respns = TestUtil.sendHRequest("", appApi_baseURI, "/siddhi-apps/TestSiddhiAppxml", HEADER_CONTTP_TEXT, "DELETE",
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);*/
        //while condition true and test case pass then
    }

    @AfterTest
    public void tearDown(){
        //TODO: close all the connections created...
    }
}
