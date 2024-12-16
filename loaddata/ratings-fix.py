import pandas as pd
import random

# Load the dataset
file_path = 'cleaned_fixed_data.csv'  # Replace with the path to your file
df = pd.read_csv(file_path)

# Function to generate random float values between 0 and 5
def generate_random_rating():
    return round(random.uniform(0, 5), 2)

# Update `overall_rating` and `product_rating` fields
df['overall_rating'] = df['overall_rating'].apply(lambda x: generate_random_rating() if x == 'No rating available' else x)
df['product_rating'] = df['product_rating'].apply(lambda x: generate_random_rating() if x == 'No rating available' else x)

# Save the updated dataset
output_file_path = 'updated_dataset.csv'  # Replace with the desired output path
df.to_csv(output_file_path, index=False)

print(f"Updated dataset saved to {output_file_path}")