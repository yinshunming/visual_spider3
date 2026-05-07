<template>
  <div class="task-config">
    <el-card>
      <template #header>
        <span>{{ isEdit ? '编辑任务' : '新建任务' }}</span>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
        <!-- 基本信息 -->
        <el-divider content-position="left">基本信息</el-divider>

        <el-form-item label="任务名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入任务名称" />
        </el-form-item>

        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="可选" />
        </el-form-item>

        <!-- URL 模式 -->
        <el-divider content-position="left">URL 配置</el-divider>

        <el-form-item label="模式" prop="urlMode">
          <el-radio-group v-model="form.urlMode">
            <el-radio value="LIST_PAGE">列表页模式</el-radio>
            <el-radio value="DIRECT_URL">直接URL模式</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="列表页URL" v-if="form.urlMode === 'LIST_PAGE'" prop="listPageUrl">
          <el-input v-model="form.listPageUrl" placeholder="https://example.com/articles" />
        </el-form-item>

        <el-form-item label="种子URL" v-if="form.urlMode === 'DIRECT_URL'">
          <el-input v-model="seedUrlsText" type="textarea" :rows="4" placeholder="每行一个URL" @blur="parseSeedUrls" />
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
            <el-input v-model="listPageRule.containerSelector" placeholder="点击容器元素后自动生成" readonly>
              <template #append>
                <el-button @click="startSelecting('containerSelector')" :type="selectingField === 'containerSelector' ? 'success' : 'default'">
                  {{ selectingField === 'containerSelector' ? '选择中...' : '选取' }}
                </el-button>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item label="链接选择器">
            <el-input v-model="listPageRule.itemUrlSelector" placeholder="点击链接元素后自动生成" readonly>
              <template #append>
                <el-button @click="startSelecting('itemUrlSelector')" :type="selectingField === 'itemUrlSelector' ? 'success' : 'default'">
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

          <el-form-item label="下一页选择器" v-if="listPageRule.paginationRule.type === 'NEXT_BUTTON'">
            <el-input v-model="listPageRule.paginationRule.nextPageSelector" placeholder="点击下一页按钮元素" />
          </el-form-item>

          <el-form-item label="页码URL模板" v-if="listPageRule.paginationRule.type === 'PAGE_NUMBER'">
            <el-input v-model="listPageRule.paginationRule.pagePattern" placeholder="/page/{page}" />
          </el-form-item>
        </template>

        <!-- 内容页字段配置 -->
        <el-divider content-position="left">内容页字段配置</el-divider>

        <div class="section-tip">
          <p>添加需要提取的字段。点击"加载页面"进入内容页后，点击页面元素选择器。</p>
        </div>

        <div v-for="(field, index) in form.fields" :key="index" class="field-item">
          <el-form-item label="字段名称">
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
            <el-input v-model="field.selector" placeholder="点击选择器后自动生成" readonly>
              <template #append>
                <el-button @click="startSelectingField(index)" :type="selectingFieldIndex === index ? 'success' : 'default'">
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
          <el-form-item label="属性名" v-if="field.extractType === 'attr'">
            <el-input v-model="field.attrName" placeholder="如: href, src" />
          </el-form-item>
          <el-form-item label="默认值">
            <el-input v-model="field.defaultValue" placeholder="可选" />
          </el-form-item>
          <el-form-item label="必填">
            <el-switch v-model="field.required" />
          </el-form-item>

          <el-button type="danger" size="small" @click="removeField(index)" style="margin-bottom: 16px;">删除字段</el-button>
          <el-divider />
        </div>

        <el-button type="primary" plain @click="addField">+ 添加字段</el-button>

        <!-- 内嵌浏览器 -->
        <el-divider content-position="left" style="margin-top: 24px;">内嵌浏览器</el-divider>

        <EmbeddedBrowser ref="embeddedBrowser" @selector-generated="onSelectorGenerated" style="margin-bottom: 24px;" />

        <!-- 提交 -->
        <el-form-item style="margin-top: 24px;">
          <el-button type="primary" @click="handleSubmit" :loading="saving">{{ isEdit ? '保存修改' : '创建任务' }}</el-button>
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
const embeddedBrowser = ref(null)
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
  form.seedUrls = seedUrlsText.value.split('\n').map(s => s.trim()).filter(s => s.length > 0)
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
.section-tip p { margin: 0 0 8px 0; }
.section-tip ol { margin: 0; padding-left: 20px; }
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
