from dotenv import load_dotenv
import os

# Load the .env file
load_dotenv()

# Read values from the environment
EUREKA_SERVER_URL = os.getenv("EUREKA_SERVER_URL")  # Eureka server URL
APP_NAME = os.getenv("APP_NAME")  # Application name
INSTANCE_HOST = os.getenv("INSTANCE_HOST")  # Hostname or IP address
INSTANCE_PORT = int(os.getenv("INSTANCE_PORT"))  # Application port (convert to int)

KAFKA_BROKER = os.getenv("KAFKA_BROKER")  # Kafka broker address
KAFKA_GROUP_ID = os.getenv("KAFKA_GROUP_ID")  # Kafka consumer group ID
KAFKA_TOPICS = os.getenv("KAFKA_TOPICS").split(",")  # Kafka topics (split into list)

REDIS_HOST = os.getenv("REDIS_HOST")
REDIS_PORT = int(os.getenv("REDIS_PORT"))  # Redis port (convert to int)
REDIS_DB = os.getenv("REDIS_DB")
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD")
REDIS_USER = os.getenv("REDIS_USER")