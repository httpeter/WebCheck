/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.webcheck.models;

/**
 *
 * @author PeterH
 */
public class JobModel
{

    private String jobName,
            targetURL,
            element,
            attribute,
            query;

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public String getJobName()
    {
        return jobName;
    }

    public void setJobName(String jobName)
    {
        this.jobName = jobName;
    }

    public String getTargetURL()
    {
        return targetURL;
    }

    public void setTargetURL(String targetURL)
    {
        this.targetURL = targetURL;
    }

    public String getElement()
    {
        return element;
    }

    public void setElement(String element)
    {
        this.element = element;
    }

    public String getAttribute()
    {
        return attribute;
    }

    public void setAttribute(String attribute)
    {
        this.attribute = attribute;
    }

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }
//</editor-fold>

    public JobModel(String jobName, String targetURL, String element, String attribute, String query)
    {
        this.jobName = jobName;
        this.targetURL = targetURL;
        this.element = element;
        this.attribute = attribute;
        this.query = query;
    }

}
