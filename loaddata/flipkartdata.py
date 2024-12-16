import csv

# Input and output file paths
input_file = 'clean-data.csv'
output_file = 'cleaned_fixed_data.csv'

# Open the input file and process it
with open(input_file, 'r', encoding='utf-8', errors='replace') as infile, open(output_file, 'w', newline='',
                                                                               encoding='utf-8') as outfile:
    reader = csv.reader(infile)
    writer = csv.writer(outfile)

    for row in reader:
        try:
            # Fix rows with missing or mismatched quotes
            fixed_row = [cell.replace('"', '').replace('\n', ' ').strip() for cell in row]
            writer.writerow(fixed_row)
        except Exception as e:
            print(f"Error processing row: {row} - Error: {e}")

print(f"File has been cleaned and saved as {output_file}.")