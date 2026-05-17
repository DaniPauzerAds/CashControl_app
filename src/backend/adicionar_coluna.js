const mysql = require('mysql2');
const db = mysql.createPool({
  host: 'centerbeam.proxy.rlwy.net',
  port: 31645,
  user: 'root',
  password: 'mJIxZGmBtoPpEXErdjUFRArEArErwGOl',
  database: 'railway',
  ssl: { rejectUnauthorized: false }
});

db.query('ALTER TABLE gastos ADD COLUMN tipo VARCHAR(10) NOT NULL DEFAULT "gasto"', (err) => {
  if(err) console.log('Erro:', err);
  else console.log('Coluna tipo adicionada!');
  process.exit();
});