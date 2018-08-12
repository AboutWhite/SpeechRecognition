package dataTransfer;

/*
    Source: https://www.myandroidsolutions.com/2012/07/20/android-tcp-connection-tutorial/#.WuMW_JdCSUl
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Calendar;

import edu.ucsd.sccn.LSL;

public class TCPClient extends AsyncTask <Integer, Integer, Double>
{
    private final String STREAM_INFO_NAME = "Numbers";
    private final String STREAM_INFO_TYPE = "SpeechRecognition";
    private final String STREAM_ID = "sr1"; //sr1 = speechRecognition1
    private final String LSL_SERVER_STREAM_INFO_NAME = "Instructions";
    private final String LSL_SERVER_STREAM_INFO_TYPE = "TCP_Server";
    private final String LSL_SERVER_STREAM_INFO_ID= "tcpS1";

    private final String[] CLOSE_CONNECTION = {"closeC"};
    private final String SERVER_LISTEN_REQEST = "listen";

    private String serverIP = "";
    private int serverPort = 0;

    private OnChangeUIText mUIThreadListener;
    private OnNumberRequested mRequestListener;

    private Activity uiActivity;
    private Queue queue;

    private boolean mRun = false;       //is client listening?
    private boolean isRunning = false;  //is client running?

    private String[] stringToSend = new String[1];

    PrintWriter out = null;
    LSL.StreamOutlet outlet = null;
    LSL.StreamInlet inlet = null;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnChangeUIText uiListener, OnNumberRequested requestListener, Queue q, Activity a, String sIP, int port) {
        mUIThreadListener = uiListener;
        mRequestListener = requestListener;
        queue = q;
        uiActivity = a;
        serverIP = sIP;
        serverPort = port;

        initLSL();
    }

    public void initLSL()
    {
        LSL.StreamInfo out_info = new LSL.StreamInfo(STREAM_INFO_NAME, STREAM_INFO_TYPE, 1, LSL.IRREGULAR_RATE, LSL.ChannelFormat.string, STREAM_ID);

        try
        {
            outlet = new LSL.StreamOutlet(out_info);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String[] message){
        /*if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }*/

        if(outlet != null)
        {
            outlet.push_sample(message, Calendar.getInstance().getTimeInMillis());
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {
        try {
            byte [] byteArray;
            int length;
            String serverMessage = "";
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            changeTextViewInMainActivity("Client: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, serverPort);

            changeTextViewInMainActivity("Client: socket initiated");

            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                changeTextViewInMainActivity("Client: printWriter initiated");

                mRun = true;
                changeTextViewInMainActivity("Client: ready to send");

                String[] receive = new String[1];

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    //////////////////////////////////
                    // send message

                    if(serverMessage.equals(SERVER_LISTEN_REQEST))
                    {
                        requestNumber();
                        serverMessage = "";
                    }

                    stringToSend[0] = queue.getFirstObjectFromList();
                    if(stringToSend != null)
                    {
                        sendMessage(stringToSend);
                        stringToSend[0] = "";
                        Log.d("Client", "sent message to server");
                        changeTextViewInMainActivity("Client: message sent to server");
                    }

                    //////////////////////////////////
                    // receive message

                    inlet.pull_sample(receive);
                    Log.d("Server msg", receive[0]);

                    /*InputStream inputStream = socket.getInputStream();

                    if((length = inputStream.available()) > 0)
                    {
                        byteArray = new byte[length];
                        inputStream.read(byteArray);

                        serverMessage = new String (byteArray);
                        Log.d("TCP Client", serverMessage);
                        changeTextViewInMainActivity("Client: Server message received: " + serverMessage + ".");
                    }*/

                    //////////////////////////////////

                    if(!mRun)
                    {
                        sendMessage(CLOSE_CONNECTION);
                        Log.d("Client", "close message sent");
                    }

                }
            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);
                changeTextViewInMainActivity("Client: error after socket initiation");
                e.printStackTrace();

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);
            changeTextViewInMainActivity("Client: error during socket initiation: " + e.getMessage());

        }

    }

    /**
        client thread requested a number from the speech recognition
        which uses this method to return the number to the client thread
     */
    public void numberReceived(String number)
    {
        stringToSend[0] = number;
        Log.d("Number", stringToSend[0]);
        int a = 0;
    }

    private void changeTextViewInMainActivity(final String txt)
    {
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUIThreadListener.changeTextViewText(txt);
            }
        });
    }

    private void requestNumber()
    {
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRequestListener.requestNumber();
            }
        });
    }

    public boolean isRunning()
    {
        return isRunning;
    }

    @Override
    protected void onProgressUpdate(Integer... values)
    {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(Double result)
    {
        Log.d("Client", "client shutdown");
        super.onPostExecute(result);
    }

    @Override
    protected Double doInBackground(Integer... params)
    {
        if(serverIP != "" && serverPort != 0) {
            isRunning = true;
            run();
            changeTextViewInMainActivity("Client: closed");
            isRunning = false;
        }
        else
        {
            Log.e("Client", "Server IP or port not specified");
        }
        return 0.0;
    }



    //Declare the interface.
    public interface OnChangeUIText {
        public void changeTextViewText(String txt);
    }

    public interface OnNumberRequested
    {
        public void requestNumber();
    }
}
