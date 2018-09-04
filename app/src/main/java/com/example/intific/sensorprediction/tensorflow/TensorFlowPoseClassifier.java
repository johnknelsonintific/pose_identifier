package com.example.intific.sensorprediction.tensorflow;

import android.content.res.AssetManager;
import android.util.Log;

import com.example.intific.sensorcapture.sensorlogger.SensorLogEntry;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class TensorFlowPoseClassifier {
    // Static tags
    private static final String TAG = "TensorFlowPoseClassifier";

    // Config values
    private String modelFilename;
    private String labelFilename;
    private int inputLength;
    private String inputName;
    private String[] outputNames;

    // TensorFlow inference interface
    TensorFlowInferenceInterface inferenceInterface;
    private List<String> labels = new ArrayList<String>();

    public TensorFlowPoseClassifier(AssetManager assetManager,
                                    String modelFilename,
                                    String labelFilename,
                                    int inputSize,
                                    String inputName,
                                    String[] outputNames
                                    ){
        this.modelFilename = modelFilename;
        this.labelFilename = labelFilename;
        this.inputLength = inputSize;
        this.inputName = inputName;
        this.outputNames = outputNames;

        inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);

        String actualFilename = labelFilename.split("file:///android_asset/")[1];
        Log.i(TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(assetManager.open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!" , e);
        }
    }

    public float[] predictPose(SensorLogEntry sensorLogEntry){
        if(!isSensorLogValid(sensorLogEntry)){
            return null;
        }

        Integer inference = 0;// Initially standing

        //TODO Make this more configurable/less hardcoded
        float[] floatInputBuffer = new float[inputLength];// Array of long which will hold the sensor values
        floatInputBuffer[0] = sensorLogEntry.linearAccelerometerX;
        floatInputBuffer[1] = sensorLogEntry.linearAccelerometerY;
        floatInputBuffer[2] = sensorLogEntry.linearAccelerometerZ;
        floatInputBuffer[3] = sensorLogEntry.accelerometerX;
        floatInputBuffer[4] = sensorLogEntry.accelerometerY;
        floatInputBuffer[5] = sensorLogEntry.accelerometerZ;
        floatInputBuffer[6] = sensorLogEntry.gyroscopeX;
        floatInputBuffer[7] = sensorLogEntry.gyroscopeY;
        floatInputBuffer[8] = sensorLogEntry.gyroscopeZ;
        floatInputBuffer[9] = sensorLogEntry.gameRotationX;
        floatInputBuffer[10] = sensorLogEntry.gameRotationY;
        floatInputBuffer[11] = sensorLogEntry.gameRotationZ;

        // Create output array
        float[] outputScores = new float[labels.size()];//Array which we will pass by reference into "fetch" - will be filled with classification values

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(inputName, floatInputBuffer,  1, inputLength);
//        inferenceInterface.feed(inputName, floatInputBuffer, inputLength, 1);
//        inferenceInterface.feed(inputName, floatInputBuffer, inputLength);

        // Run the inference call.
//        inferenceInterface.run(outputNames, true);//TODO Implement the line here to get log statistics as well
        String[] output = {"dense_2/Softmax"};
        inferenceInterface.run(output);
//        inferenceInterface.run(outputNames);

        // Copy the output Tensor back into the output array.
        // Probably will get scores as 1 vs. all
        inferenceInterface.fetch(output[0], outputScores);

        return outputScores;
    }

    private Boolean isSensorLogValid(SensorLogEntry sensorLogEntry) {
        for (Field f : sensorLogEntry.getClass().getDeclaredFields()) {
            try {
                if (f.get(sensorLogEntry) == null)
                    return false;
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

}
