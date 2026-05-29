const express = require('express');
const mysql = require('mysql2');
const bcrypt = require('bcrypt');
const cors = require('cors');
const jwt = require('jsonwebtoken');
const JWT_SECRET = 'cashcontrol_secret_key';

function verificarToken(req, res, next) {
  const token = req.headers['authorization'];
  if (!token) return res.status(401).json({ erro: 'Token não fornecido' });
  try {
    const decoded = jwt.verify(token, JWT_SECRET);
    req.usuario = decoded;
    next();
  } catch (err) {
    return res.status(401).json({ erro: 'Token inválido' });
  }
}

const app = express();
app.use(cors());
app.use(express.json());

const db = mysql.createPool({
  host: process.env.MYSQLHOST,
  port: process.env.MYSQLPORT,
  user: process.env.MYSQLUSER,
  password: process.env.MYSQLPASSWORD,
  database: process.env.MYSQLDATABASE,
  ssl: { rejectUnauthorized: false }
});

db.getConnection((err, connection) => {
  if (err) { console.log('Erro ao conectar no banco:', err); return; }
  console.log('Conectado ao banco de dados!');
  connection.release();
});

app.get('/', (req, res) => {
  res.status(200).json({ status: 'ok' });
});

app.post('/cadastro', async (req, res) => {
  const { nome, email, senha } = req.body;
  const senhaCriptografada = await bcrypt.hash(senha, 10);
  const sql = 'INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)';
  db.query(sql, [nome, email, senhaCriptografada], (err, result) => {
    if (err) { console.log('Erro detalhado:', err); return res.status(500).json({ erro: 'Erro ao cadastrar usuário' }); }
    res.status(201).json({ mensagem: 'Usuário cadastrado com sucesso!' });
  });
});

app.post('/login', async (req, res) => {
  const { email, senha } = req.body;
  const sql = 'SELECT * FROM usuarios WHERE email = ?';
  db.query(sql, [email], async (err, results) => {
    if (err) return res.status(500).json({ erro: 'Erro no servidor' });
    if (results.length === 0) return res.status(401).json({ erro: 'Email ou senha inválidos' });
    const senhaCorreta = await bcrypt.compare(senha, results[0].senha);
    if (!senhaCorreta) return res.status(401).json({ erro: 'Email ou senha inválidos' });
    
    const token = jwt.sign(
      { id: results[0].id, email: results[0].email },
      JWT_SECRET,
      { expiresIn: '30d' }
    );
    
    res.status(200).json({
      mensagem: 'Login realizado com sucesso!',
      usuario: results[0].nome,
      id: results[0].id,
      token: token
    });
  });
});

app.post('/gastos', async (req, res) => {
  const { descricao, valor, categoria, data, id_usuario, tipo } = req.body;
  const sql = 'INSERT INTO gastos (descricao, valor, categoria, data, id_usuario, tipo) VALUES (?, ?, ?, ?, ?, ?)';
  db.query(sql, [descricao, valor, categoria, data, id_usuario, tipo || 'gasto'], (err, result) => {
    if (err) { console.log('Erro detalhado:', err); return res.status(500).json({ erro: 'Erro ao adicionar gasto' }); }
    res.status(201).json({ mensagem: 'Gasto adicionado com sucesso!' });
  });
});

app.get('/gastos/resumo/:id_usuario', (req, res) => {
  const { id_usuario } = req.params;
  const sql = `
    SELECT 
      categoria, 
      SUM(CASE WHEN tipo = 'gasto' THEN valor ELSE 0 END) as total_gastos,
      SUM(CASE WHEN tipo = 'ganho' THEN valor ELSE 0 END) as total_ganhos,
      tipo
    FROM gastos 
    WHERE id_usuario = ? 
    AND data >= DATE_SUB(CURDATE(), INTERVAL 30 DAY) 
    GROUP BY categoria, tipo
  `;
  db.query(sql, [id_usuario], (err, results) => {
    if (err) return res.status(500).json({ erro: 'Erro ao buscar resumo' });
    res.status(200).json(results);
  });
});

app.get('/gastos/:id_usuario', (req, res) => {
  const { id_usuario } = req.params;
  const sql = 'SELECT * FROM gastos WHERE id_usuario = ? ORDER BY id DESC';
  db.query(sql, [id_usuario], (err, results) => {
    if (err) return res.status(500).json({ erro: 'Erro ao buscar gastos' });
    res.status(200).json(results);
  });
});

app.delete('/gastos/:id', (req, res) => {
  const { id } = req.params;
  const sql = 'DELETE FROM gastos WHERE id = ?';
  db.query(sql, [id], (err, result) => {
    if (err) return res.status(500).json({ erro: 'Erro ao deletar gasto' });
    res.status(200).json({ mensagem: 'Gasto deletado com sucesso!' });
  });
});

app.put('/gastos/:id', (req, res) => {
  const { id } = req.params;
  const { descricao, valor, categoria, data } = req.body;
  const sql = 'UPDATE gastos SET descricao = ?, valor = ?, categoria = ?, data = ? WHERE id = ?';
  db.query(sql, [descricao, valor, categoria, data, id], (err, result) => {
    if (err) return res.status(500).json({ erro: 'Erro ao editar gasto' });
    res.status(200).json({ mensagem: 'Gasto editado com sucesso!' });
  });
});

app.put('/redefinir-senha', async (req, res) => {
  const { email, novaSenha } = req.body;
  const sql = 'SELECT * FROM usuarios WHERE email = ?';
  db.query(sql, [email], async (err, results) => {
    if (err) return res.status(500).json({ erro: 'Erro no servidor' });
    if (results.length === 0) return res.status(404).json({ erro: 'Email não encontrado' });
    const senhaCriptografada = await bcrypt.hash(novaSenha, 10);
    const sqlUpdate = 'UPDATE usuarios SET senha = ? WHERE email = ?';
    db.query(sqlUpdate, [senhaCriptografada, email], (err) => {
      if (err) return res.status(500).json({ erro: 'Erro ao atualizar senha' });
      res.status(200).json({ mensagem: 'Senha atualizada com sucesso!' });
    });
  });
});

app.put('/usuarios/:id', verificarToken, async (req, res) => {
  const { id } = req.params;
  const { nome, email, senha } = req.body;
  
  if (req.usuario.id != id) {
    return res.status(403).json({ erro: 'Sem permissão para alterar este usuário' });
  }

  if (senha) {
    const senhaCriptografada = await bcrypt.hash(senha, 10);
    const sql = 'UPDATE usuarios SET nome = ?, email = ?, senha = ? WHERE id = ?';
    db.query(sql, [nome, email, senhaCriptografada, id], (err) => {
      if (err) return res.status(500).json({ erro: 'Erro ao atualizar usuário' });
      res.status(200).json({ mensagem: 'Dados atualizados com sucesso!' });
    });
  } else {
    const sql = 'UPDATE usuarios SET nome = ?, email = ? WHERE id = ?';
    db.query(sql, [nome, email, id], (err) => {
      if (err) return res.status(500).json({ erro: 'Erro ao atualizar usuário' });
      res.status(200).json({ mensagem: 'Dados atualizados com sucesso!' });
    });
  }
});

app.delete('/usuarios/:id', verificarToken, async (req, res) => {
  const { id } = req.params;

  if (req.usuario.id != id) {
    return res.status(403).json({ erro: 'Sem permissão para deletar este usuário' });
  }

  db.query('DELETE FROM gastos WHERE id_usuario = ?', [id], (err) => {
    if (err) return res.status(500).json({ erro: 'Erro ao deletar gastos' });
    db.query('DELETE FROM usuarios WHERE id = ?', [id], (err) => {
      if (err) return res.status(500).json({ erro: 'Erro ao deletar usuário' });
      res.status(200).json({ mensagem: 'Conta deletada com sucesso!' });
    });
  });
});

const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
  console.log('Servidor rodando na porta ' + PORT);
});