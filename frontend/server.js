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

        // Injeta o token no sessionStorage do cliente e redireciona para a página de registro
        res.send(`
            <!DOCTYPE html>
            <html>
            <body>
                <script>
                    sessionStorage.setItem('token', '${token}');
                    window.location.href = '/register';
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
// Serve página de registro
app.get('/register', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'register.html'));
});

// POST /register - recebe name/role do cliente e encaminha para User Service
app.post('/register', async (req, res) => {
    const token = req.headers['authorization'];
    const { name, role } = req.body;
    if (!token) return res.status(401).send('Token ausente');
    try {
        const response = await axios.post('http://localhost:8081/users/update-profile', { name, role }, { headers: { Authorization: token } });
        return res.status(response.status).send(response.data);
    } catch (err) {
        console.error('Erro ao atualizar perfil:', err.response ? err.response.data : err.message);
        const status = err.response ? err.response.status : 500;
        return res.status(status).send(err.response ? JSON.stringify(err.response.data) : err.message);
    }
});

// GET /dashboard - serve a página do dashboard (cliente fará chamadas via JS)
app.get('/dashboard', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'dashboard.html'));
});

// Proxy para endpoint protegido (chama /users/test/customer)
app.get('/api/protected', async (req, res) => {
    const token = req.headers['authorization'];
    if (!token) return res.status(401).send('Token ausente');
    try {
        const response = await axios.get('http://localhost:8081/users/test/customer', { headers: { Authorization: token } });
        res.status(response.status).send(response.data);
    } catch (err) {
        console.error('Erro proxy /api/protected:', err.response ? err.response.data : err.message);
        const status = err.response ? err.response.status : 500;
        res.status(status).send(err.response ? JSON.stringify(err.response.data) : err.message);
    }
});

// Proxy para /users/me
app.get('/api/me', async (req, res) => {
    const token = req.headers['authorization'];
    if (!token) return res.status(401).send('Token ausente');
    try {
        const response = await axios.get('http://localhost:8081/users/me', { headers: { Authorization: token } });
        res.status(response.status).send(response.data);
    } catch (err) {
        console.error('Erro proxy /api/me:', err.response ? err.response.data : err.message);
        const status = err.response ? err.response.status : 500;
        res.status(status).send(err.response ? JSON.stringify(err.response.data) : err.message);
    }
});

app.listen(PORT, () => {
    console.log(`Frontend rodando em http://localhost:${PORT}`);
});
