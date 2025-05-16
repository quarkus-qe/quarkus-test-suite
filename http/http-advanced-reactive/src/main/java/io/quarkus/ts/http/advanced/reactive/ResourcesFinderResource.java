package io.quarkus.ts.http.advanced.reactive;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/resource-finder")
@ApplicationScoped
public class ResourcesFinderResource {

    @GET
    @Path("/find")
    public String findResources(@QueryParam("resourcePath") String resourcePath,
            @QueryParam("lookupPattern") String lookupPattern) throws URISyntaxException, IOException {
        ClassLoader classLoader = this.getClass().getClassLoader();

        List<URL> result = new ArrayList<>();
        Enumeration<URL> resources = classLoader.getResources(resourcePath);
        while (resources.hasMoreElements()) {
            URL url = resources.nextElement();
            if (isJarURL(url)) {
                getMatchingResourcesInsideJar(url, lookupPattern, result);
            } else {
                // This is useful when running Quarkus in dev mode
                getMatchingResourcesOutsideJar(new File(url.toURI()), lookupPattern, result);
            }
        }
        return result.stream()
                .map(URL::toString)
                .collect(Collectors.joining("\n"));
    }

    /**
     * Travers over the files inside jar and looking for files which match the pattern
     * This is simplified PathMatchingResourcePatternResolver#doFindPathMatchingJarResources
     * from spring which was used in reproducer
     */
    private void getMatchingResourcesInsideJar(URL rootDirURL, String lookupPattern, List<URL> result) throws IOException {

        URLConnection con = rootDirURL.openConnection();
        JarFile jarFile;
        String rootEntryPath = "";

        if (con instanceof JarURLConnection) {
            JarURLConnection jarCon = (JarURLConnection) con;
            jarFile = jarCon.getJarFile();
            JarEntry jarEntry = jarCon.getJarEntry();
            rootEntryPath = (jarEntry != null ? jarEntry.getName() : "");
        } else {
            jarFile = new JarFile(rootDirURL.getFile());
        }

        try {
            if (!rootEntryPath.isEmpty() && !rootEntryPath.endsWith("/")) {
                rootEntryPath = rootEntryPath + "/";
            }
            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements();) {
                JarEntry entry = entries.nextElement();
                String entryPath = entry.getName();
                if (entryPath.startsWith(rootEntryPath)) {
                    String relativePath = entryPath.substring(rootEntryPath.length());
                    if (matchResourceWithPattern(lookupPattern, relativePath)) {
                        result.add(new URL(rootDirURL, relativePath));
                    }
                }
            }
        } finally {
            jarFile.close();
        }
    }

    /**
     * Find resources on classpath when the resources are not inside JAR.
     * This can be useful when running the dev mode
     */
    private void getMatchingResourcesOutsideJar(File dir, String lookupPattern, List<URL> result) throws IOException {
        for (File content : listDirectory(dir)) {
            String currPath = content.getAbsolutePath().replace(File.separator, "/");
            if (content.isDirectory()) {
                getMatchingResourcesOutsideJar(content, lookupPattern, result);
            }
            if (matchResourceWithPattern(dir + lookupPattern, currPath)) {
                result.add(content.toURI().toURL());
            }
        }
    }

    private static boolean matchResourceWithPattern(String fullPattern, String path) {
        return path.matches(fullPattern);
    }

    private static boolean isJarURL(URL rootDirUrl) {
        return rootDirUrl.getProtocol().equals("jar");
    }

    protected File[] listDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) {
            return new File[0];
        }
        return files;
    }
}
