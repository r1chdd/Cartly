import os
import json
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.common.by import By
from webdriver_manager.chrome import ChromeDriverManager
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
import time

def init_firebase():
    firebase_json = os.environ.get("FIREBASE_CREDENTIALS")
    if firebase_json:
        print("Using Firebase credentials from environment...")
        cred_dict = json.loads(firebase_json)
        cred = credentials.Certificate(cred_dict)
    else:
        print("Using Firebase credentials from file...")
        cred = credentials.Certificate("cartly-firebase.json")
    firebase_admin.initialize_app(cred)
    return firestore.client()

def categorize(name):
    name_lower = name.lower()
    if any(w in name_lower for w in ["milk", "cheese", "yoghurt", "butter", "cream", "dairy"]):
        return "Dairy"
    elif any(w in name_lower for w in ["bread", "roll", "bun", "bagel", "toast"]):
        return "Bread & Bakery"
    elif any(w in name_lower for w in ["chicken", "beef", "lamb", "pork", "mince", "braai", "meat", "sausage", "wors"]):
        return "Meat & Poultry"
    elif any(w in name_lower for w in ["apple", "banana", "orange", "tomato", "potato", "vegetable", "fruit", "lettuce", "grapes", "avocado"]):
        return "Fruit & Veg"
    elif any(w in name_lower for w in ["cola", "juice", "water", "drink", "soda", "beer", "wine", "cider", "seltzer", "cooler", "spritzer", "guarana"]):
        return "Drinks"
    elif any(w in name_lower for w in ["oil", "cooking", "sunflower", "olive"]):
        return "Cooking & Oils"
    elif any(w in name_lower for w in ["chips", "chocolate", "sweets", "biscuit", "snack", "cookie"]):
        return "Snacks & Treats"
    elif any(w in name_lower for w in ["washing", "cleaning", "soap", "detergent", "bleach"]):
        return "Household"
    else:
        return "Other"

def get_emoji(category):
    return {
        "Dairy": "🥛",
        "Bread & Bakery": "🍞",
        "Meat & Poultry": "🍗",
        "Fruit & Veg": "🥦",
        "Drinks": "🥤",
        "Cooking & Oils": "🫙",
        "Snacks & Treats": "🍫",
        "Household": "🧹",
        "Other": "🛒"
    }.get(category, "🛒")

def clean_price(text):
    """Extract only the first price from a string like 'R16.99 R17.69'"""
    if not text:
        return ""
    text = text.strip()
    parts = text.split()
    for part in parts:
        if part.startswith("R") and any(c.isdigit() for c in part):
            return part
    return text

def scrape_pnp(db):
    print("Starting Pick n Pay scraper...")
    options = webdriver.ChromeOptions()
    options.add_argument("--headless")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")
    options.add_argument("--disable-blink-features=AutomationControlled")
    options.add_argument("--window-size=1920,1080")
    options.add_experimental_option("excludeSwitches", ["enable-automation"])
    options.add_experimental_option("useAutomationExtension", False)
    options.add_argument("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36")

    driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)
    driver.execute_script("Object.defineProperty(navigator, 'webdriver', {get: () => undefined})")

    try:
        print("Opening Pick n Pay specials page...")
        driver.get("https://www.pnp.co.za/b/PNP?sortCode=most-popular&q=%3Amost-popular%3AonPromotion%3Atrue")
        print("Waiting 25 seconds for products to load...")
        time.sleep(25)

        print("Page title: " + driver.title)
        print("Page URL: " + driver.current_url)

        products = driver.find_elements(By.CSS_SELECTOR, "div[class*='product']")
        print("Total product cards found: " + str(len(products)))

        seen = set()
        deals = []

        for product in products:
            try:
                name = ""
                price_now = ""
                price_was = ""
                image_url = ""

                # Get name
                for ns in ["a[class*='product']", "span[class*='name']", "div[class*='name']", "p[class*='name']", "h3", "h2", "a"]:
                    try:
                        elem = product.find_element(By.CSS_SELECTOR, ns)
                        name = elem.get_attribute("aria-label") or elem.text
                        if name and len(name) > 3:
                            break
                    except:
                        pass

                # Skip blank or already seen
                if not name or len(name) <= 3 or name in seen:
                    continue

                # Get prices
                price_elems = []
                for ps in ["span[class*='price']", "div[class*='price']", "span[class*='Price']", "p[class*='price']"]:
                    try:
                        found = product.find_elements(By.CSS_SELECTOR, ps)
                        if found:
                            price_elems = found
                            break
                    except:
                        pass

                if price_elems:
                    price_now = clean_price(price_elems[0].text)
                if len(price_elems) > 1:
                    price_was = clean_price(price_elems[1].text)

                # Skip if price_now and price_was are the same (means only one price exists)
                if price_was == price_now:
                    price_was = ""

                # Get image
                try:
                    img = product.find_element(By.CSS_SELECTOR, "img")
                    image_url = img.get_attribute("src") or img.get_attribute("data-src") or ""
                except:
                    pass

                if name and price_now:
                    seen.add(name)
                    category = categorize(name)
                    emoji = get_emoji(category)
                    deals.append({
                        "name": name,
                        "price_now": price_now,
                        "price_was": price_was,
                        "store": "Pick n Pay",
                        "category": category,
                        "emoji": emoji,
                        "distance": "Nearby",
                        "image_url": image_url
                    })
                    print("- " + name + " | " + price_now + " | " + (price_was if price_was else "no was-price") + " | " + category)

            except Exception as e:
                pass

        print("Total deals scraped: " + str(len(deals)))

        if len(deals) > 0:
            print("Pushing to Firebase...")
            existing = db.collection("deals").where("store", "==", "Pick n Pay").get()
            for doc in existing:
                doc.reference.delete()
            for i, deal in enumerate(deals):
                db.collection("deals").document("pnp_" + str(i)).set(deal)
            print("Successfully pushed " + str(len(deals)) + " deals to Firebase!")
        else:
            print("No deals found - not updating Firebase")

    except Exception as e:
        print("Error: " + str(e))
        import traceback
        traceback.print_exc()
    finally:
        driver.quit()

print("Initialising Firebase...")
db = init_firebase()
scrape_pnp(db)
print("All done!")