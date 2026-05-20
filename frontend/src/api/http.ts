interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export class ApiError extends Error {
  constructor(
    message: string,
    public readonly status: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

export async function request<T>(url: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(url, {
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
    ...options,
  })

  const contentType = response.headers.get('content-type') || ''
  if (!contentType.includes('application/json')) {
    throw new Error('后端服务未启动或接口不可用')
  }

  const result = (await response.json()) as ApiResponse<T>

  if (!response.ok || result.code !== 0) {
    if (response.status === 401) {
      window.dispatchEvent(new CustomEvent('finance:unauthorized'))
    }
    throw new ApiError(result.message || '请求失败，请稍后再试', response.status)
  }

  return result.data
}

export function buildQuery(params: object): string {
  const searchParams = new URLSearchParams()

  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, String(value))
    }
  })

  const query = searchParams.toString()
  return query ? `?${query}` : ''
}
