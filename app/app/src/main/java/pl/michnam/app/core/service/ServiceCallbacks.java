package pl.michnam.app.core.service;

import java.util.ArrayList;

public interface ServiceCallbacks {
    void setResults(ArrayList<String> results);
    void setCurrentArea(String currentArea);
    void setExcludedDevices(ArrayList<String> excludedDevices);
}
