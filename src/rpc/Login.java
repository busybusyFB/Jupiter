package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;

@WebServlet("/login")
public class Login extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public Login() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		DBConnection conn = DBConnectionFactory.getConnection();
		
		try {
			HttpSession session = request.getSession(false);
			JSONObject obj = new JSONObject();

			if (session != null) {
				String userId = session.getAttribute("user_id").toString();
				obj.put("status", "OK").put("user_id", userId).put("name", conn.getFullname(userId));
			} else {
				response.setStatus(403); // No authorization
				obj.put("status", "Invalid session!");
				
			}
			RPCHelper.writeJsonObject(response, obj);
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		DBConnection conn = DBConnectionFactory.getConnection();
		
		try {
			
			JSONObject obj = RPCHelper.readJSONObject(request);
			String userId = obj.getString("user_id");
			String password = obj.getString("password");
			
			JSONObject returnObj = new JSONObject();
			if (conn.verifyLogin(userId, password)) {
				
				HttpSession session = request.getSession(); //a new one is created since no one in the request
				session.setAttribute("user_id", userId);
				session.setMaxInactiveInterval(600); //time out
				returnObj.put("status", "OK").put("user_id", userId).put("name", conn.getFullname(userId));
				
			} else {
				
				response.setStatus(401); // No such user
				returnObj.put("status", "User doesn't exist!");
				
			}
			
			RPCHelper.writeJsonObject(response, returnObj);
			//** when creating a session in request, then this session is also created in the session. Function of TomCat.
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

}
