package biz.an_droid.riftevents.api;

import java.util.ArrayList;

/**
 * Created by alex (alexzkhr@gmail.com) on 11/10/17.
 * At 04:37
 */
public class EventWraper
{
    private String        status = "failed";
    private ArrayList<ServerEvent> data;

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public ArrayList<ServerEvent> getData()
    {
        return data;
    }

    public void setData(ArrayList<ServerEvent> data)
    {
        this.data = data;
    }

    public boolean isSuccess()
    {
        return "success".equals(getStatus());
    }
}
