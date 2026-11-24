// Executar com a API iniciada em um banco de testes vazio. Veja README.md.
const { chromium } = require('../.tools/browser-qa/node_modules/playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');

const baseURL = process.env.TEST_BASE_URL || 'http://localhost:18082';
const checks = [];
const evidence = path.join(__dirname, '..', 'evidencias');
fs.mkdirSync(evidence, { recursive: true });

(async () => {
  const browser = await chromium.launch({
    executablePath: process.env.CHROME_EXECUTABLE || 'C:/Program Files/Google/Chrome/Application/chrome.exe',
    headless: true,
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 1000 }, locale: 'pt-BR' });
  const errors = [];
  page.on('pageerror', (error) => errors.push(error.message));
  const visibleText = async (selector, text) => page.locator(selector).filter({ hasText: text }).waitFor({ state: 'visible' });
  const clickSave = async (selector, method, route) => {
    const response = page.waitForResponse((r) => r.request().method() === method && r.url().endsWith(route));
    await page.locator(selector).click();
    return (await response).status();
  };

  try {
    await page.goto(baseURL);
    await visibleText('#results-count', '0 de 0');
    const ids = await page.locator('[id]').evaluateAll((elements) => elements.map((element) => element.id));
    assert.equal(new Set(ids).size, ids.length);
    checks.push('Identificadores HTML unicos, incluindo icones e campos de formulario.');
    await page.locator('#products-empty').waitFor({ state: 'visible' });
    checks.push('Estado inicial vazio e carregamento real pela API.');

    await page.locator('#create-main').click();
    await page.locator('#save-product').click();
    assert.equal(await page.locator('#product-dialog').evaluate((d) => d.open), true);
    assert.equal(await page.locator('#product-name').evaluate((input) => input.validity.valueMissing), true);
    await page.locator('#quick-category').click();
    await page.locator('#category-name').fill('Informática');
    assert.equal(await clickSave('#save-category', 'POST', '/api/categorias'), 201);
    await page.locator('#category-dialog').waitFor({ state: 'hidden' });
    await page.waitForFunction(() => document.querySelector('#product-category').value !== '');
    checks.push('Criacao de categoria pela GUI e preenchimento do select via HTTP.');

    await page.locator('#product-name').fill('   ');
    await page.locator('#product-price').fill('49.90');
    await page.locator('#product-quantity').fill('4');
    assert.equal(await clickSave('#save-product', 'POST', '/api/produtos'), 400);
    await visibleText('#error-nome', 'Informe o nome');
    assert.equal(await page.locator('#product-name').getAttribute('aria-invalid'), 'true');
    checks.push('Erro 400 real do backend exibido no formulario, com destaque do campo invalido.');

    await page.locator('#product-name').fill('Mouse sem fio');
    assert.equal(await clickSave('#save-product', 'POST', '/api/produtos'), 201);
    await visibleText('#products-body tr', 'Mouse sem fio');
    assert.match(await page.locator('#products-body').textContent(), /Informática/);
    await visibleText('#metric-value', '199,60');
    checks.push('Cadastro, listagem com JOIN e resumo monetario calculado a partir dos dados reais.');

    await page.locator('#search').fill('nao existe');
    await visibleText('#empty-title', 'Nenhum produto');
    await page.locator('#clear-filters').click();
    await visibleText('#products-body tr', 'Mouse sem fio');
    await page.locator('#stock-filter').selectOption('empty');
    await visibleText('#empty-title', 'Nenhum produto');
    await page.locator('#stock-filter').selectOption('');
    checks.push('Busca, limpeza de filtros e filtro por situacao do estoque.');

    await page.getByRole('button', { name: 'Editar Mouse sem fio', exact: true }).click();
    await page.locator('#product-name').fill('Mouse atualizado');
    await page.locator('#product-price').fill('79.90');
    await page.locator('#product-quantity').fill('10');
    const updateResponse = page.waitForResponse((r) => r.request().method() === 'PUT' && r.url().includes('/api/produtos/'));
    await page.locator('#save-product').click();
    assert.equal((await updateResponse).status(), 200);
    await visibleText('#products-body tr', 'Mouse atualizado');
    await visibleText('#metric-value', '799,00');
    await visibleText('#products-body .stock-badge', 'Disponível');
    await page.reload();
    await visibleText('#products-body tr', 'Mouse atualizado');
    checks.push('Edicao por PUT, atualizacao de quantidades e recarga da pagina preservando o cadastro.');

    await page.locator('[data-view="categorias"]').click();
    await visibleText('#categories-grid', 'Informática');
    await page.locator('#create-main').click();
    await page.locator('#category-name').fill(' informática ');
    assert.equal(await clickSave('#save-category', 'POST', '/api/categorias'), 409);
    await visibleText('#category-error', 'Já existe');
    await page.locator('#category-dialog [data-close]').first().click();
    checks.push('Conflito 409 por categoria duplicada apresentado na GUI.');

    await page.getByRole('button', { name: 'Excluir categoria Informática', exact: true }).click();
    const conflict = page.waitForResponse((r) => r.request().method() === 'DELETE' && r.url().includes('/api/categorias/'));
    await page.locator('#confirm-delete').click();
    assert.equal((await conflict).status(), 409);
    await visibleText('#delete-error', 'possui produtos');
    await page.locator('#delete-dialog [data-close]').first().click();
    checks.push('Exclusao de categoria vinculada bloqueada, com mensagem amigavel e tela funcional.');

    await page.getByRole('button', { name: 'Editar categoria Informática', exact: true }).click();
    await page.locator('#category-name').fill('Tecnologia');
    const categoryUpdate = page.waitForResponse((r) => r.request().method() === 'PUT' && r.url().includes('/api/categorias/'));
    await page.locator('#save-category').click();
    assert.equal((await categoryUpdate).status(), 200);
    await visibleText('#categories-grid', 'Tecnologia');
    await page.locator('[data-view="produtos"]').click();
    await visibleText('#products-body tr', 'Tecnologia');
    checks.push('Edicao de categoria refletida automaticamente nos produtos relacionados.');

    await page.locator('#create-main').click();
    await page.locator('#product-name').fill('<img src=x onerror=alert(1)>');
    await page.locator('#product-price').fill('15.50');
    await page.locator('#product-quantity').fill('0');
    await page.locator('#product-category').selectOption({ label: 'Tecnologia' });
    assert.equal(await clickSave('#save-product', 'POST', '/api/produtos'), 201);
    await visibleText('#products-body tr', '<img src=x');
    assert.equal(await page.locator('#products-body img').count(), 0);
    checks.push('Nomes renderizados como texto, sem interpretar HTML recebido da API.');

    // Complementa a captura com produtos inseridos pela API no banco isolado de teste.
    const categories = await (await page.request.get(`${baseURL}/api/categorias`)).json();
    for (const item of [
      { nome: 'Teclado mecânico', preco: 229.90, quantidadeEstoque: 12 },
      { nome: 'Cabo USB-C', preco: 29.90, quantidadeEstoque: 3 },
      { nome: 'Suporte para notebook', preco: 89.90, quantidadeEstoque: 8 },
    ]) {
      const response = await page.request.post(`${baseURL}/api/produtos`, { data: { ...item, categoriaId: categories[0].id } });
      assert.equal(response.status(), 201);
    }
    // Remove o nome de teste de HTML antes das imagens de demonstracao.
    const products = await (await page.request.get(`${baseURL}/api/produtos`)).json();
    const malicious = products.find((p) => p.nome.startsWith('<img'));
    await page.request.delete(`${baseURL}/api/produtos/${malicious.id}`);
    await page.locator('#reload').click();
    await visibleText('#results-count', '4 de 4');
    await page.locator('#toast').waitFor({ state: 'hidden' });
    await page.screenshot({ path: path.join(evidence, 'interface-desktop.png'), fullPage: true });
    await page.setViewportSize({ width: 390, height: 844 });
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth), true);
    await page.screenshot({ path: path.join(evidence, 'interface-mobile.png'), fullPage: true });
    checks.push('Interface responsiva verificada em 1440 px e 390 px, sem overflow da pagina.');
    await page.setViewportSize({ width: 1440, height: 1000 });

    await page.getByRole('button', { name: 'Excluir Mouse atualizado', exact: true }).click();
    const removed = page.waitForResponse((r) => r.request().method() === 'DELETE' && r.url().includes('/api/produtos/'));
    await page.locator('#confirm-delete').click();
    assert.equal((await removed).status(), 204);
    await page.waitForFunction(() => !document.querySelector('#products-body').textContent.includes('Mouse atualizado'));
    checks.push('Exclusao de produto por DELETE a partir da interface.');

    await page.route('**/api/produtos', (route) => route.abort());
    await page.locator('#reload').click();
    await visibleText('#page-error', 'Não foi possível acessar');
    await page.unroute('**/api/produtos');
    await page.locator('#reload').click();
    await page.waitForFunction(() => document.querySelector('#page-error').hidden && !document.querySelector('#reload').disabled);
    checks.push('Falha de conexao tratada com mensagem e recuperacao pelo botao Atualizar.');
    assert.deepEqual(errors, []);
    checks.push('Nenhuma excecao JavaScript nao tratada no navegador.');
    const report = checks.map((check, i) => `OK ${i + 1}: ${check}`).join('\n');
    fs.writeFileSync(path.join(evidence, 'testes-interface.txt'), `${report}\n\n${checks.length} verificacoes aprovadas.\n`);
    console.log(report);
  } catch (error) {
    await page.screenshot({ path: path.join(evidence, 'falha-interface.png'), fullPage: true }).catch(() => {});
    throw error;
  } finally { await browser.close(); }
})().catch((error) => { console.error(error); process.exitCode = 1; });
