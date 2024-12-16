import datetime
import json
import hashlib
import aioredis
from google.cloud import bigquery
from app.config import REDIS_HOST, REDIS_PORT, REDIS_USER, REDIS_PASSWORD

# Initialize BigQuery client
bigquery_client = bigquery.Client(project="virtualization-and-cloud")

class RecommendationService:
    def __init__(self):
        # Initialize aioredis client asynchronously
        self.redis_client = aioredis.from_url(
            f"redis://{REDIS_USER}:{REDIS_PASSWORD}@{REDIS_HOST}:{REDIS_PORT}",
            decode_responses=True
        )

    async def get_recommendations(self, user_id: str, category: str):
        """Fetch recommendations for a user based on product category."""
        print(f"[DEBUG] Starting recommendation flow for user: {user_id}, category: {category}")

        # Hash the category to create a Redis key
        redis_key = f"user:{user_id}:category:{hashlib.md5(category.encode()).hexdigest()}"
        print(f"[DEBUG] Redis key generated: {redis_key}")

        # Check Redis cache
        try:
            cached_recommendations = await self.redis_client.get(redis_key)
            if cached_recommendations:
                print(f"[DEBUG] Cache hit for user: {user_id}, category: {category}")
                return json.loads(cached_recommendations)
        except Exception as e:
            print(f"[ERROR] Redis error: {e}")

        # Cache miss
        print(f"[DEBUG] Cache miss for user: {user_id}, querying BigQuery for category: {category}")
        recommendations = await self.query_bigquery(category)

        # Check result length and broaden the category if needed
        print(f"[DEBUG] BigQuery returned {len(recommendations)} recommendations for category: {category}")
        if len(recommendations) < 20:
            print(f"[DEBUG] Found less than 20 products. Broadening the category for user: {user_id}")
            broader_category = self.broaden_category(category)
            print(f"[DEBUG] Broader category: {broader_category}")
            recommendations += await self.query_bigquery(broader_category)

        # Remove duplicates based on product_id
        print(f"[DEBUG] Removing duplicates from {len(recommendations)} total recommendations")
        recommendations = list({r['uniq_id']: r for r in recommendations}.values())

        # Store in Redis
        try:
            print(f"[DEBUG] Storing {len(recommendations)} recommendations in Redis with key: {redis_key}")
            # Async set in Redis
            recommendations = json.dumps(recommendations, default=self.serialize_datetime)
            await self.redis_client.set(redis_key, recommendations, ex=3600)
        except Exception as e:
            print(f"[ERROR] Failed to store recommendations in Redis: {e}")

        return recommendations

    async def query_bigquery(self, category: str):
        """Query BigQuery for products under the given category."""
        print(f"[DEBUG] Querying BigQuery for category: {category}")
        query = """
        SELECT * FROM `virtualization-and-cloud.amazon_bigdata.flipkart-data`
        WHERE product_category_tree LIKE @category
        LIMIT 20
        """
        job_config = bigquery.QueryJobConfig(
            query_parameters=[bigquery.ScalarQueryParameter("category", "STRING", f"%{category}%")]
        )

        try:
            query_job = bigquery_client.query(query, job_config=job_config)
            results = query_job.result()
            print(f"[DEBUG] BigQuery query completed, fetched {results.total_rows} rows")
        except Exception as e:
            print(f"[ERROR] BigQuery query failed: {e}")
            return []

        # Process results
        return [
            {field: row.get(field, None) for field in row.keys()}
            for row in results
        ]

    def broaden_category(self, category: str):
        """Broaden the category for a more generic search."""
        print(f"[DEBUG] Broadening category: {category}")
        parts = category.split(">>")
        if len(parts) > 1:
            broader = ">>".join(parts[:-1])
            print(f"[DEBUG] Broadened category to: {broader}")
            return broader
        print("[DEBUG] Category cannot be broadened further")
        return category

    def serialize_datetime(self, obj):
        """Custom JSON serializer for datetime objects."""
        if isinstance(obj, datetime.datetime):
            return obj.isoformat()  # Converts datetime to ISO 8601 string format
        raise TypeError(f"Type {type(obj)} not serializable")