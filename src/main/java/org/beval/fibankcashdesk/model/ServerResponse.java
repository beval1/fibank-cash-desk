package org.beval.fibankcashdesk.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerResponse<T>
{
    private String message;
    private T content;
    private LocalDateTime timestamp;
    private int statusCode;

    public ServerResponse(String message, T content, HttpStatus status)
    {
        this.message = message;
        this.content = content;
        this.timestamp = LocalDateTime.now();
        this.statusCode = status.value();
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(final String message)
    {
        this.message = message;
    }

    public Object getContent()
    {
        return content;
    }

    public void setContent(final T content)
    {
        this.content = content;
    }

    public LocalDateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(final LocalDateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public int getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(final int statusCode)
    {
        this.statusCode = statusCode;
    }
}
