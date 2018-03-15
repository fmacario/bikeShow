package ua.bikeshow;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Settings extends AppCompatActivity {
    public static final String PREFS_NAME = "MyPrefsFile";
    ImageButton backToMain;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String FILENAME = "hello_file";
        String string = "hello world!";


        try {
            FileOutputStream fos = openFileOutput("temperature", Context.MODE_PRIVATE);
            fos.write(string.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int i=0;
        char c;
        String s="";

        try {
            FileInputStream fis = openFileInput("settings");
            //i = fis.read();
            while((i = fis.read())!=-1) {
                s += ""+(char)i;
            }
            fis.close();
            Log.d("TESTE", s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        initilaizeComponents();
        initializeEvents();
    }

    private void initilaizeComponents() {
        backToMain = (ImageButton)findViewById(R.id.backToMain);
        save = (Button)findViewById(R.id.save);
    }

    private void initializeEvents() {
        backToMain.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener()   {
            public void onClick(View v)  {
                try {
                    //save emergency number to db
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
