package rpc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;

    public ItemHistory() {
        super();
    }

    //return favorite items in the response
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();

		DBConnection conn = DBConnectionFactory.getConnection();
		try {
			Set<Item> items = conn.getFavoriteItems(userId);
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.append("favorite", true);
				array.put(obj);
			}

			RPCHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}

	}
	
	//get favorite items in request and update in DB
	//request: {{"user_id": id}, {"favorite": [itmeid1,itemid2,...]}}
	//return result: success
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	
		JSONObject obj = RPCHelper.readJSONObject(request);
		
		DBConnection conn = DBConnectionFactory.getConnection("mysql");
		
		try {
			String userId = obj.getString("user_id");

		
			JSONArray array = obj.getJSONArray("favorite");
		
			List<String> item_ids = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				item_ids.add(array.getString(i));
			}
		
			
			conn.setFavoriteItems(userId, item_ids);
			
			RPCHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		
	}


	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject obj = RPCHelper.readJSONObject(request);
		
		DBConnection conn = DBConnectionFactory.getConnection("mysql");
		
		try {
			String userId = obj.getString("user_id");

		
			JSONArray array = obj.getJSONArray("favorite");
		
			List<String> item_ids = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				item_ids.add(array.getString(i));
			}
		
			
			conn.unsetFavoriteItems(userId, item_ids);
			
			RPCHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
			
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
	}

}
