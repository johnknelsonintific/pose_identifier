package com.example.intific.sensorprediction.activity;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.intific.sensorcapture.sensorcapture.SensorCapturer;
import com.example.intific.sensorcapture.sensorlogger.SensorLogEntry;
import com.example.intific.sensorprediction.R;
import com.example.intific.sensorprediction.tensorflow.TensorFlowPoseClassifier;

import org.w3c.dom.Text;

import java.util.Timer;

public class MainActivity extends AppCompatActivity implements SensorCapturer.SensorCaptureListenerInterface {

    // Static tags
//    private static final String MODEL_FILE = "file:///android_asset/frozen_pose_model24_08_2018.pb";
//    private static final String MODEL_FILE = "file:///android_asset/pose_graph_def28_08-15_51_13.pb";
    private static final String MODEL_FILE = "file:///android_asset/posegraph_def_trans.pb";
    private static final String LABEL_FILE = "file:///android_asset/pose_labels.txt";
    private static final int INPUT_SIZE = 12;
    private static final String INPUT_NAME = "dense_1_input";
    private static final String[] OUTPUT_NAME = {"pose_0", "pose_1", "pose_2"};
//    private static final String[] OUTPUT_NAME = {"pose_0"};

    // Member variables
    private Timer PoseInferenceTimer;// Timer for appending new sensor data to logs
    private TensorFlowPoseClassifier poseClassifier;
    private SensorCapturer sensorCapturer;
    TextView standConfidence;
    TextView crouchConfidence;
    TextView proneConfidence;

    Button predictionButton;

    Boolean predicting = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get reference to UI
        standConfidence = (TextView) findViewById(R.id.stand_confidence);
        crouchConfidence = (TextView) findViewById(R.id.crouch_confidence);
        proneConfidence = (TextView) findViewById(R.id.prone_confidence);

        predictionButton = (Button) findViewById(R.id.predict_button);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        poseClassifier = new TensorFlowPoseClassifier(getAssets(), MODEL_FILE, LABEL_FILE, INPUT_SIZE, INPUT_NAME, OUTPUT_NAME);

        SensorCapturer sensorCapturer = new SensorCapturer(this, this);
        predictionButton.setOnClickListener(v -> {
            if(predicting == false){
                // Start predicting
                sensorCapturer.startCapture();
                predicting = true;

                // Set UI
                predictionButton.setText(getResources().getString(R.string.end_predictions));
            } else {
                // Stop predicting
                sensorCapturer.stopCapture();
                predicting = false;

                //clear UI
                standConfidence.setText(null);
                crouchConfidence.setText(null);
                proneConfidence.setText(null);
                predictionButton.setText(getResources().getString(R.string.begin_predictions));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSensorCapture(SensorLogEntry sensorLogEntry) {
        //TODO Run inference
        float[] confidences = poseClassifier.predictPose(sensorLogEntry);
        showConfidence(confidences);
    }

    private void showConfidence(float[] confidences){
        if(confidences == null){
            return;
        }
        float standingConfidenceFloat = confidences[0];
        float crouchingConfidenceFloat = confidences[1];
        float proneConfidenceFloat = confidences[2];

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                standConfidence.setText(Float.toString(standingConfidenceFloat));
                crouchConfidence.setText(Float.toString(crouchingConfidenceFloat));
                proneConfidence.setText(Float.toString(proneConfidenceFloat));
            }
        });
    }
}
