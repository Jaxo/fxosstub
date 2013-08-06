window.addEventListener(
   "load",
   function() {
      document.getElementById('menuaction_trigger').onclick = showActionMenu;
      document.querySelector(".menuaction menu + footer > button").onclick = hideActionMenu;
      var iconics = document.querySelectorAll("menuitem[icon]");
      for (var i=0, max=iconics.length; i < max; ++i) {
         var iconic = iconics[i];
         iconic.style.backgroundImage = (
            "url(\"" + iconic.getAttribute("icon") + "\")"
         );
      }
   }
);

function showActionMenu() {
   document.querySelector(".menuaction").style.display="block";
}

function hideActionMenu() {
   document.querySelector(".menuaction").style.display="none";
}

