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

import org.jetbrains.annotations.NotNull;

/**
 * A holder type
 *
 * @param <T> the type contained in this object
 */
public class Cell<T> {
    /**
     * The value contained in this object
     */
    private T value = null;

    public Cell() {
    }

    /**
     * Sets the value of this cell
     *
     * @param value the value to set
     * @return the value set
     */
    public @NotNull T setValue(@NotNull T value) {
        this.value = value;
        return this.value;
    }

    /**
     * @return the value of the cell
     */
    public T getValue() {
        return this.value;
    }
}
