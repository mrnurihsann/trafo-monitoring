package com.pln.trafo.exception;

import com.pln.trafo.dto.ApiResponse;
import org.jboss.logging.Logger;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.stream.Collectors;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionHandler.class);

    @Override
    public Response toResponse(Exception exception) {
        LOG.error("Exception occurred", exception);

        if (exception instanceof WebApplicationException) {
            WebApplicationException webEx = (WebApplicationException) exception;
            return Response.status(webEx.getResponse().getStatus())
                .entity(ApiResponse.error(webEx.getMessage()))
                .build();
        }

        if (exception instanceof ConstraintViolationException) {
            ConstraintViolationException cvEx = (ConstraintViolationException) exception;
            String violations = cvEx.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Validation error: " + violations))
                .build();
        }

        if (exception instanceof IllegalArgumentException) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(ApiResponse.error("Invalid request: " + exception.getMessage()))
                .build();
        }

        // Generic server error
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(ApiResponse.error("Terjadi kesalahan internal server"))
            .build();
    }
}
