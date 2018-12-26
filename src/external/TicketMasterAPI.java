package external;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import entity.Item;
import entity.Item.ItemBuilder;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = ""; // no restriction
	private static final String API_KEY = "p9V6VeV2GvQMLqyP8hjtheDuAGaJ2Bmi";
	
	public List<Item> search(double lat, double lon, String keyword) {
		
		if (keyword == null) {
			keyword = DEFAULT_KEYWORD;
		}
		
		try {
			keyword = URLEncoder.encode(keyword,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String geoHash = GeoHash.encodeGeohash(lat, lon, 8);
		
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=%s", API_KEY, geoHash,keyword, 50);
		String url = URL + "?" + query;
//		System.out.println(url);
		
		try {
			
			HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
			int code = connection.getResponseCode();
			if (code != 200) {
				System.out.println("Connection failed");
				return new ArrayList<>();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder response = new StringBuilder();

			String line = "";
			while((line = reader.readLine()) != null) {
				response.append(line);
			}
			reader.close();
			
			JSONObject obj = new JSONObject(response.toString());
			
			//{"_embedded",{"events", jsonArray[]}}
			if (!obj.isNull("_embedded")) {
				JSONObject embedded = obj.getJSONObject("_embedded");
				return getItemList(embedded.getJSONArray("events"));
			}
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return new ArrayList<>();
	}
	
	// Convert JSONArray to a list of item objects.
	private List<Item> getItemList(JSONArray events) throws JSONException {
		
		List<Item> itemList = new ArrayList<>();
		
		for (int i = 0; i < events.length(); ++i) {
			
			JSONObject event = events.getJSONObject(i);
			
			ItemBuilder builder = new ItemBuilder();
			if (!event.isNull("id")) {
				builder.setItemId(event.getString("id"));
			}
			if (!event.isNull("name")) {
				builder.setName(event.getString("name"));
			}
			if (!event.isNull("url")) {
				builder.setUrl(event.getString("url"));
			}
			if (!event.isNull("distance")) {
				builder.setDistance(event.getDouble("distance"));
			}
			if (!event.isNull("rating")) {
				builder.setRating(event.getDouble("rating"));
			}
			
			builder.setAddress(getAddress(event));
			
			builder.setCategories(getCategories(event));
			
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}

		return itemList;
	}
	
	private String getAddress(JSONObject event) throws JSONException {
		if (event.isNull("_embedded")) {
			return "";
		}
			
		JSONObject embedded = event.getJSONObject("_embedded");
		
		if (embedded.isNull("venues")) {
			return "";
		}
			
		JSONArray venues = embedded.getJSONArray("venues");
		
		for (int i = 0; i < venues.length(); ++i) {
			JSONObject venue = venues.getJSONObject(i);
			StringBuilder addressBuilder = new StringBuilder();
			if (!venue.isNull("address")) {
				JSONObject address = venue.getJSONObject("address");
				if (!address.isNull("line1")) {
					addressBuilder.append(address.getString("line1"));
				}
				if (!address.isNull("line2")) {
					addressBuilder.append(",");
					addressBuilder.append(address.getString("line2"));
				}
				if (!address.isNull("line3")) {
					addressBuilder.append(",");
					addressBuilder.append(address.getString("line3"));
				}
			}
				
			if (!venue.isNull("city")) {
				JSONObject city = venue.getJSONObject("city");
				if (!city.isNull("name")) {
					addressBuilder.append(",");
					addressBuilder.append(city.getString("name"));
				}
			}
					
			String addressStr = addressBuilder.toString();
			if (!addressStr.equals("")) {
				return addressStr;
			}
		}
		
		return "";
	}
	
	private Set<String> getCategories(JSONObject event) throws JSONException {
		Set<String> categories = new HashSet<String>();
		if (!event.isNull("classifications")) {
			JSONArray array = event.getJSONArray("classifications");
			for (int i = 0; i < array.length(); i++) {
				JSONObject obj = (JSONObject) array.get(i);
				if (!obj.isNull("segment")) {
					JSONObject segment = obj.getJSONObject("segment");
					if (!segment.isNull("name")) {
						categories.add(segment.getString("name"));
					}
				}
			}
		}
		return categories;
	}
	
	private String getImageUrl(JSONObject event) throws JSONException {
		if (!event.isNull("images")) {
			JSONArray array = event.getJSONArray("images");
			for (int i = 0; i < array.length(); ++i) {
				JSONObject image = array.getJSONObject(i);
				if (!image.isNull("url")) {
					return image.getString("url");
				}
			}
		}
		return "";
	}
	//For testing
	private void queryAPI(double lat, double lon) {
		
		List<Item> events = search(lat, lon, null);
		System.out.println(events.size());
//		try {
//			for (int i = 0; i < events.size(); ++i) {
//				JSONObject event = events.getJSONObject(i);
//				System.out.println(event.toString(2));
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public static void main(String[] args) {
		System.out.println("starting");
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		tmApi.queryAPI(37.38, -122.08);
		System.out.println("finish");
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		//tmApi.queryAPI(29.682684, -95.295410);
	}

	
}
