package fr.insalyon.creatis.vip.portal.server;

import java.io.IOException;
import java.util.Set;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * For SPA routes under /new_front, avoid 404 for other routes than /new_front
 */
public class NewFrontSpaForwardFilter implements Filter {

    private static final Set<String> STATIC_EXTENSIONS = Set.of(
        "js", "css", "png", "jpg", "jpeg", "gif", "svg", "ico",
        "woff", "woff2", "ttf", "eot",
        "map", "json", "txt", "pdf", "xml",
        "webp", "avif", "mp4", "webm"
    );

    @Override
    public void init(final FilterConfig filterConfig) {
        // No init
    }

    @Override
    public void doFilter(
            final ServletRequest request,
            final ServletResponse response,
            final FilterChain chain
    ) throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final String method = httpRequest.getMethod();

        if (!"GET".equalsIgnoreCase(method) && !"HEAD".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }

        final String contextPath = httpRequest.getContextPath();
        final String requestUri = httpRequest.getRequestURI();
        final String path = requestUri.substring(contextPath.length());

        if (isSpaRoute(path)) {
            httpRequest.getRequestDispatcher("/new_front/index.html").forward(request, response);
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No rsc to delete
    }

    private boolean isSpaRoute(final String path) {
        if (!path.startsWith("/new_front")) {
            return false;
        }

        String subpath = path.substring("/new_front".length());
        if (subpath.isEmpty() || subpath.equals("/") || subpath.equals("/index.html")) {
            return false;
        }

        String lastSegment = subpath.substring(subpath.lastIndexOf('/') + 1);
        int dot = lastSegment.lastIndexOf('.');
        if (dot > 0 && dot < lastSegment.length() - 1) {
            String ext = lastSegment.substring(dot + 1).toLowerCase();
            return !STATIC_EXTENSIONS.contains(ext);
        }
        return true;
    }
}
