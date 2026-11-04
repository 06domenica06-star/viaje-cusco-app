import os,re,pathlib,sys
root=pathlib.Path(__file__).resolve().parents[1]
version=os.environ.get('APP_VERSION_NAME','1.0');code=os.environ.get('APP_VERSION_CODE','1')
if not re.fullmatch(r'\d+\.\d+(?:\.\d+)?',version):sys.exit('Usa una versión como 1.0 o 1.1.')
if not code.isdigit() or not 0<int(code)<2100000000:sys.exit('El código debe ser un entero positivo menor de 2100000000.')
for name in ['web/index.html','web/js/cloud.js','web/js/firebase-config.js','app/src/main/res/drawable/logo.png']:
 if not (root/name).is_file():sys.exit('Falta '+name)
p=root/'web/index.html';s=p.read_text();s=re.sub(r'VIAJE CUSCO · v[0-9.]+','VIAJE CUSCO · v'+version,s);p.write_text(s)
print('Versión y archivos verificados.')
