public class NetworkStats {

    public String interfaceName = "Unknown";
    public String ipAddress = "Unknown";
    public String gateway = "Unknown";

    public boolean connected = false;

    public double downloadSpeed = 0;
    public double uploadSpeed = 0;

    public double latency = -1;

    public long receivedBytes = 0;
    public long transmittedBytes = 0;

    public String status = "Disconnected";
}