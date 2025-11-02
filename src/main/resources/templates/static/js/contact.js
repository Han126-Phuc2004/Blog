function contactValidation(){
    const form = document.querySelector('#contactForm');
    if(!form) return;
  
    const name = form.querySelector('#contactName');
    const email = form.querySelector('#contactEmail');
    const msg = form.querySelector('#contactMsg');
    const emailRe = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  
    const showInvalid = (input, cond) => {
      input.classList.toggle('is-invalid', cond);
    };
  
    form.setAttribute('novalidate', 'novalidate');
    form.addEventListener('submit', e=>{
      showInvalid(name, !name.value.trim());
      showInvalid(email, !emailRe.test(email.value.trim()));
      showInvalid(msg, !msg.value.trim() || msg.value.trim().length < 10);
  
      const invalid = form.querySelectorAll('.is-invalid').length > 0;
      if(invalid){
        e.preventDefault();
        e.stopPropagation();
        console.warn('Contact form invalid');
      }
    });
  }
  
  document.addEventListener('DOMContentLoaded', contactValidation);
  