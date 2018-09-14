/*
 * Source: https://gist.github.com/danielbierwirth/0636650b005834204cb19ef5ae6ccedb
 */


using System;
using System.Collections;
using System.Collections.Generic;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using UnityEngine;
using Assets.LSL4Unity.Scripts;
//using LSL;

public class TCPServer : MonoBehaviour
{
    private TcpListener tcpListener;
    private Thread tcpListenerThread;
    private TcpClient connectedTcpClient;
    private bool clientConnected = false;
    private bool shouldSend = false; //whenever shouldSend is true, a listen request is sent to the client

    private ArrayList receivedNumbers = new ArrayList();

    float timer = 10;

    private const string LSL_SERVER_STREAM_INFO_NAME = "Instructions";
    private const string LSL_SERVER_STREAM_INFO_TYPE = "TCP_Server";
    private const string LSL_SERVER_STREAM_INFO_ID= "tcpS1";
    private const string LSL_SR_STREAM_INFO_NAME = "SpeechRecognition";
    private const string LSL_SR_STREAM_INFO_TYPE = "Numbers";

    private readonly string CLOSE_CONNECTION = "closeC";
    private readonly string LISTEN_REQUEST = "listen";
    private readonly string SPEECH_RECOGNITION_FAILED = "NaN";

    // Use this for initialization
    void Start()
    {
        Debug.Log("starting Server");



        tcpListenerThread = new Thread(new ThreadStart(ListenForIncommingRequests));
        tcpListenerThread.IsBackground = true;
        tcpListenerThread.Start();
    }

    // Update is called once per frame
    // ATTENTION: this method must be adapted to the games requirement
    void Update()
    {
        timer += Time.deltaTime;
        if (timer >= 5)
        {
            shouldSend = true;
            timer = 0;
        }
    }

    private void ListenForIncommingRequests()
    {
        Debug.Log("Server active");
        try
        {
            tcpListener = new TcpListener(IPAddress.Any, 4444);
            tcpListener.Start();
            Debug.Log("Server is listening");
            Byte[] bytes = new Byte[1024];
            while (true)
            {
                Debug.Log("Waiting for client to connect");
                using (connectedTcpClient = tcpListener.AcceptTcpClient())
                {
                    // Get a stream object for reading
                    using (NetworkStream stream = connectedTcpClient.GetStream())
                    {
                        clientConnected = true;
                        Debug.Log("Client connected");

                        string[] receive = new string[1];
                        string[] send = new string[1];
                        while (clientConnected)
                        {

                            /////////////////////////////////////////
                            // this if statement must be adapted to fit the game's requirements
                            if (shouldSend)
                            {
                                shouldSend = false;
                                sendMessage(LISTEN_REQUEST);
                            }

                            ////////////////////////////////////////
                            // this commented paragraph was used for receiving data via TCP
                            /*if (stream.DataAvailable)
                            {
                                int length;
                                // Read incomming stream into byte arrary.
                                if ((length = stream.Read(bytes, 0, bytes.Length)) != 0)
                                {
                                    var incommingData = new byte[length];
                                    Array.Copy(bytes, 0, incommingData, 0, length);
                                    // Convert byte array to string message.
                                    string clientMessage = Encoding.ASCII.GetString(incommingData);
                                    clientMessage = clientMessage.TrimEnd('\r', '\n');
                                    Debug.Log("client message received as: " + clientMessage + ".");

                                    if (clientMessage.Equals(CLOSE_CONNECTION))
                                    {
                                        clientConnected = false;
                                        connectedTcpClient.Close();
                                    }
                                    else
                                    {
                                        receivedNumbers.Add(clientMessage);
                                    }
                                }
                            }//*/
                        }
                        Debug.Log("Client disconnected");
                    }
                }
            }
        }
        catch (SocketException socketException)
        {
            Debug.Log("SocketException " + socketException.ToString());
        }
    }

    /** sends a given message to the client via TCP
     */
    private void sendMessage(string msg)
    {
        if (connectedTcpClient == null)
        {
            Debug.Log("error - no tcp client");
            return;
        }

        try
        {
            // Get a stream object for writing. 			
            NetworkStream stream = connectedTcpClient.GetStream();
            if (stream.CanWrite)
            {
                string serverMessage = msg;
                // Convert string message to byte array.                 
                byte[] serverMessageAsByteArray = Encoding.ASCII.GetBytes(serverMessage);
                // Write byte array to socketConnection stream.               
                stream.Write(serverMessageAsByteArray, 0, serverMessageAsByteArray.Length);
                Debug.Log("Server sent his message - should be received by client");
            }
        }
        catch (SocketException socketException)
        {
            Debug.Log("Socket exception: " + socketException);
        }
    }

    /** ensures that the TCP connection is closed when server is stopped
     */
    private void OnDestroy()
    {
        if (connectedTcpClient != null)
        {
            connectedTcpClient.Close();
        }
        tcpListenerThread.Abort();
    }
}