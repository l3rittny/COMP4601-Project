package comp4601.project.dao;
/*
 * This class reads in products from our data files and writes them to the database,
 * */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

import comp4601.project.models.Product;
import comp4601.project.models.Product.Condition;

public class ProductService {
	String path = "C:/Users/IBM_ADMIN/dev/COMP4601-Project/data";
	//String path = "/Users/kellymaclauchlan/code/mobile/project/COMP4601-Project/data";
	MongoClient mongoClient;
	DB database;
	DBCollection productCollection;
	
	public ProductService(){
		try {
			mongoClient = new MongoClient("localhost", 27017);
			database = mongoClient.getDB("craftyDB");
			productCollection = database.getCollection("products");
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void readFiles() {
		File dataDir = new File(this.path);
		System.out.println(this.path);
		File[] dataFiles = dataDir.listFiles();
		System.out.println(Arrays.toString(dataFiles));
		try {
			if (dataFiles != null) {
				for (File jsonInputFile : dataFiles) {
					if(jsonInputFile.getName().contains(".json")){
						boolean usd = false;
						if(jsonInputFile.getName().contains("jerry")){
							usd = true;
						}
						System.out.println(jsonInputFile.getName());
						InputStream jsonStream = new FileInputStream(jsonInputFile);
						JsonReader jsonReader = Json.createReader(jsonStream);
						JsonArray dataArray = jsonReader.readArray();
		            
						// Get the JsonObject structure from JsonReader.
						for(int i = 0; i < dataArray.size(); i++){
							JsonObject productJSON = (JsonObject) dataArray.get(i);
							JsonValue store = productJSON.get("store");
							JsonValue url = productJSON.get("url");
							JsonValue title = productJSON.get("title");
							JsonValue brand = productJSON.get("brand");
							JsonValue price = productJSON.get("price");
							
							double priceD = 0.0;
							if(price.toString().length() > 0){
								priceD = Double.parseDouble(price.toString().replaceAll("(\"|,)", ""));
								if(usd){
									priceD = priceD * 1.3;
								}
							}
							//analyzer
							String titleString = title.toString().replaceAll("\"", "");
							Product newProd = new Product(titleString, store.toString().replaceAll("\"", ""), url.toString().replaceAll("\"", ""), priceD);
							newProd.setBrand(brand.toString());
							newProd.appendAndAnalyzeFullText("indexed_text", titleString);
							this.addProductToDB(newProd);
						}
					}
				}
			} 
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void addProductToDB(Product p){
		//craftyDB products
		try {
			BasicDBObject newDocument = new BasicDBObject();
			newDocument.put("title", p.getTitle());
			newDocument.put("store", p.getStore());
			newDocument.put("url", p.getUrl());
			newDocument.put("price", p.getPrice());
			newDocument.put("brand", p.getBrand());
			BasicDBList tokens = new BasicDBList();
			for(String token :  p.getTokens()){
				tokens.add(token);
			}
			/*this.tokens*/
			newDocument.put("indexed_text", tokens);
			productCollection.insert(newDocument);
		} catch(Exception e){
			System.out.println(e.getMessage());
			System.out.println("error adding product to db");
		}
	}
	
	public Product queryOne(String queryString){
		try {
			DBObject query = new Product(new StandardAnalyzer()).createQuery("indexed_text",queryString, Condition.ALL);
			BasicDBObject result = (BasicDBObject) productCollection.findOne(query);	
			String title = result.getString("title");
			String store = result.getString("store");
			String url = result.getString("url");
			String brand = result.getString("brand");
			Double price = result.getDouble("price");
			return new Product(title,store,url,price);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public ArrayList<Product> query(String queryString){
		ArrayList<Product> products = new ArrayList<Product>();
		try {
			DBObject query = new Product(new StandardAnalyzer()).createQuery("indexed_text",queryString, Condition.ALL);
			DBCursor cursor = productCollection.find(query);	
			cursor.sort(new BasicDBObject("price", 1));
			while(cursor.hasNext()){
				BasicDBObject result = (BasicDBObject) cursor.next();
				String title = result.getString("title");
				String store = result.getString("store");
				String url = result.getString("url");
				String brand = result.getString("brand");
				Double price = result.getDouble("price");
				Product p = new Product(title,store,url,price);
				products.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return products;
	}
	public ArrayList<Product> recomendedSearch(String storeFave,String price, String querry){
		int max=10;//max number of products to retreive
		int found=0;
		int ms=0;
		int mas=1000;
		if(price=="one"){
			ms=0;
			mas=20;
		}if(price=="two"){
			ms=20;
			mas=50;
		}
		if(price=="three"){
			ms=50;
			mas=100;
		}
		if(price=="four"){
			ms=100;
			mas=300;
		}
		if(price=="five"){
			ms=300;
			mas=300000;
		}
		ArrayList<Product> products= new ArrayList<Product>();
		ArrayList<Product> rands =this.query(querry);
		
		for(Product p :rands){
			p.setSuggested(false);
			if(p.getPrice()>=ms&&p.getPrice()<=mas){
				//System.out.println("found a match");
				if(p.getStore().contentEquals(storeFave)){
					p.setSuggested(true);
					System.out.println("found a match");
					products.add(p);
					found++;
				}
			}
		}
		for(Product p :products){
			if(rands.contains(p)){
				boolean r = rands.remove(p);
				System.out.println(r);
			}
		}
		products.addAll(rands);
		Collections.sort(products, 
                (o1, o2) -> (Boolean.compare(o1.isSuggested(),o2.isSuggested())*-1));
		// Store unique items in result.
        ArrayList<Product> result = new ArrayList<>();

        // Record encountered Strings in HashSet.
        HashSet<String> set = new HashSet<>();

        // Loop over argument list.
        for (Product item : products) {

            // If String is not in set, add it to the list and the set.
            if (!set.contains(item.getTitle() + " " + item.getStore())) {
                result.add(item);
                set.add(item.getTitle() + " " + item.getStore());
            }
        }
		return result;
	}
	public ArrayList<Product> recomended(String storeFave,String price){
		int max=10;//max number of products to retreive
		int found=0;
		int ms=0;
		int mas=1000;
		if(price=="one"){
			ms=0;
			mas=20;
		}if(price=="two"){
			ms=20;
			mas=50;
		}
		if(price=="three"){
			ms=50;
			mas=100;
		}
		if(price=="four"){
			ms=100;
			mas=300;
		}
		if(price=="five"){
			ms=300;
			mas=300000;
		}
		ArrayList<Product> products= new ArrayList<Product>();
		ArrayList<Product> rands =this.getRandomProducts(200);
		
		for(Product p :rands){
			if(p.getPrice()>=ms&&p.getPrice()<=mas){
				//System.out.println("found a match");
				if(p.getStore().contentEquals(storeFave)){
					System.out.println("found a match");
					products.add(p);
					found++;
					if(found==max){
						return products;
					}
				}
			}
		}
		
		
		
		//
//		//DBObject query = new Product(new StandardAnalyzer()).createQuery("store",storeFave, Condition.ALL);
//		DBCursor cursor = productCollection.find();	
//		//cursor.sort(new BasicDBObject("price", 1));
//		System.out.println("found a match");
//		while(cursor.hasNext()){
//			System.out.println("found a match");
//			BasicDBObject result = (BasicDBObject) cursor.next();
//			
//			String title = result.getString("title");
//			String store = result.getString("store");
//			String url = result.getString("url");
//			String brand = result.getString("brand");
//			Double priceed = result.getDouble("price");
//			if(priceed>=ms&&priceed<=mas){
//			Product p = new Product(title,store,url,priceed);
//			products.add(p);
//			System.out.println("found a match");
//			found++;
//			if(found==max){
//				return products;
//			}
//			}
//		}
		return products;
	}

	public ArrayList<Product> queryTop(String queryString, int n){
		ArrayList<Product> products = new ArrayList<Product>();
		try {
			DBObject query = new Product(new StandardAnalyzer()).createQuery("indexed_text",queryString, Condition.ALL);
			DBCursor cursor = productCollection.find(query);	
			cursor.sort(new BasicDBObject("price", 1)).limit(n);
			while(cursor.hasNext()){
				BasicDBObject result = (BasicDBObject) cursor.next();
				String title = result.getString("title");
				String store = result.getString("store");
				String url = result.getString("url");
				String brand = result.getString("brand");
				Double price = result.getDouble("price");
				Product p = new Product(title,store,url,price);
				products.add(p);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return products;
	}
	public ArrayList<Product> getRandomProducts(int limit){
		ArrayList<Product> products=new ArrayList<Product>();
		long count = productCollection.getCount();
		//int limit = count; //or whatever you want
//https://stackoverflow.com/questions/12912317/get-random-documents-records-from-mongodb-with-java
		if (count <= limit) {//if you are asking for the whole collection or more 
		  DBCursor cursor = productCollection.find();
		  while (cursor.hasNext()) {
			  BasicDBObject result = (BasicDBObject) cursor.next();
				String title = result.getString("title");
				String store = result.getString("store");
				String url = result.getString("url");
				String brand = result.getString("brand");
				Double price = result.getDouble("price");
				Product p = new Product(title,store,url,price);
				products.add(p);
		  }

		} else {//should always fall in here 
		  long skip = Math.round((double) count / limit);

		  DBCursor cursor = productCollection.find();

		  while (products.size() < limit) {
		    int offset = (int) ((skip * products.size() + (int) ((Math.random() * skip) % count)) % count);
		    //System.out.println(offset);
		    DBObject next = cursor.skip(offset).next();
		    BasicDBObject result = (BasicDBObject)next;
		    String title = result.getString("title");
			String store = result.getString("store");
			String url = result.getString("url");
			String brand = result.getString("brand");
			Double price = result.getDouble("price");
			Product p = new Product(title,store,url,price);
			products.add(p);
		    cursor = productCollection.find();
		  }

		}
		return products;
	}

}
