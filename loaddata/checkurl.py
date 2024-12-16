import requests

# File paths
input_file = "product_urls.txt"  # Replace with your actual text file path
output_file = "valid_urls.txt"  # Output file for valid URLs

# HTTP headers
headers = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36"
}
# Read URLs from file
with open(input_file, "r") as file:
    urls = [line.strip() for line in file if line.strip()]

valid_urls = []
batch_size = 1000  # Define batch size for saving
count = 0

# Open the output file in append mode
with open(output_file, "a") as file:
    # Check the HTTP status of each URL
    for url in urls:
        try:
            response = requests.get(url, headers=headers, timeout=10)
            count += 1
            if response.status_code == 200:
                valid_urls.append(url)
            else:
                print(f"URL returned status {response.status_code}: {url}")

            # Save to file in batches
            if len(valid_urls) >= batch_size:
                file.write("\n".join(valid_urls) + "\n")
                file.flush()  # Ensure data is written to disk
                print(f"Saved {len(valid_urls)} valid URLs to file.")
                valid_urls = []  # Clear the list

            if count % 1000 == 0:
                print(f"Processed {count} URLs")
        except requests.RequestException as e:
            print(f"Failed to reach URL: {url} - Error: {e}")

    # Save any remaining valid URLs
    if valid_urls:
        file.write("\n".join(valid_urls) + "\n")
        print(f"Saved remaining {len(valid_urls)} valid URLs to file.")

print(f"Processing complete. Valid URLs have been saved to {output_file}.")
