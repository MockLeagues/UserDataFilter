package de.starwit.auth.userdata;

import java.io.IOException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

/**
 * TODO
 * * extract user ID from session
 * 
 * @author ztarbug
 *
 */
public class UserDataFilter implements Filter {
	
	final String ATTRIBUTE_NAME = "de.starwit.auth.userdata";
	
	private Logger log = Logger.getLogger(UserDataFilter.class);
	
	public void init(FilterConfig filterConfig) throws ServletException {
	
	}

	private DirContext getDirContext() {
		DirContext ctx;
		BasicConfigurator.configure();
		log.debug("UserDataFilter init");
		
		ConfigData configData = ConfigData.getInstance();
		String username = (String) configData.get("directory.username");
		String password = (String) configData.get("directory.password");
		String directoryUrl = (String) configData.get("directory.url");
		
		Hashtable<Object, Object> env = new Hashtable<Object, Object>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, directoryUrl);
		env.put(Context.SECURITY_CREDENTIALS, password);
		env.put(Context.SECURITY_PRINCIPAL, username);
		
		try {
			return new InitialDirContext(env);
		} catch (NamingException e) {
			// panic no connection to directory!
			log.error("Could not reach user directory " + 
					directoryUrl + 
					" with error " + 
					e.getMessage() + " " + 
					e.getExplanation());
			return null;
		}
	}

	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		log.debug("UserDataFilter called, check if user data is already present on session");
		HttpServletRequest request = (HttpServletRequest) req;
		HttpSession session = request.getSession();
		Object data = session.getAttribute(ATTRIBUTE_NAME);
		Map<String, String> userData = new HashMap<String, String>();
		
		if (data == null) {
			if (request.getUserPrincipal() != null) {
				DirContext ctx = getDirContext();
				String loggedInUser = request.getUserPrincipal().getName();
				if (ctx != null) {
					UserDirectoryDataRequester dataRequester = new UserDirectoryDataRequester(ctx);
					userData = dataRequester.getUserData(loggedInUser);
				}
				userData.put("alias", loggedInUser);
				session.setAttribute(ATTRIBUTE_NAME, userData);
			
				if (userData.size() > 1 ) {
					log.debug("loaded user data to session");
				} else {
					log.warn("no data for user " + loggedInUser + " could be loaded from directory - empty data set on session");
				}
			} else {
				log.warn("No logged in user found, please use UserDataFilter only on context paths with forced login.");
			}

			
		} // nothing to to do if data is present - therefore no else
		
		chain.doFilter(req, resp);
	}

	public void destroy() {
		log.debug("UserDataFilter destroyed");
	}
}
