import json
from urllib.request import Request, urlopen
from urllib.error import HTTPError

base = 'http://localhost:8082'
email = 'flowtest@example.com'
password = 'Test1234!'
print('email', email)

create_req = Request(
    f'{base}/users',
    data=json.dumps({'email': email, 'password': password, 'role': 'ROLE_CUSTOMER'}).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
)
try:
    resp = urlopen(create_req)
    print('create', resp.getcode(), resp.read().decode('utf-8'))
except HTTPError as e:
    print('create ERROR', e.code, e.read().decode('utf-8'))
    raise

login_req = Request(
    f'{base}/users/login',
    data=json.dumps({'email': email, 'password': password}).encode('utf-8'),
    headers={'Content-Type': 'application/json'},
)
try:
    resp = urlopen(login_req)
    body = json.loads(resp.read().decode('utf-8'))
    print('login', body)
except HTTPError as e:
    print('login ERROR', e.code, e.read().decode('utf-8'))
    raise

token = body['token']
for path in ['/users/test', '/users/test/customer', '/users/me']:
    req = Request(f'{base}{path}', headers={'Authorization': f'Bearer {token}'})
    try:
        resp = urlopen(req)
        print(path, resp.getcode(), resp.read().decode('utf-8'))
    except HTTPError as e:
        print(path, 'ERROR', e.code, e.read().decode('utf-8'))

update_req = Request(
    f'{base}/users/update-profile',
    data=json.dumps({'name': 'Fluxo Teste', 'role': 'ROLE_ADMINISTRATOR'}).encode('utf-8'),
    headers={'Content-Type': 'application/json', 'Authorization': f'Bearer {token}'},
)
try:
    resp = urlopen(update_req)
    print('/users/update-profile', resp.getcode(), resp.read().decode('utf-8'))
except HTTPError as e:
    print('/users/update-profile ERROR', e.code, e.read().decode('utf-8'))
