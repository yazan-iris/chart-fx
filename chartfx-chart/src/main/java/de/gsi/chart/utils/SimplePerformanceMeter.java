package de.gsi.chart.utils;

import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.Scene;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.javafx.perf.PerformanceTracker; // keep for the future in case this becomes public API
import com.sun.management.OperatingSystemMXBean;

public class SimplePerformanceMeter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimplePerformanceMeter.class);
    private static final int MIN_UPDATE_PERIOD = 40; // [ms]
    private static final int MAX_UPDATE_PERIOD = 10_000; // [ms]
    private static final OperatingSystemMXBean OS_BEAN = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private static final int N_CORES = OS_BEAN.getAvailableProcessors();

    // IIR-alpha typically: alpha ~ Ts /(Ts+T) with Ts = 100 & alpha = 0.01 -> T~10s
    private final DoubleProperty averageFactor = new SimpleDoubleProperty(this, "averageFactor", 0.01);
    private final DoubleProperty pulseRate = new SimpleDoubleProperty(this, "pulseRate", 0);
    private final DoubleProperty pulseRateAvg = new SimpleDoubleProperty(this, "pulseRateAvg", 0);
    private final DoubleProperty trackerFrameRate = new SimpleDoubleProperty(this, "trackerFrameRate", 0);
    private final DoubleProperty avgTrackerFrameRate = new SimpleDoubleProperty(this, "avgTrackerFrameRate", 0);
    private final DoubleProperty processCpuLoad = new SimpleDoubleProperty(this, "processCpuLoad", 0);
    private final DoubleProperty minProcessCpuLoad = new SimpleDoubleProperty(this, "minProcessCpuLoad", -1);
    private final DoubleProperty avgProcessCpuLoad = new SimpleDoubleProperty(this, "avgProcessCpuLoad", -1);
    private final DoubleProperty maxProcessCpuLoad = new SimpleDoubleProperty(this, "maxProcessCpuLoad", -1);
    private final DoubleProperty systemCpuLoad = new SimpleDoubleProperty(this, "systemCpuLoad", 0);
    private final DoubleProperty avgSystemCpuLoad = new SimpleDoubleProperty(this, "avgSystemCpuLoad", -1);
    private double pulseRateInternal;
    private double pulseRateAvgInternal = -1;
    private double frameRateInternal;
    private double frameRateAvgInternal = -1;
    private double cpuLoadProcessInternal;
    private double cpuLoadProcessAvgInternal;
    private double cpuLoadSystemInternal;
    private double cpuLoadSystemAvgInternal = -1;
    private final Scene scene;
    private final Field dirtyRootBits;
    private final Field dirtyNodesSize;
    private final long updateDuration;
    private final PerformanceTracker fxPerformanceTracker; // keep for the future in case this becomes public
    private Timer timer;
    private final AnimationTimer animationTimer;
    private long timerIterationLast = System.currentTimeMillis();
    private final AtomicInteger pulseCounter = new AtomicInteger(0);
    private final AtomicInteger frameCounter = new AtomicInteger(0);
    private final Runnable pulseListener = () -> {
        if (isSceneDirty()) {
            frameCounter.getAndIncrement();
        }
    };

    public SimplePerformanceMeter(Scene scene, long updateDuration) {
        if (scene == null) {
            throw new IllegalArgumentException("scene must not be null");
        }
        this.scene = scene;
        this.updateDuration = Math.max(MIN_UPDATE_PERIOD, Math.min(updateDuration, MAX_UPDATE_PERIOD));
        fxPerformanceTracker = PerformanceTracker.getSceneTracker(scene); // keep for the future in case this becomes public

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                pulseCounter.getAndIncrement();
            }
        };

        Field field1 = null;
        Field field2 = null;
        try {
            field1 = Node.class.getDeclaredField("dirtyBits");
            field1.setAccessible(true);
            field2 = Scene.class.getDeclaredField("dirtyNodesSize");
            field2.setAccessible(true);
        } catch (SecurityException | NoSuchFieldException e) {
            LOGGER.atError().setCause(e).log("cannot access scene root's dirtyBits field");
        }
        dirtyRootBits = field1;
        dirtyNodesSize = field2;

        registerListener(); // NOPMD
    }

    public ReadOnlyDoubleProperty actualFrameRateProperty() {
        return trackerFrameRate;
    }

    /**
     * 
     * IIR-alpha filter constant as in y(n) = alpha * x(n) + (1-alpha) * y(n-1)
     * 
     * typically: alpha ~ Ts /(Ts+T) with
     * 
     * 'Ts' being the sampling period, and 'T' the desired IIR time constant
     * 
     * @return average factor alpha
     */
    public DoubleProperty averageFactorProperty() {
        return averageFactor;
    }

    public ReadOnlyDoubleProperty averageFrameRateProperty() {
        return avgTrackerFrameRate;
    }

    public ReadOnlyDoubleProperty averageFxFrameRateProperty() {
        return pulseRateAvg;
    }

    public ReadOnlyDoubleProperty averageProcessCpuLoadProperty() {
        return avgProcessCpuLoad;
    }

    public ReadOnlyDoubleProperty averageSystemCpuLoadProperty() {
        return avgSystemCpuLoad;
    }

    public void deregisterListener() {
        animationTimer.stop();
        // only available for Java9+
        // scene.removePostLayoutPulseListener(pulseListener);
        timer.cancel();
    }

    public ReadOnlyDoubleProperty fxFrameRateProperty() {
        return pulseRate;
    }

    public double getActualFrameRate() {
        return actualFrameRateProperty().get();
    }

    public double getAverageFrameRate() {
        return averageFrameRateProperty().get();
    }

    public double getAverageFxFrameRate() {
        return averageFxFrameRateProperty().get();
    }

    public double getAverageProcessCpuLoad() {
        return averageProcessCpuLoadProperty().get();
    }

    public double getAverageSystemCpuLoad() {
        return averageSystemCpuLoadProperty().get();
    }

    public double getFxFrameRate() {
        return fxFrameRateProperty().get();
    }

    @Deprecated
    public double getMaxProcessCpuLoad() {
        return minProcessCpuLoadProperty().get();
    }

    @Deprecated
    public double getMinProcessCpuLoad() {
        return minProcessCpuLoadProperty().get();
    }

    public double getProcessCpuLoad() {
        return processCpuLoadProperty().get();
    }

    public double getSystemCpuLoad() {
        return systemCpuLoadProperty().get();
    }

    @Deprecated // remove for next x.2.y release
    public ReadOnlyDoubleProperty maxProcessCpuLoadProperty() {
        return maxProcessCpuLoad;
    }

    @Deprecated // remove for next x.2.y release
    public ReadOnlyDoubleProperty minProcessCpuLoadProperty() {
        return minProcessCpuLoad;
    }

    public ReadOnlyDoubleProperty processCpuLoadProperty() {
        return processCpuLoad;
    }

    public void registerListener() {
        animationTimer.start();
        timer = new Timer("SimplePerformanceMeter-timer", true);
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                final long timerIterationThis = System.currentTimeMillis();
                final int pulseCount = pulseCounter.getAndSet(0);
                final int frameCount = frameCounter.getAndSet(0);
                final double diff = (timerIterationThis - timerIterationLast) * 1e-3;
                timerIterationLast = timerIterationThis;
                pulseRateInternal = diff > 0 ? pulseCount / diff : -1;
                frameRateInternal = diff > 0 ? frameCount / diff : -1;
                // alt via fxPerformanceTracker - keep for the future in case this becomes public
                pulseRateInternal = fxPerformanceTracker.getInstantPulses();
                frameRateInternal = fxPerformanceTracker.getInstantFPS();

                cpuLoadProcessInternal = OS_BEAN.getProcessCpuLoad() * 100 * N_CORES;
                cpuLoadSystemInternal = OS_BEAN.getSystemCpuLoad() * 100 * N_CORES;
                // alt via non sun classes - keep for the future in case this becomes public
                // cpuLoadProcessInternal = getProcessCpuLoadInternal() * N_CORES;
                // cpuLoadSystemInternal = OS_BEAN.getSystemLoadAverage() * N_CORES;

                final double alpha = averageFactor.get();
                pulseRateAvgInternal = computeAverage(pulseRateInternal, pulseRateAvgInternal, alpha);
                frameRateAvgInternal = computeAverage(frameRateInternal, frameRateAvgInternal, alpha);
                cpuLoadProcessAvgInternal = computeAverage(cpuLoadProcessInternal, cpuLoadProcessAvgInternal, alpha);
                cpuLoadSystemAvgInternal = computeAverage(cpuLoadSystemInternal, cpuLoadSystemAvgInternal, alpha);

                FXUtils.runFX(SimplePerformanceMeter.this::updateProperties);
            }
        }, 0, updateDuration);
        // available since java 9+
        //scene.addPostLayoutPulseListener(pulseListener);
    }

    public void resetAverages() {
        pulseRateInternal = -1;
        pulseRateAvgInternal = -1;
        frameRateInternal = -1;
        frameRateAvgInternal = -1;
        cpuLoadProcessInternal = -1;
        cpuLoadProcessAvgInternal = -1;
        cpuLoadSystemInternal = -1;
        cpuLoadSystemAvgInternal = -1;
    }

    public ReadOnlyDoubleProperty systemCpuLoadProperty() {
        return systemCpuLoad;
    }

    private void updateProperties() {
        // to be compatible with 'top' definition multiply CPU loads with number
        // of cores, ie. one fully loaded core yields 100%

        pulseRate.set(pulseRateInternal);
        pulseRateAvg.set(pulseRateAvgInternal);
        trackerFrameRate.set(frameRateInternal);
        avgTrackerFrameRate.set(frameRateAvgInternal);
        processCpuLoad.set(cpuLoadProcessInternal);
        avgProcessCpuLoad.set(cpuLoadProcessAvgInternal);
        systemCpuLoad.set(cpuLoadSystemInternal);
        avgSystemCpuLoad.set(cpuLoadSystemAvgInternal);

        // TODO: following methods are deprecated and will be removed for x.2.y
        if (minProcessCpuLoad.get() < 0) {
            minProcessCpuLoad.set(processCpuLoad.get());
        } else {
            minProcessCpuLoad.set(Math.min(minProcessCpuLoad.get(), processCpuLoad.get()));
        }
        if (maxProcessCpuLoad.get() < 0) {
            maxProcessCpuLoad.set(processCpuLoad.get());
        } else {
            maxProcessCpuLoad.set(Math.max(maxProcessCpuLoad.get(), processCpuLoad.get()));
        }
    }

    public boolean isSceneDirty() {
        if (scene.getRoot() == null) {
            return false;
        }
        try {
            return dirtyNodesSize.getInt(scene) != 0 || dirtyRootBits.getInt(scene.getRoot()) != 0;
        } catch (IllegalAccessException | IllegalArgumentException ignoreException) {
            LOGGER.atError().setCause(ignoreException).log("cannot access scene root's dirtyBits field");
            return true;
        }
        // alternate implementation (potential issues with Java Jigsaw (com.sun... dependency):
        // return !NodeHelper.isDirtyEmpty(scene.getRoot())
    }

    protected static double computeAverage(final double newValue, final double oldValue, final double alpha) {
        if (oldValue < 0) {
            return newValue;
        }
        return (1 - alpha) * oldValue + alpha * newValue;
    }

    public static double getProcessCpuLoadInternal() {
        final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();

        try {
            final ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            final AttributeList list = mbs.getAttributes(name, new String[] { "ProcessCpuLoad" });

            if (list.isEmpty()) {
                return Double.NaN;
            }

            final Attribute att = (Attribute) list.get(0);
            final Double value = (Double) att.getValue();

            // usually takes a couple of seconds before we get real values
            if (value == -1.0) {
                return Double.NaN;
            }
            // returns a percentage value with 1 decimal point precision
            return ((int) (value * 1000) / 10.0);
        } catch (MalformedObjectNameException | NullPointerException | InstanceNotFoundException | ReflectionException e) {
            return Double.NaN;
        }
    }
}
