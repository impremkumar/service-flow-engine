package com.ebtedge.service.flow;

import com.ebtedge.service.flow.domain.ErrorDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ResponseWrapper<T> {
    private final T data;
    private final ErrorDetails error;
    private final boolean success;

    public static <T> ResponseWrapper<T> success(T data) {
        return new ResponseWrapper<>(data, null, true);
    }

    public static <T> ResponseWrapper<T> fail(ErrorDetails error) {
        return new ResponseWrapper<>(null, error, false);
    }
}

