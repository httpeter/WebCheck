package com.webcheck;

import java.io.InputStream;
import static java.lang.System.out;
import java.net.URL;
import static java.text.Normalizer.Form.NFD;
import static java.text.Normalizer.normalize;
import java.util.ArrayList;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

/**
 *
 * @author peterhendriks
 */
public class Scraper
{

    private Tidy tidy;

    /**
     * Scrape!
     */
    public Scraper()
    {
        tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
    }

    /**
     * Retrieving the content from the website specified.
     *
     * @param url
     * @param xmlElement
     * @param xmlAttribute
     * @param filterChars
     * @return
     */
    public ArrayList getContent(String url, String xmlElement, String xmlAttribute, String filterChars)
    {
        ArrayList result = new ArrayList();
        try
        {
//            tidy.setXHTML(true);            
            tidy.setInputEncoding("UTF-8");
            tidy.setOutputEncoding("UTF-8");
            InputStream inputStream = new URL(url).openStream();
            Document doc = tidy.parseDOM(inputStream, null);
            NodeList elements = doc.getElementsByTagName(xmlElement);

            Say("\n\nScanning URL: ");
            Say(url);
            Say("\n");
            for (int i = 0; i < elements.getLength(); i++)
            {
                //If the Attribute is empty, the Element value is returned!
                if (xmlAttribute.equals("") || xmlAttribute.equals(" "))
                {
                    String elementValue = elements.item(i).getChildNodes().item(0).getNodeValue();
                    if (elementValue.contains(filterChars))
                    {
                        result.add(normalize(elementValue, NFD).replaceAll("[^\\p{ASCII}�]", ""));
                    }
                } //Apparently the Attribute is not empty, returning  it's value
                else
                {
                    String attributeValue = elements.item(i).getAttributes().getNamedItem(xmlAttribute).getNodeValue();
                    if (attributeValue.contains(filterChars))
                    {
                        result.add(normalize(attributeValue, NFD).replaceAll("[^\\p{ASCII}�]", ""));
                    }
                }
            }
        } catch (Exception e)
        {
            //e.printStackTrace();
        }
        return result;
    }

    private void Say(String msg)
    {
        out.print(msg);
    }
}
