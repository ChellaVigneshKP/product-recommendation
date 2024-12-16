from typing import List


class Product:
    def __init__(self, productId: str, productName: str, category: List[str], brandName: str, price: float, discountedPrice: float, imageUrl: List[str]):
        self.productId = productId
        self.productName = productName
        self.category = category
        self.brandName = brandName
        self.price = price
        self.discountedPrice = discountedPrice
        self.imageUrl = imageUrl

class UserInteraction:
    def __init__(self, userId: str):
        self.userId = userId
        self.productHistory = []

    def add_product(self, productId: str):
        self.productHistory.append(productId)
