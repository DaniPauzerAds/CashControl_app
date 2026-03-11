const express = require('express');
const mysql = require('mysql2');
const bcrypt = require('bcrypt');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());
const db = mysql.createPool({
  host: 'centerbeam.proxy.rlwy.net',
  port: 31645,
  user: 'root',
  password: 'mJIxZGmBtoPpEXErdjUFRArEArErwGOl',
  database: 'railway',
  ssl: {
    rejectUnauthorized: false
  }
});

db.getConnection((err, connection) => {
  if (err) {
    console.log('Erro ao conectar no banco:', err);
    return;
  }
  console.log('Conectado ao banco de dados!');
  connection.release();
});

// Rota de cadastro
app.post('/cadastro', async (req, res) => {
  const { nome, email, senha } = req.body;
  const senhaCriptografada = await bcrypt.hash(senha, 10);
  const sql = 'INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)';
  db.query(sql, [nome, email, senhaCriptografada], (err, result) => {
    if (err) {
      console.log('Erro detalhado:', err);
      return res.status(500).json({ erro: 'Erro ao cadastrar usuário' });
    }
    res.status(201).json({ mensagem: 'Usuário cadastrado com sucesso!' });
  });
});

// Rota de login
app.post('/login', async (req, res) => {
  const { email, senha } = req.body;
  const sql = 'SELECT * FROM usuarios WHERE email = ?';
  db.query(sql, [email], async (err, results) => {
    if (err) return res.status(500).json({ erro: 'Erro no servidor' });
    if (results.length === 0) return res.status(401).json({ erro: 'Email ou senha inválidos' });
    const senhaCorreta = await bcrypt.compare(senha, results[0].senha);
    if (!senhaCorreta) return res.status(401).json({ erro: 'Email ou senha inválidos' });
    res.status(200).json({ mensagem: 'Login realizado com sucesso!', usuario: results[0].nome });
  });
});

app.listen(3000, () => {
  console.log('Servidor rodando na porta 3000');
});