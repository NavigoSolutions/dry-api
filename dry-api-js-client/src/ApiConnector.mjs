import request from 'request'

export class ApiConnector {
    constructor(baseAddress, extraHeaders={}, printCalls=false) {
        this.baseAddress = baseAddress
        this.extraHeaders = extraHeaders
        this.printCalls = printCalls
    }

    async execute(method, input) {
        if (this.printCalls) {
            console.log(`[CALL] ${method}`)            
        }

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
                    'content-type': 'application/json;charset=utf-8',
                    ...this.extraHeaders
                },
                json: r
            }, (err, res, body) => {
                if (err || (res.statusCode!=200 && res.statusCode!=400)) {
                    console.log(body)
                    reject(err || `Status code was unexpected: ${res.statusCode}`)
                } else {
                    const resp = body.responses[0]

                    if (body.overallSuccess) {
                        resolve(resp.output)
                    } else {
                        reject(resp)
                    }
                }
            })
        })

        return promise
    }
}