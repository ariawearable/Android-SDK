package com.deus_tech.ariasdk.calibrationBleService;


public interface CasListener{


    void onCalibrationStarted();

    void onCalibrationStepStarted(int _gestureIndex, int _gestureIteration);

    void onCalibrationStepRecording(int _gestureIndex, int _gestureIteration);

    void onCalibrationStepDone(int _gestureIndex, int _gestureIteration);

    void onCalibrationStepError(int _gestureIndex, int _gestureIteration);

    void onCalibrationFinished();


}//CasListener