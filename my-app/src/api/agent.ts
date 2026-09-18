import request from './request'

const AGENT_BASE_URL = import.meta.env.VITE_AGENT_URL || 'http://localhost:5000'

export async function agentChat(
  conversationId: string,
  content: string,
  carId?: number,
  carName?: string,
  token?: string
): Promise<string> {
  const res = await fetch(`${AGENT_BASE_URL}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      conversation_id: conversationId,
      content,
      car_id: carId,
      car_name: carName,
      token,
    }),
  })
  const data = await res.json()
  return data.data?.content || ''
}

export async function agentChatStream(
  conversationId: string,
  content: string,
  carId?: number,
  carName?: string,
  token?: string,
  onDelta?: (chunk: string) => void,
  onDone?: () => void,
  onError?: (err: string) => void
): Promise<AbortController> {
  const controller = new AbortController()

  try {
    const res = await fetch(`${AGENT_BASE_URL}/chat/stream`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        conversation_id: conversationId,
        content,
        car_id: carId,
        car_name: carName,
        token,
      }),
      signal: controller.signal,
    })

    const reader = res.body?.getReader()
    if (!reader) {
      onError?.('无法读取流')
      return controller
    }

    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let eventType = ''
      for (const line of lines) {
        if (line.startsWith('event:')) {
          eventType = line.slice(6).trim()
        } else if (line.startsWith('data:')) {
          const raw = line.slice(5).trim()
          try {
            const data = JSON.parse(raw)
            if (eventType === 'delta') {
              onDelta?.(data.content)
            } else if (eventType === 'done') {
              onDone?.()
            } else if (eventType === 'error') {
              onError?.(data.message || 'Agent 错误')
            }
          } catch {}
        }
      }
    }
    onDone?.()
  } catch (err: any) {
    if (err.name !== 'AbortError') {
      onError?.('Agent 连接失败: ' + err.message)
    }
  }

  return controller
}
