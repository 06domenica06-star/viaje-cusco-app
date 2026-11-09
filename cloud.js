window.TripCloud=(()=>{
 const pendingKey='rumbo-pending-cloud-v1';let pending=[];try{pending=JSON.parse(localStorage.getItem(pendingKey)||'[]')}catch{};
 let api,db,auth,uid='',ref,off,working,remote,ready=false,busy=false,timer,callbacks;
 const clone=x=>structuredClone(x);
 const status=(text)=>{document.querySelector('#cloud-status').textContent=text};
 function persist(){localStorage.setItem(pendingKey,JSON.stringify(pending))}
 function showError(error){status(error?.code==='permission-denied'?'Dispositivo pendiente de autorización.':'No se pudo conectar. Tus cambios siguen guardados en este dispositivo.');document.querySelector('#cloud-retry').hidden=false}
 function deliver(){if(!remote)return;working=TripMerge.apply(remote,pending.flatMap(p=>p.ops));callbacks.receive(clone(working))}
 async function flush(){if(!ready||busy||!pending.length)return;busy=true;const batch=pending.slice();try{
 status('Sincronizando…');await api.runTransaction(db,async tx=>{const snap=await tx.get(ref);const current=snap.exists()?snap.data().state:working;const next=TripMerge.apply(current,batch.flatMap(p=>p.ops));tx.set(ref,{state:next,updatedAt:api.serverTimestamp(),updatedBy:uid,schemaVersion:1})});
 pending=pending.filter(p=>!batch.some(b=>b.id===p.id));persist();status(pending.length?'Cambios pendientes…':'Sincronizado');
 }catch(e){showError(e)}finally{busy=false;if(pending.length&&navigator.onLine)timer=setTimeout(flush,6000)}}
 function capture(next){if(!ready||!working)return;const ops=TripMerge.diff(working,next);working=clone(next);if(!ops.length)return;pending.push({id:crypto.randomUUID(),ops});try{persist()}catch{status('No hay espacio para guardar cambios pendientes. Exporta una copia.')}clearTimeout(timer);timer=setTimeout(flush,700);status(navigator.onLine?'Cambios pendientes…':'Sin internet · cambios pendientes')}
 async function connect(){if(!api||!uid)return;document.querySelector('#cloud-retry').hidden=true;try{
 status('Comprobando acceso…');const permit=await api.getDoc(api.doc(db,'allowedDevices',uid));if(!permit.exists()||permit.data().enabled!==true){ready=false;status('Dispositivo pendiente de autorización.');document.querySelector('#cloud-retry').hidden=false;return}
 ref=api.doc(db,'trips','cusco-2026');let snap=await api.getDoc(ref);
 if(!snap.exists())await api.runTransaction(db,async tx=>{const current=await tx.get(ref);if(!current.exists())tx.set(ref,{state:callbacks.current(),updatedAt:api.serverTimestamp(),updatedBy:uid,schemaVersion:1})});
 else if(!localStorage.getItem('rumbo-cloud-used-v1')){
 // Copia recuperable antes de adoptar por primera vez los datos del otro dispositivo.
 localStorage.setItem('rumbo-before-first-sync-v1',JSON.stringify(callbacks.current()));
 }
 ready=true;localStorage.setItem('rumbo-cloud-used-v1','yes');if(off)off();off=api.onSnapshot(ref,s=>{if(!s.exists())return;remote=s.data().state;deliver();if(!busy)status(pending.length?'Cambios pendientes…':'Sincronizado');if(pending.length)flush()},showError);flush();
 }catch(e){ready=false;showError(e)}}
 async function start(handlers){callbacks=handlers;working=clone(handlers.current());try{
 const [app,authentication,store]=await Promise.all([import('https://www.gstatic.com/firebasejs/13.2.0/firebase-app.js'),import('https://www.gstatic.com/firebasejs/13.2.0/firebase-auth.js'),import('https://www.gstatic.com/firebasejs/13.2.0/firebase-firestore.js')]);
 api=store;const instance=app.initializeApp(window.tripFirebaseConfig);auth=authentication.getAuth(instance);await authentication.setPersistence(auth,authentication.browserLocalPersistence);uid=(auth.currentUser|| (await authentication.signInAnonymously(auth)).user).uid;document.querySelector('#device-code').value=uid;db=store.getFirestore(instance);await connect();
 }catch(e){showError(e)} }
 document.addEventListener('click',async e=>{if(e.target.id==='cloud-retry'){if(api)connect();else if(callbacks)start(callbacks)}if(e.target.id==='cloud-copy'){const input=document.querySelector('#device-code');try{await navigator.clipboard.writeText(input.value);status('Código copiado')}catch{input.select();status('Selecciona y copia el código')}}if(e.target.id==='cloud-backup'){const text=localStorage.getItem('rumbo-before-first-sync-v1');if(!text){status('No hay una copia anterior a la primera sincronización.');return}if(window.NativeFiles){window.NativeFiles.saveText(text,'viaje-antes-de-sincronizar.json','application/json');return}const a=document.createElement('a');a.href=URL.createObjectURL(new Blob([text],{type:'application/json'}));a.download='viaje-antes-de-sincronizar.json';a.click();setTimeout(()=>URL.revokeObjectURL(a.href),1000)}});
 window.addEventListener('online',()=>ready?flush():connect());
 return{start,capture};
})();
