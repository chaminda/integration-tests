package org.wso2.sp.tests.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import io.netty.handler.codec.http.HttpMethod;

import org.apache.commons.codec.binary.Base64;

/**
 * Created by chaminda on 8/7/17.
 */
public class TestUtil {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestUtil.class);

    public static HTTPResponse sendHRequest(String body, URI baseURI, String path, String contentType,
                                                   String methodType, Boolean auth, String userName, String password) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = TestUtil.generateRequest(baseURI, path, methodType, false);
            } catch (IOException e) {
                TestUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                //TestUtil.setHeader(urlConn, "Authorization",
                 //       "Basic " + Base64.encodeBase64((userName + ":" + password).getBytes()));
                TestUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().
                                encodeToString((userName + ":" + password).getBytes()));
            }
            if (contentType != null) {
                TestUtil.setHeader(urlConn, "Content-Type", contentType);
            }
            TestUtil.setHeader(urlConn, "HTTP_METHOD", methodType);
            if (methodType.equals(HttpMethod.POST.name()) || methodType.equals(HttpMethod.PUT.name())) {
                TestUtil.writeContent(urlConn, body);
            }
            assert urlConn != null;

            HTTPResponse httpResponseMessage = new HTTPResponse(urlConn.getResponseCode(),
                    urlConn.getContentType(), TestUtil.getResponseMessage(urlConn));
            urlConn.disconnect();
            return httpResponseMessage;
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
        return new HTTPResponse();
    }

    private static String getResponseMessage(HttpURLConnection urlConn) {
        StringBuilder sb =null;
        try {
        BufferedReader br;
            if (200 <= urlConn.getResponseCode() && urlConn.getResponseCode() <= 299) {
                br = new BufferedReader(new InputStreamReader((urlConn.getInputStream())));
            } else {
                br = new BufferedReader(new InputStreamReader((urlConn.getErrorStream())));
            }
        sb = new StringBuilder();
        String output;
        /*while ((output = br.readLine()) != null) {
            sb.append(output);
                    }*/
            while (true) {
                final String line = br.readLine();
                if (line == null) break;
                    sb.append(line);
                            }
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while getting the response message: ", e);
        }
        return sb.toString();
    }

    private static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(
                urlConn.getOutputStream());
        out.write(content);
        out.close();
    }

    private static HttpURLConnection generateRequest(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod(method);
        if (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.PUT.name())) {
            urlConn.setDoOutput(true);
        }
        if (keepAlive) {
            urlConn.setRequestProperty("Connection", "Keep-Alive");
        }
        return urlConn;
    }

    private static void setHeader(HttpURLConnection urlConnection, String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    private static void handleException(String msg, Exception ex) {
        logger.error(msg, ex);
    }

    public static void waitThread(long timemils){
        try {
            Thread.sleep(timemils);
        } catch (InterruptedException e) {
            TestUtil.handleException("IO Exception when thread sleep : ", e);
        }
    }

}
