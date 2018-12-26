package db.mongodb;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;

import static com.mongodb.client.model.Filters.eq;

import db.DBConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterAPI;

public class MongoDBCollection implements DBConnection {
	
	private MongoClient mongoClient;
	private MongoDatabase db;
	
	public MongoDBCollection() {
		mongoClient = new MongoClient();
		db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
	}
	
	@Override
	public void close() {
		if (mongoClient != null) {
			mongoClient.close();
		}
	}

	@Override
	public List<Item> searchItems(double lat, double lon, String term) {
		TicketMasterAPI tmAPI = new TicketMasterAPI();
		List<Item> items = tmAPI.search(lat, lon, term);
		for (Item item : items) {
			saveItem(item);
		}
		return items;
	}
	
	
	@Override
	public void saveItem(Item item) {
		if (db == null) {
	   		System.err.println("DB connection failed");
	   		return;			
		}
		
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", item.getItemId()));
		
		if (iterable.first() == null) {
			return;
		}
		
		db.getCollection("items").insertOne(new Document()
				.append("item_id", item.getItemId())
				.append("name", item.getName())
				.append("rating", item.getRating())
				.append("address", item.getAddress())
				.append("image_url", item.getImageUrl())
				.append("url", item.getUrl())
				.append("distance", item.getDistance())
				.append("categories", item.getCategories()));
	}
	
	
	@Override
	public void setFavoriteItems(String userId, List<String> itemIds) {
		if (db == null) {
	   		System.err.println("DB connection failed");
	   		return;			
		}
		db.getCollection("users").updateOne(new Document("user_id", userId),
											new Document("$push", new Document("favorite", new Document("$each", itemIds))));
	}

	@Override
	public void unsetFavoriteItems(String userId, List<String> itemIds) {
		if (db == null) {
	   		System.err.println("DB connection failed");
	   		return;		
		}
		db.getCollection("users").updateOne(new Document("user_id", userId),
											new Document("$pullAll", new Document("favorite", itemIds)));
	}

	@Override
	public Set<String> getFavoriteItemIds(String userId) {
		Set<String> favoriteItemIds = new HashSet<>();
		FindIterable<Document> iterable = db.getCollection("users").find(eq("user_id", userId));
		if (iterable.first() != null && iterable.first().containsKey("favorite")) {
			@SuppressWarnings("unchecked")
			List<String> itemList = (List<String>) iterable.first().get("favorite");
			favoriteItemIds.addAll(itemList);
		}
		return favoriteItemIds;
	}

	@Override
	public Set<Item> getFavoriteItems(String userId) {
		
		Set<String> favoriteItemIds = getFavoriteItemIds(userId);
		Set<Item> favoriteItems = new HashSet<>();
		for (String itemId : favoriteItemIds) {
			FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
			if (iterable.first() != null) {
				Document doc = iterable.first();
				ItemBuilder builder = new ItemBuilder();
				builder	.setItemId(itemId)
						.setName(doc.getString("name"))
						.setAddress(doc.getString("address"))
						.setDistance(doc.getDouble("distance"))
						.setUrl(doc.getString("url"))
						.setImageUrl(doc.getString("image_url"))
						.setRating(doc.getDouble("rating"))
						.setCategories(getCategories(itemId));
				favoriteItems.add(builder.build());
			}
		}
		return favoriteItems;
	}

	@Override
	public Set<String> getCategories(String itemId) {
		Set<String> categories = new HashSet<String>();
		FindIterable<Document> iterable = db.getCollection("items").find(eq("item_id", itemId));
		if (iterable.first() != null && iterable.first().containsKey("category")) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) iterable.first().get("category");
			categories.addAll(list);
		}
		return categories;
	}
	
	@Override
	public String getFullname(String userId) {
		String fullName = "";
		FindIterable<Document> iterable = db.getCollection("users").find(eq("users_id", userId));
		if (iterable.first() != null) {
			if (iterable.first().containsKey("first_name")) {
				fullName = fullName + iterable.first().getString("first_name");
			}
			if (iterable.first().containsKey("last_name")) {
				fullName = fullName + " " + iterable.first().getString("last_name");
			}
		}
		return fullName;
	}

	@Override
	public boolean verifyLogin(String userId, String password) {
		FindIterable<Document> iterable = db.getCollection("users").find(eq("users_id", userId));
		if (iterable.first() != null && iterable.first().getString("password").equals(password)) {
			return true;
		}
		return false;
	}



}
