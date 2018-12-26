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
import recommendation.GeoRecommendation;

@WebServlet("/recommendation")
public class RecommendItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
 
    public RecommendItem() {
        super();
    }
    
    //return favorite items in the response
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String userId = request.getParameter("user_id");
		Double lat = Double.parseDouble(request.getParameter("lat"));
		Double lon = Double.parseDouble(request.getParameter("lon"));
		
		List<Item> recommendItems = new GeoRecommendation().recommendItems(userId, lat, lon);
		System.out.println("after geo");
		DBConnection conn = DBConnectionFactory.getConnection();
		JSONArray array = new JSONArray();
		
		try {
			Set<String> favoriteItemIds = conn.getFavoriteItemIds(userId);
			
			for (Item item : recommendItems) {
				
				JSONObject obj = item.toJSONObject();
				
				if (favoriteItemIds.contains(item.getItemId())) {
					obj.append("favorite", true);
				}
				
				array.put(obj);
			}

			RPCHelper.writeJsonArray(response, array);
		} catch (JSONException e) {
			e.printStackTrace();
		} finally {
			conn.close();
		}
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
