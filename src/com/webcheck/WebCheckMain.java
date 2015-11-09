package com.webcheck;

import com.webcheck.models.JobModel;
import java.awt.Desktop;
import static java.awt.Desktop.getDesktop;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import static java.lang.System.out;
import java.net.URI;
import java.util.ArrayList;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import static javax.xml.parsers.DocumentBuilderFactory.newInstance;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import static org.w3c.dom.Node.ELEMENT_NODE;
import org.w3c.dom.NodeList;

/**
 *
 * @author peterhendriks
 */
public class WebCheckMain
{
    //Getters and setters...

    public Boolean getRunning()
    {
        return running;
    }

    /**
     * Providing the scheduling time in seconds
     *
     * @param interval
     */
    public void setInterval(int interval)
    {
        this.interval = interval * 1000;
    }
    /**
     * Privat Objects
     */
//    private ArrayList jobNames;
//    private ArrayList targetURLS;
//    private ArrayList elements;
//    private ArrayList attributes;
//    private ArrayList chars;

    private ArrayList<JobModel> jobs;
    private int interval;
    private Boolean running;

    /**
     * Making this class runnable... Please comment this when instantiating this
     * class...
     *
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            setLookAndFeel(getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e)
        {
            e.printStackTrace();
        }
        WebCheckMain dispatcher = new WebCheckMain();
        dispatcher.Start();
    }

    public WebCheckMain()
    {
        LoadConfig();
        LoadJobs();
    }

    private void LoadConfig()
    {
        try
        {
            DocumentBuilderFactory docBuilderFactory = newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File("config/config.xml"));
            doc.getDocumentElement().normalize();
            NodeList config = doc.getElementsByTagName("config");
            for (int i = 0; i < config.getLength(); i++)
            {
                Node firstConfigNode = config.item(i);
                if (firstConfigNode.getNodeType() == ELEMENT_NODE)
                {
                    Element firstConfigElement = (Element) firstConfigNode;
                    NodeList runningList = firstConfigElement.getElementsByTagName("running");
                    Element firstRunningElement = (Element) runningList.item(0);
                    NodeList intervalList = firstConfigElement.getElementsByTagName("interval");
                    Element firstIntervalElement = (Element) intervalList.item(0);
                    running = Boolean.parseBoolean(firstRunningElement.getChildNodes().item(0).getNodeValue());
                    interval = Integer.parseInt(firstIntervalElement.getChildNodes().item(0).getNodeValue()) * 1000;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void LoadJobs()
    {
//        jobNames = new ArrayList();
//        targetURLS = new ArrayList();
//        elements = new ArrayList();
//        attributes = new ArrayList();
//        chars = new ArrayList();
        jobs = new ArrayList<>();
        try
        {
            DocumentBuilderFactory docBuilderFactory = newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File("config/config.xml"));

            // normalize text representation
            doc.getDocumentElement().normalize();
            NodeList jobNodes = doc.getElementsByTagName("job");
            int totalJobs = jobNodes.getLength();
            StringBuilder sb = new StringBuilder("\nConfiguration:\n               Jobs #: ");
            sb.append(totalJobs);
            sb.append("\n               Interval: ");
            sb.append(interval / 1000);
            sb.append(" sec.");
            sb.append("\n               Running: ");
            sb.append(running);
            out.println(sb.toString());
            for (int i = 0; i < jobNodes.getLength(); i++)
            {
                Node firstJobNode = jobNodes.item(i);
                if (firstJobNode.getNodeType() == ELEMENT_NODE)
                {
                    Element firstJobElement = (Element) firstJobNode;

                    NodeList nameList = firstJobElement.getElementsByTagName("name");
                    Element firstNameElement = (Element) nameList.item(0);

                    NodeList urlList = firstJobElement.getElementsByTagName("url");
                    Element firstUrlElement = (Element) urlList.item(0);

                    NodeList elementList = firstJobElement.getElementsByTagName("element");
                    Element firstElementElement = (Element) elementList.item(0);

                    NodeList attributeList = firstJobElement.getElementsByTagName("attribute");
                    Element firstAttributeElement = (Element) attributeList.item(0);

                    NodeList queryList = firstJobElement.getElementsByTagName("query");
                    Element firstQueryElement = (Element) queryList.item(0);

                    jobs.add(new JobModel(firstNameElement.getChildNodes().item(0).getNodeValue(),
                            firstUrlElement.getChildNodes().item(0).getNodeValue(),
                            firstElementElement.getChildNodes().item(0).getNodeValue(),
                            firstAttributeElement.getChildNodes().item(0).getNodeValue(),
                            firstQueryElement.getChildNodes().item(0).getNodeValue()));
                }

            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Starting a threaded scrape of all targetURLS
     */
    public void Start()
    {
        ArrayList results = new ArrayList();
        while (running)
        {
            try
            {
                jobs.stream().forEach(job ->
                {
                    ArrayList buf = new Scraper().getContent(job.getTargetURL(), job.getElement(), job.getAttribute(), job.getQuery());
                    buf.stream().forEach(itemFond ->
                    {
                        results.add(itemFond);
                    });
                    SaveNewItems(job.getJobName(), job.getTargetURL(), results);
                    results.clear();
                });
                Thread.sleep(interval);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            LoadConfig();
            LoadJobs();
        }
    }

    public void Stop()
    {
        running = false;
    }

    private void SaveNewItems(String currentJobName, String url, ArrayList foundItems)
    {
        StringBuilder knownItems = new StringBuilder();
        StringBuilder newItems = new StringBuilder();
        try
        {
            File dataFile = new File("config/data.txt");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(dataFile), "UTF-8")))
            {
                String buf = null;
                while ((buf = br.readLine()) != null)
                {
                    knownItems.append(buf);
                }
            }
            foundItems.stream().filter((foundItem) -> (!knownItems.toString().contains(foundItem.toString()))).map((foundItem) ->
            {
                newItems.append(foundItem.toString());
                return foundItem;
            }).forEach((_item) ->
            {
                newItems.append("\n");
            });
            if (!newItems.toString().isEmpty())
            {
                int n = showConfirmDialog(null, "Do you want to view the following new items now?\n\n" + newItems.toString(), "New Items found for Job: '" + currentJobName + "'", YES_NO_OPTION);
                if (n == 0)
                {
                    URI uri = new URI(url);
                    Desktop desktop = getDesktop();
                    desktop.browse(uri);

                }
                FileOutputStream fos = new FileOutputStream(dataFile, true);
                try (Writer out = new OutputStreamWriter(fos, "UTF8"))
                {
                    out.write(newItems.toString());
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
