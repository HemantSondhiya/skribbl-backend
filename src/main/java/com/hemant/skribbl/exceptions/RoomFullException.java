package com.hemant.skribbl.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RoomFullException extends RuntimeException {
    public RoomFullException(String msg) { super(msg); }
}
