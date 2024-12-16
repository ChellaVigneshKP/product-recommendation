import json
import re
import random
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import NoSuchElementException
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium_stealth import stealth
from fake_useragent import UserAgent
def extract_technical_details(driver):
    technical_details = "Technical details not found"

    # Primary Method: Using `productDetails_techSpec_section_1`
    try:
        tech_details_section = driver.find_element(By.ID, "productDetails_techSpec_section_1")
        technical_details = tech_details_section.text.strip()
    except NoSuchElementException:
        pass

    # Fallback 1: Using `productDetails_detailBullets_sections1`
    if technical_details == "Technical details not found":
        try:
            detail_bullets_section = driver.find_element(By.ID, "productDetails_detailBullets_sections1")
            technical_details = detail_bullets_section.text.strip()
        except NoSuchElementException:
            pass

    # Fallback 2: JSON-LD Structured Data
    if technical_details == "Technical details not found":
        try:
            script = driver.find_element(By.XPATH, "//script[@type='application/ld+json']")
            json_data = json.loads(script.get_attribute("textContent"))
            tech_details_json = json_data.get("specifications", [])
            if not tech_details_json:
                tech_details_json = json_data.get("additionalProperty", [])
            technical_details = (
                "\n".join([f"{item['name']}: {item['value']}" for item in tech_details_json])
                if tech_details_json else "Technical details not found"
            )
        except (NoSuchElementException, KeyError):
            pass

    # Fallback 3: Using `detailBulletsWrapper_feature_div`
    if technical_details == "Technical details not found":
        try:
            detail_bullets_container = driver.find_element(By.ID, "detailBulletsWrapper_feature_div")
            bullets = detail_bullets_container.find_elements(By.TAG_NAME, "li")
            tech_details_bullets = [
                bullet.text.strip()
                for bullet in bullets
                if bullet.text.strip() and ":" in bullet.text.strip()
            ]
            technical_details = "\n".join(tech_details_bullets)
        except NoSuchElementException:
            pass

    # Fallback 4: Product Description
    if technical_details == "Technical details not found":
        try:
            product_description = driver.find_element(By.ID, "productDescription").text.strip()
            if "Technical" in product_description or "Specifications" in product_description:
                technical_details = product_description
        except NoSuchElementException:
            pass

    return technical_details

def extract_categories(driver):
    categories = "Category not found"

    # Method 1: Breadcrumbs
    try:
        breadcrumb_container = driver.find_element(By.ID, "wayfinding-breadcrumbs_container")
        breadcrumb_items = breadcrumb_container.find_elements(By.TAG_NAME, "li")
        categories = [item.text.strip() for item in breadcrumb_items if item.text.strip() != '›']
    except NoSuchElementException:
        pass

    # Method 2: JSON-LD Metadata
    if categories == "Category not found":
        try:
            script = driver.find_element(By.XPATH, "//script[@type='application/ld+json']")
            json_data = json.loads(script.get_attribute("textContent"))
            category_data = json_data.get("category", "Category not found")
            categories = category_data if isinstance(category_data, list) else [category_data]
        except NoSuchElementException:
            pass

    # Method 3: Best Sellers Section
    if categories == "Category not found":
        try:
            best_sellers_section = driver.find_element(By.XPATH, "//li[contains(., 'Best Sellers Rank')]")
            category_links = best_sellers_section.find_elements(By.TAG_NAME, "a")
            categories = [link.text.strip() for link in category_links if link.text.strip()]
        except NoSuchElementException:
            pass

    # Method 4: Page Headers
    if categories == "Category not found":
        try:
            header = driver.find_element(By.ID, "nav-subnav")
            categories = [item.text.strip() for item in header.find_elements(By.CLASS_NAME, "nav-a") if item.text.strip()]
        except NoSuchElementException:
            pass

    # Method 5: Side Panel
    if categories == "Category not found":
        try:
            side_panel = driver.find_element(By.ID, "s-refinements")
            categories = [item.text.strip() for item in side_panel.find_elements(By.CLASS_NAME, "a-list-item") if item.text.strip()]
        except NoSuchElementException:
            pass

    # Method 6: Product Details Section
    if categories == "Category not found":
        try:
            product_details = driver.find_element(By.ID, "prodDetails")
            details_text = product_details.text.strip()
            categories = [
                line.split(":")[1].strip() for line in details_text.split("\n")
                if "Department" in line or "Category" in line
            ]
        except NoSuchElementException:
            pass

    # Method 7: Parse URL
    if categories == "Category not found" or categories == ["Category not found"]:
        try:
            product_name = driver.find_element(By.ID, "productTitle").text.strip()
            categories = [product_name.split()[0]]  # Use the first word of the product name
        except NoSuchElementException:
            categories = ["Unknown Category"]

    return categories

def extract_product_description(driver):
    description = "Description not found"

    # Primary Method: Using `productDescription` ID
    try:
        description = driver.find_element(By.ID, "productDescription").text.strip()
        if not description:
            description = "Description not found"
    except NoSuchElementException:
        pass

    # Fallback 1: Using `feature-bullets`
    if description == "Description not found":
        try:
            feature_bullets_container = driver.find_element(By.ID, "feature-bullets")
            description = " ".join([
                bullet.text.strip()
                for bullet in feature_bullets_container.find_elements(By.TAG_NAME, "li")
                if bullet.text.strip()
            ])
        except NoSuchElementException:
            pass

    if description == "Description not found":
        try:
            description_list = driver.find_element(By.ID, "productFactsDesktopExpander")
            description = description_list.text.strip()
        except NoSuchElementException:
            pass
    # Fallback 3: Using JSON-LD Structured Data
    if description == "Description not found":
        try:
            script = driver.find_element(By.XPATH, "//script[@type='application/ld+json']")
            json_data = json.loads(script.get_attribute("textContent"))
            description = json_data.get("description", "Description not found")
        except NoSuchElementException:
            pass

    return description

def get_element_text(driver, by, identifier, default="Not found"):
    """Helper function to get element text with exception handling."""
    try:
        element = WebDriverWait(driver, 10).until(EC.presence_of_element_located((by, identifier)))
        return element.text.strip()
    except Exception:
        return default

def get_element_attribute(driver, by, identifier, attribute, default="Not found"):
    """Helper function to get element attribute with exception handling."""
    try:
        element = WebDriverWait(driver, 10).until(EC.presence_of_element_located((by, identifier)))
        return element.get_attribute(attribute).strip()
    except Exception:
        return default

def get_product_details(driver):
    """Extract all possible product details."""
    details = {}

    # Product Name
    details["Product Name"] = get_element_text(driver, By.ID, "productTitle")

    # Product Image
    try:
        product_image = driver.find_element(By.ID, "landingImage")
        details["Product Image URL"] = product_image.get_attribute("src")
        details["Product Image Alt"] = product_image.get_attribute("alt")
    except NoSuchElementException:
        details["Product Image URL"] = "Not found"
        details["Product Image Alt"] = "Not found"

    # ASIN
    details["ASIN"] = get_element_attribute(driver, By.ID, "ASIN", "value", "ASIN not found")
    try:
        rating_element = driver.find_element(By.CLASS_NAME, "a-icon-alt")
        rating_text = rating_element.get_attribute("textContent").strip()

        # Validate the text format
        if "out of 5 stars" in rating_text:
            details["Average Rating"] = rating_text.split()[0]  # Extract the numeric rating
        else:
            details["Average Rating"] = round(random.uniform(1.8, 3.2), 1)  # Fallback value
    except NoSuchElementException:
        details["Average Rating"] = round(random.uniform(1.8, 3.2), 1)
    # Categories (Breadcrumbs)
    details["Categories"] = extract_categories(driver)
    # Product Description
    details["Description"] = extract_product_description(driver)
    # Technical Details
    details["Technical Details"] = extract_technical_details(driver)
    # Bullet Points
    try:
        feature_bullets_container = driver.find_element(By.ID, "feature-bullets")
        bullet_points = feature_bullets_container.find_elements(By.TAG_NAME, "li")
        details["Feature Bullets"] = [bullet.text.strip() for bullet in bullet_points]
    except NoSuchElementException:
        try:
            feature_bullets_container = driver.find_element(By.ID, "detailBulletsWrapper_feature_div")
            feature_bullets = [
                bullet.text.strip()
                for bullet in feature_bullets_container.find_elements(By.TAG_NAME, "li")
                if bullet.text.strip()
            ]
            details["Feature Bullets"] = feature_bullets
        except NoSuchElementException:
            details["Feature Bullets"] = "Bullet points not found"

    # Price
    try:
        details["Price"] = get_element_text(driver, By.ID, "priceblock_ourprice", "Not found")
    except NoSuchElementException:
        details["Price"] = "Not found"

    if details["Price"] == "Not found":
        details["Price"] = random.randint(500, 3000)
        details["Cutted Price"] = details["Price"] + random.randint(int(details["Price"] * 0.2),
                                                                    int(details["Price"] * 0.4))
    else:
        try:
            # Ensure the price is numeric if available
            price_value = float(re.sub(r"[^\d.]", "", details["Price"]))
            details["Price"] = price_value
            details["Cutted Price"] = price_value + random.randint(int(price_value * 0.2), int(price_value * 0.4))
        except ValueError:
            details["Price"] = random.randint(500, 3000)
            details["Cutted Price"] = details["Price"] + random.randint(int(details["Price"] * 0.2),
                                                                        int(details["Price"] * 0.4))


    # Ratings
    details["Ratings"] = get_element_text(driver, By.ID, "acrCustomerReviewText", "Ratings not found")
    if details["Ratings"] == "Ratings not found":
        details["Ratings"] = random.randint(100, 4000)
    # Stock Information
    try:
        stock_element = driver.find_element(By.ID, "availability")
        stock_text = stock_element.text.strip()
        match = re.search(r"Only (\d+) left in stock", stock_text)
        details["Stock Count"] = match.group(1) if match else stock_text
    except NoSuchElementException:
        details["Stock Count"] = "Not found"

    if not isinstance(details["Stock Count"], (int, float)):
        details["Stock Count"] = random.randint(1, 100)
    # Brand Name
    try:
        brand_element = driver.find_element(By.ID, "bylineInfo")
        raw_brand_text = brand_element.text.strip()
        if "Visit the" in raw_brand_text and "Store" in raw_brand_text:
            details["Brand Name"] = re.search(r"Visit the (.+?) Store", raw_brand_text).group(1)
        else:
            details["Brand Name"] = raw_brand_text
    except NoSuchElementException:
        details["Brand Name"] = "Not found"

    if details["Brand Name"] == "Not found" or not details["Brand Name"]:
        if details["Product Name"] != "Not found":
            details["Brand Name"] = details["Product Name"].split()[0]
        else:
            details["Brand Name"] = "Unknown"
    # Reviews
    try:
        review_containers = driver.find_elements(By.XPATH, "//div[contains(@id, 'customer_review-')]")
        reviews = []
        for container in review_containers[:5]:  # Limit to 5 reviews
            review = {}
            try:
                review["Title"] = container.find_element(By.CLASS_NAME, "review-title").text.strip()
            except NoSuchElementException:
                review["Title"] = "Not found"
            try:
                review["Text"] = container.find_element(By.CLASS_NAME, "review-text-content").text.strip()
            except NoSuchElementException:
                review["Text"] = "Not found"
            try:
                review["Rating"] = container.find_element(By.CLASS_NAME, "a-icon-alt").get_attribute("textContent").strip()
            except NoSuchElementException:
                review["Rating"] = "Not found"
            reviews.append(review)
        details["Reviews"] = reviews
    except NoSuchElementException:
        details["Reviews"] = "No reviews found"

    return details


def main(URL):
    # Set up Selenium WebDriver
    options = Options()
    # options.add_argument("--headless")
    ua = UserAgent()
    userAgent = ua.random
    print(userAgent)
    options.add_argument(f'--user-agent={userAgent}')
    service = Service("C:\\Users\\Chella Vignesh K P\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe")
    driver = webdriver.Chrome(service=service, options=options)
    # stealth(driver,
    #         languages=["en-US", "en"],
    #         vendor="Google Inc.",
    #         platform="Win32",
    #         webgl_vendor="Intel Inc.",
    #         renderer="Intel Iris OpenGL Engine",
    #         fix_hairline=True)
    try:
        driver.get(URL)
        product_details = get_product_details(driver)
        missing_details = []
        if product_details["Product Name"] == "Not found":
            missing_details.append("Product Name")
        if product_details["Categories"] == ["Category not found"]:
            missing_details.append("Categories")
        if product_details["Product Image URL"] == "Not found":
            missing_details.append("Product Image URL")
        for key, value in product_details.items():
            print(f"{key}: {value}\n")
        if missing_details:
            print(f"Missing Details for URL: {URL}")
            print(f" - Missing Fields: {', '.join(missing_details)}\n")
    except Exception as e:
        print(f"Failed to scrape {URL} - Error: {e}")
    finally:
        driver.quit()


txt_file_path = "product_urls.txt"

def load_urls_from_txt(file_path):
    try:
        # Read the file and extract non-empty lines
        with open(file_path, 'r') as file:
            urls = [line.strip() for line in file if line.strip()]  # Strip and exclude empty lines
        return urls
    except Exception as e:
        print(f"Error reading text file: {e}")
        return []

urls = load_urls_from_txt(txt_file_path)
for url in urls:
    main(url)
