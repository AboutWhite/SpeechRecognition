package dataTransfer;

/*
    Source: https://www.myandroidsolutions.com/2012/07/20/android-tcp-connection-tutorial/#.WuMW_JdCSUl
 */

import android.os.AsyncTask;
import android.util.Log;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPClient extends AsyncTask <Integer, Integer, Double>
{

    private String serverMessage;
    public static final String SERVERIP = "192.168.2.101"; //"192.168.178.34/46"; //217.232.249.44; //"192.168.0.102"; //your computer IP address
    public static final int SERVERPORT = 4444;
    private OnMessageReceived mMessageListener = null;
    private boolean mRun = false;

    PrintWriter out;
    BufferedReader in;

    /**
     *  Constructor of the class. OnMessagedReceived listens for the messages received from server
     */
    public TCPClient(OnMessageReceived listener) {
        mMessageListener = listener;
    }

    /**
     * Sends the message entered by client to the server
     * @param message text entered by client
     */
    public void sendMessage(String message){
        if (out != null && !out.checkError()) {
            out.println(message);
            out.flush();
            //Log.d("message", "sendMessage: sent");
        }
    }

    public void stopClient(){
        mRun = false;
    }

    public void run() {

        mRun = true;

        try {
            //here you must put your computer's IP address.
            InetAddress serverAddr = InetAddress.getByName(SERVERIP);

            Log.e("TCP Client", "C: Connecting...");

            //create a socket to make the connection with the server
            Socket socket = new Socket(serverAddr, SERVERPORT);
            Log.d("Clinet", "socket initiated");

            try {

                //send the message to the server
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())),true);

                Log.e("TCP Client", "C: Sent.");

                Log.e("TCP Client", "C: Done.");

                //receive the message which the server sends back
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //in this while the client listens for the messages sent by the server
                while (mRun) {
                    //Log.d("Client", "running");
                    //serverMessage = in.readLine();

                    if (serverMessage != null && mMessageListener != null) {
                        //call the method messageReceived from MyActivity class
                        mMessageListener.messageReceived(serverMessage);
                    }
                    serverMessage = null;
                    sendMessage("This is a client message to the server");

                }

                Log.e("RESPONSE FROM SERVER", "S: Received Message: '" + serverMessage + "'");

            } catch (Exception e) {

                Log.e("TCP", "S: Error", e);
                e.printStackTrace();

            } finally {
                //the socket must be closed. It is not possible to reconnect to this socket
                // after it is closed, which means a new socket instance has to be created.
                socket.close();
            }

        } catch (Exception e) {

            Log.e("TCP", "C: Error", e);

        }

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
        run();
        return 0.0;
    }

    //Declare the interface. The method messageReceived(String message) will must be implemented in the MyActivity
    //class at on asynckTask doInBackground
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}
