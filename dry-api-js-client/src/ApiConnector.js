import generateUuid from "uuid"

export class ApiConnector {
  constructor(baseAddress, extraHeaders = {}, printCalls = false, criticalErrorHandler = () => {}) {
    this.baseAddress = baseAddress
    this.extraHeaders = extraHeaders
    this.printCalls = printCalls
    this._criticalErrorHandler = criticalErrorHandler
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
      fetch(`${this.baseAddress}?e=${encodeURIComponent(method)}${extraUrl}`, {
        method: "POST",
        headers: {
          "content-type": "application/json;charset=utf-8",
          ...this.extraHeaders
        },
        body: JSON.stringify(r)
      })
        .catch(err => this._criticalErrorHandler("fetch", err))
        .then(async response => {
          if (response?.status != 200 && !(response?.status == 400 && onValidationError)) {
            this._criticalErrorHandler("status", response)
          } else {
            const json = await response.json()

            const resp = json.responses[0]

            if (json.overallSuccess) {
              resolve(resp.output)
            } else if (onValidationError && resp && resp.status === "INVALID_INPUT") {
              onValidationError(resp)
            } else {
              reject(json)
            }
          }
        })
        .catch(err => this._criticalErrorHandler("handling", err))
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
      fetch(`${this.baseAddress}?v=${encodeURIComponent(method)}${extraUrl}`, {
        method: "POST",
        headers: {
          "content-type": "application/json;charset=utf-8",
          ...this.extraHeaders
        },
        body: JSON.stringify(r)
      })
        .catch(err => this._criticalErrorHandler("fetch", err))
        .then(async response => {
          if (response?.status != 200 && response?.status != 400) {
            this._criticalErrorHandler("status", response)
          } else {
            const json = await response.json()

            const resp = json.responses[0]

            if (json.overallSuccess) {
              resolve([resp.validation, resp])
            } else {
              reject(json)
            }
          }
        })
        .catch(err => this._criticalErrorHandler("handling", err))
    })

    return promise
  }

  async callAsync(req) {
    if (this.printCalls) {
      console.log(`[CALL] ${req.map(r => r.qualifiedName).join(", ")}`)
    }
    const promise = new Promise((resolve, reject) => {
      fetch(`${this.baseAddress}?e=${encodeURIComponent(req.map(r => r.qualifiedName).join(", "))}`, {
        method: "POST",
        headers: {
          "content-type": "application/json;charset=utf-8",
          ...this.extraHeaders
        },
        body: JSON.stringify({ requests: req })
      })
        .catch(err => this._criticalErrorHandler("fetch", err))
        .then(async response => {
          const json = await response.json()

          if (response?.status != 200 && response?.status != 400) {
            this._criticalErrorHandler("status", response)
          } else {
            if (json.overallSuccess) {
              resolve(json)
            } else {
              reject(json.responses[0])
            }
          }
        })
        .catch(err => this._criticalErrorHandler("handling", err))
    })

    return promise
  }

  downloadLink(method, input, forceDownload) {
    return `/API/execute?qualifiedName=${encodeURIComponent(method)}&forceDownload=${
      forceDownload ? true : false
    }&input=${encodeURIComponent(JSON.stringify(input))}`
  }
}
