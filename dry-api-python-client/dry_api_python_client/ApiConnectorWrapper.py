from ApiConnector import ApiConnector
from Validator import Validator
from ValidationErrorHandler import ValidationErrorHandler
from ApiBatch import ApiBatch
from typing import Dict, List, Callable
import uuid
import json


class ApiConnectorWrapper:
    __current_user: ApiConnector = None
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
        connector = ApiConnector(f"https://{self.__instance}.navigo3.com/API", True)
        connector.login(login, password)

        self.__users[login] = connector
        self.set_current_user(login)

    def set_current_user(self, login: str) -> None:
        Validator.check_contained(login, self.__users.keys())

        self.__current_user = self.__users[login]
        print(f"\nCurrent user is {login}\n")

    def execute_batch(self, batch: ApiBatch):
        Validator.check_set(self.__current_user)
        responses = self.__current_user.call(batch.get_requests())
        self.handle_validation(responses)
        data = {}
        for resp in responses:
            data[resp["requestUuid"]] = resp["output"]
        return data

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
