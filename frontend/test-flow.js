const axios = require('axios');

const baseUrl = 'http://localhost:8081';
const email = `flow-${Date.now()}@example.com`;
const password = 'Test1234!';

async function run() {
  try {
    console.log('1) Criando usuário');
    const createResp = await axios.post(`${baseUrl}/users`, {
      email,
      password,
      role: 'ROLE_CUSTOMER'
    });
    console.log('create status', createResp.status);
  } catch (err) {
    if (err.response) {
      console.error('create failed', err.response.status, err.response.data);
    } else {
      console.error('create error', err.message);
    }
  }

  try {
    console.log('2) Fazendo login');
    const loginResp = await axios.post(`${baseUrl}/users/login`, {
      email,
      password
    });
    console.log('login status', loginResp.status, loginResp.data);
    const token = loginResp.data.token;

    console.log('3) Atualizando perfil');
    const updateResp = await axios.post(`${baseUrl}/users/update-profile`, {
      name: 'Fluxo Teste',
      role: 'ROLE_ADMINISTRATOR'
    }, {
      headers: {
        Authorization: `Bearer ${token}`
      }
    });
    console.log('update status', updateResp.status, updateResp.data);

    console.log('4) Buscando /users/me');
    const meResp = await axios.get(`${baseUrl}/users/me`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('me status', meResp.status, meResp.data);

    console.log('5) Testando /users/test/administrator');
    const adminResp = await axios.get(`${baseUrl}/users/test/administrator`, {
      headers: { Authorization: `Bearer ${token}` }
    });
    console.log('admin test status', adminResp.status, adminResp.data);
  } catch (err) {
    if (err.response) {
      console.error('erro', err.response.status, err.response.data);
    } else {
      console.error('erro', err.message);
    }
  }
}

run();
