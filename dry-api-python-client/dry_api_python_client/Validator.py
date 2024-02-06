class Validator:
    @staticmethod
    def check_only_one_present(values: list, message=None):
        present = False
        for value in values:
            if present and value is not None:
                raise ValueError(message or "Expected only one value to be present")
            present = value != None

    @staticmethod
    def check_set(obj, message=None):
        if obj is None:
            raise ValueError(message or "Object is None")

    @staticmethod
    def check_not_set(obj, message=None):
        if obj is not None:
            raise ValueError(message or "Object should be None but it is set to value")

    @staticmethod
    def check_false(boolean, message=None):
        if boolean is not False:
            raise ValueError(message or f"False expected, got {boolean}")

    @staticmethod
    def check_true(boolean, message=None):
        if boolean is not True:
            raise ValueError(message or f"True expected, got {boolean}")

    @staticmethod
    def check_not_blank(obj, message=None):
        Validator.check_type(obj, str)

        if obj is None or len(obj) < 1:
            raise ValueError(message or "String should not be blank")

    @staticmethod
    def check_type(obj, klass, message=None):
        Validator.check_set(obj)
        Validator.check_set(klass)

        if obj.__class__ != klass:
            raise ValueError(
                message
                or f"Expected that object is of class {klass.__name__} but got {obj.__class__.__name__}"
            )

    @staticmethod
    def check_function_with_arity(func, arity, message=None):
        if not callable(func):
            raise ValueError(f"Expected function but got {type(func)}")

        if len(func.__code__.co_varnames) != arity:
            raise ValueError(
                message
                or f"Expected function with arity {arity} but got {len(func.__code__.co_varnames)}"
            )

    @staticmethod
    def check_function_with_arity_between(func, arity_min, arity_max, message=None):
        if not callable(func):
            raise ValueError(f"Expected function but got {type(func)}")

        if (
            len(func.__code__.co_varnames) < arity_min
            or len(func.__code__.co_varnames) > arity_max
        ):
            raise ValueError(
                message
                or f"Expected function with arity between {arity_min} and {arity_max} but got {len(func.__code__.co_varnames)}"
            )

    @staticmethod
    def check_array_of_size(arr, length, message=None):
        Validator.check_type(arr, list)

        if len(arr) != length:
            raise ValueError(
                message or f"Expected array with length {length} but got {len(arr)}"
            )

    @staticmethod
    def check_string_of_size(s, length, message=None):
        Validator.check_type(s, str)

        if len(s) != length:
            raise ValueError(
                message or f"Expected string with length {length} but got {len(s)}"
            )

    @staticmethod
    def check_greater_than(number, ref, message=None):
        Validator.check_type(number, (int, float))
        Validator.check_type(ref, (int, float))

        if number <= ref:
            raise ValueError(
                message
                or f"Expected positive number greater than {ref} but got {number}"
            )

    @staticmethod
    def check_less_than(number, ref, message=None):
        Validator.check_type(number, (int, float))
        Validator.check_type(ref, (int, float))

        if number >= ref:
            raise ValueError(
                message
                or f"Expected positive number lesser than {ref} but got {number}"
            )

    @staticmethod
    def check_positive_number(number, message=None):
        Validator.check_type(number, (int, float))

        if number < 0:
            raise ValueError(message or f"Expected positive number but got {number}")

    @staticmethod
    def check_equals(value, ref, message=None):
        if value != ref:
            raise ValueError(
                message or f"Expected same values but got '{value}' and '{ref}'"
            )

    @staticmethod
    def check_non_empty_array(arr, message=None):
        Validator.check_type(arr, list)

        if len(arr) <= 0:
            raise ValueError(message or "Expected non-empty array but got empty")

    @staticmethod
    def check_array_of_strings(arr, message=None):
        Validator.check_type(arr, list)

        for index, item in enumerate(arr):
            if item is None:
                raise ValueError(
                    message
                    or f"Found None item on index {index} in array [{', '.join(map(str, arr))}]"
                )

            if not isinstance(item, str):
                raise ValueError(
                    message
                    or f"Expected string but got {item.__class__.__name__} on index {index} in array [{', '.join(map(str, arr))}]"
                )

    @staticmethod
    def check_one_of(value, options, message=None):
        Validator.check_type(options, list)

        if value not in options:
            raise ValueError(
                message
                or f"Value '{value}' is not among allowed values {', '.join(map(str, options))}"
            )

    @staticmethod
    def check_contained(obj, arr, message=None):
        if obj not in arr:
            raise ValueError(message or f"Object {obj} not found in map")

    @staticmethod
    def check_not_contained(obj, arr: list, message=None):
        if obj in arr:
            raise ValueError(message or f"Object {obj} is already in map")

    @staticmethod
    def check_not_included(arr, obj, message=None):
        Validator.check_type(arr, list)

        if obj in arr:
            raise ValueError(message or f"Object {obj} is already in array")

    @staticmethod
    def check_regexp(s, regexp, message=None):
        Validator.check_type(s, str)
        Validator.check_type(regexp, type(re.compile("")))
        if not regexp.match(s):
            raise ValueError(
                message or f'String "{s}" does not match pattern "{regexp.pattern}"'
            )
