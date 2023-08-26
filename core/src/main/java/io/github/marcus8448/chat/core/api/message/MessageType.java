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

package io.github.marcus8448.chat.core.api.message;

/**
 * Represents different types of messages
 */
public enum MessageType {
    /**
     * A message containing markdown-formatted text
     */
    TEXT(TextMessage.class),
    /**
     * A message containing a file
     */
    FILE(FileMessage.class);

    /**
     * The class that represents this type
     */
    private final Class<? extends Message> subClass;

    MessageType(Class<? extends Message> subClass) {
        this.subClass = subClass;
    }
}
