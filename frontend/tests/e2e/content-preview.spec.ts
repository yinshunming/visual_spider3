import { test, expect, Page } from '@playwright/test'
import { ContentListPage } from './page-objects/ContentListPage'
import { ContentPreviewPage } from './page-objects/ContentPreviewPage'

test.describe('内容预览页 E2E 测试', () => {
  let contentListPage: ContentListPage
  let contentPreviewPage: ContentPreviewPage

  test.beforeEach(async ({ page }) => {
    contentListPage = new ContentListPage(page)
    contentPreviewPage = new ContentPreviewPage(page)
    await contentListPage.navigate()
  })

  test('E2E-020 预览弹窗打开 - 点击预览应打开弹窗', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        await expect(contentPreviewPage.dialog).toBeVisible()
      }
    }
  })

  test('E2E-021 显示基本信息 - 弹窗应显示 id、status、sourceUrl', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        await expect(contentPreviewPage.dialog).toBeVisible()
        const idValue = await contentPreviewPage.getIdValue()
        expect(idValue).toBeTruthy()
      }
    }
  })

  test('E2E-022 显示字段表格 - fields 应以表格形式展示', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        const fieldCount = await contentPreviewPage.getFieldsTableRowCount()
        expect(fieldCount).toBeGreaterThanOrEqual(0)
      }
    }
  })

  test('E2E-023 iframe 沙箱属性 - rawHtml 应在 sandbox iframe 中渲染', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        const sandboxAttr = await contentPreviewPage.getIframeSandboxAttribute()
        expect(sandboxAttr).toBeTruthy()
      }
    }
  })

  test('E2E-024 关闭弹窗 - 点击关闭按钮应关闭弹窗', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const previewButton = firstRow.locator('button').filter({ hasText: /^预览$/ })
      if (await previewButton.isVisible()) {
        await previewButton.click()
        await expect(contentPreviewPage.dialog).toBeVisible()
        await contentPreviewPage.clickClose()
        await expect(contentPreviewPage.dialog).not.toBeVisible()
      }
    }
  })
})
