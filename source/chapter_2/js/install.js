window.addEventListener(
   "load",
   function() {
      var buttonElt = document.getElementById("install");
      dispatcher.on(
         "z_install_changed",
         function action(state, info) {
            buttonElt.style.display = (
               (state == "z_uninstalled")? "table-cell" : "none"
            );
            if (state == "z_failed") {
               alert(
                 "Install failed: " + info.name +
                 (info.message? ("\n" + info.message) : "")
               );
            }
         }
      );
      dispatcher.on(
         "z_install_forIOS",
         function action(state) {
            buttonElt.style.display = "none";
            alert("To install, press the forward arrow in Safari and tap \"Add to Home Screen\"");
         }
      );
      var install = new Install();
      buttonElt.addEventListener("click", function() { install.doIt(); });
   }
);

function Install() {
   this.doIt = function() {};
   if (navigator.mozApps) {
      var request = navigator.mozApps.getSelf();
      var that = this;
      request.onsuccess = function () {
         if (
            request.result
            // work-around for a bug in Firefox OS Desktop
            || (
               window.location.href.startsWith("http://") &&
               !window.locationbar.visible
            )
         ) {
            // if  we already run as OWA, then nothing to be done
            dispatcher.post("z_install_changed", "z_installed", request.result);
         }else {

            // we are not running as OWA.
            // Don't bother.  Assume this app was not installed.
            that.doIt = mozInstall;
            dispatcher.post("z_install_changed", "z_uninstalled");
         }
      };
      request.onerror = function() {
         dispatcher.post("z_install_changed", "z_failed", req1.error);
      };
   }else if ((typeof chrome !== "undefined") && chrome.webstore && chrome.app) {
      if (chrome.app.isInstalled) {
         dispatcher.post("z_install_changed", "z_installed");
      }else {
         this.doIt = function() {
            chrome.webstore.install(
               null,
               function() {
                  dispatcher.post("z_install_changed", "z_installed");
               },
               function(error) {
                  dispatcher.post("z_install_changed", "z_failed", error);
               }
            );
         };
         dispatcher.post("z_install_changed", "z_uninstalled");
      }
   }else if (typeof window.navigator.standalone !== "undefined") {
      if (window.navigator.standalone) {
         dispatcher.post("z_install_changed", "z_installed");
      }else {
         /*
         | Right now, just asks that something show a UI element mentioning
         | how to install using Safari's "Add to Home Screen" button.
         */
         this.doIt = function() {
            dispatcher.post("z_install_forIOS", navigator.platform.toLowerCase());
         };
         dispatcher.post("z_install_changed", "z_uninstalled");
      }
   }else {
      dispatcher.post("z_install_changed", "z_unsupported");
   }

   function mozInstall() {
      try {
         /*
         | we are not running as OWA, but this does NOT mean that the app
         | (or another app from same origin) is not installed...
         | REINSTALL_FORBIDDEN
         */
         var here = window.location;
         var there = document.createElement('a');
         var req2 = navigator.mozApps.getInstalled();
         req2.onsuccess = function() {
            var max = req2.result.length;
            var apps = req2.result;
            for (var i=0; i < max; ++i) {
               there.href = apps[i].manifestURL;
               if (
                  (here.hostname == there.hostname) &&
                  (here.port == there.port) &&
                  (here.protocol == there.protocol)
               ) {
                  var manifest = apps[i].manifest;
                  dispatcher.post(
                     "z_install_changed",
                     "z_failed", {
                        "name": "SAME_ORIGIN_CONFLICT",
                        "message":
                        "Conflict with \"" + manifest.name +
                        "\" v." + manifest.version +
                        " from same origin.\n\"" +
                        manifest.description +
                        "\"\nVersion:" + manifest.version +
                        "\nFrom:" + manifest.developer.name +
                        "\nInstalled:" + new Date(apps[i].installTime) +
                        "\nLast check:" + new Date(apps[i].lastUpdateCheck)
                     }
                  );
                  /*
                  Instructions for removing an App on Ubuntu:
                  cd ~/.local/share/applications
                  rm owa-http\;localhost\;8888.desktop
                  cd ~/.mozilla/firefox/jxyjgc2f.default/webapps
                  rm -r \{98b7d4fa-7b08-43ec-8ac7-db0794611942}
                  ** and remove the entry in webapps.json **
                  cd ~
                  rm -r  .http\;localhost\;8888/
                  ls ~/.http*
                  */

                  // var req4 = apps[i].checkForUpdate();
                  // req4.onsuccess = function() { alert("Success!"); };
                  // req4.onerror = function() { alert("Failure " + req3.error.name); }
                  return;
               }
            }
            // OK.  This app is brand new.  Try installing it.
            var req3 = navigator.mozApps.install(
               here.href.substring(0, here.href.lastIndexOf("/")) +
               "/manifest.webapp"
            );
            req3.onsuccess = function() {
               dispatcher.post("z_install_changed", "z_installed", req3.result);
            };
            req3.onerror = function() {
               dispatcher.post("z_install_changed", "z_failed", req3.error);
            };
         };
         req2.onerror = function() {
             dispatcher.post("z_install_changed", "z_failed", req2.error);
         };
      }catch (error) {
         dispatcher.post("z_install_changed", "z_failed", error);
      }
   }
}

