package fly.speedmeter.grub.mainWithFragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import fly.speedmeter.grub.DataToBD;
import fly.speedmeter.grub.R;

public class HistoryFromBD extends Fragment {

    DatabaseReference myRef;
    private ArrayList<String> historyList = new ArrayList<String>();;
    private ArrayAdapter<String> adapter;
    private ListView list;
    private Button deleteAllData;

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
                if(historyList != null)
                    historyList.clear();

                for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
                    DataToBD data = dataSnap.getValue(DataToBD.class);
                    if (data != null) {
                        historyList.add(data.m_myDateTime);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history_from_bd, container, false);

        myRef = FirebaseDatabase.getInstance().getReference("default-user");

        list = (ListView) view.findViewById(R.id.list);
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_item, historyList);

        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GetGPSDataFragment nextFrag= new GetGPSDataFragment();

                Bundle b = new Bundle();

                /** Storing the position in the bundle object */
                b.putString("dateTime", list.getItemAtPosition(position).toString());

                nextFrag.setArguments(b);

                /** Getting FragmentManager object */
                FragmentManager fragmentManager = getFragmentManager();

                /** Starting a FragmentTransaction */
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                /** Getting the previously created fragment object from the fragment manager */
                GetGPSDataFragment prevFrag =  ( GetGPSDataFragment ) fragmentManager.findFragmentByTag("time_dialog");

                /** If the previously created fragment object still exists, then that has to be removed */
                if(prevFrag!=null)
                    fragmentTransaction.remove(prevFrag);

                nextFrag.show(fragmentTransaction, "data_dialog");
            }
        });

        deleteAllData = (Button) view.findViewById(R.id.deleteAllData);

        deleteAllData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeDataFromDatabase();
                historyList.clear();
                Toast.makeText(getContext(), "Data deleted! ", Toast.LENGTH_LONG).show();
            }
        });

        return view;
    }

    private void removeDataFromDatabase() {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        rootRef.setValue(null);
    }
}
