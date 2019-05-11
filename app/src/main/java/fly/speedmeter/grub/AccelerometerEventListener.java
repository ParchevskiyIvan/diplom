package fly.speedmeter.grub;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class AccelerometerEventListener implements SensorEventListener {

    TextView output;
    LineGraphView graph;
    float[] record = new float[3];

    private static List<Float> accelCoordsX = new ArrayList<Float>();
    private static List<Float> accelCoordsY = new ArrayList<Float>();

    public AccelerometerEventListener(TextView outputView, LineGraphView graph) {
        output = outputView;
        this.graph = graph;
    }

    public AccelerometerEventListener() {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            max(event.values);
//            output.setText(String.format("Proper acceleration on x, y, z axis:\n(%,5.2f, %,5.2f, %,5.2f) in m/s^2 "
//                            + "\nMaximum record value:\n(%,6.2f, %,6.2f, %,6.2f) in m/s^2\n\n",
//                    event.values[0], event.values[1], event.values[2], record[0], record[1], record[2]));

            if (accelCoordsX.size() != 16) {
                accelCoordsX.add(event.values[0]);
                accelCoordsY.add(event.values[1]);
            } else {
                for (int i = 0; i < accelCoordsX.size(); i++) {
                    accelCoordsX.remove(i);
                    accelCoordsY.remove(i);
                }
                accelCoordsX.add(event.values[0]);
                accelCoordsY.add(event.values[1]);
            }

            //graph.addPoint(event.values);
        }
    }

    public static List<Float> getAccelerometerDataX() {
//        for (; ; ) {
//            if (accelCoordsX.size() == 15)
//                break;
//        }
        Log.e("Accel coordsX: ", accelCoordsX.toString());
        return accelCoordsX;
    }

    public static List<Float> getAccelerometerDataY() {
//        for (; ; ) {
//            if (accelCoordsY.size() == 15)
//                break;
//        }
        Log.e("Accel coordsY: ", accelCoordsY.toString());
        return accelCoordsY;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    private void max(float[] current) {
        if (record[0] * record[0] + record[1] * record[1] + record[2] * record[2] <
                current[0] * current[0] + current[1] * current[1] + current[2] * current[2]) {
            record[0] = current[0];
            record[1] = current[1];
            record[2] = current[2];
        }
    }
}
