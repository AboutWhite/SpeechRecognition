package com.example.aboutwhite.speechrecognizer;

import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;

import java.util.List;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.vikramezhil.droidspeech.DroidSpeech;
import com.vikramezhil.droidspeech.OnDSListener;
import com.vikramezhil.droidspeech.OnDSPermissionsListener;
import java.util.List;
import java.util.Random;

import dataTransfer.TCPClient;
import dataTransfer.TCPServer;

public class MainActivity extends Activity implements OnDSListener, OnClickListener, TCPClient.OnMessageReceived, TCPServer.OnMessageReceived {

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
        client.stopClient();
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

        client = new TCPClient(this, this, ip, 4444);
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

    @Override
    public void onDroidSpeechFinalResult(String finalSpeechResult)
    {
        // Setting the final speech result
        this.finalSpeechResult.setText(finalSpeechResult);

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


