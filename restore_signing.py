import base64,os,pathlib,sys
key=os.environ.get('KEYSTORE_BASE64','').strip()
password=os.environ.get('STORE_PASSWORD','')
if not key or not password:
 sys.exit('Faltan los secretos ANDROID_KEYSTORE_BASE64 y ANDROID_KEYSTORE_PASSWORD. Configúralos en Settings > Secrets and variables > Actions.')
try:
 raw=base64.b64decode(''.join(key.split()),validate=True)
except Exception:
 sys.exit('ANDROID_KEYSTORE_BASE64 no contiene Base64 válido.')
p=pathlib.Path(os.environ['RUNNER_TEMP'])/'viaje-cusco.p12'
p.write_bytes(raw);p.chmod(0o600)
print('Firma privada preparada sin mostrar su contenido.')
