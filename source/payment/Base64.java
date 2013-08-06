/*
* $Id: Base64.java,v 1.7 2011-12-17 14:22:31 pgr Exp $
*
* (C) Copyright 2006-2011 Jaxo Systems.
*
* 11/30/1998 In com.jaxo.io.UUStreams, original
* 07/15/2006 In com.jaxo.midp.encoder, migration
* 12/17/2012 In com.jaxo.midp.encoder, last update
* 04/03/2013 In com.jaxo.googapp.jaxogram, modified to include Base64Url
* Mozilla Public License 2.0 since 2013/04/01
*
* Author:  Pierre G. Richard
* Written: 11/30/1998
*/
package com.jaxo.googapp.jaxogram;

/*-- class Base64 --+
*//**
*
* @author  Pierre G. Richard
* @version $Id: Base64.java,v 1.7 2011-12-17 14:22:31 pgr Exp $
*/
public class Base64
{
   private static final byte[] xlate = {
      65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
      81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101,
      102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
      115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53,
      54, 55, 56, 57, 43, 47
   };
   private static final byte[] unxlate = {
      62, 0, 0, 0, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
      0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
      12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0,
      0, 0, 0, 0, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
      37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
   };

   /*------------------------------------------------------------------encode-+
   *//**
   * Base64 encode an array of bytes
   *
   * @param inBuf array of bytes to encode
   * @return encoded bytes
   *//*
   +-------------------------------------------------------------------------*/
   public static byte[] encode(byte[] inBuf) {
      return encode(inBuf, xlate, true);
   }

   /*------------------------------------------------------------------decode-+
   *//**
   * Base64 decode an array of bytes
   *
   * @param inBuf bytes to decode
   * @return decoded bytes
   *//*
   +-------------------------------------------------------------------------*/
   public static byte[] decode(byte[] inBuf) {
      return decode(inBuf, unxlate);
   }

   public static class Url {  // e.g.: Base64.Url
      private static final byte[] xlateUrl = {
         65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
         81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 97, 98, 99, 100, 101,
         102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
         115, 116, 117, 118, 119, 120, 121, 122, 48, 49, 50, 51, 52, 53,
         54, 55, 56, 57, 45, 95
      };
      private static final byte[] unxlateUrl = {
         0, 0, 62, 0, 0, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61,
         0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11,
         12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0,
         0, 0, 0, 63, 0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36,
         37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51
      };
      /*---------------------------------------------------------------encode-+
      *//**
      * Base64URL encode an array of bytes
      *
      * @param inBuf bytes to encode
      * @return encoded bytes
      *//*
      +----------------------------------------------------------------------*/
      public static byte[] encode(byte[] inBuf) {
         return Base64.encode(inBuf, xlateUrl, false);
      }
      /*---------------------------------------------------------------decode-+
      *//**
      * Base64URL decode an array of bytes
      *
      * @param inBuf bytes to decode
      * @return decoded bytes
      *//*
      +----------------------------------------------------------------------*/
      public static byte[] decode(byte[] inBuf) {
         return Base64.decode(inBuf, unxlateUrl);
      }
   }

   /*------------------------------------------------------------------encode-+
   *//**
   * Base64 encode an array of bytes
   *
   * @param inBuf array of bytes to encode
   * @return encoded bytes
   *//*
   +-------------------------------------------------------------------------*/
   private static byte[] encode(byte[] inBuf, byte[] xlate, boolean isPadded)
   {
      int inLen = inBuf.length;
      int outLen = (((inLen << 2) / 3) + 3) & 0xFFFFFFFC;
      if (!isPadded) outLen -= (3 + (-inLen % 3)) % 3;
      byte[] outBuf = new byte[outLen];
      int iTimes = -1;
      int b1;
      int b2 = 0;
      int outIx=0;

      for (int inIx=0; inIx < inLen; ++inIx) {
         b1 = inBuf[inIx] & 0xFF;
         switch (++iTimes) {
         case 0:
            b2 = b1>>2;
            break;
         case 1:
            b2 = ((b2 & 0x3) << 4) | (b1 >> 4);
            break;
         default: // case 2:
            outBuf[outIx++] = xlate[(byte)((b1>>6) | ((b2 & 0xF)<<2))];
            b2 = b1 & 0x3f;
            iTimes = -1;
            break;
         }
         outBuf[outIx++] = xlate[(byte)b2];
         b2 = b1;
      }

      switch (iTimes) {    // take care of orphans!
      case 0:
         outBuf[outIx++] = xlate[(byte)((b2 & 0x3)<<4)];
         if (isPadded) {
            outBuf[outIx++] = (byte)'=';
            outBuf[outIx++] = (byte)'=';
         }
         break;
      case 1:
         outBuf[outIx++] = xlate[(byte)((b2 & 0xF)<<2)];
         if (isPadded) outBuf[outIx++] = (byte)'=';
         break;
      default: // case 2 (really: -1)
         break;
      }
      return outBuf;
   }

   /*------------------------------------------------------------------decode-+
   *//**
   * Base64 decode a String
   *
   * @param inBuf bytes to decode
   * @return decoded bytes
   *//*
   +-------------------------------------------------------------------------*/
   private static byte[] decode(byte[] inBuf, byte[] unxlate)
   {
      int inLen = inBuf.length;
      int outLen = (inLen * 3) >> 2;
      while (inBuf[inLen-1] == '=') {
         --outLen;
         --inLen;
      }
      byte[] outBuf = new byte[outLen];
      int outIx = 0;
      int iTimes = -1;
      byte b2 = 0;

      for (int inIx=0; inIx < inLen; ++inIx) {
         int v = inBuf[inIx] - 43;
         if (v >= 0) {               // skip CR, LF, TABS, etc...
            byte b1 = unxlate[v];
            switch (++iTimes) {
            case 0:
               break;
            case 1:
               outBuf[outIx++] = (byte)((b2<<2) | (b1>>4));
               break;
            case 2:
               outBuf[outIx++] = (byte)((b2<<4) | (b1>>2));
               break;
            default: // case 3:
               outBuf[outIx++] = (byte)((b2<<6) | b1);
               iTimes = -1;
               break;
            }
            b2 = b1;
         }
      }
      return outBuf;
   }
}
/*===========================================================================*/
