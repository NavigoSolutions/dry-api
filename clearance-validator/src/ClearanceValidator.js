export class ClearanceValidator {
  static isSet(object, message = undefined) {
    if (object === null || object === undefined) {
      throw new Error(message || "Object is null/undefined")
    }
  }

  static isNotSet(object, message = undefined) {
    if (object !== null && object !== undefined) {
      throw new Error(message || "Object should be null/undefined but it is set to value")
    }
  }

  static isTrue(bool, message = undefined) {
    if (bool !== true) {
      throw new Error(message || `True expected, got ${bool}`)
    }
  }

  static notBlank(object, message = undefined) {
    this.isA(object, String)

    if (object === null || object === undefined || object.length < 1) {
      throw new Error(message || "String should not be blank")
    }
  }

  static isA(object, klass, message = undefined) {
    this.isSet(object)
    this.isSet(klass)

    if (object.constructor !== klass) {
      throw new Error(message || `Expected that object is of class ${klass.name} but got ${object.constructor.name}`)
    }
  }

  static isFunctionWithArity(func, arity, message = undefined) {
    if (typeof func !== "function") {
      throw new Error(`Expected function but got ${typeof func}`)
    }

    if (func.length !== arity) {
      throw new Error(message || `Expected function with arity ${arity} but got ${func.length}`)
    }
  }

  static isFunctionWithArityBetween(func, arityMin, arityMax, message = undefined) {
    if (typeof func !== "function") {
      throw new Error(`Expected function but got ${typeof func}`)
    }

    if (func.length < arityMin || func.length > arityMax) {
      throw new Error(
        message || `Expected function with arity between ${arityMin} and ${arityMax} but got ${func.length}`
      )
    }
  }

  static arrayOfSize(array, length, message = undefined) {
    this.isA(array, Array)

    if (array.length !== length) {
      throw new Error(message || `Expected array with length ${length} but got ${array.length}`)
    }
  }

  static stringOfSize(str, length, message = undefined) {
    this.isA(str, String)

    if (str.length !== length) {
      throw new Error(message || `Expected string with length ${length} but got ${str.length}`)
    }
  }

  static greaterThan(number, ref, message = undefined) {
    this.isA(number, Number)
    this.isA(ref, Number)

    if (number <= ref) {
      throw new Error(message || `Expected positive number greather than ${ref} but got ${number}`)
    }
  }

  static lessThan(number, ref, message = undefined) {
    this.isA(number, Number)
    this.isA(ref, Number)

    if (number >= ref) {
      throw new Error(message || `Expected positive number lesser than ${ref} but got ${number}`)
    }
  }

  static positiveNumber(number, message = undefined) {
    this.isA(number, Number)

    if (number < 0) {
      throw new Error(message || `Expected positive number but got ${number}`)
    }
  }

  static equals(value, ref, message = undefined) {
    if (value !== ref) {
      throw new Error(message || `Expected same values but got '${value}' and '${ref}'`)
    }
  }

  static nonEmptyArray(array, message = undefined) {
    this.isA(array, Array)

    if (array.length <= 0) {
      throw new Error(message || `Expected non empty array but got empty`)
    }
  }

  static arrayOfStrings(array, message = undefined) {
    this.isA(array, Array)

    array.forEach((item, index) => {
      if (item === null || item === undefined) {
        throw new Error(message || `Found null/undefined item on index ${index} in array [${array.join(", ")}]`)
      }

      if (item.constructor !== String) {
        throw new Error(
          message ||
            `Expected string but got ${object.constructor.name} on index ${index} in array [${array.join(", ")}]`
        )
      }
    })
  }

  static oneOf(value, options, message = undefined) {
    this.isA(options, Array)

    if (options.indexOf(value) == -1) {
      throw new Error(message || `Value '${value}' is not among allowed values ${options.join(", ")}`)
    }
  }

  static contained(map, object, message = undefined) {
    if (!(object in map)) {
      throw new Error(message || `Object ${object} not found in map`)
    }
  }

  static notContained(map, object, message = undefined) {
    if (object in map) {
      throw new Error(message || `Object ${object} is already in map`)
    }
  }

  static included(array, object, message = undefined) {
    this.isA(array, Array)

    if (!array.includes(object)) {
      throw new Error(message || `Object ${object} not found in array`)
    }
  }

  static notIncluded(array, object, message = undefined) {
    this.isA(array, Array)

    if (array.includes(object)) {
      throw new Error(message || `Object ${object} is already in array`)
    }
  }

  static regexp(str, regexp, message = undefined) {
    this.isA(str, String)
    this.isA(regexp, RegExp)

    if (!regexp.test(str)) {
      throw new Error(message || `String "${str}" does not match pattern "${regexp}"`)
    }
  }
}
