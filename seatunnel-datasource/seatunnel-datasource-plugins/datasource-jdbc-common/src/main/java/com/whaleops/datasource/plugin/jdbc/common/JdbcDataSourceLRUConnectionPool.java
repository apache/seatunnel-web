package com.whaleops.datasource.plugin.jdbc.common;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class JdbcDataSourceLRUConnectionPool implements AutoCloseable {
    private static final Duration SLOW_CONNECTION_CREATE_LIMIT = Duration.ofSeconds(2);
    private static final Duration CLEAN_CONNECTION_INTERVAL = Duration.ofMinutes(30);
    private static Cache<String, HikariDataSource> datasourceCache;
    private static ScheduledExecutorService cleanUpExecutor;

    public Connection getConnection(Map<String, String> requestParams) throws SQLException {
        initialize();

        HikariDataSource dataSource;
        try {
            dataSource =
                    datasourceCache.get(
                            datasourceCacheKey(requestParams),
                            createDataSourceLoader(requestParams));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        long start = System.currentTimeMillis();
        Connection connection = dataSource.getConnection();
        long end = System.currentTimeMillis();
        if (end - start > SLOW_CONNECTION_CREATE_LIMIT.toMillis()) {
            log.warn(
                    "Slow jdbc connection creation, cost {}ms for {}",
                    end - start,
                    dataSource.getJdbcUrl());
        }
        return connection;
    }

    protected abstract HikariDataSource createDataSource(Map<String, String> requestParams);

    protected abstract String datasourceCacheKey(Map<String, String> requestParams);

    private Callable<HikariDataSource> createDataSourceLoader(Map<String, String> requestParams) {
        return () -> {
            log.info("Creating jdbc datasource for {}", desensitize(requestParams));
            HikariDataSource dataSource = createDataSource(requestParams);
            log.info("Created jdbc datasource for {}", desensitize(requestParams));
            return dataSource;
        };
    }

    protected Map<String, String> desensitize(Map<String, String> requestParams) {
        Map<String, String> newRequestParams = new HashMap<>(requestParams);
        newRequestParams.remove("password");
        return newRequestParams;
    }

    public void initialize() {
        if (cleanUpExecutor == null) {
            synchronized (JdbcDataSourceLRUConnectionPool.class) {
                if (cleanUpExecutor == null) {
                    log.info("Initializing jdbc datasource connection pool");
                    datasourceCache =
                            CacheBuilder.newBuilder()
                                    .expireAfterAccess(
                                            CLEAN_CONNECTION_INTERVAL.toMillis(),
                                            TimeUnit.MILLISECONDS)
                                    .removalListener(
                                            (RemovalListener<String, HikariDataSource>)
                                                    notification -> {
                                                        try {
                                                            log.info(
                                                                    "Closing expired jdbc datasource connection pool for {}",
                                                                    notification.getKey());
                                                            notification.getValue().close();
                                                            log.info(
                                                                    "Closed expired jdbc datasource connection pool for {}",
                                                                    notification.getKey());
                                                        } catch (Exception e) {
                                                            log.error(
                                                                    "Failed to close expired jdbc datasource connection pool for {}",
                                                                    notification.getKey(),
                                                                    e);
                                                        }
                                                    })
                                    .build();
                    cleanUpExecutor =
                            Executors.newSingleThreadScheduledExecutor(
                                    new ThreadFactoryBuilder()
                                            .setNameFormat(
                                                    "datasource-plugin-jdbc-connection-pool-clean-up-%d")
                                            .build());
                    cleanUpExecutor.scheduleWithFixedDelay(
                            () -> {
                                log.debug("Cleaning up jdbc datasource cache");
                                datasourceCache.cleanUp();
                                log.debug("Cleaned up jdbc datasource cache");
                            },
                            CLEAN_CONNECTION_INTERVAL.toMillis(),
                            CLEAN_CONNECTION_INTERVAL.toMillis(),
                            TimeUnit.MILLISECONDS);
                    log.info("Initialized jdbc datasource connection pool");
                }
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (cleanUpExecutor != null) {
            synchronized (JdbcDataSourceLRUConnectionPool.class) {
                log.info("Closing jdbc datasource connection pool");
                cleanUpExecutor.shutdownNow();
                datasourceCache.cleanUp();
                cleanUpExecutor = null;
                log.info("Closed jdbc datasource connection pool");
            }
        }
    }

    public static String extractDbName(String jdbcUrl) {
        String dbName = null;
        Pattern pattern = Pattern.compile("[:/](\\w+)(?:\\?|&|$)");
        Matcher matcher = pattern.matcher(jdbcUrl);

        while (matcher.find()) {
            dbName = matcher.group(1);
        }

        return dbName;
    }
}
