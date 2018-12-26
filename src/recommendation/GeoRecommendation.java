package recommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;
import java.util.Set;

import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;

public class GeoRecommendation {
	public List<Item> recommendItems(String userId, double lat, double lon) {
		//step 1: get favorite items of user
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<Item> favoeriteItems = connection.getFavoriteItems(userId);
		
		//step 2: get and count categories of favorite items
		Map<String, Integer> favoriteCategories = new HashMap<>();
		for (Item item : favoeriteItems) {
			Set<String> categories = connection.getCategories(item.getItemId());
			for (String category : categories) {
				favoriteCategories.put(category, favoriteCategories.getOrDefault(category, 0));
			}
		}
		
		//step 3: sort them based on popularity
		List<Entry<String, Integer>> favoriteCatoList =  new ArrayList<>(favoriteCategories.entrySet());
		Collections.sort(favoriteCatoList, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> e1, Entry<String, Integer> e2) {
				return e1.getValue() - e2.getValue();
			}
		});
		
		//step 4: search nearby events of position (lat,lon, categories)
		Set<String> visitedItemIds = new HashSet<String>(); //deduplication
		List<Item> result = new ArrayList<>();
		for (Entry<String, Integer> entry : favoriteCatoList) {
			List<Item> items = connection.searchItems(lat, lon, entry.getKey());
			for (Item item : items) {
				if (visitedItemIds.add(item.getItemId())) {
					result.add(item);
				}
			}
		}
		
		connection.close();
		return result;
	}
}
