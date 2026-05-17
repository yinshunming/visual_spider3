<template>
  <div class="embedded-browser">
    <!-- Toolbar -->
    <div class="browser-toolbar">
      <el-input
        v-model="url"
        placeholder="输入URL，例如: https://example.com"
        style="width: 400px;"
        @keyup.enter="loadPage"
        clearable
      >
        <template #append>
          <el-button @click="loadPage" :loading="loading" :disabled="!url">
            加载页面
          </el-button>
        </template>
      </el-input>

      <el-radio-group v-model="selectorType" style="margin-left: 16px;">
        <el-radio-button label="CSS">CSS 选择器</el-radio-button>
        <el-radio-button label="XPATH">XPath</el-radio-button>
      </el-radio-group>

      <el-button
        v-if="sessionId"
        style="margin-left: 16px;"
        @click="refreshPage"
        :loading="loading"
      >
        刷新
      </el-button>

      <el-button
        v-if="sessionId"
        style="margin-left: 8px;"
        @click="closeSession"
        type="danger"
        plain
      >
        关闭浏览器
      </el-button>
    </div>

    <!-- Click Mode Instruction -->
    <div v-if="sessionId" class="click-mode-hint">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          点击选择模式
        </template>
        <template #default>
          点击右侧截图中的页面元素将自动生成选择器。
        </template>
      </el-alert>
    </div>

    <!-- Content Area -->
    <div class="browser-content">
      <!-- Browser Frame -->
      <div class="browser-frame" ref="frameContainer">
        <div v-if="loading" class="loading-mask">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>{{ loadingMessage }}</p>
        </div>

        <!-- Screenshot Display -->
        <div v-if="screenshotData && !loading" class="screenshot-container" @click="handleScreenshotClick">
          <img
            ref="screenshotImage"
            :src="'data:image/png;base64,' + screenshotData"
            class="screenshot-image"
            :style="{ width: screenshotWidth + 'px', height: screenshotHeight + 'px' }"
          />
          <!-- Click marker -->
          <div
            v-if="clickMarker"
            class="click-marker"
            :style="{ left: clickMarker.x + 'px', top: clickMarker.y + 'px' }"
          ></div>
        </div>

        <!-- Session expired message -->
        <div v-if="sessionExpired && !loading" class="error-state">
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>
              Session 已过期
            </template>
            <template #default>
              请重新加载页面
            </template>
          </el-alert>
        </div>

        <div v-if="!sessionId && !loading && !sessionExpired" class="empty-state">
          <el-empty description="输入URL并点击加载页面开始">
            <template #image>
              <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6"/>
              </svg>
            </template>
          </el-empty>
        </div>
      </div>

      <!-- Selector Panel -->
      <div class="selector-panel">
        <h4 class="panel-title">生成的选择器</h4>

        <!-- CSS Selector -->
        <div class="selector-section">
          <label class="selector-label">CSS 选择器:</label>
          <el-input
            v-model="cssSelector"
            type="textarea"
            :rows="3"
            placeholder="点击页面元素将显示 CSS 选择器..."
            readonly
          />
          <div class="selector-actions" v-if="cssSelector">
            <el-button size="small" type="primary" @click="copySelector('CSS')">
              复制 CSS
            </el-button>
            <el-button size="small" @click="testSelector('CSS')">
              测试
            </el-button>
          </div>
        </div>

        <!-- XPath Selector -->
        <div class="selector-section">
          <label class="selector-label">XPath:</label>
          <el-input
            v-model="xpathSelector"
            type="textarea"
            :rows="3"
            placeholder="点击页面元素将显示 XPath..."
            readonly
          />
          <div class="selector-actions" v-if="xpathSelector">
            <el-button size="small" type="primary" @click="copySelector('XPATH')">
              复制 XPath
            </el-button>
            <el-button size="small" @click="testSelector('XPATH')">
              测试
            </el-button>
          </div>
        </div>

        <!-- Test Result -->
        <div v-if="testResult" class="test-result">
          <el-divider content-position="left">测试结果</el-divider>
          <pre class="test-result-content">{{ testResult }}</pre>
        </div>

        <!-- Element Info -->
        <div v-if="lastClickedElement" class="element-info">
          <el-divider content-position="left">最近点击的元素</el-divider>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="标签">
              {{ lastClickedElement.tagName }}
            </el-descriptions-item>
            <el-descriptions-item label="ID" v-if="lastClickedElement.id">
              {{ lastClickedElement.id }}
            </el-descriptions-item>
            <el-descriptions-item label="类名" v-if="lastClickedElement.className">
              {{ lastClickedElement.className }}
            </el-descriptions-item>
            <el-descriptions-item label="文本内容" v-if="lastClickedElement.textContent" :width="200">
              {{ truncateText(lastClickedElement.textContent, 100) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'
import * as playwrightApi from '../api/playwright'

const url = ref('')
const loading = ref(false)
const loadingMessage = ref('页面加载中...')
const sessionId = ref(null)
const sessionExpired = ref(false)

const screenshotData = ref('')
const screenshotWidth = ref(1280)
const screenshotHeight = ref(720)
const screenshotImage = ref(null)

const selectorType = ref('CSS')
const cssSelector = ref('')
const xpathSelector = ref('')
const testResult = ref('')
const lastClickedElement = ref(null)
const clickMarker = ref(null)

let pingInterval = null

const loadPage = async () => {
  if (!url.value) return

  let normalizedUrl = url.value.trim()
  if (!normalizedUrl.startsWith('http://') && !normalizedUrl.startsWith('https://')) {
    normalizedUrl = 'https://' + normalizedUrl
  }

  try {
    new URL(normalizedUrl)
  } catch {
    ElMessage.error('请输入有效的URL')
    return
  }

  loading.value = true
  loadingMessage.value = '正在创建浏览器会话...'
  sessionExpired.value = false
  cssSelector.value = ''
  xpathSelector.value = ''
  testResult.value = ''
  lastClickedElement.value = null
  clickMarker.value = null

  try {
    const res = await playwrightApi.createSession(normalizedUrl)
    sessionId.value = res.data.sessionId

    loadingMessage.value = '正在获取截图...'
    await refreshScreenshot()

    startPingInterval()
  } catch (err) {
    handleApiError(err)
  } finally {
    loading.value = false
  }
}

const refreshPage = async () => {
  if (!sessionId.value) return

  loading.value = true
  loadingMessage.value = '正在刷新...'

  try {
    await refreshScreenshot()
  } catch (err) {
    handleApiError(err)
  } finally {
    loading.value = false
  }
}

const refreshScreenshot = async () => {
  if (!sessionId.value) return

  const res = await playwrightApi.getScreenshot(sessionId.value)
  screenshotData.value = res.data.data
  screenshotWidth.value = res.data.width
  screenshotHeight.value = res.data.height
}

const closeSession = async () => {
  if (!sessionId.value) return

  try {
    await playwrightApi.closeSession(sessionId.value)
  } catch (err) {
    console.warn('Close session failed:', err)
  }

  sessionId.value = null
  screenshotData.value = ''
  stopPingInterval()
}

const startPingInterval = () => {
  stopPingInterval()
  pingInterval = setInterval(async () => {
    if (!sessionId.value) {
      stopPingInterval()
      return
    }
    try {
      await playwrightApi.pingSession(sessionId.value)
    } catch (err) {
      if (err.response && err.response.status === 404) {
        sessionExpired.value = true
        stopPingInterval()
      }
    }
  }, 30000)
}

const stopPingInterval = () => {
  if (pingInterval) {
    clearInterval(pingInterval)
    pingInterval = null
  }
}

const handleScreenshotClick = async (event) => {
  if (!sessionId.value || !screenshotImage.value) return

  const rect = screenshotImage.value.getBoundingClientRect()
  const scaleX = screenshotWidth.value / rect.width
  const scaleY = screenshotHeight.value / rect.height

  const x = Math.round(event.offsetX * scaleX)
  const y = Math.round(event.offsetY * scaleY)

  clickMarker.value = { x: event.offsetX, y: event.offsetY }

  loading.value = true
  loadingMessage.value = '正在获取元素信息...'

  try {
    const res = await playwrightApi.getElementAt(sessionId.value, x, y)
    const elementInfo = res.data

    lastClickedElement.value = {
      tagName: elementInfo.tagName,
      id: elementInfo.id || '',
      className: elementInfo.className || '',
      textContent: elementInfo.textContent || ''
    }

    const fakeElement = {
      tagName: elementInfo.tagName,
      id: elementInfo.id || '',
      className: elementInfo.className || ''
    }

    cssSelector.value = generateCssSelector(fakeElement)
    xpathSelector.value = generateXPath(fakeElement)

  } catch (err) {
    handleApiError(err)
  } finally {
    loading.value = false
  }
}

const generateCssSelector = (element) => {
  if (!element) return ''

  if (element.id) {
    const idSelector = `#${element.id}`
    return idSelector
  }

  if (element.className && typeof element.className === 'string') {
    const classes = element.className.trim().split(/\s+/).filter(c => c)
    if (classes.length > 0) {
      return `${element.tagName.toLowerCase()}.${classes.join('.')}`
    }
  }

  return element.tagName.toLowerCase()
}

const generateXPath = (element) => {
  if (!element) return ''

  const parts = []
  let current = element

  while (current && current.tagName) {
    const tagName = current.tagName.toLowerCase()
    parts.unshift(tagName)
    current = { tagName: 'BODY' }
  }

  return '/' + parts.join('/')
}

const testSelector = async (type) => {
  if (!sessionId.value) return

  const selector = type === 'CSS' ? cssSelector.value : xpathSelector.value
  if (!selector) return

  loading.value = true
  loadingMessage.value = '正在测试选择器...'

  try {
    const res = await playwrightApi.testSelector(sessionId.value, selector, type)
    const result = res.data

    if (result.unique) {
      testResult.value = `✓ 选择器唯一，匹配 1 个元素`
    } else {
      testResult.value = `⚠ 选择器匹配 ${result.count} 个元素，请优化\n\n` +
        result.elements.slice(0, 5).map((el, i) =>
          `${i + 1}. <${el.tagName}>` +
          (el.id ? ` id="${el.id}"` : '') +
          (el.className ? ` class="${el.className}"` : '')
        ).join('\n')
    }
  } catch (err) {
    if (err.response && err.response.data && err.response.data.errorCode === 'INVALID_SELECTOR') {
      testResult.value = `✗ 选择器语法错误：${err.response.data.message}`
    } else {
      handleApiError(err)
    }
  } finally {
    loading.value = false
  }
}

const copySelector = async (type) => {
  const selector = type === 'CSS' ? cssSelector.value : xpathSelector.value
  if (!selector) return

  try {
    await navigator.clipboard.writeText(selector)
    ElMessage.success(`${type === 'CSS' ? 'CSS' : 'XPath'} 选择器已复制到剪贴板`)
  } catch {
    const textarea = document.createElement('textarea')
    textarea.value = selector
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success(`${type === 'CSS' ? 'CSS' : 'XPath'} 选择器已复制到剪贴板`)
  }
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  const trimmed = text.trim().replace(/\s+/g, ' ')
  return trimmed.length > maxLength ? trimmed.substring(0, maxLength) + '...' : trimmed
}

const handleApiError = (err) => {
  if (err.response) {
    const { status, data } = err.response
    if (status === 404) {
      sessionExpired.value = true
      ElMessage.error('Session 已过期，请重新加载页面')
    } else if (status === 409) {
      ElMessage.error('已达最大并发数，请关闭其他浏览器窗口后重试')
    } else if (status === 400) {
      ElMessage.error(`请求错误：${data.message}`)
    } else if (status === 500) {
      ElMessage.error('服务器错误，请稍后重试')
    } else {
      ElMessage.error(`错误：${data.message || '未知错误'}`)
    }
  } else {
    ElMessage.error('网络错误，请检查连接')
  }
}

const handleMessage = (event) => {
  if (event.data && event.data.type === 'ELEMENT_CLICKED') {
    lastClickedElement.value = {
      tagName: event.data.tagName,
      id: event.data.id || '',
      className: event.data.className || '',
      textContent: event.data.textContent || ''
    }

    const tag = event.data.tagName
    const id = event.data.id
    const className = event.data.className

    if (id) {
      cssSelector.value = `#${id}`
    } else if (className) {
      cssSelector.value = `${tag}.${className.split(/\s+/)[0]}`
    } else {
      cssSelector.value = tag
    }

    xpathSelector.value = `//${tag}` + (id ? `[@id='${id}']` : '') + (className ? `[@class='${className}']` : '')
  }
}

onMounted(() => {
  window.addEventListener('message', handleMessage)
})

onUnmounted(() => {
  window.removeEventListener('message', handleMessage)
  stopPingInterval()
  if (sessionId.value) {
    playwrightApi.closeSession(sessionId.value).catch(() => {})
  }
})

defineExpose({
  getCssSelector: () => cssSelector.value,
  getXpathSelector: () => xpathSelector.value,
  getCurrentUrl: () => url.value
})
</script>

<style scoped>
.embedded-browser {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

.browser-toolbar {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #e4e7ed;
  gap: 8px;
  flex-wrap: wrap;
}

.click-mode-hint {
  padding: 8px 16px;
  background: #ecf5ff;
}

.browser-content {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.browser-frame {
  flex: 1;
  position: relative;
  background: #fafafa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin: 8px;
  overflow: auto;
}

.screenshot-container {
  position: relative;
  display: inline-block;
  margin: 8px;
}

.screenshot-image {
  display: block;
  cursor: crosshair;
  border: 1px solid #e4e7ed;
}

.click-marker {
  position: absolute;
  width: 20px;
  height: 20px;
  border: 2px solid red;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  pointer-events: none;
  background: rgba(255, 0, 0, 0.1);
}

.loading-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
  z-index: 10;
}

.loading-mask p {
  margin-top: 12px;
  color: #606266;
}

.empty-state {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

.selector-panel {
  width: 360px;
  padding: 16px;
  background: white;
  border-left: 1px solid #e4e7ed;
  overflow-y: auto;
}

.panel-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.selector-section {
  margin-bottom: 16px;
}

.selector-label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #606266;
}

.selector-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
}

.test-result {
  margin-top: 16px;
}

.test-result-content {
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.element-info {
  margin-top: 16px;
}
</style>