package db.mySQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MySQLConnection implements DBConnection {
	
	private Connection conn;
	
	public MySQLConnection () {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver").getConstructor().newInstance();
			conn = DriverManager.getConnection(MySQLDBUtil.URL);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void close() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	//Use TicketMaster API to get items from online.
	//Then save them in DB one by one and return results;
	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}
	
	//Save items in MySQL db
	@Override
	public void saveItem(Item item) {
		
		if (conn == null) {
	   		System.err.println("DB connection failed");
	   		return;			
		}

		try {
			String sql = "INSERT IGNORE INTO items VALUES (?,?,?,?,?,?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, item.getItemId());
			ps.setString(2, item.getName());
			ps.setString(3, String.valueOf(item.getRating()));
			ps.setString(4, item.getAddress());
			ps.setString(5, item.getImageUrl());
			ps.setString(6, item.getUrl());
			ps.setString(7, String.valueOf(item.getDistance()));
			ps.execute();
			
			sql = "INSERT IGNORE INTO categories VALUES (?,?)";
			ps = conn.prepareStatement(sql);
			for (String category : item.getCategories()) {
				ps.setString(1, item.getItemId());
				ps.setString(2, category);
				ps.execute();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
			
	}
	
	//write into DB history: userid, itemid one by one
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
	   		System.err.println("DB connection failed");
	   		return;			
		}

		try {
			String sql = "INSERT IGNORE INTO history(user_id, item_id) VALUES (?,?)";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//delete from DB history: userid, itemid lastfavortime 
	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (conn == null) {
	   		System.err.println("DB connection failed");
	   		return;			
		}
		
		try {
			
			String sql = "DELETE FROM history WHERE user_id = ? AND item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			
			for (String itemId : itemIds) {
				ps.setString(2, itemId);
				ps.execute();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//given userId, get a set of item id strings.
	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<String>();
		}
		Set<String> favoriteItemIds = new HashSet<>();
		try {
			String sql = "SELECT item_id from history where user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				favoriteItemIds.add(rs.getString("item_id"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItemIds;
	}
	
	//given userId get a set of items liked by user. Get itemId first and then search item based on item_id
	@Override
	public Set<Item> getFavoriteItems(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<Item>();
		}
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		try {
			String sql = "SELECT * FROM items where item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			for (String itemId : favoriteItemIds) {
				ps.setString(1, itemId);
				ResultSet rs = ps.executeQuery();
				ItemBuilder builder = new ItemBuilder();
				while (rs.next()) {
					builder.setItemId(itemId)
							.setName(rs.getString("name"))
							.setAddress(rs.getString("address"))
							.setDistance(rs.getDouble("distance"))
							.setUrl(rs.getString("url"))
							.setImageUrl(rs.getString("image_url"))
							.setRating(rs.getDouble("rating"))
							.setCategories(getCategories(itemId));
				}
				favoriteItems.add(builder.build());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return favoriteItems;
	}
	
	//get the categories of itemId
	@Override
	public Set<String> getCategories(String itemId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return new HashSet<String>();
		}
		Set<String> categories = new HashSet<>();
		try {
			String sql = "SELECT category FROM categories where item_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, itemId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				categories.add(rs.getString("category"));
			}
					   
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}

	@Override
	public String getFullname(String userId) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return "";
		}
		String fullName = "";
		try {
			String sql = "SELECT first_name, last_name FROM users WHERE user_id = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ResultSet rs = ps.executeQuery();
			if(rs.next()) {
				fullName = rs.getString("first_name") + " " + rs.getString("last_name");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return fullName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		if (conn == null) {
			System.err.println("DB connection failed");
			return false;
		}
		try {
			String sql = "SELECT user_id FROM users WHERE user_id = ? AND password = ?";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, userId);
			ps.setString(2, password);
			ResultSet rs = ps.executeQuery();
			return rs.next();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

}
