const axios = require('axios');

(async () => {
  const email = `flow-${Date.now()}@example.com`;
  const password = 'Test1234!';
  try {
    await axios.post('http://localhost:8081/users', { email, password, role: 'ROLE_CUSTOMER' });
    const login = await axios.post('http://localhost:8081/users/login', { email, password });
    const token = login.data.token;
    try {
      await axios.post('http://localhost:8081/users/update-profile', { name: 'Fluxo Teste', role: 'ROLE_ADMINISTRATOR' }, { headers: { Authorization: `Bearer ${token}` } });
      console.log('update-profile OK');
    } catch (err) {
      if (err.response) {
        console.error('status', err.response.status);
        console.error('statusText', err.response.statusText);
        console.error('headers', err.response.headers);
        console.error('body', err.response.data);
      } else {
        console.error('error', err.message);
      }
    }
  } catch (err) {
    console.error(err.response ? err.response.data : err.message);
  }
})();
