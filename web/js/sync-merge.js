// Cambios por registro y campo: editar un gasto no reemplaza las otras listas.
(function(root){
 const same=(a,b)=>JSON.stringify(a)===JSON.stringify(b);
 const copy=x=>x===undefined?undefined:JSON.parse(JSON.stringify(x));
 function diff(before,after){const out=[];for(const k of new Set([...Object.keys(before),...Object.keys(after)])){
 const a=before[k],b=after[k];if(same(a,b))continue;
 if(Array.isArray(a)&&Array.isArray(b)&&[...a,...b].every(x=>x&&typeof x==='object'&&x.id!==undefined)){
 const old=new Map(a.map(x=>[String(x.id),x]));const next=new Map(b.map(x=>[String(x.id),x]));
 for(const [id]of old)if(!next.has(id))out.push({kind:'remove',key:k,id});
 for(const [id,row]of next){if(!old.has(id)){out.push({kind:'add',key:k,id,value:copy(row)});continue}const changes={};for(const f of new Set([...Object.keys(old.get(id)),...Object.keys(row)]))if(!same(old.get(id)[f],row[f]))changes[f]=row[f]===undefined?null:copy(row[f]);if(Object.keys(changes).length)out.push({kind:'edit',key:k,id,value:changes})}
 }else out.push({kind:'set',key:k,value:b===undefined?null:copy(b)});
 }return out}
 function apply(base,ops){const result=copy(base);for(const op of ops){if(op.kind==='set'){result[op.key]=copy(op.value);continue}const list=Array.isArray(result[op.key])?result[op.key]:[];const i=list.findIndex(x=>String(x.id)===op.id);if(op.kind==='remove'){if(i>=0)list.splice(i,1)}else if(op.kind==='add'){if(i<0)list.push(copy(op.value));else list[i]={...list[i],...copy(op.value)}}else if(op.kind==='edit'&&i>=0){list[i]={...list[i],...copy(op.value)}}result[op.key]=list}return result}
 root.TripMerge={diff,apply};
})(typeof window==='undefined'?globalThis:window);
