function Install() {
   this.state = "idle";

   // trigger events registration
   var events = new Object();
   this.on = function(name, func) {
      events[name] = (events[name] || []).concat([func]);
   };
   this.off = function(name, func) {
      if (events[name]) {
         var res = [];
         for (var i=0, l=events[name].length; i<l; ++i) {
            var f = events[name][i];
            if (f != func) res.push();
         }
         events[name] = res;
      }
   };
   this.trigger = function(name) {
      var args = Array.prototype.slice.call(arguments, 1);
      if (events[name]) {
         for (var i=0, l=events[name].length; i<l; ++i) {
            events[name][i].apply(this, args);
         }
      }
   };
   this.triggerChange = function(state) {  // helper
      //var msg = "State: " + this.state + " -> " + state;
      //if (typeof this.error !== "undefined") msg += "\n" + this.error;
      //alert(msg);
      this.state = state;
      this.trigger("change", this.state);
   }

   if (navigator.mozApps) {
      var request = navigator.mozApps.getSelf();
      var that = this;
      request.onsuccess = function () {
         if (!this.result) {
            that.triggerChange("uninstalled");
            that.installUrl = (
               location.href.substring(0, location.href.lastIndexOf("/")) +
               "/manifest.webapp"
            );
            that.doIt = function() {
               //*/ alert("Faking install from " + that.installUrl);
               try {
                  var req2 = navigator.mozApps.install(that.installUrl);
                  req2.onsuccess = function(data) {
                     that.triggerChange("installed");
                     //*/ alert("Bingo!");
                  };
                  req2.onerror = function() {
                     that.error = this.error;
                     that.triggerChange("failed");
                  };
               }catch (error) {
                  that.error = error;
                  that.triggerChange("failed");
               }
            };
         }else {
            that.triggerChange("installed");
         }
      };
      request.onerror = function (error) {
         that.error = error;
         that.triggerChange("failed");
      };
   }else if ((typeof chrome !== "undefined") && chrome.webstore && chrome.app) {
      if (!chrome.app.isInstalled) {
         this.triggerChange("uninstalled");
         var that = this;
         this.doIt = function() {
            chrome.webstore.install(
               null,
               function () { that.triggerChange("installed"); },
               function (err) {
                  that.error = err;
                  that.triggerChange("failed");
               }
            );
         };
      }else {
         this.triggerChange("installed");
      }
   }else if (typeof window.navigator.standalone !== "undefined") {
      if (!window.navigator.standalone) {
         this.triggerChange("uninstalled");
         /*
         | Right now, just asks that something show a UI element mentioning
         | how to install using Safari's "Add to Home Screen" button.
         */
         this.doIt = function() {
            this.trigger("showiOSInstall", navigator.platform.toLowerCase());
         };
      }else {
         this.triggerChange("installed");
      }
   }else {
      this.triggerChange("unsupported");
   }
   return this;
}

function setInstallButton(buttonId) {
   if (!document.getElementById(buttonId)) {
      document.addEventListener("DOMContentLoaded", setInstallButton);
   }else {
      var install = new Install();
      var buttonElt = document.getElementById(buttonId);
      install.on(
         "change",
         function() {
            buttonElt.style.display = (
               (install.state == "uninstalled")? "table-cell" : "none"
            );
            if (install.state == "failed") {
               alert("Install failed:\n" + install.error);
            }
         }
      );
      install.on(
         "showiOSInstall",
         function() {
            alert(
               "To install, press the forward arrow in Safari " +
               "and touch \"Add to Home Screen\""
            );
         }
      );
      buttonElt.addEventListener(
         "click", function() { install.doIt(); }
      );
   }
}

function activatePage(sourceElt, target) {
   if (sourceElt != null) {
      var siblings = sourceElt.parentNode.childNodes;
      for (var i=0; i < siblings.length; ++i) {
         var sib = siblings[i];
         if (sib.nodeType == 1) {
            if (siblings[i] == sourceElt) {
               sib.setAttribute('class','activated');
            }else {
               sib.removeAttribute('class', '');
            }
         }
      }
   }
   window.location='#'+target;
}

function toggleSidebarView() {
   var btnMainStyle = document.getElementById('btnMainImage').style;
   var bodyStyle = document.getElementById('main').style;
   var sidebarStyle = document.getElementById('sidebar').style;
   if (bodyStyle.left != "80%") {
      btnMainStyle.backgroundImage = "url(style/images/back.png)";
      bodyStyle.left = "80%";
      bodyStyle.right = "-80%";
      sidebarStyle.left = "0%";
      sidebarStyle.right = "20%"; /* (100-80)% */
   }else {
      btnMainStyle.backgroundImage = "url(style/images/menu.png)";
      bodyStyle.left = "0%";
      bodyStyle.right = "0%";
      sidebarStyle.left = "-80%";
      sidebarStyle.right = "100%";
   }
}

function getAttribute(node, attrName) {  // helper
   var attrs = node.attributes;
   if ((attrs != null) && (attrs[attrName] != null)) {
      return attrs[attrName].value;
   }else {
      return null;
   }
}

function menuListClicked(event) {
   var liElt = event.target;        // the clicked <LI> (or its SPAN child)
   var ulChildElt, ulParentElt;
   switch (liElt.getAttribute("role")) {
   case "dropItem":
      ulChildElt = liElt.getElementsByTagName("UL")[0];
      liElt.setAttribute("role", "dropItem_open");
      if (ulChildElt != null) ulChildElt.style.display = "block";
      break;
   case "dropItem_open":
      ulChildElt = liElt.getElementsByTagName("UL")[0];
      liElt.setAttribute("role", "dropItem");
      if (ulChildElt != null) ulChildElt.style.display = "none";
      break;
   case "selectedItem":
      if (getAttribute(liElt.parentNode, "role") != "radioList") {
         // selecting a selected item in a radioList does nothing..
         // but, for lambda lists, it deselects the item
         liElt.removeAttribute("role");
      }
      break;
   default: // default role is "unselected"
      if (getAttribute(liElt.parentNode, "role") == "radioList") {
         // selecting a new item in a radioList deselects the others
         var otherLiNodes = liElt.parentNode.childNodes;
         for (var i=0; i < otherLiNodes.length; ++i) {
            if (getAttribute(otherLiNodes[i], "role") == "selectedItem") {
               otherLiNodes[i].attributes.removeNamedItem("role");
            }
         }
      }
      liElt.setAttribute("role", "selectedItem");
   }
}

