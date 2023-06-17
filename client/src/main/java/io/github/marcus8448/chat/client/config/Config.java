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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Config {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ObservableList.class,
                    (InstanceCreator<Object>) type -> FXCollections.observableArrayList())
            .registerTypeAdapter(Account.class, new Account.Serializer())
            .registerTypeAdapter(AccountData.EncryptedAccountData.class, AccountData.EncryptedAccountData.Serializer.INSTANCE)
            .create();

    @Expose
    public int lastAccount = 0;

    @Expose
    public final ObservableList<Account> accounts = FXCollections.observableArrayList();

    private boolean isLoading = true;

    private File configFile = null;

    public Config() {
    }

    private void save() {
        if (!this.isLoading) {
            LOGGER.trace("Saving config file");
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(this, writer);
                writer.flush();
            } catch (IOException e) {
                LOGGER.error("Failed to save config file: ", e);
            }
        } else {
            LOGGER.warn("Not saving config file as it is still loading");
        }
    }

    public static Config load(File configFile) {
        LOGGER.trace("Loading configuration file at {}", configFile.getPath());
        if (configFile.exists()) {
            try (Reader reader = new FileReader(configFile)) {
                Config config = GSON.fromJson(reader, Config.class);
                config.isLoading = false;
                config.configFile = configFile;
                List<RSAPublicKey> keys = new ArrayList<>(config.accounts.size());
                for (Iterator<Account> iterator = config.accounts.iterator(); iterator.hasNext(); ) {
                    Account account = iterator.next();
                    if (keys.contains(account.publicKey())) {
                        LOGGER.warn("Removing account '{}' as a account already exists with the same key.", account.username());
                        iterator.remove();
                    } else {
                        keys.add(account.publicKey());
                    }
                }
                return config;
            } catch (IOException e) {
                LOGGER.fatal("Failed to open config file", e);
                throw new RuntimeException(e);
            } catch (JsonSyntaxException e) {
                try {
                    LOGGER.warn("Failed to load existing config file", e);
                    Path old = new File(configFile.getParentFile(), "chat.json.old").toPath();
                    try {
                        Files.delete(old);
                    } catch (Exception ignored) {}
                    Files.move(configFile.toPath(), old);
                } catch (IOException ex) {
                    try {
                        Files.delete(configFile.toPath());
                    } catch (IOException exc) {
                        RuntimeException exception = new RuntimeException(exc);
                        exception.addSuppressed(ex);
                        exception.addSuppressed(e);
                        LOGGER.fatal("Failed to refresh config file", ex);
                        LOGGER.fatal(exc);
                        throw exception;
                    }
                }
            }
        }
        LOGGER.warn("Failed to find existing config file - generating a new one");
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

    public void updateAccountData(RSAPublicKey key, AccountData.EncryptedAccountData encrypted) {
        int idx = -1;
        for (int i = 0; i < this.accounts.size(); i++) {
            Account next = this.accounts.get(i);
            if (next.publicKey().equals(key)) {
                idx = i;
                break;
            }
        }
        if (idx != -1) {
            Account remove = this.accounts.remove(idx);
            this.accounts.add(idx, new Account(remove.username(), key, encrypted));
            this.save();
        }
    }

    public int getLastAccount() {
        return lastAccount;
    }
}
