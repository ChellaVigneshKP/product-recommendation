import pandas as pd
import csv
import re
import json


def clean_field(field):
    """
    Clean individual fields by removing invalid characters and fixing quotes.
    """
    try:
        # Replace invalid JSON-like delimiters (=>) with proper (:) for parsing
        field = re.sub(r'=>', ':', field)

        # Replace single quotes with double quotes for JSON compatibility
        field = re.sub(r"(?<!\\)'", '"', field)

        # Ensure balanced quotes
        if field.count('"') % 2 != 0:
            field += '"'

        # Attempt to parse as JSON if applicable
        if '{' in field and '}' in field:
            field = json.loads(field)  # Try converting to JSON
        return field
    except Exception:
        # Return raw field if parsing fails
        return field


def clean_csv(input_file, output_file):
    """
    Clean a CSV file by addressing malformed rows, unbalanced quotes, and JSON issues.
    """
    try:
        with open(input_file, 'r', encoding='utf-8') as infile, open(output_file, 'w', newline='',
                                                                     encoding='utf-8') as outfile:
            reader = csv.reader(infile)
            writer = csv.writer(outfile)

            for row in reader:
                cleaned_row = []
                for field in row:
                    try:
                        cleaned_field = clean_field(field)
                        cleaned_row.append(cleaned_field)
                    except Exception as e:
                        print(f"Error processing field: {field}, Error: {e}")
                        cleaned_row.append(field)  # Add raw field if cleaning fails
                writer.writerow(cleaned_row)

        print(f"Cleaned CSV saved to {output_file}")
    except Exception as e:
        print(f"An error occurred during processing: {e}")


# Usage
input_csv = "D:\\Portfolio\\product-recommendation\\data\\flipkart_com-ecommerce_sample.csv"  # Replace with your input CSV file path
output_csv = "cleaned.csv"  # Replace with your output file path
clean_csv(input_csv, output_csv)


def load_and_save_csv(input_file, output_file):
    try:
        df = pd.read_csv(input_file, engine="python")
        df.to_csv(output_file, index=False)
        print(f"Cleaned data loaded and saved to {output_file}")
    except Exception as e:
        print(f"An error occurred while loading or saving the file: {e}")

load_and_save_csv("cleaned.csv", "cleaned-final.csv")