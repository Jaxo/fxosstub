window.addEventListener(
   "load",
   function() {
      var revealers = document.querySelectorAll(
         "footer.toolbar > menu > li[aria-controls]"
      );
      for (var i=0, max=revealers.length; i < max; ++i) {
         revealers[i].addEventListener(
            "click",
            function() { revealPage(this.getAttribute("aria-controls")); }
         );
      }
   }
);
function showToolbar(barId) {
   var menus = document.querySelectorAll("footer.toolbar > menu");
   var container = menus[0].parentNode;
   container.style.display = "none";
   for (max=menus.length, i=0; i < max; ++i) {
      var menu = menus[i];
      if (menu.id === barId) {
         container.style.display = "";
         menu.setAttribute("aria-expanded", "true");
      }else {
         menu.setAttribute("aria-expanded", "false");
      }
   }
}
