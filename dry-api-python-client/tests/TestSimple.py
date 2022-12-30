
from dry_api_python_client.ApiConnector import ApiConnector
import json
import unittest


class TestSimple(unittest.TestCase):

    def test_simple(self):
        apiConnector = ApiConnector("https://testapi.navigo3.com/API")

        apiConnector.login("api-test", "TODO")

        print(json.dumps(apiConnector.validate(
            "employee/get", {"id": 1}), indent=4))

        print(json.dumps(apiConnector.execute(
            "employee/get", {"id": 1}), indent=4))

        apiConnector.logout()
