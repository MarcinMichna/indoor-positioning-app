package pl.michnam.app.config;

public class AppConfig {
    public static final String host = "http://michnam.pl:5000";
    public static final int wifiScanWaitTime = 500; // in ms
    public static final int bleScanWaitTime = 1000; // in ms
    public static final int bleScanTime = 2500; // in ms
    public static final int btScanWaitTime = 500; // in ms
    public static final int btScanTime = 1500; // in ms
    public static final int wifiAreaScanWaitTime = 300; //in ms
    public static final String databaseName = "ScanData.db";
    public static final int mainNotificationId = 364;
    public static final int minNumberOfSignalsToAnalyse = 20;
    public static final int maxScanAge = 1000 * 35; // in ms
    public static final int fittingThreshold = 10; // in %
}
