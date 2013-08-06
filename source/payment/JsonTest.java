/*
* $Id: $
*
* (C) Copyright 2013 Jaxo Inc.
*
* Mozilla Public License 2.0
*
* Author:  Pierre G. Richard
* Written: 4/22/2013
*/
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.List;

/*-- class JsonTest --+
*//**
* Basic example for using the Json class.
*
* @author  Pierre G. Richard
* @version $Id: $
*/
class JsonTest {
   public static void main(String[] args) {
      try {
//       Json.Root root = Json.parse(new java.io.FileReader("sample.json"));
         Json.Root root = Json.parse(new StringReader(sample));
         System.out.println("Fax: " + getFaxNumber(root));

         Writer out = new OutputStreamWriter(System.out);
         Json.generate(root, out);
         out.close();
      }catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static String getFaxNumber(Json.Root root) {
      String type = "";
      String number = "???";
      for (Json.Member member : ((Json.Object)root).members) {
         if (member.getKey().equals("phoneNumbers")) {
            for (Object phones : ((Json.Array)member.getValue()).values) {
               for (Json.Member phone : ((Json.Object)phones).members) {
                  if (phone.getKey().equals("type")) {
                     type = (String)phone.getValue();
                  }else if (phone.getKey().equals("number")) {
                     number = (String)phone.getValue();
                  }
               }
               if ("fax".equals(type)) return number;
            }
         }
      }
      return "???";
   }

   static final String sample = (
      "{\n" +
      "  \"firstName\": \"John\",\n" +
      "  \"lastName\": \"Smith\",\n" +
      "  \"age\": 25,\n" +
      "  \"address\": {\n" +
      "     \"streetAddress\": \"21 2nd Street\",\n" +
      "     \"city\": \"New York\",\n" +
      "     \"state\": \"NY\",\n" +
      "     \"postalCode\": 10021\n" +
      "  },\n" +
      "  \"phoneNumbers\": [\n" +
      "     {\n" +
      "       \"type\": \"home\",\n" +
      "       \"number\": \"212 555-1234\"\n" +
      "     },\n" +
      "     {\n" +
      "       \"type\": \"fax\",\n" +
      "       \"number\": \"646 555-4567\"\n" +
      "     }\n" +
      "  ]\n" +
      "}\n"
   );
}
