package com.wacai.open.sdk.response;


import java.util.List;

public class FileGatewayRes{
    public static final int SUCCESS_CODE = 0;
    public static final int ERROR_CODE = 1;
    public static final int RETRY_CODE = 8;
    private List<RemoteFile> data;

    public int code;

    public String error;

    public List<RemoteFile> getData() {
        return data;
    }

    public void setData(List<RemoteFile> data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "FileGatewayRes{" +
                "data=" + data +
                ", code=" + code +
                ", error='" + error + '\'' +
                '}';
    }
}
