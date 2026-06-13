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
        // Redireciona passando o e-mail via query string
        res.redirect(`/verify?email=${encodeURIComponent(email)}`);
    } catch (error) {
        console.error(error);
        res.status(500).send('Erro ao solicitar código de validação.');
    }
});

// GET /verify - Serve o formulário de validação
app.get('/verify', (req, res) => {
    res.sendFile(path.join(__dirname, 'views', 'verify.html'));
});

// POST /verify-code - Valida o código, salva JWT em session e redireciona
app.post('/verify-code', async (req, res) => {
    const { email, code } = req.body;
    try {
        const response = await axios.post('http://localhost:8081/auth/verify-code', { email, code });
        const token = response.data.token; // Ajuste se seu backend retornar uma chave diferente

        // Injeta script para salvar no sessionStorage do cliente e redirecionar
        res.send(`
            <script>
                sessionStorage.setItem('token', '${token}');
                window.location.href = '/dashboard';
            </script>
        `);
    } catch (error) {
        console.error(error);
        res.status(401).send('Código inválido ou expirado.');
    }
});

app.listen(PORT, () => {
    console.log(`Frontend rodando na porta ${PORT}`);
});
