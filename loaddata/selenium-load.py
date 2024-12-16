import re

from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.common.exceptions import NoSuchElementException

def main(URL):
    # Set up Selenium WebDriver (Headless mode for performance)
    options = Options()
    options.add_argument("--headless")  # Run browser in headless mode
    options.add_argument("--disable-gpu")
    options.add_argument("--no-sandbox")
    options.add_argument("--disable-dev-shm-usage")

    # Path to your ChromeDriver
    service = Service("C:\\Users\\Chella Vignesh K P\\Downloads\\chromedriver-win64\\chromedriver-win64\\chromedriver.exe")  # Update the path to ChromeDriver
    driver = webdriver.Chrome(service=service, options=options)

    try:
        # Open the URL
        driver.get(URL)

        # Scrape product name
        try:
            product_name = driver.find_element(By.ID, "productTitle").text.strip()
        except NoSuchElementException:
            product_name = "Product name not found"

        # Scrape product image URL and alt text
        try:
            product_image = driver.find_element(By.ID, "landingImage")
            img_alt = product_image.get_attribute("alt")
            src_url = product_image.get_attribute("src")
        except NoSuchElementException:
            img_alt = "Image alt text not found"
            src_url = "Image URL not found"

        # Scrape ASIN
        try:
            input_asin = driver.find_element(By.ID, "ASIN")
            input_asin_value = input_asin.get_attribute("value")
        except NoSuchElementException:
            input_asin_value = "ASIN not found"

        # Scrape breadcrumb categories
        try:
            breadcrumb_container = driver.find_element(By.ID, "wayfinding-breadcrumbs_container")
            breadcrumb_items = breadcrumb_container.find_elements(By.TAG_NAME, "li")
            categories = [item.text.strip() for item in breadcrumb_items if item.text.strip() and item.text.strip() != '›']
            category_list = categories if categories else ["Category not found"]
        except NoSuchElementException:
            category_list = ["Breadcrumb container not found"]

        try:
            product_description = driver.find_element(By.ID, "productDescription").text.strip()
        except NoSuchElementException:
            product_description = "Product description not found"

        try:
            feature_bullets_container = driver.find_element(By.ID, "detailBulletsWrapper_feature_div")
            feature_bullets = [
                bullet.text.strip()
                for bullet in feature_bullets_container.find_elements(By.TAG_NAME, "li")
                if bullet.text.strip()
            ]
        except NoSuchElementException:
            feature_bullets = ["Feature bullets not found"]

        try:
            rating_element = driver.find_element(By.CLASS_NAME, "a-icon-alt")
            rating = rating_element.get_attribute("textContent").strip()
        except NoSuchElementException:
            rating = "Rating not found"

        try:
            # Attempt to find the standard price
            try:
                price = driver.find_element(By.ID, "priceblock_ourprice").text.strip()
            except NoSuchElementException:
                # Attempt to find the deal price
                price = driver.find_element(By.ID, "priceblock_dealprice").text.strip()
        except NoSuchElementException:
            # As a fallback, check the core price container
            try:
                core_price = driver.find_element(By.ID, "corePrice_feature_div")
                price = core_price.text.strip().split('\n')[0]  # Extract the first line as price
            except NoSuchElementException:
                price = "Price not found"

        try:
            # First, look for a price range
            price_range = driver.find_element(By.CLASS_NAME, "a-price-range").text.strip()
        except NoSuchElementException:
            # Fallback to standard price
            try:
                price_range = driver.find_element(By.ID, "priceblock_ourprice").text.strip()
            except NoSuchElementException:
                try:
                    price_range = driver.find_element(By.ID, "priceblock_dealprice").text.strip()
                except NoSuchElementException:
                    price_range = "Price not found"
        try:
            price_element = driver.find_element(By.ID, "color_name_0_price")
            price_text = price_element.find_element(By.CLASS_NAME, "olpMessageWrapper").text.strip()
            price_1 = price_text.split("\n")[-1]
        except NoSuchElementException:
            price_1 = "Price not found"

        try:
            brand_element = driver.find_element(By.ID, "bylineInfo")
            raw_brand_text = brand_element.text.strip()
        except NoSuchElementException:
            raw_brand_text = "Brand name not found"

        if "Visit the" in raw_brand_text and "Store" in raw_brand_text:
            # Extract text between "Visit the" and "Store"
            brand_name = re.search(r"Visit the (.+?) Store", raw_brand_text)
            brand_name = brand_name.group(1) if brand_name else raw_brand_text
        else:
            brand_name = raw_brand_text

        try:
            logo_element = driver.find_element(By.XPATH, "//div[@id='brand']//img")
            logo_url = logo_element.get_attribute("src")  # Get the image source URL
        except NoSuchElementException:
            logo_url = "Brand logo not found"

        try:
            ratings_element = driver.find_element(By.ID, "acrCustomerReviewText")
            raw_ratings_text = ratings_element.text.strip()  # e.g., "1,234 ratings"
            # Extract the numeric value using regex
            match = re.search(r"[\d,]+", raw_ratings_text)
            ratings_count = match.group() if match else "Ratings count not found"
        except NoSuchElementException:
            ratings_count = "Ratings count not found"

        try:
            stock_element = driver.find_element(By.ID, "availability")
            raw_stock_text = stock_element.text.strip()  # e.g., "Only 3 left in stock - order soon."
            # Extract numeric stock count using regex
            match = re.search(r"Only (\d+) left in stock", raw_stock_text)
            stock_count = match.group(1) if match else raw_stock_text  # If no match, use the raw text
        except NoSuchElementException:
            stock_count = "Stock information not found"

        warranty_info = "Warranty information not found"

        # Check in product description
        try:
            description = driver.find_element(By.ID, "productDescription")
            if "warranty" in description.text.lower():
                warranty_info = description.text.strip()
        except NoSuchElementException:
            pass

        # Check in bullet points
        try:
            bullets = driver.find_element(By.ID, "feature-bullets")
            bullet_points = bullets.find_elements(By.TAG_NAME, "li")
            for bullet in bullet_points:
                if "warranty" in bullet.text.lower():
                    warranty_info = bullet.text.strip()
                    break
        except NoSuchElementException:
            pass

        try:
            review_containers = driver.find_elements(By.XPATH, "//div[contains(@id, 'customer_review-')]")
            reviews = []

            for container in review_containers[:5]:  # Limit to 5 reviews for sampling
                try:
                    # Extract review title
                    title_element = container.find_element(By.CLASS_NAME, "review-title")
                    review_title = title_element.text.strip()
                except NoSuchElementException:
                    review_title = "Title not found"

                try:
                    # Extract review text
                    text_element = container.find_element(By.CLASS_NAME, "review-text-content")
                    review_text = text_element.text.strip()
                except NoSuchElementException:
                    review_text = "Text not found"

                try:
                    # Extract review rating
                    rating_element = container.find_element(By.CLASS_NAME, "a-icon-alt")
                    review_rating = rating_element.get_attribute("textContent").strip()
                except NoSuchElementException:
                    review_rating = "Rating not found"

                # Add review to the list
                reviews.append({
                    "title": review_title,
                    "text": review_text,
                    "rating": review_rating
                })

        except NoSuchElementException:
            reviews = "No reviews found"

            # Print sample reviews
        # for i, review in enumerate(reviews, 1):
        #     print(f"Review {i}:")
        #     print(f"Title: {review['title']}")
        #     print(f"Text: {review['text']}")
        #     print(f"Rating: {review['rating']}")
        #     print("-" * 50)

        # Print warranty information
        # print(f"Warranty Information: {warranty_info}")
        # Print the results
        print(f"Product Name: {product_name}")
        print(f"Product Image URL: {src_url}")
        # print(f"Product Image Alt Text: {img_alt}")
        print(f"Input ASIN: {input_asin_value}")
        # print(f"Categories: {category_list}")
        print(f"Product Description: {product_description}")
        print(f"Feature Bullets: {feature_bullets}")
        print(f"Rating: {rating}")
        # print(f"Price: {price}")
        # print(f"Price Range: {price_range}")
        # print(f"Color Price: {price_1}")
        # print(f"Brand Name: {brand_name}")
        # print(f"Logo URL: {logo_url}")
        print(f"Ratings Count: {ratings_count}")
        print(f"Stock Count: {stock_count}")

    except Exception as e:
        print(f"Failed to scrape {URL} - Error: {e}")
    finally:
        # Quit the driver
        driver.quit()

# URLs of the product pages to scrape
url1 = "https://www.amazon.co.uk/dp/B0C7VF96BP"
url2 = "https://www.amazon.co.uk/dp/B0CC98XHXW"
url3 = "https://www.amazon.co.uk/dp/1988884047"
url4 = "https://www.amazon.in/Apple-iPhone-13-128GB-Starlight/dp/B09G9D8KRQ/ref=sr_1_3?sr=8-3"
main(url1)
main(url2)
main(url3)
main(url4)