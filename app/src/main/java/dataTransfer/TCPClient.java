package dataTransfer;

/*
    Source: https://www.myandroidsolutions.com/2012/07/20/android-tcp-connection-tutorial/#.WuMW_JdCSUl
 */

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;

public class TCPClient extends AsyncTask <Integer, Integer, Double>
{
    private String serverIP = "";
    private final String CLOSE_CONNECTION = "closeC";
    private final String NO_NUMBER_DETECTED = "NaN";
    private final String SERVER_LISTEN_REQEST = "listen";
    private int serverPort = 0;
    private OnMessageReceived mMessageListener;
    private OnNumberRequested mRequestListener;
    private boolean mRun = false;       //is client listening?
    private boolean isRunning = false;  //is client running?
    private Activity uiActivity;

    PrintWriter out;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived messageListener, OnNumberRequested requestListener, Activity a, String sIP, int port) {
        mMessageListener = messageListener;
        mRequestListener = requestListener;
        uiActivity = a;
        serverIP = sIP;
        serverPort = port;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {
        try {
            byte [] byteArray = new byte[6];
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
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    //////////////////////////////////
                    // send message

                    if(serverMessage.equals(SERVER_LISTEN_REQEST))
                    {
                        Log.d("Client", "here");
                        //sendMessage("This is a client message to the server");

                        requestNumber();

                        changeTextViewInMainActivity("Client: message sent to server");
                        serverMessage = "";
                    }

                    //////////////////////////////////
                    // receive message
                    InputStream inputStream = socket.getInputStream();

                    if(inputStream.available() > 0)
                    {
                        inputStream.read(byteArray);

                        serverMessage = new String (byteArray);
                        Log.d("TCP Client", serverMessage);
                        changeTextViewInMainActivity("Client: Server message received: " + serverMessage + ".");
                    }

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
        sendMessage(number);
    }

    private void changeTextViewInMainActivity(final String txt)
    {
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageListener.changeTextViewText(txt);
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

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
        public void changeTextViewText(String txt);
    }

    public interface OnNumberRequested
    {
        public void requestNumber();
    }
}
