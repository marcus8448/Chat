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

package io.github.marcus8448.chat.server;

import io.github.marcus8448.chat.core.room.ChatRoom;
import io.github.marcus8448.chat.core.user.User;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class Server implements Closeable {
    private final List<ChatRoom> rooms = new ArrayList<>();
    private final List<User> users = new ArrayList<>();
    private final List<User> online = new ArrayList<>();
    private final Connection database;

    public Server(File database) throws SQLException {
        this.database = DriverManager.getConnection("jdbc:sqlite:" + database.getAbsolutePath());
        try (Statement stmt = this.database.createStatement()) {
            stmt.executeUpdate("""
create table IF NOT EXISTS Users (
    UUID TEXT PRIMARY KEY NOT NULL,
    USERNAME TEXT NOT NULL UNIQUE,
    PUBLICKEY BLOB NOT NULL UNIQUE
);
""");
            stmt.executeUpdate("""
create table IF NOT EXISTS ChatRooms (
    UUID TEXT PRIMARY KEY NOT NULL,
    NAME TEXT NOT NULL,
    PARTICIPANTS TEXT
);
""");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void createAccount() {

    }

    @Override
    public void close() throws IOException {
        try {
            this.database.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
