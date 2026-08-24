import { describe, expect, it } from 'vitest'
import { sanitizeHtml } from './sanitizeHtml'

describe('sanitizeHtml', () => {
  it('removes script tags and inline event handlers while keeping rich text html', () => {
    const raw = `
      <div class="wrap" style="color: red">
        <h1 onclick="alert('xss')">Title</h1>
        <p>Safe text</p>
        <strong>Bold</strong>
        <script>alert('bad')</script>
      </div>
    `

    const safe = sanitizeHtml(raw)

    expect(safe).toContain('<div class="wrap" style="color: red">')
    expect(safe).toContain('<h1>Title</h1>')
    expect(safe).toContain('<strong>Bold</strong>')
    expect(safe).not.toContain('onclick=')
    expect(safe).not.toContain('<script>')
  })

  it('removes javascript urls and dangerous tags with html profile', () => {
    const raw = `
      <a href="javascript:alert(1)">link</a>
      <img src="javascript:alert(2)" />
      <iframe src="https://example.org"></iframe>
      <object data="https://example.org/test"></object>
    `
    const safe = sanitizeHtml(raw)

    expect(safe).toContain('<a>link</a>')
    expect(safe).toContain('<img>')
    expect(safe).not.toContain('javascript:')
    expect(safe).not.toContain('<iframe')
    expect(safe).not.toContain('<object')
  })
})
