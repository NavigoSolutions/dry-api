import request from 'request'
import syncRequest from 'sync-request'
import generateUuid from 'uuid/v1'

export class ApiConnector {
    constructor(baseAddress, extraHeaders={}, printCalls=false) {
        this.baseAddress = baseAddress
        this.extraHeaders = extraHeaders
        this.printCalls = printCalls
    }

    async executeAsync(method, input) {
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
                    "requestUuid" : generateUuid()
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
                    console.log(body.replace(/\\n/g, '\n'))
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

    executeSync(method, input) {
        const res = this.executeSyncRaw([{ 
            "input" : input, 
            "inputMappings" : null, 
            "qualifiedName" : method, 
            "requestType" : "EXECUTE", 
            "requestUuid" : generateUuid()
        }])

        return res[0].output
    }

    executeSyncRaw(requests) {
        if (this.printCalls) {
            console.log(`[CALL] ${requests.map(r=>r.qualifiedName).join(', ')}`)            
        }

        const r = {
            requests
        }

        const res = syncRequest("POST", this.baseAddress, {
            headers: {
                'content-type': 'application/json;charset=utf-8',
                ...this.extraHeaders
            },
            json: r
        })

        if (res.statusCode!=200 && res.statusCode!=400) {
            console.log(res.getBody('utf8'))
            throw `Status code was unexpected: ${res.statusCode}`
        } else {
            const rawBody = res.body.toString('utf8')

            const body = JSON.parse(rawBody)

            if (body.overallSuccess) {
                return body.responses
            } else {
                body.responses.forEach(resp=>{
                    if (resp.validation && resp.validation.items && resp.validation.items.length>0) {
                        console.log(`################ERROR - ${resp.requestUuid}################`)
                        console.log("Found issues:")
                        resp.validation.items.forEach(i=>{
                            console.log(`\t${i.message} (${i.path.items.map(p=>p.key?p.key:`[${p.index}]`).join('.')})`)
                        })
                        throw `Request validation failed`
                    } else {
                        console.error(resp)
                        throw `Request partially failed:\n`+(resp.errorMessage||'').replace(/\\n/g, '\n')
                    }
                })                
            }
        }
    }
}