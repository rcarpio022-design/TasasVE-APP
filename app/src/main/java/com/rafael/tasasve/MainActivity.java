package com.rafael.tasasve;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    private WebView webView;

    private static final String HTML = """
<!DOCTYPE html>
<html lang="es">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1, viewport-fit=cover">
<title>Tasas VE</title>
<style>
:root{
  --bg:#EAEFF4;--surface:#FFFFFF;--ink:#10233B;--muted:#5A6B7F;--line:#D5DDE6;
  --bcv:#1F4E8C;--eur:#6B4FBB;--usdt:#C98A00;--gap:#C8372D;--up:#1E8E5A;--down:#C8372D;--focus:#1F4E8C;
  box-sizing:border-box;padding-top:env(safe-area-inset-top,0px);padding-bottom:env(safe-area-inset-bottom,0px);color-scheme:light;
}
@media (prefers-color-scheme:dark){
  :root:not([data-theme="light"]){--bg:#0B1520;--surface:#132130;--ink:#E8EEF5;--muted:#93A4B8;--line:#23354A;--bcv:#6FA3E6;--eur:#B7A2FF;--usdt:#F2B705;--gap:#FF7A6B;--up:#4CD08F;--down:#FF7A6B;--focus:#6FA3E6;color-scheme:dark}
}
:root[data-theme="dark"]{--bg:#0B1520;--surface:#132130;--ink:#E8EEF5;--muted:#93A4B8;--line:#23354A;--bcv:#6FA3E6;--eur:#B7A2FF;--usdt:#F2B705;--gap:#FF7A6B;--up:#4CD08F;--down:#FF7A6B;--focus:#6FA3E6;color-scheme:dark}
html{scroll-padding-top:env(safe-area-inset-top,0px)}
*{box-sizing:border-box}
body{margin:0;background:var(--bg);color:var(--ink);font-family:system-ui,-apple-system,"Segoe UI",Roboto,sans-serif;font-size:16px;line-height:1.4}
main{max-width:1180px;margin:0 auto;padding:18px 14px 40px}
h1{font-size:clamp(26px,4vw,36px);line-height:1.05;margin:0;font-weight:800;letter-spacing:-.02em}
h2{font-size:16px;margin:0;font-weight:600}
button,input,select{font:inherit;color:inherit}
.top{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;margin-bottom:16px;flex-wrap:wrap}
.note{color:var(--muted);font-size:14px;margin:6px 0 0}
.acts{display:flex;gap:8px}
.btn{background:var(--ink);color:var(--bg);border:0;border-radius:12px;padding:10px 16px;font-weight:600;cursor:pointer;white-space:nowrap}
.btn.ghost{background:var(--surface);color:var(--ink);border:1px solid var(--line)}
.grid{display:grid;gap:14px;grid-template-columns:minmax(0,1fr)}
@media(min-width:680px){.grid{grid-template-columns:repeat(2,minmax(0,1fr))}.wide,.last{grid-column:span 2}}
.tile{background:color-mix(in srgb,var(--surface) 75%,transparent);border:1px solid color-mix(in srgb,var(--ink) 12%,transparent);box-shadow:inset 0 3px 0 var(--c,transparent),0 8px 24px color-mix(in srgb,var(--ink) 10%,transparent);border-radius:22px;padding:16px 18px 18px;min-width:0}
.c-bcv{--c:var(--bcv)}.c-eur{--c:var(--eur)}.c-par{--c:#12A59B}.c-usdt{--c:var(--usdt)}.c-gap{--c:var(--gap)}
.th{display:flex;align-items:center;gap:8px;margin-bottom:10px;flex-wrap:wrap}
.src{margin-left:auto;font-size:13px;color:var(--muted)}
.dot{width:10px;height:10px;border-radius:50%;background:var(--muted);flex:none}
.dot.ok{background:var(--up)}.dot.mid{background:var(--usdt)}
.big{font-size:clamp(30px,4.2vw,42px);font-weight:800;letter-spacing:-.03em;line-height:1.05;margin:0 0 8px;font-variant-numeric:tabular-nums}
.lab{font-size:13px;color:var(--muted);margin:0 0 2px}
.chip{display:inline-block;font-size:13px;font-weight:600;padding:3px 9px;border-radius:99px;background:color-mix(in srgb,var(--ink) 8%,transparent);color:var(--muted);white-space:nowrap}
.chip.up{color:var(--up)}.chip.down{color:var(--down)}
.pr{display:flex;justify-content:space-between;padding:8px 0;border-top:1px solid var(--line);font-variant-numeric:tabular-nums}
.pr:first-of-type{margin-top:12px}
.pr span{color:var(--muted)}
.tf{font-size:13px;color:var(--muted);margin:12px 0 0}
.gapnum{font-size:clamp(44px,8vw,64px);font-weight:800;letter-spacing:-.03em;line-height:1;color:var(--gap);margin:0 0 4px}
.gaptxt{margin:0 0 16px;color:var(--muted);font-size:15px}
.bar{margin-bottom:10px}
.bar .lbl{display:flex;justify-content:space-between;font-size:14px;margin-bottom:4px}
.track{height:12px;background:color-mix(in srgb,var(--ink) 10%,transparent);border-radius:6px;overflow:hidden}
.fill{height:100%;border-radius:6px;transition:width .5s}
.row{display:flex;justify-content:space-between;align-items:baseline;gap:10px;padding:9px 0;border-bottom:1px solid var(--line)}
.row:last-child{border-bottom:0}
.row small{display:block;color:var(--muted);font-size:13px}
.v{font-weight:600;font-variant-numeric:tabular-nums;text-align:right}
.log{max-height:300px;overflow-y:auto}
.conv{display:grid;grid-template-columns:minmax(0,1fr) auto minmax(0,1fr);gap:8px;align-items:end;margin-top:12px}
.conv label{display:block;font-size:13px;color:var(--muted);margin-bottom:4px}
.fld{width:100%;background:color-mix(in srgb,var(--surface) 45%,transparent);border:1px solid color-mix(in srgb,var(--ink) 12%,transparent);border-radius:12px;padding:11px;min-width:0}
.amount{grid-column:1/-1}
.swap{background:color-mix(in srgb,var(--surface) 45%,transparent);border:1px solid color-mix(in srgb,var(--ink) 12%,transparent);border-radius:12px;height:44px;width:44px;cursor:pointer;font-size:18px}
.result{grid-column:1/-1;margin-top:8px;font-size:clamp(26px,4vw,34px);font-weight:800;letter-spacing:-.02em;font-variant-numeric:tabular-nums;overflow-wrap:anywhere}
.result small{display:block;font-size:14px;font-weight:400;color:var(--muted);letter-spacing:0}
.calc{margin-top:12px}
.cexpr{min-height:20px;text-align:right;color:var(--muted);font-size:14px;overflow-wrap:anywhere}
.cdisp{text-align:right;font-size:clamp(34px,6vw,44px);font-weight:800;letter-spacing:-.02em;font-variant-numeric:tabular-nums;overflow-wrap:anywhere;margin-bottom:10px}
.quick{display:flex;gap:6px;flex-wrap:wrap;margin-bottom:10px}
.quick button{background:color-mix(in srgb,var(--surface) 45%,transparent);border:1px solid color-mix(in srgb,var(--ink) 12%,transparent);border-radius:99px;padding:6px 12px;font-size:13px;cursor:pointer}
.keys{display:grid;grid-template-columns:repeat(4,1fr);gap:8px}
.keys button{height:54px;border-radius:14px;border:1px solid color-mix(in srgb,var(--ink) 12%,transparent);background:color-mix(in srgb,var(--surface) 45%,transparent);font-size:20px;font-weight:600;cursor:pointer}
.keys button:active{transform:scale(.97)}
.keys .op{color:var(--bcv)}
.keys .eq{background:var(--ink);color:var(--bg);border-color:var(--ink)}
.wl{display:grid;grid-template-columns:repeat(auto-fill,minmax(250px,1fr));gap:0 28px}
.srch{margin:10px 0 6px}
.foot{color:var(--muted);font-size:13px;text-align:center;margin-top:16px}
body::before{content:"";position:fixed;inset:0;z-index:-1;pointer-events:none;background:radial-gradient(520px 420px at 8% 4%,color-mix(in srgb,var(--bcv) 38%,transparent),transparent 70%),radial-gradient(480px 400px at 92% 10%,color-mix(in srgb,var(--eur) 34%,transparent),transparent 70%),radial-gradient(560px 460px at 85% 92%,color-mix(in srgb,var(--usdt) 32%,transparent),transparent 70%)}
</style>
</head>
<body>
<main>
  <div class="top">
    <div>
      <h1>Tasas VE</h1>
      <p class="note" id="stamp">Cargando tasas…</p>
    </div>
    <div class="acts">
      <button class="btn ghost" id="theme" type="button">Claro/Oscuro</button>
      <button class="btn" id="refresh" type="button">Actualizar</button>
    </div>
  </div>

  <div class="grid">
    <section class="tile c-bcv" id="t-usd"></section>
    <section class="tile c-eur" id="t-eur"></section>
    <section class="tile c-par" id="t-par"></section>
    <section class="tile c-usdt" id="t-bin"></section>
    <section class="tile c-usdt last" id="t-okx"></section>

    <section class="tile c-gap wide">
      <div class="th"><h2>Brecha entre el BCV y el USDT</h2></div>
      <p class="gapnum" id="gapnum">0 %</p>
      <p class="gaptxt" id="gaptxt"></p>
      <div class="bar"><div class="lbl"><span>Dólar BCV</span><b id="b1"></b></div><div class="track"><div class="fill" id="f1" style="background:var(--bcv)"></div></div></div>
      <div class="bar"><div class="lbl"><span>Euro BCV</span><b id="b3"></b></div><div class="track"><div class="fill" id="f3" style="background:var(--eur)"></div></div></div>
      <div class="bar"><div class="lbl"><span>Dólar paralelo</span><b id="b4"></b></div><div class="track"><div class="fill" id="f4" style="background:#12A59B"></div></div></div>
      <div class="bar"><div class="lbl"><span>USDT promedio</span><b id="b2"></b></div><div class="track"><div class="fill" id="f2" style="background:var(--usdt)"></div></div></div>
    </section>

    <section class="tile wide">
      <div class="th"><h2>Historial de actualizaciones</h2></div>
      <div class="log" id="log"></div>
    </section>

    <section class="tile wide">
      <div class="th"><h2>Convertidor de monedas</h2></div>
      <div class="conv">
        <div class="amount"><label for="amt">Monto</label><input class="fld" id="amt" type="number" inputmode="decimal" value="100" min="0"></div>
        <div><label for="from">De</label><select class="fld" id="from"></select></div>
        <button class="swap" id="swap" type="button">⇄</button>
        <div><label for="to">A</label><select class="fld" id="to"></select></div>
        <div class="result" id="result"></div>
      </div>
    </section>

    <section class="tile wide">
      <div class="th"><h2>Calculadora</h2></div>
      <div class="calc">
        <div class="cexpr" id="cexpr"></div>
        <div class="cdisp" id="cdisp">0</div>
        <div class="quick" id="quick"></div>
        <div class="keys" id="keys"></div>
      </div>
    </section>

    <section class="tile wide">
      <div class="th"><h2>Monedas del mundo</h2><span class="src">Valor en bolívares según el dólar BCV</span></div>
      <input class="fld srch" id="q" type="search" placeholder="Buscar moneda o país">
      <div class="wl" id="world"></div>
    </section>
  </div>
  <p class="foot">Fuentes: BCV y paralelo vía DolarApi, USDT desde el P2P de Binance y OKX, monedas del mundo vía open.er-api.com.</p>
</main>

<script>
var API_URL = "https://tasas-ve-api.rafael-f60.workers.dev/api";
var $ = function(id){return document.getElementById(id)};
var data, items = [], log = [], hasCached = false;

function makeSample(){
  var n = Date.now();
  return {
    bcv:{usd:400.00,eur:460.00,prevUsd:400.00,prevEur:460.00,at:new Date(n)},
    binance:{buy:null,sell:null,prevAvg:null,at:new Date(n)},
    okx:{buy:null,sell:null,prevAvg:null,at:new Date(n)},
    paralelo:{usd:500.00,prevUsd:500.00,at:new Date(n)},
    world:[
      {code:"COP",name:"Peso colombiano",country:"Colombia",perUsd:4100},
      {code:"BRL",name:"Real",country:"Brasil",perUsd:5.4},
      {code:"ARS",name:"Peso argentino",country:"Argentina",perUsd:1000},
      {code:"MXN",name:"Peso mexicano",country:"México",perUsd:18.5},
      {code:"CLP",name:"Peso chileno",country:"Chile",perUsd:950},
      {code:"PEN",name:"Sol",country:"Perú",perUsd:3.75},
      {code:"GBP",name:"Libra esterlina",country:"Reino Unido",perUsd:0.78},
      {code:"CAD",name:"Dólar canadiense",country:"Canadá",perUsd:1.38},
      {code:"DOP",name:"Peso dominicano",country:"República Dominicana",perUsd:60},
      {code:"PAB",name:"Balboa",country:"Panamá",perUsd:1},
      {code:"JPY",name:"Yen",country:"Japón",perUsd:150}
    ]
  };
}
function nf(n){var d=Math.abs(n)>=1?2:4;return n.toLocaleString("es-VE",{minimumFractionDigits:d,maximumFractionDigits:d})}
function hhmm(d){return d.toLocaleTimeString("es-VE",{hour:"2-digit",minute:"2-digit"})}
function hms(d){return d.toLocaleTimeString("es-VE",{hour:"2-digit",minute:"2-digit",second:"2-digit"})}
function rel(d){var s=Math.round((Date.now()-d)/1000);if(s<45)return "hace unos segundos";var m=Math.round(s/60);if(m<60)return "hace "+m+" min";var h=Math.round(m/60);if(h<24)return "hace "+h+" h";return "hace "+Math.round(h/24)+" d"}
function fresh(d){var m=(Date.now()-d)/60000;return m<10?"ok":m<240?"mid":"old"}
function chip(c,p){
  var d=c-p;if(Math.abs(d)<0.005)return '<span class="chip">Sin cambios</span>';
  var up=d>0;
  return '<span class="chip '+(up?"up":"down")+'">'+(up?"▲ ":"▼ ")+nf(Math.abs(d))+" ("+Math.abs(d/p*100).toLocaleString("es-VE",{maximumFractionDigits:1})+" %)</span>";
}
function tile(id,title,sub,body,at,note){
  $(id).innerHTML='<div class="th"><span class="dot '+fresh(at)+'"></span><h2>'+title+'</h2><span class="src">'+sub+'</span></div>'+body+'<p class="tf">'+note+' a las '+hhmm(at)+', '+rel(at)+'</p>';
}
function avg(o){var v=[o.buy,o.sell].filter(function(x){return x!=null});return v.length?v.reduce(function(a,c){return a+c},0)/v.length:null}
function bs(x){return x==null?"—":"Bs "+nf(x)}
function usdtAvg(){var v=[avg(data.binance),avg(data.okx)].filter(function(x){return x!==null});return v.length?v.reduce(function(a,c){return a+c},0)/v.length:null}
function usdtTile(id,title,o){
  var a=avg(o);
  if(a===null){tile(id,title,"P2P",'<p class="big">—</p><p class="lab">No disponible ahora. Se reintenta al actualizar.</p>',o.at,"Último intento");return}
  tile(id,title,"P2P",'<p class="lab">Promedio</p><p class="big">Bs '+nf(a)+'</p>'+chip(a,o.prevAvg)+pair(o),o.at,"Actualizado");
}
function pair(o){return '<div class="pr"><span>Compra</span><b>'+bs(o.buy)+'</b></div><div class="pr"><span>Venta</span><b>'+bs(o.sell)+'</b></div>'}

function renderRates(offlineNote){
  var b=data.bcv,bn=data.binance,ok=data.okx;
  tile("t-usd","Dólar BCV","Tasa oficial",'<p class="big">Bs '+nf(b.usd)+'</p>'+chip(b.usd,b.prevUsd),b.at,"Verificado");
  tile("t-eur","Euro BCV","Tasa oficial",'<p class="big">Bs '+nf(b.eur)+'</p>'+chip(b.eur,b.prevEur),b.at,"Verificado");
  usdtTile("t-bin","USDT en Binance",bn);
  usdtTile("t-okx","USDT en OKX",ok);
  var pa=data.paralelo,pg=(pa.usd-b.usd)/b.usd*100;
  tile("t-par","Dólar paralelo","Mercado paralelo",'<p class="big">Bs '+nf(pa.usd)+'</p>'+chip(pa.usd,pa.prevUsd)+'<div class="pr"><span>Brecha con el BCV</span><b>'+(pg>=0?"+":"")+pg.toLocaleString("es-VE",{maximumFractionDigits:1})+' %</b></div>',pa.at,"Actualizado");
  var lastTime = hhmm(new Date(Math.max(bn.at,ok.at,b.at,pa.at)));
  $("stamp").textContent = offlineNote ? "Sin internet • Mostrando últimas tasas guardadas ("+lastTime+")." : "Última actualización a las "+lastTime+".";
  renderLog();
}
function renderLog(){
  $("log").innerHTML=log.map(function(e){
    return '<div class="row"><div>'+e.src+'<small>'+hms(e.at)+', '+rel(e.at)+'</small></div><div class="v">Bs '+nf(e.val)+' '+chip(e.val,e.val-e.delta)+'</div></div>';
  }).join("");
}
function build(offlineNote){
  var b=data.bcv,bn=avg(data.binance),ok=avg(data.okx),usdt=usdtAvg();
  items=[
    {id:"VES",name:"Bolívar (Bs)",ves:1},
    {id:"USD",name:"Dólar BCV",ves:b.usd},
    {id:"EUR",name:"Euro BCV",ves:b.eur},
    {id:"PAR",name:"Dólar paralelo",ves:data.paralelo.usd},
    {id:"USDTB",name:"USDT Binance",ves:bn},
    {id:"USDTO",name:"USDT OKX",ves:ok}
  ];
  data.world.forEach(function(w){items.push({id:w.code,name:w.name,ves:b.usd/w.perUsd})});
  items=items.filter(function(i){return i.ves!==null});
  var ref=usdt!==null?usdt:data.paralelo.usd;
  var gap=(ref-b.usd)/b.usd*100;
  $("gapnum").textContent=(gap>=0?"+":"")+gap.toLocaleString("es-VE",{maximumFractionDigits:1})+" %";
  $("gaptxt").textContent="El "+(usdt!==null?"USDT":"dólar paralelo")+" cuesta Bs "+nf(Math.abs(ref-b.usd))+(ref>=b.usd?" más":" menos")+" que el dólar oficial.";
  var max=Math.max(ref,b.usd,b.eur,data.paralelo.usd);
  [["1",b.usd],["3",b.eur],["4",data.paralelo.usd],["2",usdt]].forEach(function(p){$("b"+p[0]).textContent=p[1]===null?"No disponible":"Bs "+nf(p[1]);$("f"+p[0]).style.width=(p[1]===null?0:p[1]/max*100)+"%"});
  ["from","to"].forEach(function(id){
    var sel=$(id),keep=sel.value;sel.textContent="";
    items.forEach(function(it){var o=document.createElement("option");o.value=it.id;o.textContent=(it.id.indexOf("USDT")===0||it.id==="PAR")?it.name:it.id+" "+it.name;sel.appendChild(o)});
    sel.value=keep||(id==="from"?"USD":"VES");
  });
  renderRates(offlineNote);convert();renderWorld();
}
function short(id){return id.indexOf("USDT")===0?"USDT":id==="PAR"?"USD paralelo":id}
function convert(){
  var a=parseFloat($("amt").value)||0;
  var f=items.filter(function(i){return i.id===$("from").value})[0],t=items.filter(function(i){return i.id===$("to").value})[0];
  if(!f||!t)return;
  $("result").innerHTML=nf(a*f.ves/t.ves)+" "+short(t.id)+"<small>1 "+short(f.id)+" = "+nf(f.ves/t.ves)+" "+short(t.id)+"</small>";
}
function renderWorld(){
  var q=$("q").value.trim().toLowerCase(),box=$("world");
  var list=data.world.filter(function(w){return !q||(w.code+" "+w.name+" "+w.country).toLowerCase().indexOf(q)>-1});
  box.innerHTML=list.length?list.map(function(w){
    return '<div class="row"><div>'+w.name+'<small>'+w.country+', '+w.code+'</small></div><div class="v">1 '+w.code+' = Bs '+nf(data.bcv.usd/w.perUsd)+'</div></div>';
  }).join(""):'<p class="note">No hay resultados. Prueba con el código de la moneda, por ejemplo COP.</p>';
}

/* Calculadora */
var cur="0",acc=null,op=null,fr=true,ex="";
var SYM={"+":"+","-":"−","*":"×","/":"÷"};
function cfmt(s){
  if(s==="Error")return s;
  var neg=s[0]==="-",t=neg?s.slice(1):s,p=t.split(".");
  var i=isFinite(p[0])?parseInt(p[0],10).toLocaleString("es-VE"):p[0];
  return (neg?"-":"")+i+(p.length>1?","+p[1]:"");
}
function cshow(){$("cdisp").textContent=cfmt(cur);$("cexpr").textContent=ex}
function calc(a,o,b){
  var r=o==="+"?a+b:o==="-"?a-b:o==="*"?a*b:b===0?NaN:a/b;
  return isNaN(r)?null:parseFloat(r.toPrecision(12));
}
function press(k){
  if(/^[0-9]$/.test(k)){if(fr){cur=k;fr=false}else if(cur.replace(/[-.]/g,"").length<12)cur=cur==="0"?k:cur+k}
  else if(k==="."){if(fr){cur="0.";fr=false}else if(cur.indexOf(".")<0)cur+="."}
  else if(SYM[k]){
    if(op&&!fr){var r=calc(acc,op,parseFloat(cur));if(r===null){cur="0";ex="No se puede dividir entre 0";op=null;acc=null;fr=true;return cshow()}cur=String(r)}
    acc=parseFloat(cur);op=k;fr=true;ex=cfmt(String(acc))+" "+SYM[k];
  }
  else if(k==="="){
    if(op){var b=parseFloat(cur),r2=calc(acc,op,b);
      if(r2===null){cur="0";ex="No se puede dividir entre 0"}else{ex=cfmt(String(acc))+" "+SYM[op]+" "+cfmt(String(b))+" =";cur=String(r2)}
      op=null;acc=null;fr=true}
  }
  else if(k==="C"){cur="0";acc=null;op=null;fr=true;ex=""}
  else if(k==="B"){if(!fr&&cur.length>1)cur=cur.slice(0,-1);else{cur="0";fr=true}if(cur==="-")cur="0"}
  else if(k==="N"){if(cur!=="0")cur=cur[0]==="-"?cur.slice(1):"-"+cur}
  else if(k==="%"){cur=String(parseFloat((parseFloat(cur)/100).toPrecision(12)));fr=true}
  cshow();
}
var KEYS=[["C","C"],["⌫","B"],["%","%"],["÷","/"],["7","7"],["8","8"],["9","9"],["×","*"],["4","4"],["5","5"],["6","6"],["−","-"],["1","1"],["2","2"],["3","3"],["+","+"],["±","N"],["0","0"],[",","."],["=","="]];
KEYS.forEach(function(k){
  var b=document.createElement("button");b.type="button";b.textContent=k[0];
  if("/*-+".indexOf(k[1])>-1||k[1]==="%")b.className="op";
  if(k[1]==="=")b.className="eq";
  b.addEventListener("click",function(){press(k[1])});
  $("keys").appendChild(b);
});
[["Dólar BCV",function(){return data.bcv.usd}],["Dólar paralelo",function(){return data.paralelo.usd}],["Euro BCV",function(){return data.bcv.eur}],["USDT",function(){return usdtAvg()}]].forEach(function(q){
  var b=document.createElement("button");b.type="button";b.textContent="Usar "+q[0];
  b.addEventListener("click",function(){var v=q[1]();if(v==null)return;cur=String(parseFloat(v.toFixed(4)));fr=true;cshow()});
  $("quick").appendChild(b);
});

var live=false;
function dt(s,f){var d=s?new Date(s):null;return d&&!isNaN(d)?d:f}
function track(k,v){
  if(v==null)return null;
  var s={};try{s=JSON.parse(localStorage.getItem("tv_"+k)||"{}")}catch(e){}
  if(s.last!==undefined&&s.last!==v)s.prev=s.last;
  s.last=v;try{localStorage.setItem("tv_"+k,JSON.stringify(s))}catch(e){}
  return s.prev!==undefined?s.prev:v;
}
function addLog(src,val,prev,at,nodup){
  if(val==null)return;
  var last=log.filter(function(x){return x.src===src})[0];
  if(last&&last.val===val&&(nodup||+last.at===+at))return;
  log.unshift({at:at,src:src,val:val,delta:val-(prev==null?val:prev)});
}
function applyPayload(j,offlineNote){
  var up=dt(j.updated,new Date()),bu=j.bcv&&j.bcv.usd,be=j.bcv&&j.bcv.eur,pa=j.paralelo;
  if(!bu||!bu.value||!be||!be.value||!pa||!pa.value)throw new Error("faltan tasas");
  var bd=dt(bu.at,up);
  var nd={
    bcv:{usd:bu.value,eur:be.value,prevUsd:track("usd",bu.value),prevEur:track("eur",be.value),at:up},
    paralelo:{usd:pa.value,prevUsd:track("par",pa.value),at:dt(pa.at,up)},
    binance:{buy:j.binance?j.binance.buy:null,sell:j.binance?j.binance.sell:null,at:up},
    okx:{buy:j.okx?j.okx.buy:null,sell:j.okx?j.okx.sell:null,at:up},
    world:data.world.map(function(w){var r=j.world&&j.world.rates&&j.world.rates[w.code];return {code:w.code,name:w.name,country:w.country,perUsd:r||w.perUsd}})
  };
  if(live||hasCached){["binance","okx"].forEach(function(k){if(avg(nd[k])===null&&avg(data[k])!==null)nd[k]=data[k]})}
  nd.binance.prevAvg=track("bin",avg(nd.binance));
  nd.okx.prevAvg=track("okx",avg(nd.okx));
  var ds=bd.toLocaleDateString("es-VE",{day:"2-digit",month:"2-digit"});
  addLog("Dólar BCV, tasa del "+ds,nd.bcv.usd,nd.bcv.prevUsd,up,true);
  addLog("Euro BCV, tasa del "+ds,nd.bcv.eur,nd.bcv.prevEur,up,true);
  addLog("Dólar paralelo",nd.paralelo.usd,nd.paralelo.prevUsd,nd.paralelo.at,true);
  addLog("USDT OKX",avg(nd.okx),nd.okx.prevAvg,nd.okx.at,false);
  addLog("USDT Binance",avg(nd.binance),nd.binance.prevAvg,nd.binance.at,false);
  log.length=Math.min(log.length,20);
  data=nd;build(offlineNote);
}
function loadCache(){
  try{
    var raw=localStorage.getItem("tv_cached_api");
    if(!raw)return false;
    applyPayload(JSON.parse(raw),true);
    hasCached=true;
    return true;
  }catch(e){return false}
}
function loadReal(){
  var btn=$("refresh");btn.disabled=true;
  return fetch(API_URL).then(function(r){if(!r.ok)throw new Error("HTTP "+r.status);return r.json()}).then(function(j){
    try{localStorage.setItem("tv_cached_api",JSON.stringify(j))}catch(e){}
    applyPayload(j,false);
    live=true;hasCached=true;
  }).catch(function(){
    if(live||hasCached){renderRates(true)}
    else{$("stamp").textContent="Sin conexión. Conéctate a internet una vez para descargar las tasas."}
  }).then(function(){btn.disabled=false});
}

$("amt").addEventListener("input",convert);
$("from").addEventListener("change",convert);
$("to").addEventListener("change",convert);
$("q").addEventListener("input",renderWorld);
$("swap").addEventListener("click",function(){var a=$("from").value;$("from").value=$("to").value;$("to").value=a;convert()});
$("refresh").addEventListener("click",function(){loadReal()});
$("theme").addEventListener("click",function(){
  var r=document.documentElement,c=r.dataset.theme||(matchMedia("(prefers-color-scheme: dark)").matches?"dark":"light");
  r.dataset.theme=c==="dark"?"light":"dark";
});
window.addEventListener("online",function(){loadReal()});
setInterval(function(){if(live||hasCached)renderRates(!live)},20000);

data=makeSample();log=[];cshow();
if(!loadCache()){build(false)}
loadReal();
setInterval(function(){loadReal()},60000);
</script>
</body>
</html>
""";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        webView = new WebView(this);
        webView.setLayoutParams(new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setContentView(webView);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);

        webView.setBackgroundColor(0xFF0B1520);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(
            "https://tasas-ve-api.rafael-f60.workers.dev/",
            HTML,
            "text/html",
            "UTF-8",
            null
        );
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
