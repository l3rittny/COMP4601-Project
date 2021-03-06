package comp4601.project.dao;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import comp4601.project.models.Product;
import comp4601.project.models.User;
import comp4601.project.models.Product.Condition;

public class UserService {
	MongoClient mongoClient;
	DB database;
	DBCollection userCollection;

	int refresh = 300000;
	
	public UserService(){
		try {
			mongoClient = new MongoClient("localhost", 27017);
			database = mongoClient.getDB("craftyDB");
			userCollection = database.getCollection("users");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean createUser(String username, String password){
		//craftyDB products
		try {
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.put("username", username);
			newDocument.put("password", password);

			newDocument.put("wallack", 0);
			newDocument.put("abovegound", 0);
			newDocument.put("artshack", 0);
			newDocument.put("jerrys", 0);
			newDocument.put("deserres", 0);
			newDocument.put("oneTwenty", 0);
			newDocument.put("twentyFifty", 0);
			newDocument.put("fiftyHunderd", 0);
			newDocument.put("hundredThree", 0);
			newDocument.put("overThree", 0);
			userCollection.insert(newDocument);
			return true;
		} catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error creating user");
			return false;
		}
	}
	public boolean updateUser(String username, User u){
		//craftyDB products
		try {
			BasicDBObject searchQuery = new BasicDBObject().append("username", username);
			
			//userCollection.update(searchQuery, newDocument2);
			BasicDBObject newDocument = new BasicDBObject();
			
			newDocument.append("wallack",u.wallack);
			newDocument.append("abovegound", u.aboveground);
			newDocument.append("artshack", u.artshack);
			newDocument.append("jerrys", u.jerrys);
			newDocument.append("deserres", u.deserres);
			newDocument.append("oneTwenty",u.oneTwenty);
			newDocument.append("twentyFifty", u.twentyFifty);
			newDocument.append("fiftyHunderd", u.fiftyHunderd);
			newDocument.append("hundredThree", u.hundredThree);
			newDocument.append("overThree", u.overThree);
			newDocument.append("cluster", u.cluster);
			BasicDBObject newDocument2 = new BasicDBObject("$set",newDocument);
			userCollection.update(searchQuery, newDocument2);
			return true;
		} catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error updating user");
			return false;
		}
	}
	
	public User findOne(String username, String password){
		//System.out.println("in db fn");
		BasicDBObject query = new BasicDBObject();
		//System.out.println("in db fn 2");
		query.put("username", username);
		//System.out.println("in db fn 3");
		query.put("password", password);
		//System.out.println("in db fn 4");
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);	
		//System.out.println("in db fn 5");
		if(!result.isEmpty()){
			//System.out.println("user 1!");
			String usernameRes = result.getString("username");
			String token = "";
			Long accessTimeMS = (long) 0;
			if(result.containsField("token")){
				token = result.getString("token");
			}
			if(result.containsField("accessTimeMS")){
				accessTimeMS = result.getLong("accessTimeMS");
			}
			int community=0;
			//System.out.println("user 2!");
			if(result.containsField("community")){
				community = result.getInt("community");
			}
			int wallack = result.getInt("wallack");
			int aboveground = result.getInt("abovegound");
			int artshack = result.getInt("artshack");
			int jerrys = result.getInt("jerrys");
			int deserres = result.getInt("deserres");
			int oneTwenty = result.getInt("oneTwenty");
			int twentyFifty = result.getInt("twentyFifty");
			int fiftyHundred = result.getInt("fiftyHunderd");
			int hundredThree = result.getInt("hundredThree");
			int overThree = result.getInt("overThree");
			User u = new User(usernameRes, token, accessTimeMS, wallack, aboveground, artshack, jerrys, deserres, oneTwenty, twentyFifty, fiftyHundred, hundredThree, overThree, community);
			
			return u;		
		}
		//System.out.println("no user :-(");
		return null;
	}
	public User findOne(String username){
		//System.out.println("in db fn");
		BasicDBObject query = new BasicDBObject();
		//System.out.println("in db fn 2");
		query.put("username", username);
		//System.out.println("in db fn 3");
		//query.put("password", password);
		//System.out.println("in db fn 4");
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);	
		//System.out.println("in db fn 5");
		
		if(!result.isEmpty()){
			//System.out.println("user 1!");
			String usernameRes = result.getString("username");
			String token = "";
			Long accessTimeMS = (long) 0;
			if(result.containsField("token")){
				token = result.getString("token");
			}
			if(result.containsField("accessTimeMS")){
				accessTimeMS = result.getLong("accessTimeMS");
			}
			int community=0;
			//System.out.println("user 2!");
			if(result.containsField("community")){
				community = result.getInt("community");
			}
			int wallack = result.getInt("wallack");
			int aboveground = result.getInt("abovegound");
			int artshack = result.getInt("artshack");
			int jerrys = result.getInt("jerrys");
			int deserres = result.getInt("deserres");
			int oneTwenty = result.getInt("oneTwenty");
			int twentyFifty = result.getInt("twentyFifty");
			int fiftyHundred = result.getInt("fiftyHunderd");
			int hundredThree = result.getInt("hundredThree");
			int overThree = result.getInt("overThree");
			User u = new User(usernameRes, token, accessTimeMS, wallack, aboveground, artshack, jerrys, deserres, oneTwenty, twentyFifty, fiftyHundred, hundredThree, overThree, community);
			
			return u;		
		}
		
		//System.out.println("no user :-(");
		return null;
	}
	
	public User findUserWithToken(String token){
		BasicDBObject query = new BasicDBObject();
		query.put("token", token);
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);	

		if(result != null && !result.isEmpty()){
			//System.out.println("user 1!");
			String usernameRes = result.getString("username");
			Long accessTimeMS = (long) 0;
			if(result.containsField("token")){
				token = result.getString("token");
			}
			if(result.containsField("accessTimeMS")){
				accessTimeMS = result.getLong("accessTimeMS");
			}
			int community=0;
			//System.out.println("user 2!");
			if(result.containsField("community")){
				community = result.getInt("community");
			}
			int wallack = result.getInt("wallack");
			int aboveground = result.getInt("abovegound");
			int artshack = result.getInt("artshack");
			int jerrys = result.getInt("jerrys");
			int deserres = result.getInt("deserres");
			int oneTwenty = result.getInt("oneTwenty");
			int twentyFifty = result.getInt("twentyFifty");
			int fiftyHundred = result.getInt("fiftyHunderd");
			int hundredThree = result.getInt("hundredThree");
			int overThree = result.getInt("overThree");
			User u = new User(usernameRes, token, accessTimeMS, wallack, aboveground, artshack, jerrys, deserres, oneTwenty, twentyFifty, fiftyHundred, hundredThree, overThree, community);
			
			return u;		
		}
		return null;
	}
	
	public ArrayList<String> getUserWatchingList(String token){
		BasicDBObject query = new BasicDBObject();
		query.put("token", token);
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);	

		ArrayList<String> results = new ArrayList<String>();
		if(result != null && !result.isEmpty()){
			BasicDBList list = (BasicDBList) result.get("watching");
			ArrayList<Product> res = new ArrayList<Product>();
			for(Object el: list) {
				String terms = (String) el;
				results.add(terms);
			}	
		}
		return results;
	}
	
	public User checkToken(String token){
		BasicDBObject query = new BasicDBObject();
		query.put("token", token);
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);	
		if(!result.isEmpty()){
			String usernameRes = result.getString("username");
			Long accessTimeMS = result.getLong("accessTimeMS");
			Long max = accessTimeMS + refresh;
			Long currTime = System.currentTimeMillis();
			if(max < accessTimeMS){
				return null;
			}
			return new User(usernameRes, token, accessTimeMS);		
		}
		return null;
	}
	
	public User updateToken(String username, String token){
		User u = new User(username, token);
		BasicDBObject newDocument = new BasicDBObject("$set", new BasicDBObject().append("token", token));
		BasicDBObject newDocument2 = new BasicDBObject("$set", new BasicDBObject().append("accessTimeMS", u.getAccessTimeMS()));
		BasicDBObject searchQuery = new BasicDBObject().append("username", username);
		userCollection.update(searchQuery, newDocument);
		userCollection.update(searchQuery, newDocument2);
		return u;
	}
	
	public void watchQuery(String username, String watchQuery){
		 DBObject listItem = new BasicDBObject("watching", watchQuery);
		 DBObject updateQuery = new BasicDBObject("$push", listItem);
		 userCollection.update(new BasicDBObject("username", username), updateQuery);
	}
	
	
	public void removeWatchQuery(String username, String watchQuery){
		 DBObject listItem = new BasicDBObject("watching", watchQuery);
		 DBObject updateQuery = new BasicDBObject("$pull", listItem);
		 userCollection.update(new BasicDBObject("username", username), updateQuery);
	}
	

	public void addViewedProduct(String username, String product){
		 DBObject listItem = new BasicDBObject("viewed", product);
		 DBObject updateQuery = new BasicDBObject("$push", listItem);
		 userCollection.update(new BasicDBObject("username", username), updateQuery);
	}
	
	public HashMap<String, ArrayList<Product>> getUserWatchedQueryProducts(String username){
		HashMap<String, ArrayList<Product>> results = new HashMap<String, ArrayList<Product>>();
		BasicDBObject query = new BasicDBObject();
		query.put("username", username);
		BasicDBObject result = (BasicDBObject) userCollection.findOne(query);
		if(result.containsField("watching")){
			ProductService p = new ProductService();
			BasicDBList list = (BasicDBList) result.get("watching");
			ArrayList<Product> res = new ArrayList<Product>();
			for(Object el: list) {
				String terms = (String) el;
				ArrayList<Product> topProds = p.queryTop(terms, 5);
				results.put(terms, topProds);
			}
		}
		return results;	
	}
	public ArrayList<User> getAllUsers() throws IOException{
		ArrayList<User> users= new ArrayList<User>();
		DBCursor cursor = userCollection.find();
		while(cursor.hasNext()){
			BasicDBObject result = (BasicDBObject) cursor.next();
			String usernameRes = result.getString("username");
			String token = "";
			Long accessTimeMS = (long) 0;
			int community=0;
			if(result.containsField("token")){
				token = result.getString("token");
			}
			if(result.containsField("accessTimeMS")){
				accessTimeMS = result.getLong("accessTimeMS");
			}
			if(result.containsField("community")){
				community = result.getInt("community");
			}
			int wallack = result.getInt("wallack");
			int aboveground = result.getInt("abovegound");
			int artshack = result.getInt("artshack");
			int jerrys = result.getInt("jerrys");
			int deserres = result.getInt("deserres");
			int oneTwenty = result.getInt("oneTwenty");
			int twentyFifty = result.getInt("twentyFifty");
			int fiftyHundred = result.getInt("fiftyHunderd");
			int hundredThree = result.getInt("hundredThree");
			int overThree = result.getInt("overThree");
			User p = new User(usernameRes, token, accessTimeMS, wallack, aboveground, artshack, jerrys, deserres, oneTwenty, twentyFifty, fiftyHundred, hundredThree, overThree, community);
			users.add(p);
		}
		return users;
	}
}
