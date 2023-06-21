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

/**
 * Client configuration file
 */
public class Config {
    /**
     * Google JSON
     */
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(ObservableList.class,
                    (InstanceCreator<Object>) type -> FXCollections.observableArrayList())
            .registerTypeAdapter(Account.class, new Account.Serializer())
            .registerTypeAdapter(AccountData.EncryptedAccountData.class, AccountData.EncryptedAccountData.Serializer.INSTANCE)
            .create();
    private static final Logger LOGGER = LogManager.getLogger();
    /**
     * List of available accounts
     */
    @Expose
    public final ObservableList<Account> accounts = FXCollections.observableArrayList();
    /**
     * The last account selected on the login screen
     */
    @Expose
    public int lastAccount = 0;
    /**
     * Whether the config file is in the process of loading (disables saving on changes)
     */
    private boolean isLoading = true;

    /**
     * The associated file to write to
     */
    private File configFile = null;

    /**
     * Creates a new config instance
     */
    public Config() {
    }

    /**
     * Loads a configuration from a file, or creates a new one if it does not exist
     *
     * @param configFile the file to load from
     * @return the configuration represented by the file, or a new configuration if it does not exist
     */
    public static Config load(File configFile) {
        LOGGER.trace("Loading configuration file at {}", configFile.getPath());
        if (configFile.exists()) { // check if the file exists
            try (Reader reader = new FileReader(configFile)) { // read the file
                Config config = GSON.fromJson(reader, Config.class); // parse the file
                config.isLoading = false; // mark config as loaded
                config.configFile = configFile; // set the associated config file
                List<RSAPublicKey> visited = new ArrayList<>(config.accounts.size()); // list of keys already seen
                for (Iterator<Account> iterator = config.accounts.iterator(); iterator.hasNext(); ) {//iterate over keys
                    Account account = iterator.next(); // get the next account
                    if (visited.contains(account.publicKey())) { // check if we have already seen this key
                        LOGGER.warn("Removing account '{}' as a account already exists with the same key.", account.username());
                        iterator.remove(); // remove the key
                    } else {
                        visited.add(account.publicKey()); // add the key to the list of visited ones
                    }
                }
                return config; // return the config
            } catch (IOException e) { // I/O error, so just crash.
                LOGGER.fatal("Failed to open config file", e);
                throw new IllegalStateException(e);
            } catch (JsonSyntaxException e) { // parse error - invalid config format
                try {
                    LOGGER.warn("Failed to load existing config file", e);
                    Path old = new File(configFile.getParentFile(), "chat.json.old").toPath(); // place to copy config
                    if (old.toFile().exists()) {
                        try {
                            Files.delete(old); // delete any previously invalid configs, if present
                        } catch (Exception ignored) {
                        }
                    }
                    Files.move(configFile.toPath(), old); // move the invalid config to the new location
                } catch (IOException ex) {
                    try {
                        Files.delete(configFile.toPath()); // just delete the old config - we've tried everything else
                    } catch (IOException exc) { // something is not right - just crash.
                        RuntimeException exception = new IllegalStateException(exc);
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
        Config config = new Config(); //create a new config instance
        config.isLoading = false; // mark as loaded
        config.configFile = configFile; // set the associated value
        config.save(); // save the config
        return config; // return it
    }

    /**
     * Saves the configuration file
     */
    private void save() {
        if (!this.isLoading) { // Check if we are still loading the file
            LOGGER.trace("Saving config file"); // we're not loading it, so we can save
            try (FileWriter writer = new FileWriter(configFile)) { // open the file for writing
                GSON.toJson(this, writer); // write the values
                writer.flush(); // flush the buffer
            } catch (IOException e) {
                LOGGER.error("Failed to save config file: ", e);
            }
        } else {
            LOGGER.warn("Not saving config file as it is still loading");
        }
    }

    public ObservableList<Account> getAccounts() {
        return accounts;
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

    public void setLastAccount(int lastAccount) {
        this.lastAccount = lastAccount;
        this.save();
    }
}
