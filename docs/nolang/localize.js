function localize(elt) {
   if (elt != null) {
      for (var children=elt.children, i=0, max=children.length; i < max; ++i) {
         var child = children[i];
         if (child.selected) {
            var loc = window.location.href;
            var b = loc.indexOf("/docs/") + 6;
            var e = loc.indexOf("/", b);
            window.location.href = (
               loc.substring(0, b) + child.value + loc.substring(e)
            );
//          elt.form.action = loc.substring(0, b) + child.value + loc.substring(e);
//          elt.form.submit();
            break;
         }
      }
   }
}
