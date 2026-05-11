import http from 'http'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const MIME_TYPES = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'application/javascript',
  '.css': 'text/css',
  '.json': 'application/json',
}

function serveStaticFile(res, filePath, contentType) {
  fs.readFile(filePath, (err, data) => {
    if (err) {
      res.writeHead(404, { 'Content-Type': 'text/plain' })
      res.end('Not Found')
      return
    }
    res.writeHead(200, { 'Content-Type': contentType })
    res.end(data)
  })
}

function handleRequest(req, res) {
  const url = req.url.split('?')[0]

  if (url === '/' || url === '/list.html') {
    const filePath = path.join(__dirname, 'list-page.html')
    serveStaticFile(res, filePath, MIME_TYPES['.html'])
    return
  }

  if (url === '/content-page-1.html') {
    const filePath = path.join(__dirname, 'content-page-1.html')
    serveStaticFile(res, filePath, MIME_TYPES['.html'])
    return
  }

  if (url === '/content-page-2.html') {
    const filePath = path.join(__dirname, 'content-page-2.html')
    serveStaticFile(res, filePath, MIME_TYPES['.html'])
    return
  }

  res.writeHead(404, { 'Content-Type': 'text/plain' })
  res.end('Not Found')
}

let server = null

export function startMockServer() {
  return new Promise((resolve, reject) => {
    server = http.createServer(handleRequest)
    server.listen(0, '127.0.0.1', () => {
      const address = server.address()
      const port = address.port
      console.log(`Mock server running on http://127.0.0.1:${port}`)
      resolve(port)
    })
    server.on('error', reject)
  })
}

export function stopMockServer() {
  return new Promise((resolve) => {
    if (server) {
      server.close(() => {
        console.log('Mock server stopped')
        resolve()
      })
    } else {
      resolve()
    }
  })
}

export function getMockServerUrl(port) {
  return `http://127.0.0.1:${port}`
}
