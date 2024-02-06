from ApiConnectorWrapper import ApiConnectorWrapper
from ApiBatch import ApiBatch


api = ApiConnectorWrapper("env.json")

user = api.execute("user/view/get", {"id": 1})

b = ApiBatch()

list1RUuid = b.add_request("user/view/list")
list2RUuid = b.add_request("user/view/list")


data = api.execute_batch(b)

print(user)
