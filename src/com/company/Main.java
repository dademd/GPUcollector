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

//    A class for storing information about processes
    static class Process{

        public Process(String pid, int usedMemory){
            this.pid = pid;
            this.usedMemory = usedMemory;
        }
//        process id
        String pid;
//        used gpu memory in MiB
        int usedMemory;

        @Override
        public String toString() {
            return "Process{" +
                    "pid='" + pid + '\'' +
                    ", usedMemory='" + usedMemory + '\'' +
                    '}';
        }
    }


    public static void run_nvidia() throws IOException, InterruptedException {
//        get current working directory. Should be modified for a new porject
        String cwd = System.getProperty("user.dir") + "/src/com/company/data.xml";
//        prepare bash command
        String command = "nvidia-smi -q -x | sed 2,2d > '" + cwd + "'";
//        create a process
        ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", command);
//        start execution of command
        java.lang.Process process = processBuilder.start();
//        Let current process sleep until another process terminates
//        this is for preventing race condition with process writing into file
//        and parse() reading from it
        process.waitFor();
    }

//    save all process infos in a list
    public static ArrayList<Process> parse() throws ParserConfigurationException, IOException, SAXException {
//        Get Document Builder
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

//        Build Document
        Document document = builder.parse(new File("src/com/company/data.xml"));

//        Normalize the XML Structure; It's just too important !!
        document.getDocumentElement().normalize();

//        Here comes the root node
        Element root = document.getDocumentElement();
//        System.out.println(root.getNodeName());

//        Get all processes's information nodelist
        NodeList proc_infos = document.getElementsByTagName("process_info");

//        list that stores all processes' information
        var processes = new ArrayList<Process>();

//        for each node in found process
        for(int proc_info_idx = 0; proc_info_idx < proc_infos.getLength(); ++proc_info_idx){

            Node node = proc_infos.item(proc_info_idx);
//            if it is an element node
            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element proc_info = (Element) node;
//                get used memory in format 12 MiB
                String used_memory_str = proc_info.getElementsByTagName("used_memory").item(0).getTextContent();
//                truncate, extract and save integer part
                int used_memory = Integer.parseInt(used_memory_str.substring(0, used_memory_str.length() - 4));
//                get pid of a process
                String pid = proc_info.getElementsByTagName("pid").item(0).getTextContent();
//                add new process information to the list
                processes.add(new Process(pid, used_memory));
            }
        }
//        return list with all processses' information
        return processes;
    }

//    Here, one can write all processes to database
    public static void writeToDB() throws IOException, SAXException, ParserConfigurationException, InterruptedException {
        run_nvidia();
        var proccessInfos = parse();
//        do something with list of process
    }

//    a loop for testing
    public static void loop() throws InterruptedException, IOException, ParserConfigurationException, SAXException {
        for (int i = 0; i < 20; i++){
            run_nvidia();
//            parse();
            System.out.println(parse());
//            Sleep between sessions
            Thread.sleep(2000);
        }
    }

    public static void main(String[] args) throws InterruptedException, ParserConfigurationException, SAXException, IOException {
        loop();
    }
}
