package com.team10.codeflow;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;


public class ProjectsMenu extends AppCompatActivity {
    //Prepare arraylists for data from files to listview
    ListView listView;
    ArrayList<String> project_titles = new ArrayList<String>();
    ArrayList<String> project_dates = new ArrayList<String>();
    ArrayList<String> project_descriptions = new ArrayList<String>();
    ProjectAdapter adapter;
    //Permission Requests
    public static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects_menu);

        //Identify listView
        listView = (ListView) findViewById(R.id.projects_listView);
        //Apply modified adapter
        adapter = new ProjectAdapter(ProjectsMenu.this, R.layout.project_row_layout);
        listView.setAdapter(adapter);

        //Populate listview
        listFiles();

        //Handle selecting a project
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Here is where we actually load the file and change activity
                String thisPath = null;
                //Look in projects folder of internal storage
                File dir = new File(getFilesDir().getAbsolutePath(), "projects");
                File[] directoryListing = dir.listFiles();
                if (directoryListing != null) {
                    int i = 0;
                    //Iterate over each found file
                    for (File child : directoryListing) {
                        //Find the file linked to the selected item
                        if (i == position) {

                            Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.team10.codeflow.fileprovider", child);
                            Intent intent = new Intent(ProjectsMenu.this, Sandbox.class);
                            intent.setAction(Intent.ACTION_SEND);
                            intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                            intent.setType("*/*");
                            startActivity(intent);
                            //Close this activity
                            ProjectsMenu.this.finish();

                            break;
                        } else {
                            i++;
                        }
                    }
                }

            }
        });
        registerForContextMenu(listView);

        Log.d("Lifecycle", "Projects onCreate() called");
    }

    //Method to essentially 'refresh' the listview
    private void listFiles() {
        //Empty arrays and adapter
        project_titles.clear();
        project_dates.clear();
        project_descriptions.clear();
        listView.setAdapter(null);

        //Re-apply adapter
        adapter = new ProjectAdapter(getApplicationContext(), R.layout.project_row_layout, this);
        listView.setAdapter(adapter);

        //Look in projects folder of internal storage
        File dir = new File(getFilesDir().getAbsolutePath(), "projects");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            //Iterate over each found file
            for (File child : directoryListing) {
                FileInputStream fi = null;
                try {
                    //Load project information object from file
                    fi = new FileInputStream(child);

                    ObjectInputStream os = new ObjectInputStream(fi);

                    ProjectInfo data = (ProjectInfo) os.readObject();

                    os.close();

                    //Append appropriate project information to arrays
                    project_titles.add(data.getTitle());
                    project_dates.add(data.getDate());
                    project_descriptions.add(data.getDesc());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //File does not exist
                    Log.d("Projects", "File does not exist2");
                } catch (IOException e) {
                    e.printStackTrace();
                    //Unable to open file
                    Log.d("Projects", "Unable to open file2");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    //Class from file not defined in program
                    Log.d("Projects", "Class from file not defined in program");
                } finally {
                    try {
                        fi.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Unable to close file
                        Log.d("Projects", "Unable to close file2");
                    } catch (NullPointerException e) {
                        //File was probably never opened
                        Log.d("Projects", "File was probably never opened2");
                    }
                }

            }

            if (project_titles.isEmpty()) {
                project_titles.add("No projects found");
                project_dates.add("");
                project_descriptions.add("");
            }

        } else {
            //When directory has no files, show no projects found
            project_titles.add("No projects found");
            project_dates.add("");
            project_descriptions.add("");
        }

        //Iterator to access all necessary arrays at once
        int i = 0;
        for (String titles : project_titles) {
            ProjectInfo dataProvider = new ProjectInfo(titles, project_dates.get(i), project_descriptions.get(i));
            adapter.add(dataProvider);
            i++;
        }
    }

    //Import a file from external storage to local
    public void importFiles(View v) {

        AlertDialog.Builder alert_import = new AlertDialog.Builder(this);
        alert_import.setTitle("Import Projects");
        alert_import.setMessage("This will import copies of project files from your downloads folder, do you wish to continue?");
        alert_import.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //Button was clicked
                Toast.makeText(getApplicationContext(), "Importing projects from downloads...", Toast.LENGTH_SHORT).show();
                //Check we have permission to continue
                verifyStoragePermissions(ProjectsMenu.this, 0);
                //Find downloads folder
                File extDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File[] extDirList = extDir.listFiles();
                if (extDirList != null) {
                    for (File child : extDirList) {
                        String childPath = child.getPath();
                        String childName = child.getName();
                        if (childPath.endsWith(".codeblock")) {
                            //New file of the file we want to copy's name in internal directory
                            File intDir = new File(getFilesDir().getAbsolutePath() + File.separator + "projects" + File.separator + childName);
                            try {
                                copyFile(child, intDir);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                listFiles();
                adapter.notifyDataSetChanged();
            }
        });
        alert_import.setNegativeButton("No", null);
        alert_import.show();
    }

    //Method to copy a file from one location to another
    public static void copyFile(File src, File dst) throws IOException
    {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try
        {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        }
        finally
        {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }

    //Check permission for read/write to external storage
    public static void verifyStoragePermissions(Activity activity, int type) {
        switch(type){
            case 0:
                //Check if read permission exists
                int permission0 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

                if (permission0 != PackageManager.PERMISSION_GRANTED) {
                    //We don't have permission, prompt user
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                break;
            case 1:
                //Check if write permission exists
                int permission1 = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (permission1 != PackageManager.PERMISSION_GRANTED) {
                    //We don't have permission, prompt user
                    ActivityCompat.requestPermissions(
                            activity,
                            PERMISSIONS_STORAGE,
                            REQUEST_EXTERNAL_STORAGE
                    );
                }
                break;
        }
    }

    private ShareActionProvider mShareActionProvider = null;

    //Create context menu for this
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        //Set title of context view to project title
        AdapterView.AdapterContextMenuInfo info;
        try {
            //Cast incoming data into type for adapterview object
            info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            //If menu can't be cast, log error
            Log.e("ProjectsContextMenu", "bad menuInfo", e);
            return;
        }
        ProjectInfo myPDP = (ProjectInfo) adapter.getItem(info.position);
        if (myPDP == null) {
            //If requested item not available, do nothing.
            return;
        }

        menu.setHeaderTitle(myPDP.getTitle());
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.projects_context_menu, menu);

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        String thisPath = null;
        String shortPath = null;
        //Look in projects folder of internal storage
        File dir = new File(getFilesDir().getAbsolutePath(), "projects");
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            int i = 0;
            //Iterate over each found file
            for (File child : directoryListing) {
                //Find the file linked to the selected item
                if (i == info.position) {
                    //Save path to variable
                    thisPath = child.getAbsolutePath();
                    shortPath = child.getName();

                    break;
                } else {
                    i++;
                }
            }
        }

        Log.d("File path", thisPath);

        final String finalPath = thisPath;
        final String finalShortPath = shortPath;

        switch (item.getItemId()) {
            case R.id.rename_id:
                //Rename file here
                AlertDialog.Builder alert_rename = new AlertDialog.Builder(this);

                final EditText title = new EditText(this);
                title.setPadding(50, 60, 50, 30);
                title.setSingleLine();
                title.setHint("Enter new project name");

                //Make the Edit Text keyboard auto-appear
                title.post(new Runnable() {
                    public void run() {
                        title.requestFocusFromTouch();                      //Request Edit Text focus
                        InputMethodManager lManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        lManager.showSoftInput(title, 0);                   //Show soft input keyboard
                    }
                });

                alert_rename.setView(title);

                alert_rename.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Button was clicked
                        if (finalPath != null) {
                            String titleString = title.getText().toString();
                            FileInputStream fi = null;
                            FileOutputStream fs = null;
                            try {
                                //Load project information object from file
                                fi = new FileInputStream(finalPath);

                                ObjectInputStream is = new ObjectInputStream(fi);

                                ProjectInfo data = (ProjectInfo) is.readObject();
                                ArrayList<Block> blocks = (ArrayList<Block>) is.readObject();

                                is.close();

                                //Change title to new
                                data.setTitle(titleString);

                                //Save modified file as old file
                                fs = new FileOutputStream(finalPath);

                                ObjectOutputStream os = new ObjectOutputStream(fs);

                                os.writeObject(data);
                                os.writeObject(blocks);

                                os.close();

                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                //File does not exist
                                Log.d("Projects", "File does not exist2");
                            } catch (IOException e) {
                                e.printStackTrace();
                                //Unable to open file
                                Log.d("Projects", "Unable to open file2");
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                                //Class from file not defined in program
                                Log.d("Projects", "Class from file not defined in program");
                            } finally {
                                try {
                                    fi.close();
                                    fs.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    //Unable to close file
                                    Log.d("Projects", "Unable to close file2");
                                } catch (NullPointerException e) {
                                    //File was probably never opened
                                    Log.d("Projects", "File was probably never opened2");
                                }
                            }
                            Toast.makeText(getApplicationContext(), "Project Renamed!", Toast.LENGTH_SHORT).show();
                            //Tell the adapter to update list
                            listFiles();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                alert_rename.setNegativeButton("Cancel", null);
                alert_rename.show();
                break;
            case R.id.delete_id:
                //Delete file
                AlertDialog.Builder alert_delete = new AlertDialog.Builder(this);
                alert_delete.setMessage("Are you sure?");
                alert_delete.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Button was clicked
                        if (finalPath != null) {
                            File thisFile = new File(finalPath);
                            thisFile.delete();
                            Toast.makeText(getApplicationContext(), "Project Deleted!", Toast.LENGTH_SHORT).show();
                            //Tell the adapter to update list
                            listFiles();
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                alert_delete.setNegativeButton("Cancel", null);
                alert_delete.show();
                break;
            case R.id.export_id:
                //Share file here
                AlertDialog.Builder alert_export = new AlertDialog.Builder(this);
                alert_export.setTitle("Export Project");
                alert_export.setMessage("This will export a copy of the project to your downloads folder, do you wish to continue?");
                alert_export.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        //Button was clicked
                        verifyStoragePermissions(ProjectsMenu.this, 1);
                        Log.d("Files", finalShortPath);
                        String extPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + finalShortPath;
                        Log.d("Files", extPath);
                        File extDir = new File(extPath);
                        if (finalPath != null) {
                            File thisFile = new File(finalPath);
                            try {
                                copyFile(thisFile, extDir);
                                Toast.makeText(getApplicationContext(), "Project Exported!", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                alert_export.setNegativeButton("No", null);
                alert_export.show();
                break;
            case R.id.share_id:

                mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

                File thisProject = new File(finalPath);

                Uri contentUri = FileProvider.getUriForFile(getApplicationContext(), "com.team10.codeflow.fileprovider", thisProject);

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "CodeFlow_Project_"+shortPath);
                sendIntent.putExtra(Intent.EXTRA_TITLE, "Psuedocode made easy!");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this project that I made with CodeFlow!");
                sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                sendIntent.setType("*/*");
                startActivity(Intent.createChooser(sendIntent, "Share this project"));

                break;
            default:
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_projects_menu, menu);
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

    /**
     * Called when the user clicks the BACK Button
     * Creates a new activity
     */
    public void startMainMenuScreen(View view) {        //this view parameter is the one that was clicked (the button)
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle", "Projects onDestroy() called");
    }
}
