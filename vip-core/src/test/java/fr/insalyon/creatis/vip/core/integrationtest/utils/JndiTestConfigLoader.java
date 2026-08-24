package fr.insalyon.creatis.vip.core.integrationtest.utils;

import java.io.IOException;
import java.util.Properties;

import fr.insalyon.creatis.vip.core.integrationtest.database.SpringJndiIT;

/**
 * This util was designed to avoid simple-jndi to log 
 * ugly "ERROR org.osjava.sj.SimpleJndi - Unable to load" messages in others modules.
 * 
 * By default simple-jndi try to load the file `jndi.properties` if 
 * the library is present in the classpath (even if you do not use it)!
 * This is the issue that we fight against.
 * 
 * To avoid that, we can manually load the a properties file 
 * with a different name into the module that need jndi support.
 * This is not very beaufitul in term of code but it works and avoids ugly messages.
 */
public class JndiTestConfigLoader {
    
    public static void load() throws IOException {
        Properties properties = new Properties();

        properties.load(SpringJndiIT.class.getResourceAsStream("/test-jndi.properties"));
        properties.forEach((k, v) -> {
            System.setProperty(k.toString(), v.toString());
        });
    }
}
