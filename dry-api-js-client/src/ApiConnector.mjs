import request from "request"
import syncRequest from "sync-request"
import generateUuid from "uuid/v1"

export class ApiConnector {
  constructor(baseAddress, extraHeaders = {}, printCalls = false) {
    this.baseAddress = baseAddress
    this.extraHeaders = extraHeaders
    this.printCalls = printCalls
  }

  async executeAsync(method, input, onValidationError, options = {}) {
    if (this.printCalls) {
      console.log(`[CALL] ${method}`)
    }

    const r = {
      requests: [
        {
          input: input,
          inputMappings: null,
          qualifiedName: method,
          requestType: "EXECUTE",
          requestUuid: generateUuid()
        }
      ]
    }

    const extraUrl =
      Object.keys(options.urlParams || {}).length > 0
        ? `&${Object.keys(options.urlParams)
            .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(options.urlParams[k])}`)
            .join("&")}`
        : ""

    const promise = new Promise((resolve, reject) => {
      request(
        {
          method: "POST",
          uri: `${this.baseAddress}?e=${encodeURIComponent(method)}${extraUrl}`,
          headers: {
            "content-type": "application/json;charset=utf-8",
            ...this.extraHeaders
          },
          body: JSON.stringify(r)
        },
        (err, res, body) => {
          if (err || (res.statusCode != 200 && res.statusCode != 400)) {
            console.log((body || "").replace(/\\n/g, "\n"))
            reject(err || `Status code was unexpected: ${res.statusCode}`)
          } else {
            const json = JSON.parse(body)
            const resp = json.responses[0]

            if (json.overallSuccess) {
              resolve(resp.output)
            } else if (onValidationError && resp && resp.status === "INVALID_INPUT") {
              onValidationError(resp)
            } else {
              reject(resp)
            }
          }
        }
      )
    })

    return promise
  }

  async validateAsync(method, input, options = {}) {
    if (this.printCalls) {
      console.log(`[CALL] ${method}`)
    }

    const r = {
      requests: [
        {
          input: input,
          inputMappings: null,
          qualifiedName: method,
          requestType: "VALIDATE",
          requestUuid: generateUuid()
        }
      ]
    }

    const extraUrl =
      Object.keys(options.urlParams || {}).length > 0
        ? `&${Object.keys(options.urlParams)
            .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(options.urlParams[k])}`)
            .join("&")}`
        : ""

    const promise = new Promise((resolve, reject) => {
      request(
        {
          method: "POST",
          uri: `${this.baseAddress}?v=${encodeURIComponent(method)}${extraUrl}`,
          headers: {
            "content-type": "application/json;charset=utf-8",
            ...this.extraHeaders
          },
          body: JSON.stringify(r)
        },
        (err, res, body) => {
          if (err || (res.statusCode != 200 && res.statusCode != 400)) {
            console.log((body || "").replace(/\\n/g, "\n"))
            reject(err || `Status code was unexpected: ${res.statusCode}`)
          } else {
            const json = JSON.parse(body)
            const resp = json.responses[0]

            if (json.overallSuccess) {
              resolve([resp.validation, resp])
            } else {
              reject(resp)
            }
          }
        }
      )
    })

    return promise
  }

  async callAsync(req) {
    if (this.printCalls) {
      console.log(`[CALL] ${req.map(r => r.qualifiedName).join(", ")}`)
    }
    const promise = new Promise((resolve, reject) => {
      request(
        {
          method: "POST",
          uri: `${this.baseAddress}?e=${encodeURIComponent(req.map(r => r.qualifiedName).join(", "))}`,
          headers: {
            "content-type": "application/json;charset=utf-8",
            ...this.extraHeaders
          },
          body: JSON.stringify({ requests: req })
        },
        (err, res, body) => {
          const json = JSON.parse(body)

          if (err || (res.statusCode != 200 && res.statusCode != 400)) {
            console.log((body || "").replace(/\\n/g, "\n"))
            reject(err || `Status code was unexpected: ${res.statusCode}`)
          } else {
            if (json.overallSuccess) {
              resolve(json)
            } else {
              reject(json.responses[0])
            }
          }
        }
      )
    })

    return promise
  }

  executeSync(method, input) {
    const res = this.executeSyncRaw([
      {
        input: input,
        inputMappings: null,
        qualifiedName: method,
        requestType: "EXECUTE",
        requestUuid: generateUuid()
      }
    ])

    return res[0].output
  }

  executeSyncRaw(requests) {
    if (this.printCalls) {
      console.log(`[CALL] ${requests.map(r => r.qualifiedName).join(", ")}`)
    }

    const r = {
      requests
    }

    const res = syncRequest("POST", this.baseAddress, {
      headers: {
        "content-type": "application/json;charset=utf-8",
        ...this.extraHeaders
      },
      body: JSON.stringify(r)
    })

    if (res.statusCode != 200 && res.statusCode != 400) {
      console.log(res.getBody("utf8"))
      throw `Status code was unexpected: ${res.statusCode}`
    } else {
      const rawBody = res.body.toString("utf8")

      const body = JSON.parse(rawBody)

      if (body.overallSuccess) {
        return body.responses
      } else {
        body.responses.forEach(resp => {
          if (resp.validation && resp.validation.items && resp.validation.items.length > 0) {
            console.log(`################ERROR - ${resp.requestUuid}################`)
            console.log("Found issues:")
            resp.validation.items.forEach(i => {
              console.log(`\t${i.message} (${i.path.items.map(p => (p.key ? p.key : `[${p.index}]`)).join(".")})`)
            })
            throw `Request validation failed`
          } else {
            console.error(resp)
            throw `Request partially failed:\n` + (resp.errorMessage || "").replace(/\\n/g, "\n")
          }
        })
      }
    }
  }

  downloadLink(method, input, forceDownload) {
    return `/API/execute?qualifiedName=${encodeURIComponent(method)}&forceDownload=${
      forceDownload ? true : false
    }&input=${encodeURIComponent(JSON.stringify(input))}`
  }
}
