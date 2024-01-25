from dry_api_python_client.ApiConnector import ApiConnector
from dry_api_python_client.Validator import Validator
from dry_api_python_client.ValidationErrorHandler import ValidationErrorHandler
from typing import Dict, List, Callable, Union
import uuid
import json


class ApiConnectorWrapper:
    __current_user: ApiConnector = None
    __requests: List[List[Dict[str, dict]]] = []

    __users: Dict[str, ApiConnector] = {}
    __validation_error_handler = None

    __instance = None

    def __init__(
        self,
        config_path,
        validation_error_handler: Callable[
            [List[Dict[str, dict]]], None
        ] = ValidationErrorHandler.console,
    ):
        self.__validation_error_handler = validation_error_handler
        with open(config_path) as cfg:
            config = json.load(cfg)
            self.__instance = config["instance"]
            Validator.check_set(self.__instance)
            self.log_in(config["rootLogin"], config["rootPassword"])

    def log_in(self, login: str, password: str) -> None:
        Validator.check_not_contained(login, self.__users.keys())
        print(f"xxxx https://{self.__instance}.navigo3.com/API")
        connector = ApiConnector(f"https://{self.__instance}.navigo3.com/API")
        connector.login(login, password)

        self.__users[login] = connector
        self.set_current_user(login)

    def set_current_user(self, login: str) -> None:
        Validator.check_array_of_size(self.__requests, 0)
        Validator.check_contained(login, self.__users.keys())

        self.__current_user = self.__users[login]
        print(f"\nCurrent user is {login}\n")

    def create_batch(self) -> None:
        self.__requests.append([])

    def execute_batch(self) -> Dict[str, dict]:
        Validator.check_set(self.__current_user)

        responses = self.__current_user.call(self.__requests.pop())
        self.handle_validation(responses)

        data = {}
        for resp in responses:
            data[resp["requestUuid"]] = resp["output"]
        return data

    def __create_api_path(self, list_path: List[Union[int, str]]):
        path = []
        for item in list_path:
            if isinstance(item, str):
                path.append({"index": None, "key": item, "type": "KEY"})
            elif isinstance(item, int):
                path.append({"index": item, "key": None, "type": "INDEX"})
            else:
                raise ValueError(f"Unexpected item {item} of type {type(item)}")
        return path

    def add_batch_request(
        self,
        method: str,
        input_data: dict = {},
        from_uuid: str = None,
        upsert_path: List[Union[int, str]] = [],
    ) -> str:
        Validator.check_only_one_present([from_uuid, input_data])

        path = self.__create_api_path(upsert_path)
        request = self.__current_user.create_request(
            method, "EXECUTE", input_data, from_uuid, path
        )

        self.__current_batch().append(request)
        return request["requestUuid"]

    def __current_batch(self):
        Validator.check_non_empty_array(self.__requests)
        return self.__requests[-1]

    def execute(self, method: str, input_data: dict = {}) -> dict:
        Validator.check_set(self.__current_user)
        resp = self.__current_user.validate(method, input_data)
        if resp["validation"]:
            if resp["validation"]["overallSuccess"]:
                return self.__current_user.execute(method, input_data)
            else:
                self.handle_validation([resp])
        else:
            raise Exception(resp)

    def handle_validation(self, responses):
        self.__validation_error_handler(responses)

    def dispose(self):
        for conn in self.__users.values():
            conn.logout()

        self.__current_user = None
        self.__users.clear()
