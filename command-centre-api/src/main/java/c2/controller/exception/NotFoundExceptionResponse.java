package c2.controller.exception;

import com.sun.jersey.api.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundExceptionResponse extends NotFoundException {
}
