import { v4 as generateUuid } from "uuid"

export interface ApiConnectorOptions {
  urlParams?: Record<string, string>
}

export interface ApiRequest {
  input: unknown
  inputMappings: null
  qualifiedName: string
  requestType: "EXECUTE" | "VALIDATE"
  requestUuid: string
}

export interface ApiResponse {
  output?: unknown
  validation?: unknown
  status?: string
}

export interface ApiResult {
  overallSuccess: boolean
  responses: ApiResponse[]
}

export type CriticalErrorHandler = (phase: "fetch" | "status" | "handling", error: unknown) => void

export class ApiConnector {
  private baseAddress: string
  private extraHeaders: Record<string, string>
  private printCalls: boolean
  private _criticalErrorHandler: CriticalErrorHandler

  constructor(
    baseAddress: string,
    extraHeaders: Record<string, string> = {},
    printCalls: boolean = false,
    criticalErrorHandler: CriticalErrorHandler = () => {}
  ) {
    this.baseAddress = baseAddress
    this.extraHeaders = extraHeaders
    this.printCalls = printCalls
    this._criticalErrorHandler = criticalErrorHandler
  }

  async executeAsync(
    method: string,
    input: unknown,
    onValidationError?: (response: ApiResponse) => void,
    options: ApiConnectorOptions = {}
  ): Promise<unknown> {
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
        ? `&${Object.keys(options.urlParams!)
            .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(options.urlParams![k])}`)
            .join("&")}`
        : ""

    const promise = new Promise<unknown>((resolve, reject) => {
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
          if (!response) {
            reject(new Error("No response received"))
            return
          }
          
          if (response.status != 200 && !(response.status == 400 && onValidationError)) {
            this._criticalErrorHandler("status", response)
            reject(response)
          } else {
            const json: ApiResult = await response.json()

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

  async validateAsync(
    method: string,
    input: unknown,
    options: ApiConnectorOptions = {}
  ): Promise<[unknown, ApiResponse]> {
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
        ? `&${Object.keys(options.urlParams!)
            .map(k => `${encodeURIComponent(k)}=${encodeURIComponent(options.urlParams![k])}`)
            .join("&")}`
        : ""

    const promise = new Promise<[unknown, ApiResponse]>((resolve, reject) => {
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
          if (!response) {
            reject(new Error("No response received"))
            return
          }

          if (response.status != 200 && response.status != 400) {
            this._criticalErrorHandler("status", response)
            reject(response)
          } else {
            const json: ApiResult = await response.json()

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

  async callAsync(req: Array<{ qualifiedName: string; [key: string]: unknown }>): Promise<ApiResult> {
    if (this.printCalls) {
      console.log(`[CALL] ${req.map(r => r.qualifiedName).join(", ")}`)
    }
    
    const promise = new Promise<ApiResult>((resolve, reject) => {
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
          if (!response) {
            reject(new Error("No response received"))
            return
          }

          const json: ApiResult = await response.json()

          if (response.status != 200 && response.status != 400) {
            this._criticalErrorHandler("status", response)
            reject(response)
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

  downloadLink(method: string, input: unknown, forceDownload: boolean): string {
    return `/API/execute?qualifiedName=${encodeURIComponent(method)}&forceDownload=${
      forceDownload ? true : false
    }&input=${encodeURIComponent(JSON.stringify(input))}`
  }
}
