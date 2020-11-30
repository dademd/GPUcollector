package com.company;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {

    static class Process{

        public Process(String pid, String usedMemory){
            this.pid = pid;
            this.usedMemory = usedMemory;
        }

        String pid;
//        in Mib
        String usedMemory;

        @Override
        public String toString() {
            return "Process{" +
                    "pid='" + pid + '\'' +
                    ", usedMemory='" + usedMemory + '\'' +
                    '}';
        }
    }

    public static ArrayList<Process> parse() throws ParserConfigurationException, IOException, SAXException {
        // Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Build Document
        Document document = builder.parse(new File("src/com/company/data.xml"));

        // Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

        // Here comes the root node
        Element root = document.getDocumentElement();
        System.out.println(root.getNodeName());

        // Get all employees
        NodeList proc_infos = document.getElementsByTagName("process_info");
        System.out.println("============================");

        var processes = new ArrayList<Process>();

        for(int proc_info_idx = 0; proc_info_idx < proc_infos.getLength(); ++proc_info_idx){
            Node node = proc_infos.item(proc_info_idx);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element proc_info = (Element) node;

                String used_memory = proc_info.getElementsByTagName("used_memory").item(0).getTextContent();
                used_memory = used_memory.substring(0, used_memory.length() - 4);

                String pid = proc_info.getElementsByTagName("pid").item(0).getTextContent();

                processes.add(new Process(pid, used_memory));
            }
        }

        return processes;
    }

    public static void writeToDB(){
//        do something with list of process
//        parse();
    }

    public static void loop() throws InterruptedException, IOException, ParserConfigurationException, SAXException {
        String cwd = System.getProperty("user.dir") + "/src/com/company/data.xml";
        for (int i = 0; i < 20; i++){
            String command = "nvidia-smi -q -x | sed 2,2d > '" + cwd + "'";
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
            java.lang.Process process = processBuilder.start();

//            Let current thread sleep until process terminates
            process.waitFor();

            System.out.println(parse());
//            writeToDB();

//            Sleep between sessions
            Thread.sleep(2000);
        }

    }

    public static void main(String[] args) throws InterruptedException, ParserConfigurationException, SAXException, IOException {
        loop();
//        System.out.println();
    }
}
