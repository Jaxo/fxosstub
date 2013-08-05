/*
| Dispatcher is a singleton to broadcast "events" to registered listeners.
| Events are identified by their name ("somethingHappened" in the example
| below.)
|
| - listener(s) declare their intent to receive an event by calling:
|      dispatcher.on(
|         "somethingHappened",
|         function myAction(arg1, arg2) {
|            alert(arg1, arg2);
|         }
|      }
|   myAction(arg1, arg2) will be called when the "somethingHappened" event
|   is posted.
|
| - the notifier calls:
|      dispatcher.post("somethingHappened", "whatHappened", "how");
|   to broadcast the event "somethingHappened" to the registered listeners
|
| - when a listener is no more interested in being notified, it calls:
|      dispatcher.off("somethingHappened", myAction);
|
*/
var dispatcher = (
   function () {
      var events = [];
      var dispatcher = {};
      dispatcher.post = function(name) {
         if (events[name]) {
            var args = Array.prototype.slice.call(arguments, 1);
            for (var i=0, l=events[name].length; i<l; ++i) {
               events[name][i].apply(this, args);
            }
         }
      };
      dispatcher.on = function(name, func) {
         events[name] = (events[name] || []).concat([func]);
      };
      dispatcher.off = function(name, func) {
         if (events[name]) {
            var newEvents = [];
            for (var i=0, l=events[name].length; i<l; ++i) {
               var f = events[name][i];
               if (f != func) newEvents.push();
            }
            events[name] = newEvents;
         }
      };
      dispatcher.clean = function() { events = []; };
      return dispatcher;
   }
)();
