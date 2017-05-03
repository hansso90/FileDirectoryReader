package nl.lemkes.example;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by hans on 3-5-2017.
 */
public class Main {

    private static final String FILENAME= "featuretype.xml";

    private static final String SETTINGS_FILE = "application.properties";

    private File serviceDir;

    public static void main(String[] args) throws Exception
    {
        new Main().start();
    }

    /**
     *
     * @throws Exception
     */
    public void start() throws Exception
    {
        final Properties properties = new Properties();
        try (final InputStream stream = getClass().getClassLoader().getResourceAsStream(SETTINGS_FILE)) {
            properties.load(stream);
        }

        String datadir     = properties.getProperty("datadir");
        String service      = properties.getProperty("service");

        setServiceDir(datadir, service);
        getFilesEnVoerActieUit();
    }

    /**
     *
     * @param datadir
     * @param service
     */
    private void setServiceDir(String datadir, String service)
    {
        String filePath = datadir + "workspaces/"+ service + "/" + service + "/";
        File file = new File(filePath);
        if(file.exists() && file.isDirectory())
        {
            serviceDir = file;
            return;
        }
        throw new IllegalStateException("Service dir is geen directory: " +filePath);
    }

    /**
     *
     * @param fileList
     * @param directory
     * @return
     */
    public static List<String> listf(List<String> fileList, File directory) {

        // get all the files from a directory
        File[] fList = directory.listFiles();
        for (File file : fList) {
            if (file.isFile() && file.getName().equals(FILENAME)) {
                fileList.add(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                listf(fileList, file);
            }
        }
        return fileList;
    }

    private void getFilesEnVoerActieUit() throws Exception {
        List<String> featureTypes = listf(new ArrayList<String>(), serviceDir);

        featureTypes.forEach(s -> changeXml(s));
    }


    public void changeXml(String filepath)
    {
        System.out.println("Start with: " + filepath);
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(filepath);

            Node nativeBoundingBox = doc.getElementsByTagName("nativeBoundingBox").item(0);

            getDirectChild((Element) nativeBoundingBox, "minx").getFirstChild().setNodeValue("0");
            getDirectChild((Element) nativeBoundingBox, "maxx").getFirstChild().setNodeValue("1");
            getDirectChild((Element) nativeBoundingBox, "miny").getFirstChild().setNodeValue("2");
            getDirectChild((Element) nativeBoundingBox, "maxy").getFirstChild().setNodeValue("3");

            Node latLonBoundingBox = doc.getElementsByTagName("latLonBoundingBox").item(0);
            getDirectChild((Element) latLonBoundingBox, "minx").getFirstChild().setNodeValue("10");
            getDirectChild((Element) latLonBoundingBox, "maxx").getFirstChild().setNodeValue("11");
            getDirectChild((Element) latLonBoundingBox, "miny").getFirstChild().setNodeValue("12");
            getDirectChild((Element) latLonBoundingBox, "maxy").getFirstChild().setNodeValue("13");

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filepath));
            transformer.transform(source, result);

            System.out.println("Done with: " + filepath);

        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        }
    }

    public static Element getDirectChild(Element parent, String name)
    {
        for(Node child = parent.getFirstChild(); child != null; child = child.getNextSibling())
        {
            if(child instanceof Element && name.equals(child.getNodeName()))
            {
                System.out.println("Direct child met naam: " + name + " gevonden en aangepast");
                return (Element) child;
            }
        }
        return null;
    }

}
