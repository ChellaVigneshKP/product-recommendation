import pandas as pd
file_path='D:\\Portfolio\\product-recommendation\\data\\amz_uk_processed_data.csv'
df=pd.read_csv(file_path)
unique_urls = df["productURL"].drop_duplicates().str.strip()
output_file = "product_urls.txt"
unique_count = df["productURL"].nunique()
print(f"The number of unique product URLs is: {unique_count}")
with open(output_file, "w") as file:
    file.write("\n".join(unique_urls))
print(f"Unique product URLs have been saved to {output_file}")