function editSourceable() {
   var elts = document.querySelectorAll(".sourceable");
   for (var i=0, max=elts.length; i < max; ++i) {
      var elt = elts[i];
      populateSources(elt, elt.getAttribute("src"));
   }
}

function populateSources(elt, file) {
   var xhr = new XMLHttpRequest();
// xhr.open("GET", "file:///home/pgr/fxos/fxosstub/docs/nolang/chapter_2/step1.html");
// xhr.open("GET", "../../nolang/chapter_2/step1.html");
   xhr.open("GET", file);
   xhr.onreadystatechange = function () {
      if ((this.readyState === 4) && ((this.status === 200) || (this.status === 0))) {
         var re3 = new RegExp(
            "\\s*<!-- REPLACED -->(.|\\n)*?<!-- REPLACEMENT((.|\\n)*?)REPLACE END -->\\s*",
            "g"
         );
         var re0 = new RegExp("\\s*<!--\\[(.*?)\\]-->", "g");
         var re1 = new RegExp("<", "g");
         var re2 = new RegExp("AAA", "g");
         var text = this.response.replace(re3, "$2");
         text = text.replace(re0, "AAA$1>");
         text = text.replace(re1, "&lt;");
         text = text.replace(re2, "<");
         elt.innerHTML = text;
//       elt.textContent = this.response;
      }
   };
   xhr.send();
}
