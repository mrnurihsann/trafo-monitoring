import type { Transformer, User, StatusHistory, WorkOrder } from '@/types'

// Mock API service for demo purposes
export const api = {
  async getTransformers(): Promise<Transformer[]> {
    try {
      const response = await fetch('/data/transformers.json')
      const data = await response.json()
      return data
    } catch (error) {
      console.error('Error fetching transformers:', error)
      return []
    }
  },

  async getUsers(): Promise<User[]> {
    try {
      const response = await fetch('/data/users.json')
      const data = await response.json()
      // Convert data format to match User interface
      return data.map((user: any): User => ({
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role as 'admin' | 'operator' | 'viewer',
        avatar: user.avatar,
        createdAt: user.created_at || new Date().toISOString(),
        updatedAt: user.last_login || new Date().toISOString()
      }))
    } catch (error) {
      console.error('Error fetching users:', error)
      return []
    }
  },

  async getStatusHistory(): Promise<StatusHistory[]> {
    try {
      const response = await fetch('/data/status_history.json')
      const data = await response.json()
      return data
    } catch (error) {
      console.error('Error fetching status history:', error)
      return []
    }
  },

  async getWorkOrders(): Promise<WorkOrder[]> {
    try {
      const response = await fetch('/data/work_orders.json')
      const data = await response.json()
      return data
    } catch (error) {
      console.error('Error fetching work orders:', error)
      return []
    }
  },

  // Mock API methods for CRUD operations
  async createTransformer(transformer: Omit<Transformer, 'id' | 'createdAt' | 'updatedAt'>): Promise<Transformer> {
    const newTransformer: Transformer = {
      ...transformer,
      id: Date.now(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    return newTransformer
  },

  async updateTransformer(id: number, updates: Partial<Transformer>): Promise<Transformer> {
    // In a real app, this would make an HTTP request
    const transformers = await this.getTransformers()
    const transformer = transformers.find(t => t.id === id)
    if (!transformer) throw new Error('Transformer not found')
    
    return {
      ...transformer,
      ...updates,
      updatedAt: new Date().toISOString()
    }
  },

  async deleteTransformer(id: number): Promise<void> {
    // In a real app, this would make an HTTP request
    console.log('Deleting transformer:', id)
  },

  async createWorkOrder(workOrder: Omit<WorkOrder, 'id' | 'createdAt' | 'updatedAt'>): Promise<WorkOrder> {
    const newWorkOrder: WorkOrder = {
      ...workOrder,
      id: Date.now(),
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    return newWorkOrder
  },

  async uploadFile(file: File): Promise<string> {
    // Mock file upload - in real app would upload to server
    return URL.createObjectURL(file)
  }
}

// WebSocket service for real-time updates
export class WebSocketService {
  private ws: WebSocket | null = null
  private callbacks: Map<string, Function[]> = new Map()

  connect(url: string = 'ws://localhost:8080/ws') {
    try {
      this.ws = new WebSocket(url)
      
      this.ws.onopen = () => {
        console.log('WebSocket connected')
        this.emit('connected', null)
      }
      
      this.ws.onmessage = (event) => {
        try {
          const data = JSON.parse(event.data)
          this.emit(data.type, data.payload)
        } catch (error) {
          console.error('Error parsing WebSocket message:', error)
        }
      }
      
      this.ws.onclose = () => {
        console.log('WebSocket disconnected')
        this.emit('disconnected', null)
        
        // Auto-reconnect after 5 seconds
        setTimeout(() => this.connect(url), 5000)
      }
      
      this.ws.onerror = (error) => {
        console.error('WebSocket error:', error)
        this.emit('error', error)
      }
    } catch (error) {
      console.error('Failed to connect WebSocket:', error)
    }
  }

  disconnect() {
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
  }

  send(type: string, payload: any) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify({ type, payload }))
    }
  }

  on(event: string, callback: Function) {
    if (!this.callbacks.has(event)) {
      this.callbacks.set(event, [])
    }
    this.callbacks.get(event)!.push(callback)
  }

  off(event: string, callback: Function) {
    const callbacks = this.callbacks.get(event)
    if (callbacks) {
      const index = callbacks.indexOf(callback)
      if (index !== -1) {
        callbacks.splice(index, 1)
      }
    }
  }

  private emit(event: string, data: any) {
    const callbacks = this.callbacks.get(event)
    if (callbacks) {
      callbacks.forEach(callback => callback(data))
    }
  }
}

export const wsService = new WebSocketService()
