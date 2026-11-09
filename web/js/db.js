// Base de datos local IndexedDB; respaldo compatible con la versión anterior.
window.TripDB = (() => {
 const key='rumbo-cusco-v1';let db=null;let queue=Promise.resolve();
 function transact(mode,action){return new Promise((resolve,reject)=>{const tx=db.transaction('data',mode);let result;const req=action(tx.objectStore('data'));req.onsuccess=()=>result=req.result;tx.oncomplete=()=>resolve(result);tx.onerror=()=>reject(tx.error);tx.onabort=()=>reject(tx.error||new Error('Transacción cancelada'));})}
 async function load(initial){let old;try{old=JSON.parse(localStorage.getItem(key))}catch{}try{db=await new Promise((resolve,reject)=>{const req=indexedDB.open('rumbo-cusco',1);req.onupgradeneeded=()=>req.result.createObjectStore('data');req.onsuccess=()=>resolve(req.result);req.onerror=()=>reject(req.error);req.onblocked=()=>reject(new Error('Base bloqueada'));});const saved=await transact('readonly',s=>s.get('trip'));const value=saved||old||structuredClone(initial);await save(value);return value;}catch{db=null;return old||structuredClone(initial)}}
 function save(state){const snapshot=structuredClone(state);queue=queue.catch(()=>{}).then(async()=>{let backup=false;try{localStorage.setItem(key,JSON.stringify(snapshot));backup=true}catch{}if(db){await transact('readwrite',s=>s.put(snapshot,'trip'))}else if(!backup)throw new Error('Almacenamiento no disponible');});return queue;}
 return {load,save};
})();
