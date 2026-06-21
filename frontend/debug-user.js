const axios = require('axios');

(async () => {
  const email = `flow-${Date.now()}@example.com`;
  const password = 'Test1234!';

  try {
    const create = await axios.post('http://localhost:8081/users', { email, password, role: 'ROLE_CUSTOMER' });
    console.log('create', create.status);
  } catch (err) {
    console.error('create failed', err.response ? err.response.status : err.message, err.response ? err.response.data : '');
    return;
  }

  let token;
  try {
    const login = await axios.post('http://localhost:8081/users/login', { email, password });
    console.log('login', login.status, login.data);
    token = login.data.token;
  } catch (err) {
    console.error('login failed', err.response ? err.response.status : err.message, err.response ? err.response.data : '');
    return;
  }

  try {
    const testCustomer = await axios.get('http://localhost:8081/users/test/customer', { headers: { Authorization: `Bearer ${token}` } });
    console.log('test/customer', testCustomer.status, testCustomer.data);
  } catch (err) {
    console.error('test/customer failed', err.response ? err.response.status : err.message, err.response ? err.response.data : '');
  }

  try {
    const update = await axios.post('http://localhost:8081/users/update-profile', { name: 'Fluxo Teste', role: 'ROLE_ADMINISTRATOR' }, { headers: { Authorization: `Bearer ${token}` } });
    console.log('update-profile', update.status, update.data);
  } catch (err) {
    console.error('update-profile failed', err.response ? err.response.status : err.message, err.response ? err.response.data : '');
  }
})();
