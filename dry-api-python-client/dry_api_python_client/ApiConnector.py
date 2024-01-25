import requests
import uuid


class ApiConnector:
    def __init__(self, baseAddress: str):
        self.baseAddress = baseAddress

    def login(self, login: str, password: str):
        res = requests.post(
            self.baseAddress + "/login",
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
            self.baseAddress + "/logout",
            headers={"content-type": "application/json;charset=utf-8"},
            json={"sessionId": self._sessionId},
        )

        if res.status_code == 200:
            self._sessionId = None
        else:
            raise Exception("Cannot log out!")

    def create_request(
        self,
        method: str,
        request_type: str,
        input_data: dict = {},
        from_uuid: str = None,
        upsert_path: list = [],
    ):
        data = {
            "input": input_data,
            "qualifiedName": method,
            "requestType": request_type,
            "requestUuid": str(uuid.uuid1()),
        }

        if from_uuid != None:
            data["inputMappings"] = [
                {
                    "fromUuid": from_uuid,
                    "fromPath": {"items": upsert_path},
                    "toPath": {"items": []},
                }
            ]

        return data

    def execute(self, method: str, input_data: dict):
        request = self.create_request(method, "EXECUTE", input_data)
        return self.call([request])[0]["output"]

    def validate(self, method: str, input_data: dict):
        request = self.create_request(method, "VALIDATE", input_data)
        return self.call([request])[0]

    def call(self, req: list):
        if not self._sessionId:
            raise Exception("Not logged in!")
        res = requests.post(
            self.baseAddress + "/execute",
            headers={
                "content-type": "application/json;charset=utf-8",
                "X-API-Session": self._sessionId,
            },
            json={"requests": req},
        )

        return res.json()["responses"]
