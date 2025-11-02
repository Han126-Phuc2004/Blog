/* ========= helpers ========= */
const $ = (sel, ctx=document) => ctx.querySelector(sel);
const $$ = (sel, ctx=document) => Array.from(ctx.querySelectorAll(sel));
const debounce = (fn, d=120) => { let t; return (...a)=>{ clearTimeout(t); t=setTimeout(()=>fn(...a),d); }; };

/* ========= active nav by URL ========= */
function setActiveNav(){
  const path = location.pathname.replace(/\/+$/,'') || '/';
  $$('.navbar .nav-link').forEach(a=>{
    const href = a.getAttribute('href') || '';
    if ((path === '/' && href === '/')
      || (path.startsWith('/blog') && href.includes('/blog'))
      || (path.startsWith('/about') && href.includes('/about'))
      || (path.startsWith('/contact') && href.includes('/contact'))) {
      a.classList.add('active');
    } else {
      a.classList.remove('active');
    }
  });
}

/* ========= back to top ========= */
function backToTop(){
  const btn = $('#backToTop');
  if(!btn) return;
  const toggle = () => btn.style.display = window.scrollY > 400 ? 'flex' : 'none';
  window.addEventListener('scroll', debounce(toggle, 50));
  toggle();
  btn.addEventListener('click', e=>{
    e.preventDefault();
    window.scrollTo({top:0, behavior:'smooth'});
  });
}

/* ========= reveal on scroll ========= */
function revealOnScroll(){
  const els = $$('.reveal');
  if(!('IntersectionObserver' in window) || !els.length) {
    els.forEach(el=>el.classList.add('is-visible'));
    return;
  }
  const io = new IntersectionObserver(entries=>{
    entries.forEach(ent=>{
      if(ent.isIntersecting){ ent.target.classList.add('is-visible'); io.unobserve(ent.target); }
    });
  }, {threshold:.15});
  els.forEach(el=>io.observe(el));
}

/* ========= smooth anchor scroll ========= */
function smoothAnchors(){
  $$('a[href^="#"]').forEach(a=>{
    a.addEventListener('click', e=>{
      const id = a.getAttribute('href').slice(1);
      const target = document.getElementById(id);
      if(target){
        e.preventDefault();
        target.scrollIntoView({behavior:'smooth', block:'start'});
      }
    });
  });
}

/* ========= Init Layout ========= */
document.addEventListener('DOMContentLoaded', ()=>{
  setActiveNav();
  backToTop();
  revealOnScroll();
  smoothAnchors();
});
