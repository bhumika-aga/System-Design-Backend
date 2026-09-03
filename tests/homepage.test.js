const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const root = path.resolve(__dirname, '..');
const homepage = fs.readFileSync(path.join(root, 'index.html'), 'utf8');
const enhancements = fs.readFileSync(path.join(root, 'assets/enhancements.css'), 'utf8');

assert.match(homepage, /\.btn-contribute:hover\s*\{[^}]*color:\s*#fff\s*;[^}]*\}/);
assert.doesNotMatch(enhancements, /a:hover\s*\{[^}]*color:\s*[^;}]+!important[^}]*\}/);

assert.match(
    homepage,
    /href="https:\/\/github\.com\/bhumika-aga\/System-Design-Backend"/,
);

console.log('homepage checks passed');
