const request = require('request')
const util = require('util')

class DryApi {
    constructor(baseAddress) {
        this.baseAddress = baseAddress
    }

    async execute(method, input) {
        const r = {
            "requests" : [
                { 
                    "input" : input, 
                    "inputMappings" : null, 
                    "qualifiedName" : method, 
                    "requestType" : "EXECUTE", 
                    "requestUuid" : "{C19B0899-D54C-498F-81E3-7BCBD3BD20F3}" 
                }
            ] 
        }

        const promise = new Promise((resolve, reject)=>{
            request({
                method: "POST",
                uri: this.baseAddress,
                headers: {
                    'content-type': 'application/json;charset=utf-8'
                },
                json: r
            }, (err, res, body) => {
                resolve(body.responses[0].output)

                // console.log(JSON.stringify(body, null, 4))
            })
        })

        return promise
    }
}

module.exports.DryApi = DryApi