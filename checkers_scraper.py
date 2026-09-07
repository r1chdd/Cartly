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
    if any(word in name_lower for word in ["milk", "cheese", "yoghurt", "butter", "cream", "dairy"]):
        return "Dairy"
    elif any(word in name_lower for word in ["bread", "roll", "bun", "bagel", "toast"]):
        return "Bread & Bakery"
    elif any(word in name_lower for word in ["chicken", "beef", "lamb", "pork", "mince", "braai", "meat", "sausage", "wors"]):
        return "Meat & Poultry"
    elif any(word in name_lower for word in ["apple", "banana", "orange", "tomato", "potato", "vegetable", "fruit", "lettuce", "grapes", "avocado"]):
        return "Fruit & Veg"
    elif any(word in name_lower for word in ["cola", "juice", "water", "drink", "soda", "beer", "wine", "cider", "seltzer", "cooler", "spritzer", "guarana"]):
        return "Drinks"
    elif any(word in name_lower for word in ["oil", "cooking", "sunflower", "olive"]):
        return "Cooking & Oils"
    elif any(word in name_lower for word in ["chips", "chocolate", "sweets", "biscuit", "snack", "cookie"]):
        return "Snacks & Treats"
    elif any(word in name_lower for word in ["washing", "cleaning", "soap", "detergent", "bleach"]):
        return "Household"
    else:
        return "Other"

def full_price(p):
    parts = [s.text for s in p.find_elements(By.CSS_SELECTOR, "span")]
    text = "".join(parts).strip()
    return text if text else p.text

def join_span_prices(elems):
    """Rejoin split prices like 'R499' + '.99' back into 'R499.99'."""
    prices = []
    i = 0
    while i < len(elems):
        t = elems[i].text.strip()
        if t.startswith("R") and i + 1 < len(elems):
            nxt = elems[i + 1].text.strip()
            if nxt.startswith("."):
                prices.append(t + nxt)
                i += 2
                continue
        prices.append(t)
        i += 1
    return prices

def scrape_checkers(db):
    print("Starting Checkers scraper...")
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
        products = []
        attempts = 0
        while len(products) == 0 and attempts < 3:
            attempts += 1
            print("Opening Checkers specials page (attempt " + str(attempts) + ")...")
            driver.get("https://www.checkers.co.za/specials")
            print("Waiting 20 seconds for products to load...")
            time.sleep(20)

            print("Page title: " + driver.title)

            # Scroll down the page to trigger any lazy-loaded images before scraping
            print("Scrolling to trigger lazy-loaded images...")
            last_height = driver.execute_script("return document.body.scrollHeight")
            for _ in range(6):
                driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
                time.sleep(1.5)
                new_height = driver.execute_script("return document.body.scrollHeight")
                if new_height == last_height:
                    break
                last_height = new_height
            driver.execute_script("window.scrollTo(0, 0);")
            time.sleep(2)

            products = driver.find_elements(By.CSS_SELECTOR, "div[class*='DsB3']")
            print("Found " + str(len(products)) + " product cards")
            if len(products) == 0:
                print("No product cards rendered - the specials page may be empty right now")

        deals = []
        for product in products:
            try:
                name = ""
                price_now = ""
                price_was = ""
                image_url = ""

                try:
                    name_elem = product.find_element(By.CSS_SELECTOR, "a[data-testid='product-card-link']")
                    name = name_elem.get_attribute("aria-label") or name_elem.text
                except:
                    pass

                if not name:
                    try:
                        name = product.find_element(By.CSS_SELECTOR, "p[class*='name']").text
                    except:
                        pass

                if not name:
                    try:
                        name = product.find_element(By.CSS_SELECTOR, "h3").text
                    except:
                        pass

                try:
                    price_elems = product.find_elements(By.CSS_SELECTOR, "span[class*='price']")
                    prices = join_span_prices(price_elems)
                    if prices:
                        price_now = prices[0]
                    if len(prices) > 1:
                        price_was = prices[1]
                except:
                    pass

                if not price_now:
                    try:
                        price_now = product.find_element(By.CSS_SELECTOR, "span[class*='Price']").text
                    except:
                        pass

                # --- Image extraction ---
                try:
                    img_elem = product.find_element(By.CSS_SELECTOR, "img")
                    image_url = (
                        img_elem.get_attribute("src")
                        or img_elem.get_attribute("data-src")
                        or img_elem.get_attribute("data-srcset")
                        or ""
                    )
                    # Some sites use srcset with multiple sizes - grab the first URL if so
                    if image_url and " " in image_url and "," in image_url:
                        image_url = image_url.split(",")[0].strip().split(" ")[0]
                    # Guard against relative/protocol-relative URLs
                    if image_url.startswith("//"):
                        image_url = "https:" + image_url
                    elif image_url.startswith("/"):
                        image_url = "https://www.checkers.co.za" + image_url
                except:
                    pass

                if name and len(name) > 3 and price_now:
                    category = categorize(name)
                    deals.append({
                        "name": name,
                        "price_now": price_now,
                        "price_was": price_was if price_was else "",
                        "store": "Checkers",
                        "category": category,
                        "distance": "Nearby",
                        "image_url": image_url
                    })
                    print("- " + name + " | " + price_now + " | " + category + " | img: " + ("yes" if image_url else "NO IMAGE"))

            except Exception as e:
                pass

        print("Total deals scraped: " + str(len(deals)))
        missing_images = sum(1 for d in deals if not d["image_url"])
        print("Deals missing images: " + str(missing_images) + " / " + str(len(deals)))

        if len(deals) > 0:
            print("Pushing to Firebase...")
            existing = db.collection("deals").where("store", "==", "Checkers").get()
            for doc in existing:
                doc.reference.delete()

            for i, deal in enumerate(deals):
                db.collection("deals").document("checkers_" + str(i)).set(deal)

            print("Successfully pushed " + str(len(deals)) + " deals to Firebase!")
        else:
            print("No deals found - not updating Firebase")

    except Exception as e:
        print("Error: " + str(e))
    finally:
        driver.quit()

print("Initialising Firebase...")
db = init_firebase()
scrape_checkers(db)
print("All done!")