package fr.insalyon.creatis.vip.datamanager.server;

import fr.insalyon.creatis.vip.core.models.Group;
import fr.insalyon.creatis.vip.core.server.business.CoreUtil;
import java.io.File;
import java.net.URI;
import java.util.*;

public class DataManagerUtil {

    public static String extractName(String lfcDir) {
        return lfcDir.substring(lfcDir.lastIndexOf("/") + 1);
    }

     public static List<String> getPaths(List<String> groups){
        ArrayList<String> paths = new ArrayList<>();
        for(String s : groups)
            paths.add(s.replaceAll(" ", "_"));
        return paths;
    }

    public static List<String> getPaths(Set<Group> groups){
        ArrayList<String> paths = new ArrayList<>();
        return groups.stream()
                .map(g -> g.getName().replace(" ", "_"))
                .toList();
    }

    /*
        remove spaces, accents and non-ascii characters
     */
    public static String getCleanFilename(String fileName) {
        fileName = new File(fileName).getName().trim().replaceAll(" ", "_");
        return CoreUtil.getCleanStringAscii(fileName, "");
    }

    public static String selectUriQueries(URI uri, String... parametersToKeep) {
        if (uri.getQuery() == null || uri.getQuery().isEmpty()) {
            return uri.toString();
        }
        
        List<String> whitelist = java.util.Arrays.asList(parametersToKeep);
        String newQuery = java.util.Arrays.stream(uri.getQuery().split("&"))
                .filter(q -> {
                    int index = q.indexOf("=");
                    return index > 0 && whitelist.contains(q.substring(0, index));
                })
                .collect(java.util.stream.Collectors.joining("&"));
        
        String uriStr = uri.toString();
        int sep = uriStr.indexOf("?");
        String base = (sep == -1) ? uriStr : uriStr.substring(0, sep);
        
        return newQuery.isEmpty() ? base : base + "?" + newQuery;
    }
}
