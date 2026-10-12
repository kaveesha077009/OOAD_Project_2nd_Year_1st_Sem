// Shared helpers for all pages. Text is always set with textContent, so user input can never run as HTML.

function h(tag, attrs, ...children) {
  const el = document.createElement(tag);
  for (const [k, v] of Object.entries(attrs || {})) {
    if (k.startsWith('on')) el.addEventListener(k.slice(2), v);
    else if (k === 'class') el.className = v;
    else if (v !== false && v != null) el.setAttribute(k, v);
  }
  for (const c of children.flat()) {
    if (c != null) el.append(c.nodeType ? c : document.createTextNode(c));
  }
  return el;
}

async function api(method, url, body) {
  let res;
  try {
    res = await fetch(url, {
      method,
      headers: body ? { 'Content-Type': 'application/json' } : {},
      body: body ? JSON.stringify(body) : undefined,
    });
  } catch (e) {
    throw new Error('Cannot reach the server. Is the application running?');
  }
  if (res.status === 401) { location.href = 'login.html'; throw new Error('Please sign in'); }
  if (res.status === 403) throw new Error('Your account is not allowed to do this');
  if (res.status === 204) return null;
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.message || 'Something went wrong (' + res.status + ')');
  return data;
}

let toastTimer;
function toast(message, isError) {
  const el = document.getElementById('toast');
  el.textContent = message;
  el.className = 'show' + (isError ? ' error' : '');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => (el.className = ''), 3500);
}

const num = (v) => Number(v).toLocaleString('en-LK', { maximumFractionDigits: 2 });
const money = (v) => 'Rs. ' + Number(v).toLocaleString('en-LK', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

async function getMe() {
  try {
    const res = await fetch('/api/auth/me');
    return res.ok ? await res.json() : null;
  } catch (e) { return null; }
}

const NAV = {
  ADMIN: [['products.html', 'Products and stock'], ['categories.html', 'Categories'], ['low-stock.html', 'Low stock'], ['sale.html', 'New sale'], ['orders.html', 'Orders']],
  EMPLOYEE: [['products.html', 'Products and stock'], ['low-stock.html', 'Low stock'], ['sale.html', 'New sale'], ['orders.html', 'Orders']],
  CUSTOMER: [['shop.html', 'Shop'], ['orders.html', 'My orders']],
  NONE: [['shop.html', 'Shop']],
};

/** Builds the sidebar for the signed-in role. Returns the user (or null when signed out). */
async function renderLayout(active, title, subtitle, opts) {
  document.getElementById('page-head').append(h('h1', {}, title), h('p', { class: 'sub' }, subtitle));
  document.body.append(h('div', { id: 'toast', role: 'status', 'aria-live': 'polite' }));
  const me = await getMe();
  if (!me && !(opts && opts.public)) { location.href = 'login.html'; return null; }
  const links = NAV[me ? me.role : 'NONE'];
  const foot = me
    ? h('div', { class: 'side-user' }, h('div', {}, me.displayName), h('small', {}, me.role.toLowerCase()),
        h('button', { class: 'ghost small', onclick: async () => { await fetch('/api/auth/logout', { method: 'POST' }); location.href = 'login.html'; } }, 'Sign out'))
    : h('div', { class: 'side-user' }, h('a', { href: 'login.html' }, 'Sign in'));
  document.body.prepend(h('nav', { class: 'side', 'aria-label': 'Main' },
    h('div', { class: 'brand' }, 'AgroShop', h('small', {}, me && me.role !== 'CUSTOMER' ? 'Staff panel' : 'Farm and hardware supplies')),
    links.map(([href, label]) => h('a', { href, class: href === active ? 'active' : '', 'aria-current': href === active ? 'page' : false }, label)),
    foot));
  return me;
}

/** Run an async action, show its error as a toast. */
async function run(fn) {
  try { return await fn(); } catch (e) { toast(e.message, true); }
}
