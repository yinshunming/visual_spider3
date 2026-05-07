import { describe, it, expect } from 'vitest'
import * as api from '../index.js'

describe('API Module', () => {
  describe('exports', () => {
    it('should export getTasks function', () => {
      expect(typeof api.getTasks).toBe('function')
    })

    it('should export getTask function', () => {
      expect(typeof api.getTask).toBe('function')
    })

    it('should export createTask function', () => {
      expect(typeof api.createTask).toBe('function')
    })

    it('should export updateTask function', () => {
      expect(typeof api.updateTask).toBe('function')
    })

    it('should export deleteTask function', () => {
      expect(typeof api.deleteTask).toBe('function')
    })

    it('should export enableTask function', () => {
      expect(typeof api.enableTask).toBe('function')
    })

    it('should export disableTask function', () => {
      expect(typeof api.disableTask).toBe('function')
    })

    it('should export runTask function', () => {
      expect(typeof api.runTask).toBe('function')
    })

    it('should export default api instance', () => {
      expect(api.default).toBeDefined()
      expect(typeof api.default.get).toBe('function')
      expect(typeof api.default.post).toBe('function')
      expect(typeof api.default.put).toBe('function')
      expect(typeof api.default.delete).toBe('function')
    })
  })

  describe('function signatures', () => {
    it('getTasks should accept params object', () => {
      // Should not throw with correct params
      expect(() => api.getTasks({ page: 1, size: 10 })).not.toThrow()
    })

    it('getTask should accept id number', () => {
      expect(() => api.getTask(1)).not.toThrow()
    })

    it('createTask should accept data object', () => {
      expect(() => api.createTask({ name: 'Test' })).not.toThrow()
    })

    it('updateTask should accept id and data', () => {
      expect(() => api.updateTask(1, { name: 'Test' })).not.toThrow()
    })

    it('deleteTask should accept id', () => {
      expect(() => api.deleteTask(1)).not.toThrow()
    })

    it('enableTask should accept id', () => {
      expect(() => api.enableTask(1)).not.toThrow()
    })

    it('disableTask should accept id', () => {
      expect(() => api.disableTask(1)).not.toThrow()
    })

    it('runTask should accept id', () => {
      expect(() => api.runTask(1)).not.toThrow()
    })
  })
})
