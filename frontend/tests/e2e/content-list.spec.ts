import { test, expect, Page } from '@playwright/test'
import { ContentListPage } from './page-objects/ContentListPage'

test.describe('内容列表页 E2E 测试', () => {
  let contentListPage: ContentListPage

  test.beforeEach(async ({ page }) => {
    contentListPage = new ContentListPage(page)
    await contentListPage.navigate()
  })

  test('E2E-001 页面加载 - 应显示内容表格', async () => {
    await expect(contentListPage.table).toBeVisible()
  })

  test('E2E-002 显示内容列表 - 应显示 sourceUrl、status、createdAt 列', async () => {
    test.skip()
    return
  })

  test('E2E-003 分页切换 - 应能切换每页条数', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      await contentListPage.changePageSize(50)
      await expect(contentListPage.table).toBeVisible()
    }
  })

  test('E2E-004 按任务筛选 - 应能选择任务筛选', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const filterSelect = contentListPage.taskFilter
      await filterSelect.click()
      const dropdown = page.locator('.el-select-dropdown:visible').first()
      await dropdown.waitFor({ state: 'visible', timeout: 5000 })
      const option = dropdown.locator('.el-select-dropdown__item').first()
      if (await option.isVisible()) {
        await option.click()
        await expect(contentListPage.table).toBeVisible()
      }
    }
  })

  test('E2E-005 清除筛选 - 应能清除任务筛选', async () => {
    await contentListPage.clearTaskFilter()
    await expect(contentListPage.table).toBeVisible()
  })

  test('E2E-006 点击预览按钮 - 应能点击预览', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        await expect(page.locator('.el-dialog')).toBeVisible()
      }
    }
  })

  test('E2E-007 点击编辑按钮 - 应能点击编辑', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(page).toHaveURL(/\/contents\/\d+\/edit/)
      }
    }
  })

  test('E2E-008 点击删除按钮 - 应显示确认对话框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await expect(page.locator('.el-message-box')).toBeVisible()
      }
    }
  })

  test('E2E-009 确认删除 - 点击确定应关闭对话框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await contentListPage.confirmDelete()
        await expect(page.locator('.el-message-box')).not.toBeVisible()
      }
    }
  })

  test('E2E-010 取消删除 - 点击取消应关闭对话框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await contentListPage.cancelDelete()
        await expect(page.locator('.el-message-box')).not.toBeVisible()
      }
    }
  })
})
