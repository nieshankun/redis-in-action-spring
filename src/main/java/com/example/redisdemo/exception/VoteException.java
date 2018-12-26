package com.example.redisdemo.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author nsk
 * 2018/12/26 20:09
 */
@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class VoteException extends RuntimeException {

    public VoteException(String message){
        super(message);
    }
}
