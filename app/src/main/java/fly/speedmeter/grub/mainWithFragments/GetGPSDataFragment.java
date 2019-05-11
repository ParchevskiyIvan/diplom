package fly.speedmeter.grub.mainWithFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.EntryXComparator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fly.speedmeter.grub.DataToBD;
import fly.speedmeter.grub.R;

public class GetGPSDataFragment extends DialogFragment {

    private TextView time, maxSpeed, averageSpeed, distance;
    DatabaseReference myRef;
    private List<Float> accelX = new ArrayList<>();
    private List<Float> accelY = new ArrayList<>();
    private LineChart lineChart;
    private Bundle bundle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String dateTime = bundle.getString("dateTime");
                for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                    DataToBD data = dataSnap.getValue(DataToBD.class);
                    if (data != null && dateTime.equals(data.m_myDateTime)) {
                        time.setText(data.m_myTime);
                        maxSpeed.setText(data.m_myMaxSpeed);
                        averageSpeed.setText(data.m_myAvgSpeed);
                        distance.setText(data.m_myDistance);

                        accelX.addAll(data.m_myAccelX);
                        accelY.addAll(data.m_myAccelY);
                    }
                }

                //if (accelX.size() == accelY.size() && accelY.size() > 0 && accelX.size() > 0 && accelX.size() <= 15)
                if (accelX.size() > 0)
                    createChart();
                //Toast.makeText(DataFromDBActivity.this, "Data count: " + count, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_get_gps_data, container, false);

        bundle = getArguments();

        time = (TextView) view.findViewById(R.id.timeDB);
        maxSpeed = (TextView) view.findViewById(R.id.maxSpeedDB);
        averageSpeed = (TextView) view.findViewById(R.id.averageSpeedDB);
        distance = (TextView) view.findViewById(R.id.distanceDB);
        lineChart = (LineChart) view.findViewById(R.id.lineChart);
        myRef = FirebaseDatabase.getInstance().getReference("default-user");

        return view;
    }


    private void createChart() {
        ArrayList<Entry> entries = new ArrayList<>();

        if (accelX != null && accelY != null) {
            for (int i = 0; i < accelX.size(); i++) {
                Log.e("Set accel " + i + " : ", accelX.get(i).toString() + " " + accelY.get(i).toString());
                Entry entry = new Entry(accelX.get(i), accelY.get(i));
                entries.add(entry);
            }
        }
        Collections.sort(entries, new EntryXComparator());
        LineDataSet dataSet = new LineDataSet(entries, "Customized values");
        dataSet.setColor(ContextCompat.getColor(getContext(), R.color.blue_light));
        dataSet.setValueTextColor(ContextCompat.getColor(getContext(), R.color.blue_dark));

        // Setting Data
        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        //lineChart.animateX(500);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.getAxisRight().setEnabled(false);

        //refresh
        lineChart.invalidate();
    }

}
