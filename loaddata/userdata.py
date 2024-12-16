import bcrypt
import csv

# Function to encrypt a password
def encrypt_password(password):
    return bcrypt.hashpw(password.encode('utf-8'), bcrypt.gensalt()).decode('utf-8')

# File paths
input_csv = "Generated_Passwords.csv"  # Replace with your input file path
output_csv = "encrypted_passwords.csv"  # Replace with your output file path

# Read the CSV, encrypt passwords, and write to a new CSV
def process_passwords(input_file, output_file):
    try:
        with open(input_file, mode='r') as infile, open(output_file, mode='w', newline='') as outfile:
            reader = csv.reader(infile)
            writer = csv.writer(outfile)

            # Write header if applicable
            header = next(reader, None)
            if header:
                writer.writerow(header + ["Encrypted Password"])

            # Process each row
            for row in reader:
                if row:  # Ensure the row is not empty
                    plain_password = row[0]  # Assuming password is in the first column
                    encrypted_password = encrypt_password(plain_password)
                    writer.writerow(row + [encrypted_password])

        print(f"Encrypted passwords have been saved to '{output_file}'")
    except Exception as e:
        print(f"Error: {e}")

process_passwords(input_csv,output_csv)