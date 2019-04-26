package com.geocento.webapps.earthimages.emis.common.server.utils.hubspot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;

import java.io.IOException;

public class PropertiesDeserializer extends KeyDeserializer {
 
  @Override
  public Property deserializeKey(String key, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      return new Property(key);
    }
}