package it.piemonte.arpa.openoise;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogFilesReadActivity extends AppCompatActivity {

    private String path;

    TextView logFileName;
    TextView logFileText;
    ScrollView verticalScrollLogFiles;
    HorizontalScrollView horizontalScrollLogFiles;

    Button buttonLogFileTop;
    Button buttonLogFileBottom;
    Button buttonLogFileLeft;
    Button buttonLogFileRight;

    ShareActionProvider mShareActionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_files_read);

        //Find the view by its id
        logFileName = (TextView)findViewById(R.id.logFileName);
        logFileText = (TextView)findViewById(R.id.logFileText);

        //Button
        buttonLogFileTop = (Button) findViewById(R.id.ButtonLogFileTop);
        buttonLogFileBottom = (Button) findViewById(R.id.ButtonLogFileBottom);
        buttonLogFileLeft = (Button) findViewById(R.id.ButtonLogFileLeft);
        buttonLogFileRight = (Button) findViewById(R.id.ButtonLogFileRight);

        // Scroll
        verticalScrollLogFiles = (ScrollView) findViewById(R.id.VerticalScrollLogFiles);
        horizontalScrollLogFiles = (HorizontalScrollView) findViewById(R.id.HorizontalScrollLogFiles);



        Intent intent = getIntent();
        String logFileNameGet = intent.getStringExtra("LOG_FILE_NAME");

        //Set the text
        logFileName.setText(logFileNameGet.toString());

        path = Environment.getExternalStorageDirectory() + File.separator + "openoise";
        File file = new File(path,logFileNameGet);
        Log.d("nostro log linee", logFileNameGet);

        //Read text from file
        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
                Log.d("nostro log linee", line);
            }
            br.close();
            logFileText.setText(text);

        } catch (IOException e) {
            //You'll need to add proper error handling here
        }



        buttonLogFileTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalScrollLogFiles.smoothScrollTo(0, logFileText.getTop());
            }
        });

        buttonLogFileBottom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verticalScrollLogFiles.smoothScrollTo(0,logFileText.getBottom());
            }
        });

        buttonLogFileLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horizontalScrollLogFiles.fullScroll(horizontalScrollLogFiles.FOCUS_LEFT);
            }
        });

        buttonLogFileRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                horizontalScrollLogFiles.fullScroll(horizontalScrollLogFiles.FOCUS_RIGHT);
            }
        });

        //share file

//        buttonActionLogFilesShare.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//                sharingIntent.setType("text/plain");
//                String shareBody = "Here is the share content body";
//                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//                startActivity(Intent.createChooser(sharingIntent, "Share via"));
//            }
//        });

    }



//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_log_files_read, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_log_files_share) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }
//
//    private void shareLogFile() {
//        //sharing implementation here
//        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//        String shareBody = "Here is the share content body";
//        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
//        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        startActivity(Intent.createChooser(sharingIntent, "Share via"));
//
//    }
}
