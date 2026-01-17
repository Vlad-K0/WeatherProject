package com.vladko.Database;

import com.vladko.Utils.PropertyParsers.YamlPropertySourceFactory;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ConnectionPool implements DataSource {


    private final int connectionPoolSize;
    private final String databasePassword;
    private final String databaseUsername;
    private final String databaseUrl;

    private List<Connection> connections = new ArrayList<>();
    private BlockingDeque<Connection> proxyConnections = new LinkedBlockingDeque<>();


    public ConnectionPool(int connectionPoolSize,
                          String databasePassword,
                          String databaseUsername,
                          String databaseUrl) throws SQLException {
        this.connectionPoolSize = connectionPoolSize;
        this.databasePassword = databasePassword;
        this.databaseUsername = databaseUsername;
        this.databaseUrl = databaseUrl;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL Driver not found", e);
        }

        initConnectionPool();
    }

    private void initConnectionPool() throws SQLException {
        for (int i = 0; i < connectionPoolSize; i++) {
            Connection connection = DriverManager.getConnection(
                    databaseUrl,
                    databaseUsername,
                    databasePassword
            );
            connections.add(connection);

            Connection proxyConnection = (Connection) Proxy.newProxyInstance(
                    ConnectionPool.class.getClassLoader(),
                    new Class[]{Connection.class},
                    (proxy, method, args) -> {
                        if (method.getName().equals("close")) {
                            return proxyConnections.add((Connection) proxy); // Возвращаем в пул сам прокси-объект
                        }
                        try {
                            return method.invoke(connection, args);
                        } catch (java.lang.reflect.InvocationTargetException e) {
                            throw e.getTargetException();
                        } catch (IllegalAccessException | IllegalArgumentException e) {
                            throw new RuntimeException("Error during proxy delegation.", e);
                        }
                    });
            proxyConnections.add(proxyConnection);

        }
    }

    @PreDestroy
    public void close() {
        System.out.println("INFO: Closing ConnectionPool and all real connections...");

        // 1. Сначала очищаем очередь (на всякий случай, если там остались прокси)
        proxyConnections.clear();

        // 2. Закрываем реальные соединения
        for (Connection realConnection : connections) {
            try {
                if (realConnection != null && !realConnection.isClosed()) {
                    realConnection.close();
                }
            } catch (SQLException e) {
                System.err.println("WARNING: Failed to close a real connection: " + e.getMessage());
                // Здесь лучше не бросать исключение, а просто залогировать и продолжить
            }
        }
        connections.clear();
        System.out.println("INFO: ConnectionPool shutdown complete.");
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return proxyConnections.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Failed to retrieve connection from pool.", e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {

    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
