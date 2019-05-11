package fly.speedmeter.grub;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class DataFromDBActivity extends AppCompatActivity {

    private TextView time, maxSpeed, averageSpeed, distance;
    DatabaseReference myRef;
    private List<Float> accelX = new ArrayList<>();
    private List<Float> accelY = new ArrayList<>();
    private Button deleteAllData;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_from_db);

        time = (TextView) findViewById(R.id.timeDB);
        maxSpeed = (TextView) findViewById(R.id.maxSpeedDB);
        averageSpeed = (TextView) findViewById(R.id.averageSpeedDB);
        distance = (TextView) findViewById(R.id.distanceDB);
        deleteAllData = (Button) findViewById(R.id.deleteAllData);
        myRef = FirebaseDatabase.getInstance().getReference();

        deleteAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromDatabase();
                Toast.makeText(DataFromDBActivity.this, "Data deleted! ", Toast.LENGTH_LONG).show();
            }
        });

//        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                DataToBD data = dataSnapshot.getValue(DataToBD.class);
//                    if(data != null) {
//                        Toast.makeText(DataFromDBActivity.this, "MaxSpeed from dv: "+ data.m_myMaxSpeed, Toast.LENGTH_LONG).show();
//                        time.setText(data.m_myTime);
//                        maxSpeed.setText(data.m_myMaxSpeed);
//                        averageSpeed.setText(data.m_myAvgSpeed);
//                        distance.setText(data.m_myDistance);
////                        accelX = data.m_myaccelArrayX;
////                        accelY = data.m_myaccelArrayY;
////                        createChart();
//                    }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });


        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = 0;
                for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                    DataToBD data = dataSnap.getValue(DataToBD.class);
                    if (data != null) {
                        time.setText(data.m_myTime);
                        maxSpeed.setText(data.m_myMaxSpeed);
                        averageSpeed.setText(data.m_myAvgSpeed);
                        distance.setText(data.m_myDistance);

                        accelX.addAll(data.m_myAccelX);
                        accelY.addAll(data.m_myAccelY);

                        count++;
                    }
                }

                if (accelX.size() == accelY.size() && accelY.size() > 0 && accelX.size() > 0 && accelX.size() <= 15)
                    createChart();
                //Toast.makeText(DataFromDBActivity.this, "Data count: " + count, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void removeDataFromDatabase() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.setValue(null);
    }

    private void createChart() {
        LineChart lineChart = (LineChart) findViewById(R.id.lineChart);

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
        dataSet.setColor(ContextCompat.getColor(this, R.color.blue_light));
        dataSet.setValueTextColor(ContextCompat.getColor(this, R.color.blue_dark));

        // Setting Data
        LineData data = new LineData(dataSet);
        lineChart.setData(data);
        //lineChart.animateX(500);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);

        //refresh
        lineChart.invalidate();
    }
}
