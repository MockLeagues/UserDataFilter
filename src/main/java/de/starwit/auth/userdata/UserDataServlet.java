package de.starwit.auth.userdata;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Extracts user data from web session and returns a JSON object, that contains 
 * every field from session data as JSON object attribute. Returns empty object if 
 * no session data can be found.
 * 
 * @author Markus Zarbock
 *
 */
@WebServlet("/GetUserData")
public class UserDataServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	final String ATTRIBUTE_NAME = "de.starwit.auth.userdata";
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		PrintWriter out = resp.getWriter();
		
		Map<String, String> userData;
		userData = (Map<String, String>) req.getSession().getAttribute(ATTRIBUTE_NAME);

		// check if userData is available, return empty JSON object if not.
		if (userData != null) {
			Set<String> keys = userData.keySet();
			int size = keys.size();
			
			out.write("{");
			for (String key : keys) {
				String tmp = "\"" + key + "\"" + " : \"" + userData.get(key) + "\"";
				if (size > 1) {
					tmp += ",";
				}
					
				out.write(tmp);
				size--;
			}
			out.write("}");
		} else {
			out.write("{}");
		}
		
		out.flush();
		out.close();
	}
	
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		this.doGet(req, resp);
	}

}
