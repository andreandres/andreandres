const { spawn } = require('node:child_process');
const fs = require('node:fs');
const path = require('node:path');
const os = require('node:os');
const assert = require('node:assert/strict');
const { setTimeout: pause } = require('node:timers/promises');

const root = path.join(__dirname, '..');
const backend = path.join(root, 'backend');
const java = process.env.JAVA_EXE || (process.env.JAVA_HOME
  ? path.join(process.env.JAVA_HOME, 'bin', process.platform === 'win32' ? 'java.exe' : 'java') : 'java');
const port = Number(process.env.TEST_PORT || 18083);
const base = `http://localhost:${port}`;
const temporary = fs.mkdtempSync(path.join(os.tmpdir(), 'estoque-persistencia-'));
let server;

async function stop() {
  if (!server || server.exitCode !== null) return;
  await new Promise((resolve) => { server.once('exit', resolve); server.kill(); });
}

async function start() {
  const log = fs.openSync(path.join(temporary, 'servidor.log'), 'a');
  server = spawn(java, ['-jar', path.join(backend, 'target', 'estoque-api-2.0.0.jar'),
    `--server.port=${port}`, `--spring.datasource.url=jdbc:h2:file:${temporary.replaceAll('\\', '/')}/estoque;WRITE_DELAY=0`,
    '--spring.jpa.hibernate.ddl-auto=update', '--debug=false', '--logging.level.root=WARN'],
  { cwd: backend, windowsHide: true, stdio: ['ignore', log, log] });
  fs.closeSync(log);
  let launchError;
  server.once('error', (error) => { launchError = error; });
  for (let attempt = 0; attempt < 200; attempt++) {
    if (launchError) throw launchError;
    if (server.exitCode !== null) throw new Error(`Servidor encerrou. Veja ${temporary}/servidor.log`);
    try { if ((await fetch(`${base}/api/categorias`, { signal: AbortSignal.timeout(1000) })).ok) return; }
    catch { /* aguarda a inicializacao do servidor */ }
    await pause(250);
  }
  throw new Error('Tempo de inicializacao esgotado');
}

(async () => {
  // Recusa portas ocupadas para nao operar em outra instancia com dados reais.
  let occupied = false;
  try { await fetch(base, { signal: AbortSignal.timeout(1000) }); occupied = true; } catch {}
  if (occupied) throw new Error(`Porta ${port} ocupada. Escolha outra com TEST_PORT.`);
  try {
    await start();
    const categoryResponse = await fetch(`${base}/api/categorias`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ nome: 'Categoria persistida' }),
    });
    assert.equal(categoryResponse.status, 201);
    const category = await categoryResponse.json();
    const productResponse = await fetch(`${base}/api/produtos`, {
      method: 'POST', headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nome: 'Produto persistido', preco: 19.90, quantidadeEstoque: 5, categoriaId: category.id }),
    });
    assert.equal(productResponse.status, 201);
    const product = await productResponse.json();
    await stop();
    await start();
    const recovered = await (await fetch(`${base}/api/produtos/${product.id}`)).json();
    assert.deepEqual(recovered, product);
    const page = await fetch(base);
    assert.equal(page.status, 200);
    assert.match(await page.text(), /icon-search/);
    const report = 'OK: categoria e produto cadastrados por HTTP no H2 em arquivo.\n'
      + 'OK: processo Java encerrado e iniciado novamente com o mesmo banco.\n'
      + 'OK: produto, categoria relacionada, valores e data recuperados sem alteracoes.\n'
      + 'OK: interface entregue pelo JAR final na mesma origem da API.\n';
    fs.mkdirSync(path.join(root, 'evidencias'), { recursive: true });
    fs.writeFileSync(path.join(root, 'evidencias', 'testes-persistencia.txt'), report);
    console.log(report);
  } finally { await stop(); }
})().catch((error) => { console.error(error); process.exitCode = 1; });
