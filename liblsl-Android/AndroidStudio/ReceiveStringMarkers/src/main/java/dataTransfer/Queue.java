package dataTransfer;

import java.util.ArrayList;

public class Queue extends Thread {
    private ArrayList<String> list = new ArrayList<>();


    @Override
    public void run()
    {

    }

    public void addToList(String s)
    {
        list.add(s);
    }

    public String getFirstObjectFromList()
    {
        if(list.size() > 0)
        {
            String s = list.get(0);
            list.remove(0);
            return s;
        }

        return null;
    }
}
