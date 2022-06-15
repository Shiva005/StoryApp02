package com.dreamlibrary.storyapp.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RemoveContinueBook {

    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("message")
    @Expose
    private String message;
    @SerializedName("msg")
    @Expose
    private String msg;
    @SerializedName("success")
    @Expose
    private Integer success;
    @SerializedName("is_continue_book")
    @Expose
    private Boolean isContinueBook;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Integer getSuccess() {
        return success;
    }

    public void setSuccess(Integer success) {
        this.success = success;
    }

    public Boolean getIsContinueBook() {
        return isContinueBook;
    }

    public void setIsContinueBook(Boolean isContinueBook) {
        this.isContinueBook = isContinueBook;
    }

}
