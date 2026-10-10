import React, {useEffect, useRef, useState} from 'react';
import {createRoot} from 'react-dom/client';
import './style.css';
const Recognition=window.SpeechRecognition||window.webkitSpeechRecognition;
async function api(path,body){
 const response=await fetch('/api'+path,{method:body===undefined?'GET':'POST',headers:{'Content-Type':'application/json'},...(body===undefined?{}:{body:JSON.stringify(body)})});
 const data=await response.json();if(!response.ok)throw new Error(data.error||'The request could not be completed. Please try again.');return data;
}
function App(){
 const [orders,setOrders]=useState([]),[health,setHealth]=useState(null),[state,setState]=useState('Ready'),[error,setError]=useState(''),[messages,setMessages]=useState([]),[summary,setSummary]=useState(null),[input,setInput]=useState(''),[active,setActive]=useState(false),[mic,setMic]=useState(false),[tools,setTools]=useState([]);
 const call=useRef(null),rec=useRef(null),busy=useRef(false),ending=useRef(false),listenWanted=useRef(false),phase=useRef('Ready'),timer=useRef(null),bottom=useRef(null),sendRef=useRef(null),utterance=useRef(null);
 function status(s){phase.current=s;setState(s);}
 useEffect(()=>{Promise.all([api('/health'),api('/orders/samples')]).then(([h,o])=>{setHealth(h);setOrders(o);}).catch(e=>setError(e.message));return()=>{listenWanted.current=false;clearTimeout(timer.current);rec.current?.abort();window.speechSynthesis?.cancel();};},[]);
 useEffect(()=>{bottom.current?.scrollIntoView({behavior:'smooth',block:'nearest'});},[messages]);
 function stopRecognition(){clearTimeout(timer.current);const r=rec.current;rec.current=null;r?.abort();}
 function listen(){
  if(!call.current||busy.current||ending.current||!listenWanted.current||!Recognition)return;
  stopRecognition();const r=new Recognition();rec.current=r;r.lang='en-IN';r.interimResults=false;r.continuous=false;
  r.onstart=()=>status('Listening');
  r.onresult=e=>{const text=e.results[0][0].transcript.trim();if(text)sendRef.current(text);};
  r.onerror=e=>{if(e.error==='aborted')return;if(e.error==='no-speech'){setError('I didn’t hear anything. Please speak again.');return;}listenWanted.current=false;setMic(false);status('Ready');setError(e.error==='not-allowed'?'Microphone permission was denied. Allow it in browser settings, then choose Resume microphone.':'Speech recognition stopped. Check your microphone and connection, then resume.');};
  r.onend=()=>{if(rec.current===r)rec.current=null;if(listenWanted.current&&call.current&&!busy.current&&!ending.current&&phase.current!=='Speaking'){timer.current=setTimeout(listen,350);}};
  try{r.start();}catch{listenWanted.current=false;setMic(false);setError('Could not start the microphone. Try Resume microphone.');status('Ready');}
 }
 function speak(text){
  stopRecognition();
  if(!window.speechSynthesis){setError('Spoken playback is unavailable in this browser. Responses remain visible below.');status('Ready');listen();return;}
  window.speechSynthesis.cancel();status('Speaking');const u=new SpeechSynthesisUtterance(text);utterance.current=u;u.lang='en-IN';u.rate=1;
  const voices=window.speechSynthesis.getVoices();u.voice=voices.find(v=>v.lang==='en-IN')||voices.find(v=>v.lang.startsWith('en'))||null;
  const done=()=>{if(utterance.current!==u)return;utterance.current=null;if(call.current&&!ending.current){status('Ready');listen();}};
  u.onend=done;u.onerror=()=>{if(!ending.current)setError('Audio playback stopped. You can read the response below.');done();};window.speechSynthesis.speak(u);
 }
 async function start(){
  setError('');setSummary(null);setMessages([]);setTools([]);status('Connecting');
  try{const data=await api('/calls',{});call.current=data.callId;ending.current=false;setActive(true);listenWanted.current=!!Recognition;setMic(!!Recognition);setMessages([{role:'agent',text:data.greeting}]);speak(data.greeting);}
  catch(e){setError(e.message);status('Ready');}
 }
 async function send(text){
  if(!call.current||busy.current||ending.current||!text.trim())return;
  busy.current=true;stopRecognition();utterance.current=null;window.speechSynthesis?.cancel();status('Thinking');setError('');setInput('');setMessages(m=>[...m,{role:'customer',text}]);
  try{const result=await api('/chat',{callId:call.current,message:text});setMessages(result.transcript);setTools(t=>[...t,...result.tool_calls]);busy.current=false;if(!ending.current)speak(result.reply);}
  catch(e){busy.current=false;setError(e.message);listenWanted.current=false;setMic(false);if(!ending.current)status('Ready');}
 }
 sendRef.current=send;
 async function end(){
  if(!call.current||ending.current)return;ending.current=true;listenWanted.current=false;setMic(false);stopRecognition();utterance.current=null;window.speechSynthesis?.cancel();status('Ending');
  try{const result=await api('/calls/'+call.current+'/end',{});setMessages(result.transcript);setSummary(result.summary);call.current=null;setActive(false);status('Complete');}
  catch(e){setError(e.message);status('Ready');}finally{ending.current=false;}
 }
 function toggleMic(){if(listenWanted.current){listenWanted.current=false;setMic(false);stopRecognition();if(phase.current==='Listening')status('Ready');}else{listenWanted.current=true;setMic(true);setError('');if(phase.current!=='Speaking')listen();}}
 function download(){const blob=new Blob([JSON.stringify({transcript:messages,summary},null,2)],{type:'application/json'});const url=URL.createObjectURL(blob);const a=document.createElement('a');a.href=url;a.download='aura-call.json';a.click();URL.revokeObjectURL(url);}
 return <div className="shell">
  <header><a className="brand" href="/">aura<span>SKINCARE</span></a><span className="header-note">Customer care / Voice concierge</span></header>
  <main><section className="call-panel"><div className="eyebrow">A LITTLE CARE, A REAL CONVERSATION</div><h1>Talk to Aria.</h1><p className="intro">Questions about your order, delivery or returns? Let’s talk.</p>
   <div className={'voice-disc '+state.toLowerCase()} aria-hidden="true"><span>A</span><div className="bars">{[0,1,2,3,4].map(i=><i key={i}/>)}</div></div>
   <div className="agent-name">Aria <span>from Aura Skincare</span></div><div className="state" role="status" aria-live="polite"><i/>{state}</div>
   <div className="actions">{!active?<button className="primary" disabled={state==='Connecting'||health?.aiConfigured!==true} onClick={start}>{state==='Connecting'?'Connecting…':'Start call'} <span>↗</span></button>:<><button className="end" disabled={state==='Ending'} onClick={end}>{state==='Ending'?'Preparing summary…':'End call'}</button>{Recognition&&<button className="secondary" disabled={state==='Ending'||state==='Thinking'} onClick={toggleMic}>{mic?'Pause microphone':'Resume microphone'}</button>}</>}</div>
   <p className="small">Allow microphone access when prompted. Use headphones for clearer audio.</p>
   {!Recognition&&<p className="notice">Voice recognition is unavailable here. Try a supported desktop browser; text chat is available during a call.</p>}
   {health&&!health.aiConfigured&&<p className="notice">AI is not configured. Set GEMINI_API_KEY on the server, restart it and refresh this page.</p>}
   {error&&<p className="error" role="alert">{error}</p>}
   <div className="policy-note"><b>Care, with clarity.</b><p>Aria can check order details and explain policies. Cancellations, refunds and replacements are not performed in this demo.</p></div>
  </section>
  <section className="workspace"><div className="section-heading"><h2>Your conversation</h2><span>ENGLISH · INDIA</span></div><div className="transcript" aria-live="polite" aria-label="Call transcript">{messages.length===0?<div className="empty"><div className="quote">“</div><h3>We’re here to listen.</h3><p>Start a call and ask, “Where is order ORD-101?”</p></div>:messages.map((m,i)=><div key={i} className={'message '+m.role}><b>{m.role==='agent'?'ARIA':'YOU'}</b><p>{m.text}</p></div>)}<div ref={bottom}/></div>
   <form onSubmit={e=>{e.preventDefault();send(input);}}><label className="sr-only" htmlFor="message">Type your message</label><input id="message" maxLength={2000} value={input} onChange={e=>setInput(e.target.value)} placeholder={active?'Or type your message…':'Start a call to speak or type'} disabled={!active||state==='Ending'}/><button aria-label="Send message" disabled={!active||state==='Thinking'||state==='Ending'||!input.trim()}>Send</button></form>
   {tools.length>0&&<details className="trace"><summary>{tools.length} order lookup{tools.length===1?'':'s'} performed</summary><pre>{JSON.stringify(tools,null,2)}</pre></details>}
   {summary&&<section className="summary"><div className="section-heading"><h2>Call outcome</h2><button onClick={download}>Download JSON</button></div><pre>{JSON.stringify(summary,null,2)}</pre></section>}
  </section></main>
  <section className="orders"><div className="section-heading"><h2>Try a sample order</h2><span>FICTIONAL TEST DATA</span></div><div className="order-grid">{orders.map(o=><article key={o.order_id}><div className="order-title"><b>{o.order_id}</b><span className={'badge '+o.status.replaceAll(' ','-').toLowerCase()}>{o.status}</span></div><h3>{o.customer}</h3><p>{o.product}</p><div className="order-meta"><b>₹{o.value}</b><span>{o.courier}{o.tracking?' · '+o.tracking:''}</span></div><p className="order-note">{o.notes}</p></article>)}</div></section>
  <footer><span>Aura Skincare · Assessment project</span><span>Calls are kept in server memory for up to one hour. Speech and messages may be processed by external providers.</span></footer>
 </div>;
}
createRoot(document.getElementById('root')).render(<App/>);
