import json
import time
from urllib.request import Request, urlopen
from urllib.error import HTTPError

base_url = 'http://localhost:8085'

email = f'flow-{int(time.time())}@example.com'
password = 'Test1234!'
print('email', email)

req = Request(
    f'{base_url}/users',
    data=json.dumps({'email': email, 'password': password, 'role': 'ROLE_CUSTOMER'}).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
)
resp = urlopen(req)
print('create', resp.getcode(), resp.read().decode('utf-8'))

req = Request(
    f'{base_url}/users/login',
    data=json.dumps({'email': email, 'password': password}).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
)
resp = urlopen(req)
body = json.loads(resp.read().decode('utf-8'))
print('login', body)
token = body['token']

req = Request(
    f'{base_url}/users/test/customer',
    headers={'Authorization': f'Bearer {token}'},
)
resp = urlopen(req)
print('test/customer', resp.getcode(), resp.read().decode('utf-8'))

req = Request(
    f'{base_url}/users/me',
    headers={'Authorization': f'Bearer {token}'},
)
resp = urlopen(req)
print('me', resp.getcode(), resp.read().decode('utf-8'))

req = Request(
    f'{base_url}/users/update-profile',
    data=json.dumps({'name': 'Fluxo Teste', 'role': 'ROLE_ADMINISTRATOR'}).encode('utf-8'),
    headers={'Content-Type': 'application/json', 'Authorization': f'Bearer {token}'},
)
try:
    resp = urlopen(req)
    print('update-profile', resp.getcode(), resp.read().decode('utf-8'))
except HTTPError as e:
    print('update-profile ERROR', e.code)
    print(e.read().decode('utf-8'))
