package de.gsi.chart.legend;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import de.gsi.chart.Chart;
import de.gsi.chart.XYChart;
import de.gsi.chart.XYChartCss;
import de.gsi.chart.axes.Axis;
import de.gsi.chart.axes.spi.DefaultNumericAxis;
import de.gsi.chart.legend.spi.DefaultLegend;
import de.gsi.chart.legend.spi.DefaultLegend.LegendItem;
import de.gsi.chart.renderer.Renderer;
import de.gsi.chart.ui.geometry.Side;
import de.gsi.chart.ui.utils.JavaFXInterceptorUtils.SelectiveJavaFxInterceptor;
import de.gsi.chart.ui.utils.TestFx;
import de.gsi.dataset.DataSet;
import de.gsi.dataset.testdata.spi.SineFunction;

/**
 * Tests {@link de.gsi.chart.legend.Legend }, {@link de.gsi.chart.legend.spi.DefaultLegend } and it's position in {@link de.gsi.chart.Chart }  
 *
 * @author rstein
 *
 */
@ExtendWith(ApplicationExtension.class)
@ExtendWith(SelectiveJavaFxInterceptor.class)
public class LegendTests {
    private static final int WIDTH = 300;
    private static final int HEIGHT = 200;
    private Renderer testRenderer = new TestRenderer();
    private DataSet testDataSet = new SineFunction("sine", 100);
    private DataSet testDataSetAlt = new SineFunction("sineAlt", 100);
    private XYChart chart;

    @Start
    public void start(Stage stage) {
        assertDoesNotThrow(() -> new DefaultLegend());

        chart = new XYChart(new DefaultNumericAxis(), new DefaultNumericAxis());
        chart.getRenderers().setAll(testRenderer);
        testRenderer.getDatasets().add(testDataSet);
        assertEquals(testDataSet, testRenderer.getDatasets().get(0));
        assertEquals(testRenderer.getDatasets(), testRenderer.getDatasetsCopy());

        stage.setScene(new Scene(chart, WIDTH, HEIGHT));
        stage.show();
    }

    @TestFx
    public void testLegendItemSetterGetter() {
        Node symbol1 = new Rectangle();
        Node symbol2 = new Circle();

        assertDoesNotThrow(() -> new LegendItem("test", symbol1));

        LegendItem legendItem = new LegendItem("test", symbol1);

        assertEquals("test", legendItem.getText());

        assertEquals(symbol1, legendItem.getSymbol());
        assertDoesNotThrow(() -> legendItem.setSymbol(symbol2));
        assertEquals(symbol2, legendItem.getSymbol());
    }

    @TestFx
    public void testLegendPositioning() {
        assertDoesNotThrow(() -> new DefaultLegend());

        final DefaultLegend legend = new DefaultLegend();

        assertDoesNotThrow(() -> chart.setLegend(null));
        assertNull(chart.getLegend());
        assertDoesNotThrow(() -> chart.setLegend(legend));
        assertEquals(legend, chart.getLegend());

        for (Side side : Side.values()) {
            assertDoesNotThrow(() -> chart.setLegendSide(side));
            assertTrue(chart.getTitleLegendPane(side).getChildren().contains(legend));

            // assert that legend is not attached in any pane
            for (Side side2 : Side.values()) {
                if (side2.equals(side)) {
                    continue;
                }
                assertFalse(chart.getTitleLegendPane(side2).getChildren().contains(legend));
            }

            chart.setLegendVisible(false);
            for (Side side2 : Side.values()) {
                assertFalse(chart.getTitleLegendPane(side2).getChildren().contains(legend));
            }

            chart.setLegendVisible(true);
            assertTrue(chart.getTitleLegendPane(side).getChildren().contains(legend));
            for (Side side2 : Side.values()) {
                if (side2.equals(side)) {
                    continue;
                }
                assertFalse(chart.getTitleLegendPane(side2).getChildren().contains(legend));
            }
        }
    }

    @TestFx
    public void testSetterGetter() {
        assertDoesNotThrow(() -> new DefaultLegend());
        final DefaultLegend legend = new DefaultLegend();

        assertEquals(legend, legend.getNode());

        assertFalse(legend.isVertical());
        legend.setVertical(true);
        assertTrue(legend.isVertical());
        legend.setVertical(false);
        assertFalse(legend.isVertical());

        final LegendItem legendItem1 = new LegendItem("test2", new Rectangle());
        final LegendItem legendItem2 = new LegendItem("test1", new Circle());

        final ObservableList<LegendItem> legendItems = FXCollections.observableArrayList(legendItem1, legendItem2);

        // add legend items
        assertTrue(legend.getItems().isEmpty());
        assertDoesNotThrow(() -> legend.setItems(legendItems));
        assertEquals(legendItems, legend.getItems());
        assertFalse(legend.getItems().isEmpty());

        // assertDoesNotThrow(() ->  legend.setItems(null));
        // assertTrue(legend.getItems().isEmpty());

        LegendItem legendItem = legend.getNewLegendItem(testRenderer, testDataSet, 0);
        assertEquals("sine", legendItem.getText());
        assertTrue(legendItem.getSymbol() instanceof Canvas);

        legend.getItems().clear();
        assertTrue(legend.getItems().isEmpty());
        legend.updateLegend(Collections.singletonList(testDataSet), Collections.singletonList(testRenderer), false);
        assertFalse(legend.getItems().isEmpty());
        assertEquals("sine", legend.getItems().get(0).getText());
        assertTrue(legend.getItems().get(0).getSymbol() instanceof Canvas);

        final List<DataSet> dataSetList = new ArrayList<>();
        dataSetList.add(testDataSet);
        dataSetList.add(testDataSet);
        legend.updateLegend(dataSetList, Collections.singletonList(testRenderer), false);
        assertEquals(1, legend.getItems().size());

        final List<Renderer> rendererList = new ArrayList<>();
        rendererList.add(testRenderer);
        rendererList.add(testRenderer);
        legend.updateLegend(Collections.singletonList(testDataSet), rendererList, false);
        assertEquals(1, legend.getItems().size());

        testDataSet.setStyle(XYChartCss.DATASET_SHOW_IN_LEGEND + "=false;");
        legend.updateLegend(Collections.singletonList(testDataSet), Collections.singletonList(testRenderer), true);
        assertEquals(0, legend.getItems().size());
        testDataSet.setStyle(XYChartCss.DATASET_SHOW_IN_LEGEND + "=true;");
        legend.updateLegend(Collections.singletonList(testDataSet), Collections.singletonList(testRenderer), true);
        assertEquals(1, legend.getItems().size());

        legend.updateLegend(Collections.singletonList(testDataSet), Collections.emptyList(), true);
        assertEquals(0, legend.getItems().size());

        testRenderer.setShowInLegend(false);
        legend.updateLegend(Collections.singletonList(testDataSet), Collections.singletonList(testRenderer), true);
        assertEquals(0, legend.getItems().size());

        testRenderer.setShowInLegend(true);
        legend.updateLegend(Collections.singletonList(testDataSet), Collections.singletonList(testRenderer), true);
        assertEquals(1, legend.getItems().size());

        legend.updateLegend(Collections.singletonList(testDataSetAlt), Collections.singletonList(testRenderer), false);
        assertEquals(2, legend.getItems().size());
    }

    private class TestRenderer implements Renderer {
        private final BooleanProperty showInLegend = new SimpleBooleanProperty(this, "showInLegend", true);
        private final ObservableList<Axis> axisList = FXCollections.observableArrayList();
        private final ObservableList<DataSet> dataSetList = FXCollections.observableArrayList();

        @Override
        public Canvas drawLegendSymbol(DataSet dataSet, int dsIndex, int width, int height) {
            final Canvas canvas = new Canvas(width, height);
            canvas.getGraphicsContext2D().setFill(Color.DARKRED);
            canvas.getGraphicsContext2D().fillRect(0, 0, width, height);
            return canvas;
        }

        @Override
        public ObservableList<Axis> getAxes() {
            return axisList;
        }

        @Override
        public ObservableList<DataSet> getDatasets() {
            return dataSetList;
        }

        @Override
        public ObservableList<DataSet> getDatasetsCopy() {
            return FXCollections.observableArrayList(dataSetList);
        }

        @Override
        public void render(GraphicsContext gc, Chart chart, int dataSetOffset, ObservableList<DataSet> datasets) {
            // not (yet) needed in this test case -- only Legend aspects are considered
            //TODO: add DataSet reference counting once 'void renderer(...)' is changed to 'int renderer(...)'
        }

        @Override
        public Renderer setShowInLegend(boolean state) {
            showInLegendProperty().set(state);
            return this;
        }

        @Override
        public boolean showInLegend() {
            return showInLegendProperty().get();
        }

        @Override
        public BooleanProperty showInLegendProperty() {
            return showInLegend;
        }
    }
}
