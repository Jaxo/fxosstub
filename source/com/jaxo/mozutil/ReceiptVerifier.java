/*
* (C) Copyright 2013 Jaxo Inc.
*
* This Source Code Form is subject to the terms of the Mozilla Public
* License, v. 2.0.  If a copy of the MPL was not distributed with this
* file, you can obtain one at http://mozilla.org/MPL/2.0/.
*
* Author:  Pierre G. Richard
* Written: 7/10/2013
*/
package com.jaxo.mozutil;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*-- class ReceiptVerifier --+
*//**
* A verifier for pre-paid mozApps.
*
* @author  Pierre G. Richard
*/
public class ReceiptVerifier {

   /*------------------------------------------------------------------verify-+
   *//**
   * Verify whether an Application has the proper payment receipt.
   * The String <code>app</code> is a JSON stringified Mozilla App object.
   *
   * For example, <code>window.navigator.mozApps.getSelf()</code> calls
   * <code>onsuccess</code> passing such an <code>App<code> object
   * in the <code>result</code> field of the <code>DomRequest</code> argument.
   *
   * @param app JSON stringified Mozilla App Object to check the receipt of
   * @return a ReceiptVerifier.Status enumeration value (see below)
   *//*
   +-------------------------------------------------------------------------*/
   static public Status verify(String app)
   {
      Status status = Status.ERROR;
      try {
         Json.Root appRoot = Json.parse(new StringReader(app));
         for (Json.Member appMbr : ((Json.Object)appRoot).members) {
            if (appMbr.getKey().equals("receipts")) {
               for (Object value : ((Json.Array)appMbr.getValue()).values) {
                  status = Status.getBestOf(
                     checkReceipt((String)value), status
                  );
                  if (status == Status.OK) return status;
               }
            }
         }
      }catch (Exception e) {}
      return status;
   }

   /*------------------------------------------------------------checkReceipt-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private Status checkReceipt(String jwt)
   {
      try {
         // split up the JWT to get the part that contains the receipt
         int ix;
         ix = jwt.indexOf('~');
         if (ix == -1) {
            return Status.BAD_JWT;
         }
         String subJwt = jwt.substring(1+jwt.indexOf('.', ix+1));
         ix = subJwt.indexOf('.');
         if (ix == -1) {
            ix = subJwt.indexOf('~');
            if (ix == -1) {
               ix = subJwt.length();
            }
         }
         Json.Root rcpRoot = Json.parse(
            new StringReader(Base64.Url.decode(subJwt.substring(0, ix)))
         );
         ReceiptType rcpType = null;
         String storeUrl = null;
         Date issuedAt = null;
         String verifierUrl = null;
         for (Json.Member rcpMbr : ((Json.Object)rcpRoot).members) {
            String key = rcpMbr.getKey();
            if (key.equals("typ")) {
               rcpType = ReceiptType.make((String)rcpMbr.getValue());
            }else if (key.equals("iss")) {
               storeUrl = (String)rcpMbr.getValue();
            }else if (key.equals("iat")) {
               // currently, we do nothing with it
               issuedAt = new Date((Long)rcpMbr.getValue());
            }else if (key.equals("verify")) {
               verifierUrl = (String)rcpMbr.getValue();
            }
         }
         if (
            (rcpType == null) ||
            !rcpType.isAllowed() ||
            (issuedAt == null) ||
            (storeUrl == null) ||
            (verifierUrl == null) ||
            !isSubdomain(storeUrl, verifierUrl)
         ) {
            return Status.BAD_FIELDS;
         }else {
            Status status = getVerifierStatus(
               post(new URL(verifierUrl), jwt.getBytes("UTF-8"))
            );
            if ((status == Status.OK) && (rcpType == ReceiptType.TEST)) {
               return Status.TESTONLY;
            }else {
               return status;
            }
         }
      }catch (Exception e) {
         return Status.ERROR;
      }
   }

   /*---------------------------------------------------------------getDomain-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static Pattern pat = Pattern.compile("^https?:\\/\\/(.*?)(([:\\/].*)|$)");
   private static String getDomain(String url) {
      Matcher matcher = pat.matcher(url);
      matcher.matches();
      return matcher.group(1).toLowerCase();
   }

   /*-------------------------------------------------------------isSubdomain-+
   *//**
   * Returns true if url2 is the same as url1, or a subdomain of it,
   * irregardless of http/https protocol, and/or port
   *
   * @param url1 main url
   * @param url2 url the domain or subdomain of which
   *             must match the domain of <code>url1</code>
   * @return true if url2 is the same as url1, or a subdomain of it
   * @exception IllegalStateException or IndexOutOfBoundsException
   *//*
   +-------------------------------------------------------------------------*/
   private static boolean isSubdomain(String url1, String url2) {
      return getDomain(url2).endsWith(getDomain(url1));
   }

   /*--------------------------------------------------------------------post-+
   *//**
   * Post the full JWT to the verifier URL and returns the JSON response
   *//*
   +-------------------------------------------------------------------------*/
   private static Json.Root post(URL verifierUrl, byte[] jwt) throws Exception
   {
      HttpURLConnection conn = null;
      try {
         conn = (HttpURLConnection)verifierUrl.openConnection();
         conn.setRequestMethod("POST");
         conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
         conn.setRequestProperty("Content-Length", Integer.toString(jwt.length));
         conn.setDoInput(true);
         conn.setDoOutput(true);
         OutputStream out = conn.getOutputStream();
         out.write(jwt);
         out.close();
         return Json.parse(
            new InputStreamReader(conn.getInputStream(), "UTF-8")
         );
      }finally {
         if (conn != null) conn.disconnect();
      }
   }

   /*-------------------------------------------------------getVerifierStatus-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   private static Status getVerifierStatus(Json.Root root)
   throws Exception
   {
      for (Json.Member mbr : ((Json.Object)root).members) {
         if (mbr.getKey().equals("status")) {
            return Status.make((String)mbr.getValue());
         }
      }
      return Status.UNKNOWN;
   }

   /*----------------------------------------------------------- enum Status -+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   public static enum Status {
      OK("ok", true, 20),
      PENDING("pending", true, 18),
      REFUNDED("refunded", true, 16),
      EXPIRED("expired", true, 14),
      INVALID("invalid", true, 12),
      UNKNOWN(null, true, 10),
      BAD_JWT("this receipt is unreadable", false, 4),
      BAD_FIELDS("this receipt cannot be verified", false, 3),
      TESTONLY("this receipt is only valid for tests", false, 2),
      ERROR("uncorrectable errors found", false, 1);

      private String m_name;
      private int m_rank;
      private boolean m_isFromStore;
      private static Map<String, Status> m_fromName = new HashMap<String, Status>();
      static {
         for (Status t : values()) {
            if (t.m_isFromStore) m_fromName.put(t.m_name, t);
         }
      }
      private Status(String name, boolean isFromStore, int rank) {
         m_isFromStore = isFromStore;
         m_name = name;
         m_rank = rank;
      }
      public static Status make(String name) {
         Status status = m_fromName.get(name);
         return (status == null)? UNKNOWN : status;
      }
      public static Status getBestOf(
         Status status1, Status status2
      ) {
         return (status1.m_rank > status2.m_rank)? status1 : status2;
      }
      public String toString() {
         if (m_isFromStore) {
            return("Issuer said: \"" + ((m_name!=null)? m_name : "?") + "\"");
         }else {
            return(m_name);
         }
      }
   }

   /*------------------------------------------------------ enum ReceiptType -+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   private static enum ReceiptType {
      PURCHASE("purchase-receipt", true),
      DEVELOPER("developer-receipt", true),
      REVIEWER("reviewer-receipt", true),
      TEST("test-receipt", true),
      UNKNOWN(null, false);

      private String m_name;
      private boolean m_isAllowed;
      private static Map<String, ReceiptType> m_fromName = new HashMap<String, ReceiptType>();
      static {
         for (ReceiptType t : values()) m_fromName.put(t.m_name, t);
      }
      private ReceiptType(String name, boolean isAllowed) {
         m_name = name;
         m_isAllowed = isAllowed;
      }
      public boolean isAllowed() {
         return m_isAllowed;
      }
      public static ReceiptType make(String name) {
         ReceiptType type = m_fromName.get(name);
         return (type == null)? UNKNOWN : type;
      }
   }
}
/*===========================================================================*/
