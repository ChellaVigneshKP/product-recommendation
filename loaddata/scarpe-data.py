import re

from bs4 import BeautifulSoup
import requests
def main(URL):
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64 x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
    }
    try:
        response = requests.get(URL, headers=headers, timeout=10)
        if response.status_code == 200:
            soup = BeautifulSoup(response.content, "html.parser")
            product_name = soup.find("span", attrs={"id": 'productTitle'}).text.strip()
            # price = soup.find("span", {"class": "a-price-range"}).text.strip()
            # match = re.search(r"[\d,]+\.\d{2}", price)
            # f_price = match.group() if match else "Price not found"
            product_url = soup.find("img", {"id": "landingImage"})
            img_alt = product_url.get('alt')
            src_url = product_url.get('src')
            input_asin = soup.find("input", {"id": "ASIN"})
            input_asin_value = input_asin["value"] if input_asin else None
            print(f"Product Name: {product_name}")
            # print(f"Product Price: {f_price}")
            print(f"Product Image URL: {src_url}")
            print(f"Product Image Alt Text: {img_alt}")
            print(f"Input ASIN: {input_asin_value}")
        else:
            print(f"URL returned status {response.status_code}: {URL}")
    except requests.RequestException as e:
        print(f"Failed to reach URL: {URL} - Error: {e}")

# URL of the product page to scrape
url = "https://www.amazon.co.uk/dp/B0C7VF96BP"
url2="https://www.amazon.co.uk/dp/B0CC98XHXW"
url3="https://www.amazon.co.uk/dp/1988884047"
main(url)
main(url2)
main(url3)