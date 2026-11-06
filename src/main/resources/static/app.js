const form = document.querySelector('#product-form');
const status = document.querySelector('#status');
const formStatus = document.querySelector('#form-status');
const tbody = document.querySelector('#products');
const refresh = document.querySelector('#refresh');
const save = document.querySelector('#save');
const cancel = document.querySelector('#cancel');
const title = document.querySelector('#form-title');
let editingId = null;
let saving = false;
const filters = document.querySelector('#filter-form');
const search = document.querySelector('#search');
const categoryFilter = document.querySelector('#category-filter');
let loadVersion = 0;
const money = new Intl.NumberFormat('kk-KZ', {style: 'currency', currency: 'KZT'});
async function request(url, options) {
  const response = await fetch(url, options);
  if (!response.ok) {
    const error = await response.json().catch(() => ({}));
    throw new Error(error.errors ? Object.values(error.errors).join('. ') : error.detail || 'Сұрау орындалмады. Қайталап көріңіз.');
  }
  return response.status === 204 ? null : response.json();
}
async function loadProducts() {
  const version = ++loadVersion;
  const searchValue = search.value.trim();
  const categoryValue = categoryFilter.value;
  refresh.disabled = true;
  status.className = '';
  status.textContent = 'Өнімдер жүктелуде…';
  try {
    const params = new URLSearchParams({search: searchValue, category: categoryValue});
    const [products, categories] = await Promise.all([
      request(`/api/products?${params}`), request('/api/products/categories')
    ]);
    // A slower previous request must not replace newer search results.
    if (version !== loadVersion) return;
    categoryFilter.replaceChildren(new Option('Барлық санаттар', ''));
    if (categoryValue && !categories.includes(categoryValue)) categories.push(categoryValue);
    for (const category of categories) categoryFilter.add(new Option(category, category));
    categoryFilter.value = categoryValue;
    tbody.replaceChildren();
    for (const product of products) {
      const row = document.createElement('tr');
      if (product.stockQuantity <= product.minimumStock) row.className = 'low';
      for (const value of [product.name, product.category || '—', money.format(product.price), product.stockQuantity, product.minimumStock]) {
        const cell = document.createElement('td');
        cell.textContent = value;
        row.append(cell);
      }
      const actions = document.createElement('td');
      actions.className = 'actions';
      const edit = document.createElement('button');
      edit.type = 'button';
      edit.textContent = 'Өңдеу';
      edit.setAttribute('aria-label', `${product.name}: өңдеу`);
      edit.addEventListener('click', () => editProduct(product));
      const remove = document.createElement('button');
      remove.type = 'button';
      remove.className = 'danger';
      remove.textContent = 'Өшіру';
      remove.setAttribute('aria-label', `${product.name}: өшіру`);
      remove.addEventListener('click', () => deleteProduct(product, remove));
      actions.append(edit, remove);
      row.append(actions);
      tbody.append(row);
    }
    const empty = document.querySelector('#empty');
    empty.hidden = products.length !== 0;
    empty.textContent = searchValue || categoryValue
      ? 'Сұрауыңызға сәйкес өнім табылмады. Іздеуді өзгертіңіз немесе сүзгілерді тазалаңыз.'
      : 'Әзірге өнім жоқ. Алғашқы өнімді қосыңыз.';
    status.textContent = `Көрсетілгені: ${products.length} өнім. Сары жол — қор азайған өнім.`;
  } catch (error) {
    if (version !== loadVersion) return;
    status.className = 'error';
    status.textContent = error.message;
  } finally { if (version === loadVersion) refresh.disabled = false; }
}
function resetEditor() {
  editingId = null;
  form.reset();
  title.textContent = 'Өнім қосу';
  save.textContent = 'Өнімді қосу';
  cancel.hidden = true;
}
function editProduct(product) {
  if (saving) return;
  editingId = product.id;
  for (const field of ['name', 'category', 'price', 'stockQuantity', 'minimumStock']) {
    form.elements.namedItem(field).value = product[field] ?? '';
  }
  title.textContent = 'Өнімді өңдеу';
  save.textContent = 'Өзгерістерді сақтау';
  cancel.hidden = false;
  formStatus.textContent = '';
  form.elements.namedItem('name').focus();
}
async function deleteProduct(product, button) {
  if (saving || !window.confirm(`«${product.name}» өнімін өшіргіңіз келе ме?`)) return;
  button.disabled = true;
  try {
    await request(`/api/products/${product.id}`, {method: 'DELETE'});
    if (editingId === product.id) resetEditor();
    formStatus.className = '';
    formStatus.textContent = 'Өнім өшірілді.';
    await loadProducts();
  } catch (error) {
    status.className = 'error';
    status.textContent = error.message;
  } finally { button.disabled = false; }
}
form.addEventListener('submit', async event => {
  event.preventDefault();
  if (saving) return;
  saving = true;
  save.disabled = true;
  cancel.disabled = true;
  formStatus.className = '';
  formStatus.textContent = 'Сақталуда…';
  const data = new FormData(form);
  const updating = editingId !== null;
  try {
    await request(updating ? `/api/products/${editingId}` : '/api/products', {
      method: updating ? 'PUT' : 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({name: data.get('name'), category: data.get('category'), price: data.get('price'), stockQuantity: Number(data.get('stockQuantity')), minimumStock: Number(data.get('minimumStock'))})
    });
    resetEditor();
    formStatus.textContent = updating ? 'Өнім жаңартылды.' : 'Өнім қосылды.';
    await loadProducts();
  } catch (error) { formStatus.className = 'error'; formStatus.textContent = error.message; }
  finally { saving = false; save.disabled = false; cancel.disabled = false; }
});
cancel.addEventListener('click', () => { resetEditor(); formStatus.textContent = ''; });
filters.addEventListener('submit', event => { event.preventDefault(); loadProducts(); });
categoryFilter.addEventListener('change', loadProducts);
document.querySelector('#clear-filters').addEventListener('click', () => {
  filters.reset();
  loadProducts();
});
refresh.addEventListener('click', loadProducts);
loadProducts();
