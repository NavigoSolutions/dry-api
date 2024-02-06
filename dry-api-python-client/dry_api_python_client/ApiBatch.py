from Validator import Validator
from Utils import Utils
from typing import Dict, List, Union


class ApiBatch:
    __requests: List[Dict[str, dict]] = []

    def get_requests(self) -> Dict[str, dict]:
        return self.__requests

    def add_request(
        self,
        method: str,
        input_data: dict = {},
        from_uuid: str = None,
        upsert_path: List[Union[int, str]] = [],
    ) -> str:
        Validator.check_only_one_present([from_uuid, input_data])

        path = Utils.create_api_path(upsert_path)
        request = Utils.create_request(method, "EXECUTE", input_data, from_uuid, path)

        self.__requests.append(request)
        return request["requestUuid"]
