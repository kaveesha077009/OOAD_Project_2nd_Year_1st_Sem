// Supplier & Purchasing Management - AgroShop
const API = "/api";

let suppliers = [];
let products = [];

// Display success or error messages
function showMessage(message, isError = false) {
    const box = document.getElementById("messageBox");

    box.textContent = message;
    box.className = `alert ${isError ? "alert-danger" : "alert-success"}`;
}

// Show a helpful error from the API
async function getError(response) {
    const text = await response.text();

    try {
        const data = JSON.parse(text);
        return data.message || data.detail || data.error || `HTTP ${response.status}`;
    } catch {
        return text || `HTTP ${response.status}`;
    }
}

// Retrieve all suppliers
async function loadSuppliers() {
    try {
        const response = await fetch(`${API}/suppliers`);

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        suppliers = await response.json();
        renderSuppliers();

    } catch (error) {
        showMessage("Could not load suppliers: " + error.message, true);
    }
}

// Display suppliers in the table
function renderSuppliers() {
    const table = document.getElementById("supplierTable");
    table.replaceChildren();

    if (suppliers.length === 0) {
        const row = table.insertRow();
        const cell = row.insertCell();
        cell.colSpan = 7;
        cell.textContent = "No suppliers found.";
        cell.className = "text-center text-muted";
        return;
    }

    suppliers.forEach(supplier => {
        const row = table.insertRow();

        [
            supplier.id,
            supplier.name,
            supplier.phone,
            supplier.email || "-",
            supplier.address || "-",
            supplier.status ? "Active" : "Inactive"
        ].forEach(value => {
            row.insertCell().textContent = value;
        });

        const actions = row.insertCell();

        const editButton = document.createElement("button");
        editButton.className = "btn btn-sm btn-outline-primary me-2";
        editButton.textContent = "Edit";
        editButton.addEventListener("click", () => editSupplier(supplier.id));

        const deleteButton = document.createElement("button");
        deleteButton.className = "btn btn-sm btn-outline-danger";
        deleteButton.textContent = "Delete";
        deleteButton.addEventListener("click", () => deleteSupplier(supplier.id));

        actions.append(editButton, deleteButton);
    });
}

// Fill the form with an existing supplier
function editSupplier(id) {
    const supplier = suppliers.find(s => s.id === id);
    if (!supplier) return;

    document.getElementById("supplierId").value = supplier.id;
    document.getElementById("supplierName").value = supplier.name;
    document.getElementById("supplierPhone").value = supplier.phone;
    document.getElementById("supplierEmail").value = supplier.email || "";
    document.getElementById("supplierAddress").value = supplier.address || "";
    document.getElementById("supplierStatus").value = String(supplier.status);

    document.getElementById("supplierForm").scrollIntoView({
        behavior: "smooth"
    });
}

// Reset supplier form
function clearSupplierForm() {
    document.getElementById("supplierForm").reset();
    document.getElementById("supplierId").value = "";
}

// Create or update supplier
document.getElementById("supplierForm").addEventListener("submit", async event => {
    event.preventDefault();

    const id = document.getElementById("supplierId").value;

    const supplier = {
        name: document.getElementById("supplierName").value.trim(),
        phone: document.getElementById("supplierPhone").value.trim(),
        email: document.getElementById("supplierEmail").value.trim(),
        address: document.getElementById("supplierAddress").value.trim(),
        status: document.getElementById("supplierStatus").value === "true"
    };

    try {
        const response = await fetch(
            id ? `${API}/suppliers/${id}` : `${API}/suppliers`,
            {
                method: id ? "PUT" : "POST",
                headers: {"Content-Type": "application/json"},
                body: JSON.stringify(supplier)
            }
        );

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        clearSupplierForm();
        await loadSuppliers();

        showMessage(id ? "Supplier updated successfully." :
                         "Supplier added successfully.");

    } catch (error) {
        showMessage(error.message, true);
    }
});

// Delete supplier
async function deleteSupplier(id) {
    if (!confirm("Are you sure you want to delete this supplier?")) {
        return;
    }

    try {
        const response = await fetch(`${API}/suppliers/${id}`, {
            method: "DELETE"
        });

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        await loadSuppliers();
        showMessage("Supplier deleted successfully.");

    } catch (error) {
        showMessage(error.message, true);
    }
}

document.getElementById("clearSupplierBtn")
    .addEventListener("click", clearSupplierForm);

// Load suppliers when the page opens
loadSuppliers();

// Load products for purchase orders
async function loadProducts() {
    try {
        const response = await fetch(`${API}/products`);
        if (!response.ok) throw new Error(await getError(response));

        products = await response.json();
    } catch (error) {
        showMessage("Could not load products: " + error.message, true);
    }
}

// Add a product row to the purchase order
function addPurchaseItem() {
    if (products.length === 0) {
        showMessage("No products available. Add products first.", true);
        return;
    }

    const row = document.createElement("tr");

    const productCell = row.insertCell();
    const productSelect = document.createElement("select");
    productSelect.className = "form-select product-select";
    productSelect.required = true;

    products.forEach(product => {
        const option = document.createElement("option");
        option.value = product.id;
        option.textContent = product.name;
        productSelect.appendChild(option);
    });
    productCell.appendChild(productSelect);

    const quantityCell = row.insertCell();
    const quantityInput = document.createElement("input");
    quantityInput.type = "number";
    quantityInput.min = "1";
    quantityInput.step = "1";
    quantityInput.value = "1";
    quantityInput.required = true;
    quantityInput.className = "form-control quantity-input";
    quantityCell.appendChild(quantityInput);

    const priceCell = row.insertCell();
    const priceInput = document.createElement("input");
    priceInput.type = "number";
    priceInput.min = "0";
    priceInput.step = "0.01";
    priceInput.value = "0";
    priceInput.required = true;
    priceInput.className = "form-control price-input";
    priceCell.appendChild(priceInput);

    const subtotalCell = row.insertCell();
    subtotalCell.className = "subtotal";
    subtotalCell.textContent = "0.00";

    const actionCell = row.insertCell();
    const removeButton = document.createElement("button");
    removeButton.type = "button";
    removeButton.className = "btn btn-sm btn-outline-danger";
    removeButton.textContent = "Remove";
    removeButton.addEventListener("click", () => {
        row.remove();
        calculatePurchaseTotal();
    });
    actionCell.appendChild(removeButton);

    quantityInput.addEventListener("input", calculatePurchaseTotal);
    priceInput.addEventListener("input", calculatePurchaseTotal);

    document.getElementById("purchaseItems").appendChild(row);
    calculatePurchaseTotal();
}

// Calculate order total
function calculatePurchaseTotal() {
    let total = 0;

    document.querySelectorAll("#purchaseItems tr").forEach(row => {
        const quantity = Number(row.querySelector(".quantity-input").value);
        const price = Number(row.querySelector(".price-input").value);

        const subtotal = quantity * price;
        row.querySelector(".subtotal").textContent = subtotal.toFixed(2);
        total += subtotal;
    });

    document.getElementById("purchaseTotal").textContent = total.toFixed(2);
}

// Update the supplier dropdown
function populatePurchaseSuppliers() {
    const select = document.getElementById("purchaseSupplier");
    select.replaceChildren();

    suppliers.filter(s => s.status).forEach(supplier => {
        const option = document.createElement("option");
        option.value = supplier.id;
        option.textContent = supplier.name;
        select.appendChild(option);
    });
}

// Create purchase order
document.getElementById("purchaseForm").addEventListener("submit", async event => {
    event.preventDefault();

    const rows = [...document.querySelectorAll("#purchaseItems tr")];

    if (rows.length === 0) {
        showMessage("Please add at least one product.", true);
        return;
    }

    const items = rows.map(row => ({
        productId: Number(row.querySelector(".product-select").value),
        quantity: Number(row.querySelector(".quantity-input").value),
        unitPrice: Number(row.querySelector(".price-input").value)
    }));

    if (items.some(item =>
        !Number.isInteger(item.quantity) || item.quantity <= 0 ||
        !Number.isFinite(item.unitPrice) || item.unitPrice < 0)) {
        showMessage("Enter valid quantities and purchasing prices.", true);
        return;
    }

    const payload = {
        supplierId: Number(document.getElementById("purchaseSupplier").value),
        items
    };

    try {
        const response = await fetch(`${API}/purchase-orders`, {
            method: "POST",
            headers: {"Content-Type": "application/json"},
            body: JSON.stringify(payload)
        });

        if (!response.ok) throw new Error(await getError(response));

        const order = await response.json();

        document.getElementById("purchaseItems").replaceChildren();
        addPurchaseItem();

        showMessage(`Purchase order #${order.id} created successfully.`);

    } catch (error) {
        showMessage("Could not create purchase order: " + error.message, true);
    }
});

// Initialize purchasing controls
document.getElementById("addItemBtn")
    .addEventListener("click", addPurchaseItem);

async function initializePurchasing() {
    await loadSuppliers();
    await loadProducts();
    populatePurchaseSuppliers();
    addPurchaseItem();
}

initializePurchasing();

// Load all purchase orders
async function loadPurchaseOrders() {
    try {
        const response = await fetch(`${API}/purchase-orders`);

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        const orders = await response.json();
        const table = document.getElementById("ordersTable");
        table.replaceChildren();

        if (orders.length === 0) {
            const row = table.insertRow();
            const cell = row.insertCell();
            cell.colSpan = 6;
            cell.textContent = "No purchase orders found.";
            return;
        }

        orders.forEach(order => {
            const row = table.insertRow();

            const supplier = suppliers.find(
                s => s.id === order.supplierId
            );

            const values = [
                order.id,
                supplier ? supplier.name : `Supplier #${order.supplierId}`,
                new Date(order.orderDate).toLocaleString(),
                Number(order.totalAmount).toFixed(2),
                order.status
            ];

            values.forEach(value => {
                row.insertCell().textContent = value;
            });

            const actions = row.insertCell();

            const viewButton = document.createElement("button");
            viewButton.textContent = "View Items";
            viewButton.className = "btn btn-sm btn-outline-primary me-2";
            viewButton.addEventListener("click", () => viewOrderItems(order.id));
            actions.appendChild(viewButton);

            if (order.status === "PENDING") {
                const receiveButton = document.createElement("button");
                receiveButton.textContent = "Mark Received";
                receiveButton.className = "btn btn-sm btn-success";
                receiveButton.addEventListener("click", () => receiveOrder(order.id));
                actions.appendChild(receiveButton);
            }
        });

    } catch (error) {
        showMessage("Could not load purchase orders: " + error.message, true);
    }
}

// Display products belonging to an order
async function viewOrderItems(orderId) {
    try {
        const response = await fetch(`${API}/purchase-orders/${orderId}/items`);

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        const items = await response.json();

        const details = items.map(item => {
            const product = products.find(p => p.id === item.productId);
            const name = product ? product.name : `Product #${item.productId}`;
            const subtotal = item.quantity * item.unitPrice;

            return `${name} | Qty: ${item.quantity} | Rs. ${subtotal.toFixed(2)}`;
        });

        alert(
            `Purchase Order #${orderId}\n\n` +
            (details.join("\n") || "No items found.")
        );

    } catch (error) {
        showMessage(error.message, true);
    }
}

// Mark a pending purchase order as received
async function receiveOrder(orderId) {
    if (!confirm(`Mark Purchase Order #${orderId} as received?`)) {
        return;
    }

    try {
        const response = await fetch(
            `${API}/purchase-orders/${orderId}/receive`,
            { method: "PATCH" }
        );

        if (!response.ok) {
            throw new Error(await getError(response));
        }

        await loadPurchaseOrders();
        showMessage(`Purchase Order #${orderId} marked as received.`);

    } catch (error) {
        showMessage(error.message, true);
    }
}

// Refresh history when the Purchase Orders tab opens
document.querySelector('[data-bs-target="#ordersTab"]')
    .addEventListener("shown.bs.tab", loadPurchaseOrders);

// Refresh supplier options when purchasing tab opens
document.querySelector('[data-bs-target="#purchasingTab"]')
    .addEventListener("shown.bs.tab", async () => {
        await loadSuppliers();
        populatePurchaseSuppliers();
    });
