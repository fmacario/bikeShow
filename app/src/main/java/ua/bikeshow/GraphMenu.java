package ua.bikeshow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class GraphMenu extends AppCompatActivity {

    private String user = FirebaseAuth.getInstance().getCurrentUser().getUid();
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mySess=database.getReference().child("values").child(user);
    DatabaseReference myValues = database.getReference().child("infos").child(user);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_menu);
    }

    public void BatGraphActivity(View view){
        String time = getIntent().getStringExtra("session");
        Intent intent = new Intent(this, SessionGraphActivity.class);
        intent.putExtra("session", time);
        startActivity(intent);
    }

    public void VelGraphActivity(View view){
        String time = getIntent().getStringExtra("session");
        Intent intent = new Intent(this, VelGraphActivity.class);
        intent.putExtra("session", time);
        startActivity(intent);
    }


    public void BatVelGraph(View view){
        String time = getIntent().getStringExtra("session");
        Intent intent = new Intent(this, BatVelGraphActivity.class);
        intent.putExtra("session", time);
        startActivity(intent);
    }

    public void DeleteSession(View view){
        String time = getIntent().getStringExtra("session");
        mySess.child(time).removeValue();
        myValues.child(time).removeValue();
        Intent intent = new Intent(this, SessionsActivity.class);
        startActivity(intent);
        finish();

    }
}
