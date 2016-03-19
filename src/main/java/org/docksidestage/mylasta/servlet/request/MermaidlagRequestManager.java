package org.docksidestage.mylasta.servlet.request;

import org.lastaflute.web.servlet.request.SimpleRequestManager;
import org.lastaflute.web.util.LaServletContextUtil;

public class MermaidlagRequestManager extends SimpleRequestManager {

    @Override
    protected String removeViewPrefixFromRequestPathIfNeeds(String path) {
        if (!path.endsWith(".html")) {
            return path;
        }
        final String viewPrefix = LaServletContextUtil.getHtmlViewPrefix();
        return path.startsWith(viewPrefix) ? path.substring(viewPrefix.length()) : path;
    }
}
