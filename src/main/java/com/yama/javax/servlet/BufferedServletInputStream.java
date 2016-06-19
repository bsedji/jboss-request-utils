package com.yama.javax.servlet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;

public class BufferedServletInputStream extends ServletInputStream
{
  InputStream in;
  byte[] inputBuffer;

  BufferedServletInputStream(HttpServletRequest request) {
    duplicateInputStream(request);
  }

  @Override
  public int read() throws IOException {
    return in.read();
  }

  private void duplicateInputStream(HttpServletRequest request) {
    ServletInputStream originalSIS = null;
    try {
      originalSIS = request.getInputStream();
      inputBuffer = consumeBufferAndReturnAsByteArray(originalSIS);
      this.in = new ByteArrayInputStream(inputBuffer);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      closeStrean(originalSIS);
    }
  }

  byte[] consumeBufferAndReturnAsByteArray(InputStream is) throws IOException {
    int len = 1024;
    byte[] temp = new byte[len];
    int c = -1;
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    while ((c = is.read(temp, 0, len)) != -1) {
      baos.write(temp, 0, c);
    }
    return baos.toByteArray();
  }

  void closeStrean(ServletInputStream is) {
    if (is != null) {
      try {
        is.close();
      } catch (IOException e) {
      }
    }
  }

  byte[] getInputBuffer() {
    return inputBuffer;
  }
}
