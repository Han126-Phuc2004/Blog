/* ========= helpers ========= */
const $ = (sel, ctx=document) => ctx.querySelector(sel);
const $$ = (sel, ctx=document) => Array.from(ctx.querySelectorAll(sel));
const debounce = (fn, d=120) => { let t; return (...a)=>{ clearTimeout(t); t=setTimeout(()=>fn(...a),d); }; };

/* ========= active nav by URL ========= */
function setActiveNav(){
  const path = location.pathname.replace(/\/+$/,'') || '/';
  $$('.navbar .nav-link').forEach(a=>{
    const href = a.getAttribute('href') || a.getAttribute('th:href') || '';
    // simple match by known paths
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
  const toggle = () => {
    if (window.scrollY > 400) { btn.style.display='flex'; }
    else { btn.style.display='none'; }
  };
  window.addEventListener('scroll', debounce(toggle, 50));
  toggle();
  btn.addEventListener('click', (e)=>{
    e.preventDefault();
    window.scrollTo({top:0, behavior:'smooth'});
  });
}

/* ========= reveal on scroll (cards/sections) ========= */
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

/* ========= truncate helpers (client-side) ========= */
function clampText(){
  $$('[data-truncate]').forEach(el=>{
    const max = parseInt(el.getAttribute('data-truncate'),10) || 120;
    const full = el.getAttribute('data-full') || el.textContent.trim();
    el.setAttribute('data-full', full);
    if(full.length > max){
      el.textContent = full.slice(0, max-1).trim() + '…';
    } else {
      el.textContent = full;
    }
  });
}

/* ========= contact form validation (client) ========= */
function contactValidation(){
  const form = $('#contactForm');
  if(!form) return;
  const name = $('#contactName');
  const email = $('#contactEmail');
  const msg = $('#contactMsg');

  const showInvalid = (input, cond) => {
    if(cond){ input.classList.add('is-invalid'); }
    else{ input.classList.remove('is-invalid'); }
  };

  form.setAttribute('novalidate','novalidate');
  form.addEventListener('submit',(e)=>{
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    let invalid = false;
    showInvalid(name, !name.value.trim());
    showInvalid(email, !emailRe.test(email.value.trim()));
    showInvalid(msg, !msg.value.trim() || msg.value.trim().length < 10);
    invalid = $$('.is-invalid', form).length > 0;

    if(invalid){
      e.preventDefault();
      e.stopPropagation();
      // optional: toast
      console.warn('Contact form invalid');
    }
  });
}

/* ========= smooth anchor scroll (if any anchor links) ========= */
function smoothAnchors(){
  $$('a[href^="#"]').forEach(a=>{
    a.addEventListener('click', (e)=>{
      const id = a.getAttribute('href').slice(1);
      const target = document.getElementById(id);
      if(target){
        e.preventDefault();
        target.scrollIntoView({behavior:'smooth', block:'start'});
      }
    });
  });
}

/* ========= page init ========= */
document.addEventListener('DOMContentLoaded', ()=>{
  setActiveNav();
  backToTop();
  revealOnScroll();
  clampText();
  contactValidation();
  smoothAnchors();
});
