package com.example.intific.sensorprediction.tensorflow;

import android.content.res.AssetManager;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.util.ArrayList;
import java.util.List;

public class PoseIdentifierInstance {

    TensorFlowInferenceInterface inferenceInterface;
    private List<String> labels = new ArrayList<String>();

    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String OUTPUT_SCORES_NAME = "labels_softmax";// Label for the output we want to fetch

    PoseIdentifierInstance(AssetManager assetManager, String modelFilename){
        inferenceInterface = new TensorFlowInferenceInterface(assetManager, modelFilename);
    }

    public Integer poseInference(){
        Integer inference = 0;// Initially standing

        //TODO Make this more configurable/less hardcoded
        float[] floatInputBuffer = new float[12];// Array of long which will hold the sensor values
        float[] outputScores = new float[labels.size()];//Array which we will pass by reference into "fetch" - will be filled with classification values
        String[] outputScoresNames = new String[] {OUTPUT_SCORES_NAME};

        // Copy the input data into TensorFlow.
        inferenceInterface.feed(INPUT_DATA_NAME, floatInputBuffer, 1, inputSize, inputSize, 3);

        // Create output array

        // Run the inference call.
//        inferenceInterface.run(outputScoresNames, logStats);//TODO Implement the line here to get log statistics as well
        inferenceInterface.run(outputScoresNames);

        // Copy the output Tensor back into the output array.
        inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);

        return inference;
    }

}
