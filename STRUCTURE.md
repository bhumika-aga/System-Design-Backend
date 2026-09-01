# Chapter structure

Every chapter is one self-contained HTML file that follows the same shape. This
is the contract: match it and a chapter inherits the shared navigation, theming,
search, notes and code behaviour with no extra work.

## The shell

```html
<html>
  <head>
    <title>Caching / Chapter 09 / System Design - Backend</title>
    <meta name="description" content="Chapter 09 of System Design - Backend: ...">
    <!-- shared, in this order -->
    <link rel="stylesheet" href="../../assets/copy-button.css">
    <link rel="stylesheet" href="../../assets/code-card.css">
    <link rel="stylesheet" href="../../assets/enhancements.css">
    <link rel="stylesheet" href="../../assets/shell.css">
    <link rel="stylesheet" href="../../assets/theme.css">
    <script src="../../assets/theme.js"></script>   <!-- no defer: pre-paint -->
    <style> /* only what is unique to this chapter */ </style>
  </head>
  <body>
    <div class="bfp-shell">
      <aside class="bfp-toc"> … <a class="bfp-toc-link" href="#s1"> … </aside>
      <main class="bfp-content">
        <header> kicker · h1 · lede · meta pills </header>
        <section id="s1"> … </section>
        <footer class="bfp-footer">System Design - Backend / Chapter NN / …</footer>
      </main>
    </div>
    <nav class="chapter-nav" id="chapterNav"> prev · indicator · next </nav>
    <script>hljs.highlightAll();</script>
    <script src="../../assets/copy-button.js"></script>
    <script src="../../assets/enhancements.js"></script>
    <script src="../../assets/shell.js"></script>
  </body>
</html>
```

`assets/shell.js` supplies the drawer and scroll-spy, `assets/enhancements.js`
the dock, notes, progress bar, keyboard navigation and **code-card tabs**. None
of it needs per-chapter wiring — a chapter that adds its own copy is a bug, not
a feature.

## Rules

**Typography is not per-chapter.** Body text is 17px / 1.7 with a 16px paragraph
gap, everywhere. Do not restate it, and do not override it — two chapters once
drifted to 15.5px and read visibly smaller than the rest.

**Every TOC link must resolve.** `href="#sN"` needs a matching
`<section id="sN">`. Numbered `sN` ids are the convention; a few older chapters
use descriptive ids instead, which is fine as long as they resolve.

**One code component.** Every example uses the card from
`assets/code-card.css` — never a hand-rolled variant, and never hand-written
`<span class="kw">` highlighting. highlight.js does that:

```html
<div class="codeblock" data-cb>
  <div class="code-bar">
    <div class="dots"><i></i><i></i><i></i></div>
    <div class="code-tabs">
      <button class="on" data-lang="java">
        <span class="lang-dot lang-java"></span> Java
      </button>
      <button data-lang="py">
        <span class="lang-dot lang-py"></span> Python
      </button>
    </div>
  </div>
  <div class="code-panel on" data-panel="java">
    <pre><code class="language-java">…</code></pre>
  </div>
  <div class="code-panel" data-panel="py">
    <pre><code class="language-python">…</code></pre>
  </div>
</div>
```

Java first and selected. For one language use `class="codeblock single"` with a
`<span class="filename">` in place of the tabs. Load highlight.js in the head
and call `hljs.highlightAll()` once.

One exception: where the example _is_ the SQL and the language panels only show
how to call it — chapter 8 — the order is SQL, Java, Python, with SQL selected.
The subject of the card goes first.

A card may carry a `<span class="code-cap">` in its bar saying what the example
shows. It is hidden below 720px, where there is no room for it.

**Code lines stay at or under 84 columns.** Cards wrap rather than scroll
(`white-space: pre-wrap`), so a longer line breaks mid-token. Move a trailing
comment onto its own line rather than pushing past the budget.

**Nothing is hidden by default.** If a chapter animates content in on scroll,
gate the hiding on a `has-js` class set before first paint, so the chapter still
reads with JavaScript off.

## Extracted sources

```txt
NN-chapter-slug/code/java/    one file per example, named for the type it holds
                    python/   the Python counterpart
                    {sql,yaml,…}/
```

Java files are named after their type, as the language requires. A snippet that
is a bare method in the notes gets wrapped in the class it would really live in,
so the file is a valid compilation unit. Where two examples would collide on a
type name, give each its own package.

**A chapter contains exactly two directories: `html_notes/` and `code/`.** The
original extraction left source files in package-shaped folders at the chapter
root — `handlers/`, `tasks/`, `workflows/`, `snippets/` — and those are easy to
miss, because they sit one level deeper than a chapter's own files. Check with a
full walk, never a depth-limited one:

```bash
find NN-chapter-slug -name '*.go'                       # must be empty
ls NN-chapter-slug                                      # only code, html_notes
```

## Before calling a chapter done

```bash
node tests/homepage.test.js && node tests/dark-mode.test.js
javac -proc:none -d /tmp/out NN-*/code/java/*.java   # 0 syntax errors;
                                                     # missing Spring symbols are expected
```

Then check: every TOC anchor resolves, no `<div class="code">` or hand-written
highlight spans survive, no per-chapter tab script remains, and the chapter has
no `.go` files left.
