package com.liamfarrell.android.snapbattle.model;

/**
 * Holds either the error or the response from an AWS Function
 *
 * @param <T> The Response object from the AWS Function
 */

public class AsyncTaskResult<T> {
    private T mResult;
    private Exception mError;

    public T getResult() {
        return mResult;
    }

    public Exception getError() {
        return mError;
    }

    public AsyncTaskResult(T result) {
        super();
        this.mResult = result;
    }

    public AsyncTaskResult(Exception error) {
        super();
        this.mError = error;
    }
}