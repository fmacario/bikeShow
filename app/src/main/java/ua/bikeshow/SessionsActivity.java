package ua.bikeshow;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;


public class SessionsActivity extends AppCompatActivity {

    private ArrayList<String> listSession = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference sessionRef = database.getReference().child("infos").child(user);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sessions);

        String year = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        String month = Integer.toString(Calendar.getInstance().get(Calendar.MONTH)+1);
        String day = String.format("%02d",Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        adapter = new ArrayAdapter<>(this, R.layout.activity_listview, listSession);
        final ListView listview = (ListView) findViewById(R.id.list_sessions_history);
        listview.setAdapter(adapter);
        listSessions(year, month, day);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.graphs_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_choose:
                chooseDate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void chooseDate(){
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        final DatePicker picker = new DatePicker(this);
        dialog.setView(picker);
        dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String tmp_year= Integer.toString(picker.getYear());
                String tmp_month = Integer.toString(picker.getMonth()+1);
                String tmp_day = String.format("%02d",picker.getDayOfMonth());
                listSessions(tmp_year, tmp_month, tmp_day);
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogint, int which) {
                dialogint.cancel();
            }
        });
        dialog.create().show();
    }


    private void listSessions(final String year, final String month, final String day){
        listSession.clear();
        sessionRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String date=year+month+day;
                for(DataSnapshot singleSnap : dataSnapshot.getChildren()){
                    String entry = singleSnap.getKey();
                    String yearMonthDay = entry.substring(0,8);
                    String tmp_time = singleSnap.getKey();
                    String tmp_year=tmp_time.substring(0,4)+"-";
                    String tmp_month=tmp_time.substring(5,6)+"-";
                    String tmp_day=tmp_time.substring(7,8)+" ";
                    String tmp_hour = tmp_time.substring(8,10)+"h";
                    String tmp_min = tmp_time.substring(10,12)+"m";
                    tmp_time = tmp_year+tmp_month+tmp_day+tmp_hour + tmp_min;
                    listSession.add(tmp_time+"\n"+singleSnap.getValue().toString());
                    //String session = organizeSession(singleSnap.getValue().toString());
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private String organizeSession(String session){
        char[] sess = session.toCharArray();
        boolean flag = false;
        String tmp_sess = "";
        String vel = "";
        String bpm = "";
        String temp = "";
        int j = 1;
        for(int i = 1; i<sess.length; i++){
            if(sess[i]=='='){
                vel = session.substring(j,i);
                j=i+1;
                for(int z = j; z<sess.length;z++){
                    if(sess[z]=='='){

                    }
                }


            }
        }

        return tmp_sess;
    }
    }

