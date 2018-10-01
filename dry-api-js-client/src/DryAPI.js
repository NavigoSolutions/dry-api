class DryApi {
    constructor(baseAddress) {
        this.baseAddress = baseAddress
    }

    executeAsync(method, input) {
        console.log(method, input)

        return {ouje: "42"}
    }
}

module.exports.DryApi = DryApi