import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
        System.getenv("DB_URL");

    private static final String USER = System.getenv("DB_USERNAME");

    private static final String PASSWORD =
        System.getenv("DB_PASSWORD");

    public static Connection getConnection()
            throws SQLException {

        return DriverManager.getConnection(
            URL,
            USER,
            PASSWORD
        );
    }

    public static void main(String[] args) {

        try (Connection connection = getConnection()) {

            System.out.println(
                "MySQL connected successfully!"
            );

        } catch (SQLException e) {

            System.out.println(
                "Database connection failed!"
            );

            e.printStackTrace();
        }
    }
}