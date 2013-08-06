window.addEventListener(
   "load",
   function() {
      document.getElementById('hd20').onclick = function() {
         revealPage("page0");
      };
      document.getElementById('hd21').onclick = function() {
         alert("Validated.")
         revealPage("page0");
      };
   }
);

