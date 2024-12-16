import pandas as pd
import re
from google.cloud import bigtable
from google.cloud.bigtable import column_family
import unicodedata

# File path to CSV
file_path = 'D:\\Portfolio\\product-recommendation\\data\\amz_uk_processed_data.csv'

# Read the CSV file into a DataFrame
data = pd.read_csv(file_path)
print("Sample Data:")
print(data.head())

# Data Cleaning
data['asin'] = data['asin'].str.strip()
data['title'] = data['title'].str.strip()
data['categoryName'] = data['categoryName'].str.strip()
data['price'] = pd.to_numeric(data['price'], errors='coerce')
data['stars'] = pd.to_numeric(data['stars'], errors='coerce')
data['reviews'] = pd.to_numeric(data['reviews'], errors='coerce')
data['isBestSeller'] = data['isBestSeller'].astype(str).str.upper() == 'TRUE'
data['boughtInLastMonth'] = data['boughtInLastMonth'].fillna(0).astype(int)

# Remove duplicates based on 'asin'
data = data.drop_duplicates(subset='asin')

# Drop rows with critical missing values
data = data.dropna(subset=['asin', 'title', 'categoryName'])

# Validate URLs
valid_url_regex = re.compile(r'^https?://')
data = data[data['imgUrl'].apply(lambda x: bool(valid_url_regex.match(str(x))))]
data = data[data['productURL'].apply(lambda x: bool(valid_url_regex.match(str(x))))]

# Function to remove non-ASCII characters
def clean_text(text):
    if isinstance(text, str):
        # Normalize the text and remove non-ASCII characters
        text = unicodedata.normalize('NFKD', text)  # Normalize to Unicode
        text = ''.join([c for c in text if unicodedata.category(c) != 'Mn'])  # Remove diacritics
        text = text.encode('ascii', errors='ignore').decode('ascii')  # Remove non-ASCII characters
    return text

# Apply the clean_text function to relevant columns
data['asin'] = data['asin'].apply(clean_text)
data['title'] = data['title'].apply(clean_text)
data['categoryName'] = data['categoryName'].apply(clean_text)
data['imgUrl'] = data['imgUrl'].apply(clean_text)
data['productURL'] = data['productURL'].apply(clean_text)

print(f"Data prepared: {data.shape[0]} rows remain after cleaning.")

# Initialize Bigtable client and instance
try:
    client = bigtable.Client(project="virtualization-and-cloud", admin=True)
    instance = client.instance("product-recommendation")
    table = instance.table("products")

    # Check if the table exists, and create it if it doesn't
    if not table.exists():
        print("Table does not exist. Creating table...")
        column_families = {
            "metadata": column_family.MaxVersionsGCRule(1),
            "attributes": column_family.MaxVersionsGCRule(1),
            "links": column_family.MaxVersionsGCRule(1),
        }
        table.create(column_families=column_families)
        print("Table created successfully.")
    else:
        print("Table already exists.")

    # Prepare data for insertion
    batch_size = 10000  # Maximum mutations per request
    rows = []

    for index, row in data.iterrows():
        # Encode the row key using UTF-8
        row_key = row['asin'].encode('utf-8', errors='ignore')  # Ignore encoding errors

        # Create a new row object
        bt_row = table.row(row_key)

        # Set cells (convert all non-string values to strings explicitly)
        bt_row.set_cell("metadata", "title", str(row['title']))
        bt_row.set_cell("metadata", "categoryName", str(row['categoryName']))
        bt_row.set_cell("attributes", "stars", str(row['stars']))
        bt_row.set_cell("attributes", "reviews", str(row['reviews']))
        bt_row.set_cell("attributes", "price", str(row['price']))
        bt_row.set_cell("attributes", "isBestSeller", str(row['isBestSeller']))
        bt_row.set_cell("attributes", "boughtInLastMonth", str(row['boughtInLastMonth']))
        bt_row.set_cell("links", "imgUrl", str(row['imgUrl']))
        bt_row.set_cell("links", "productURL", str(row['productURL']))

        # Append the row to the batch
        rows.append(bt_row)

        # If the batch size exceeds the limit, mutate the rows and reset the batch
        if len(rows) >= batch_size:
            table.mutate_rows(rows)
            print(f"Batch of {len(rows)} rows mutated into Bigtable.")
            rows = []  # Reset the batch

    # Mutate any remaining rows that were not yet inserted
    if rows:
        table.mutate_rows(rows)
        print(f"Final batch of {len(rows)} rows mutated into Bigtable.")

    print("Data successfully loaded into Bigtable.")

except Exception as e:
    print(f"An error occurred: {e}")