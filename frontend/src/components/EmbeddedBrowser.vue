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

      <el-checkbox v-model="enableClickMode" style="margin-left: 16px;">
        点击选择模式
      </el-checkbox>

      <el-button
        v-if="iframeUrl"
        style="margin-left: 16px;"
        @click="refreshPage"
        :loading="loading"
      >
        刷新
      </el-button>
    </div>

    <!-- Click Mode Instruction -->
    <div v-if="enableClickMode && iframeUrl" class="click-mode-hint">
      <el-alert type="info" :closable="false" show-icon>
        <template #title>
          点击选择模式已启用
        </template>
        <template #default>
          点击 iframe 中的页面元素将自动生成选择器。
          <span v-if="!isSameOrigin">
            由于跨域限制，请手动输入元素信息来生成选择器。
          </span>
        </template>
      </el-alert>
    </div>

    <!-- Content Area -->
    <div class="browser-content">
      <!-- Browser Frame -->
      <div class="browser-frame" ref="frameContainer">
        <div v-if="loading" class="loading-mask">
          <el-icon class="is-loading" :size="40"><Loading /></el-icon>
          <p>页面加载中...</p>
        </div>

        <iframe
          v-show="!loading && iframeUrl"
          ref="browserFrame"
          :src="iframeUrl"
          class="browser-iframe"
          @load="onIframeLoad"
          @error="onIframeError"
        />

        <div v-if="!iframeUrl && !loading" class="empty-state">
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

        <!-- Manual Selector Input (for cross-origin iframes) -->
        <div v-if="enableClickMode && iframeUrl && !isSameOrigin" class="manual-input-section">
          <el-input
            v-model="manualTagName"
            placeholder="标签名，例如: div, span, a"
            clearable
            @input="generateFromManualInput"
          />
          <el-input
            v-model="manualId"
            placeholder="ID (可选)"
            clearable
            @input="generateFromManualInput"
          />
          <el-input
            v-model="manualClasses"
            placeholder="类名，多个用空格分隔 (可选)"
            clearable
            @input="generateFromManualInput"
          />
          <el-input
            v-model="manualAttributes"
            placeholder="其他属性，例如: data-testid, name"
            clearable
            @input="generateFromManualInput"
          />
        </div>

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
import { ref, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

// Refs
const url = ref('')
const loading = ref(false)
const iframeUrl = ref('')
const browserFrame = ref(null)
const frameContainer = ref(null)

// Selector state
const selectorType = ref('CSS')
const enableClickMode = ref(false)
const cssSelector = ref('')
const xpathSelector = ref('')
const testResult = ref('')
const lastClickedElement = ref(null)

// Manual input for cross-origin iframes
const manualTagName = ref('')
const manualId = ref('')
const manualClasses = ref('')
const manualAttributes = ref('')

// CORS detection
const isSameOrigin = ref(false)

// Load page into iframe
const loadPage = () => {
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
  iframeUrl.value = normalizedUrl
  cssSelector.value = ''
  xpathSelector.value = ''
  testResult.value = ''
  lastClickedElement.value = null
  isSameOrigin.value = false
}

// Refresh current page
const refreshPage = () => {
  if (iframeUrl.value) {
    loading.value = true
    // Force iframe reload by removing and re-adding src
    const iframe = browserFrame.value
    if (iframe) {
      const currentSrc = iframe.src
      iframe.src = ''
      setTimeout(() => {
        iframe.src = currentSrc
      }, 100)
    }
  }
}

// Iframe load handler
const onIframeLoad = () => {
  loading.value = false

  // Check if same origin (can only access iframe content if same origin)
  try {
    const iframe = browserFrame.value
    if (iframe && iframe.contentDocument) {
      isSameOrigin.value = true
      if (enableClickMode.value) {
        setupClickInterception()
      }
    } else {
      isSameOrigin.value = false
    }
  } catch {
    isSameOrigin.value = false
  }
}

// Iframe error handler
const onIframeError = () => {
  loading.value = false
  ElMessage.error('页面加载失败，请检查URL是否可访问')
}

// Setup click interception in iframe
const setupClickInterception = () => {
  const iframe = browserFrame.value
  if (!iframe || !iframe.contentDocument) return

  const iframeDoc = iframe.contentDocument

  // Remove existing listener if any
  iframeDoc.removeEventListener('click', handleIframeClick, true)

  // Add click listener
  iframeDoc.addEventListener('click', handleIframeClick, true)
}

// Handle click inside iframe
const handleIframeClick = (event) => {
  if (!enableClickMode.value) return

  event.preventDefault()
  event.stopPropagation()

  const target = event.target
  lastClickedElement.value = {
    tagName: target.tagName.toLowerCase(),
    id: target.id || '',
    className: target.className || '',
    textContent: target.textContent || ''
  }

  // Generate selectors
  cssSelector.value = generateCssSelector(target)
  xpathSelector.value = generateXPath(target)

  // Also send message for cross-origin support
  try {
    iframe.contentWindow.postMessage({
      type: 'ELEMENT_CLICKED',
      tagName: target.tagName.toLowerCase(),
      id: target.id,
      className: target.className,
      textContent: truncateText(target.textContent, 200)
    }, '*')
  } catch {
    // Ignore postMessage errors for cross-origin
  }
}

// CSS Selector Generation Algorithm (ID > class > tag path)
const generateCssSelector = (element) => {
  if (!element) return ''

  // Step 1: If element has ID, use it (highest priority)
  if (element.id) {
    const idSelector = `#${element.id}`
    // Verify it's unique
    if (isUniqueSelector(idSelector, 'CSS')) {
      return idSelector
    }
    // ID might not be unique, fall through
  }

  // Step 2: Build selector from classes
  if (element.className && typeof element.className === 'string') {
    const classes = element.className.trim().split(/\s+/).filter(c => c)
    if (classes.length > 0) {
      // Try each class combination from most specific to least
      for (let i = classes.length; i > 0; i--) {
        const classCombo = classes.slice(0, i).join('.')
        const selector = `${element.tagName.toLowerCase()}.${classCombo}`
        if (isUniqueSelector(selector, 'CSS')) {
          return selector
        }
      }
      // Try classes alone
      const classSelector = '.' + classes.join('.')
      if (isUniqueSelector(classSelector, 'CSS')) {
        return classSelector
      }
    }
  }

  // Step 3: Build path from tag names
  const path = []
  let current = element

  while (current && current !== document.body && current.parentElement) {
    let tag = current.tagName.toLowerCase()

    // Add index if multiple siblings of same tag
    const siblings = Array.from(current.parentElement.children).filter(
      child => child.tagName === current.tagName
    )
    if (siblings.length > 1) {
      const index = siblings.indexOf(current) + 1
      tag += `:nth-of-type(${index})`
    }

    // Add class if available (first class only)
    if (current.className && typeof current.className === 'string') {
      const firstClass = current.className.trim().split(/\s+/)[0]
      if (firstClass) {
        tag += `.${firstClass}`
      }
    }

    path.unshift(tag)
    current = current.parentElement
  }

  return path.join(' > ')
}

// XPath Generation Algorithm (DOM tree path)
const generateXPath = (element) => {
  if (!element) return ''

  const parts = []
  let current = element

  while (current && current.nodeType === Node.ELEMENT_NODE && current !== document.body) {
    let index = 1
    let sibling = current.previousSibling

    // Count previous siblings of same tag name
    while (sibling) {
      if (sibling.nodeType === Node.ELEMENT_NODE && sibling.tagName === current.tagName) {
        index++
      }
      sibling = sibling.previousSibling
    }

    const tagName = current.tagName.toLowerCase()
    const part = `${tagName}[${index}]`
    parts.unshift(part)
    current = current.parentElement
  }

  return '/' + parts.join('/')
}

// Check if selector is unique in document
const isUniqueSelector = (selector, type) => {
  if (!browserFrame.value) return false

  try {
    let doc = browserFrame.value.contentDocument
    if (!doc) return false

    if (type === 'CSS') {
      const elements = doc.querySelectorAll(selector)
      return elements.length === 1
    }
  } catch {
    // Invalid selector or cross-origin
  }
  return false
}

// Generate selectors from manual input
const generateFromManualInput = () => {
  if (!manualTagName.value) {
    cssSelector.value = ''
    xpathSelector.value = ''
    return
  }

  const tag = manualTagName.value.toLowerCase()
  const id = manualId.value.trim()
  const classes = manualClasses.value.trim().split(/\s+/).filter(c => c)
  const attrs = manualAttributes.value.trim()

  // Build CSS selector
  let css = tag
  if (id) {
    css = `#${id}`
  } else if (classes.length > 0) {
    css = `${tag}.${classes.join('.')}`
  }
  if (attrs) {
    css += `[${attrs}]`
  }
  cssSelector.value = css

  // Build XPath
  xpathSelector.value = `//${tag}` +
    (id ? `[@id='${id}']` : '') +
    (classes.length > 0 ? `[@class='${classes.join(' ')}']` : '') +
    (attrs ? `[@${attrs}]` : '')
}

// Copy selector to clipboard
const copySelector = async (type) => {
  const selector = type === 'CSS' ? cssSelector.value : xpathSelector.value
  if (!selector) return

  try {
    await navigator.clipboard.writeText(selector)
    ElMessage.success(`${type === 'CSS' ? 'CSS' : 'XPath'} 选择器已复制到剪贴板`)
  } catch {
    // Fallback for older browsers
    const textarea = document.createElement('textarea')
    textarea.value = selector
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
    ElMessage.success(`${type === 'CSS' ? 'CSS' : 'XPath'} 选择器已复制到剪贴板`)
  }
}

// Test selector in iframe
const testSelector = (type) => {
  const selector = type === 'CSS' ? cssSelector.value : xpathSelector.value
  if (!selector || !browserFrame.value) {
    testResult.value = ''
    return
  }

  try {
    const iframe = browserFrame.value
    const doc = iframe.contentDocument || iframe.contentWindow?.document

    if (!doc) {
      testResult.value = '无法访问 iframe 文档内容（跨域限制）'
      return
    }

    let elements = []
    let selectorType = ''

    if (type === 'CSS') {
      elements = Array.from(doc.querySelectorAll(selector))
      selectorType = 'CSS'
    } else {
      // XPath evaluation
      const result = doc.evaluate(selector, doc, null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null)
      for (let i = 0; i < result.snapshotLength; i++) {
        elements.push(result.snapshotItem(i))
      }
      selectorType = 'XPath'
    }

    if (elements.length === 0) {
      testResult.value = `未找到匹配的元素 (${selectorType}: ${selector})`
    } else if (elements.length === 1) {
      const el = elements[0]
      testResult.value = `✓ 找到 1 个匹配元素\n` +
        `标签: ${el.tagName.toLowerCase()}\n` +
        `ID: ${el.id || '(无)'}\n` +
        `类名: ${el.className || '(无)'}\n` +
        `文本: ${truncateText(el.textContent, 100)}`
    } else {
      testResult.value = `✓ 找到 ${elements.length} 个匹配元素\n\n` +
        elements.slice(0, 5).map((el, i) =>
          `${i + 1}. <${el.tagName.toLowerCase()}` +
          (el.id ? ` id="${el.id}"` : '') +
          (el.className ? ` class="${el.className}"` : '') +
          `>`
        ).join('\n') +
        (elements.length > 5 ? `\n... 还有 ${elements.length - 5} 个元素` : '')
    }
  } catch (error) {
    testResult.value = `选择器错误: ${error.message}`
  }
}

// Helper: truncate text
const truncateText = (text, maxLength) => {
  if (!text) return ''
  const trimmed = text.trim().replace(/\s+/g, ' ')
  return trimmed.length > maxLength ? trimmed.substring(0, maxLength) + '...' : trimmed
}

// Watch for click mode changes
watch(enableClickMode, (newVal) => {
  if (newVal && iframeUrl.value && isSameOrigin.value) {
    nextTick(() => {
      setupClickInterception()
    })
  }
})

// Handle messages from iframe content script
const handleMessage = (event) => {
  if (event.data && event.data.type === 'ELEMENT_CLICKED') {
    lastClickedElement.value = {
      tagName: event.data.tagName,
      id: event.data.id || '',
      className: event.data.className || '',
      textContent: event.data.textContent || ''
    }

    // Generate selectors from received data
    const tag = event.data.tagName
    const id = event.data.id
    const className = event.data.className

    // CSS Selector
    if (id) {
      cssSelector.value = `#${id}`
    } else if (className) {
      cssSelector.value = `${tag}.${className.split(/\s+/)[0]}`
    } else {
      cssSelector.value = tag
    }

    // XPath
    xpathSelector.value = `//${tag}` + (id ? `[@id='${id}']` : '') + (className ? `[@class='${className}']` : '')
  }
}

// Setup message listener
onMounted(() => {
  window.addEventListener('message', handleMessage)
})

// Cleanup
onUnmounted(() => {
  window.removeEventListener('message', handleMessage)

  // Remove click listener from iframe
  if (browserFrame.value && browserFrame.value.contentDocument) {
    browserFrame.value.contentDocument.removeEventListener('click', handleIframeClick, true)
  }
})

// Expose methods for parent components
defineExpose({
  getCssSelector: () => cssSelector.value,
  getXpathSelector: () => xpathSelector.value,
  getCurrentUrl: () => iframeUrl.value
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
  overflow: hidden;
}

.browser-iframe {
  width: 100%;
  height: 100%;
  border: none;
  background: white;
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

.manual-input-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 16px;
}

.manual-input-section :deep(.el-input) {
  width: 100%;
}
</style>
