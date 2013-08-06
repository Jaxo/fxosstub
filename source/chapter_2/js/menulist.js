var htmlFontsize;
window.addEventListener(
   "load",
   function() {
      htmlFontsize = parseFloat(
         window.getComputedStyle(
            document.getElementsByTagName("html")[0], null
         ).getPropertyValue('font-size')
      );
      var menus = document.querySelectorAll('div.menulist menu');
      for (var i=0, max=menus.length; i < max; ++i) {
         var menu = menus[i];
         menu.addEventListener("click", menuClicked);
         // the height must be explicitely set or the trans won't work
         if (menu.hasAttribute("aria-expanded")) {
            // assume that all menu ancestors are also expanded.
            menu.style.height = menu.scrollHeight + "px";
         }else {
            menu.style.height = "4rem";
         }
      }
   }
);
function menuClicked(event) {
   /* 3 is the collapsed menu size in rem */
   var collapsedHeight = 3 * htmlFontsize;
   for (var elt=event.target; elt; elt=elt.parentNode) {
      if (elt.nodeName === "MENU") {
         var delta = elt.scrollHeight - collapsedHeight;
         if (elt.hasAttribute("aria-expanded")) {
            delta = -delta;
            elt.style.height = "4rem";
            elt.addEventListener(
               "transitionend",
               function() {
                  this.removeAttribute("aria-expanded");
                  this.removeEventListener("transitionend", arguments.callee, true);
               },
               true
            );
         }else {
            elt.style.height = elt.scrollHeight + "px";
            elt.addEventListener(
               "transitionend",
               function() {
                  this.setAttribute("aria-expanded", true);
                  this.removeEventListener("transitionend", arguments.callee, true);
               },
               true
            );
         }
         while ((elt = elt.parentNode).className !== "menulist") {
            elt.style.height = (elt.clientHeight + delta) + "px";
         }
         break;
      }else if (elt.nodeName === "MENUITEM") {
         var group;
         if (
            (elt.getAttribute("type") === "radio") &&
            (group = elt.getAttribute("radiogroup"))
         ) {
            if (!elt.hasAttribute("checked")) {
               var radioItems = document.querySelectorAll(
                  'menuitem[radiogroup="' + group + '"]'
               );
               for (var i=0, max=radioItems.length; i < max; ++i) {
                  radioItems[i].removeAttribute("checked");
               }
               elt.setAttribute("checked", true);
            }
         }else {
            if (elt.hasAttribute("checked")) {
               elt.removeAttribute("checked");
            }else {
               elt.setAttribute("checked", true);
            }
         }
         break;
      }
   }
   event.stopPropagation();
}

