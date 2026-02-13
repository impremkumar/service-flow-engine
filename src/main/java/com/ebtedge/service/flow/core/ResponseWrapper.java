package com.ebtedge.service.flow.core;

import com.ebtedge.service.flow.domain.ErrorDetails;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.function.Function;

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

    /**
     * Map the successful value to a new type. If the wrapper represents a failure,
     * the error is propagated without applying the mapper.
     *
     * @param mapper Function to transform the success value
     * @param <R> The new type
     * @return A new ResponseWrapper with the transformed value or the same error
     */
    public <R> ResponseWrapper<R> map(Function<T, R> mapper) {
        return success ? ResponseWrapper.success(mapper.apply(data))
                       : ResponseWrapper.fail(error);
    }

    /**
     * Chain operations that return ResponseWrapper (monadic bind/flatMap).
     * Useful for chaining multiple operations that may fail.
     *
     * @param mapper Function that returns a ResponseWrapper
     * @param <R> The new type
     * @return The result of the mapper or the propagated error
     */
    public <R> ResponseWrapper<R> flatMap(Function<T, ResponseWrapper<R>> mapper) {
        return success ? mapper.apply(data)
                       : ResponseWrapper.fail(error);
    }

    /**
     * Get the data if successful, otherwise return the default value.
     *
     * @param defaultValue The value to return on failure
     * @return The data or default value
     */
    public T orElse(T defaultValue) {
        return success ? data : defaultValue;
    }

    /**
     * Recover from a failure by providing a recovery function.
     * If the wrapper is successful, returns the same wrapper.
     * If the wrapper is a failure, applies the recovery function to the error.
     *
     * @param recovery Function to recover from the error
     * @return A new ResponseWrapper with recovered value or the original success
     */
    public ResponseWrapper<T> recover(Function<ErrorDetails, T> recovery) {
        return success ? this : ResponseWrapper.success(recovery.apply(error));
    }

    /**
     * Execute a side effect if the wrapper is successful.
     *
     * @param consumer Consumer to execute on success
     * @return The same ResponseWrapper for chaining
     */
    public ResponseWrapper<T> onSuccess(java.util.function.Consumer<T> consumer) {
        if (success && data != null) {
            consumer.accept(data);
        }
        return this;
    }

    /**
     * Execute a side effect if the wrapper is a failure.
     *
     * @param consumer Consumer to execute on failure
     * @return The same ResponseWrapper for chaining
     */
    public ResponseWrapper<T> onFailure(java.util.function.Consumer<ErrorDetails> consumer) {
        if (!success && error != null) {
            consumer.accept(error);
        }
        return this;
    }
}

