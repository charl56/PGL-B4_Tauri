import type { z } from "zod"

export type ApiQueryRequest<T> = {
	route: string
	method: "GET" | "POST" | "PUT" | "PATCH" | "DELETE"
	body?: unknown
	responseSchema: z.ZodType<T>
}

export type ApiQueryResponse<T> = {
    status: "success"
    data: T
    error?: never
} | {
    status: "error"
    data?: never
    error: string
}