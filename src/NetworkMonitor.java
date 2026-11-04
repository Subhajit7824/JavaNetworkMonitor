import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;

public class NetworkMonitor {

    private long previousReceived = -1;
    private long previousTransmitted = -1;
    private long previousTime = -1;

    private String previousInterface = "";

    public NetworkStats getStats() {

        NetworkStats stats = new NetworkStats();

        try {
            String iface = getActiveInterface();

            if (iface == null) {
                return stats;
            }

            stats.interfaceName = iface;
            stats.ipAddress = getIPAddress(iface);
            stats.gateway = getGateway();
            stats.connected = !stats.ipAddress.equals("Unknown");
            stats.status = stats.connected
                    ? "Connected"
                    : "Disconnected";

            long received = getBytes(iface, "rx_bytes");
            long transmitted = getBytes(iface, "tx_bytes");

            stats.receivedBytes = received;
            stats.transmittedBytes = transmitted;

            long currentTime = System.nanoTime();

            if (previousTime != -1 &&
                iface.equals(previousInterface)) {

                double seconds =
                    (currentTime - previousTime) / 1_000_000_000.0;

                if (seconds > 0 &&
                    received >= previousReceived &&
                    transmitted >= previousTransmitted) {

                    stats.downloadSpeed =
                        ((received - previousReceived) * 8.0)
                        / seconds / 1_000_000.0;

                    stats.uploadSpeed =
                        ((transmitted - previousTransmitted) * 8.0)
                        / seconds / 1_000_000.0;
                }
            }

            previousReceived = received;
            previousTransmitted = transmitted;
            previousTime = currentTime;
            previousInterface = iface;

            stats.latency = getLatency();

        } catch (Exception e) {
            System.err.println("Monitoring error: " + e.getMessage());
        }

        return stats;
    }

    private String getActiveInterface() throws IOException {

        Process process = new ProcessBuilder(
            "ip", "-o", "-4", "addr", "show", "up"
        ).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.trim().split("\\s+");

                if (parts.length > 3 &&
                    !parts[1].equals("lo") &&
                    parts[2].equals("inet")) {

                    String iface = parts[1];

                    if (Files.exists(
                        Paths.get("/sys/class/net/" + iface))) {

                        return iface;
                    }
                }
            }

        }

        return null;
    }

    private String getIPAddress(String iface) throws IOException {

        Process process = new ProcessBuilder(
            "ip", "-4", "addr", "show", "dev", iface
        ).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line;

            while ((line = reader.readLine()) != null) {

                line = line.trim();

                if (line.startsWith("inet ")) {
                    return line.split("\\s+")[1].split("/")[0];
                }
            }
        }

        return "Unknown";
    }

    private String getGateway() throws IOException {

        Process process = new ProcessBuilder(
            "ip", "route", "show", "default"
        ).start();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {

            String line = reader.readLine();

            if (line != null) {

                String[] parts = line.split("\\s+");

                for (int i = 0; i < parts.length - 1; i++) {
                    if (parts[i].equals("via")) {
                        return parts[i + 1];
                    }
                }
            }
        }

        return "Unknown";
    }

    private long getBytes(String iface, String type)
            throws IOException {

        Path path = Paths.get(
            "/sys/class/net/" + iface + "/statistics/" + type
        );

        return Long.parseLong(Files.readString(path).trim());
    }

    private double getLatency() {

        try {
            long start = System.nanoTime();

            InetAddress address =
                InetAddress.getByName("1.1.1.1");

            boolean reachable = address.isReachable(1000);

            if (reachable) {
                return (System.nanoTime() - start)
                    / 1_000_000.0;
            }

        } catch (Exception ignored) {
        }

        return -1;
    }
}