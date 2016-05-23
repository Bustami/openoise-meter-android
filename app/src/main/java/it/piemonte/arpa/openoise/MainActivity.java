package it.piemonte.arpa.openoise;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;



public class MainActivity extends AppCompatActivity {



    private LinearLayout LayoutLMax;
    private LinearLayout LayoutLAeqTimeDisplay;
    private LinearLayout LayoutLMin;
    private LinearLayout LayoutLAeqRunning;
    private TextView LAeqTimeDisplayLabel;
    private TextView LMax;
    private TextView LAeqTimeDisplay;
    private TextView LMin;
    private TextView level;
    private TextView levelLabel;
    private TextView LAeqRunning;
    private TextView startingTimeRunning;
    private TextView durationTimeRunning;
    private Button buttonRunning;
    private Button buttonLog;
    private TextView startingTimeLog;
    private TextView durationTimeLog;
    private PlotFFT plotFFT;
    private PlotSLM plotSLM;
    private PlotSLMHistory plotSLMHistory;
    private PlotThirdOctave plotThirdOctave;

    private String startingTimeLogText;
    private String durationTimeLogText;

    private FileOutputStream fos;
    private FileOutputStream fosC;

    private AudioRecord recorder;
//    // verifica gain
//    private AutomaticGainControl AGC;
//    private boolean  agcEnable0,agcEnable1,agcEnable2,agcEnable3,agcEnable4,agcEnable5,agcEnable6;


    //16 bit per campione in mono
    private final static int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private final static int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    private final static int RECORDER_SAMPLERATE = 44100;
    private final static int BYTES_PER_ELEMENT = 2;
    private final static int BLOCK_SIZE = AudioRecord.getMinBufferSize(
            RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)
            / BYTES_PER_ELEMENT;
    private final static int BLOCK_SIZE_FFT = 1764;
    private final static int NUMBER_OF_FFT_PER_SECOND = RECORDER_SAMPLERATE
            / BLOCK_SIZE_FFT;
    private final static double FREQRESOLUTION = ((double) RECORDER_SAMPLERATE)
            / BLOCK_SIZE_FFT;


    private Thread recordingThread = null;
    private boolean isRecording = false;

    private DoubleFFT_1D fft = null;

    private double filter = 0;

    private double[] weightedA = new double[BLOCK_SIZE_FFT];
    private double actualFreq;
    private float gain;

    // check for level
    String levelToShow;

    // Running Leq
    double linearFftAGlobalRunning = 0;
    private long fftCount = 0;
    private double dbFftAGlobalRunning;

    // variabili finali per display
    private double dbFftAGlobalMax;
    private double dbFftAGlobalMin;
    private double dbATimeDisplay;

    // SLM min e max
    double dbFftAGlobalMinTemp = 0;
    double dbFftAGlobalMaxTemp = 0;
    int dbFftAGlobalMinFirst = 0;
    int dbFftAGlobalMaxFirst = 0;



    private Date dateLogStart;

    // Terzi d'ottava
    private float [] THIRD_OCTAVE = {16, 20, 25, 31.5f, 40, 50, 63, 80, 100, 125, 160, 200, 250, 315, 400, 500,
            630, 800, 1000, 1250, 1600, 2000, 2500, 3150, 4000, 5000, 6300, 8000, 10000, 12500, 16000, 20000};
    String [] THIRD_OCTAVE_LABEL = {"16", "20", "25", "31.5", "40", "50", "63", "80", "100", "125", "160", "200", "250", "315", "400", "500",
                "630", "800", "1000", "1250", "1600", "2000", "2500", "3150", "4000", "5000", "6300", "8000", "10000", "12500", "16000", "20000"};

    float[] dbBandMax = new float[THIRD_OCTAVE.length];
    float[] dbBandMin = new float[THIRD_OCTAVE.length];
    int kkk = 0; // controllo per bontà leq bande: solo se kkk > 10 misurano bene

    private int timeLog;
    private String timeLogStringMinSec;
    private int timeDisplay;



    private void precalculateWeightedA() {
        for (int i = 0; i < BLOCK_SIZE_FFT; i++) {
            double actualFreq = FREQRESOLUTION * i;
            double actualFreqSQ = actualFreq * actualFreq;
            double actualFreqFour = actualFreqSQ * actualFreqSQ;
            double actualFreqEight = actualFreqFour * actualFreqFour;

            double t1 = 20.598997 * 20.598997 + actualFreqSQ;
            t1 = t1 * t1;
            double t2 = 107.65265 * 107.65265 + actualFreqSQ;
            double t3 = 737.86223 * 737.86223 + actualFreqSQ;
            double t4 = 12194.217 * 12194.217 + actualFreqSQ;
            t4 = t4 * t4;

            double weightFormula = (3.5041384e16 * actualFreqEight)
                    / (t1 * t2 * t3 * t4);

            weightedA[i] = weightFormula;
        }
    }




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

/*
        if (this.getResources().getConfiguration().orientation == 1){
            setContentView(R.layout.activity_main);
        } else {
            setContentView(R.layout.activity_main_landscape);
        }
*/
        setContentView(R.layout.activity_main);
        // fisso orientation portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        plotSLMHistory = (PlotSLMHistory) findViewById(R.id.PlotSLMHistory);
        plotSLM = (PlotSLM) findViewById(R.id.PlotSLM);
        plotFFT = (PlotFFT) findViewById(R.id.PlotFFT);
        plotThirdOctave = (PlotThirdOctave) findViewById(R.id.PlotThirdOctave);
        plotSLMHistory.setVisibility(View.VISIBLE);
        plotSLM.setVisibility(View.GONE);
        plotFFT.setVisibility(View.GONE);
        plotThirdOctave.setVisibility(View.GONE);

        LayoutLMax = (LinearLayout) findViewById(R.id.LayoutLMax);
        LayoutLAeqTimeDisplay = (LinearLayout) findViewById(R.id.LayoutLAeqTimeDisplay);
        LayoutLMin = (LinearLayout) findViewById(R.id.LayoutLMin);
        LayoutLAeqRunning = (LinearLayout) findViewById(R.id.LayoutLAeqRunning);
        LAeqTimeDisplayLabel = (TextView) findViewById(R.id.LAeqTimeDisplay_label);
        LMax = (TextView) findViewById(R.id.LMax);
        LAeqTimeDisplay = (TextView) findViewById(R.id.LAeqTimeDisplay);
        LMin = (TextView) findViewById(R.id.LMin);
        level = (TextView) findViewById(R.id.level);
        levelLabel = (TextView) findViewById(R.id.level_label);
        LAeqRunning = (TextView) findViewById(R.id.LAeqRunning);
        startingTimeRunning = (TextView) findViewById(R.id.StartingTimeRunning);
        durationTimeRunning = (TextView) findViewById(R.id.DurationTimeRunning);
        buttonRunning = (Button) findViewById(R.id.ButtonRunning);
        buttonLog = (Button) findViewById(R.id.ButtonLog);
        startingTimeLog = (TextView) findViewById(R.id.StartingTimeLog);
        durationTimeLog = (TextView) findViewById(R.id.DurationTimeLog);


        DateFormat df = new SimpleDateFormat("EEE yyyy/MM/dd HH:mm:ss");
        startingTimeRunning.setText(String.format(df.format(new Date())));

        startingTimeLogText =  getApplicationContext().getResources().getString(R.string.StartingTimeLogText);
        durationTimeLogText =  getApplicationContext().getResources().getString(R.string.DurationTimeLogText);

        //startingTimeLog.setText("You are not logging");
        startingTimeLog.setText(startingTimeLogText);
        durationTimeLog.setText("");

        buttonLog.setText("START LOG");

        // Simulazione pulsante per i livelli
        LayoutLMin.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        LayoutLMin.setBackgroundColor(getResources().getColor(R.color.background_border));
                        levelToShow = "dbFftAGlobalMin";
                        levelLabel.setText("LAMin");
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        LayoutLMin.setBackgroundColor(getResources().getColor(R.color.background));
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        LayoutLMax.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        LayoutLMax.setBackgroundColor(getResources().getColor(R.color.background_border));
                        levelToShow = "dbFftAGlobalMax";
                        levelLabel.setText("LAMax");
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        LayoutLMax.setBackgroundColor(getResources().getColor(R.color.background));
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        LayoutLAeqTimeDisplay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        LayoutLAeqTimeDisplay.setBackgroundColor(getResources().getColor(R.color.background_border));
                        levelToShow = "dbATimeDisplay";
                        level.setText(String.format("%.1f", dbATimeDisplay));
                        levelLabel.setText("LAeq (" + timeDisplay + " s)");
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        LayoutLAeqTimeDisplay.setBackgroundColor(getResources().getColor(R.color.background));
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });

        LayoutLAeqRunning.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // PRESSED
                        LayoutLAeqRunning.setBackgroundColor(getResources().getColor(R.color.background_border));
                        levelToShow = "dbFftAGlobalRunning";
                        levelLabel.setText("LAeq(t)");
                        return true; // if you want to handle the touch event
                    case MotionEvent.ACTION_UP:
                        // RELEASED
                        LayoutLAeqRunning.setBackgroundColor(getResources().getColor(R.color.background));
                        return true; // if you want to handle the touch event
                }
                return false;
            }
        });
        /////// fine simulazione pulsante per i livelli

        buttonRunning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fftCount = 0;
                linearFftAGlobalRunning = 0;
                dbFftAGlobalMin = 0;
                dbFftAGlobalMax = 0;
                dbFftAGlobalMinFirst = 0;
                dbFftAGlobalMaxFirst = 0;
                for (int i = 0; i < dbBandMin.length; i++){dbBandMin[i] = 0f;dbBandMax[i] = 0f;}
                kkk = 0;

                DateFormat df = new SimpleDateFormat("EEE yyyy/MM/dd HH:mm:ss");
                startingTimeRunning.setText(String.format(df.format(new Date())));
            }
        });

        buttonLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonLog.getText().toString().equals("START LOG")) {
                    startRecordingLogFile();
                    //startRecordingLogParametersFile();

                    DateFormat df = new SimpleDateFormat("EEE yyyy/MM/dd HH:mm:ss");
                    dateLogStart =  new Date();
                    startingTimeLog.setText(String.format(df.format(dateLogStart)));
                    buttonLog.setText("STOP LOG");

                } else {
                    stopRecordingLogFile();
                    //stopRecordingLogParametersFile();
                    buttonLog.setText("START LOG");
                    //startingTimeLog.setText("You are not logging");
                    startingTimeLog.setText(startingTimeLogText);
                    //durationTimeLog.setText("Logging interval: " + timeLogStringMinSec);
                    durationTimeLog.setText(durationTimeLogText + " " + timeLogStringMinSec);
                }
            }
        });

        plotSLMHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plotSLMHistory.setVisibility(View.GONE);
                plotSLM.setVisibility(View.VISIBLE);
            }
        });

        plotSLM.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plotSLM.setVisibility(View.GONE);
                plotThirdOctave.setVisibility(View.VISIBLE);
            }
        });

        plotThirdOctave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plotThirdOctave.setVisibility(View.GONE);
                plotFFT.setVisibility(View.VISIBLE);
            }
        });

        plotFFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                plotFFT.setVisibility(View.GONE);
                plotSLMHistory.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.action_info:
                //openInfo();
                Intent intentInfoActivity = new Intent(this, InfoActivity.class);
                startActivity(intentInfoActivity);
                return true;

            case R.id.action_settings:
                //openSettings();
                Intent intentSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(intentSettingsActivity);
                return true;

            case R.id.action_log_files:
                //openLogFiles();
                Intent intentLogFilesListActivity = new Intent(this, LogFilesListActivity.class);
                startActivity(intentLogFilesListActivity);
                return true;

            case R.id.action_credits:
                //openCredits();
                Intent intentCreditsActivity = new Intent(this, CreditsActivity.class);
                startActivity(intentCreditsActivity);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void startRecording(final float gain, final int finalCountTimeDisplay, final int finalCountTimeLog) {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

//        recorder = new AudioRecord(MediaRecorder.AudioSource.DEFAULT,
//                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
//                RECORDER_AUDIO_ENCODING, BLOCK_SIZE * BYTES_PER_ELEMENT);

        recorder = new AudioRecord(MediaRecorder.AudioSource.VOICE_RECOGNITION,
                RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                RECORDER_AUDIO_ENCODING, BLOCK_SIZE * BYTES_PER_ELEMENT);



        if (recorder.getState() == 1)
            Log.d("nostro log", "Il recorder è pronto");
        else
            Log.d("nostro log", "Il recorder non è pronto");

        recorder.startRecording();
        isRecording = true;



        // Creo una fft da BLOCK_SIZE_FFT punti --> BLOCK_SIZE_FFT / 2 bande utili,
        // ognuna da FREQRESOLUTION Hz
        fft = new DoubleFFT_1D(BLOCK_SIZE_FFT);

        recordingThread = new Thread(new Runnable() {
            public void run() {

                // Array di raw data (tot : BLOCK_SIZE_FFT * 2 bytes)
                short rawData[] = new short[BLOCK_SIZE_FFT];

                // Array di mag non pesati (BLOCK_SIZE_FFT / 2 perchè è il numero di
                // bande utili)
                final float dbFft[] = new float[BLOCK_SIZE_FFT / 2];

                // Array di mag pesati
                final float dbFftA[] = new float[BLOCK_SIZE_FFT / 2];

                float normalizedRawData;

                // La fft lavora con double e con numeri complessi (re + im in
                // sequenza)
                double[] audioDataForFFT = new double[BLOCK_SIZE_FFT * 2];

                // Soglia di udibilita (20*10^(-6))
                float amplitudeRef = 0.00002f;


                
                // terzi ottave
                final float[] dbBand = new float[THIRD_OCTAVE.length];
                final float[] linearBand = new float[THIRD_OCTAVE.length];
                final float[] linearBandCount = new float[THIRD_OCTAVE.length];
                int n = 3;
//                float summingLinearBand = 0f;
//                int controllo_frequenze = 0;
//                int controllo_frequenze_1 = 0;

                // Variabili per calcolo medie Time Display
                int indexTimeDisplay = 1;
                double linearATimeDisplay = 0;
                final float[] dbAHistoryTimeDisplay = new float[60];

                // Variabili per calcolo medie Time Log
                int indexTimeLog = 0;
                double linearTimeLog = 0;
                double linearATimeLog = 0;
                final float[] linearBandTimeLog = new float[THIRD_OCTAVE.length];




                while (isRecording) {

                    // Leggo i dati
                    recorder.read(rawData, 0, BLOCK_SIZE_FFT);

                    for (int i = 0, j = 0; i < BLOCK_SIZE_FFT; i++, j += 2) {

                        // Range [-1,1]
                        normalizedRawData = (float) rawData[i]
                                / (float) Short.MAX_VALUE;

                        // filter = ((double) (fastA * normalizedRawData))
                        // + (fastB * filter);
                        filter = normalizedRawData;

                        // Finestra di Hannings
                        double x = (2 * Math.PI * i) / (BLOCK_SIZE_FFT - 1);
                        double winValue = (1 - Math.cos(x)) * 0.5d;

                        // Parte reale
                        audioDataForFFT[j] = filter * winValue;

                        // Parte immaginaria
                        audioDataForFFT[j + 1] = 0.0;
                    }

                    // FFT
                    fft.complexForward(audioDataForFFT);

                    // Magsum non pesati
                    double linearFftGlobal = 0;

                    // Magsum pesati
                    double linearFftAGlobal = 0;

                    // indice per terzi ottava
                    int k = 0;

                    for (int ki = 0; ki < THIRD_OCTAVE.length; ki++) {
                        linearBandCount[ki] = 0;
                        linearBand[ki] = 0;
                        dbBand[ki] = 0;
                    }

                    // Leggo fino a BLOCK_SIZE_FFT/2 perchè in tot ho BLOCK_SIZE_FFT/2
                    // bande utili
                    for (int i = 0, j = 0; i < BLOCK_SIZE_FFT / 2; i++, j += 2) {

                        double re = audioDataForFFT[j];
                        double im = audioDataForFFT[j + 1];

                        // Magnitudo
                        double mag = Math.sqrt((re * re) + (im * im));

                        // Ponderata A
                        double weightFormula = weightedA[i];

                        dbFft[i] = (float) (10 * Math.log10(mag * mag
                                / amplitudeRef))
                                + (float) gain;
                        dbFftA[i] = (float) (10 * Math.log10(mag * mag
                                * weightFormula
                                / amplitudeRef))
                                + (float) gain;
                        
                        linearFftGlobal += Math.pow(10, (float) dbFft[i] / 10f);
                        linearFftAGlobal += Math.pow(10, (float) dbFftA[i] / 10f);

                        float linearFft = (float) Math.pow(10, (float) dbFft[i] / 10f);


//                        if (buttonLog.getText().toString().equals("STOP LOG")) {
//
//                            try {
//
//                                fosC.write(Integer.toString(i).getBytes());
//                                fosC.write("\t".getBytes());
//                                fosC.write(Double.toString(i * FREQRESOLUTION).getBytes());
//                                fosC.write("\t".getBytes());
//                                fosC.write(Double.toString(linearFft).getBytes());
//                                fosC.write("\n".getBytes());
//
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }

                        if ((0 <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17.8f)) {
                            linearBandCount[0] += 1;
                            linearBand[0] += linearFft;
                            dbBand[0] =  (float) (10 * Math.log10(linearBand[0]));
                        }
                        if ((17.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22.4f)) {
                            linearBandCount[1] += 1;
                            linearBand[1] += linearFft;
                            dbBand[1] =  (float) (10 * Math.log10(linearBand[1]));
                        }
                        if ((22.4f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 28.2f)) {
                            linearBandCount[2] += 1;
                            linearBand[2] += linearFft;
                            dbBand[2] =  (float) (10 * Math.log10(linearBand[2]));
                        }
                        if ((28.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 35.5f)) {
                            linearBandCount[3] += 1;
                            linearBand[3] += linearFft;
                            dbBand[3] =  (float) (10 * Math.log10(linearBand[3]));
                        }
                        if ((35.5f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 44.7f)) {
                            linearBandCount[4] += 1;
                            linearBand[4] += linearFft;
                            dbBand[4] =  (float) (10 * Math.log10(linearBand[4]));
                        }
                        if ((44.7f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 56.2f)) {
                            linearBandCount[5] += 1;
                            linearBand[5] += linearFft;
                            dbBand[5] =  (float) (10 * Math.log10(linearBand[5]));
                        }
                        if ((56.2f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 70.8f)) {
                            linearBandCount[6] += 1;
                            linearBand[6] += linearFft;
                            dbBand[6] =  (float) (10 * Math.log10(linearBand[6]));
                        }
                        if ((70.8f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 89.1f)) {
                            linearBandCount[7] += 1;
                            linearBand[7] += linearFft;
                            dbBand[7] =  (float) (10 * Math.log10(linearBand[7]));
                        }
                        if ((89.1f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 112f)) {
                            linearBandCount[8] += 1;
                            linearBand[8] += linearFft;
                            dbBand[8] =  (float) (10 * Math.log10(linearBand[8]));
                        }
                        if ((112f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 141f)) {
                            linearBandCount[9] += 1;
                            linearBand[9] += linearFft;
                            dbBand[9] =  (float) (10 * Math.log10(linearBand[9]));
                        }
                        if ((141f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 178f)) {
                            linearBandCount[10] += 1;
                            linearBand[10] += linearFft;
                            dbBand[10] =  (float) (10 * Math.log10(linearBand[10]));
                        }
                        if ((178f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 224f)) {
                            linearBandCount[11] += 1;
                            linearBand[11] += linearFft;
                            dbBand[11] =  (float) (10 * Math.log10(linearBand[11]));
                        }
                        if ((224f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 282f)) {
                            linearBandCount[12] += 1;
                            linearBand[12] += linearFft;
                            dbBand[12] =  (float) (10 * Math.log10(linearBand[12]));
                        }
                        if ((282f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 355f)) {
                            linearBandCount[13] += 1;
                            linearBand[13] += linearFft;
                            dbBand[13] =  (float) (10 * Math.log10(linearBand[13]));
                        }
                        if ((355f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 447f)) {
                            linearBandCount[14] += 1;
                            linearBand[14] += linearFft;
                            dbBand[14] =  (float) (10 * Math.log10(linearBand[14]));
                        }
                        if ((447f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 562f)) {
                            linearBandCount[15] += 1;
                            linearBand[15] += linearFft;
                            dbBand[15] =  (float) (10 * Math.log10(linearBand[15]));
                        }
                        if ((562f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 708f)) {
                            linearBandCount[16] += 1;
                            linearBand[16] += linearFft;
                            dbBand[16] =  (float) (10 * Math.log10(linearBand[16]));
                        }
                        if ((708f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 891f)) {
                            linearBandCount[17] += 1;
                            linearBand[17] += linearFft;
                            dbBand[17] =  (float) (10 * Math.log10(linearBand[17]));
                        }
                        if ((891f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1122f)) {
                            linearBandCount[18] += 1;
                            linearBand[18] += linearFft;
                            dbBand[18] =  (float) (10 * Math.log10(linearBand[18]));
                        }
                        if ((1122f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1413f)) {
                            linearBandCount[19] += 1;
                            linearBand[19] += linearFft;
                            dbBand[19] =  (float) (10 * Math.log10(linearBand[19]));
                        }
                        if ((1413f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 1778f)) {
                            linearBandCount[20] += 1;
                            linearBand[20] += linearFft;
                            dbBand[20] =  (float) (10 * Math.log10(linearBand[20]));
                        }
                        if ((1778f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2239f)) {
                            linearBandCount[21] += 1;
                            linearBand[21] += linearFft;
                            dbBand[21] =  (float) (10 * Math.log10(linearBand[21]));
                        }
                        if ((2239f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 2818f)) {
                            linearBandCount[22] += 1;
                            linearBand[22] += linearFft;
                            dbBand[22] =  (float) (10 * Math.log10(linearBand[22]));
                        }
                        if ((2818f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 3548f)) {
                            linearBandCount[23] += 1;
                            linearBand[23] += linearFft;
                            dbBand[23] =  (float) (10 * Math.log10(linearBand[23]));
                        }
                        if ((3548f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 4467f)) {
                            linearBandCount[24] += 1;
                            linearBand[24] += linearFft;
                            dbBand[24] =  (float) (10 * Math.log10(linearBand[24]));
                        }
                        if ((4467f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 5623f)) {
                            linearBandCount[25] += 1;
                            linearBand[25] += linearFft;
                            dbBand[25] =  (float) (10 * Math.log10(linearBand[25]));
                        }
                        if ((5623f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 7079f)) {
                            linearBandCount[26] += 1;
                            linearBand[26] += linearFft;
                            dbBand[26] =  (float) (10 * Math.log10(linearBand[26]));
                        }
                        if ((7079f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 8913f)) {
                            linearBandCount[27] += 1;
                            linearBand[27] += linearFft;
                            dbBand[27] =  (float) (10 * Math.log10(linearBand[27]));
                        }
                        if ((8913f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 11220f)) {
                            linearBandCount[28] += 1;
                            linearBand[28] += linearFft;
                            dbBand[28] =  (float) (10 * Math.log10(linearBand[28]));
                        }
                        if ((11220f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 14130f)) {
                            linearBandCount[29] += 1;
                            linearBand[29] += linearFft;
                            dbBand[29] =  (float) (10 * Math.log10(linearBand[29]));
                        }
                        if ((14130f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 17780f)) {
                            linearBandCount[30] += 1;
                            linearBand[30] += linearFft;
                            dbBand[30] =  (float) (10 * Math.log10(linearBand[30]));
                        }
                        if ((17780f <= i * FREQRESOLUTION) && (i * FREQRESOLUTION < 22390f)) {
                            linearBandCount[31] += 1;
                            linearBand[31] += linearFft;
                            dbBand[31] =  (float) (10 * Math.log10(linearBand[31]));
                        }

//                        if (i * FREQRESOLUTION < THIRD_OCTAVE[k] * (float) Math.pow(2, (float) 1 / (n * 2f))) {
//
//                            summingLinearBand += linearFft;
//                            controllo_frequenze_1++;
//                        } else {
//                            linearBand[k] = summingLinearBand;
//                            if (summingLinearBand == 0){
//                                dbBand[k] = 0f;
//                            } else {
//                                dbBand[k] = 10 * (float) (Math.log10(summingLinearBand));
//                            }
//                            summingLinearBand = (float) linearFft;
//                            controllo_frequenze_1 = 0;
//                            k++;
//                        }
                    }
//                    // Scrivo sul file controllo
//                    if (controllo_frequenze <1500) {
//
//                        if (buttonLog.getText().toString().equals("STOP LOG")) {
//
//                            try {
//                                for (int ki = 0; ki < THIRD_OCTAVE.length; ki++) {
//                                    fosC.write(Integer.toString(ki).getBytes());
//                                    fosC.write("\t".getBytes());
//                                    fosC.write(Double.toString(THIRD_OCTAVE[ki]).getBytes());
//                                    fosC.write("\t".getBytes());
//                                    fosC.write(Double.toString(linearBandCount[ki]).getBytes());
//                                    fosC.write("\t".getBytes());
//                                    fosC.write(Double.toString(linearBand[ki]).getBytes());
//                                    fosC.write("\t".getBytes());
//                                    fosC.write(Double.toString(dbBand[ki]).getBytes());
//                                    fosC.write(("\n").getBytes());
//
//                                }
//
////                                    fosC.write(Integer.toString(i).getBytes());
////                                    fosC.write("\t".getBytes());
////                                    fosC.write(Double.toString(i * FREQRESOLUTION).getBytes());
////                                    fosC.write("\t".getBytes());
////                                    fosC.write(Integer.toString(k).getBytes());
////                                    fosC.write("\t".getBytes());
////                                    fosC.write(Integer.toString(controllo_frequenze_1).getBytes());
////                                    fosC.write("\t".getBytes());
////                                    fosC.write(Double.toString(THIRD_OCTAVE[k]).getBytes());
////                                    fosC.write("\t".getBytes());
////                                    fosC.write(Double.toString(THIRD_OCTAVE[k] * (float) Math.pow(2, (float) 1 / (n * 2f))).getBytes());
////                                    fosC.write(("\n").getBytes());
//
//                            } catch (Exception e) {
//                                throw new RuntimeException(e);
//                            }
//                        }
//                    }
                    ////


//                    controllo_frequenze++;


                    final double dbFftAGlobal = 10 * Math.log10(linearFftAGlobal);

                    // calcolo min e max valore globale FFT pesato A
                     if (dbFftAGlobal > 0) {
                        if (dbFftAGlobalMinFirst == 0) {
                            dbFftAGlobalMinTemp = dbFftAGlobal;
                            dbFftAGlobalMinFirst = 1;
                        } else {
                            if (dbFftAGlobalMinTemp > dbFftAGlobal) {
                                dbFftAGlobalMinTemp = dbFftAGlobal;
                            }
                        }
                        if (dbFftAGlobalMaxFirst == 0){
                            dbFftAGlobalMaxTemp = dbFftAGlobal;
                            dbFftAGlobalMaxFirst = 1;
                        } else {
                            if (dbFftAGlobalMaxTemp < dbFftAGlobal){
                                dbFftAGlobalMaxTemp = dbFftAGlobal;
                            }
                        }
                    }
                    dbFftAGlobalMin = dbFftAGlobalMinTemp;
                    dbFftAGlobalMax = dbFftAGlobalMaxTemp;
                    
                    
                    // Running Leq
                    fftCount++;
                    linearFftAGlobalRunning += linearFftAGlobal;
                    dbFftAGlobalRunning = 10 * Math.log10(linearFftAGlobalRunning/fftCount);
                    final int TimeRunning = (int) fftCount / NUMBER_OF_FFT_PER_SECOND;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LMax.setText(String.format("%.1f", dbFftAGlobalMax));
                            LAeqRunning.setText(String.format("%.1f", dbFftAGlobalRunning));
                            durationTimeRunning.setText(String.format("%d days %02d:%02d:%02d", TimeRunning / (3600 * 24), (TimeRunning % (3600*24) / 3600), (TimeRunning % 3600) / 60, (TimeRunning % 60)));
                            LMin.setText(String.format("%.1f", dbFftAGlobalMin));
                            if (levelToShow == "dbFftAGlobalMin") {
                                level.setText(String.format("%.1f", dbFftAGlobalMin));
                            }
                            if (levelToShow == "dbFftAGlobalMax") {
                                level.setText(String.format("%.1f", dbFftAGlobalMax));
                            }
                            if (levelToShow == "dbFftAGlobalRunning") {
                                level.setText(String.format("%.1f", dbFftAGlobalRunning));
                            }
                            if (buttonLog.getText().toString().equals("STOP LOG")) {
                                int diffInSec = (int) (new Date().getTime() - dateLogStart.getTime())/1000;

                                durationTimeLog.setText(String.format("%d days %02d:%02d:%02d", diffInSec / (3600 * 24), (diffInSec % (3600*24) / 3600), (diffInSec % 3600) / 60, (diffInSec % 60 )) + " (" + timeLogStringMinSec + ")");
                                //durationTimeLog.setText(String.format("%d days %02d:%02d:%02d", TimeUnit.MILLISECONDS.toDays(diffInMilliSec), TimeUnit.MILLISECONDS.toHours(diffInMilliSec), TimeUnit.MILLISECONDS.toMinutes(diffInMilliSec), TimeUnit.MILLISECONDS.toSeconds(diffInMilliSec)));
                            }
                        }
                    });

                    // calcolo min e max per dbBand non pesato
                    // definisco minimi e massimi per le bande
                    for (int kk = 0; kk < dbBand.length; kk++) {
                        if (dbBandMax[kk] < dbBand[kk]) {
                            dbBandMax[kk] = dbBand[kk];
                        }
                        if (kkk >= 10) { // controllo per bontà leq bande: solo se kkk > 10 misurano bene
                            if (dbBandMin[kk] == 0f) {
                                if (dbBand[kk] > 0) {
                                    dbBandMin[kk] = dbBand[kk];
                                }
                            } else if (dbBandMin[kk] > dbBand[kk]) {
                                dbBandMin[kk] = dbBand[kk];
                            }
                        }
                    }
                    kkk++;

                    // FFT plot
                    if (plotFFT.getVisibility() == View.VISIBLE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                plotFFT.setDataPlot(BLOCK_SIZE_FFT, FREQRESOLUTION, dbFft, dbFftA);
                            }
                        });
                    };
                    // SLM plot
                    if (plotSLM.getVisibility() == View.VISIBLE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                plotSLM.setDataPlot((float) dbFftAGlobal, (float) dbFftAGlobalMin, (float) dbFftAGlobalMax);
                            }
                        });
                    };
                    // ThirdOctave plot
                    if (plotThirdOctave.getVisibility() == View.VISIBLE) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                plotThirdOctave.setDataPlot(dbBand, dbBandMin, dbBandMax);
                            }
                        });
                    };

                    // SLMHistory plot e LAeqTimeDisplay
                    // Calcolo Medie per Time Display
                    linearATimeDisplay += linearFftAGlobal;
                    if (indexTimeDisplay < finalCountTimeDisplay) {
                        indexTimeDisplay++;
                    } else {
                        dbATimeDisplay = 10 * Math.log10(linearATimeDisplay/finalCountTimeDisplay);
                        indexTimeDisplay = 1;
                        linearATimeDisplay = 0;

                        for (int i=1; i<60; i++){
                            dbAHistoryTimeDisplay[i-1] = dbAHistoryTimeDisplay[i];
                        }
                        dbAHistoryTimeDisplay[59] = (float) dbATimeDisplay;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                LAeqTimeDisplay.setText(String.format("%.1f",dbATimeDisplay));
                                if (levelToShow == "dbATimeDisplay") {
                                    level.setText(String.format("%.1f", dbATimeDisplay));
                                }
                                if (plotSLMHistory.getVisibility() == View.VISIBLE) {
                                    plotSLMHistory.setDataPlot(dbAHistoryTimeDisplay);
                                }
                            }
                        });
                    }

                    // Scrittura log file
                    // Calcolo medie per Time Log
                    linearTimeLog += linearFftGlobal;
                    linearATimeLog += linearFftAGlobal;
                    for (int i=0; i < THIRD_OCTAVE.length; i++){
                        linearBandTimeLog[i] += linearBand[i];
                    }
                    if (indexTimeLog < finalCountTimeLog) {
                        indexTimeLog++;
                    } else {
                        final double dbTimeLog = 10 * Math.log10(linearTimeLog/finalCountTimeLog);
                        final double dbATimeLog = 10 * Math.log10(linearATimeLog/finalCountTimeLog);
                        final double[] dbBandTimeLog = new double[THIRD_OCTAVE.length];
                        for (int i=0; i < THIRD_OCTAVE.length; i++){
                            dbBandTimeLog[i] = 10 * Math.log10(linearBandTimeLog[i]/finalCountTimeLog);
                        }
                        // parte per bande senza valori
                        dbBandTimeLog[1] = dbBandTimeLog[0];
                        dbBandTimeLog[3] = dbBandTimeLog[2];
                        dbBandTimeLog[4] = dbBandTimeLog[5];
                        dbBandTimeLog[6] = dbBandTimeLog[7];

                        indexTimeLog = 1;
                        linearTimeLog = 0;
                        linearATimeLog = 0;
                        for (int i=0; i < THIRD_OCTAVE.length; i++){
                            linearBandTimeLog[i] = 0;
                        }
                        // Scrivo sul file
                        if (buttonLog.getText().toString().equals("STOP LOG")) {
                            DateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss,SSS");
                            String strDate = sdf.format(new Date());
                            try {
                                fos.write(String.format("%s\t%.1f\t%.1f", strDate, dbTimeLog, dbATimeLog).getBytes());
                                for (int i=0; i < THIRD_OCTAVE.length; i++){
                                    fos.write(String.format("\t%.1f", dbBandTimeLog[i]).getBytes());
                                }
                                fos.write(("\n").getBytes());

                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }

                    }

                } // while
            }
        }, "AudioRecorder Thread");
        recordingThread.start();

    }

    private void stopRecording() {
        // stops the recording activity
        if (recorder != null) {
            isRecording = false;
            try {
                recordingThread.join();
                //fos.close();
            } catch (Exception e) {
                Log.d("nostro log",
                        "Il Thread principale non può attendere la chiusura del thread secondario dell'audio");
            }
            recorder.stop();
            recorder.release();
            recorder = null;
            recordingThread = null;
        }
    }


    private void startRecordingLogFile() {
        // start the recording log file
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        String filename = String.format("%s.logAudio.txt", df.format(new Date()));
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + "openoise");

        if (!path.exists()) {
            Log.d("mio", "il path non esiste. Creato? : " + path.mkdirs());
        }
        try {
            File file = new File(path, filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(("DataTime\tdB\tdB(A)").getBytes());
            for (int i=0; i < THIRD_OCTAVE_LABEL.length; i++) {
                fos.write(("\t" + THIRD_OCTAVE_LABEL[i]).getBytes());
            }
            fos.write(("\n").getBytes());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void stopRecordingLogFile() {
        // stop the recording log file
        try {
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onDestroy() {
        stopRecording();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        stopRecording();
        finish();
        super.onBackPressed();
    }

    @Override
    public void onResume() {
        super.onResume();
        stopRecording();
        // read the parameter in the settings
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String gainString = sharedPref.getString("gain", "");
            String timeLogString = sharedPref.getString("timeLog", "");
            String timeDisplayString = sharedPref.getString("timeDisplay", "");
            gain = Float.parseFloat(gainString);
            timeDisplay = Integer.parseInt(timeDisplayString);
            timeLog = Integer.parseInt(timeLogString);
        } catch (Exception e) {
            gain = 0.0f;
            timeDisplay = 1;
            timeLog = 1;
        }

        final int finalCountTimeDisplay = (int) (timeDisplay * NUMBER_OF_FFT_PER_SECOND);
        final int finalCountTimeLog = (int) (timeLog * NUMBER_OF_FFT_PER_SECOND);

        LAeqTimeDisplayLabel.setText("LAeq (" + timeDisplay + " s)");
//        levelLabel.setText("LAeq(t)");
//        levelToShow = "dbFftAGlobalRunning";
        levelLabel.setText(LAeqTimeDisplayLabel.getText());
        levelToShow = "dbATimeDisplay";

        if (timeLog == 1) {
            timeLogStringMinSec = "1 sec";
        } else if (timeLog == 2) {
            timeLogStringMinSec = "2 sec";
        } if (timeLog == 5) {
            timeLogStringMinSec = "5 sec";
        } if (timeLog == 30) {
            timeLogStringMinSec = "30 sec";
        } if (timeLog == 60) {
            timeLogStringMinSec = "1 min";
        } if (timeLog == 120) {
            timeLogStringMinSec = "2 min";
        } if (timeLog == 300) {
            timeLogStringMinSec = "5 min";
        } if (timeLog == 600) {
            timeLogStringMinSec = "10 min";
        } if (timeLog == 3600) {
            timeLogStringMinSec = "1 hour";
        }
        //durationTimeLog.setText("Logging interval: " + timeLogStringMinSec);
        durationTimeLog.setText(durationTimeLogText + " " + timeLogStringMinSec);

        precalculateWeightedA();

        startRecording((Float) gain, (Integer) finalCountTimeDisplay, (Integer) finalCountTimeLog);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_main);}
        else if  (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            setContentView(R.layout.activity_main_landscape);}

    }

    private void startRecordingLogParametersFile() {
        // start the recording log file
        DateFormat df = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss");
        String filename = String.format("%s.logAudioParameters.txt", df.format(new Date()));
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + "openoise");

        if (!path.exists()) {
            Log.d("mio", "il path non esiste. Creato? : " + path.mkdirs());
        }
        try {
            File file = new File(path, filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            fosC = new FileOutputStream(file);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try{
            fosC.write("getMinBufferSize: ".getBytes());
            fosC.write(Integer.toString(AudioRecord.getMinBufferSize(
                    RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING)).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("BLOCK_SIZE_FFT: ".getBytes());
            fosC.write(Integer.toString(BLOCK_SIZE_FFT).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("NUMBER_OF_FFT_PER_SECOND: ".getBytes());
            fosC.write(Integer.toString(NUMBER_OF_FFT_PER_SECOND).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("FREQRESOLUTION: ".getBytes());
            fosC.write(Double.toString(FREQRESOLUTION).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("Short.MAX_VALUE: ".getBytes());
            fosC.write(Double.toString(Short.MAX_VALUE).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("Time Display: ".getBytes());
            fosC.write(Double.toString(timeDisplay).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("Time Log: ".getBytes());
            fosC.write(Double.toString(timeLog).getBytes());
            fosC.write("\n".getBytes());
            fosC.write("Time Log string minsec: ".getBytes());
            fosC.write(timeLogStringMinSec.getBytes());
            fosC.write("\n".getBytes());

            fosC.write("\n\n\n\n\n\n\n".getBytes());


        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    private void stopRecordingLogParametersFile() {
        // stop the recording log file
        try {
            fosC.close();
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }


}
