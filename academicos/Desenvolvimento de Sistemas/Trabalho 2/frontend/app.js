'use strict';

const $ = (selector) => document.querySelector(selector);
const state = { products: [], categories: [], view: 'produtos', productId: null, categoryId: null, deletion: null, loading: false };
const money = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
const integer = new Intl.NumberFormat('pt-BR');
let toastTimeout;

class ApiError extends Error {
  constructor(message, fields = {}) { super(message); this.fields = fields; }
}

async function api(path, options = {}) {
  let response;
  try {
    response = await fetch(`/api/${path}`, {
      ...options,
      headers: { Accept: 'application/json', ...(options.body ? { 'Content-Type': 'application/json' } : {}) },
      signal: AbortSignal.timeout(15000),
    });
  } catch {
    throw new ApiError('Não foi possível acessar o servidor. Verifique se a aplicação está em execução e tente novamente.');
  }
  if (response.status === 204) return null;
  const body = await response.json().catch(() => null);
  if (!response.ok) throw new ApiError(body?.detail || `Não foi possível concluir a operação (HTTP ${response.status}).`, body?.campos);
  return body;
}

function icon(name) {
  const svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
  const use = document.createElementNS('http://www.w3.org/2000/svg', 'use');
  use.setAttribute('href', `#${name}`);
  svg.setAttribute('aria-hidden', 'true');
  svg.append(use);
  return svg;
}

function node(tag, className, text) {
  const element = document.createElement(tag);
  if (className) element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function notice(id, message) {
  const element = $(id);
  element.textContent = message;
  element.hidden = !message;
}

function toast(message) {
  clearTimeout(toastTimeout);
  $('#toast').textContent = message;
  $('#toast').hidden = false;
  toastTimeout = setTimeout(() => { $('#toast').hidden = true; }, 5000);
}

function populateCategories() {
  for (const [selector, placeholder] of [['#category-filter', 'Todas as categorias'], ['#product-category', 'Selecione uma categoria']]) {
    const select = $(selector);
    const current = select.value;
    select.replaceChildren(new Option(placeholder, ''));
    for (const category of state.categories) select.add(new Option(category.nome, String(category.id)));
    select.value = state.categories.some((category) => String(category.id) === current) ? current : '';
  }
}

async function reload() {
  if (state.loading) return;
  state.loading = true;
  $('#reload').disabled = true;
  $('#products-view').setAttribute('aria-busy', 'true');
  notice('#page-error', '');
  try {
    const [products, categories] = await Promise.all([api('produtos'), api('categorias')]);
    state.products = products;
    state.categories = categories;
    populateCategories();
    render();
  } catch (error) {
    notice('#page-error', error.message);
    $('#results-count').textContent = 'Não foi possível atualizar os dados. Tente novamente em Atualizar.';
  } finally {
    state.loading = false;
    $('#reload').disabled = false;
    $('#products-view').setAttribute('aria-busy', 'false');
  }
}

function stockStatus(quantity) {
  if (quantity === 0) return ['empty', 'Sem estoque'];
  if (quantity <= 5) return ['low', 'Estoque baixo'];
  return ['', 'Disponível'];
}

function actionButton(label, symbol, handler, danger = false) {
  const button = node('button', `icon-button${danger ? ' danger' : ''}`);
  button.type = 'button';
  button.setAttribute('aria-label', label);
  button.title = label;
  button.append(icon(symbol));
  button.addEventListener('click', handler);
  return button;
}

function renderProducts() {
  const search = $('#search').value.trim().toLocaleLowerCase('pt-BR');
  const category = $('#category-filter').value;
  const stock = $('#stock-filter').value;
  const products = state.products.filter((product) => product.nome.toLocaleLowerCase('pt-BR').includes(search)
    && (!category || String(product.categoria.id) === category)
    && (!stock || (stock === 'low' ? product.quantidadeEstoque <= 5 : product.quantidadeEstoque === 0)));
  const rows = products.map((product) => {
    const row = node('tr');
    const productCell = node('td');
    const content = node('div', 'product-cell');
    const initials = product.nome.split(/\s+/).slice(0, 2).map((word) => [...word][0]).join('').toLocaleUpperCase('pt-BR');
    content.append(node('span', 'product-avatar', initials));
    const info = node('div', 'product-info');
    info.append(node('strong', '', product.nome), node('span', '', `#${String(product.id).padStart(4, '0')}`));
    content.append(info);
    productCell.append(content);
    const categoryCell = node('td');
    categoryCell.append(node('span', 'category-pill', product.categoria.nome));
    const statusCell = node('td');
    const [statusClass, statusLabel] = stockStatus(product.quantidadeEstoque);
    statusCell.append(node('span', `stock-badge ${statusClass}`, statusLabel));
    const actionsCell = node('td');
    const actions = node('div', 'row-actions');
    actions.append(actionButton(`Editar ${product.nome}`, 'edit', () => openProduct(product)),
      actionButton(`Excluir ${product.nome}`, 'trash', () => openDelete('produtos', product), true));
    actionsCell.append(actions);
    row.append(productCell, categoryCell, node('td', 'number price', money.format(product.preco)),
      node('td', 'number', integer.format(product.quantidadeEstoque)), statusCell, actionsCell);
    return row;
  });
  $('#products-body').replaceChildren(...rows);
  const filtered = Boolean(search || category || stock);
  $('#products-empty').hidden = products.length > 0;
  $('#empty-title').textContent = filtered ? 'Nenhum produto encontrado' : 'Seu estoque começa aqui';
  $('#empty-description').textContent = filtered ? 'Tente outro nome ou ajuste os filtros para encontrar o que precisa.'
    : state.categories.length ? 'Adicione seu primeiro produto e acompanhe tudo por aqui.' : 'Cadastre uma categoria e adicione seu primeiro produto.';
  $('#empty-create').hidden = filtered;
  $('#clear-filters').hidden = !filtered;
  $('#product-count').textContent = integer.format(state.products.length);
  $('#results-count').textContent = `${integer.format(products.length)} de ${integer.format(state.products.length)} produto(s)`;
}

function renderCategories() {
  const cards = state.categories.map((category) => {
    const card = node('article', 'category-card');
    const top = node('div', 'category-card-top');
    const actions = node('div', 'row-actions');
    actions.append(actionButton(`Editar categoria ${category.nome}`, 'edit', () => openCategory(category)),
      actionButton(`Excluir categoria ${category.nome}`, 'trash', () => openDelete('categorias', category), true));
    top.append(icon('tag'), actions);
    const count = state.products.filter((product) => product.categoria.id === category.id).length;
    card.append(top, node('h3', '', category.nome), node('p', '', `${count} produto${count === 1 ? '' : 's'} vinculado${count === 1 ? '' : 's'}`));
    return card;
  });
  $('#categories-grid').replaceChildren(...cards);
  $('#categories-empty').hidden = cards.length > 0;
}

function render() {
  $('#nav-product-count').textContent = integer.format(state.products.length);
  $('#nav-category-count').textContent = integer.format(state.categories.length);
  $('#metric-products').textContent = integer.format(state.products.length);
  $('#metric-categories').textContent = `${state.categories.length} categoria${state.categories.length === 1 ? '' : 's'} no catálogo`;
  $('#metric-units').textContent = integer.format(state.products.reduce((sum, product) => sum + product.quantidadeEstoque, 0));
  const low = state.products.filter((product) => product.quantidadeEstoque <= 5).length;
  $('#metric-low').textContent = low ? `${low} produto(s) com estoque baixo ou zerado` : 'Nenhum produto com estoque baixo';
  const cents = state.products.reduce((sum, product) => sum + BigInt(Math.round(product.preco * 100)) * BigInt(product.quantidadeEstoque), 0n);
  // O cálculo em centavos evita acumular imprecisão nas multiplicações monetárias.
  $('#metric-value').textContent = money.format(cents / 100n).replace(/,\d{2}$/, `,${String(cents % 100n).padStart(2, '0')}`);
  renderProducts();
  renderCategories();
}

function setView(view) {
  state.view = view;
  const products = view === 'produtos';
  $('#products-view').hidden = !products;
  $('#categories-view').hidden = products;
  $('#breadcrumb').textContent = products ? 'Produtos' : 'Categorias';
  $('#page-title').textContent = products ? 'Seus produtos' : 'Suas categorias';
  $('#page-subtitle').textContent = products ? 'Uma visão clara de tudo o que você tem.' : 'Cada produto na categoria certa.';
  $('#create-main span').textContent = products ? 'Novo produto' : 'Nova categoria';
  for (const button of document.querySelectorAll('[data-view]')) {
    button.classList.toggle('active', button.dataset.view === view);
    if (button.dataset.view === view) button.setAttribute('aria-current', 'page');
    else button.removeAttribute('aria-current');
  }
}

function clearErrors(form, errorSelector) {
  notice(errorSelector, '');
  for (const error of form.querySelectorAll('.field-error')) error.textContent = '';
  for (const input of form.querySelectorAll('[aria-invalid]')) input.removeAttribute('aria-invalid');
}

function showErrors(form, errorSelector, error) {
  notice(errorSelector, error.message);
  for (const [name, message] of Object.entries(error.fields || {})) {
    const input = form.elements.namedItem(name);
    if (!input) continue;
    input.setAttribute('aria-invalid', 'true');
    const description = document.getElementById(input.getAttribute('aria-describedby'));
    if (description) description.textContent = message;
  }
  form.querySelector('[aria-invalid=true]')?.focus();
}

function openProduct(product = null) {
  state.productId = product?.id ?? null;
  $('#product-form').reset();
  clearErrors($('#product-form'), '#product-error');
  $('#product-dialog-title').textContent = product ? 'Editar produto' : 'Novo produto';
  $('#save-product').textContent = product ? 'Salvar alterações' : 'Cadastrar produto';
  if (product) {
    $('#product-name').value = product.nome;
    $('#product-price').value = product.preco;
    $('#product-quantity').value = product.quantidadeEstoque;
    $('#product-category').value = String(product.categoria.id);
  }
  $('#product-dialog').showModal();
  $('#product-name').focus();
}

function openCategory(category = null) {
  state.categoryId = category?.id ?? null;
  $('#category-form').reset();
  clearErrors($('#category-form'), '#category-error');
  $('#category-name').value = category?.nome ?? '';
  $('#category-dialog-title').textContent = category ? 'Editar categoria' : 'Nova categoria';
  $('#save-category').textContent = category ? 'Salvar alterações' : 'Criar categoria';
  $('#category-dialog').showModal();
  $('#category-name').focus();
}

function openDelete(resource, item) {
  state.deletion = { resource, item };
  $('#delete-title').textContent = resource === 'produtos' ? 'Excluir produto?' : 'Excluir categoria?';
  $('#delete-description').textContent = `Você está prestes a excluir “${item.nome}”.`;
  notice('#delete-error', '');
  $('#delete-dialog').showModal();
}

async function busy(form, operation) {
  if (form.dataset.busy) return;
  form.dataset.busy = 'true';
  const buttons = [...form.querySelectorAll('button')];
  buttons.forEach((button) => { button.disabled = true; });
  try { await operation(); }
  finally { delete form.dataset.busy; buttons.forEach((button) => { button.disabled = false; }); }
}

$('#product-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  clearErrors(form, '#product-error');
  if (!form.reportValidity()) return;
  const body = { nome: $('#product-name').value.trim(), preco: Number($('#product-price').value),
    quantidadeEstoque: Number($('#product-quantity').value), categoriaId: Number($('#product-category').value) };
  busy(form, async () => {
    try {
      const editing = state.productId !== null;
      await api(`produtos${editing ? `/${state.productId}` : ''}`, { method: editing ? 'PUT' : 'POST', body: JSON.stringify(body) });
      $('#product-dialog').close();
      toast(editing ? 'Produto atualizado com sucesso.' : 'Produto cadastrado com sucesso.');
      await reload();
    } catch (error) { showErrors(form, '#product-error', error); }
  });
});

$('#category-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  clearErrors(form, '#category-error');
  if (!form.reportValidity()) return;
  busy(form, async () => {
    try {
      const editing = state.categoryId !== null;
      const saved = await api(`categorias${editing ? `/${state.categoryId}` : ''}`, {
        method: editing ? 'PUT' : 'POST', body: JSON.stringify({ nome: $('#category-name').value.trim() }),
      });
      $('#category-dialog').close();
      toast(editing ? 'Categoria atualizada com sucesso.' : 'Categoria criada com sucesso.');
      await reload();
      if ($('#product-dialog').open && !editing) $('#product-category').value = String(saved.id);
    } catch (error) { showErrors(form, '#category-error', error); }
  });
});

$('#delete-form').addEventListener('submit', (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  if (!state.deletion) return;
  busy(form, async () => {
    try {
      await api(`${state.deletion.resource}/${state.deletion.item.id}`, { method: 'DELETE' });
      $('#delete-dialog').close();
      toast('Registro excluído com sucesso.');
      await reload();
    } catch (error) { notice('#delete-error', error.message); }
  });
});

for (const button of document.querySelectorAll('[data-close]')) button.addEventListener('click', () => document.getElementById(button.dataset.close).close());
for (const button of document.querySelectorAll('[data-view]')) button.addEventListener('click', () => setView(button.dataset.view));
for (const dialog of document.querySelectorAll('dialog')) dialog.addEventListener('cancel', (event) => {
  if (dialog.querySelector('form')?.dataset.busy) event.preventDefault();
});
$('#create-main').addEventListener('click', () => state.view === 'produtos' ? openProduct() : openCategory());
$('#empty-create').addEventListener('click', () => openProduct());
$('#quick-category').addEventListener('click', () => openCategory());
$('#empty-category').addEventListener('click', () => openCategory());
$('#reload').addEventListener('click', reload);
$('#search').addEventListener('input', renderProducts);
$('#category-filter').addEventListener('change', renderProducts);
$('#stock-filter').addEventListener('change', renderProducts);
$('#clear-filters').addEventListener('click', () => {
  $('#search').value = '';
  $('#category-filter').value = '';
  $('#stock-filter').value = '';
  renderProducts();
});
reload();
