
const API = '/api/suppliers';
const ORDER_API = '/api/purchase-orders';

let suppliers = [];
let orders = [];

const el = id => document.getElementById(id);

const escapeHTML = value =>
    String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;'
    }[char]));

const money = value =>
    Number(value).toLocaleString('en-LK', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    });

// Display success and error messages
function flash(message, error = false) {
    el('alert').innerHTML = `
        <div class="alert alert-${error ? 'danger' : 'success'}">
            ${escapeHTML(message)}
        </div>
    `;
}

// General API helper
async function request(baseUrl, path = '', options = {}) {
    const response = await fetch(baseUrl + path, {
        ...options,
        headers: {
            'Content-Type': 'application/json',
            ...(options.headers || {})
        }
    });

    if (!response.ok) {
        let details = '';

        try {
            const body = await response.json();
            details = body.message || body.error || '';
        } catch (_) {
            // Some error responses do not contain JSON.
        }

        throw new Error(
            `HTTP ${response.status}${details ? ': ' + details : ''}`
        );
    }

    if (response.status === 204 || response.status === 205) {
        return null;
    }

    const contentType = response.headers.get('content-type') || '';

    return contentType.includes('application/json')
        ? response.json()
        : null;
}

function api(path = '', options = {}) {
    return request(API, path, options);
}

function orderApi(path = '', options = {}) {
    return request(ORDER_API, path, options);
}

// --------------------------------------------------
// NAVIGATION
// --------------------------------------------------

function showPage(page) {
    document.querySelectorAll('.page').forEach(section => {
        section.classList.toggle('active', section.id === page);
    });

    document.querySelectorAll('[data-page]').forEach(button => {
        button.classList.toggle(
            'active',
            button.dataset.page === page
        );
    });

    render();
}

document.querySelectorAll('[data-page]').forEach(button => {
    button.addEventListener('click', () => {
        showPage(button.dataset.page);
    });
});

// --------------------------------------------------
// SUPPLIER MANAGEMENT
// --------------------------------------------------

async function loadSuppliers() {
    try {
        suppliers = await api();
        render();
    } catch (error) {
        flash(
            'Could not load suppliers: ' + error.message,
            true
        );
    }
}

function showSupplierForm(id) {
    el('supplierFormWrap').classList.remove('d-none');
    el('supplierForm').reset();

    el('supplierId').value = '';
    el('supplierFormTitle').textContent = 'Add Supplier';

    if (id !== undefined) {
        const supplier = suppliers.find(
            item => item.supplierId === id
        );

        if (!supplier) {
            flash('Supplier not found.', true);
            hideSupplierForm();
            return;
        }

        el('supplierFormTitle').textContent = 'Edit Supplier';

        el('supplierId').value = supplier.supplierId;
        el('supplierName').value = supplier.name ?? '';
        el('supplierPhone').value = supplier.phone ?? '';
        el('supplierEmail').value = supplier.email ?? '';
        el('supplierAddress').value = supplier.address ?? '';
    }

    el('supplierName').focus();
}

function hideSupplierForm() {
    el('supplierFormWrap').classList.add('d-none');
}

el('supplierForm').addEventListener('submit', async event => {
    event.preventDefault();

    const id = el('supplierId').value;

    const payload = {
        name: el('supplierName').value.trim(),
        phone: el('supplierPhone').value.trim(),
        email: el('supplierEmail').value.trim(),
        address: el('supplierAddress').value.trim()
    };

    if (!payload.name || !payload.phone) {
        flash('Supplier name and phone are required.', true);
        return;
    }

    const saveButton = el('supplierForm').querySelector(
        'button[type="submit"], button:not([type])'
    );

    saveButton.disabled = true;

    try {
        await api(id ? `/${id}` : '', {
            method: id ? 'PUT' : 'POST',
            body: JSON.stringify(payload)
        });

        hideSupplierForm();
        await loadSuppliers();

        flash(
            id
                ? 'Supplier updated successfully.'
                : 'Supplier saved successfully.'
        );
    } catch (error) {
        flash(
            'Could not save supplier: ' + error.message,
            true
        );
    } finally {
        saveButton.disabled = false;
    }
});

async function deleteSupplier(id) {
    // Prevent deleting suppliers referenced by purchase orders.
    if (orders.some(order => order.supplier?.supplierId === id)) {
        flash(
            'This supplier has purchase orders and cannot be deleted.',
            true
        );
        return;
    }

    if (!confirm('Are you sure you want to delete this supplier?')) {
        return;
    }

    try {
        await api(`/${id}`, {
            method: 'DELETE'
        });

        await loadSuppliers();
        flash('Supplier deleted successfully.');
    } catch (error) {
        flash(
            'Could not delete supplier: ' + error.message,
            true
        );
    }
}

el('supplierSearch').addEventListener('input', render);

// --------------------------------------------------
// PURCHASE ORDER MANAGEMENT
// --------------------------------------------------

async function loadOrders() {
    try {
        orders = await orderApi();
        render();
    } catch (error) {
        flash(
            'Could not load purchase orders: ' + error.message,
            true
        );
    }
}

function showOrderForm() {
    if (suppliers.length === 0) {
        flash(
            'Please add a supplier before creating a purchase order.',
            true
        );

        showPage('suppliers');
        return;
    }

    el('orderFormWrap').classList.remove('d-none');
    el('orderForm').reset();

    // Use the local date instead of UTC.
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0');
    const day = String(today.getDate()).padStart(2, '0');

    el('orderDate').value = `${year}-${month}-${day}`;

    updateTotal();
}

function updateTotal() {
    const quantity = Number(el('orderQuantity').value) || 0;
    const price = Number(el('orderPrice').value) || 0;

    el('orderTotal').textContent =
        'LKR ' + money(quantity * price);
}

['orderQuantity', 'orderPrice'].forEach(id => {
    el(id).addEventListener('input', updateTotal);
});

el('orderForm').addEventListener('submit', async event => {
    event.preventDefault();

    const payload = {
        supplierId: Number(el('orderSupplier').value),
        date: el('orderDate').value,
        product: el('orderProduct').value.trim(),
        quantity: Number(el('orderQuantity').value),
        price: Number(el('orderPrice').value)
    };

    if (
        !suppliers.some(
            supplier => supplier.supplierId === payload.supplierId
        ) ||
        !payload.date ||
        !payload.product ||
        !Number.isInteger(payload.quantity) ||
        payload.quantity < 1 ||
        !Number.isFinite(payload.price) ||
        payload.price < 0
    ) {
        flash('Please enter valid purchase order details.', true);
        return;
    }

    const saveButton = el('orderForm').querySelector(
        'button[type="submit"], button:not([type])'
    );

    saveButton.disabled = true;

    try {
        await orderApi('', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        el('orderFormWrap').classList.add('d-none');

        await loadOrders();

        flash('Purchase order saved successfully in MySQL.');
    } catch (error) {
        flash(
            'Could not create purchase order: ' + error.message,
            true
        );
    } finally {
        saveButton.disabled = false;
    }
});

// --------------------------------------------------
// RECEIVE STOCK
// --------------------------------------------------

async function receiveOrder(id) {
    const order = orders.find(item => item.id === id);

    if (!order || order.status !== 'Pending') {
        return;
    }

    if (!confirm(`Mark purchase order #${id} as received?`)) {
        return;
    }

    try {
        await orderApi(`/${id}/receive`, {
            method: 'PATCH'
        });

        await loadOrders();

        flash(
            'Purchase order marked as received. ' +
            'Product inventory is not updated yet.'
        );
    } catch (error) {
        flash(
            'Could not receive purchase order: ' + error.message,
            true
        );
    }
}

// --------------------------------------------------
// RENDER DASHBOARD AND TABLES
// --------------------------------------------------

function render() {
    // Dashboard statistics
    el('supplierCount').textContent = suppliers.length;

    el('orderCount').textContent = orders.length;

    el('pendingCount').textContent = orders.filter(
        order => order.status === 'Pending'
    ).length;

    // Supplier search
    const search = el('supplierSearch').value
        .trim()
        .toLowerCase();

    const filteredSuppliers = suppliers.filter(supplier => {
        const searchableText = [
            supplier.name,
            supplier.phone,
            supplier.email,
            supplier.address
        ].join(' ').toLowerCase();

        return searchableText.includes(search);
    });

    // Supplier table
    el('supplierRows').innerHTML = filteredSuppliers.map(
        supplier => `
            <tr>
                <td>${escapeHTML(supplier.name)}</td>
                <td>${escapeHTML(supplier.phone)}</td>
                <td>${escapeHTML(supplier.email)}</td>
                <td>${escapeHTML(supplier.address)}</td>
                <td>
                    <button
                        class="btn btn-sm btn-outline-success"
                        onclick="showSupplierForm(${supplier.supplierId})"
                    >
                        Edit
                    </button>

                    <button
                        class="btn btn-sm btn-outline-danger"
                        onclick="deleteSupplier(${supplier.supplierId})"
                    >
                        Delete
                    </button>
                </td>
            </tr>
        `
    ).join('') || `
        <tr>
            <td colspan="5" class="text-secondary">
                No suppliers found
            </td>
        </tr>
    `;

    // Supplier dropdown in purchase order form
    const previousSelection = el('orderSupplier').value;

    el('orderSupplier').innerHTML = `
        <option value="">Select supplier</option>
        ${suppliers.map(supplier => `
            <option value="${supplier.supplierId}">
                ${escapeHTML(supplier.name)}
            </option>
        `).join('')}
    `;

    // Preserve selection while the form is open.
    if (
        suppliers.some(
            supplier => String(supplier.supplierId) === previousSelection
        )
    ) {
        el('orderSupplier').value = previousSelection;
    }

    // Purchase order table
    el('orderRows').innerHTML = orders.map(order => {
        const total = Number(order.quantity) * Number(order.price);

        const statusClass = order.status === 'Received'
            ? 'bg-success'
            : 'bg-warning text-dark';

        return `
            <tr>
                <td>#${order.id}</td>
                <td>${escapeHTML(order.supplier?.name || 'Unknown')}</td>
                <td>${escapeHTML(order.orderDate)}</td>
                <td>${escapeHTML(order.product)}</td>
                <td>${order.quantity}</td>
                <td>${money(total)}</td>
                <td>
                    <span class="badge ${statusClass}">
                        ${escapeHTML(order.status)}
                    </span>
                </td>
            </tr>
        `;
    }).join('') || `
        <tr>
            <td colspan="7" class="text-secondary">
                No purchase orders yet
            </td>
        </tr>
    `;

    // Receive Stock table
    el('receivingRows').innerHTML = orders.map(order => `
        <tr>
            <td>#${order.id}</td>
            <td>${escapeHTML(order.product)}</td>
            <td>${order.quantity}</td>
            <td>${escapeHTML(order.status)}</td>
            <td>
                ${
                    order.status === 'Pending'
                        ? `
                            <button
                                class="btn btn-sm btn-success"
                                onclick="receiveOrder(${order.id})"
                            >
                                Mark Received
                            </button>
                        `
                        : '—'
                }
            </td>
        </tr>
    `).join('') || `
        <tr>
            <td colspan="5" class="text-secondary">
                No purchase orders yet
            </td>
        </tr>
    `;
}

// --------------------------------------------------
// INITIALIZE APPLICATION
// --------------------------------------------------

render();

Promise.all([
    loadSuppliers(),
    loadOrders()
]);
