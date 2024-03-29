/*
 * Copyright 2023 marcus8448
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.marcus8448.chat.core.api.misc;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

// i wish i was using rust.

/**
 * Similar to an {@link java.util.Optional} but has a value for failures
 *
 * @param <Ok>    the successful result type
 * @param <Error> the failed result type
 */
public sealed interface Result<Ok, Error> {
    @Contract(value = "_ -> new", pure = true)
    static <Ok, Error> @NotNull Result<Ok, Error> ok(Ok ok) {
        return (Result<Ok, Error>) new Success<>(ok);
    }

    @Contract(value = "_ -> new", pure = true)
    static <Ok, Error> @NotNull Result<Ok, Error> error(Error error) {
        return (Result<Ok, Error>) new Failure<>(error);
    }

    /**
     * @return the value of a successful result
     * @throws UnsupportedOperationException if the result is a failure
     */
    Ok unwrap();

    /**
     * @return whether the result is successful
     */
    boolean isOk();

    /**
     * @return the value of an unsuccessful result
     * @throws UnsupportedOperationException if the result is a success
     */
    Error unwrapError();


    /**
     * @return whether the result is a failure
     */
    boolean isError();

    <NewError> Result<Ok, NewError> coerce();

    <NewOk> Result<NewOk, Error> coerceError();

    /**
     * A successful result
     *
     * @param value the value of the result
     * @param <Ok>  the type held
     */
    record Success<Ok>(Ok value) implements Result<Ok, Void> {
        @Override
        public Ok unwrap() {
            return this.value;
        }

        @Override
        public boolean isOk() {
            return true;
        }

        @Override
        public Void unwrapError() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isError() {
            return false;
        }

        @Override
        public <NewError> Result<Ok, NewError> coerce() {
            return (Result<Ok, NewError>) this;
        }

        @Override
        public <NewOk> Result<NewOk, Void> coerceError() {
            throw new UnsupportedOperationException();
        }

    }

    /**
     * A failed result
     *
     * @param error   the value held
     * @param <Error> the type held
     */
    record Failure<Error>(Error error) implements Result<Void, Error> {
        @Override
        public Void unwrap() {
            throw new UnsupportedOperationException(this.error.toString());
        }

        @Override
        public boolean isOk() {
            return false;
        }

        @Override
        public Error unwrapError() {
            return this.error;
        }

        @Override
        public boolean isError() {
            return true;
        }

        @Override
        public <NewError> Result<Void, NewError> coerce() {
            throw new UnsupportedOperationException();
        }

        @Override
        public <NewOk> Result<NewOk, Error> coerceError() {
            return (Result<NewOk, Error>) this;
        }
    }
}
