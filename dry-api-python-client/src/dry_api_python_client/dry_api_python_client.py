import requests
import uuid
import json


class ApiConnector:

    def __init__(self, baseAddress):
        self.baseAddress = baseAddress

    def login(self, login, password):
        res = requests.post(self.baseAddress+"/login",
                            headers={
                                "content-type": "application/json;charset=utf-8"},
                            json={"login": login, "password": password})

        if res.status_code == 200:
            self._sessionId = res.json()["sessionId"]
        else:
            raise Exception("Cannot log in!")

    def logout(self):
        if not self._sessionId:
            raise Exception("Not logged in!")

        res = requests.post(self.baseAddress+"/logout",
                            headers={
                                "content-type": "application/json;charset=utf-8"},
                            json={"sessionId": self._sessionId})

        if res.status_code == 200:
            self._sessionId = None
        else:
            raise Exception("Cannot log out!")

    def execute(self, method, input):
        res = self.call({
            "requests": [
                {
                    "input": input,
                    "inputMappings": None,
                    "qualifiedName": method,
                    "requestType": "EXECUTE",
                    "requestUuid": str(uuid.uuid1())
                }
            ]
        })

        return res["responses"][0]["output"]

    def validate(self, method, input):
        res = self.call({
            "requests": [
                {
                    "input": input,
                    "inputMappings": None,
                    "qualifiedName": method,
                    "requestType": "VALIDATE",
                    "requestUuid": str(uuid.uuid1())
                }
            ]
        })

        return res["responses"][0]["validation"]

    def call(self, req):
        if not self._sessionId:
            raise Exception("Not logged in!")

        res = requests.post(self.baseAddress+"/execute",
                            headers={
                                "content-type": "application/json;charset=utf-8",
                                "X-API-Session": self._sessionId},
                            json=req)

        if res.status_code == 200:
            return res.json()
        else:
            print(res.text)
            raise Exception(
                f"Execution failed ! Status code={res.status_code}")


apiConnector = ApiConnector("https://testapi.navigo3.com/API")

apiConnector.login("api-test", "TODO")

print(json.dumps(apiConnector.validate("employee/get", {"id": 1}), indent=4))

print(json.dumps(apiConnector.execute("employee/get", {"id": 1}), indent=4))

apiConnector.logout()

print("ok")
