package com.team10.codeflow;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class Sandbox extends AppCompatActivity {

    public ArrayList<Block> testBlockList;
    public static StableArrayAdapter adapter;
    public static DynamicListView listView;

    public static TextView dragDeleteZone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("Lifecycle", "Sandbox onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sandbox);

        //Create Toolbar
        final Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //Set title to name of project
        getSupportActionBar().setTitle("New Sandbox");
        findViewById(R.id.toolbar_wrapper_inner).setPadding(0, getStatusBarHeight(), 0, 0);

        //Hide extended toolbar for now
        final RelativeLayout altToolbar = (RelativeLayout) findViewById(R.id.my_alt_toolbar);

        dragDeleteZone = (TextView) findViewById(R.id.dragDeleteZone);

        findViewById(R.id.action_newBlock).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView plusminusblock = (TextView) findViewById(R.id.toolbar_addBlock);
                ImageView plusimg = (ImageView) findViewById(R.id.toolbar_addimage);
                ImageView minusimg = (ImageView) findViewById(R.id.toolbar_removeimage);
                if (altToolbar.getVisibility() == View.GONE) {
                    altToolbar.setVisibility(View.VISIBLE);
                    plusimg.setVisibility(View.GONE);
                    minusimg.setVisibility(View.VISIBLE);
                    plusminusblock.setText("Minimise");
                } else if (altToolbar.getVisibility() == View.VISIBLE) {
                    altToolbar.setVisibility(View.GONE);
                    plusimg.setVisibility(View.VISIBLE);
                    minusimg.setVisibility(View.GONE);
                    plusminusblock.setText("Add Block");
                }
            }
        });

        //Get buttons for adding new blocks
        Button b01 = (Button) findViewById(R.id.baseblock01);
        Button b02 = (Button) findViewById(R.id.baseblock02);
        Button b03 = (Button) findViewById(R.id.baseblock03);
        Button b04 = (Button) findViewById(R.id.baseblock04);
        Button b05 = (Button) findViewById(R.id.baseblock05);
        Button b06 = (Button) findViewById(R.id.baseblock06);
        Button b07 = (Button) findViewById(R.id.baseblock07);
        Button b08 = (Button) findViewById(R.id.baseblock08);
        //Attach touch listeners to new block buttons
        b01.setOnTouchListener(clickListen);
        b02.setOnTouchListener(clickListen);
        b03.setOnTouchListener(clickListen);
        b04.setOnTouchListener(clickListen);
        b05.setOnTouchListener(clickListen);
        b06.setOnTouchListener(clickListen);
        b07.setOnTouchListener(clickListen);
        b08.setOnTouchListener(clickListen);

        //Get block colours
        Block sampleblock1 = new Block(Block.BlockType.COMMENT, this);
        Block sampleblock2 = new Block(Block.BlockType.VARIABLE, this);
        Block sampleblock3 = new Block(Block.BlockType.INPUT, this);
        Block sampleblock4 = new Block(Block.BlockType.OUTPUT, this);
        Block sampleblock5 = new Block(Block.BlockType.WHILE_LOOP, this);
        Block sampleblock6 = new Block(Block.BlockType.FOR_LOOP, this);
        Block sampleblock7 = new Block(Block.BlockType.IF_STATEMENT, this);
        Block sampleblock8 = new Block(Block.BlockType.ELSE_STATEMENT, this);

        //Set buttons to have background colours of respective blocks
        b01.setBackgroundColor(sampleblock1.getColor());
        b02.setBackgroundColor(sampleblock2.getColor());
        b03.setBackgroundColor(sampleblock3.getColor());
        b04.setBackgroundColor(sampleblock4.getColor());
        b05.setBackgroundColor(sampleblock5.getColor());
        b06.setBackgroundColor(sampleblock6.getColor());
        b07.setBackgroundColor(sampleblock7.getColor());
        b08.setBackgroundColor(sampleblock8.getColor());
        //The above will be overwritten by block.getColor() anyway so I'm just going to leave it in
        //instead of changing it and accidentally messing it up

        //Areas to react to them being dropped in
        findViewById(R.id.mainContent).setOnDragListener(dropListener);
        findViewById(R.id.toolbar_wrapper_outer).setOnDragListener(dropListener2);

        //Get Resources
        Resources res = getResources();

        //Get the linear layout view on the sandbox screen
        RelativeLayout mainContent = (RelativeLayout) findViewById(R.id.mainContent);

        ArrayList<Block> newBlockList = new ArrayList<Block>();

        //Check whether recreating a previously destroyed instance or not
        if(savedInstanceState != null) {
            //Restore state
            testBlockList = (ArrayList<Block>) savedInstanceState.getSerializable("blockList");

            getSupportActionBar().setTitle(savedInstanceState.getString("projTitle"));

        }
        else {
            //Check if a project has been loaded
            //Get intent
            Intent myIntent = getIntent();
            //Initialise uri variable
            Uri myUri = null;
            //String myUriFile = null;
            File myUriFile = null;
            //Content uri flag
            Boolean uriFlag = false;
            if (Intent.ACTION_SEND.equals(myIntent.getAction())) {
                //If ACTION_SEND, get uri in this way - as it's in local storage
                Bundle extras = myIntent.getExtras();
                if (extras.containsKey(Intent.EXTRA_STREAM)) {
                    myUri = (Uri) extras.get(Intent.EXTRA_STREAM);
                    //Get just file name, append to local file directory
                    myUriFile = new File(getFilesDir().getAbsolutePath() + File.separator + "projects" + File.separator + myUri.getLastPathSegment());
                }
            }
            else if (Intent.ACTION_VIEW.equals(myIntent.getAction())){
                //Else if ACTION_VIEW, get uri in this way
                myUri = (Uri) myIntent.getData();
                String scheme = myUri.getScheme();
                if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
                    //Handle as content uri
                    try {
                        getContentResolver().openInputStream(myUri);
                        uriFlag = true;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    //Handle as file uri
                    myUriFile = new File(myUri.getPath());
                }
            }

            if (myUri != null) {
                Log.d("uri", myUri.toString());

                InputStream fi = null;
                try {
                    //Load project information object from file

                    if(uriFlag) {
                        fi = getContentResolver().openInputStream(myUri);
                    }
                    else {
                        fi = new FileInputStream(myUriFile);
                    }

                    ObjectInputStream is = new ObjectInputStream(fi);

                    ProjectInfo projInfo = (ProjectInfo) is.readObject();
                    newBlockList = (ArrayList<Block>) is.readObject();

                    is.close();

                    //Change title to new
                    getSupportActionBar().setTitle(projInfo.getTitle());


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
                    testBlockList = newBlockList;
                }
            }
            else {
                //Create a new Block List
                testBlockList = new ArrayList<Block>();

                // CREATING DEFAULT BLOCKS
                Block newBlock1 = new Block(this);
                newBlock1.setUserVal1("Enter a description of your program.");
                testBlockList.add(newBlock1);

                Block newBlock2 = new Block(Block.BlockType.VARIABLE, this);
                testBlockList.add(newBlock2);

                Block newBlock3 = new Block(Block.BlockType.INPUT, this);
                testBlockList.add(newBlock3);

                Block newBlock4 = new Block(Block.BlockType.OUTPUT, this);
                testBlockList.add(newBlock4);

            }


        }

        adapter = new StableArrayAdapter(this, R.layout.block_customview_layout, testBlockList);
        listView = (DynamicListView) findViewById(R.id.listview);

        listView.setBlockList(testBlockList);
        listView.setAdapter(adapter);
        listView.setDivider(null);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    }

    // A method to find height of the status bar
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    //Listener for dragging button blocks to main blockview
    long then = 0;
    View.OnTouchListener clickListen = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch(event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    then = (Long) System.currentTimeMillis();
                    v.setAlpha((float)0.6);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if(((Long) System.currentTimeMillis() - then) > 100) {

                        //Create shadow so user can see item is being dragged
                        DragShadow dragShadow = new DragShadow(v);

                        //Start drag action
                        ClipData data = ClipData.newPlainText("", "");
                        v.startDrag(data, dragShadow, v, 0);

                        //Dim the blocktype selected
                        v.setAlpha((float)0.3);

                        return true;
                    } else {
                        v.setAlpha(1);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    v.setAlpha(1);
                    break;
            }
            return false;

        }
    };

    //Listener for dropping button blocks to main blockview
    View.OnDragListener dropListener = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int dragEvent = event.getAction();
            Button dragged = (Button) event.getLocalState();

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    //Do stuff when block dropped in main blockview
                    String btext = dragged.getText().toString();
                    Toast.makeText(Sandbox.this, "Added new " + btext + " block.", Toast.LENGTH_SHORT).show();

                    Block.BlockType newBlockType = Block.BlockType.COMMENT;

                    switch (btext) {
                        case "FOR":
                            newBlockType = Block.BlockType.FOR_LOOP;
                            break;
                        case "WHILE":
                            newBlockType = Block.BlockType.WHILE_LOOP;
                            break;
                        case "COMMENT":
                            newBlockType = Block.BlockType.COMMENT;
                            break;
                        case "IF":
                            newBlockType = Block.BlockType.IF_STATEMENT;
                            break;
                        case "ELSE":
                            newBlockType = Block.BlockType.ELSE_STATEMENT;
                            break;
                        case "VAR":
                            newBlockType = Block.BlockType.VARIABLE;
                            break;
                        case "INPUT":
                            newBlockType = Block.BlockType.INPUT;
                            break;
                        case "OUTPUT":
                            newBlockType = Block.BlockType.OUTPUT;
                            break;
                        default:
                            newBlockType = Block.BlockType.COMMENT;
                            break;
                    }

                    //Get current blocklist
                    ArrayList<Block> newBlockList = DynamicListView.mBlockList;

                    if (newBlockType == Block.BlockType.WHILE_LOOP) {
                        newBlockList.add(new Block(Block.BlockType.WHILE_LOOP, Sandbox.this));
                        newBlockList.add(new Block(Block.BlockType.WHILE_LOOP_END, Sandbox.this));
                    }
                    else if (newBlockType == Block.BlockType.FOR_LOOP) {
                        newBlockList.add(new Block(Block.BlockType.FOR_LOOP, Sandbox.this));
                        newBlockList.add(new Block(Block.BlockType.FOR_LOOP_END, Sandbox.this));
                    }
                    else if (newBlockType == Block.BlockType.IF_STATEMENT) {
                        newBlockList.add(new Block(Block.BlockType.IF_STATEMENT, Sandbox.this));
                        newBlockList.add(new Block(Block.BlockType.IF_STATEMENT_END, Sandbox.this));
                    }
                    else if (newBlockType == Block.BlockType.ELSE_STATEMENT) {
                        newBlockList.add(new Block(Block.BlockType.ELSE_STATEMENT, Sandbox.this));
                        newBlockList.add(new Block(Block.BlockType.ELSE_STATEMENT_END, Sandbox.this));
                    }
                    else {
                        newBlockList.add(new Block(newBlockType, Sandbox.this));
                    }

                    listView.setAdapter(null);
                    adapter = new StableArrayAdapter(Sandbox.this, R.layout.block_customview_layout, newBlockList);

                    listView.setBlockList(newBlockList);
                    listView.setAdapter(adapter);

                    //Reset opacity of selected block to 1
                    dragged.setAlpha((float) 1);
                    break;
            }

            return true;
        }
    };
    //Listner for dropping button blocks outside of main blockview
    View.OnDragListener dropListener2 = new View.OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int dragEvent = event.getAction();
            Button dragged = (Button) event.getLocalState();

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    //Reset opacity of selected block to 1
                    dragged.setAlpha((float) 1);
                    break;
            }

            return true;
        }
    };

    //Dragshadow created when dragging a button block
    private class DragShadow extends View.DragShadowBuilder {

        ColorDrawable greyBox;

        public DragShadow(View view) {
            super(view);
            greyBox = new ColorDrawable(Color.LTGRAY);
        }

        @Override
        public void onDrawShadow(Canvas canvas) {
            greyBox.draw(canvas);
        }

        @Override
        public void onProvideShadowMetrics(Point shadowSize, Point shadowTouchPoint) {
            //Get height and width of view for shadowmetrics
            View v = getView();

            int height = (int) v.getHeight();
            int width = (int) v.getWidth();

            greyBox.setBounds(0, 0, width, height);

            shadowSize.set(width, height);

            shadowTouchPoint.set((int) width / 2, (int) height);
        }
    }

    private void saveProject(final ArrayList<Block> blockList) {

        //Get current date and time in desired format as string
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        final String currentDateandTime = sdf.format(new Date());

        //Rename file here
        AlertDialog.Builder alert_save = new AlertDialog.Builder(this);
        alert_save.setTitle("Save Project");
        //Linear layout and two editboxes for entering save file details
        LinearLayout lila = new LinearLayout(this);
        lila.setOrientation(LinearLayout.VERTICAL);
        //Padding on layout
        lila.setPadding(10, 10, 10, 10);
        final EditText title = new EditText(this);
        final EditText desc = new EditText(this);
        //padding on text boxes for visibility
        title.setPadding(50, 40, 50, 30);
        desc.setPadding(50, 60, 50, 30);
        //We don't want more than one line.
        title.setSingleLine();
        desc.setSingleLine();
        //If project already has a name (exists) default input to description to preserve title
        if (getSupportActionBar().getTitle() != "New Sandbox") {
            title.setText(getSupportActionBar().getTitle());
            desc.post(new Runnable() {
                public void run() {
                    desc.requestFocusFromTouch();                      //Request Edit Text focus
                }
            });
        }
        title.setHint("Project Title");
        desc.setHint("Project Description");
        lila.addView(title);
        lila.addView(desc);
        alert_save.setView(lila);

        alert_save.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                //Button was clicked
                String proj_title = title.getText().toString();
                String proj_date = currentDateandTime;
                String proj_desc = desc.getText().toString();
                ProjectInfo info = new ProjectInfo(proj_title, proj_date, proj_desc);

                Toast.makeText(Sandbox.this, "Project: '" + proj_title + "' saved.", Toast.LENGTH_SHORT).show();

                String proj_name = proj_title.replaceAll(" ", "_").toLowerCase();

                FileOutputStream fs = null;
                try {
                    fs = new FileOutputStream(getFilesDir().getAbsolutePath() + File.separator + "projects" + File.separator + proj_name + ".codeflow");

                    ObjectOutputStream os = new ObjectOutputStream(fs);

                    os.writeObject(info);
                    os.writeObject(blockList);

                    os.close();

                    getSupportActionBar().setTitle(proj_title);


                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    //File does not exist
                    Log.d("Projects", "File does not exist");
                } catch (IOException e) {
                    e.printStackTrace();
                    //Unable to open file
                    Log.d("Projects", "Unable to open file");

                } finally {
                    try {
                        fs.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        //Unable to close file
                        Log.d("Projects", "Unable to close file");
                    } catch (NullPointerException e) {
                        //File was probably never opened
                        Log.d("Projects", "File was probably never opened");
                    }
                }

            }
        });
        alert_save.setNegativeButton("Cancel", null);
        alert_save.show();
    }

    private ShareActionProvider mShareActionProvider = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sandbox, menu);

        // Locate MenuItem with ShareActionProvider
        MenuItem item = menu.findItem(R.id.action_share);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        ArrayList<Block> blockList = DynamicListView.mBlockList;

        switch (item.getItemId()) {
            case R.id.action_save:
                saveProject(blockList);
                break;
            case R.id.action_clear:
                getIntent().removeExtra("filePath");
                startActivity(getIntent());
                Sandbox.this.finish();
                return true;
            case R.id.action_projects:
                Intent projIntent = new Intent(this, ProjectsMenu.class);
                startActivity(projIntent);
                return true;
            case R.id.action_share:
                String shareText = "";
                for (Block b : blockList) {
                    shareText += b.stringify() + "\n";
                }

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sendIntent.setType("text/plain");

                mShareActionProvider.setShareIntent(sendIntent);

                return true;
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsMenu.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_mainmenu:
                Sandbox.this.finish();
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        //Handle saving data on things like orientation change

        savedInstanceState.putSerializable("blockList", testBlockList);
        savedInstanceState.putString("projTitle", getSupportActionBar().getTitle().toString());


        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Lifecycle","Sandbox onDestroy() called");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Lifecycle","Sandbox onPause() called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Lifecycle","Sandbox onResume() called");
    }
}