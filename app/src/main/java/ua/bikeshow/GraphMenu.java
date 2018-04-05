package ua.bikeshow;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class GraphMenu extends AppCompatActivity {

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

    /** Called when the user clicks the Steps button */
    public void VelGraphActivity(View view){
        String time = getIntent().getStringExtra("session");
        Intent intent = new Intent(this, VelGraphActivity.class);
        intent.putExtra("session", time);
        startActivity(intent);
    }

    /** Called when the user clicks the Calories button */
    public void BatVelGraph(View view){
        String time = getIntent().getStringExtra("session");
        Intent intent = new Intent(this, BatVelGraphActivity.class);
        intent.putExtra("session", time);
        startActivity(intent);
    }
}
