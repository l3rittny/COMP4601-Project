import sys
import platform
import json
from pymongo import MongoClient
from bs4 import BeautifulSoup
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from seleniumrequests import Chrome, Safari
import requests
import re

def scrapeDeserres():
	url = "https://www.deserres.ca/en/brands"
	res =  requests.get(url);
	soup = BeautifulSoup(res.text, "lxml")

	raw_links = soup.find_all("a", href=re.compile("brands\/"))

	brand_links = []
	product_links = [] 
	product_map = {}

	for a in raw_links:
		if a['href'] not in brand_links:
			brand_links.append(a['href'])
			res =  requests.get(a['href']+"?limit=all")
			pageSoup = BeautifulSoup(res.text, "lxml")
			product_headers = pageSoup.find_all("h2", {"class": "product-name"})
			for h2 in product_headers:
				raw_product_links = h2.find_all("a")
				for product_link in raw_product_links:
					if product_link['href'] not in product_links:
						product_links.append(product_link['href'])
						if product_link['data-price']:
							product_map[product_link['href']] = {
								'title': product_link.contents[0],
								'description': "",
								'price': product_link['data-price'],
								'url': product_link['href'],
								'brand': product_link['data-brand'],
								'store': "Deserres"
							}
						else:
							print("error")
							print(product_link.contents)
							break
	#print(product_map)
	print("Done Deserres!!")
	#Save our results
	f = open("deserres.json","w+")
	f.write(json.dumps(list(product_map.values())))
	f.close()

#In order of annoying to do
#https://store.abovegroundartsupplies.com/pages/shop_by_category.html
#	https://store.abovegroundartsupplies.com/departments/charcoal-%7CC1.html?top=0
def scrapeAboveGroundArtSupplies():
	url = "https://store.abovegroundartsupplies.com/pages/shop_by_category.html"
	# get all a href
	res =  requests.get(url);
	soup = BeautifulSoup(res.text, "lxml")
	products = []
	product_links = []
	categories = soup.find_all("div", {"class": "categories-wrapper"})
	#print(categories[0].contents)
	if len(categories):
		cat_links = categories[0].find_all("a") #gets all category links
		for cat_link in cat_links:
			#print(cat_link['href'])
			res =  requests.get(cat_link['href'])
			pageSoup = BeautifulSoup(res.text, "lxml")
			departments = pageSoup.find_all("div", {"class": "category_container"})
			for department in departments:
				#print(department)
				department_links = department.find_all("a")
				for department_link in department_links:
					curr = 0;
					maxItems = 0;
					print(curr)
					while curr < maxItems or curr == 0: #ask for all the pages in the table
						res =  requests.get("https://store.abovegroundartsupplies.com"+ department_link['href'] + "?top=" + str(15*curr))
						deptPageSoup = BeautifulSoup(res.text, "lxml")
						maxItemTD = deptPageSoup.find_all("td", {"class": "message"})
						#print(maxItemTD)
						if len(maxItemTD):
							maxItems = int(re.search(r'\d+', maxItemTD[0].contents[0].replace(',', '')).group()) + 15
						else:
							break
						print(curr)
						print(maxItems)
						product_listings = deptPageSoup.find_all("div", {"class": "prod_list"})
						for product_listing in product_listings:
							name_div = product_listing.find_all("div", {"class": "item_name"})
							if len(name_div):
								links = name_div[0].find_all("a")
								price = product_listing.find_all("div", {"class": "product_price"})
								if len(links) and links[0]['href'] not in product_links:
									print("product: https://store.abovegroundartsupplies.com"+links[0]['href'])
									product_links.append(links[0]['href'])
									products.append({
										'title': links[0].contents[0],
										'description': "",
										'price': price[0].contents[0],
										'url': "https://store.abovegroundartsupplies.com"+links[0]['href'],
										'brand': "",
										'store': "Above Ground Art Supplies"
									})
						curr = curr + 15
	print(products)
	#Save our results
	f = open("abovegroundartsupplies.json","w+")
	f.write(json.dumps(products))
	f.close()
	print("Done Above ground art supplies!!")
#scrapeAboveGroundArtSupplies()

#http://www.artshack.ca/index.php/art-supplies.html
#	http://www.artshack.ca/index.php/art-supplies.html?p=1 to
#   http://www.artshack.ca/index.php/art-supplies.html?p=61
def scrapeArtshack():
	url = "http://www.artshack.ca/index.php/art-supplies.html"
	products = []
	product_links = []
	for i  in range(1, 62):
		res =  requests.get(url + "?p=" + str(i));
		soup = BeautifulSoup(res.text, "lxml")
		product_containers = soup.find_all("div", {"class": "product-container"})
		#product-name
		for product_container in product_containers:
			names = product_container.find_all("h2", {"class": "product-name"})
			prices = product_container.find_all("span", {"class": "price"})
			if len(names) and len(prices):
				links = names[0].find_all("a")
				if len(links) and links[0]['href'] not in product_links:
					product_links.append(links[0]['href'])
					print(links[0]['href'])
					products.append({
						'title': links[0].contents[0],
						'description': "",
						'price': prices[0].contents[0],
						'url': links[0]['href'],
						'brand': "",
						'store': "Art Shack"
					})
	print(products)
	#Save our results
	f = open("artshack.json","w+")
	f.write(json.dumps(products))
	f.close()
	print("Done Art Shack!!")
#scrapeArtshack()

#http://www.jerrysartarama.com/all-products
#	http://www.jerrysartarama.com/all-products?p=2
def scrapeJerrysArtarama():
	url = "http://www.jerrysartarama.com/all-products" 
	products = []
	product_links = []
	for i  in range(1, 66):
		res =  requests.get(url + "?p=" + str(i));
		soup = BeautifulSoup(res.text, "lxml")
		product_containers = soup.find_all("div", {"class": "product-info"})
		for product_container in product_containers:
			names = product_container.find_all("h2", {"class": "product-name"})
			prices = product_container.find_all("p", {"class": "minimal-price"})
			if len(names) and len(prices):
				prices = prices[0].find_all("span", {"class" : "price"})
				links = names[0].find_all("a")
				if len(links) and links[0]['href'] not in product_links:
					product_links.append(links[0]['href'])
					print(links[0]['href'] + " " + prices[0].contents[0])
					products.append({
						'title': links[0].contents[0],
						'description': "",
						'price': prices[0].contents[0],
						'url': links[0]['href'],
						'brand': "",
						'store': "Jerrys"
					})
	print(products)
	#Save our results
	f = open("jerrys_USA.json","w+")
	f.write(json.dumps(products))
	f.close()
	print("Done Jerrys Artarama!!")
#scrapeJerrysArtarama()
#https://wallacks.com/collections - paginated tables
#	https://wallacks.com/collections/adhesives?page=2
def scrapeWallacks():
	url = "https://wallacks.com/search?q=%2A&type=product" #search with a regex *
	products = []
	product_links = []
	for i in range (1, 64):
		res =  requests.get(url + "&page=" + str(i));
		soup = BeautifulSoup(res.text, "lxml")
		product_containers = soup.find_all("a", {"class": "product-grid-item"})
		for product_container in product_containers:
			link = "https://wallacks.com/" + product_container['href']
			print(link)
			price_wrap = product_container.find_all("span", {"class": "h1 medium--left"})
			paras = product_container.find_all("p")
			if len(price_wrap) and len(paras):
				spans = price_wrap[0].find_all("span")
				if len(spans) > 1:
					product_links.append(link)
					print(paras[0].contents[0])
					products.append({
						'title': paras[0].contents[0],
						'description': "",
						'price': spans[1].contents[0],
						'url': link,
						'brand': "",
						'store': "Wallacks"
					})
	print(products)
	#Save our results
	f = open("wallacks.json","w+")
	f.write(json.dumps(products))
	f.close()
	print("Done Wallacks!!")
scrapeWallacks()
#https://www.staples.ca/en/Arts-Crafts/cat_CG2623_2-CA_1_20001
#https://www.staples.ca/en/Office-Supplies/cat_SC5218_2-CA_1_20001
#def scrapeStaples():
#	url = "https://www.staples.ca/en/Arts-Crafts/cat_CG2623_2-CA_1_20001"
#	print("Done Staples!!")

#https://www.currys.com/catalogpc.htm?Category=shop_by_brand - too old and gross to scrape
#def scrapeCurrys():
#	print("Done Currys!!")