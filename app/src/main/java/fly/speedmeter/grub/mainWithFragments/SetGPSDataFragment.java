package fly.speedmeter.grub.mainWithFragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.melnykov.fab.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import fly.speedmeter.grub.AccelerometerEventListener;
import fly.speedmeter.grub.Data;
import fly.speedmeter.grub.DataToBD;
import fly.speedmeter.grub.GpsServices;
import fly.speedmeter.grub.R;
import fly.speedmeter.grub.Settings;

import static android.content.Context.SENSOR_SERVICE;

public class SetGPSDataFragment extends Fragment implements LocationListener, GpsStatus.Listener, View.OnClickListener{

    private SharedPreferences sharedPreferences;
    private LocationManager mLocationManager;
    private static Data data;

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };

    private static final int INITIAL_REQUEST = 1337;
    private static final int LOCATION_REQUEST = INITIAL_REQUEST + 3;

    private FloatingActionButton fab;
    private FloatingActionButton refresh;
    private ProgressBar progressBarCircularIndeterminate;
    private TextView satellite;
    private TextView status;
    private TextView currentSpeed;
    private TextView maxSpeed;
    private TextView averageSpeed;
    private TextView distance;
    private TextView accelTime;
    private Chronometer time;
    private Data.onGpsServiceUpdate onGpsServiceUpdate;
    private int temp_seconds;

    private boolean firstfix;

    DatabaseReference myRef;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SensorManager sensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);

        Sensor accelerometer =
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        SensorEventListener accelaration = new AccelerometerEventListener();
        sensorManager.registerListener(accelaration, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        myRef = FirebaseDatabase.getInstance().getReference("default-user");

        data = new Data(onGpsServiceUpdate);

        temp_seconds = 0;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        onGpsServiceUpdate = new Data.onGpsServiceUpdate() {
            @Override
            public void update() {
                double maxSpeedTemp = data.getMaxSpeed();
                double distanceTemp = data.getDistance();
                double averageTemp;
                if (sharedPreferences.getBoolean("auto_average", false)) {
                    averageTemp = data.getAverageSpeedMotion();
                } else {
                    averageTemp = data.getAverageSpeed();
                }

                String speedUnits;
                String distanceUnits;
                if (sharedPreferences.getBoolean("miles_per_hour", false)) {
                    maxSpeedTemp *= 0.62137119;
                    distanceTemp = distanceTemp / 1000.0 * 0.62137119;
                    averageTemp *= 0.62137119;
                    speedUnits = "mph";
                    distanceUnits = "mil";
                } else {
                    speedUnits = "mph";
                    if (distanceTemp <= 1000.0) {
                        distanceUnits = "m";
                    } else {
                        distanceTemp /= 1000.0;
                        distanceUnits = "mil";
                    }
                }

                SpannableString s = new SpannableString(String.format("%.0f", maxSpeedTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                maxSpeed.setText(s);

                s = new SpannableString(String.format("%.0f", averageTemp) + speedUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 4, s.length(), 0);
                averageSpeed.setText(s);

                s = new SpannableString(String.format("%.3f", distanceTemp) + distanceUnits);
                s.setSpan(new RelativeSizeSpan(0.5f), s.length() - 2, s.length(), 0);
                distance.setText(s);
            }
        };

        mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_set_gps_data, container, false);

        accelTime = (TextView) view.findViewById(R.id.accelTime);
        accelTime.setText("");

        fab = (FloatingActionButton) view.findViewById(R.id.fab);
        fab.setVisibility(View.INVISIBLE);
        fab.setOnClickListener(this);

        refresh = (FloatingActionButton) view.findViewById(R.id.refresh);
        refresh.setVisibility(View.INVISIBLE);
        refresh.setOnClickListener(this);

        satellite = (TextView) view.findViewById(R.id.satellite);
        status = (TextView) view.findViewById(R.id.status);
        //accuracy = (TextView) findViewById(R.id.accuracy);
        maxSpeed = (TextView) view.findViewById(R.id.maxSpeed);
        averageSpeed = (TextView) view.findViewById(R.id.averageSpeedInscription);
        distance = (TextView) view.findViewById(R.id.distance);
        time = (Chronometer) view.findViewById(R.id.time);
        time.setText(""); //starttime
        time.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            boolean isPair = true;

            @Override
            public void onChronometerTick(Chronometer chrono) {
                long time;
                if (data.isRunning()) {
                    time = SystemClock.elapsedRealtime() - chrono.getBase();
                    data.setTime(time);
                } else {
                    time = data.getTime();
                }

                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000; ///second activity
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                chrono.setText(hh + ":" + mm + ":" + ss);

                if (data.isRunning()) {
                    chrono.setText(hh + ":" + mm + ":" + ss);

                    if (s % 5 == 0) {//отправка данных в бд
                        if (s - temp_seconds > 0) {
                            loadToBD();
                            Toast.makeText(getContext(), "Data loaded in " + s, Toast.LENGTH_LONG).show();
                        }
                        temp_seconds = s;
                    }
                } else {
                    if (isPair) {
                        isPair = false;
                        chrono.setText(hh + ":" + mm + ":" + ss);
                    } else {
                        isPair = true;
                        chrono.setText("");
                    }
                }

            }
        });
        currentSpeed = (TextView) view.findViewById(R.id.currentSpeed);
        progressBarCircularIndeterminate = (ProgressBar) view.findViewById(R.id.progressBarCircularIndeterminate);

        return view;
    }

    private boolean canAccessLocation() {
        return (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == getActivity().checkSelfPermission(perm));
    }

    public void onFabClick(View v) {
        if (!data.isRunning()) {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_pause));
            data.setRunning(true);
            time.setBase(SystemClock.elapsedRealtime() - data.getTime());
            time.start();
            data.setFirstTime(true);
            getActivity().startService(new Intent(getActivity(), GpsServices.class));
            refresh.setVisibility(View.INVISIBLE);
        } else {
            fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
            data.setRunning(false);
            status.setText("");
            getActivity().stopService(new Intent(getActivity(), GpsServices.class));
            refresh.setVisibility(View.VISIBLE);
        }
    }

    public void onRefreshClick(View v) {
        resetData();
        getActivity().stopService(new Intent(getActivity(), GpsServices.class));
    }

    public void loadToBD() {

        String id = myRef.push().getKey();

        List<Float> accelX = AccelerometerEventListener.getAccelerometerDataX();
        List<Float> accelY = AccelerometerEventListener.getAccelerometerDataY();

        if (accelX.size() == 0)
            Toast.makeText(getContext(), "NULL List!", Toast.LENGTH_LONG).show();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy_HH:mm:ss");
        String currentDateAndTime = sdf.format(new Date());

        DataToBD data = new DataToBD(currentDateAndTime, time.getText().toString(), maxSpeed.getText().toString(), averageSpeed.getText().toString(), distance.getText().toString(), accelX, accelY);

        assert id != null;
        myRef.child(id).setValue(data);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!canAccessLocation()) {
            requestPermissions(INITIAL_PERMS, INITIAL_REQUEST);
        } else {
            firstfix = true;
            if (!data.isRunning()) {
                Gson gson = new Gson();
                String json = sharedPreferences.getString("data", "");
                data = gson.fromJson(json, Data.class);
            }
            if (data == null) {
                data = new Data(onGpsServiceUpdate);
            } else {
                data.setOnGpsServiceUpdate(onGpsServiceUpdate);
            }

            if (mLocationManager.getAllProviders().indexOf(LocationManager.GPS_PROVIDER) >= 0) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
            } else {
                Log.w("MainActivity", "No GPS location provider found. GPS data display will not be available.");
            }

            if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                showGpsDisabledDialog();
            }

            mLocationManager.addGpsStatusListener(this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mLocationManager.removeUpdates(this);
        mLocationManager.removeGpsStatusListener(this);
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(data);
        prefsEditor.putString("data", json);
        prefsEditor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().stopService(new Intent(getActivity(), GpsServices.class));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(getContext(), Settings.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location.hasAccuracy()) {
            SpannableString s = new SpannableString(String.format("%.0f", location.getAccuracy()) + "m");
            s.setSpan(new RelativeSizeSpan(0.75f), s.length() - 1, s.length(), 0);
            //accuracy.setText(s);

            if (firstfix) {
                status.setText("");
                fab.setVisibility(View.VISIBLE);
                if (!data.isRunning() && !maxSpeed.getText().equals("")) {
                    refresh.setVisibility(View.VISIBLE);
                }
                firstfix = false;
            }
        } else {
            firstfix = true;
        }

        if (location.hasSpeed()) {
            progressBarCircularIndeterminate.setVisibility(View.GONE);
            String speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6) + "km/h";

            if (sharedPreferences.getBoolean("miles_per_hour", false)) { // Convert to MPH
                speed = String.format(Locale.ENGLISH, "%.0f", location.getSpeed() * 3.6 * 0.62137119) + "mph";
            }
            SpannableString s = new SpannableString(speed);
            s.setSpan(new RelativeSizeSpan(0.25f), s.length() - 4, s.length(), 0);
            currentSpeed.setText(s);
        }

    }

    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                GpsStatus gpsStatus = mLocationManager.getGpsStatus(null);
                int satsInView = 0;
                int satsUsed = 0;
                Iterable<GpsSatellite> sats = gpsStatus.getSatellites();
                for (GpsSatellite sat : sats) {
                    satsInView++;
                    if (sat.usedInFix()) {
                        satsUsed++;
                    }
                }
                satellite.setText(String.valueOf(satsUsed) + "/" + String.valueOf(satsInView));
                if (satsUsed == 0) {
                    fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
                    data.setRunning(false);
                    status.setText("");
                    getActivity().stopService(new Intent(getActivity(), GpsServices.class));
                    fab.setVisibility(View.INVISIBLE);
                    refresh.setVisibility(View.INVISIBLE);
                    //accuracy.setText("");
                    status.setText(getResources().getString(R.string.waiting_for_fix));
                    firstfix = true;
                }
                break;

            case GpsStatus.GPS_EVENT_STOPPED:
                if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    showGpsDisabledDialog();
                }
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                break;
        }
    }

    public void showGpsDisabledDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(getResources().getString(R.string.please_enable_gps))
                .setTitle(getResources().getString(R.string.gps_disabled));

        builder.setPositiveButton(R.string.accept_gps_on, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked
                startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void resetData() {
        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_play));
        refresh.setVisibility(View.INVISIBLE);
        time.stop();
        maxSpeed.setText("");
        averageSpeed.setText("");
        distance.setText("");
        time.setText("00:00:00");
        data = new Data(onGpsServiceUpdate);
    }

    public static Data getData() {
        return data;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.fab:
                onFabClick(v);
                break;
            case  R.id.refresh:
                onRefreshClick(v);
                break;
        }
    }
}
