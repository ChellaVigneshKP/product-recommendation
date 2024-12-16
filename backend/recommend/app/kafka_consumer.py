import asyncio
import json
from aiokafka import AIOKafkaConsumer
from app.recommendation import RecommendationService  # Updated to reference the correct service


class KafkaConsumer:
    def __init__(self, kafka_broker, group_id, topics):
        self.kafka_broker = kafka_broker
        self.group_id = group_id
        self.topics = topics
        self.consumer = None

        # Initialize the recommendation service
        self.recommendation_service = RecommendationService()

    async def consume_messages(self):
        """Consume messages asynchronously."""
        self.consumer = AIOKafkaConsumer(
            *self.topics,
            bootstrap_servers=self.kafka_broker,
            group_id=self.group_id,
            auto_offset_reset="earliest",
        )

        try:
            # Start Kafka consumer
            print("Connecting to Kafka broker...")
            await self.consumer.start()
            print("Connected to Kafka broker, consuming messages...")

            async for msg in self.consumer:
                print(f"Consumed message: {msg.value}")
                event_data = json.loads(msg.value.decode("utf-8"))
                await self.process_event(event_data)
        except asyncio.CancelledError:
            print("Kafka consumer was cancelled.")
        except Exception as e:
            print(f"Error while consuming messages: {e}")
        finally:
            await self.consumer.stop()
            print("Kafka consumer stopped.")

    async def process_event(self, event_data: dict):
        """Process events from Kafka."""
        try:
            user_id = event_data["userId"]
            product_id = event_data["productId"]
            category_tree = " >> ".join(event_data["category"])  # Flatten category list into '>>' format

            print(f"Processing event for user: {user_id}, product: {product_id}, category: {category_tree}")

            # Get recommendations dynamically
            recommendations = await self.recommendation_service.get_recommendations(user_id, category_tree)

            # Log or process recommendations further
            print(f"Recommended products for user {user_id}: {recommendations}")
        except KeyError as e:
            print(f"Missing expected key in event data: {e}")
        except Exception as e:
            print(f"Error processing event data: {e}")

    async def start(self):
        """Start Kafka consumer."""
        await self.consume_messages()

    async def stop(self):
        """Stop Kafka consumer."""
        if self.consumer:
            await self.consumer.stop()
            print("Kafka consumer stopped.")
