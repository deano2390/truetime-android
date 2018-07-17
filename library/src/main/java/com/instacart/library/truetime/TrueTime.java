package com.instacart.library.truetime;

import android.content.Context;
import android.os.SystemClock;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;

public class TrueTime {

    private static final String TAG = TrueTime.class.getSimpleName();
    private final SntpClient SNTP_CLIENT = new SntpClient();

    private float _rootDelayMax = 100;
    private float _rootDispersionMax = 100;
    private int _serverResponseDelayMax = 750;
    private int _udpSocketTimeoutInMillis = 30_000;

    private String _ntpHost = "1.us.pool.ntp.org";

    /**
     * @return Date object that returns the current time in the default Timezone
     */
    public Date now() {
        if (!isInitialized()) {
            throw new IllegalStateException("You need to call init() on TrueTime at least once.");
        }

        long cachedSntpTime = _getCachedSntpTime();
        long cachedDeviceUptime = _getCachedDeviceUptime();
        long deviceUptime = SystemClock.elapsedRealtime();
        long now = cachedSntpTime + (deviceUptime - cachedDeviceUptime);

        return new Date(now);
    }

    public boolean isInitialized() {
        return SNTP_CLIENT.wasInitialized();
    }

    public void forceInitialize() throws IOException {
        forceInitialize(_ntpHost);
    }

    public void initialize() throws IOException {
        initialize(_ntpHost);
    }

    public synchronized TrueTime withConnectionTimeout(int timeoutInMillis) {
        _udpSocketTimeoutInMillis = timeoutInMillis;
        return this;
    }

    public synchronized TrueTime withRootDelayMax(float rootDelayMax) {
        if (rootDelayMax > _rootDelayMax) {
            String log = String.format(Locale.getDefault(),
                    "The recommended max rootDelay value is %f. You are setting it at %f",
                    _rootDelayMax, rootDelayMax);
            TrueLog.w(TAG, log);
        }

        _rootDelayMax = rootDelayMax;
        return this;
    }

    public synchronized TrueTime withRootDispersionMax(float rootDispersionMax) {
        if (rootDispersionMax > _rootDispersionMax) {
            String log = String.format(Locale.getDefault(),
                    "The recommended max rootDispersion value is %f. You are setting it at %f",
                    _rootDispersionMax, rootDispersionMax);
            TrueLog.w(TAG, log);
        }

        _rootDispersionMax = rootDispersionMax;
        return this;
    }

    public synchronized TrueTime withServerResponseDelayMax(int serverResponseDelayInMillis) {
        _serverResponseDelayMax = serverResponseDelayInMillis;
        return this;
    }

    public synchronized TrueTime withNtpHost(String ntpHost) {
        _ntpHost = ntpHost;
        return this;
    }

    public synchronized TrueTime withLoggingEnabled(boolean isLoggingEnabled) {
        TrueLog.setLoggingEnabled(isLoggingEnabled);
        return this;
    }

    // -----------------------------------------------------------------------------------

    protected void forceInitialize(String ntpHost) throws IOException {
        requestTime(ntpHost);
    }

    protected void initialize(String ntpHost) throws IOException {
        if (isInitialized()) {
            TrueLog.i(TAG, "---- TrueTime already initialized from previous boot/init");
            return;
        }

        requestTime(ntpHost);
    }

    long[] requestTime(String ntpHost) throws IOException {
        return SNTP_CLIENT.requestTime(ntpHost,
                _rootDelayMax,
                _rootDispersionMax,
                _serverResponseDelayMax,
                _udpSocketTimeoutInMillis);
    }

    void cacheTrueTimeInfo(long[] response) {
        SNTP_CLIENT.cacheTrueTimeInfo(response);
    }

    private long _getCachedDeviceUptime() {
        return SNTP_CLIENT.getCachedDeviceUptime();
    }

    private long _getCachedSntpTime() {
        return SNTP_CLIENT.getCachedSntpTime();
    }

}
