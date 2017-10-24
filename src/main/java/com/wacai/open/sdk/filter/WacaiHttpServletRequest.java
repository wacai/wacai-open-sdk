package com.wacai.open.sdk.filter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;

public class WacaiHttpServletRequest extends HttpServletRequestWrapper {

  private byte[] body;

  WacaiHttpServletRequest(HttpServletRequest request) {
    super(request);

    try {
      body = IOUtils.toByteArray(request.getInputStream());
    } catch (IOException ex) {
      body = new byte[0];
    }
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return new BufferedReader(new InputStreamReader(getInputStream()));
  }

  @Override
  public ServletInputStream getInputStream() throws IOException {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(body);
    return new ServletInputStream() {
      @Override
      public int read() throws IOException {
        return byteArrayInputStream.read();
      }
    };
  }
}
