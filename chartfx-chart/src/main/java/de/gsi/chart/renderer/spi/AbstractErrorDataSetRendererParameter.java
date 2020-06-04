package de.gsi.chart.renderer.spi;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;

import de.gsi.chart.axes.Axis;
import de.gsi.chart.renderer.ErrorStyle;
import de.gsi.chart.renderer.LineStyle;
import de.gsi.chart.renderer.RendererDataReducer;
import de.gsi.chart.renderer.datareduction.DefaultDataReducer;
import de.gsi.chart.renderer.datareduction.MaxDataReducer;
import de.gsi.chart.renderer.datareduction.RamanDouglasPeukerDataReducer;
import de.gsi.chart.renderer.datareduction.VisvalingamMaheswariWhyattDataReducer;
import de.gsi.dataset.utils.AssertUtils;

/**
 * simple class to move the various parameters out of the class containing the algorithms uses the shadow field pattern
 * to minimise memory usage (lots of boiler-plate code ... sorry)
 *
 * @author rstein
 * @param <R> generic object type for renderer parameter
 */
@SuppressWarnings({ "PMD.TooManyMethods", "PMD.TooManyFields", "PMD.ExcessivePublicCount" }) // designated purpose of
// this class
public abstract class AbstractErrorDataSetRendererParameter<R extends AbstractErrorDataSetRendererParameter<R>>
        extends AbstractPointReductionManagment<R> {
    // intensity fading factor per stage
    protected static final double DEFAULT_HISTORY_INTENSITY_FADING = 0.65;
    private final ObjectProperty<ErrorStyle> errorStyle = new SimpleObjectProperty<ErrorStyle>(this, "errorStyle",
            ErrorStyle.ERRORCOMBO);
    private final ObjectProperty<RendererDataReducer> rendererDataReducer = new SimpleObjectProperty<RendererDataReducer>(this,
            "rendererDataReducer", new DefaultDataReducer());

    private final IntegerProperty dashSize = new SimpleIntegerProperty(this, "dashSize", 3);
    private final DoubleProperty markerSize = new SimpleDoubleProperty(this, "markerSize", 1.5);
    private final BooleanProperty drawMarker = new SimpleBooleanProperty(this, "drawMarker", true);
    private final ObjectProperty<LineStyle> polyLineStyle = new SimpleObjectProperty<LineStyle>(this, "polyLineStyle",
            LineStyle.NORMAL);
    private final BooleanProperty drawChartDataSets = new SimpleBooleanProperty(this, "drawChartDataSets", true);
    private final BooleanProperty drawBars = new SimpleBooleanProperty(this, "drawBars", false);
    private final BooleanProperty shiftBar = new SimpleBooleanProperty(this, "shiftBar", true);
    private final IntegerProperty shiftBarOffset = new SimpleIntegerProperty(this, "shiftBarOffset", 3);
    private final BooleanProperty dynamicBarWidth = new SimpleBooleanProperty(this, "dynamicBarWidth", true);
    private final DoubleProperty barWidthPercentage = new SimpleDoubleProperty(this, "barWidthPercentage", 70.0);
    private final IntegerProperty barWidth = new SimpleIntegerProperty(this, "barWidth", 5);
    private final DoubleProperty intensityFading = new SimpleDoubleProperty(this, "intensityFading",
            AbstractErrorDataSetRendererParameter.DEFAULT_HISTORY_INTENSITY_FADING);
    private final BooleanProperty drawBubbles = new SimpleBooleanProperty(this, "drawBubbles", false);
    private final BooleanProperty allowNaNs = new SimpleBooleanProperty(this, "allowNans", false);

    /**
     * 
     */
    public AbstractErrorDataSetRendererParameter() {
        super();
    }

    /**
     * @return the drawBubbles property
     */
    public BooleanProperty allowNaNsProperty() {
        return allowNaNs;
    }

    public DoubleProperty barWidthPercentageProperty() {
        return barWidthPercentage;
    }

    public IntegerProperty barWidthProperty() {
        return barWidth;
    }

    public IntegerProperty dashSizeProperty() {
        return dashSize;
    }

    /**
     * @return the drawBars state
     */
    public BooleanProperty drawBarsProperty() {
        return drawBars;
    }

    /**
     * @return the drawBubbles property
     */
    public BooleanProperty drawBubblesProperty() {
        return drawBubbles;
    }

    /**
     * @return the drawChartDataSets state, ie. if all or only the DataSets attached to the Renderer shall be drawn 
     */
    public BooleanProperty drawChartDataSetsProperty() {
        return drawChartDataSets;
    }

    /**
     * @return the drawMarker state
     */
    public BooleanProperty drawMarkerProperty() {
        return drawMarker;
    }

    /**
     * @return the dynamicBarWidth state
     */
    public BooleanProperty dynamicBarWidthProperty() {
        return dynamicBarWidth;
    }

    /**
     * sets the error bar/surface plotting style ErrorBarRenderer.ESTYLE_NONE: no errors are drawn
     * ErrorBarRenderer.ESTYLE_BAR: error bars are drawn (default) ErrorBarRenderer.ESTYLE_SURFACE: error surface is
     * drawn
     *
     * @return the errorStyle
     */
    public ObjectProperty<ErrorStyle> errorStyleProperty() {
        return errorStyle;
    }

    /**
     * @return the <code>barWidth</code> in case of constant width bards being drawn @see setDynamicBarWidth()
     */
    public int getBarWidth() {
        return barWidthProperty().get();
    }

    /**
     * @return the <code>barWidthPercentage</code> of the total X space should be taken to paint // bars.
     */
    public double getBarWidthPercentage() {
        return barWidthPercentageProperty().get();
    }

    /**
     * Returns the <code>dashSize</code>.
     *
     * @return the <code>dashSize</code>.
     */
    public int getDashSize() {
        return dashSizeProperty().get();
    }

    /**
     * @return returns error plotting style
     * @see ErrorDataSetRenderer#setErrorType(ErrorStyle style) for details
     */
    public ErrorStyle getErrorType() {
        return errorStyleProperty().get();
    }

    /**
     * Returns the <code>intensityFading</code>.
     *
     * @return the <code>intensityFading</code>.
     */
    public double getIntensityFading() {
        return intensityFadingProperty().get();
    }

    /**
     * Returns the <code>markerSize</code>.
     *
     * @return the <code>markerSize</code>.
     */
    public double getMarkerSize() {
        return markerSizeProperty().get();
    }

    /**
     * whether renderer should draw no, simple (point-to-point), stair-case, Bezier, ... lines
     *
     * @return LineStyle
     */
    public LineStyle getPolyLineStyle() {
        return polyLineStyleProperty().get();
    }

    /**
     * @see #rendererDataReducerProperty()
     * @return the active data set reducer algorithm
     */
    public RendererDataReducer getRendererDataReducer() {
        return rendererDataReducerProperty().get();
    }

    /**
     * Returns the <code>shiftBarOffset</code>.
     *
     * @return the <code>shiftBarOffset</code>.
     */
    public int getShiftBarOffset() {
        return shiftBarOffsetProperty().get();
    }

    public DoubleProperty intensityFadingProperty() {
        return intensityFading;
    }

    /**
     * @return true if NaN values are permitted
     */
    public boolean isallowNaNs() {
        return allowNaNsProperty().get();
    }

    /**
     * @return true if bars from the data points to the y==0 axis shall be drawn
     */
    public boolean isDrawBars() {
        return drawBarsProperty().get();
    }

    /**
     * @return true if bubbles shall be draw
     */
    public boolean isDrawBubbles() {
        return drawBubblesProperty().get();
    }

    /**
     * 
     * @return whether all or only the DataSets attached to the Renderer shall be drawn 
     */
    public boolean isDrawChartDataSets() {
        return drawChartDataSetsProperty().get();
    }

    /**
     * @return true if point reduction is on (default) else false.
     */
    public boolean isDrawMarker() {
        return drawMarkerProperty().get();
    }

    /**
     * @return true whether the width of bars drawn to the '0' shall be dynamically to the shown axis width
     */
    public boolean isDynamicBarWidth() {
        return dynamicBarWidthProperty().get();
    }

    /**
     * @return true if bars drawn to the y==0 axis shall be horizontally shifted for each DataSet
     */
    public boolean isShiftBar() {
        return shiftBarProperty().get();
    }

    public DoubleProperty markerSizeProperty() {
        return markerSize;
    }

    /**
     * Sets whether renderer should draw no, simple (point-to-point), stair-case, Bezier, ... lines
     *
     * @return property
     */
    public ObjectProperty<LineStyle> polyLineStyleProperty() {
        return polyLineStyle;
    }

    /**
     * sets the data reduction algorithm: possibly implementations are<br>
     * <ul>
     * <li>{@link DefaultDataReducer} (default)</li>
     * <li>{@link MaxDataReducer} (a simple down-sampling algorithm, returning fixed number of max. 1000 points)</li>
     * <li>{@link RamanDouglasPeukerDataReducer}</li>
     * <li>{@code DouglasPeukerDataReducer}</li>
     * <li>{@link VisvalingamMaheswariWhyattDataReducer} (being developed)</li>
     * </ul>
     *
     * @return the rendererDataReducerProperty
     */
    public ObjectProperty<RendererDataReducer> rendererDataReducerProperty() {
        return rendererDataReducer;
    }

    /**
     * @param state true if NaN values are permitted
     * @return itself (fluent design)
     */
    public R setAllowNaNs(final boolean state) {
        allowNaNsProperty().set(state);
        return getThis();
    }

    /**
     * @param barWidth the <code>barWidth</code> in case of constant width bards being drawn @see setDynamicBarWidth()
     * @return itself (fluent design)
     */
    public R setBarWidth(final int barWidth) {
        AssertUtils.gtEqThanZero("barWidth", barWidth);
        barWidthProperty().setValue(barWidth);
        return getThis();
    }

    /**
     * @param size the <code>barWidthPercentage</code> of the total X space should be taken to paint
     * @return itself (fluent design)
     */
    public R setBarWidthPercentage(final double size) {
        AssertUtils.gtEqThanZero("barWidthPercentage", size);
        barWidthPercentageProperty().setValue(size);
        return getThis();
    }

    /**
     * Sets the <code>dashSize</code> to the specified value. The dash is the horizontal line painted at the ends of the
     * vertical line. It is not painted if set to 0.
     *
     * @param dashSize the <code>dashSize</code> to set.
     * @return itself (fluent design)
     */
    public R setDashSize(final int dashSize) {
        AssertUtils.gtEqThanZero("dash size", dashSize);
        dashSizeProperty().setValue(dashSize);
        return getThis();
    }

    /**
     * @param state true if bars from the data points to the y==0 axis shall be drawn
     * @return itself (fluent design)
     */
    public R setDrawBars(final boolean state) {
        drawBarsProperty().set(state);
        return getThis();
    }

    /**
     * @param state true if bubbles shall be draw
     * @return itself (fluent design)
     */
    public R setDrawBubbles(final boolean state) {
        drawBubblesProperty().set(state);
        return getThis();
    }

    /**
     * 
     * @param state whether all (true) or only the DataSets attached to the Renderer shall be drawn (false) 
     */
    public void setDrawChartDataSets(final boolean state) {
        drawChartDataSetsProperty().set(state);
    }

    /**
     * @param state true -&gt; draws markers
     * @return itself (fluent design)
     */
    public R setDrawMarker(final boolean state) {
        drawMarkerProperty().set(state);
        return getThis();
    }

    /**
     * @param state true whether the width of bars drawn to the '0' shall be dynamically to the shown axis width
     * @return itself (fluent design)
     */
    public R setDynamicBarWidth(final boolean state) {
        dynamicBarWidthProperty().set(state);
        return getThis();
    }

    /**
     * sets the error bar/surface plotting style ErrorBarRenderer.ESTYLE_NONE: no errors are drawn
     * ErrorBarRenderer.ESTYLE_BAR: error bars are drawn (default) ErrorBarRenderer.ESTYLE_SURFACE: error surface is
     * drawn
     *
     * @param style ErrorStyle @see ErrorStyle enum
     * @return itself (fluent design)
     */
    public R setErrorType(final ErrorStyle style) {
        errorStyleProperty().set(style);
        return getThis();
    }

    /**
     * Sets the <code>intensityFading</code> to the specified value.
     *
     * @param size the <code>intensityFading</code> to set.
     * @return itself (fluent design)
     */
    public R setIntensityFading(final double size) {
        intensityFadingProperty().setValue(size);
        return getThis();
    }

    /**
     * Sets the <code>markerSize</code> to the specified value.
     *
     * @param size the <code>markerSize</code> to set.
     * @return itself (fluent design)
     */
    public R setMarkerSize(final double size) {
        AssertUtils.gtEqThanZero("marker size ", size);
        markerSizeProperty().setValue(size);
        return getThis();
    }

    /**
     * Sets whether renderer should draw no, simple (point-to-point), stair-case, Bezier, ... lines
     *
     * @param style draw no, simple (point-to-point), stair-case, Bezier, ... lines
     * @return itself (fluent design)
     */
    public R setPolyLineStyle(final LineStyle style) {
        polyLineStyleProperty().set(style);
        return getThis();
    }

    /**
     * @see #rendererDataReducerProperty()
     * @param algorithm the new data reducing algorithm to be set (null -&gt; {@link DefaultDataReducer})
     * @return itself (fluent design)
     */
    public R setRendererDataReducer(final RendererDataReducer algorithm) {
        if (algorithm == null) {
            rendererDataReducerProperty().set(new DefaultDataReducer());
        } else {
            rendererDataReducerProperty().set(algorithm);
        }
        return getThis();
    }

    /**
     * @param state true if bars drawn to the y==0 axis shall be horizontally shifted for each DataSet
     * @return itself (fluent design)
     */
    public R setShiftBar(final boolean state) {
        shiftBarProperty().set(state);
        return getThis();
    }

    /**
     * Sets the <code>shiftBarOffset</code> to the specified value.
     *
     * @param shiftBarOffset the <code>shiftBarOffset</code> to set.
     * @return itself (fluent design)
     */
    public R setshiftBarOffset(final int shiftBarOffset) {
        AssertUtils.gtEqThanZero("shiftBarOffset", shiftBarOffset);
        shiftBarOffsetProperty().setValue(shiftBarOffset);
        return getThis();
    }

    public IntegerProperty shiftBarOffsetProperty() {
        return shiftBarOffset;
    }

    /**
     * @return the shiftBar state
     */
    public BooleanProperty shiftBarProperty() {
        return shiftBar;
    }

    protected R bind(final R other) {
        errorStyleProperty().bind(other.errorStyleProperty());
        pointReductionProperty().bind(other.pointReductionProperty());
        assumeSortedDataProperty().bind(other.assumeSortedDataProperty());
        dashSizeProperty().bind(other.dashSizeProperty());
        minRequiredReductionSizeProperty().bind(other.minRequiredReductionSizeProperty());
        markerSizeProperty().bind(other.markerSizeProperty());
        drawMarkerProperty().bind(other.drawMarkerProperty());
        polyLineStyleProperty().bind(other.polyLineStyleProperty());
        drawChartDataSetsProperty().bind(other.drawChartDataSetsProperty());
        drawBarsProperty().bind(other.drawBarsProperty());
        drawBubblesProperty().bind(other.drawBubblesProperty());
        allowNaNsProperty().bind(other.allowNaNsProperty());
        shiftBarProperty().bind(other.shiftBarProperty());
        shiftBarOffsetProperty().bind(other.shiftBarOffsetProperty());
        dynamicBarWidthProperty().bind(other.dynamicBarWidthProperty());
        barWidthPercentageProperty().bind(other.barWidthPercentageProperty());
        barWidthProperty().bind(other.barWidthProperty());
        getAxes().setAll(other.getAxes());
        intensityFadingProperty().bind(other.intensityFadingProperty());

        // Bindings.bindContent(axesList(), other.axesList());

        other.getAxes().addListener((ListChangeListener<? super Axis>) change -> {
            while (change.next()) {
                getAxes().addAll(change.getAddedSubList());
                getAxes().removeAll(change.getRemoved());
            }
        });
        return getThis();
    }

    /**
     * @return the instance of this AbstractErrorDataSetRendererParameter.
     */
    @Override
    protected abstract R getThis();

    protected R unbind() {
        errorStyleProperty().unbind();
        pointReductionProperty().unbind();
        dashSizeProperty().unbind();
        minRequiredReductionSizeProperty().unbind();
        markerSizeProperty().unbind();
        drawMarkerProperty().unbind();
        polyLineStyleProperty().unbind();
        drawChartDataSetsProperty().unbind();
        drawBarsProperty().unbind();
        drawBubblesProperty().unbind();
        allowNaNsProperty().unbind();
        shiftBarProperty().unbind();
        shiftBarOffsetProperty().unbind();
        dynamicBarWidthProperty().unbind();
        barWidthPercentageProperty().unbind();
        barWidthProperty().unbind();
        intensityFadingProperty().unbind();

        return getThis();
    }
}
