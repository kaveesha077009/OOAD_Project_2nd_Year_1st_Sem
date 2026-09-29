// Supplier CRUD uses the Spring Boot API; orders/receiving remain browser-only demos.
const API = '/api/suppliers';
const orderKey = 'agroshop_demo_orders';
let suppliers = [];
let orders = JSON.parse(localStorage.getItem(orderKey) || '[]');
const el = id => document.getElementById(id);
const saveOrders = () => localStorage.setItem(orderKey, JSON.stringify(orders));
const escapeHTML = s => String(s ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const money = n => Number(n).toLocaleString('en-LK', {minimumFractionDigits:2, maximumFractionDigits:2});
function flash(msg, error=false) {
  el('alert').innerHTML = `<div class="alert alert-${error?'danger':'success'}">${escapeHTML(msg)}</div>`;
}
async function api(path='', options={}) {
  const response = await fetch(API + path, {
    ...options,
    headers: {'Content-Type':'application/json', ...(options.headers || {})}
  });
  if (!response.ok) throw new Error(`Request failed (${response.status}). Check the Spring Boot console.`);
  return response.status === 204 || response.status === 205 ? null :
    (response.headers.get('content-type')?.includes('application/json') ? response.json() : null);
}
async function loadSuppliers() {
  try {
    suppliers = await api();
    render();
  } catch (error) {
    flash('Could not load suppliers. Start Spring Boot and check MySQL. ' + error.message, true);
  }
}
function showPage(page) {
  document.querySelectorAll('.page').forEach(x => x.classList.toggle('active', x.id===page));
  document.querySelectorAll('[data-page]').forEach(x => x.classList.toggle('active', x.dataset.page===page));
  render();
}
document.querySelectorAll('[data-page]').forEach(x => x.addEventListener('click', () => showPage(x.dataset.page)));
function showSupplierForm(id) {
  el('supplierFormWrap').classList.remove('d-none');
  el('supplierForm').reset(); el('supplierId').value = '';
  el('supplierFormTitle').textContent = 'Add Supplier';
  if (id !== undefined) {
    const s = suppliers.find(x => x.supplierId === id);
    if (!s) return;
    el('supplierFormTitle').textContent = 'Edit Supplier';
    el('supplierId').value = s.supplierId;
    el('supplierName').value = s.name ?? '';
    el('supplierPhone').value = s.phone ?? '';
    el('supplierEmail').value = s.email ?? '';
    el('supplierAddress').value = s.address ?? '';
  }
  el('supplierName').focus();
}
function hideSupplierForm() {el('supplierFormWrap').classList.add('d-none');}
el('supplierForm').addEventListener('submit', async e => {
  e.preventDefault();
  const id = el('supplierId').value;
  const payload = {
    name:el('supplierName').value.trim(), phone:el('supplierPhone').value.trim(),
    email:el('supplierEmail').value.trim(), address:el('supplierAddress').value.trim()
  };
  if (!payload.name || !payload.phone) return;
  const btn = el('supplierForm').querySelector('button[type="submit"],button:not([type])');
  btn.disabled = true;
  try {
    await api(id ? `/${id}` : '', {method:id?'PUT':'POST', body:JSON.stringify(payload)});
    hideSupplierForm(); await loadSuppliers(); flash(id?'Supplier updated in MySQL':'Supplier saved in MySQL');
  } catch (error) {flash('Could not save supplier: '+error.message, true);}
  finally {btn.disabled = false;}
});
async function deleteSupplier(id) {
  if (orders.some(o => o.supplierId === id)) {
    flash('This supplier has local demo purchase orders. Remove those first.', true); return;
  }
  if (!confirm('Delete this supplier from MySQL?')) return;
  try {await api(`/${id}`, {method:'DELETE'}); await loadSuppliers(); flash('Supplier deleted');}
  catch(error) {flash('Could not delete supplier: '+error.message, true);}
}
el('supplierSearch').addEventListener('input', render);
function showOrderForm() {
  if (!suppliers.length) {flash('Add a supplier before creating an order', true); showPage('suppliers'); return;}
  el('orderFormWrap').classList.remove('d-none'); el('orderForm').reset();
  el('orderDate').value = new Date().toLocaleDateString('en-CA'); updateTotal();
}
function updateTotal() {el('orderTotal').textContent = 'LKR '+money(Number(el('orderQuantity').value)*Number(el('orderPrice').value));}
['orderQuantity','orderPrice'].forEach(id => el(id).addEventListener('input', updateTotal));
el('orderForm').addEventListener('submit', e => {
  e.preventDefault();
  const o = {
    id:orders.reduce((m,x)=>Math.max(m,x.id),1000)+1,
    supplierId:Number(el('orderSupplier').value), date:el('orderDate').value,
    product:el('orderProduct').value.trim(), quantity:Number(el('orderQuantity').value),
    price:Number(el('orderPrice').value), status:'Pending'
  };
  if (!suppliers.some(s=>s.supplierId===o.supplierId) || !o.product || o.quantity<1 || o.price<0) return;
  orders.push(o); saveOrders(); el('orderFormWrap').classList.add('d-none'); render();
  flash('Demo purchase order created (browser storage only)');
});
function receiveOrder(id) {
  const o=orders.find(x=>x.id===id);
  if(o && o.status==='Pending' && confirm(`Mark demo order #${id} as received?`)) {
    o.status='Received'; saveOrders(); render();
    flash('Marked received in demo only; MySQL inventory was not updated');
  }
}
function render() {
  el('supplierCount').textContent=suppliers.length;
  el('orderCount').textContent=orders.length;
  el('pendingCount').textContent=orders.filter(o=>o.status==='Pending').length;
  const q=el('supplierSearch').value.toLowerCase();
  el('supplierRows').innerHTML=suppliers.filter(s=>((s.name??'')+' '+(s.phone??'')).toLowerCase().includes(q)).map(s=>
    `<tr><td>${escapeHTML(s.name)}</td><td>${escapeHTML(s.phone)}</td><td>${escapeHTML(s.email)}</td><td>${escapeHTML(s.address)}</td><td><button class="btn btn-sm btn-outline-success" onclick="showSupplierForm(${s.supplierId})">Edit</button> <button class="btn btn-sm btn-outline-danger" onclick="deleteSupplier(${s.supplierId})">Delete</button></td></tr>`
  ).join('') || '<tr><td colspan="5" class="text-secondary">No suppliers found</td></tr>';
  el('orderSupplier').innerHTML='<option value="">Select supplier</option>'+suppliers.map(s=>`<option value="${s.supplierId}">${escapeHTML(s.name)}</option>`).join('');
  el('orderRows').innerHTML=orders.map(o=>`<tr><td>#${o.id}</td><td>${escapeHTML(suppliers.find(s=>s.supplierId===o.supplierId)?.name||'Unknown')}</td><td>${escapeHTML(o.date)}</td><td>${escapeHTML(o.product)}</td><td>${o.quantity}</td><td>${money(o.quantity*o.price)}</td><td><span class="badge ${o.status==='Received'?'bg-success':'bg-warning text-dark'}">${o.status}</span></td></tr>`).join('') || '<tr><td colspan="7" class="text-secondary">No demo purchase orders yet</td></tr>';
  el('receivingRows').innerHTML=orders.map(o=>`<tr><td>#${o.id}</td><td>${escapeHTML(o.product)}</td><td>${o.quantity}</td><td>${o.status}</td><td>${o.status==='Pending'?`<button class="btn btn-sm btn-success" onclick="receiveOrder(${o.id})">Mark Received</button>`:'—'}</td></tr>`).join('') || '<tr><td colspan="5" class="text-secondary">No demo orders yet</td></tr>';
}
render(); loadSuppliers();
