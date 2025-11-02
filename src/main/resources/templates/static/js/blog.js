/* ========= truncate helpers (client-side) ========= */
function clampText(){
    document.querySelectorAll('[data-truncate]').forEach(el=>{
      const max = parseInt(el.getAttribute('data-truncate'),10) || 120;
      const full = el.getAttribute('data-full') || el.textContent.trim();
      el.setAttribute('data-full', full);
      el.textContent = full.length > max ? full.slice(0,max-1).trim() + '…' : full;
    });
  }
  
  /* ========= init Blog ========= */
  document.addEventListener('DOMContentLoaded', ()=>{
    clampText();
  });
  