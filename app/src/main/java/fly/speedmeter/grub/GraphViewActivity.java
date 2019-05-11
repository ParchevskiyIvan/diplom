package fly.speedmeter.grub;

import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by Этиловый Воин on 13.05.2018.
 */

public class GraphViewActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container,
                    false);
            LinearLayout l = (LinearLayout) rootView.findViewById(R.id.label1);
            l.setOrientation(LinearLayout.VERTICAL);

            //TextView lightValue = createLabel(rootView, l, "");

            TextView accelarationValue = createLabel(rootView, l, "");

            //create line graph for accelerometer
            LineGraphView graph = new LineGraphView(rootView.getContext(),
                    100,
                    Arrays.asList("x", "y", "z"));
            l.addView(graph);
            graph.setVisibility(View.VISIBLE);

            createLabel(rootView, l, "\n\n");

            //TextView magneticFieldValue = createLabel(rootView, l, "");
            //TextView rotationVectorValue = createLabel(rootView, l, "");

            SensorManager sensorManager = (SensorManager)
                    rootView.getContext().getSystemService(SENSOR_SERVICE);

            //light sensor
            /*Sensor lightSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
            SensorEventListener light = new LightSensorEventListener(lightValue);
            sensorManager.registerListener(light, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);*/

            //accelerometer
            Sensor accelerometer =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            SensorEventListener accelaration = new AccelerometerEventListener(accelarationValue, graph);
            sensorManager.registerListener(accelaration, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            //Magnetic Field sensor
            /*Sensor magneticFieldSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            SensorEventListener magneticField = new MagneticFieldEventListener(magneticFieldValue);
            sensorManager.registerListener(magneticField, magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);

            //Rotation Vector sensor
            Sensor rotationVectorSensor =
                    sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            SensorEventListener rotationVector = new RotationVectorEventListener(rotationVectorValue);
            sensorManager.registerListener(rotationVector, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);*/


            return rootView;
        }

        private TextView createLabel(View rootView, LinearLayout l, String labelName){
            TextView tv = new TextView(rootView.getContext());
            tv.setText(labelName);
            l.addView(tv);

            return tv;

        }
    }
}
