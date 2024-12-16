import asyncio
from contextlib import asynccontextmanager
from fastapi import FastAPI
from app.config import EUREKA_SERVER_URL, APP_NAME, INSTANCE_HOST, INSTANCE_PORT
from app.config import KAFKA_BROKER, KAFKA_GROUP_ID, KAFKA_TOPICS
from app.controllers import root, hello  # Import controllers
from app.eureka import EurekaService
from app.kafka_consumer import KafkaConsumer

# Initialize Eureka service
eureka_service = EurekaService(
    eureka_server=EUREKA_SERVER_URL,
    app_name=APP_NAME,
    instance_host=INSTANCE_HOST,
    instance_port=INSTANCE_PORT
)

# Initialize Kafka consumer
kafka_consumer = KafkaConsumer(
    kafka_broker=KAFKA_BROKER,
    group_id=KAFKA_GROUP_ID,
    topics=KAFKA_TOPICS
)

# Define FastAPI lifespan to manage Eureka and Kafka startup/stop
@asynccontextmanager
async def lifespan(app: FastAPI):
    # Start Eureka client during startup
    await eureka_service.start()
    print(f"{APP_NAME} registered with Eureka.")

    # Start Kafka consumer in the background
    consumer_task = asyncio.create_task(kafka_consumer.start())
    print("Kafka consumer started...")

    yield

    # Stop Kafka consumer during shutdown
    print("Stopping Kafka consumer...")
    consumer_task.cancel()
    await kafka_consumer.stop()
    print(f"{APP_NAME} deregistered from Eureka.")

# Initialize FastAPI app with lifespan for Eureka and Kafka management
app = FastAPI(lifespan=lifespan)

# Include the route controllers
app.include_router(root.router)  # Root controller
app.include_router(hello.router)  # Hello controller

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=INSTANCE_PORT, reload=True)
