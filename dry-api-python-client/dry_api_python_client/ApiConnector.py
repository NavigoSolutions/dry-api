import requests
import uuid
from Utils import Utils


class ApiConnector:
    __base_address: str = None
    __fault_tolerant = False

    def __init__(self, base_address: str, fault_tolerant=False):
        self.__base_address = base_address
        self.__fault_tolerant = fault_tolerant

    def login(self, login: str, password: str):
        res = requests.post(
            self.__base_address + "/login",
            headers={"content-type": "application/json;charset=utf-8"},
            json={"login": login, "password": password},
        )

        if res.status_code == 200:
            self._sessionId = res.json()["sessionId"]
        else:
            raise Exception("Cannot log in!")

    def logout(self):
        if not self._sessionId:
            raise Exception("Not logged in!")

        res = requests.post(
            self.__base_address + "/logout",
            headers={"content-type": "application/json;charset=utf-8"},
            json={"sessionId": self._sessionId},
        )

        if res.status_code == 200:
            self._sessionId = None
        else:
            raise Exception("Cannot log out!")

    def execute(self, method: str, input_data: dict):
        request = Utils.create_request(method, "EXECUTE", input_data)
        return self.call([request])[0]["output"]

    def validate(self, method: str, input_data: dict):
        request = Utils.create_request(method, "VALIDATE", input_data)
        return self.call([request])[0]

    def call(self, req: list):
        if not self._sessionId:
            raise Exception("Not logged in!")
        res = requests.post(
            self.__base_address + "/execute",
            headers={
                "content-type": "application/json;charset=utf-8",
                "X-API-Session": self._sessionId,
            },
            json={"requests": req},
        )

        if res.status_code == 200 or self.__fault_tolerant:
            return res.json()["responses"]
        else:
            raise Exception(f"Execution failed ! Status code={res.status_code}")
