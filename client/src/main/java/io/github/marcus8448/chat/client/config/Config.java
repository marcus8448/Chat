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

package io.github.marcus8448.chat.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    @Expose
    private int lastAccount = 0;

    private final List<Account> accounts = new ArrayList<>();

    public static Config load(File configFile) {
        if (configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                return GSON.fromJson(reader, Config.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonSyntaxException e) {
                try {
                    Path old = new File(configFile.getParentFile(), "chat.json.old").toPath();
                    try {
                        Files.delete(old);
                    } catch (Exception ignored) {}
                    Files.move(configFile.toPath(), old);
                } catch (IOException ex) {
                    try {
                        Files.delete(configFile.toPath());
                    } catch (IOException exc) {
                        RuntimeException runtimeException = new RuntimeException(exc);
                        runtimeException.addSuppressed(ex);
                        runtimeException.addSuppressed(e);
                        throw runtimeException;
                    }
                }
            }
        }
        Config config = new Config();
        try (FileWriter writer = new FileWriter(configFile)) {
            GSON.toJson(config, writer);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }
}
