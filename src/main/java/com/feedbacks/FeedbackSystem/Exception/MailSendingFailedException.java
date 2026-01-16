package com.feedbacks.FeedbackSystem.Exception;

public class MailSendingFailedException extends RuntimeException{
    public MailSendingFailedException(String msg){
        super(msg);
    }
}
