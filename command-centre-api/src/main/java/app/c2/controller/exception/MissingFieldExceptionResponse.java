package app.c2.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(reason = "Some parameters are invalid", code = HttpStatus.BAD_REQUEST)
public class MissingFieldExceptionResponse extends Exception {
    public MissingFieldExceptionResponse(String var, String desc){
        super("Missing "+var + ", "+desc);
    }
}
