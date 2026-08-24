package fr.insalyon.creatis.vip.core.server.business;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import fr.insalyon.creatis.vip.core.client.view.CoreConstants;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.insalyon.creatis.vip.core.client.VipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CoreUtil {
    
    private static final Logger log = LoggerFactory.getLogger(CoreUtil.class);

    /*
        remove accents and non-ascii characters
    */
    public static String getCleanStringAscii(String s, String replacement) {
        return getCleanString(s, replacement, true, false);
    }

    public static String getCleanStringAlnum(String s, String replacement) {
        return getCleanString(s, replacement, false, true);
    }

    public static void assertOnlyLatin1Characters(String s) throws VipException {
        String nonLatin1Char = filterNonLatin1Characters(s);
        if ( ! nonLatin1Char.isEmpty()) {
            throw new VipException("Non-valid characters : [" + nonLatin1Char + "] (in string \"" + s + "\" )");
        }
    }

    public static String filterNonLatin1Characters(String s) {
        // remove latin1 characters to only keep non-latin1 ones
        return s.replaceAll("[\\p{InBasicLatin}\\p{InLatin-1Supplement}]", "");
    }

    private static String getCleanString(String s, String replacement, boolean onlyKeepAscii, boolean onlyKeepAlnum) {
        // Normalizer.normalize with NFKD form decompose accentuated
        // letters into separate "accent mark + base letter" characters
        s = Normalizer.normalize(s, Normalizer.Form.NFKD);

        if (onlyKeepAscii) {
            // the [^\\p{ASCII}] regex remove all non-ascii characters
            // so also the separated accents char if removeAccents is true
            s = s.replaceAll("[^\\p{ASCII}]", replacement);
        } else if (onlyKeepAlnum) {
            s = s.replaceAll("[^a-zA-Z0-9]", replacement);
        }
        return s;
    }

    public static String createUUID() {
        final String alphabet = CoreConstants.UUID_ALPHABET;
        final SecureRandom random = new SecureRandom();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CoreConstants.UUID_SIZE; i++) {
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        return sb.toString();
    }

    static public class CommandErrorException extends Exception {

        private final List<String> cout;

        public CommandErrorException(List<String> cout) {
            this.cout = cout;
        }

        public String getFirstLine() {
            return cout != null && !cout.isEmpty() ? cout.getFirst() : "<No output>";
        }

        public String getOutput() {
            return cout == null ? "<No output>" : String.join("\n", cout);
        }
    }

    static public List<String> runCommand(String... command) throws CommandErrorException, VipException {
        return runCommand(Arrays.asList(command));
    }

    static public List<String> runCommand(List<String> command) throws CommandErrorException, VipException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = null;
        List<String> cout = new ArrayList<>();

        try {
            log.info("Executing command : {}", command);
            process = builder.start();

            try (BufferedReader reader = process.inputReader()) {
                cout = reader.lines().toList();
            }
            process.waitFor();
        } catch (IOException | InterruptedException e) {
            log.error("Unexpected error in a boutiques command : {}",
                    String.join("\n", cout), e);
            throw new VipException("Unexpected error in a boutiques command", e);
        } finally {
            if (process != null) {
                process.destroy();
            }
        }

        if (process.exitValue() != 0) {
            log.error("Command failed : {}", String.join("\n", cout));
            throw new CommandErrorException(cout);
        }
        return cout;
    }

    public static SAXParserFactory getSecureSAXParserFactory() throws VipException  {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        parserFactory.setNamespaceAware(true);
  
        try {
            // https://docs.semgrep.dev/cheat-sheets/java-xxe#3-c-saxparserfactory
            parserFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

        } catch (ParserConfigurationException | SAXNotRecognizedException | SAXNotSupportedException e) {
            log.error("The SAX parser does not support some XXE security features: {}", e.getMessage(), e);
            throw new VipException("The SAX parser does not support some XXE security features", e);
        }
        return parserFactory;
    }
}
