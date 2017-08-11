package org.wso2.sp.tests.util;

/**
 * Created by chaminda on 8/7/17.
 */
public class HTTPResponse {

    private int responseCode;
    private String contentType;
    private String message;

    public HTTPResponse(int responseCode, String contentType, String message) {
        this.responseCode = responseCode;
        this.contentType = contentType;
        this.message = message;
    }
    public HTTPResponse(){}


    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
