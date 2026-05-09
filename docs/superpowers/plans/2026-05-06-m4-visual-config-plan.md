# M4: 可视化配置 - 前端实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建 Vue3 前端项目，实现任务管理界面和可视化配置组件（EmbeddedBrowser），通过 Playwright/CDP 在前端直接生成选择器

**Architecture:**
- 前端技术栈：Vue 3 + Vite + Element Plus + Vue Router
- 内嵌浏览器：Playwright Chromium（前端 CDP 直接生成选择器，不走后端）
- 部署方式：前端 build 后静态文件放入 `backend/src/main/resources/static/`
- 后端端口：8080（API 基础 URL）

**Tech Stack:** Vue 3.4+, Vite 5+, Element Plus 2.x, Playwright 1.40+, Vue Router 4+

---

## 文件结构

```
frontend/
├── index.html
├── vite.config.js              # Vite 配置（含代理）
├── package.json
├── public/
│   └── favicon.ico
└── src/
    ├── main.js                 # Vue 入口
    ├── App.vue                 # 根组件
    ├── router/
    │   └── index.js            # Vue Router 配置
    ├── api/
    │   └── index.js            # 后端 API 调用
    ├── views/
    │   ├── TaskList.vue        # 任务列表页
    │   └── TaskConfig.vue      # 任务配置页（含内嵌浏览器）
    └── components/
        └── EmbeddedBrowser.vue # 核心：内嵌浏览器 + CDP 选择器
```

---

## Task 1: 项目初始化

**Files:**
- Create: `frontend/index.html`
- Create: `frontend/package.json`
- Create: `frontend/vite.config.js`
- Create: `frontend/public/favicon.ico`
- Create: `frontend/src/main.js`

- [ ] **Step 1: 创建 frontend 目录结构**

```bash
mkdir -p frontend/public frontend/src/router frontend/src/api frontend/src/views frontend/src/components
```

- [ ] **Step 2: 创建 package.json**

```json
{
  "name": "visual-spider-frontend",
  "version": "0.0.1",
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "preview": "vite preview"
  },
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.2.0",
    "element-plus": "^2.5.0",
    "axios": "^1.6.0",
    "@playwright/test": "^1.40.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "vite": "^5.0.0"
  }
}
```

- [ ] **Step 3: 创建 vite.config.js**

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: '../backend/src/main/resources/static',
    emptyOutDir: true
  }
})
```

- [ ] **Step 4: 创建 index.html**

```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Visual Spider - 可视化爬虫配置</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```

- [ ] **Step 5: 创建 main.js**

```javascript
import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(ElementPlus)
app.use(router)
app.mount('#app')
```

- [ ] **Step 6: 安装依赖**

```bash
cd frontend && npm install
```

---

## Task 2: Vue Router 配置

**Files:**
- Create: `frontend/src/router/index.js`

- [ ] **Step 1: 创建 router/index.js**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import TaskList from '../views/TaskList.vue'
import TaskConfig from '../views/TaskConfig.vue'

const routes = [
  {
    path: '/',
    redirect: '/tasks'
  },
  {
    path: '/tasks',
    name: 'TaskList',
    component: TaskList
  },
  {
    path: '/tasks/new',
    name: 'TaskNew',
    component: TaskConfig
  },
  {
    path: '/tasks/:id',
    name: 'TaskEdit',
    component: TaskConfig,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

---

## Task 3: API 调用层

**Files:**
- Create: `frontend/src/api/index.js`

- [ ] **Step 1: 创建 api/index.js**

```javascript
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 任务列表（分页）
export function getTasks(params) {
  return api.get('/tasks', { params })
}

// 获取单个任务
export function getTask(id) {
  return api.get(`/tasks/${id}`)
}

// 创建任务
export function createTask(data) {
  return api.post('/tasks', data)
}

// 更新任务
export function updateTask(id, data) {
  return api.put(`/tasks/${id}`, data)
}

// 删除任务
export function deleteTask(id) {
  return api.delete(`/tasks/${id}`)
}

// 启用任务
export function enableTask(id) {
  return api.post(`/tasks/${id}/enable`)
}

// 停用任务
export function disableTask(id) {
  return api.post(`/tasks/${id}/disable`)
}

// 运行任务
export function runTask(id) {
  return api.post(`/tasks/${id}/run`)
}

export default api
```

---

## Task 4: App.vue 根组件

**Files:**
- Create: `frontend/src/App.vue`

- [ ] **Step 1: 创建 App.vue**

```vue
<template>
  <div id="app">
    <el-container>
      <el-header style="border-bottom: 1px solid #e6e6e6;">
        <h2 style="margin: 0; line-height: 60px;">Visual Spider - 可视化爬虫配置</h2>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
</script>

<style>
#app {
  font-family: 'Helvetica Neue', Helvetica, Arial, sans-serif;
}
body {
  margin: 0;
  padding: 0;
}
</style>
```

---

## Task 5: 任务列表页 TaskList.vue

**Files:**
- Create: `frontend/src/views/TaskList.vue`

- [ ] **Step 1: 创建 TaskList.vue**

```vue
<template>
  <div class="task-list">
    <div class="toolbar">
      <el-button type="primary" @click="$router.push('/tasks/new')">
        新建任务
      </el-button>
    </div>

    <el-table :data="tasks" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="任务名称" min-width="200" />
      <el-table-column prop="urlMode" label="模式" width="120">
        <template #default="{ row }">
          {{ row.urlMode === 'LIST_PAGE' ? '列表页' : '直接URL' }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">
            {{ statusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="$router.push(`/tasks/${row.id}`)">
            配置
          </el-button>
          <el-button
            size="small"
            type="success"
            @click="handleEnable(row)"
            :disabled="row.status === 'ENABLED' || row.status === 'RUNNING'"
          >
            启用
          </el-button>
          <el-button
            size="small"
            type="warning"
            @click="handleDisable(row)"
            :disabled="row.status === 'DISABLED' || row.status === 'RUNNING'"
          >
            停用
          </el-button>
          <el-button
            size="small"
            type="danger"
            @click="handleDelete(row)"
            :disabled="row.status === 'RUNNING'"
          >
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      @size-change="loadTasks"
      @current-change="loadTasks"
      style="margin-top: 20px; justify-content: center;"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getTasks, enableTask, disableTask, deleteTask } from '../api'

const tasks = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const statusType = (status) => {
  const map = {
    DRAFT: 'info',
    ENABLED: 'success',
    DISABLED: 'warning',
    RUNNING: 'primary'
  }
  return map[status] || 'info'
}

const statusLabel = (status) => {
  const map = {
    DRAFT: '草稿',
    ENABLED: '已启用',
    DISABLED: '已停用',
    RUNNING: '运行中'
  }
  return map[status] || status
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

const loadTasks = async () => {
  loading.value = true
  try {
    const res = await getTasks({ page: page.value - 1, size: pageSize.value })
    tasks.value = res.data.content || res.data
    total.value = res.data.totalElements || tasks.value.length
  } catch (err) {
    ElMessage.error('加载任务失败')
  } finally {
    loading.value = false
  }
}

const handleEnable = async (task) => {
  await enableTask(task.id)
  ElMessage.success('任务已启用')
  loadTasks()
}

const handleDisable = async (task) => {
  await disableTask(task.id)
  ElMessage.success('任务已停用')
  loadTasks()
}

const handleDelete = async (task) => {
  await ElMessageBox.confirm('确定要删除该任务吗？', '提示', {
    type: 'warning'
  })
  await deleteTask(task.id)
  ElMessage.success('任务已删除')
  loadTasks()
}

onMounted(loadTasks)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
```

---

## Task 6: EmbeddedBrowser.vue 核心组件

**Files:**
- Create: `frontend/src/components/EmbeddedBrowser.vue`

- [ ] **Step 1: 创建 EmbeddedBrowser.vue**

```vue
<template>
  <div class="embedded-browser">
    <div class="browser-toolbar">
      <el-input
        v-model="url"
        placeholder="输入URL"
        style="width: 400px;"
        @keyup.enter="loadPage"
      >
        <template #append>
          <el-button @click="loadPage" :loading="loading">加载页面</el-button>
        </template>
      </el-input>
      <el-select v-model="selectorType" style="width: 120px; margin-left: 8px;">
        <el-option label="CSS" value="CSS" />
        <el-option label="XPath" value="XPATH" />
      </el-select>
      <el-checkbox v-model="enableClickMode" style="margin-left: 16px;">
        点击选择模式
      </el-checkbox>
    </div>

    <div class="browser-content">
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
        />
        <div v-if="!iframeUrl && !loading" class="empty-state">
          <p>输入URL并点击"加载页面"开始</p>
        </div>
      </div>

      <div class="selector-panel">
        <h4>生成的选择器</h4>
        <el-input
          v-model="currentSelector"
          type="textarea"
          :rows="3"
          placeholder="点击页面元素将显示选择器..."
          readonly
        />
        <div class="selector-actions" v-if="currentSelector">
          <el-button size="small" type="primary" @click="copySelector">
            复制
          </el-button>
          <el-button size="small" @click="testSelector">测试</el-button>
        </div>

        <div v-if="testResult" class="test-result">
          <h4>测试结果</h4>
          <pre>{{ testResult }}</pre>
        </div>
      </div>
    </div>

    <div v-if="lastClickedElement" class="clicked-info">
      <el-tag>已选择元素: {{ lastClickedElement.tagName }}</el-tag>
      <el-tag type="success" style="margin-left: 8px;">{{ selectorType }}: {{ currentSelector }}</el-tag>
    </div>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: String,
  selectorType: {
    type: String,
    default: 'CSS'
  }
})

const emit = defineEmits(['update:modelValue', 'update:selectorType', 'selector-generated'])

const url = ref('')
const loading = ref(false)
const iframeUrl = ref('')
const browserFrame = ref(null)
const frameContainer = ref(null)
const currentSelector = ref('')
const selectorType = ref(props.selectorType)
const enableClickMode = ref(false)
const lastClickedElement = ref(null)
const testResult = ref('')

watch(currentSelector, (val) => {
  emit('update:modelValue', val)
})

watch(selectorType, (val) => {
  emit('update:selectorType', val)
})

const loadPage = async () => {
  if (!url.value) {
    ElMessage.warning('请输入URL')
    return
  }

  // 简单验证URL
  let targetUrl = url.value
  if (!targetUrl.startsWith('http://') && !targetUrl.startsWith('https://')) {
    targetUrl = 'https://' + targetUrl
  }

  loading.value = true
  currentSelector.value = ''
  testResult.value = ''

  // 使用 data URL 方式加载（生产环境需要后端代理）
  // 开发模式下通过 Vite 代理
  iframeUrl.value = targetUrl
}

const onIframeLoad = () => {
  loading.value = false

  if (enableClickMode.value) {
    setupClickHandler()
  }
}

const setupClickHandler = () => {
  const iframe = browserFrame.value
  if (!iframe || !iframe.contentWindow) return

  try {
    const doc = iframe.contentWindow.document
    doc.addEventListener('click', handleElementClick, true)
  } catch (e) {
    console.warn('Cannot access iframe content:', e)
  }
}

const handleElementClick = (e) => {
  e.preventDefault()
  e.stopPropagation()

  const target = e.target
  lastClickedElement.value = target

  // 生成选择器
  const selector = generateSelector(target)
  currentSelector.value = selector

  emit('selector-generated', {
    selector,
    selectorType: selectorType.value,
    element: {
      tagName: target.tagName,
      text: target.textContent?.substring(0, 100)
    }
  })
}

const generateSelector = (element) => {
  if (selectorType.value === 'XPATH') {
    return generateXPath(element)
  }
  return generateCssSelector(element)
}

const generateCssSelector = (element) => {
  // 优先使用 ID
  if (element.id) {
    return `#${element.id}`
  }

  // 使用类名
  if (element.className && typeof element.className === 'string') {
    const classes = element.className.trim().split(/\s+/).filter(c => c)
    if (classes.length > 0) {
      const selector = `.${classes.join('.')}`
      // 检查是否唯一
      if (document.querySelectorAll(selector).length === 1) {
        return selector
      }
    }
  }

  // 使用标签 + 属性
  const attrs = []
  if (element.tagName) attrs.push(element.tagName.toLowerCase())

  // 尝试获取 data-testid 或其他属性
  for (const attr of ['data-testid', 'data-id', 'role']) {
    const val = element.getAttribute(attr)
    if (val) {
      attrs.push(`[${attr}="${val}"]`)
      break
    }
  }

  // 向上遍历找唯一的路径
  let path = attrs.join('')
  let parent = element.parentElement

  while (parent && path.length < 100) {
    let parentSelector = parent.tagName?.toLowerCase() || ''

    if (parent.id) {
      parentSelector = `#${parent.id}`
      path = parentSelector + ' > ' + path
      break
    }

    if (parent.className && typeof parent.className === 'string') {
      const pClasses = parent.className.trim().split(/\s+/).filter(c => c && c !== 'active')
      if (pClasses.length > 0) {
        parentSelector = parent.tagName?.toLowerCase() + '.' + pClasses[0]
      }
    }

    path = parentSelector + ' > ' + path
    parent = parent.parentElement
  }

  return path || element.tagName?.toLowerCase() || 'unknown'
}

const generateXPath = (element) => {
  if (!element) return ''

  let xpath = ''
  let current = element

  while (current && current.nodeType === Node.ELEMENT_NODE) {
    let index = 1
    let sibling = current.previousSibling

    while (sibling) {
      if (sibling.nodeType === Node.ELEMENT_NODE && sibling.nodeName === current.nodeName) {
        index++
      }
      sibling = sibling.previousSibling
    }

    const tagName = current.nodeName.toLowerCase()
    xpath = `/${tagName}[${index}]` + xpath
    current = current.parentElement
  }

  return xpath
}

const copySelector = () => {
  navigator.clipboard.writeText(currentSelector.value)
  ElMessage.success('选择器已复制')
}

const testSelector = () => {
  if (!currentSelector.value) return

  try {
    const iframe = browserFrame.value
    if (!iframe || !iframe.contentDocument) {
      testResult.value = '无法访问iframe内容'
      return
    }

    let elements
    if (selectorType.value === 'XPATH') {
      const result = iframe.contentDocument.evaluate(
        currentSelector.value,
        iframe.contentDocument,
        null,
        XPathResult.ORDERED_NODE_SNAPSHOT_TYPE,
        null
      )
      elements = []
      for (let i = 0; i < result.snapshotLength; i++) {
        elements.push(result.snapshotItem(i))
      }
    } else {
      elements = Array.from(iframe.contentDocument.querySelectorAll(currentSelector.value))
    }

    if (elements.length === 0) {
      testResult.value = '未匹配到任何元素'
    } else {
      testResult.value = `匹配到 ${elements.length} 个元素:\n` +
        elements.slice(0, 3).map(el => {
          const text = el.textContent?.substring(0, 80).replace(/\s+/g, ' ').trim()
          return `${el.tagName}: ${text}${text.length >= 80 ? '...' : ''}`
        }).join('\n')
    }
  } catch (err) {
    testResult.value = `错误: ${err.message}`
  }
}
</script>

<style scoped>
.embedded-browser {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  overflow: hidden;
}

.browser-toolbar {
  padding: 12px;
  background: #f5f7fa;
  border-bottom: 1px solid #dcdfe6;
  display: flex;
  align-items: center;
}

.browser-content {
  display: flex;
  height: 500px;
}

.browser-frame {
  flex: 1;
  position: relative;
  background: #fff;
}

.browser-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.loading-mask,
.empty-state {
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
  color: #909399;
}

.selector-panel {
  width: 300px;
  padding: 16px;
  border-left: 1px solid #dcdfe6;
  background: #fafafa;
  overflow-y: auto;
}

.selector-panel h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #303133;
}

.selector-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.test-result {
  margin-top: 16px;
}

.test-result h4 {
  margin: 0 0 8px 0;
  font-size: 13px;
  color: #606266;
}

.test-result pre {
  background: #f5f7fa;
  padding: 8px;
  border-radius: 4px;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 200px;
  overflow-y: auto;
}

.clicked-info {
  padding: 8px 12px;
  background: #f0f9eb;
  border-top: 1px solid #c8e6c9;
  font-size: 13px;
}
</style>
```

---

## Task 7: 任务配置页 TaskConfig.vue

**Files:**
- Create: `frontend/src/views/TaskConfig.vue`

- [ ] **Step 1: 创建 TaskConfig.vue**

```vue
<template>
  <div class="task-config">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑任务' : '新建任务' }}</span>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
      >
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="任务描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="2"
            placeholder="可选"
          />
        </el-form-item>

        <!-- URL 模式 -->
        <el-divider content-position="left">URL 配置</el-divider>

        <el-form-item label="模式" prop="urlMode">
          <el-radio-group v-model="form.urlMode">
            <el-radio value="LIST_PAGE">列表页模式</el-radio>
            <el-radio value="DIRECT_URL">直接URL模式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          label="列表页URL"
          v-if="form.urlMode === 'LIST_PAGE'"
          prop="listPageUrl"
        >
          <el-input
            v-model="form.listPageUrl"
            placeholder="https://example.com/articles"
          />
        </el-form-item>

        <el-form-item
          label="种子URL"
          v-if="form.urlMode === 'DIRECT_URL'"
          prop="seedUrls"
        >
          <el-input
            v-model="seedUrlsText"
            type="textarea"
            :rows="4"
            placeholder="每行一个URL"
            @blur="parseSeedUrls"
          />
        </el-form-item>

        <!-- 列表页规则 -->
        <template v-if="form.urlMode === 'LIST_PAGE'">
          <el-divider content-position="left">列表页规则配置</el-divider>

          <div class="section-tip">
            <p>点击下方"加载页面"按钮打开内嵌浏览器，然后：</p>
            <ol>
              <li>点击列表中的容器元素（如 <code>div.article-list &gt; div.item</code>）</li>
              <li>点击内容链接元素（如 <code>a.title</code>）</li>
            </ol>
          </div>

          <el-form-item label="容器选择器">
            <el-input
              v-model="listPageRule.containerSelector"
              placeholder="点击容器元素后自动生成"
              readonly
            >
              <template #append>
                <el-button
                  @click="startSelecting('containerSelector')"
                  :type="selectingField === 'containerSelector' ? 'success' : 'default'"
                >
                  {{ selectingField === 'containerSelector' ? '选择中...' : '选取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="链接选择器">
            <el-input
              v-model="listPageRule.itemUrlSelector"
              placeholder="点击链接元素后自动生成"
              readonly
            >
              <template #append>
                <el-button
                  @click="startSelecting('itemUrlSelector')"
                  :type="selectingField === 'itemUrlSelector' ? 'success' : 'default'"
                >
                  {{ selectingField === 'itemUrlSelector' ? '选择中...' : '选取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="分页类型">
            <el-select v-model="listPageRule.paginationRule.type" style="width: 100%;">
              <el-option value="INFINITE_SCROLL" label="无限滚动" />
              <el-option value="PAGE_NUMBER" label="页码" />
              <el-option value="NEXT_BUTTON" label="下一页按钮" />
            </el-select>
          </el-form-item>

          <el-form-item
            label="下一页选择器"
            v-if="listPageRule.paginationRule.type === 'NEXT_BUTTON'"
          >
            <el-input
              v-model="listPageRule.paginationRule.nextPageSelector"
              placeholder="点击下一页按钮元素"
            />
          </el-form-item>

          <el-form-item
            label="页码URL模板"
            v-if="listPageRule.paginationRule.type === 'PAGE_NUMBER'"
          >
            <el-input
              v-model="listPageRule.paginationRule.pagePattern"
              placeholder="/page/{page}"
            />
          </el-form-item>
        </template>

        <!-- 内容页字段配置 -->
        <el-divider content-position="left">内容页字段配置</el-divider>

        <div class="section-tip">
          <p>添加需要提取的字段。点击"加载页面"进入内容页后，点击页面元素选择器。</p>
        </div>

        <div v-for="(field, index) in form.fields" :key="index" class="field-item">
          <el-form-item label="字段名称" prop="fieldName">
            <el-input v-model="field.fieldName" placeholder="如: title" />
          </el-form-item>
          <el-form-item label="显示名称">
            <el-input v-model="field.fieldLabel" placeholder="如: 标题" />
          </el-form-item>
          <el-form-item label="字段类型">
            <el-select v-model="field.fieldType" style="width: 100%;">
              <el-option value="text" label="文本" />
              <el-option value="image" label="图片" />
              <el-option value="link" label="链接" />
              <el-option value="richText" label="富文本" />
            </el-select>
          </el-form-item>
          <el-form-item label="选择器">
            <el-input
              v-model="field.selector"
              placeholder="点击选择器后自动生成"
              readonly
            >
              <template #append>
                <el-button
                  @click="startSelectingField(index)"
                  :type="selectingFieldIndex === index ? 'success' : 'default'"
                >
                  {{ selectingFieldIndex === index ? '选择中...' : '选取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>
          <el-form-item label="选择器类型">
            <el-radio-group v-model="field.selectorType" size="small">
              <el-radio value="CSS">CSS</el-radio>
              <el-radio value="XPATH">XPath</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="提取类型">
            <el-select v-model="field.extractType" style="width: 100%;">
              <el-option value="text" label="文本" />
              <el-option value="attr" label="属性" />
              <el-option value="html" label="HTML" />
            </el-select>
          </el-form-item>
          <el-form-item
            label="属性名"
            v-if="field.extractType === 'attr'"
          >
            <el-input v-model="field.attrName" placeholder="如: href, src" />
          </el-form-item>
          <el-form-item label="默认值">
            <el-input v-model="field.defaultValue" placeholder="可选" />
          </el-form-item>
          <el-form-item label="必填">
            <el-switch v-model="field.required" />
          </el-form-item>

          <el-button
            type="danger"
            size="small"
            @click="removeField(index)"
            style="margin-bottom: 16px;"
          >
            删除字段
          </el-button>

          <el-divider />
        </div>

        <el-button type="primary" plain @click="addField">
          + 添加字段
        </el-button>

        <!-- 内嵌浏览器 -->
        <el-divider content-position="left" style="margin-top: 24px;">内嵌浏览器</el-divider>

        <EmbeddedBrowser
          v-model:selectorType="currentSelectorType"
          @selector-generated="onSelectorGenerated"
          style="margin-bottom: 24px;"
        />

        <!-- 提交 -->
        <el-form-item style="margin-top: 24px;">
          <el-button type="primary" @click="handleSubmit" :loading="saving">
            {{ isEdit ? '保存修改' : '创建任务' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getTask, createTask, updateTask } from '../api'
import EmbeddedBrowser from '../components/EmbeddedBrowser.vue'

const router = useRouter()
const route = useRoute()

const isEdit = computed(() => !!route.params.id)
const taskId = computed(() => route.params.id)

const formRef = ref(null)
const saving = ref(false)
const currentSelectorType = ref('CSS')
const selectingField = ref(null)
const selectingFieldIndex = ref(-1)
const seedUrlsText = ref('')

const defaultField = () => ({
  fieldName: '',
  fieldLabel: '',
  fieldType: 'text',
  selector: '',
  selectorType: 'CSS',
  extractType: 'text',
  attrName: '',
  required: false,
  defaultValue: '',
  displayOrder: 0
})

const form = reactive({
  name: '',
  description: '',
  urlMode: 'LIST_PAGE',
  listPageUrl: '',
  seedUrls: [],
  listPageRule: '',
  contentPageRule: '',
  fields: []
})

const listPageRule = reactive({
  containerSelector: '',
  itemUrlSelector: '',
  paginationRule: {
    type: 'INFINITE_SCROLL',
    nextPageSelector: '',
    pagePattern: ''
  }
})

const rules = {
  name: [{ required: true, message: '请输入任务名称', trigger: 'blur' }],
  urlMode: [{ required: true, message: '请选择模式', trigger: 'change' }],
  listPageUrl: [{ required: true, message: '请输入列表页URL', trigger: 'blur' }]
}

const parseSeedUrls = () => {
  form.seedUrls = seedUrlsText.value
    .split('\n')
    .map(s => s.trim())
    .filter(s => s.length > 0)
}

const addField = () => {
  form.fields.push(defaultField())
}

const removeField = (index) => {
  form.fields.splice(index, 1)
}

const startSelecting = (field) => {
  selectingField.value = field
  selectingFieldIndex.value = -1
}

const startSelectingField = (index) => {
  selectingFieldIndex.value = index
  selectingField.value = null
}

const onSelectorGenerated = ({ selector, selectorType }) => {
  if (selectingField.value === 'containerSelector') {
    listPageRule.containerSelector = selector
    selectingField.value = null
  } else if (selectingField.value === 'itemUrlSelector') {
    listPageRule.itemUrlSelector = selector
    selectingField.value = null
  } else if (selectingFieldIndex.value >= 0) {
    const field = form.fields[selectingFieldIndex.value]
    field.selector = selector
    field.selectorType = selectorType
    selectingFieldIndex.value = -1
  }
}

const buildContentPageRule = () => {
  return JSON.stringify({
    fields: form.fields.map((f, idx) => ({
      fieldName: f.fieldName,
      fieldLabel: f.fieldLabel,
      selector: f.selector,
      selectorType: f.selectorType,
      extractType: f.extractType,
      attrName: f.attrName || undefined,
      required: f.required,
      defaultValue: f.defaultValue || undefined,
      displayOrder: idx
    }))
  })
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true

  try {
    const payload = {
      name: form.name,
      description: form.description,
      urlMode: form.urlMode,
      listPageUrl: form.urlMode === 'LIST_PAGE' ? form.listPageUrl : null,
      listPageRule: form.urlMode === 'LIST_PAGE' ? JSON.stringify(listPageRule) : null,
      seedUrls: form.urlMode === 'DIRECT_URL' ? form.seedUrls : null,
      contentPageRule: buildContentPageRule(),
      fields: form.fields.filter(f => f.fieldName)
    }

    if (isEdit.value) {
      payload.id = taskId.value
      await updateTask(taskId.value, payload)
      ElMessage.success('任务已更新')
    } else {
      await createTask(payload)
      ElMessage.success('任务已创建')
    }

    router.push('/tasks')
  } catch (err) {
    ElMessage.error(err.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const loadTask = async (id) => {
  const res = await getTask(id)
  const data = res.data

  form.name = data.name
  form.description = data.description || ''
  form.urlMode = data.urlMode

  if (data.urlMode === 'LIST_PAGE') {
    form.listPageUrl = data.listPageUrl || ''
    if (data.listPageRule) {
      const parsed = JSON.parse(data.listPageRule)
      Object.assign(listPageRule, parsed)
    }
  } else {
    form.seedUrls = data.seedUrls || []
    seedUrlsText.value = (data.seedUrls || []).join('\n')
  }

  if (data.contentPageRule) {
    const parsed = JSON.parse(data.contentPageRule)
    form.fields = parsed.fields || []
  }

  if (!form.fields || form.fields.length === 0) {
    form.fields = [defaultField()]
  }
}

onMounted(() => {
  if (isEdit.value) {
    loadTask(taskId.value)
  } else {
    form.fields = [defaultField()]
  }
})
</script>

<style scoped>
.section-tip {
  background: #f0f9eb;
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 13px;
  color: #606266;
}

.section-tip p {
  margin: 0 0 8px 0;
}

.section-tip ol {
  margin: 0;
  padding-left: 20px;
}

.section-tip code {
  background: #e8f3ff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
}

.field-item {
  background: #fafafa;
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 16px;
}
</style>
```

---

## Task 8: 前后端集成

**Files:**
- Modify: `backend/src/main/resources/application.yml` (CORS 配置)
- Modify: `backend/pom.xml` (可选：添加前端构建)

- [ ] **Step 1: 检查后端 CORS 配置**

检查 `backend/src/main/java/com/example/visualspider/config/` 目录是否已有 CORS 配置类。如果不存在，需要创建：

**Create:** `backend/src/main/java/com/example/visualspider/config/WebConfig.java`

```java
package com.example.visualspider.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000", "http://localhost:8080")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
```

- [ ] **Step 2: 在 backend/pom.xml 中添加前端构建配置（可选）**

在 `</plugins>` 前添加：

```xml
<!-- Frontend build plugin (optional) -->
<plugin>
    <groupId>com.github.eirslett</groupId>
    <artifactId>frontend-maven-plugin</artifactId>
    <version>1.15.0</version>
    <configuration>
        <workingDirectory>../frontend</workingDirectory>
        <nodeVersion>v20.10.0</nodeVersion>
        <npmVersion>10.2.0</npmVersion>
    </configuration>
    <executions>
        <execution>
            <id>install node and npm</id>
            <goals>
                <goal>install-node-and-npm</goal>
            </goals>
        </execution>
        <execution>
            <id>npm install</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>install</arguments>
            </configuration>
        </execution>
        <execution>
            <id>npm build</id>
            <goals>
                <goal>npm</goal>
            </goals>
            <configuration>
                <arguments>run build</arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

- [ ] **Step 3: 构建前端**

```bash
cd frontend && npm run build
```

验证输出目录 `backend/src/main/resources/static/` 包含构建文件。

- [ ] **Step 4: 启动后端测试**

```bash
cd backend && mvn spring-boot:run
# 或
java -jar target/visual-spider-*.jar
```

访问 `http://localhost:8080` 应能看到前端页面。

---

## Task 9: 端到端测试

**验证项：**

- [ ] **Task 9.1: 任务列表页**

1. 访问 `/tasks`
2. 验证任务列表正确显示
3. 验证分页、启用/停用按钮工作正常

- [ ] **Task 9.2: 新建任务**

1. 点击"新建任务"
2. 填写任务名称、选择模式
3. 使用内嵌浏览器加载页面
4. 点击页面元素生成选择器
5. 添加字段配置
6. 保存任务

- [ ] **Task 9.3: 编辑任务**

1. 从列表页点击"配置"进入编辑页
2. 验证现有配置正确加载
3. 修改配置并保存

- [ ] **Task 9.4: 任务启用/执行**

1. 在列表页启用任务
2. 点击"运行"按钮
3. 验证任务状态变为 RUNNING

---

## 验证清单

- [ ] `frontend/` 目录结构正确
- [ ] `npm install` 成功
- [ ] `npm run dev` 开发服务器启动（端口 3000）
- [ ] `npm run build` 构建成功，文件输出到 `backend/src/main/resources/static/`
- [ ] 任务列表页正常显示
- [ ] 新建/编辑任务表单完整
- [ ] 内嵌浏览器可以加载页面
- [ ] 点击页面元素可以生成选择器
- [ ] 任务 CRUD API 调用正常
- [ ] CORS 配置正确（开发环境）

---

## 已知限制与后续优化

1. **iframe 跨域**：当前实现使用 iframe 加载页面，存在跨域限制。生产环境建议使用 Playwright 直接启动浏览器。
2. **选择器生成算法**：当前 CSS 选择器生成算法较简单，可后续升级为更智能的生成策略。
3. **代理配置**：生产环境需要后端提供页面代理接口，避免跨域问题。
