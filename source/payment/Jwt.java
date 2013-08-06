/*
* $Id: $
*
* (C) Copyright 2013 Jaxo Inc.  All rights reserved.
*
* Mozilla Public License 2.0
*
* Author:  Pierre G. Richard
* Written: 4/12/2013
*/
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/*-- class Jwt --+
*//**
*
* @author  Pierre G. Richard
* @version $Id: $
*/
class Jwt {
   static final boolean TEST_MODE = true;        // REMOVE ME when going prod
   static final boolean PAYMENT_GRANTED = true;  // REMOVE ME when going prod
   private static final String ENC = "UTF-8";
   private static final String ALGO = "HmacSHA256";
   private static final String HDR_SHA_256 = "{\"typ\":\"JWT\",\"alg\":\"HS256\"}";

   static String payKey = "?";
   static String paySecret = "?";
   static final String payKeySimulated = "11111111-2222-3333-9999-deadbeef4321";
   static final String paySecretSimulated = "123456789abcdef123456789abcdef123456789abcdef123456789abcdef123456789abcdef123456789abcdef123456";
   static final String productName = "MagicMystic";
   static final String productId = "aaaaaaaa-1111-bbbb-2222-cccccccccccc";
   static final String productDescr = "Adventure In the Middle of Nowhere";
   static final int productPrice = 1;
   static final long TEN_YEARS_AS_SECS = 10 * 366 * 24 * 60 * 60;

   /*----------------------------------------------------------------base64Url-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private String base64Url(String in) throws Exception {
      return base64Url(in.getBytes(ENC));
   }

   /*----------------------------------------------------------------base64Url-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private String hashKey(String in) throws Exception {
      Mac mac = Mac.getInstance(ALGO);
      mac.init(new SecretKeySpec(paySecret.getBytes(ENC), ALGO));
      return base64Url(mac.doFinal(in.getBytes(ENC)));
   }

   /*----------------------------------------------------------------base64Url-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private String base64Url(byte[] inBuf) throws Exception {
      return new String(Base64.Url.encode(inBuf), ENC);
   }

   /*------------------------------------------------------------------encode-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private String encode(String claim) throws Exception {
      StringBuilder sb = new StringBuilder();
      sb.append(base64Url(HDR_SHA_256)).append('.').append(base64Url(claim));
      String sig = hashKey(sb.toString());
      return sb.append('.').append(sig).toString();
   }

   /*------------------------------------------------------------------decode-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static private String decode(String response) throws Exception {
      String[] ar = response.split("\\.", 3);
      if (
         !ar[2].equals(
            hashKey(
               new StringBuilder().append(ar[0]).append('.').append(ar[1]).
               toString()
            )
         )
      ) {
         throw new Exception("Invalid Signature");
      }
      return new String(Base64.Url.decode(ar[1].getBytes(ENC)), ENC);
   }

   /*-------------------------------------------------------makePurchaseOrder-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static public String makePurchaseOrder(String userId, String backUrl)
   throws Exception
   {
      String simulate = "";
      if (TEST_MODE) {
         payKey = payKeySimulated;
         paySecret = paySecretSimulated;
         StringBuilder sb = new StringBuilder("\n  ,\"simulate\": { \"result\": \"");
         if (PAYMENT_GRANTED) {      // 0:denied, 1:granted,
            sb.append("postback");
         }else {
            sb.append("chargeback").append("\", \"reason\": \"").append("some reason");
         }
         sb.append("\" }");
         simulate = sb.toString();
      }
      String claim = (
         "{\n\"iss\": \"" + payKey + "\"" +
         ",\n\"aud\": \"marketplace.firefox.com\"" +
         ",\n\"typ\": \"mozilla/payments/pay/v1\"" +
         ",\n\"iat\": " + (System.currentTimeMillis() / 1000L) +
         ",\n\"exp\": " + (TEN_YEARS_AS_SECS + (System.currentTimeMillis() / 1000L)) +
         ",\n\"request\": {" +
         "\n  \"id\": \"" + productId + "\"" +
         ",\n  \"pricePoint\":" + productPrice +
         ",\n  \"name\": \"" + productName + "\"" +
         ",\n  \"description\": \"" + productDescr + "\"" +
         ",\n  \"productData\": \"" + userId + "\"" +
         ",\n  \"postbackURL\": \"" + backUrl + "YES\"" +
         ",\n  \"chargebackURL\": \"" + backUrl + "NO\"" +
         simulate +
         "\n  }" +
         "\n}"
      );
      return encode(claim);
   }

   /*--------------------------------------------------------getPaymentNotice-+
   *//**
   *//*
   +-------------------------------------------------------------------------*/
   static public String getPaymentNotice(String notice) throws Exception {
      return decode(notice);
   }
// static public void main(String[] args) {
//    try {
//       System.out.println(
//          makePurchaseOrder(
//             "abracadabra-12345",
//             "https://www.jaxo.com/magicmystic&agree=",
//             null
//          )
//       );
//    }catch (Exception e) {
//    }
// }
}

/*==========================================================================*/
