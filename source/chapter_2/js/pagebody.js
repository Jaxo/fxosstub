function revealPage(id) {
   var elt = document.getElementById(id);
   var nodeName = elt.nodeName; // expect "DIV" (.body)
   var siblings = elt.parentNode.children;
   for (var i=0; i < siblings.length; ++i) {
      var sib = siblings[i];
      if ((sib.nodeName === nodeName) && (sib != elt)) {
         sib.setAttribute("aria-expanded", "false");
      }
   }
   elt.setAttribute("aria-expanded", "true");
   var toolbarId = elt.getAttribute("aria-owns");
   if (toolbarId) showToolbar(toolbarId);
}
