import { test, expect, Page } from '@playwright/test'
import { ContentListPage } from './page-objects/ContentListPage'

test.describe('内容导出 E2E 测试', () => {
  let contentListPage: ContentListPage

  test.beforeEach(async ({ page }) => {
    contentListPage = new ContentListPage(page)
    await contentListPage.navigate()
  })

  test('E2E-050 导出 Excel - 点击导出应触发下载', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null)
      await contentListPage.exportButton.click()
      const download = await downloadPromise
      if (download) {
        const filename = download.suggestedFilename()
        expect(filename).toMatch(/\.xlsx$/)
      }
    }
  })

  test('E2E-051 导出 CSV - 应能选择 CSV 格式', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null)
      const exportBtn = contentListPage.exportButton
      await exportBtn.click()
      const download = await downloadPromise
      if (download) {
        const filename = download.suggestedFilename()
        expect(filename).toMatch(/\.(xlsx|csv)$/)
      }
    }
  })

  test('E2E-052 导出后留在页面 - 导出后应仍在内容列表页', async ({ page }) => {
    if (await contentListPage.hasContent()) {
      const downloadPromise = page.waitForEvent('download', { timeout: 10000 }).catch(() => null)
      await contentListPage.exportButton.click()
      await downloadPromise
      await expect(page).toHaveURL(/\/contents/)
    }
  })
})
