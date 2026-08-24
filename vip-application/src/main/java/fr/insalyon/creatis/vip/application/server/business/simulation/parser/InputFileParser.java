package fr.insalyon.creatis.vip.application.server.business.simulation.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insalyon.creatis.vip.core.client.VipException;
import fr.insalyon.creatis.vip.core.models.Triplet;
import fr.insalyon.creatis.vip.datamanager.client.view.DataManagerException;
import fr.insalyon.creatis.vip.datamanager.server.business.LfcPathsBusiness;
import fr.insalyon.creatis.vip.core.server.business.CoreUtil;

/**
 * Parse a m2 input file.
 *
 * This stores data in fields and this is not threadsafe. So it cannot be used
 * as a spring singleton and this needs prototype scope.
 *
 */
@Service
@Scope("prototype")
public class InputFileParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String currentUserFolder;
    private LfcPathsBusiness lfcPathsBusiness;
    private Map<String, String> inputs = new HashMap<>();

    @Autowired
    public final void setLfcPathsBusiness(LfcPathsBusiness lfcPathsBusiness) {
        this.lfcPathsBusiness = lfcPathsBusiness;
    }

    public InputFileParser() {
        this(null);
    }

    public InputFileParser(String currentUserFolder) {
        this.currentUserFolder = currentUserFolder;
    }

    // If the filePath do not contain an extension, then JSON and XML will be tested
    // the first founded file will be used
    public List<Map<String, String>> parse(Path path) throws VipException {
        String ext = FilenameUtils.getExtension(path.toString());

        if (ext.isEmpty()) {
            for (String candidateExt : List.of("json", "xml")) {
                Path candidate = path.resolveSibling(path.getFileName() + "." + candidateExt);
                if (candidate.toFile().exists()) {
                    path = candidate;
                    ext = candidateExt;
                    break;
                }
            }
        }

        switch (ext) {
            case "xml":
                return handleXML(path.toFile());
            case "json":
                return handleJSON(path.toFile());
            default:
                throw new VipException("Cannot find inputs file at this location: " + path);
        }
    }
    public List<Map<String, String>> handleXML(File file) throws VipException {
        try {
            XMLHandler handler = new XMLHandler();
            SAXParserFactory parserFactory = CoreUtil.getSecureSAXParserFactory();

            XMLReader reader = parserFactory.newSAXParser().getXMLReader();
            reader.setContentHandler(handler);
            reader.parse(new InputSource(new FileReader(file)));

            return List.of(inputs);
        } 
        catch (IOException | SAXException | ParserConfigurationException e) {
            logger.error("Error parsing {}", file.getName(), e);
            throw new VipException(e);
        }
    }
    public List<Map<String, String>> handleJSON(File file) throws VipException {
        ObjectMapper mapper = new ObjectMapper();
        List<Map<String, String>> inputsData = new ArrayList<>();

        try {
            JsonNode root = mapper.readTree(file);
            List<Map<String, List<String>>> inputMaps;
            if (root.isArray()) {
                // List of input maps format (API/Executions)
                inputMaps = mapper.convertValue(root, new TypeReference<>() {});
            } else if (root.isObject()) {
                // Single input map format (UI/Simulations)
                Map<String, List<String>> data = mapper.convertValue(root, new TypeReference<>() {});
                inputMaps = List.of(data);
            }
            else {
                throw new IOException("Invalid JSON format");
            }
            // Create result List of input maps, keep same amount of maps and apply PathHandler/List logic to their values

            for (Map<String, List<String>> inputMap : inputMaps) {
                Map<String, String> parsed = new HashMap<>();
                inputMap.forEach((name, items) -> {
                    // Filter out  empty strings
                    List<String> filteredItems = items.stream()
                            .filter(item -> item != null && !item.isEmpty())
                            .toList();

                    if (filteredItems.isEmpty()) {
                        return;
                    }

                    if (filteredItems.size() == 1) {
                        parsed.put(name, pathHandler(filteredItems.getFirst()));
                    } else {
                        parsed.put(name, handleList(filteredItems));
                    }
                });

                inputsData.add(parsed);
            }
        } catch (IOException e) {
            logger.error("Error parsing {}", file.getName(), e);
            throw new VipException(e);
        }

        return inputsData;
    }

    private String pathHandler(String path) {
        try {
            if (lfcPathsBusiness != null) {
                path = lfcPathsBusiness.parseRealDir(path, currentUserFolder);
            }
        } catch (DataManagerException ex) {
            // do nothing
        }
        return path;
    }

    private Triplet<Double, Double, Double> getStartStopStep(List<String> values) {
        double step = -1;
        double start = -1;
        double stop = -1;

        for (String v : values) {
            try {
                double value = Double.valueOf(v);

                if (start == -1) {
                    start = value;

                } else if (stop == -1) {
                    stop = value;
                    step = stop - start;

                } else {
                    if (step != (value - stop)) {
                        return null;
                    } else {
                        stop = value;
                    }
                }
            } catch (NumberFormatException ex) {
                return null;
            }
        }
        return new Triplet<Double, Double, Double>(start, stop, step);
    }

    private String formatListString(List<String> items) {
        return items.stream()
                .map(this::pathHandler)
                .collect(Collectors.joining("; "));
    }

    private String handleList(List<String> items) {
        Triplet<Double, Double, Double> startStopStep = getStartStopStep(items);
        if (startStopStep == null) {
            return formatListString(items);
        } else {
            return "Start: " + startStopStep.getFirst() + " - Stop: "
                    + startStopStep.getSecond() + " - Step: " + startStopStep.getThird();
        }
    }

    class XMLHandler extends DefaultHandler {
        private boolean parsingItem = false;
        private List<String> values;
        private String name;
        private StringBuilder itemContent;

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {

            if (localName.equals("source")) {
                name = attributes.getValue("name");
                values = new ArrayList<String>();

            } else if (localName.equals("item")) {
                parsingItem = true;
                itemContent = new StringBuilder();
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if (localName.equals("source")) {
                if (values.size() == 1) {
                    String path = values.get(0);
                    inputs.put(name, pathHandler(path));

                } else {
                    inputs.put(name, handleList(values));
                }
            } else if (localName.equals("item")) {
                values.add(itemContent.toString().trim());
                itemContent = null;
                parsingItem = false;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            if (parsingItem) {
                itemContent.append(ch, start, length);
            }
        }
    }
}
