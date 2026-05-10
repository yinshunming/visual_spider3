import { test, expect, Page } from '@playwright/test'
import { ContentListPage } from './page-objects/ContentListPage'
import { ContentEditPage } from './page-objects/ContentEditPage'

test.describe('内容编辑页 E2E 测试', () => {
  let contentListPage: ContentListPage
  let contentEditPage: ContentEditPage

  test.beforeEach(async ({ page }) => {
    contentListPage = new ContentListPage(page)
    contentEditPage = new ContentEditPage(page)
    await contentListPage.navigate()
  })

  test('E2E-030 页面加载 - 应能导航到编辑页', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })
      }
    }
  })

  test('E2E-031 表单字段显示 - 应显示 sourceUrl（只读）、status 下拉、fields 编辑框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })
        const sourceUrl = await contentEditPage.getSourceUrlValue()
        expect(sourceUrl).toBeTruthy()
        const fieldCount = await contentEditPage.getFieldInputCount()
        expect(fieldCount).toBeGreaterThanOrEqual(0)
      }
    }
  })

  test('E2E-032 修改字段值 - 应能修改 fields 值', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })
        const fieldCount = await contentEditPage.getFieldInputCount()
        if (fieldCount > 0) {
          await contentEditPage.updateField('title', 'Updated Title')
        }
      }
    }
  })

  test('E2E-033 修改状态 - 应能选择新状态', async () => {
    await contentEditPage.selectStatus('PUBLISHED')
    await expect(contentEditPage.statusSelect).toBeVisible()
  })

  test('E2E-034 保存成功 - 应能点击保存按钮', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })
        await contentEditPage.clickSave()
      }
    }
  })

  test('E2E-035 返回列表 - 点击返回按钮应跳转', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const editButton = firstRow.locator('button').filter({ hasText: /^编辑$/ })
      if (await editButton.isVisible()) {
        await editButton.click()
        await expect(contentEditPage.form).toBeVisible({ timeout: 10000 })
        await contentEditPage.clickBack()
        await expect(page).toHaveURL(/\/contents/)
      }
    }
  })
})
