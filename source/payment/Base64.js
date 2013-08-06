/*
* (C) Copyright 2013 Jaxo, Inc.
* Released under Mozilla Public License 2.0 since 2013/04/01
*
* Author:  Pierre G. Richard
* Written: 11/30/1998 (Migrated from Base64.java on 04/17/2013)
*
* use the Yahoo Module Pattern to define:
* Base64.encode(string);
* Base64.decode(string);
* Base64.Url.encode(string);
* Base64.Url.decode(string);
*/
var Base64 = (
   function() {
      var xlate = new Uint8Array([
         65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
         81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101,
         102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
         115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53,
         54, 55, 56, 57, 43, 47
      ]);
      var unxlate = new Uint8Array([
         62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
         0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
         12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0,
         0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
         37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
      ]);
      var privateEncode = function(inString, xlate, isPadded) {
         var inBuf = unescape(encodeURIComponent(""+inString));
         var inLen = inBuf.length;
         var outLen = (((inLen << 2) / 3) + 3) & 0xFFFFFFFC;
         if (!isPadded) outLen -= (3 + (-inLen % 3)) % 3;
         var outBuf = new Uint8Array(outLen);
         var iTimes = -1;
         var b1;
         var b2 = 0;
         var outIx = 0;
         var res = "";
         for (var inIx=0; inIx < inLen; ++inIx) {
            b1 = inBuf.charCodeAt(inIx);
            switch (++iTimes) {
            case 0:
               b2 = b1>>2;
               break;
            case 1:
               b2 = ((b2 & 0x3) << 4) | (b1 >> 4);
               break;
            default: // case 2:
               outBuf[outIx++] = xlate[(b1>>6) | ((b2 & 0xF)<<2)];
               b2 = b1 & 0x3f;
               iTimes = -1;
               break;
            }
            outBuf[outIx++] = xlate[b2];
            b2 = b1;
         }
         switch (iTimes) {    // take care of orphans!
         case 0:
            outBuf[outIx++] = xlate[(b2 & 0x3)<<4];
            if (isPadded) {
               outBuf[outIx++] = 61;
               outBuf[outIx++] = 61;
            }
            break;
         case 1:
            outBuf[outIx++] = xlate[(b2 & 0xF)<<2];
            if (isPadded) outBuf[outIx++] = 61;
            break;
         default: // case 2 (really: -1)
            break;
         }
         return String.fromCharCode.apply(null, outBuf);
      };
      var privateDecode = function(inString, unxlate) {
         var inBuf = inString;
         var inLen = inBuf.length;
         var outLen = (inLen * 3) >> 2;
         while (inBuf[inLen-1] == '=') {
            --outLen;
            --inLen;
         }
         var outBuf = new Uint8Array(outLen);
         var outIx = 0;
         var iTimes = -1;
         var b2 = 0;

         for (var inIx=0; inIx < inLen; ++inIx) {
            var v = inBuf.charCodeAt(inIx) - 43;
            if (v >= 0) {               // skip CR, LF, TABS, etc...
               var b1 = unxlate[v];
               switch (++iTimes) {
               case 0:
                  break;
               case 1:
                  outBuf[outIx++] = (b2<<2) | (b1>>4);
                  break;
               case 2:
                  outBuf[outIx++] = (b2<<4) | (b1>>2);
                  break;
               default: // case 3:
                  outBuf[outIx++] = (b2<<6) | b1;
                  iTimes = -1;
                  break;
               }
               b2 = b1;
            }
         }
         return decodeURIComponent(escape(String.fromCharCode.apply(null, outBuf)));
      };
      return {
         Url: (
           function() {
              var xlateUrl = new Uint8Array([
                 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
                 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101,
                 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
                 115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53,
                 54, 55, 56, 57, 45, 95
              ]);
              var unxlateUrl = new Uint8Array([
                 0, 0, 62, 0, 0, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
                 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
                 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0,
                 0, 0, 0, 63, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
                 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
              ]);
              return {
                 encode: function(inString) {
                    return privateEncode(inString, xlateUrl, false);
                 },
                 decode: function(inString) {
                    return privateDecode(inString, unxlateUrl);
                 }
              };
           }
         )(),
         encode: function(inString) {
            return privateEncode(inString, xlate, true);
         },
         decode: function(inString) {
            return privateDecode(inString, unxlate);
         }
      };
   }
)();
