package org.tensorflow.lite.examples.textclassification;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import org.tensorflow.lite.examples.textclassification.client.Result;
import org.tensorflow.lite.examples.textclassification.client.TextClassificationClient;


import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;




/** The main activity to provide interactions with users. */
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "TextClassificationDemo";

    private TextClassificationClient client;

    private TextView predictions;
    private TextView InputText;
    private TextView textView;
    private EditText inputEditText;
    private Handler handler;
    private HorizontalBarChart mBarChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tfe_tc_activity_main);
        Log.v(TAG, "onCreate");

        client = new TextClassificationClient(getApplicationContext());
        handler = new Handler();
        Button classifyButton = findViewById(R.id.button);
        classifyButton.setOnClickListener(
                (View v) -> {
                    classify(inputEditText.getText().toString());
                });

        inputEditText = findViewById(R.id.input_text);
        InputText = findViewById(R.id.InputText);
        textView = findViewById(R.id.textView);
        predictions = findViewById(R.id.predictions);

    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        handler.post(
                () -> {
                    client.load();
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
        handler.post(
                () -> {
                    client.unload();
                });
    }

    /** Send input text to TextClassificationClient and get the classify messages. */
    private void classify(final String text) {
        handler.post(
                () -> {
                    // Run text classification with TF Lite.
                    List<Result> results = client.classify(text);

                    // Show classification result on screen
                    showResult(text, results);
                });
    }




    public static void barchart(BarChart barChart, ArrayList<BarEntry> arrayList, final ArrayList<String> xAxisValues) {
        barChart.setDrawBarShadow(false);
        barChart.setFitBars(true);
        barChart.setDrawValueAboveBar(true);
        barChart.setMaxVisibleValueCount(25);
        barChart.setPinchZoom(true);

        barChart.setDrawGridBackground(true);
        BarDataSet barDataSet = new BarDataSet(arrayList, "Class");
        barDataSet.setColors(new int[]{Color.parseColor("#03A9F4"), Color.parseColor("#FF9800"),
                Color.parseColor("#76FF03"),Color.parseColor("#000000"), Color.parseColor("#E91E63"), Color.parseColor("#2962FF")});
        //barDataSet.setColors(new int[]{Color.parseColor("#03A9F4"), Color.parseColor("#FF9800"), Color.parseColor("#76FF03"), Color.parseColor("#E91E63")});
        //barDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        BarData barData = new BarData(barDataSet);
        barData.setBarWidth(0.5f);
        barData.setValueTextSize(0.5f);

        barChart.setBackgroundColor(Color.TRANSPARENT); //set whatever color you prefer
        barChart.setDrawGridBackground(false);
        barChart.animateY(2000);

        //Legend l = barChart.getLegend(); // Customize the ledgends
        //l.setTextSize(10f);
        //l.setFormSize(10f);
//To set components of x axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setTextSize(18f);
        xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setEnabled(true);

        /*YAxis yLeft = barChart.getAxisLeft();
        yLeft.setAxisMaximum(100f);
        yLeft.setAxisMinimum(0f);
        yLeft.setEnabled(false);*/


        barChart.setData(barData);

    }




    /** Show classification result on the screen. */
    private void showResult(final String inputText, final List<Result> results) {
        // Run on UI thread as we'll updating our app UI
        runOnUiThread(
                () -> {
                    ArrayList<String> labels = new ArrayList<>();
                    ArrayList<String> BarLabel = new ArrayList<>();
                    ArrayList<Float> probability = new ArrayList<>();
                    ArrayList<BarEntry> barEntries = new ArrayList<>();

                    String textToShow = "Input: " + inputText + "\nOutput:\n";
                    for (int i = 0; i < results.size(); i++) {
                        Result result = results.get(i);
                        labels.add(result.getTitle());   // Extract labels
                        probability.add(result.getConfidence());  // Extract confidence
                    }


                    mBarChart = findViewById(R.id.chart);
                    mBarChart.setDrawBarShadow(false);
                    mBarChart.setDrawValueAboveBar(true);
                    mBarChart.getDescription().setEnabled(false);
                    mBarChart.setPinchZoom(false);
                    mBarChart.setDrawGridBackground(false);


                    XAxis xl = mBarChart.getXAxis();
                    xl.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xl.setDrawAxisLine(true);
                    xl.setDrawGridLines(false);
                    xl.setGranularity(1);

                    YAxis yl = mBarChart.getAxisLeft();
                    yl.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
                    yl.setDrawGridLines(false);
                    yl.setEnabled(false);
                    yl.setAxisMinimum(0f);

                    YAxis yr = mBarChart.getAxisRight();
                    yr.setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART);
                    yr.setDrawGridLines(false);
                    yr.setAxisMinimum(0f);



                    // PREPARING THE ARRAY LIST OF BAR ENTRIES

                    for(int i=0; i<probability.size(); i++)
                    {
                        barEntries.add(new BarEntry(i, probability.get(i) *100));
                    }


                    for(int i=0;i<labels.size(); i++)
                    {
                        BarLabel.add(Math.round(probability.get(i) * 1000) / 10.0 + "% " + labels.get(i));
                    }

                    barchart(mBarChart,barEntries,BarLabel);


                    textView.setText("Your input text:");
                    predictions.setText("Predictions:");
                    InputText.setText(inputText);



                    // Clear the input text.
                    inputEditText.getText().clear();

                });
    }
}