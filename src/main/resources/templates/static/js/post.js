document.addEventListener('DOMContentLoaded', ()=>{
    const header = document.querySelector('.post-header');
    if(header) header.classList.add('reveal');
  
    const comments = document.querySelectorAll('.comment');
    comments.forEach(c=>{
      c.classList.add('reveal');
    });
  });
  