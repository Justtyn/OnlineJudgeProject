import { defineStore } from 'pinia'

interface PageState {
  scrollPositions: Record<string, number>
  formData: Record<string, any>
}

export const usePageStateStore = defineStore('pageState', {
  state: (): PageState => ({
    scrollPositions: {},
    formData: {}
  }),
  
  actions: {
    saveScrollPosition(path: string, position: number) {
      this.scrollPositions[path] = position
    },
    
    getScrollPosition(path: string): number {
      return this.scrollPositions[path] || 0
    },
    
    saveFormData(path: string, data: any) {
      this.formData[path] = data
    },
    
    getFormData(path: string): any {
      return this.formData[path]
    },
    
    clearPageState(path: string) {
      delete this.scrollPositions[path]
      delete this.formData[path]
    }
  }
}) 