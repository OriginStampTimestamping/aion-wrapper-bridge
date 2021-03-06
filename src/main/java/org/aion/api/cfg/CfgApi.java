package org.aion.api.cfg;

import javax.xml.stream.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

public class CfgApi extends Cfg {

    private CfgApi() {
        this.secureConnectEnabled = false;

        // TODO :: test and open it later.
        // this.connect = new CfgConnect();

        if (!fromXML()) {
            toXML(null);
        }
    }

    public static CfgApi inst() {
        return ApiCfgHolder.inst;
    }

    public CfgConnect getConnect() {
        return this.connect;
    }

    public void setConnect(CfgConnect _cnt) {
        this.connect = _cnt;
    }

    public boolean isSecureConnectEnabled() {
        return this.secureConnectEnabled;
    }

    private static class ApiCfgHolder {
        private static final CfgApi inst = new CfgApi();
    }

    @Override
    public boolean fromXML() {
        File cfgFile = new File(CONF_FILE_PATH);
        if (!cfgFile.exists()) return false;
        XMLInputFactory input = XMLInputFactory.newInstance();
        FileInputStream fis;
        try {
            fis = new FileInputStream(cfgFile);
            XMLStreamReader sr = input.createXMLStreamReader(fis);
            loop:
            while (sr.hasNext()) {
                int eventType = sr.next();
                switch (eventType) {
                    case XMLStreamReader.START_ELEMENT:
                        String elementName = sr.getLocalName().toLowerCase();
                        switch (elementName) {
                            case "log":
                                break;
                            case "connect":
                                // this.connect.fromXML(sr);
                                break;
                            case "secure-connect":
                                secureConnectEnabled = Boolean.parseBoolean(Cfg.readValue(sr));
                                break;
                            default:
                                // skipElement(sr);
                                break;
                        }
                        break;
                    case XMLStreamReader.END_ELEMENT:
                        if (sr.getLocalName().toLowerCase().equals("aion_api")) break loop;
                        else break;
                }
            }
            closeFileInputStream(fis);
        } catch (Exception e) {
            System.out.println("<error on-parsing-config-xml msg=" + e.getLocalizedMessage() + ">");
            System.exit(1);
        }
        return true;
    }

    @Override
    public void toXML(final String[] args) {

        //        if (args != null) {
        //            boolean override = false;
        //            for (String arg : args) {
        //                arg = arg.toLowerCase();
        //                if (override)
        //                    System.out.println("Config Override");
        //            }
        //        }

        XMLOutputFactory output = XMLOutputFactory.newInstance();
        output.setProperty("escapeCharacters", false);
        XMLStreamWriter sw = null;

        try {

            File file = new File(CONF_FILE_PATH);
            file.getParentFile().mkdirs();

            sw = output.createXMLStreamWriter(new FileWriter(file));
            sw.writeStartDocument("utf-8", "1.0");
            sw.writeCharacters("\r\n");
            sw.writeStartElement("aion_api");

            sw.writeCharacters("\r\n");
            sw.writeCharacters("\t");
            sw.writeStartElement("secure-connect");
            sw.writeCharacters("true");
            sw.writeEndElement();
            sw.writeCharacters("\r\n");

            // sw.writeCharacters(this.getConnect().toXML());

            sw.writeCharacters("\r\n");
            sw.writeEndElement();
            sw.flush();
            sw.close();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("<error on-write-config-xml-to-file>");
            System.exit(1);
        } finally {
            if (sw != null) {
                try {
                    sw.close();
                } catch (XMLStreamException e) {
                    System.out.println("<error on-close-stream-writer>");
                    System.exit(1);
                }
            }
        }
    }

    private void closeFileInputStream(final FileInputStream fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                System.out.println("<error on-close-file-input-stream>");
                System.exit(1);
            }
        }
    }
}
