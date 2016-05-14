package com.team10.codeflow;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.File;


public class MainMenu extends AppCompatActivity {

    //Passing a message over an intent to a new activity
    public final static String EXTRA_MESSAGE = "com.team10.codeflow.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //**Create projects directory if it does not exist**
        File mydir = new File(getFilesDir() + File.separator + "projects");
        if (!mydir.exists()) {
            try {
                mydir.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("Lifecycle", "MainMenu onCreate() called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "MainMenu onDestroy() called");
    }

    /**
     * Called when the user clicks the Settings Button
     * Creates a new activity
     */
    public void startSettingsScreen(View view) {        //this view parameter is the one that was clicked (the button)
        Intent intent = new Intent(this, SettingsMenu.class);   //SettingsMenu is the new activity

        String message = "You came from the Main Menu screen.";
        intent.putExtra(EXTRA_MESSAGE, message);    //Intents can carry key value pairs called 'extras'

        startActivity(intent);
    }

    /**
     * Called when the user clicks the Projects Button
     * Creates a new activity
     */
    public void startProjectsScreen(View view) {        //this view parameter is the one that was clicked (the button)
        Intent intent = new Intent(this, ProjectsMenu.class);   //ProjectsMenu is the new activity

        startActivity(intent);
    }

    /**
     * Called when the user clicks the Sandbox Button
     * Creates a new activity
     */
    public void startSandboxScreen(View view) {        //this view parameter is the one that was clicked (the button)
        Intent intent = new Intent(this, Sandbox.class);   //Sandbox is the new activity

        startActivity(intent);

    }
}
