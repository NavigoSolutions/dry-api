import uuid
from typing import List, Union


class Utils:
    @staticmethod
    def create_api_path(list_path: List[Union[int, str]]):
        path = []
        for item in list_path:
            if isinstance(item, str):
                path.append({"index": None, "key": item, "type": "KEY"})
            elif isinstance(item, int):
                path.append({"index": item, "key": None, "type": "INDEX"})
            else:
                raise ValueError(f"Unexpected item {item} of type {type(item)}")
        return path

    @staticmethod
    def create_request(
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

        if from_uuid is not None:
            data["inputMappings"] = [
                {
                    "fromUuid": from_uuid,
                    "fromPath": {"items": upsert_path},
                    "toPath": {"items": []},
                }
            ]

        return data
