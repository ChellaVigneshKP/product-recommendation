from py_eureka_client.eureka_client import EurekaClient


class EurekaService:
    def __init__(self, eureka_server, app_name, instance_host, instance_port):
        self.client = EurekaClient(
            eureka_server=eureka_server,
            app_name=app_name,
            instance_host=instance_host,
            instance_port=instance_port
        )

    async def start(self):
        """Start the Eureka client to register and maintain heartbeats."""
        await self.client.start()  # Ensure this is awaited as it is an async method

    async def stop(self):
        """Stop the Eureka client and deregister the service."""
        await self.client.stop()  # Ensure this is awaited as well