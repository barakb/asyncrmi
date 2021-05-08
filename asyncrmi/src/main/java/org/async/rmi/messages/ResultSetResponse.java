package org.async.rmi.messages;

/**
 * Created by Barak Bar Orion
 * 7/20/15.
 */
public class ResultSetResponse extends Response {
    @SuppressWarnings("unused")
    public ResultSetResponse() {
    }
    public ResultSetResponse(Object value){
        this.result = value;
    }
    public ResultSetResponse(Object value, Throwable error){
        this.result = value;
        this.error = error;
    }
}
