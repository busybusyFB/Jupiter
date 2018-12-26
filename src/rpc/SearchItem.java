package rpc;

import java.io.IOException;
import java.io.PrintWriter;
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
import external.TicketMasterAPI;

@WebServlet("/search")
public class SearchItem extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
       
    public SearchItem() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		double lat = Double.parseDouble(request.getParameter("lat"));
		double lon = Double.parseDouble(request.getParameter("lon"));
		String term = request.getParameter("term");
		String userId = request.getParameter("user_id");
		
		DBConnection connection = DBConnectionFactory.getConnection("mysql");
		
		try {
			
			List<Item> items = connection.searchItems(lat, lon, term);
			Set<String> favoriteItemIds = connection.getFavoriteItemIds(userId);
			JSONArray array = new JSONArray();
			for (Item item : items) {
				JSONObject obj = item.toJSONObject();
				obj.put("favorite", favoriteItemIds.contains(item.getItemId()));
				array.put(obj);
			}
		
			RPCHelper.writeJsonArray(response, array);
		} catch(JSONException e) {
			e.printStackTrace();
		} finally {
			connection.close();
		}
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
