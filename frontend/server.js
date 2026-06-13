const express = require('express');
const axios = require('axios');
const path = require('path');

const app = express();
const PORT = 3000;

app.use(express.urlencoded({ extended: true }));
app.use(express.json());

// GET / - Serve o formulário de e-mail
app.get('/', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'index.html'));
});

// POST /send-code - Recebe e-mail, chama API e redireciona
app.post('/send-code', async (req, res) => {
    const { email } = req.body;
    try {
        await axios.post('http://localhost:8081/auth/request-code', { email });
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error('Erro ao solicitar código:', error.message);
        res.status(500).send(`
            <p>Erro ao solicitar código de validação.</p>
            <a href="/">Tentar novamente</a>
        `);
    }
});

// GET /verify - Serve o formulário de validação
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'verify.html'));
});

// POST /verify-code - Valida o código e redireciona com token
app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;
    try {
        // Envia "code" para o backend (VerifyCodeDto usa campo "code")
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });
        const token = response.data.token;

        // Injeta o token no sessionStorage do cliente e redireciona para o dashboard
        res.send(`
            <!DOCTYPE html>
            <html>
            <body>
                <script>
                    sessionStorage.setItem('token', '${token}');
                    window.location.href = '/dashboard';
                </script>
            </body>
            </html>
        `);
    } catch (error) {
        console.error('Erro ao verificar código:', error.message);
        res.status(401).send(`
            <p>Código inválido ou expirado.</p>
            <a href="javascript:history.back()">Tentar novamente</a>
        `);
    }
});

// GET /dashboard - Placeholder para a semana 4
app.get('/dashboard', (req, res) => {
    res.send(`
        <!DOCTYPE html>
        <html lang="pt-br">
        <head><meta charset="UTF-8"><title>Dashboard</title></head>
        <body>
            <h2>Dashboard</h2>
            <p>Autenticado com sucesso! (Semana 4)</p>
        </body>
        </html>
    `);
});

app.listen(PORT, () => {
    console.log(`Frontend rodando em http://localhost:${PORT}`);
});
