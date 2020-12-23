package pl.michnam.app.config;

public class AppConfig {
    public static final String host = "http://michnam.pl:5000";
    public static final int wifiScanWaitTime = 3000; // in ms
    public static final int wifiAreaScanWaitTime = 500; //in ms
    public static final int bleScanWaitTime = 2000; // in ms
    public static final int bleScanTime = 2500; // in ms
    public static final int btScanWaitTime = 2000; // in ms
    public static final int btScanTime = 1500; // in ms
    public static final String databaseName = "ScanData.db";
    public static final int mainNotificationId = 364;
    public static final int minNumberOfSignalsToAnalyse = 10;
    public static final int maxScanAge = 1000 * 35; // in ms
    public static final int fittingThreshold = 10; // in %
    public static final int apiRequestWaitTime = 2000;
}
