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
    private String serverIP = ""; // = "132.199.202.158"; //"192.168.178.34/46"; //217.232.249.44; //"192.168.0.102"; //your computer IP address
    private String closeConnection = "closeC";
    private int serverPort = 0;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false; //is client listening?
    private boolean isRunning = false; //is client running?
    private Activity uiActivity;

    PrintWriter out;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener, Activity a, String sIP, int port) {
        mMessageListener = listener;
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
        //sendMessage(closeConnection);

        mRun = false;
    }

    public void run() {
        try {
            byte [] byteArray = new byte[6];
            String serverMessage = "";
            InetAddress serverAddr = InetAddress.getByName(serverIP);

            Log.e("TCP Client", "C: Connecting...");
            changeTextViewInMainAcitvity("Client: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, serverPort);
            Log.d("TCP Client", "socket initiated");
            changeTextViewInMainAcitvity("Client: socket initiated");

            try {
                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                Log.d("TCP Client", "printWriter initiated");
                changeTextViewInMainAcitvity("Client: printWriter initiated");

                mRun = true;
                Log.d("TCP Client", "client ready to send");
                changeTextViewInMainAcitvity("Client: ready to send");
                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    //////////////////////////////////
                    // send message
                    String compareString = new String(new byte[]{108, 105, 115, 116, 101, 110});
                    serverMessage = "listen";
                    if(serverMessage == compareString)
                    {
                        Log.d("Client", "here");
                        sendMessage("This is a client message to the server");
                        changeTextViewInMainAcitvity("Client: message sent to server");
                        serverMessage = "";
                    }

                    //////////////////////////////////
                    // receive message
                    InputStream inputStream = socket.getInputStream();

                    if(inputStream.available() > 0)
                    {
                        inputStream.read(byteArray);

                        serverMessage = new String (byteArray);
                        if(serverMessage == "listen")
                        {
                            Log.d("Client", "listen is true");
                        }
                        Log.d("TCP Client", serverMessage);
                        changeTextViewInMainAcitvity("Client: Server message received: " + serverMessage + ".");
                    }

                    //////////////////////////////////

                    if(!mRun)
                    {
                        sendMessage(closeConnection);
                    }

                }
            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);
                changeTextViewInMainAcitvity("Client: error after socket initiation");
                e.printStackTrace();

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);
            changeTextViewInMainAcitvity("Client: error during socket initiation: " + e.getMessage());

        }

    }

    private void changeTextViewInMainAcitvity(final String txt)
    {
        uiActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageListener.changeTextViewText(txt);
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
            changeTextViewInMainAcitvity("Client: closed");
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
}
