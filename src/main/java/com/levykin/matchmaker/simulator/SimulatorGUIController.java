package com.levykin.matchmaker.simulator;

import com.levykin.matchmaker.MatchMaker;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TableView;

import java.util.ArrayList;
import java.util.List;

public class SimulatorGUIController {
    @FXML
    public BarChart<String, Number> histogram;

    @FXML
    public CategoryAxis xAxis;

    @FXML
    public NumberAxis yAxis;

    @FXML
    Spinner<Integer> spinner;

    @FXML
    Label uptimeL;

    @FXML
    Button resetB;

    @FXML
    Label matchesL;

    @FXML
    Label waitingL;

    @FXML
    TableView<MatchRow> table;

    private final List<XYChart.Data<String, Number>> chartData = new ArrayList<>(MatchMaker.MAX_RANK);

    private Runnable resetAction;

    void setResetAction(Runnable runnable) {
        this.resetAction = runnable;
    }

    @FXML
    protected void handleResetAction(ActionEvent event) {
        if (resetAction != null)
            resetAction.run();
    }

    @FXML
    private void initialize() {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        histogram.getData().add(series);

        for (int i = 0; i < MatchMaker.MAX_RANK; i++)
            chartData.add(new XYChart.Data<>(String.valueOf(i + 1), 0));

        series.getData().addAll(chartData);
        yAxis.setUpperBound(SimulatorGUI.MATCH_SIZE);
    }

    void resetState() {
        table.getItems().clear();
        matchesL.setText("0");
        uptimeL.setText("0");
        waitingL.setText("0");
        for (XYChart.Data<String, Number> data : chartData)
            data.setYValue(0);
    }

    void updateState(String uptime, String waiting, int[] ratingByUsers) {
        uptimeL.setText(uptime);
        waitingL.setText(waiting);
        for (int i = 0; i < chartData.size(); i++)
            chartData.get(i).setYValue((double) ratingByUsers[i]);
    }
}
