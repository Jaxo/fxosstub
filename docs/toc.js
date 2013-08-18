function buildToc() {
   var toc = "";
   var elts = document.querySelectorAll("h1, h2, h3");
   var lvl = 0;
   for (var i=0, max=elts.length; i < max; ++i) {
      var elt = elts[i];
      var id = elt.id;
      var val = parseInt(elt.nodeName.substr(1));
      if (!id) {
         id = "hdr" + i;
         elt.id = id;
      }
      if (val > lvl) {
         do toc += "<ul>"; while (++lvl < val);
      }else if (val < lvl) {
         do toc += "</ul>"; while (--lvl > val);
      }
      toc += "<li><a href='#" +  id + "'>" + elt.textContent + "</a></li>";
   }
   while (lvl-- > 0) toc += "</ul>";
   toc += "<span id='toc_opener'>▼ </span>"
   var tocElt = document.getElementById("toc");
   tocElt.style.height = "16px";
   tocElt.innerHTML = toc;
   document.getElementById("toc_opener").onclick = function() {
      var tocStyle = this.parentNode.style;
      if (this.getAttribute("aria-expanded")) {
         this.removeAttribute("aria-expanded");
         this.textContent = "▼ ";
         tocStyle.height = "16px";
         this.style.top = "-4px";
      }else {
         this.setAttribute("aria-expanded", "true");
         this.textContent = "▲ ";
         tocStyle.height = (this.parentNode.scrollHeight + 20) + "px";
         this.style.top = "auto";
      }
   }
}
