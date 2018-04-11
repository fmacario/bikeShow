package ua.bikeshow;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class BatVelGraphActivity extends AppCompatActivity {

    private String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mySess=database.getReference().child("values").child(user);
    private LinkedHashMap<Integer, Integer> data = new LinkedHashMap<>();

    GraphView graph;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bat_vel_graph);
        graph = (GraphView) findViewById(R.id.sessionGraph);
        initialize();
    }


    private void initialize(){
        data = new LinkedHashMap<>();
        String date = getIntent().getStringExtra("session");
        DatabaseReference date1 = mySess.child(date);
        date1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot singleSnap : dataSnapshot.getChildren()){
                    String vel = singleSnap.child("vel").getValue().toString();
                    String bat1 = singleSnap.child("bat").getValue().toString();
                    Double aux1= Double.parseDouble(vel);
                    int aux2 = aux1.intValue();
                    Double aux = Double.parseDouble(bat1);
                    int bat = aux.intValue();
                    data.put(aux2, bat);

                }

                graph.removeAllSeries();
                createGraph();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(BatVelGraphActivity.this.toString(), "onCancelled", databaseError.toException());
            }
        });


    }


    private void createGraph(){
        DataPoint[] values = new DataPoint[data.size()];
        Set set = data.entrySet();
        Iterator i = set.iterator();
        int count = 0;
        while(i.hasNext()){
            Map.Entry me = (Map.Entry) i.next();
            int x = (Integer) me.getKey();
            int tmp = (Integer) me.getValue();
            int y = tmp;

            DataPoint v = new DataPoint(x, y);
            values[count] = v;
            count++;
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(values);

        graph.addSeries(series);
        // series.setSpacing(50);
        graph.getViewport().setScalable(true);
    }
}
