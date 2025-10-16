package com.backend.crmInmobiliario.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ContractLimitExceededException extends RuntimeException {
    public ContractLimitExceededException(String msg) { super(msg); }
}
