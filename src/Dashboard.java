import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Dashboard extends JFrame {

    private final NetworkMonitor monitor = new NetworkMonitor();
    private final NetworkDAO networkDAO = new NetworkDAO();
    private int saveCounter = 0;

    private final JLabel statusLabel = new JLabel("Checking...");
    private final JLabel interfaceLabel = new JLabel("-");
    private final JLabel ipLabel = new JLabel("-");
    private final JLabel gatewayLabel = new JLabel("-");

    private final JLabel downloadLabel = new JLabel("0.00 Mbps");
    private final JLabel uploadLabel = new JLabel("0.00 Mbps");
    private final JLabel latencyLabel = new JLabel("-- ms");

    private final JLabel receivedLabel = new JLabel("0 B");
    private final JLabel sentLabel = new JLabel("0 B");

    private final SpeedGraph graph = new SpeedGraph();

    private final ScheduledExecutorService scheduler =
        Executors.newSingleThreadScheduledExecutor();

    public Dashboard() {

        setTitle("Java Laptop Network Monitor");
        setSize(850, 650);
        setMinimumSize(new Dimension(650, 550));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        getContentPane().setBackground(
            new Color(20, 25, 35)
        );

        setLayout(new BorderLayout(15, 15));

        add(createHeader(), BorderLayout.NORTH);
        add(createMainPanel(), BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                scheduler.shutdownNow();
            }
        });

        startMonitoring();
    }

    private JPanel createHeader() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 38, 52));
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("NETWORK MONITOR");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 23));

        statusLabel.setForeground(Color.GREEN);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 14));

        panel.add(title, BorderLayout.WEST);
        panel.add(statusLabel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createMainPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 15));
        panel.setBackground(new Color(20, 25, 35));
        panel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel speedPanel = new JPanel(new GridLayout(1, 3, 15, 15));
        speedPanel.setOpaque(false);

        speedPanel.add(createCard(
            "DOWNLOAD SPEED", downloadLabel,
            new Color(50, 200, 130)
        ));

        speedPanel.add(createCard(
            "UPLOAD SPEED", uploadLabel,
            new Color(70, 160, 255)
        ));

        speedPanel.add(createCard(
            "LATENCY", latencyLabel,
            new Color(255, 190, 80)
        ));

        JPanel centerPanel = new JPanel(new BorderLayout(10, 15));
        centerPanel.setOpaque(false);

        graph.setPreferredSize(new Dimension(700, 230));

        JPanel infoPanel = createInfoPanel();

        centerPanel.add(speedPanel, BorderLayout.NORTH);
        centerPanel.add(graph, BorderLayout.CENTER);
        centerPanel.add(infoPanel, BorderLayout.SOUTH);

        panel.add(centerPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCard(
        String title,
        JLabel value,
        Color color
    ) {

        JPanel card = new JPanel(new BorderLayout(5, 15));

        card.setBackground(new Color(35, 45, 62));
        card.setBorder(new CompoundBorder(
            new LineBorder(new Color(60, 75, 95), 1, true),
            new EmptyBorder(20, 15, 20, 15)
        ));

        JLabel heading = new JLabel(title);
        heading.setForeground(Color.LIGHT_GRAY);

        value.setForeground(color);
        value.setFont(new Font("SansSerif", Font.BOLD, 22));

        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        return card;
    }

    private JPanel createInfoPanel() {

        JPanel panel = new JPanel(new GridLayout(2, 3, 15, 15));

        panel.setBackground(new Color(30, 38, 52));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        panel.add(createInfoItem("INTERFACE", interfaceLabel));
        panel.add(createInfoItem("IP ADDRESS", ipLabel));
        panel.add(createInfoItem("GATEWAY", gatewayLabel));
        panel.add(createInfoItem("TOTAL DOWNLOAD", receivedLabel));
        panel.add(createInfoItem("TOTAL UPLOAD", sentLabel));

        JLabel refresh = new JLabel("Refresh: 1 second");
        refresh.setForeground(Color.LIGHT_GRAY);
        panel.add(refresh);

        return panel;
    }

    private JPanel createInfoItem(
        String title,
        JLabel value
    ) {

        JPanel item = new JPanel(new GridLayout(2, 1));
        item.setOpaque(false);

        JLabel heading = new JLabel(title);
        heading.setForeground(Color.GRAY);

        value.setForeground(Color.WHITE);
        value.setFont(new Font("SansSerif", Font.BOLD, 13));

        item.add(heading);
        item.add(value);

        return item;
    }

    private JPanel createFooter() {

        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panel.setBackground(new Color(20, 25, 35));

        JLabel footer = new JLabel(
            "Java Laptop Network Monitor | Linux"
        );

        footer.setForeground(Color.GRAY);

        panel.add(footer);

        return panel;
    }

    private void startMonitoring() {

    scheduler.scheduleAtFixedRate(() -> {

        NetworkStats stats = monitor.getStats();

        // Save data every 5 seconds
        saveCounter++;

        if (saveCounter >= 5) {

            networkDAO.saveStats(stats);

            saveCounter = 0;
        }

        // Update GUI
        SwingUtilities.invokeLater(() -> {

            updateDashboard(stats);

        });

        }, 0, 1, TimeUnit.SECONDS);
    }

    private void updateDashboard(NetworkStats stats) {

        statusLabel.setText(stats.status);

        statusLabel.setForeground(
            stats.connected ? Color.GREEN : Color.RED
        );

        interfaceLabel.setText(stats.interfaceName);
        ipLabel.setText(stats.ipAddress);
        gatewayLabel.setText(stats.gateway);

        downloadLabel.setText(
            String.format("%.2f Mbps", stats.downloadSpeed)
        );

        uploadLabel.setText(
            String.format("%.2f Mbps", stats.uploadSpeed)
        );

        latencyLabel.setText(
            stats.latency >= 0
                ? String.format("%.1f ms", stats.latency)
                : "Unavailable"
        );

        receivedLabel.setText(formatBytes(stats.receivedBytes));
        sentLabel.setText(formatBytes(stats.transmittedBytes));

        graph.addData(
            stats.downloadSpeed,
            stats.uploadSpeed
        );
    }

    private String formatBytes(long bytes) {

        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};

        int index = 0;

        while (value >= 1024 && index < units.length - 1) {
            value /= 1024;
            index++;
        }

        return String.format("%.2f %s", value, units[index]);
    }

    // Live network graph

    private class SpeedGraph extends JPanel {

        private final List<Double> downloads = new ArrayList<>();
        private final List<Double> uploads = new ArrayList<>();

        public SpeedGraph() {

            setBackground(new Color(30, 38, 52));

            setBorder(new EmptyBorder(10, 10, 10, 10));
        }

        public void addData(double download, double upload) {

            downloads.add(download);
            uploads.add(upload);

            if (downloads.size() > 60) {
                downloads.remove(0);
                uploads.remove(0);
            }

            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int width = getWidth();
            int height = getHeight();

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 14));

            g2.drawString("LIVE NETWORK SPEED (Mbps)", 15, 20);

            int left = 45;
            int top = 40;
            int graphWidth = width - 65;
            int graphHeight = height - 65;

            double max = 10;

            for (double value : downloads) {
                max = Math.max(max, value);
            }

            for (double value : uploads) {
                max = Math.max(max, value);
            }

            max = Math.ceil(max / 5.0) * 5.0;

            // Grid lines

            g2.setColor(new Color(65, 75, 90));

            for (int i = 0; i <= 4; i++) {

                int y = top + i * graphHeight / 4;

                g2.drawLine(left, y, left + graphWidth, y);

                g2.setColor(Color.LIGHT_GRAY);
                g2.drawString(
                    String.format("%.0f", max * (4 - i) / 4),
                    5, y + 5
                );

                g2.setColor(new Color(65, 75, 90));
            }

            drawLine(
                g2, downloads, Color.GREEN,
                left, top, graphWidth, graphHeight, max
            );

            drawLine(
                g2, uploads, Color.CYAN,
                left, top, graphWidth, graphHeight, max
            );

            g2.setColor(Color.GREEN);
            g2.drawString("Download", width - 180, 20);

            g2.setColor(Color.CYAN);
            g2.drawString("Upload", width - 95, 20);

            g2.dispose();
        }

        private void drawLine(
            Graphics2D g,
            List<Double> values,
            Color color,
            int left,
            int top,
            int width,
            int height,
            double max
        ) {

            if (values.size() < 2) return;

            g.setColor(color);
            g.setStroke(new BasicStroke(2));

            for (int i = 1; i < values.size(); i++) {

                int x1 = left + (i - 1) * width / 59;
                int x2 = left + i * width / 59;

                int y1 = top + height -
                    (int)(values.get(i - 1) / max * height);

                int y2 = top + height -
                    (int)(values.get(i) / max * height);

                g.drawLine(x1, y1, x2, y2);
            }
        }
    }
}