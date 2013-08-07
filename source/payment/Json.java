/*
* (C) Copyright 2013 Jaxo Inc.
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0.  If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/.
*
* Author:  Pierre G. Richard
* Written: 4/21/2013
*/
package com.jaxo.mozutil;
import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

/*-- class Json --+
*//**
* This class implements a JSON parser / generator (decode / encode).
* It's a strict interpretation of RFC 4627.
* Monolithic, lightweight, no rings and bells: good enough is good enough.
*
* @author  Pierre G. Richard
*/
public class Json
{
   /** A Json.Root is a Json.Object or a Json.Array */
   public static interface Root {
   }

   /** A Json.Object holds a list of Json.Members */
   public static class Object implements Root {
      public List<Member> members;
      Object() { members = new ArrayList<Member>(); }
   }

   /** A Json.Member is a pair of name, value
   * - a name is a Java String
   * - a value is a Java Object, out of:
   *   null, String, Long, Double, Boolean, Json.Object, Json.Array.
   */
   public static class Member extends AbstractMap.SimpleEntry<String, java.lang.Object> {
      private static final long serialVersionUID = 1L;
      Member(String name, java.lang.Object value) { super(name, value); }
   }

   /** A Json.Array holds a list of values
   *  A value is a Java Object, out of:
   *  null, String, Long, Double, Boolean, Json.Object, Json.Array.
   */
   public static class Array implements Root {
      public List<java.lang.Object> values;
      Array() { values = new Vector<java.lang.Object>(); }
   }

   /*-------------------------------------------------------------------parse-+
   *//**
   * Construct a Json.Root from reading JSON text off a Reader
   *
   * @param in Reader of the JSON text.
   * @return a Json.Root
   * @exception Exception if syntax errors were found
   *//*
   +-------------------------------------------------------------------------*/
   public static Root parse(Reader in) throws Exception {
      Tokenizer tokenizer = new Tokenizer(in);
      Root root;
      java.lang.Object token = tokenizer.nextToken();
      if (token == Tokenizer.BEGIN_OBJECT) {
         root = parseObject(tokenizer);
      }else if (token == Tokenizer.BEGIN_ARRAY) {
         root = parseArray(tokenizer);
      }else {
         throw tokenizer.new Exception("'[' or '{' expected");
      }
      if (tokenizer.nextToken() != Tokenizer.EOF) {
         throw tokenizer.new Exception("Extraneous data found");
      }
      return root;
   }

   /*----------------------------------------------------------------generate-+
   *//**
   * Generate JSON text from a Json.ROOT
   *
   * @param root the Json.Root to generate the JSON text from
   * @param out Writer for the resulting JSON Text.
   * @exception Exception if the Json.Root is invalid
   *//*
   +-------------------------------------------------------------------------*/
   public static void generate(Root root, Writer out) throws Exception {
      if (root instanceof Object) {
         generate((Object)root, out);
      }else if (root instanceof Array) {
         generate((Array)root, out);
      }else {
         throw new Exception("Not a JSON Root");
      }
   }

   private static class Tokenizer
   {
      private Reader m_in;
      private int m_chBack;
      private int m_ofs;
      public static final Character BEGIN_ARRAY = '[';
      public static final Character BEGIN_OBJECT = '{';
      public static final Character END_ARRAY = ']';
      public static final Character END_OBJECT = '}';
      public static final Character NAME_SEPARATOR = ':';
      public static final Character VALUE_SEPARATOR = ',';
      public static final Character EOF = '\uFFFF';

      public Tokenizer(Reader in) {
         m_chBack = -1;
         m_in = new FilterReader(in) {
            public int read() throws IOException {
               if (m_chBack != -1) {
                  int ch = m_chBack;
                  m_chBack = -1;
                  return ch;
               }else {
                  ++m_ofs;
                  return super.read();
               }
            }
         };
      }

      public java.lang.Object nextToken() throws Exception, IOException {
         int ch;
         while ((ch=m_in.read()) != -1) {
            switch (ch) {
            case ' ':
            case '\r':
            case '\n':
            case '\t':
               continue;
            case '[': return BEGIN_ARRAY;
            case ']': return END_ARRAY;
            case '{': return BEGIN_OBJECT;
            case '}': return END_OBJECT;
            case ':': return NAME_SEPARATOR;
            case ',': return VALUE_SEPARATOR;
            case 't':
               if ((m_in.read() == 'r') && (m_in.read() == 'u') && (m_in.read() == 'e')) {
                  return Boolean.TRUE;
               }
               break;
            case 'f':
               if ((m_in.read() == 'a') &&  (m_in.read() == 'l') &&  (m_in.read() == 's') &&  (m_in.read() == 'e')) {
                  return Boolean.FALSE;
               }
               break;
            case 'n':
               if ((m_in.read() == 'u') && (m_in.read() == 'l') && (m_in.read() == 'l')) {
                  return null;
               }
               break;
            case '"':
               return getStringValue();
            default:
               return getNumberValue(ch); // Double or Long
            }
         }
         return EOF;
      }

      private java.lang.Object getNumberValue(int ch) throws Exception, IOException
      {
         int ofs = m_ofs;
         if (((ch >= '0') && (ch <= '9')) || (ch == '-')) {
            StringBuilder sb = new StringBuilder(30);
            do {
               sb.append((char)ch);
            }while (((ch=m_in.read()) != -1) && (" ,\n\r\t]}".indexOf(ch) == -1));
            m_chBack = ch;
            String val = sb.toString();
            try {
               return new Long(Long.parseLong(val));
            }catch (java.lang.Exception e) {
               try {
                  return new Double(Double.parseDouble(val));
               }catch (java.lang.Exception e2) {}
            }
         }
         throw new Exception("Invalid token", ofs);
      }

      private String getStringValue() throws Exception, IOException
      {
         StringBuilder sb = new StringBuilder();
         int ofs = m_ofs;
         int ch;
         while ((ch=m_in.read()) != -1) {
            switch (ch) {
            case '\"':
               return sb.toString();
            case '\\':
               if ((ch=m_in.read()) == -1) {
                  throw new Exception("Quote not ended", ofs);
               }
               switch (ch) {
               case '\"':
               case '\\':
               case '/':
                  sb.append((char)ch);
                  break;
               case 'b':
                  sb.append('\b');
                  break;
               case 'f':
                  sb.append('\f');
                  break;
               case 'r':
                  sb.append('\r');
                  break;
               case 'n':
                  sb.append('\n');
                  break;
               case 't':
                  sb.append('\t');
                  break;
               case 'u':
                  {
                     int val = 0;
                     for (int i=0; i < 4; ++i) {
                        if ((ch=m_in.read()) == -1) {
                           throw new Exception("Quote not ended", ofs);
                        }
                        if ((ch <= '9') && (ch >= '0')) {
                           val = (val << 4) + (ch & 0xF);
                        }else if (((ch=(char)((ch & ~('a'-'A'))-'A')) < (char)6)) {
                           val += (val << 4) + (ch + 10);
                        }else {
                           throw new Exception("Invalid hex digits", m_ofs-i);
                        }
                     }
                     sb.append((char)val);
                  }
                  break;
               }
               break;
            default:
               if (ch < ' ') throw new Exception("Invalid character");
               sb.append((char)ch);
            }
         }
         throw new Exception("Quote not ended", m_ofs);
      }

      private class Exception extends java.lang.Exception {
         private static final long serialVersionUID = 1L;
         Exception(String reason) {
            this(reason, m_ofs);
         }
         Exception(String reason, int pos) {
            super(reason + " at offset " + pos);
         }
      }
   }

   private static Object parseObject(Tokenizer t) throws Exception {
      Object obj = new Object();
      java.lang.Object name = t.nextToken();
      if (name != Tokenizer.END_OBJECT) {
         for (;;) {
            if (name instanceof String) {
               if (t.nextToken() == Tokenizer.NAME_SEPARATOR) {
                  java.lang.Object value = t.nextToken();
                  if (
                     (value == null) ||
                     (value instanceof Double) ||
                     (value instanceof Long) ||
                     (value instanceof String) ||
                     (value instanceof Boolean)
                  ) {
                     obj.members.add(new Member((String)name, value));
                  }else if (value == Tokenizer.BEGIN_OBJECT) {
                     obj.members.add(new Member((String)name, parseObject(t)));
                  }else if (value == Tokenizer.BEGIN_ARRAY) {
                     obj.members.add(new Member((String)name, parseArray(t)));
                  }else {
                     throw new Exception("Value expected");
                  }
                  if ((value = t.nextToken()) == Tokenizer.VALUE_SEPARATOR) {
                     name = t.nextToken();
                  }else if (value != Tokenizer.END_OBJECT) {
                     throw new Exception("Value separator expected");
                  }else {
                     break;
                  }
               }else {
                  throw new Exception("Name separator expected");
               }
            }else {
               throw new Exception("Name expected");
            }
         }
      }
      return obj;
   }

   private static Array parseArray(Tokenizer t) throws Exception {
      Array array = new Array();
      java.lang.Object value = t.nextToken();
      if (value != Tokenizer.END_ARRAY) {
         for (;;) {
            if (
               (value == null) ||
               (value instanceof Double) ||
               (value instanceof Long) ||
               (value instanceof String) ||
               (value instanceof Boolean)
            ) {
               array.values.add(value);
            }else if (value == Tokenizer.BEGIN_OBJECT) {
               array.values.add(parseObject(t));
            }else if (value == Tokenizer.BEGIN_ARRAY) {
               array.values.add(parseArray(t));
            }else {
               throw new Exception("Value expected");
            }
            if ((value = t.nextToken()) == Tokenizer.VALUE_SEPARATOR) {
               value = t.nextToken();
            }else if (value != Tokenizer.END_ARRAY) {
               throw new Exception("Value separator expected");
            }else {
               break;
            }
         }
      }
      return array;
   }

   private static void generate(Object parent, Writer out) throws IOException {
      out.write(Tokenizer.BEGIN_OBJECT);
      boolean isContinuation = false;
      for (Member member : parent.members) {
         if (isContinuation) {
            out.write(Tokenizer.VALUE_SEPARATOR);
         }else {
            isContinuation = true;
         }
         generateString(member.getKey(), out);
         out.write(Tokenizer.NAME_SEPARATOR);
         generateValue(member.getValue(), out);
      }
      out.write(Tokenizer.END_OBJECT);
   }

   private static void generate(Array parent, Writer out) throws IOException {
      out.write(Tokenizer.BEGIN_ARRAY);
      boolean isContinuation = false;
      for (java.lang.Object value : parent.values) {
         if (isContinuation) {
            out.write(Tokenizer.VALUE_SEPARATOR);
         }else {
            isContinuation = true;
         }
         generateValue(value, out);
      }
      out.write(Tokenizer.END_ARRAY);
   }

   private static void generateValue(java.lang.Object value, Writer out) throws IOException {
      if (value == null) {
         out.write("null");
      }else if (
         (value instanceof Double) ||
         (value instanceof Long) ||
         (value instanceof Boolean)
      ) {
         out.write(value.toString());
      }else if (value instanceof String) {
         generateString((String)value, out);
      }else if (value instanceof Object) {
         generate((Object)value, out);
      }else if (value instanceof Array) {
         generate((Array)value, out);
      }
   }

   private static void generateString(String s, Writer out) throws IOException {
      out.write("\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"");
   }
}
/*===========================================================================*/
