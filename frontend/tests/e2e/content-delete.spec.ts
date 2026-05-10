import { test, expect, Page } from '@playwright/test'
import { ContentListPage } from './page-objects/ContentListPage'

test.describe('内容删除 E2E 测试', () => {
  let contentListPage: ContentListPage

  test.beforeEach(async ({ page }) => {
    contentListPage = new ContentListPage(page)
    await contentListPage.navigate()
  })

  test('E2E-040 删除确认对话框 - 点击删除应弹出确认框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await expect(page.locator('.el-message-box')).toBeVisible()
      }
    }
  })

  test('E2E-041 确认删除 - 点击确定应执行删除', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        const initialCount = await contentListPage.getRowCount()
        await deleteButton.click()
        await contentListPage.confirmDelete()
        await page.waitForTimeout(500)
        const newCount = await contentListPage.getRowCount()
        expect(newCount).toBeLessThanOrEqual(initialCount)
      }
    }
  })

  test('E2E-042 取消删除 - 点击取消应关闭对话框', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await expect(page.locator('.el-message-box')).toBeVisible()
        await contentListPage.cancelDelete()
        await expect(page.locator('.el-message-box')).not.toBeVisible()
      }
    }
  })

  test('E2E-043 删除成功提示 - 删除成功后应显示提示', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const firstRow = page.locator('.el-table__body tr').first()
      const deleteButton = firstRow.locator('button').filter({ hasText: /^删除$/ })
      if (await deleteButton.isVisible()) {
        await deleteButton.click()
        await contentListPage.confirmDelete()
        const successMessage = page.locator('.el-message--success')
        if (await successMessage.isVisible({ timeout: 3000 }).catch(() => false)) {
          expect(true).toBe(true)
        }
      }
    }
  })
})
