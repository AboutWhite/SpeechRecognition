using System;
using System.Collections;
using System.Collections.Generic;
using UnityEngine;
using Assets.LSL4Unity.Scripts;
using Assets.LSL4Unity.Scripts.AbstractInlets;

public class ServerStringInlet : InletStringSamples
{
    public GameObject observerObject;
    private OnNumberRequested observer = null;

    private bool pullSamplesContinuously = false;

    // Use this for initialization
    void Start()
    {
        checkObserver();
    }

    // Update is called once per frame
    void Update()
    {
        if (pullSamplesContinuously)
            pullSamples();
    }

    public void startInlet()
    {
        registerAndLookUpStream();
        Debug.Log("LSL initialized");
    }

    protected override bool isTheExpected(LSLStreamInfoWrapper stream)
    {
        bool predicate = base.isTheExpected(stream);
        // maybe more checks here

        return predicate;
    }

    /**
     * receives the sample
     * notifies the observer
     */
    protected override void Process(String[] newSample, double timeStamp)
    {
        Debug.Log(newSample[0] + " " + timeStamp);
        notifyObserver(newSample[0]);
    }

    protected override void OnStreamAvailable()
    {
        pullSamplesContinuously = true;
        Debug.Log("Stream is now available");
    }

    protected override void OnStreamLost()
    {
        pullSamplesContinuously = false;
        Debug.Log("Stream connection lost");
    }

    private void notifyObserver(string s)
    {
        observer.notify(s);
    }

    private void checkObserver()
    {
        if(observerObject == null)
        {
            throw new NullReferenceException("There is no observer registered for the inlet");
        }

        observer = observerObject.transform.GetComponent<OnNumberRequested>();

        if (observer == null)
        {
            throw new NullReferenceException("The registered observer doesn't implement OnNumberRequested interface");
        }
    }
}
