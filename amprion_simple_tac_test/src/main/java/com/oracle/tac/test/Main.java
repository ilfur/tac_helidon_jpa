package com.oracle.tac.test;

import io.helidon.config.Config;

import static io.helidon.config.ConfigSources.classpath;
import static io.helidon.config.ConfigSources.file;

import io.helidon.microprofile.server.Server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Our customized server start, which allows for an external configuration file
 * to configure our database.
 */
public class Main {
    /**
     * Used for logging.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    /**
     * Our application main method just starts the server.
     * @param args not used
     */
    public static void main(final String[] args) {
        Server server = startServer(buildConfig());
        
        LOGGER.info("Server now listening on http://localhost: {}", server.port());
    }

    /**
     * Starts the <i>Helidon</i> {@code Server} using the given
     * <i>microprofile</i> configuration.
     * @param config the <i>microprofile</i> configuration to use for the
     * server.
     * @return the started server instance.
     */
    static Server startServer(final Config config) {
        return Server.builder().config(config).build().start();
    }

    /**
     * Creates a {@code Config} instance, which contains a configuration
     * from an optional local configuration file.
     * @return a {@code Config} instance.
     */
    static Config buildConfig() {
        return Config.builder()
                .sources(
                        file("config/microprofile-config-db.properties").optional(),
                        classpath("META-INF/microprofile-config.properties"))
                .build();
    }
    
}
