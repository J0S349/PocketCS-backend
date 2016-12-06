package backend;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;


/**
 * Created by GabrielZapata on 12/5/16.
 */
public class Logging implements Filter {

    public void destroy() {
    }

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    private static final Logger LOG = LogManager.getLogger(Logging.class);
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        LOG.info("Request Logged: IP:"+ servletRequest.getRemoteAddr().toString() + " URL:"+request.getRequestURL()+" Parameters:"+ servletRequest.getParameterMap().toString() );
        chain.doFilter(servletRequest, servletResponse);
    }
    public static Logger getLOG(){
        return LOG;
    }


}
