package org.jboss.resteasy.test.skeleton.key;

import junit.framework.Assert;
import org.jboss.resteasy.jose.jws.JWSBuilder;
import org.jboss.resteasy.jose.jws.JWSInput;
import org.jboss.resteasy.jose.jws.crypto.RSAProvider;
import org.jboss.resteasy.jwt.JWTContextResolver;
import org.jboss.resteasy.jwt.JsonSerialization;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.skeleton.key.SkeletonKeyToken;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class SkeletonKeyTokenTest
{
   @Test
   public void testToken() throws Exception
   {
      SkeletonKeyToken token = new SkeletonKeyToken();
      token.id("111");
      token.addAccess("foo").addRole("admin");
      token.addAccess("bar").addRole("user");

      String json = JsonSerialization.toString(token, true);
      System.out.println(json);

      token = JsonSerialization.fromString(SkeletonKeyToken.class, json);
      Assert.assertEquals("111", token.getId());
      SkeletonKeyToken.Access foo = token.getServiceToken("foo");
      Assert.assertNotNull(foo);
      Assert.assertTrue(foo.isUserInRole("admin"));

   }

   @Test
   public void testRSA() throws Exception
   {
      SkeletonKeyToken token = new SkeletonKeyToken();
      token.id("111");
      token.addAccess("foo").addRole("admin");
      token.addAccess("bar").addRole("user");

      KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
      byte[] tokenBytes = JsonSerialization.toByteArray(token, true);

      String encoded = new JWSBuilder()
              .content(tokenBytes)
              .rsa256(keyPair.getPrivate());

      System.out.println(encoded);

      JWSInput input = new JWSInput(encoded);
      byte[] content = input.getContent();

      token = JsonSerialization.fromBytes(SkeletonKeyToken.class, content);
      Assert.assertEquals("111", token.getId());
      Assert.assertTrue(RSAProvider.verify(input, keyPair.getPublic()));
   }
}
