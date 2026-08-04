package com.securestream.auth.exception;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;

    private int status;

    private String message;

    private String path;


}
