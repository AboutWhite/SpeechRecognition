package com.example.aboutwhite.speechrecognizer;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;

import java.util.Random;

import dataTransfer.TCPClient;

public class MainActivity extends Activity implements OnDSListener, OnClickListener, TCPClient.OnMessageReceived, TCPClient.OnNumberRequested{

    public final String TAG = "Activity_DroidSpeech";
    private DroidSpeech droidSpeech;
    private TextView finalSpeechResult;
    private String result = "Result: ";
    private ImageView start;
    private ImageView stop;
    private TCPClient client;
    private Button restartClient, closeConnection;
    private EditText ipEditText;
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);


        // Initialize Droid Speech
        droidSpeech = new DroidSpeech(this, null);
        droidSpeech.setOnDroidSpeechListener(this);
        droidSpeech.setShowRecognitionProgressView(true);
        droidSpeech.setOneStepResultVerify(false);
        droidSpeech.setRecognitionProgressMsgColor(Color.BLACK);
        droidSpeech.setOneStepVerifyConfirmTextColor(Color.BLACK);
        droidSpeech.setOneStepVerifyRetryTextColor(Color.BLACK);
        droidSpeech.setOfflineSpeechRecognition(true);

        finalSpeechResult = findViewById(R.id.finalSpeechResult);

        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        start.setOnClickListener(this);
        //stop.setOnClickListener(this);


        init();
    }

    /**
     * initialises ui elements
     * sets click listeners for the buttons
     */
    private void init()
    {
        textView = findViewById(R.id.txtVwServerStatus);
        ipEditText = findViewById(R.id.ipEditText);
        restartClient = findViewById(R.id.restartClient);
        closeConnection = findViewById(R.id.closeConnection);

        restartClient.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                String ip = ipEditText.getText().toString();
                // check ip string
                if(ip != "")
                {
                    //(re)start client
                    startClient(ip);
                }
            }
        });

        closeConnection.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                closeClientConnection();
            }
        });
    }

    public void changeTextViewText(String txt)
    {
        textView.setText(txt);
    }

    public void closeClientConnection()
    {
        if (client != null)
        {
            client.stopClient();
            client = null;
        }
    }

    private void startClient(String ip)
    {
        if(client != null) {
            if (client.isRunning()) {
                client.stopClient();

                while (client.isRunning()) {
                }
            }
        }

        client = new TCPClient(this, this, this, ip, 4444);
        client.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    protected void onPause()
    {
        super.onPause();

        if(stop.getVisibility() == View.VISIBLE)
        {
            stop.performClick();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if(stop.getVisibility() == View.VISIBLE)
        {
            stop.performClick();
        }
    }

    @Override
    public void onDroidSpeechSupportedLanguages(String currentSpeechLanguage, List<String> supportedSpeechLanguages) {

    }

    @Override
    public void onDroidSpeechRmsChanged(float rmsChangedValue) {

    }

    @Override
    public void onDroidSpeechLiveResult(String liveSpeechResult)
    {
        Log.i(TAG, "Live speech result = " + liveSpeechResult);
    }


    /**
     * this method is called by the speech recognition after it detected a word/sentence
     * the method calls the analysis of the detected string and notifies the client thread afterwards
     * @param finalSpeechResult: detected word(s)
     */
    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult)
    {
        // Setting the final speech result
        this.finalSpeechResult.setText(finalSpeechResult);


        String number = analyseResult(finalSpeechResult);
        notifyClientThread(number);


        if(droidSpeech.getContinuousSpeechRecognition())
        {
            int[] colorPallets1 = new int[] {Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA};
            int[] colorPallets2 = new int[] {Color.YELLOW, Color.RED, Color.CYAN, Color.BLUE, Color.GREEN};

            // Setting random color pallets to the recognition progress view
            droidSpeech.setRecognitionProgressViewColors(new Random().nextInt(2) == 0 ? colorPallets1 : colorPallets2);
        }
        else
        {
            stop.setVisibility(View.GONE);
            start.setVisibility(View.VISIBLE);
        }
    }

    /**
     * is called by client thread to start a request for a number;
     * droidSpeech starts speech recognition
     */
    @Override
    public void requestNumber()
    {
        droidSpeech.startDroidSpeechRecognition();
    }

    /**
     * notifies the client thread that a number was detected by the speech recognition
     * and hands the number over to the client thread
     * @param number: detected number
     */
    private void notifyClientThread(String number)
    {
        client.numberReceived(number);
    }

    private String analyseResult(String s) {

        final String NO_NUMBER_DETECTED = "NaN";
        String[] splitStr = s.split("\\s+");

        ArrayList<String> numbers = new ArrayList<>();


        for (String current : splitStr)
        {
            switch (current.toLowerCase())
            {
                case "0":
                case "null":
                    numbers.add("0");
                    break;
                case "1":
                case "eins":
                case "heinz":
                case "hans":
                    numbers.add("1");
                    break;
                case "2":
                case "zwei":
                    numbers.add("2");
                    break;
                case "3":
                case "drei":
                    numbers.add("3");
                    break;
                case "4":
                case "vier":
                    numbers.add("4");
                    break;
                case "5":
                case "fünf":
                    numbers.add("5");
                    break;
                case "6":
                case "sechs":
                case "sex":
                    numbers.add("6");
                    break;
                case "7":
                case "sieben":
                    numbers.add("7");
                    break;
                case "8":
                case "acht":
                    numbers.add("8");
                    break;
                case "9":
                case "neun":
                    numbers.add("9");
                    break;
                case "10":
                case "zehn":
                    numbers.add("10");
                    break;
                default:
                    break;
            }
        }

        if(numbers.size() <= 0){
            return NO_NUMBER_DETECTED;
        }else{
            return numbers.get(numbers.size()-1);
        }
    }




    @Override
    public void onDroidSpeechClosedByUser()
    {
        stop.setVisibility(View.GONE);
        start.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDroidSpeechError(String errorMsg)
    {
        // Speech error
        Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();

        stop.post(new Runnable()
        {
            @Override
            public void run()
            {
                // Stop listening
                stop.performClick();
            }
        });
    }



    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.start:

                // Starting droid speech
                droidSpeech.startDroidSpeechRecognition();

                // Setting the view visibilities when droid speech is running
                start.setVisibility(View.GONE);
                stop.setVisibility(View.VISIBLE);

                break;

            case R.id.stop:

                // Closing droid speech
                droidSpeech.closeDroidSpeechOperations();

                stop.setVisibility(View.GONE);
                start.setVisibility(View.VISIBLE);

                break;
        }

    }

    public void messageReceived(String message)
    {
        Log.d("communication", "messageReceived: " + message);
        if(message == "listen")
        {
            //start automatic listening
        }
    }
}


