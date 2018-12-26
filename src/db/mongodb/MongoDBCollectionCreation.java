package db.mongodb;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;

public class MongoDBCollectionCreation {
	public static void main(String[] args) {
		
		//step 1: connect to DB
		MongoClient mongoClient = new MongoClient();
		MongoDatabase db = mongoClient.getDatabase(MongoDBUtil.DB_NAME);
		
		//step 2: remove old collections
		db.getCollection("items").drop();
		db.getCollection("users").drop();
		db.getCollection("history").drop();
		db.getCollection("categories").drop();
		
		//step 3: create new collections
		//make item_id and user_id unique and sort from small to large
		IndexOptions options = new IndexOptions().unique(true);
		db.getCollection("users").createIndex(new Document("user_id", 1), options); // 1 means ascending order
		db.getCollection("items").createIndex(new Document("item_id", 1), options); // document translate java code to mongodb queries
		
		//step 4: insert a fake user
		db.getCollection("users").insertOne(new Document().append("user_id", "newUser")
														  .append("password", "newUser")
														  .append("first_name", "newUser")
														  .append("last_name", "newUser"));
		
		mongoClient.close();
		System.out.println("Import is done successfully.");
	}
	
}
