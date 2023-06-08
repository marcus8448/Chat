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
import com.google.gson.InstanceCreator;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.Expose;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ObservableList.class,
                    (InstanceCreator<Object>) type -> FXCollections.observableArrayList())
            .registerTypeAdapter(Account.class, new Account.Serializer())
            .create();

    @Expose
    private int lastAccount = 0;

    @Expose
    private final ObservableList<Account> accounts = FXCollections.observableArrayList();

    private boolean isLoading = true;

    private File configFile = null;

    private Config() {
    }

    private void save() {
        if (!this.isLoading) {
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Config load(File configFile) {
        if (configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                Config config = GSON.fromJson(reader, Config.class);
                config.isLoading = false;
                config.configFile = configFile;
                return config;
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
        config.isLoading = false;
        config.configFile = configFile;
        config.save();
        return config;
    }

    public ObservableList<Account> getAccounts() {
        return accounts;
    }

    public void setLastAccount(int lastAccount) {
        this.lastAccount = lastAccount;
        this.save();
    }

    public void addAccount(Account account) {
        this.accounts.add(account);
        this.save();
    }

    public void removeAccount(Account account) {
        this.accounts.remove(account);
        this.save();
    }
}
