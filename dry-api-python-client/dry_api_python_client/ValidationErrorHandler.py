class ValidationErrorHandler:
    @staticmethod
    def console(responses):
        errors, warns = ValidationErrorHandler.get_console_validation_messages(
            responses
        )
        if errors != "":
            raise Exception(errors)
        if warns != "":
            print(warns)

    @staticmethod
    def get_console_validation_messages(responses):
        errors = ""
        warns = ""
        prefix = (
            lambda index: f"API {len(responses)} Batch[{index}] "
            if len(responses) > 1
            else ""
        )
        for i in range(len(responses)):
            resp = responses[i]

            error = ValidationErrorHandler.get_console_validation_string(
                resp["qualifiedName"],
                resp["validation"]["items"] if resp["validation"] != None else {},
                "ERROR",
            )

            warn = ValidationErrorHandler.get_console_validation_string(
                resp["qualifiedName"],
                resp["validation"]["items"] if resp["validation"] != None else {},
                "WARNING",
            )

            if error != None:
                errors += f"\n{prefix(i)}{error}\n"

            if warn != None:
                warns += f"\n{prefix(i)}{warn}\n"

        return [errors, warns]

    @staticmethod
    def get_console_validation_string(
        method: str, validation_items: list, severity
    ) -> str:
        errors = list(
            filter(lambda item: item["severity"] == severity, validation_items)
        )
        if len(errors) > 0:
            error = f"validation {severity} on method '{method}':"
            for item in errors:
                path = []
                for key in item["path"]["items"]:
                    path.append(key["key"] if key["key"] != None else key["index"])

                error += f"\n\t{path}:    {item['message']}"

            return error
        return None
