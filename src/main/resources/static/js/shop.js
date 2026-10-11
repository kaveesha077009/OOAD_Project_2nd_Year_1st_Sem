/* AgroShop customer pages: shop, cart, checkout, order-success, my-orders.
   One file; each page picks its logic from <body data-page="..."> */
(function () {
    'use strict';

    // TODO (shop owner): put the real bank details here. Shown after a bank-transfer order.
    const BANK = {
        bankName: 'Your bank name',
        accountName: 'Your shop account name',
        accountNumber: '0000000000',
        branch: 'Your branch'
    };
    const LOW_STOCK = 5;

    // TODO (User Management): replace with the logged-in customer's id once login exists.
    function customerId() {
        return Number(localStorage.getItem('agroCustomerId') || 1);
    }

    /* ---------- helpers ---------- */
    const $ = (sel, root) => (root || document).querySelector(sel);

    function esc(v) {
        return String(v === null || v === undefined ? '' : v).replace(/[&<>"']/g, function (c) {
            return { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c];
        });
    }

    function money(n) {
        return 'LKR ' + Number(n || 0).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
    }

    function fmtDate(s) {
        const d = new Date(s);
        if (isNaN(d)) return '';
        return d.toLocaleString('en-GB', { day: 'numeric', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
    }

    function plural(n, word) { return n + ' ' + word + (n === 1 ? '' : 's'); }

    async function api(path, options) {
        const opts = Object.assign({ headers: { 'Content-Type': 'application/json' } }, options);
        let res;
        try {
            res = await fetch(path, opts);
        } catch (e) {
            throw new Error('Cannot reach the server. Check your connection and try again.');
        }
        const text = await res.text();
        let data = null;
        if (text) { try { data = JSON.parse(text); } catch (e) { data = null; } }
        if (!res.ok) {
            throw new Error((data && data.message) || 'Something went wrong (error ' + res.status + ').');
        }
        return data;
    }

    function toast(message, type) {
        let box = $('#toastBox');
        if (!box) {
            box = document.createElement('div');
            box.id = 'toastBox';
            box.className = 'toast-container position-fixed bottom-0 end-0 p-3';
            document.body.appendChild(box);
        }
        const el = document.createElement('div');
        el.className = 'toast align-items-center border-0 text-bg-' + (type === 'error' ? 'danger' : 'success');
        el.setAttribute('role', type === 'error' ? 'alert' : 'status');
        el.innerHTML = '<div class="d-flex"><div class="toast-body">' + esc(message) + '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div>';
        box.appendChild(el);
        el.addEventListener('hidden.bs.toast', function () { el.remove(); });
        new bootstrap.Toast(el, { delay: 3500 }).show();
    }

    function showError(container, message, retry) {
        container.innerHTML = '<div class="col-12"><div class="alert alert-danger d-flex justify-content-between align-items-center" role="alert">' +
            '<span>' + esc(message) + '</span>' +
            '<button type="button" class="btn btn-sm btn-outline-danger retry-btn">Try again</button></div></div>';
        container.querySelector('.retry-btn').addEventListener('click', retry);
    }

    function emptyState(icon, title, text, href, label) {
        return '<div class="col-12"><div class="empty-state"><i class="fa-solid ' + icon + '" aria-hidden="true"></i>' +
            '<h2>' + esc(title) + '</h2><p>' + esc(text) + '</p>' +
            '<a class="btn btn-agro" href="' + href + '">' + esc(label) + '</a></div></div>';
    }

    async function refreshBadge() {
        try {
            const cart = await api('/api/cart/' + customerId());
            const n = cart && cart.items ? cart.items.length : 0;
            const badge = $('#cartBadge');
            if (badge) {
                badge.textContent = n;
                badge.classList.toggle('d-none', n === 0);
            }
        } catch (e) { /* badge is optional */ }
    }

    const pages = {};

    /* ---------- SHOP ---------- */
    pages.shop = async function () {
        const grid = $('#productGrid');
        const search = $('#search');
        const catSel = $('#category');
        const info = $('#resultInfo');
        let products = [];

        try {
            products = await api('/api/products');
        } catch (e) {
            showError(grid, e.message, function () { location.reload(); });
            return;
        }

        const cats = Array.from(new Set(products.map(function (p) { return p.category; }).filter(Boolean))).sort();
        catSel.innerHTML = '<option value="">All categories</option>' +
            cats.map(function (c) { return '<option>' + esc(c) + '</option>'; }).join('');

        function placeholder() {
            return '<div class="product-media"><i class="fa-solid fa-box-open" aria-hidden="true"></i></div>';
        }

        function card(p) {
            const stock = p.stockQuantity;
            const known = stock !== null && stock !== undefined;
            const out = known && Number(stock) <= 0;
            const low = known && !out && Number(stock) <= LOW_STOCK;
            const media = p.imageUrl
                ? '<div class="product-media"><img class="product-img" data-fallback src="' + esc(p.imageUrl) + '" alt="' + esc(p.name) + '" loading="lazy"></div>'
                : placeholder();
            const note = out ? '<span class="stock-note stock-out">Out of stock</span>'
                : low ? '<span class="stock-note stock-low">Only ' + esc(stock) + ' left</span>'
                    : '<span class="stock-note text-muted">In stock</span>';
            const action = out
                ? '<button class="btn btn-outline-secondary w-100 mt-2" disabled>Out of stock</button>'
                : '<div class="add-row"><label class="visually-hidden" for="qty-' + p.id + '">Quantity of ' + esc(p.name) + '</label>' +
                '<input id="qty-' + p.id + '" class="form-control" type="number" min="1" step="any" value="1">' +
                '<button class="btn btn-agro add-btn" data-id="' + p.id + '">Add to cart</button></div>';
            return '<div class="col-6 col-md-4 col-lg-3"><article class="product-card">' + media +
                '<div class="product-body">' +
                (p.category ? '<span class="product-cat">' + esc(p.category) + '</span>' : '') +
                '<h2 class="product-name">' + esc(p.name) + '</h2>' +
                (p.description ? '<p class="product-desc">' + esc(p.description) + '</p>' : '') +
                '<div class="product-price">' + money(p.price) + '</div>' + note + action +
                '</div></article></div>';
        }

        function render() {
            const q = search.value.trim().toLowerCase();
            const cat = catSel.value;
            const shown = products.filter(function (p) {
                const text = ((p.name || '') + ' ' + (p.description || '')).toLowerCase();
                return (!q || text.indexOf(q) !== -1) && (!cat || p.category === cat);
            });
            info.textContent = plural(shown.length, 'product');

            if (!products.length) {
                grid.innerHTML = emptyState('fa-box-open', 'No products yet', 'The shop has not added any products. Please check back soon.', '/my-orders', 'View my orders');
                return;
            }
            if (!shown.length) {
                grid.innerHTML = '<div class="col-12"><div class="empty-state"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i>' +
                    '<h2>No products match your search</h2><p>Try a different word or clear the filters.</p>' +
                    '<button class="btn btn-agro" id="clearFilters">Clear filters</button></div></div>';
                $('#clearFilters').addEventListener('click', function () { search.value = ''; catSel.value = ''; render(); });
                return;
            }
            grid.innerHTML = shown.map(card).join('');
        }

        search.addEventListener('input', render);
        catSel.addEventListener('change', render);

        // broken image -> placeholder
        grid.addEventListener('error', function (e) {
            if (e.target.matches && e.target.matches('img[data-fallback]')) {
                e.target.parentElement.outerHTML = placeholder();
            }
        }, true);

        grid.addEventListener('click', async function (e) {
            const btn = e.target.closest('.add-btn');
            if (!btn) return;
            const id = Number(btn.dataset.id);
            const qty = Number($('#qty-' + id).value);
            if (!(qty > 0)) { toast('Enter a quantity greater than 0.', 'error'); return; }

            const label = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Adding';
            try {
                await api('/api/cart/' + customerId() + '/items', {
                    method: 'POST', body: JSON.stringify({ productId: id, quantity: qty })
                });
                toast('Added to cart');
                refreshBadge();
            } catch (err) {
                toast(err.message, 'error');
            } finally {
                btn.disabled = false;
                btn.innerHTML = label;
            }
        });

        render();
    };

    /* ---------- CART ---------- */
    pages.cart = function () {
        const body = $('#cartBody');

        async function load() {
            let cart;
            try {
                cart = await api('/api/cart/' + customerId());
            } catch (e) {
                showError(body, e.message, load);
                return;
            }
            refreshBadge();

            if (!cart.items.length) {
                body.innerHTML = emptyState('fa-cart-shopping', 'Your cart is empty', 'Add a few products and they will show up here.', '/shop', 'Browse products');
                return;
            }

            const rows = cart.items.map(function (i) {
                return '<tr>' +
                    '<td><strong>' + esc(i.productName) + '</strong></td>' +
                    '<td>' + money(i.unitPrice) + '</td>' +
                    '<td style="width:120px"><label class="visually-hidden" for="q-' + i.productId + '">Quantity of ' + esc(i.productName) + '</label>' +
                    '<input id="q-' + i.productId + '" class="form-control form-control-sm qty-input" type="number" min="0" step="any" value="' + esc(i.quantity) + '" data-id="' + i.productId + '"></td>' +
                    '<td class="text-end">' + money(i.subtotal) + '</td>' +
                    '<td class="text-end"><button type="button" class="btn btn-sm btn-outline-secondary remove-btn" data-id="' + i.productId + '" aria-label="Remove ' + esc(i.productName) + ' from cart">' +
                    '<i class="fa-solid fa-trash-can" aria-hidden="true"></i> Remove</button></td></tr>';
            }).join('');

            body.innerHTML =
                '<div class="col-lg-8"><div class="panel p-0"><div class="table-responsive"><table class="table align-middle mb-0">' +
                '<thead><tr><th>Product</th><th>Price</th><th>Quantity</th><th class="text-end">Subtotal</th><th></th></tr></thead>' +
                '<tbody>' + rows + '</tbody></table></div></div></div>' +
                '<div class="col-lg-4"><div class="panel summary"><h2 class="h5">Order summary</h2>' +
                '<div class="d-flex justify-content-between my-3"><span>' + plural(cart.items.length, 'product') + '</span><span></span></div>' +
                '<div class="d-flex justify-content-between fw-bold fs-5 border-top pt-3"><span>Total</span><span>' + money(cart.total) + '</span></div>' +
                '<a href="/checkout" class="btn btn-agro w-100 mt-3">Go to checkout</a>' +
                '<a href="/shop" class="btn btn-link w-100 mt-1">Keep shopping</a></div></div>';
        }

        body.addEventListener('change', async function (e) {
            const input = e.target.closest('.qty-input');
            if (!input) return;
            const q = Number(input.value);
            if (!(q > 0)) { load(); return; } // invalid -> restore; removing is done with the Remove button
            input.disabled = true;
            try {
                await api('/api/cart/' + customerId() + '/items/' + input.dataset.id + '?quantity=' + encodeURIComponent(q), { method: 'PUT' });
            } catch (err) {
                toast(err.message, 'error');
            }
            load();
        });

        body.addEventListener('click', async function (e) {
            const btn = e.target.closest('.remove-btn');
            if (!btn) return;
            btn.disabled = true;
            try {
                await api('/api/cart/' + customerId() + '/items/' + btn.dataset.id, { method: 'DELETE' });
                toast('Removed from cart');
            } catch (err) {
                toast(err.message, 'error');
            }
            load();
        });

        load();
    };

    /* ---------- CHECKOUT ---------- */
    pages.checkout = async function () {
        const body = $('#checkoutBody');
        let cart;
        try {
            cart = await api('/api/cart/' + customerId());
        } catch (e) {
            showError(body, e.message, function () { location.reload(); });
            return;
        }
        if (!cart.items.length) {
            body.innerHTML = emptyState('fa-cart-shopping', 'Nothing to check out', 'Your cart is empty.', '/shop', 'Browse products');
            return;
        }

        const lines = cart.items.map(function (i) {
            return '<li class="d-flex justify-content-between py-1"><span>' + esc(i.productName) + ' &times; ' + esc(i.quantity) + '</span><span>' + money(i.subtotal) + '</span></li>';
        }).join('');

        body.innerHTML =
            '<div class="col-lg-7"><div class="panel"><fieldset><legend class="h5 mb-3">How will you pay?</legend>' +
            '<label class="pay-option"><input type="radio" name="pay" value="CASH" checked><span><strong>Cash</strong>' +
            '<small>Pay in cash when you receive or collect your order.</small></span></label>' +
            '<label class="pay-option"><input type="radio" name="pay" value="BANK_TRANSFER"><span><strong>Bank transfer</strong>' +
            '<small>Transfer the total to the shop account. The bank details appear after you place the order.</small></span></label>' +
            '</fieldset><p class="small text-muted mb-0">Our staff confirm your payment before your order is prepared.</p></div></div>' +
            '<div class="col-lg-5"><div class="panel summary"><h2 class="h5">Order summary</h2>' +
            '<ul class="list-unstyled my-3">' + lines + '</ul>' +
            '<div class="d-flex justify-content-between fw-bold fs-5 border-top pt-3"><span>Total</span><span>' + money(cart.total) + '</span></div>' +
            '<div id="checkoutError" class="mt-3" role="alert"></div>' +
            '<button id="placeOrder" type="button" class="btn btn-agro btn-lg w-100 mt-3">Place order</button>' +
            '<a href="/cart" class="btn btn-link w-100 mt-1">Back to cart</a></div></div>';

        const btn = $('#placeOrder');
        btn.addEventListener('click', async function () {
            const method = $('input[name="pay"]:checked').value;
            const err = $('#checkoutError');
            err.innerHTML = '';
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm" aria-hidden="true"></span> Placing order';
            try {
                const res = await api('/api/orders/checkout', {
                    method: 'POST', body: JSON.stringify({ customerId: customerId(), paymentMethod: method })
                });
                sessionStorage.setItem('agroLastOrder', JSON.stringify(res));
                location.href = '/order-success';
            } catch (e) {
                err.innerHTML = '<div class="alert alert-danger mb-0">' + esc(e.message) + ' <a href="/cart" class="alert-link">Review your cart</a></div>';
                btn.disabled = false;
                btn.textContent = 'Place order';
            }
        });
    };

    /* ---------- ORDER SUCCESS ---------- */
    pages.success = function () {
        const body = $('#successBody');
        let data = null;
        try { data = JSON.parse(sessionStorage.getItem('agroLastOrder')); } catch (e) { data = null; }

        if (!data || !data.order) {
            body.innerHTML = '<div class="row">' + emptyState('fa-receipt', 'No recent order found', 'You can see everything you have placed under My orders.', '/my-orders', 'View my orders') + '</div>';
            return;
        }

        const o = data.order;
        const p = data.payment || {};
        const bank = p.paymentMethod === 'BANK_TRANSFER';
        const ref = 'Order #' + o.id;

        const next = bank
            ? '<p>Transfer <strong>' + money(o.totalAmount) + '</strong> to the account below and use <strong>' + esc(ref) + '</strong> as the payment reference. Staff will confirm once they see your transfer.</p>' +
            '<dl class="bank-box row mb-3">' +
            '<dt class="col-sm-4">Bank</dt><dd class="col-sm-8">' + esc(BANK.bankName) + '</dd>' +
            '<dt class="col-sm-4">Account name</dt><dd class="col-sm-8">' + esc(BANK.accountName) + '</dd>' +
            '<dt class="col-sm-4">Account number</dt><dd class="col-sm-8">' + esc(BANK.accountNumber) + '</dd>' +
            '<dt class="col-sm-4">Branch</dt><dd class="col-sm-8 mb-0">' + esc(BANK.branch) + '</dd></dl>' +
            '<button type="button" id="copyRef" class="btn btn-outline-secondary btn-sm mb-3">Copy payment reference</button>'
            : '<p>Pay <strong>' + money(o.totalAmount) + '</strong> in cash when you receive or collect your order. Staff will confirm your payment and start preparing it.</p>';

        body.innerHTML =
            '<div class="panel text-center"><span class="success-mark" aria-hidden="true"><i class="fa-solid fa-check"></i></span>' +
            '<h1 class="page-title mt-3">Order #' + esc(o.id) + ' placed</h1>' +
            '<p class="page-sub">Total ' + money(o.totalAmount) + ' &middot; ' + (bank ? 'Bank transfer' : 'Cash') + '</p>' +
            '<div class="text-start">' + next + '</div>' +
            '<div class="d-grid gap-2 d-sm-flex justify-content-sm-center mt-2">' +
            '<a class="btn btn-agro" href="/my-orders">Track my order</a>' +
            '<a class="btn btn-outline-secondary" href="/invoice?id=' + encodeURIComponent(o.id) + '">View invoice</a>' +
            '<a class="btn btn-link" href="/shop">Keep shopping</a></div></div>';

        const copy = $('#copyRef');
        if (copy) {
            copy.addEventListener('click', function () {
                if (navigator.clipboard) {
                    navigator.clipboard.writeText(ref).then(function () { toast('Payment reference copied'); });
                } else {
                    toast('Copy this reference: ' + ref);
                }
            });
        }
    };

    /* ---------- MY ORDERS ---------- */
    pages.orders = async function () {
        const box = $('#ordersBody');
        let orders, products;
        try {
            const result = await Promise.all([
                api('/api/orders/customer/' + customerId()),
                api('/api/products').catch(function () { return []; })
            ]);
            orders = result[0];
            products = result[1];
        } catch (e) {
            showError(box, e.message, function () { location.reload(); });
            return;
        }

        if (!orders.length) {
            box.innerHTML = '<div class="row">' + emptyState('fa-receipt', 'No orders yet', 'When you place an order it will appear here.', '/shop', 'Browse products') + '</div>';
            return;
        }

        const names = {};
        products.forEach(function (p) { names[p.id] = p.name; });
        const payments = await Promise.all(orders.map(function (o) {
            return api('/api/payments/order/' + o.id).catch(function () { return null; });
        }));

        const STATUS = {
            PENDING:    { label: 'Order placed',          cls: 'warning',   step: 1 },
            PROCESSING: { label: 'Preparing your order',  cls: 'info',      step: 2 },
            COMPLETED:  { label: 'Completed',             cls: 'success',   step: 3 },
            CANCELLED:  { label: 'Cancelled',             cls: 'secondary', step: 0 }
        };
        const STEPS = ['Order placed', 'Payment confirmed', 'Completed'];

        function payText(p) {
            if (!p) return 'No payment recorded yet.';
            const m = p.paymentMethod === 'BANK_TRANSFER' ? 'Bank transfer' : (p.paymentMethod === 'CASH' ? 'Cash' : esc(p.paymentMethod));
            if (p.status === 'COMPLETED') return m + ': payment confirmed.';
            if (p.status === 'FAILED') return m + ': payment could not be confirmed. Please contact the shop.';
            return m + ': waiting for staff to confirm your payment.';
        }

        box.innerHTML = orders.map(function (o, idx) {
            const s = STATUS[o.status] || { label: o.status || 'Unknown', cls: 'secondary', step: 0 };
            const stepper = s.step > 0
                ? '<ol class="stepper" aria-label="Order progress">' + STEPS.map(function (label, i) {
                return '<li class="' + (i < s.step ? 'done' : '') + '">' + label + '</li>';
            }).join('') + '</ol>'
                : '<p class="text-muted small my-2">This order was cancelled.</p>';
            return '<article class="order-card">' +
                '<div class="d-flex flex-wrap justify-content-between align-items-start gap-2">' +
                '<div><h2 class="h5 mb-0">Order #' + esc(o.id) + '</h2><span class="text-muted small">' + esc(fmtDate(o.orderDate)) + '</span></div>' +
                '<div class="text-end"><div class="fw-bold">' + money(o.totalAmount) + '</div><span class="badge text-bg-' + s.cls + '">' + esc(s.label) + '</span></div></div>' +
                stepper +
                '<p class="small mb-2">' + payText(payments[idx]) + '</p>' +
                '<div class="d-flex gap-2 flex-wrap">' +
                '<button type="button" class="btn btn-sm btn-outline-secondary items-btn" data-id="' + o.id + '" aria-expanded="false">View items</button>' +
                '<a class="btn btn-sm btn-outline-secondary" href="/invoice?id=' + encodeURIComponent(o.id) + '">Invoice</a></div>' +
                '<div id="items-' + o.id + '" class="d-none mt-3"></div></article>';
        }).join('');

        function itemsTable(items) {
            if (!items.length) return '<p class="text-muted small mb-0">No items found for this order.</p>';
            return '<div class="table-responsive"><table class="table table-sm mb-0"><thead><tr><th>Product</th><th>Quantity</th><th class="text-end">Price</th><th class="text-end">Subtotal</th></tr></thead><tbody>' +
                items.map(function (i) {
                    return '<tr><td>' + esc(names[i.productId] || ('Product #' + i.productId)) + '</td><td>' + esc(i.quantity) + '</td><td class="text-end">' + money(i.unitPrice) + '</td><td class="text-end">' + money(i.subtotal) + '</td></tr>';
                }).join('') + '</tbody></table></div>';
        }

        box.addEventListener('click', async function (e) {
            const btn = e.target.closest('.items-btn');
            if (!btn) return;
            const id = btn.dataset.id;
            const panel = $('#items-' + id);
            if (!panel.classList.contains('d-none')) {
                panel.classList.add('d-none');
                btn.setAttribute('aria-expanded', 'false');
                btn.textContent = 'View items';
                return;
            }
            panel.classList.remove('d-none');
            btn.setAttribute('aria-expanded', 'true');
            btn.textContent = 'Hide items';
            if (panel.dataset.loaded) return;
            panel.textContent = 'Loading items...';
            try {
                panel.innerHTML = itemsTable(await api('/api/orders/' + id + '/items'));
                panel.dataset.loaded = '1';
            } catch (err) {
                panel.innerHTML = '<span class="text-danger small">' + esc(err.message) + '</span>';
            }
        });
    };

    document.addEventListener('DOMContentLoaded', function () {
        refreshBadge();
        const run = pages[document.body.dataset.page];
        if (run) run();
    });
})();
