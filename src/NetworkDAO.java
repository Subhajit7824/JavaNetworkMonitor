import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NetworkDAO {

    private static final String INSERT_QUERY =

        "INSERT INTO network_logs (" +
        "interface_name, " +
        "ip_address, " +
        "gateway, " +
        "download_speed, " +
        "upload_speed, " +
        "latency, " +
        "received_bytes, " +
        "transmitted_bytes, " +
        "connection_status" +
        ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    public void saveStats(NetworkStats stats) {

        try (
            Connection connection =
                DatabaseConnection.getConnection();

            PreparedStatement statement =
                connection.prepareStatement(INSERT_QUERY)
        ) {

            statement.setString(
                1, stats.interfaceName
            );

            statement.setString(
                2, stats.ipAddress
            );

            statement.setString(
                3, stats.gateway
            );

            statement.setDouble(
                4, stats.downloadSpeed
            );

            statement.setDouble(
                5, stats.uploadSpeed
            );

            statement.setDouble(
                6, stats.latency
            );

            statement.setLong(
                7, stats.receivedBytes
            );

            statement.setLong(
                8, stats.transmittedBytes
            );

            statement.setString(
                9, stats.status
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            System.err.println(
                "Failed to save network data: "
                + e.getMessage()
            );
        }
    }
}