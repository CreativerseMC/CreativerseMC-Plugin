package com.creativerse.files;
import java.io.File;
import java.sql.*;

public class Database {
    private static Connection c;

    public static void initiate(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            File f = new File(path);
            if (!f.exists()) {
                f.mkdirs();
                f = new File(path + "database.db");
                f.createNewFile();
            }
            c = DriverManager.getConnection("jdbc:sqlite:" + path + "database.db");
            c.setAutoCommit(false);
            execute("CREATE TABLE IF NOT EXISTS USERS " +
                    "(UUID CHAR(36) NOT NULL UNIQUE, " +
                    "ADDRESS CHAR(42) NOT NULL UNIQUE);");
            commit();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }

    public static void execute(String statement) throws SQLException {
        Statement stmt = c.createStatement();
        stmt.executeUpdate(statement);
        stmt.close();
    }

    public static String[] query(String statement) throws SQLException {
        Statement stmt = c.createStatement();
        ResultSet rs = stmt.executeQuery(statement);
        String[] x = new String[2];
        x[0] = rs.getString("UUID");
        x[1] = rs.getString("Address");
        return x;
    }

    public static void commit() throws SQLException {
        c.commit();
    }
}
